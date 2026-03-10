package com.yuranium.userservice.config;

import lombok.Getter;
import lombok.Setter;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "keycloak.admin")
public class KeycloakConfig
{
    private String url;

    private String realm;

    private String currentRealm;

    private String username;

    private String password;

    private String clientId;

    @Bean
    public Keycloak adminKeycloak()
    {
        return KeycloakBuilder.builder()
                .serverUrl(url)
                .realm(realm)
                .username(username)
                .password(password)
                .clientId(clientId)
                .build();
    }
}