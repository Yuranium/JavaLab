package com.javalab.executionservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Set;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "code-execution")
public class ExecutionConfig
{
    private ValidationConfig validation;

    private DockerContainerConfig docker;

    @Getter
    @Setter
    public static class ValidationConfig
    {
        private boolean enabled;

        private Set<String> allowedPackages;

        private Set<String> forbiddenPackages;

        private Set<String> forbiddenMethods;
    }

    @Getter
    @Setter
    public static class DockerContainerConfig
    {
        private String network;

        private String memory;

        private Duration timeout;

        private int poolSize;
    }
}
