package eu.opertusmundi.web.config;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfiguration {

    private PoolingHttpClientConnectionManager poolingHttpClientConnectionManager;

    @Value("${http-client.maxTotal}")
    private int                                maxTotal;

    @Value("${http-client.maxPerRoute}")
    private int                                maxPerRoute;

    @PostConstruct
    public void init() {
        this.poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
        this.poolingHttpClientConnectionManager.setMaxTotal(this.maxTotal);
        this.poolingHttpClientConnectionManager.setDefaultMaxPerRoute(this.maxPerRoute);
    }

    @PreDestroy
    public void destroy() {
        this.poolingHttpClientConnectionManager.shutdown();
    }

    @Bean
    public HttpClient defaultHttpClient() {
        return HttpClients
            .custom()
            .setConnectionManager(this.poolingHttpClientConnectionManager)
            .build();
    }

}
