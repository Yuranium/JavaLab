package com.wenn.notificationservice.client;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceClient.class);

    private final RestTemplate restTemplate;

    @Value("${services.user-service.url}")
    private String userServiceUrl;

    public List<String> getEmails(int page, int size) {
        try {

            String url = userServiceUrl;

            ResponseEntity<List<String>> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<>() {},
                            page,
                            size
                    );

            return response.getBody() == null ? Collections.emptyList() : response.getBody();

        } catch (Exception ex) {
            log.error("Failed to fetch emails from user service: {}", ex.getMessage(), ex);
            return Collections.emptyList();
        }
    }
}
