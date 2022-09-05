package ru.advantum.filestorage.services;


import lombok.SneakyThrows;
import org.springframework.web.multipart.MultipartFile;
import ru.advantum.filestorage.dto.BucketDto;
import ru.advantum.filestorage.dto.S3ObjDto;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public interface AmazonS3Service {
    S3ObjDto uploadFile(String bucketName, MultipartFile multipartFile) ;
    S3ObjDto uploadFile(String bucketName, MultipartFile multipartFile, String prefix) ;

    byte[] downloadFile(String bucketName, String fileUrl) ;

    void deleteFile(String bucketName, String fileUrl) ;

    List<S3ObjDto> listFiles(String bucketName) ;

    @SneakyThrows
    default File upload(String bucketName, String name, byte[] content) {
        File file = new File(name);
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(content);
        }
        return file;
    }

    byte[] getFile(String bucketName, String key) ;

    List<BucketDto> listBuckets();
}