package com.yuranium.userservice.config.s3;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "backblaze.b2")
public class BackblazeConfig
{
    private String endpoint;
    private String region;
    private String bucketName;
    private String accessKey;
    private String applicationKey;
}