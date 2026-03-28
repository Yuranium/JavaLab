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

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(minioConfig.getBucketName())
                    .key(key)
                    .contentType(file.getContentType())
                    .metadata(Map.of("upload-timestamp", Instant.now().toString()))
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
        return transliterate(Arrays.stream(fileName.split("\\."))
                .map(str -> str.isBlank() ? "unnamed" : str)
                .collect(Collectors.joining("."))
                .replaceAll(" ", "_"));
    }

    private String transliterate(String text)
    {
        if (text == null) return null;
        StringBuilder sb = new StringBuilder(text.length());

        for (int i = 0; i < text.length(); i++)
        {
            char c = text.charAt(i);
            sb.append(switch (c)
            {
                case 'а' -> "a"; case 'А' -> "A";
                case 'б' -> "b"; case 'Б' -> "B";
                case 'в' -> "v"; case 'В' -> "V";
                case 'г' -> "g"; case 'Г' -> "G";
                case 'д' -> "d"; case 'Д' -> "D";
                case 'е' -> "e"; case 'Е' -> "E";
                case 'ё' -> "yo"; case 'Ё' -> "Yo";
                case 'ж' -> "zh"; case 'Ж' -> "Zh";
                case 'з' -> "z"; case 'З' -> "Z";
                case 'и' -> "i"; case 'И' -> "I";
                case 'й' -> "y"; case 'Й' -> "Y";
                case 'к' -> "k"; case 'К' -> "K";
                case 'л' -> "l"; case 'Л' -> "L";
                case 'м' -> "m"; case 'М' -> "M";
                case 'н' -> "n"; case 'Н' -> "N";
                case 'о' -> "o"; case 'О' -> "O";
                case 'п' -> "p"; case 'П' -> "P";
                case 'р' -> "r"; case 'Р' -> "R";
                case 'с' -> "s"; case 'С' -> "S";
                case 'т' -> "t"; case 'Т' -> "T";
                case 'у' -> "u"; case 'У' -> "U";
                case 'ф' -> "f"; case 'Ф' -> "F";
                case 'х' -> "h"; case 'Х' -> "H";
                case 'ц' -> "c"; case 'Ц' -> "C";
                case 'ч' -> "ch"; case 'Ч' -> "Ch";
                case 'ш' -> "sh"; case 'Ш' -> "Sh";
                case 'щ' -> "sch"; case 'Щ' -> "Sch";
                case 'ъ' -> ""; case 'Ъ' -> "";
                case 'ы' -> "y"; case 'Ы' -> "Y";
                case 'ь' -> ""; case 'Ь' -> "";
                case 'э' -> "e"; case 'Э' -> "E";
                case 'ю' -> "yu"; case 'Ю' -> "Yu";
                case 'я' -> "ya"; case 'Я' -> "Ya";
                default -> String.valueOf(c);
            });
        }
        return sb.toString();
    }
}