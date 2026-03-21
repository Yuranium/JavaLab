package com.yuranium.userservice.config.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutBucketPolicyRequest;

@Component
@RequiredArgsConstructor
public class MinioPolicyInitializer implements CommandLineRunner
{
    private final S3Client s3Client;

    private final MinioConfig minioConfig;

    @Value("${s3.avatar-prefix}")
    private String prefix;

    @Override
    public void run(String... args)
    {
        String policyJson = """
            {
              "Version": "2012-10-17",
              "Statement": [
                {
                  "Effect": "Allow",
                  "Principal": { "AWS": ["*"] },
                  "Action": ["s3:GetObject"],
                  "Resource": ["arn:aws:s3:::%s/*"]
                }
              ]
            }
            """.formatted(minioConfig.getBucketName() + "/" + prefix);

        s3Client.putBucketPolicy(PutBucketPolicyRequest.builder()
                .bucket(minioConfig.getBucketName())
                .policy(policyJson)
                .build());
    }
}