package com.yuranium.userservice.config.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
@RequiredArgsConstructor
public class S3Config
{
    private final BackblazeConfig backblazeConfig;

    @Bean
    public S3Client s3Client()
    {
        return S3Client.builder()
                .endpointOverride(URI.create(backblazeConfig.getEndpoint()))
                .region(Region.of(backblazeConfig.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                backblazeConfig.getAccessKey(),
                                backblazeConfig.getApplicationKey()
                        )
                ))
                .build();
    }
}