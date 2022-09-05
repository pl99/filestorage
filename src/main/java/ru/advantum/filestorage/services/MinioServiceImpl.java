package ru.advantum.filestorage.services;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.advantum.filestorage.converters.BucketConverter;
import ru.advantum.filestorage.converters.S3ObjDtoConverter;
import ru.advantum.filestorage.dto.BucketDto;
import ru.advantum.filestorage.dto.S3ObjDto;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service("minio")
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class MinioServiceImpl implements AmazonS3Service {
    MinioClient client;
    S3ObjDtoConverter s3ObjDtoConverter;
    BucketConverter bucketConverter;

    @Override
    public S3ObjDto uploadFile(String bucketName, MultipartFile multipartFile) {
        return uploadFile(bucketName, multipartFile, "");
    }

    @Override
    public S3ObjDto uploadFile(String bucketName, MultipartFile multipartFile, String prefix) {
        createBucketIfNotExists(bucketName);
        return getPutObjectResult(bucketName, multipartFile, prefix);
    }

    @Override
    public byte[] downloadFile(String bucketName, String fileUrl) {
        return getFile(bucketName, fileUrl);
    }

    @SneakyThrows
    @Override
    public void deleteFile(String bucketName, String fileUrl) {
        client.removeObject(RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(fileUrl)
                .build());
    }

    @SneakyThrows
    @Override
    public List<S3ObjDto> listFiles(String bucketName) {
        Iterable<Result<Item>> results = client.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName)
                .build());
        return StreamSupport.stream(results.spliterator(), false)
                .map(it -> {
                    try {
                        return it.get();
                    } catch (ErrorResponseException |
                            InsufficientDataException |
                            InternalException |
                            InvalidKeyException |
                            InvalidResponseException |
                            IOException |
                            NoSuchAlgorithmException |
                            ServerException |
                            XmlParserException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(s3ObjDtoConverter::toDto)
                .map(it->it.toBuilder().bucketName(bucketName).build())
                .collect(Collectors.toList());
    }

    @SneakyThrows
    @Override
    public byte[] getFile(String bucketName, String key) {
        GetObjectResponse obj = client.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(key)
                .build());
        byte[] content = obj.readAllBytes();
        Headers headers = obj.headers();
        log.info("{}", headers);
        obj.close();
        return content;
    }



    @SneakyThrows
    @Override
    public List<BucketDto> listBuckets() {
        List<Bucket> buckets = client.listBuckets();
        return buckets.stream()
                .map(it -> BucketDto.builder()
                        .creationDate(it.creationDate().toInstant())
                        .name(it.name())
                        .build()
                )
                .collect(Collectors.toList());
    }

    @SneakyThrows
    private void createBucketIfNotExists(String bucketName) {
        if (!client.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            MakeBucketArgs bucketArgs = MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build();
            client.makeBucket(bucketArgs);
            log.info("Bucket {} was created at {}", bucketArgs.bucket(), Instant.now());
        }
    }

    @SneakyThrows
    private S3ObjDto getPutObjectResult(String bucketName, MultipartFile multipartFile, String prefix) {
        String folder = "";
        if (null != prefix && !prefix.isEmpty()) {
            folder = prefix + '/';
        }
        String key = folder + multipartFile.getOriginalFilename();
        ObjectWriteResponse result = client.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(key)
                .stream(multipartFile.getInputStream(), multipartFile.getSize(), -1)
                .contentType(multipartFile.getContentType())
                .build()
        );
        return s3ObjDtoConverter.toDto(result);
    }

}
