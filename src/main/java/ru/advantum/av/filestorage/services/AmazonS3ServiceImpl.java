package ru.advantum.av.filestorage.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Service
@Slf4j
public class AmazonS3ServiceImpl implements AmazonS3Service {
    private final AmazonS3 s3;

    @Autowired
    public AmazonS3ServiceImpl(AmazonS3 s3) {
        this.s3 = s3;
    }

    @Override
    @SneakyThrows
    public PutObjectResult uploadFile(String bucketName, MultipartFile multipartFile) {
        return uploadFile(bucketName, multipartFile, "");
    }
    @Override
    public PutObjectResult uploadFile(String bucketName, MultipartFile multipartFile, String prefix) {
        createBucketIfNotExists(bucketName);
        PutObjectResult result = getPutObjectResult(bucketName, multipartFile, prefix);
        boolean doesItExists = s3.doesObjectExist(bucketName, multipartFile.getOriginalFilename());

        return result;
    }


    @SneakyThrows
    private PutObjectResult getPutObjectResult(String bucketName, MultipartFile multipartFile, String prefix) {
        File file = upload(bucketName, multipartFile.getOriginalFilename(), multipartFile.getBytes());
            String folder = "";
        if( null != prefix && !"".equals(prefix)) {
            folder = prefix + '/';
        }
        PutObjectRequest request = new PutObjectRequest(bucketName, folder + multipartFile.getOriginalFilename(), file);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(multipartFile.getContentType());
        request.setMetadata(metadata);
        PutObjectResult result = s3.putObject(request);
        return result;
    }

    private void createBucketIfNotExists(String bucketName) {
        if (!s3.doesBucketExistV2(bucketName)) {
            Bucket bucket = s3.createBucket(bucketName);
            log.info("Bucket {} was created at {}", bucket.getName(), bucket.getCreationDate());
        }
    }


    @Override
    public byte[] downloadFile(String bucketName, String fileUrl) {
        return getFile(bucketName, fileUrl);
    }

    @Override
    public void deleteFile(String bucketName, String fileUrl) {
        s3.deleteObject(bucketName, fileUrl);
    }

    @Override
    public ObjectListing listFiles(String bucketName) {
        List<String> list = new LinkedList<>();
        ObjectListing listObjects = s3.listObjects(bucketName);
        listObjects.getObjectSummaries().forEach(itemResult -> {
            list.add(itemResult.getKey());
            log.info(itemResult.getKey());
        });
        return listObjects;
    }

    @Override
    @SneakyThrows
    public File upload(String bucketName, String name, byte[] content) {
        File file = new File(name);
        file.canRead();
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(content);
        }
        return file;
    }

    @SneakyThrows
    @Override
    public byte[] getFile(String bucketName, String key) {
        S3Object obj = s3.getObject(bucketName, key);
        S3ObjectInputStream stream = obj.getObjectContent();

        byte[] content = IOUtils.toByteArray(stream);
        obj.close();
        return content;
    }

    @Override
    public List<Bucket> listBuckets() {
        return s3.listBuckets();
    }
}