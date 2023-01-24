package com.microservices.demo.elastic.config;

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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.microservices.demo.elastic.index.client.repository")
public class ElasticsearchConfig {
    private final ElasticConfigData elasticConfigData;

    public ElasticsearchConfig(ElasticConfigData configData) {
        this.elasticConfigData = configData;
    }

    @Bean
    public ElasticsearchClient elasticsearchClient() throws IOException {
        final CredentialsProvider credentialsProvider =
                new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials("elastic", "qwerty@123"));

        File certFile = new File("E:\\New folder\\event-deriven microservices\\event-driven-elastic-kafka\\cert\\http_ca.crt");

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
        ElasticsearchClient client = new ElasticsearchClient(new RestClientTransport(
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

    @Bean
    public ElasticsearchOperations elasticsearchTemplate() throws IOException {
        return new ElasticsearchTemplate(elasticsearchClient());
    }
}
