package com.yuranium.userservice.service;

import com.yuranium.userservice.config.s3.BackblazeConfig;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService
{
    private final String AVATAR_FOLDER = "user-avatars";

    private final S3Client s3Client;

    private final BackblazeConfig backblazeConfig;

    @SneakyThrows
    public String uploadFile(MultipartFile file)
    {
        if (file == null || file.isEmpty())
            return null;

        String key = AVATAR_FOLDER + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        Map<String, String> metadata = new HashMap<>();
        metadata.put("original-filename", file.getOriginalFilename());
        metadata.put("upload-timestamp", LocalDateTime.now().toString());

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(backblazeConfig.getBucketName())
                .key(key)
                .contentType(file.getContentType())
                .metadata(metadata)
                .build();

        s3Client.putObject(putObjectRequest,
                RequestBody.fromBytes(file.getBytes()));

        return generateFileUrl(key);
    }

//    public byte[] downloadFile(String fileKey)
//    {
//        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
//                .bucket(backblazeConfig.getBucketName())
//                .key(fileKey)
//                .build();
//
//        ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
//        return objectBytes.asByteArray();
//    }

    private String generateFileUrl(String fileKey)
    {
        return String.format("https://%s.%s/%s",
                backblazeConfig.getBucketName(),
                backblazeConfig.getEndpoint().replace("https://", ""),
                fileKey);
    }
}