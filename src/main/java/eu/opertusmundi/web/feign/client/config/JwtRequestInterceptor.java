package eu.opertusmundi.web.feign.client.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;

public class JwtRequestInterceptor implements RequestInterceptor {

    private final String jwt;

    public JwtRequestInterceptor(String jwt) {
        this.jwt = jwt;
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header("Authorization", "Bearer " + this.jwt);
    }
}