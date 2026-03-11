package com.wenn.aiservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig
{
    @Bean
    public RestClient restClient(
            OAuth2AuthorizedClientManager authorizedClientManager,
            @Value("${spring.security.oauth2.client.client-registration-name}") String clientRegistrationName
    ) {
        var interceptor = new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);
        interceptor.setClientRegistrationIdResolver(request -> clientRegistrationName);

        return RestClient.builder()
                .requestInterceptor(interceptor)
                .build();
    }
}