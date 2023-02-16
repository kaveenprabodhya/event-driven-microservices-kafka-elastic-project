package com.microservices.demo.elastic.query.web.client.config;

import com.microservices.demo.config.UserConfigData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final UserConfigData userConfigData;

    private static final Logger LOG = LoggerFactory.getLogger(WebSecurityConfig.class);

    private static final String GROUPS_CLAIM = "groups";

    private static final String ROLE_PREFIX = "ROLE_";

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Value("${security.logout-success-url}")
    private String logoutSuccessUrl;

    private LogoutSuccessHandler oidcLogoutSuccessHandler() {
        OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler =
                new OidcClientInitiatedLogoutSuccessHandler(
                        this.clientRegistrationRepository);

        oidcLogoutSuccessHandler.setPostLogoutRedirectUri(logoutSuccessUrl);

        return oidcLogoutSuccessHandler;
    }

    public WebSecurityConfig(UserConfigData userData) {
        this.userConfigData = userData;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/").permitAll()
                        .anyRequest().authenticated())
                .logout()
                .logoutSuccessHandler(oidcLogoutSuccessHandler())
                .and()
                .oauth2Client()
                .and()
                .oauth2Login()
                .userInfoEndpoint()
                .userAuthoritiesMapper(userAuthoritiesMapper());
        return http.build();
    }

    private GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return (authorities) -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

            authorities.forEach(
                    authority -> {
                        if (authority instanceof OidcUserAuthority) {
                            OidcUserAuthority oidcUserAuthority = (OidcUserAuthority) authority;
                            OidcIdToken oidcIdToken = oidcUserAuthority.getIdToken();
                            LOG.info("Username from id token: {}", oidcIdToken.getPreferredUsername());
                            OidcUserInfo userInfo = oidcUserAuthority.getUserInfo();

                            List<SimpleGrantedAuthority> groupAuthorities =
                                    userInfo.getClaimAsStringList(GROUPS_CLAIM).stream()
                                            .map(group ->
                                                    new SimpleGrantedAuthority(ROLE_PREFIX + group.toUpperCase()))
                                            .collect(Collectors.toList());
                            mappedAuthorities.addAll(groupAuthorities);
                        }
                    });
            return mappedAuthorities;
        };
    }

//    @Bean
//    public UserDetailsService userDetailsService() {
//        UserDetails user = User.builder()
//                .username(userConfigData.getUsername())
//                .password(passwordEncoder().encode(userConfigData.getPassword()))
//                .roles(userConfigData.getRoles())
//                .build();
//        return new InMemoryUserDetailsManager(user);
//    }

    @Bean
    protected PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
