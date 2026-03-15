package com.wenn.notificationservice.client;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceClient.class);

    private final RestClient restClient;

    @Value("${services.user-service.url}")
    private String userServiceUrl;

    public List<String> getEmails(int page, int size) {
        try {

            String[] emails = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(userServiceUrl)
                            .queryParam("page", page)
                            .queryParam("size", size)
                            .build())
                    .retrieve()
                    .body(String[].class);

            return emails == null ? Collections.emptyList() : List.of(emails);

        } catch (Exception ex) {
            log.error("Failed to fetch emails from user service: {}", ex.getMessage(), ex);
            return Collections.emptyList();
        }
    }
}
