package com.javalab.executionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@EnableConfigurationProperties
@SpringBootApplication
public class ExecutionServiceApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(ExecutionServiceApplication.class, args);
    }
}