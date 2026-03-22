package com.yuranium.userservice.service;

import com.javalab.core.exception.ResourceNotCreatedException;
import com.yuranium.userservice.config.s3.MinioConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileService
{
    @Value("${s3.avatar-prefix}")
    private String AVATAR_FOLDER;

    private final S3Client s3Client;

    private final MinioConfig minioConfig;

    public String uploadFile(MultipartFile file)
    {
        if (file == null || file.isEmpty())
            return null;

        try
        {
            String key = generateKey(file.getOriginalFilename());

            Map<String, String> metadata = new HashMap<>();
            metadata.put("original-filename", file.getOriginalFilename());
            metadata.put("upload-timestamp", Instant.now().toString());

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(minioConfig.getBucketName())
                    .key(key)
                    .contentType(file.getContentType())
                    .metadata(metadata)
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromBytes(file.getBytes()));

            return key;
        } catch (Exception e)
        {
            throw new ResourceNotCreatedException(
                    "Failed to upload avatar: " + e.getMessage()
            );
        }
    }

    public String updateFile(String fileName, MultipartFile newFile)
    {
        if (newFile == null || newFile.isEmpty())
            return null;

        try
        {
            String newFileName = uploadFile(newFile);
            if (fileName != null && !fileName.isEmpty())
                deleteFile(fileName);
            return newFileName;
        } catch (Exception e)
        {
            throw new ResourceNotCreatedException("Failed to update avatar: " + e.getMessage());
        }
    }

    public void deleteFile(String fileName)
    {
        if (fileName == null || fileName.isEmpty())
            throw new IllegalArgumentException("parameter 'fileName' cannot be empty or null");

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(minioConfig.getBucketName())
                .key(fileName)
                .build();
        s3Client.deleteObject(deleteObjectRequest);
    }

    private String generateKey(String fileName)
    {
        return AVATAR_FOLDER + "/" + UUID.randomUUID() + "_" + validateFilename(fileName);
    }

    private String validateFilename(String fileName)
    {
        return Arrays.stream(fileName.split("\\."))
                .map(str -> str.isBlank() ? "unnamed" : str)
                .collect(Collectors.joining("."))
                .replaceAll(" ", "_");
    }
}