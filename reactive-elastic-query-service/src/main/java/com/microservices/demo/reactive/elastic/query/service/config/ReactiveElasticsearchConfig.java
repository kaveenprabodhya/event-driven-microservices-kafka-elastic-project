package com.microservices.demo.reactive.elastic.query.service.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.TransportUtils;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.microservices.demo.config.ElasticConfigData;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchClient;
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.repository.config.EnableReactiveElasticsearchRepositories;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

@Configuration
@EnableReactiveElasticsearchRepositories(basePackages = "com.microservices.demo.reactive.elastic.query.service.repository")
public class ReactiveElasticsearchConfig {
    private final ElasticConfigData elasticConfigData;

    @Autowired
    private Environment environment;

    public ReactiveElasticsearchConfig(ElasticConfigData configData) {
        this.elasticConfigData = configData;
    }

    @Bean
    public ReactiveElasticsearchClient reactiveElasticsearchClient() throws IOException {
        final CredentialsProvider credentialsProvider =
                new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(
                        Objects.requireNonNull(environment.getProperty("elastic-username")),
                        environment.getProperty("elastic-pwd")));

        File certFile = new File(Objects.requireNonNull(environment.getProperty("crt-path")));

        SSLContext
                sslContext = TransportUtils
                .sslContextFromHttpCaCrt(certFile);

        UriComponents serverUri = UriComponentsBuilder.fromHttpUrl(elasticConfigData.getConnectionUrl()).build();
        RestClient restClient = RestClient.builder(new HttpHost(
                        Objects.requireNonNull(serverUri.getHost()),
                        serverUri.getPort(),
                        serverUri.getScheme()
                )).setRequestConfigCallback(
                        requestConfigBuilder ->
                                requestConfigBuilder
                                        .setConnectTimeout(elasticConfigData.getConnectTimeoutMs())
                                        .setSocketTimeout(elasticConfigData.getSocketTimeoutMs())
                ).setHttpClientConfigCallback(httpAsyncClientBuilder ->
                        httpAsyncClientBuilder
                                .setSSLContext(sslContext)
                                .setDefaultCredentialsProvider(credentialsProvider))
                .build();
        ReactiveElasticsearchClient client = new ReactiveElasticsearchClient(new RestClientTransport(
                restClient,
                new JacksonJsonpMapper()));

//        StringReader jsonTOString = new StringReader("{\"foo\": \"bar\"}");
//        IndexResponse response = client.index(i -> i
//                .index("twitter")
//                .id(UUID.randomUUID().toString())
//                .withJson(jsonTOString));
//        LOGGER.info(response.id());
        return client;
    }

}
