package com.microservices.demo.elastic.query.service.config;

import com.microservices.demo.config.UserConfigData;
import com.microservices.demo.elastic.query.service.security.QueryServicePermissionEvaluator;
import com.microservices.demo.elastic.query.service.security.TwitterQueryUserDetailsService;
import com.microservices.demo.elastic.query.service.security.TwitterQueryUserJwtConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

//    private final UserConfigData userConfigData;

    @Autowired
    private QueryServicePermissionEvaluator permissionEvaluator;

    private final TwitterQueryUserDetailsService twitterQueryUserDetailsService;
    private final OAuth2ResourceServerProperties oAuth2ResourceServerProperties;
    public WebSecurityConfig(TwitterQueryUserDetailsService twitterQueryUserDetailsService, OAuth2ResourceServerProperties oAuth2ResourceServerProperties) {
        this.twitterQueryUserDetailsService = twitterQueryUserDetailsService;
        this.oAuth2ResourceServerProperties = oAuth2ResourceServerProperties;
    }

    @Value("${security.paths-to-ignore}")
    private String[] pathsToIgnore;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .csrf()
                .disable()
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(pathsToIgnore).permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(twitterQueryUserJwtConverter())
                        ));
        return http.build();
    }

    private Converter<Jwt, ? extends AbstractAuthenticationToken> twitterQueryUserJwtConverter() {
        return new TwitterQueryUserJwtConverter(twitterQueryUserDetailsService);
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
    public JwtDecoder jwtDecoder(@Qualifier("elastic-query-service-audience-validator")
                                     OAuth2TokenValidator<Jwt> audienceValidator){
        NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder) JwtDecoders
                .fromIssuerLocation(oAuth2ResourceServerProperties.getJwt().getIssuerUri());
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators
                .createDefaultWithIssuer(oAuth2ResourceServerProperties.getJwt().getIssuerUri());
        OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<Jwt>(withIssuer, audienceValidator);
        jwtDecoder.setJwtValidator(withAudience);
        return jwtDecoder;
    }

    @Bean
    protected PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setPermissionEvaluator(permissionEvaluator);
        return handler;
    }

}
