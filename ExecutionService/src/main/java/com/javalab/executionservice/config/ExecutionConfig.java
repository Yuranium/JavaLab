package com.javalab.executionservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "code-execution")
public class ExecutionConfig
{
    private ValidationConfig validation;

    private DockerContainerConfig docker;

    private CompilerConfig compiler;

    private TimeoutConfig timeout;

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
        private String compilerImage = "eclipse-temurin:23-jdk";

        private String runtimeImage = "eclipse-temurin:23-jre";

        private String network;

        private String memory;

        private Duration connectionTimeout;

        private Duration responseTimeout;

        private int maxConnections;

        private boolean tlsVerify;

        private String dockerHost;

    }

    @Getter
    @Setter
    public static class CompilerConfig
    {
        private List<String> parameters = List.of(
                "-proc:none", "-encoding", "UTF-8",
                "-source", "17", "-target", "17",
                "-Xlint:-options", "-Xmaxerrs", "10"
        );

        private String defaultUserClassName;

        private String defaultMainMethodName;
    }

    @Getter
    @Setter
    public static class TimeoutConfig
    {
        private Duration compilation = Duration.ofSeconds(30);
        private Duration execution = Duration.ofSeconds(30);
    }
}
