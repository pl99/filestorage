package ru.advantum.av.filestorage.rest;


import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectResult;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.advantum.av.filestorage.services.AmazonS3Service;

import java.util.List;

@RestController
@RequestMapping("s3")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AmazonS3Controller  {
    AmazonS3Service service;

    @Autowired
    public AmazonS3Controller(@Qualifier("amazonS3ServiceImpl") AmazonS3Service service) {
        this.service = service;
    }

    @PostMapping(value = "{bucketName}/files", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public PutObjectResult upload(@PathVariable("bucketName") String bucketName, MultipartFile file, String folder) {
        return service.uploadFile(bucketName, file, folder);
    }


    @GetMapping(value = "{bucketName}/{keyName}", consumes = "application/octet-stream")
    public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable("bucketName") String bucketName, @PathVariable("keyName") String keyName)  {
        byte[] data = service.downloadFile(bucketName, keyName);
        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity
                .ok()
                .contentLength(data.length)
                .header("Content-type", "application/octet-stream")
                .header("Content-disposition", "attachment; filename=\"" + keyName + "\"")
                .body(resource);
    }

    @DeleteMapping("{bucketName}/files/{keyName}")
    public void delete(@PathVariable("bucketName") String bucketName, @PathVariable(value = "keyName") String keyName)  {
        service.deleteFile(bucketName, keyName);
    }

    @GetMapping("{bucketName}/files")
    public ObjectListing listObjects(@PathVariable("bucketName") String bucketName)  {
        return service.listFiles(bucketName);
    }

    @GetMapping("buckets")
    public List<Bucket> listBuckets() {
        return service.listBuckets();
    }
}