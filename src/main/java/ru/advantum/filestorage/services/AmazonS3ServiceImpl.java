package ru.advantum.filestorage.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.advantum.filestorage.converters.BucketConverter;
import ru.advantum.filestorage.converters.S3ObjDtoConverter;
import ru.advantum.filestorage.dto.BucketDto;
import ru.advantum.filestorage.dto.S3ObjDto;

import java.io.File;
import java.util.List;

@Service("amazon")
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AmazonS3ServiceImpl implements AmazonS3Service {
    AmazonS3 client;
    S3ObjDtoConverter s3ObjDtoConverter;
    BucketConverter bucketConverter;

    @Override
    @SneakyThrows
    public S3ObjDto uploadFile(String bucketName, MultipartFile multipartFile) {
        return uploadFile(bucketName, multipartFile, "");
    }

    @Override
    public S3ObjDto uploadFile(String bucketName, MultipartFile multipartFile, String prefix) {
        createBucketIfNotExists(bucketName);
        S3ObjDto result = getPutObjectResult(bucketName, multipartFile, prefix);

        return result;
    }


    @SneakyThrows
    private S3ObjDto getPutObjectResult(String bucketName, MultipartFile multipartFile, String prefix) {
        File file = upload(bucketName, multipartFile.getOriginalFilename(), multipartFile.getBytes());
        String folder = "";
        if (null != prefix && !prefix.isEmpty()) {
            folder = prefix + '/';
        }
        String key = folder + multipartFile.getOriginalFilename();
        PutObjectRequest request = new PutObjectRequest(bucketName, key, file);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(multipartFile.getContentType());
        log.info("multipartFile.getContentType() - {}", multipartFile.getContentType());
        request.setMetadata(metadata);
        PutObjectResult result = client.putObject(request);
        S3Object object = client.getObject(bucketName, key);
        return s3ObjDtoConverter.toDto(object);
    }

    private void createBucketIfNotExists(String bucketName) {
        if (!client.doesBucketExistV2(bucketName)) {
            Bucket bucket = client.createBucket(bucketName);
            log.info("Bucket {} was created at {}", bucket.getName(), bucket.getCreationDate());
        }
    }


    @Override
    public byte[] downloadFile(String bucketName, String fileUrl) {
        return getFile(bucketName, fileUrl);
    }

    @Override
    public void deleteFile(String bucketName, String fileUrl) {
        client.deleteObject(bucketName, fileUrl);
    }

    @Override
    public List<S3ObjDto> listFiles(String bucketName) {
        ObjectListing listObjects = client.listObjects(bucketName);
        return s3ObjDtoConverter.toDtos(listObjects);
    }

    @SneakyThrows
    @Override
    public byte[] getFile(String bucketName, String key) {
        S3Object obj = client.getObject(bucketName, key);
        S3ObjectInputStream stream = obj.getObjectContent();
        byte[] content = StreamUtils.copyToByteArray(stream);
        obj.close();
        return content;
    }

    @Override
    public List<BucketDto> listBuckets() {
        return bucketConverter.toDtos(client.listBuckets());
    }
}