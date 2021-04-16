package ru.advantum.av.filestorage.services;


import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

public interface AmazonS3Service {
    PutObjectResult uploadFile(String bucketName, MultipartFile file) ;
    PutObjectResult uploadFile(String bucketName, MultipartFile file, String prefix) ;

    byte[] downloadFile(String bucketName, String fileUrl) ;

    void deleteFile(String bucketName, String fileUrl) ;

    ObjectListing listFiles(String bucketName) ;

    File upload(String bucketName, String name, byte[] content) ;

    byte[] getFile(String bucketName, String key) ;

    List<Bucket> listBuckets();
}