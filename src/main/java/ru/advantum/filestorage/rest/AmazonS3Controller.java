package ru.advantum.filestorage.rest;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.advantum.filestorage.dto.BucketDto;
import ru.advantum.filestorage.dto.S3ObjDto;
import ru.advantum.filestorage.services.AmazonS3Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("s3")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AmazonS3Controller {
    Map<String, AmazonS3Service> services;

    private AmazonS3Service getService(String implementation) {
        return Optional.ofNullable(services.get(implementation))
                .orElseThrow(() -> new IllegalArgumentException("Not implemented yet"));
    }

    @PostMapping(value = "{implementation}/{bucketName}/{folder}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public S3ObjDto upload(@PathVariable("bucketName") String bucketName,
                           @PathVariable("implementation") String implementation,
                           @PathVariable(value = "folder", required = false) String folder, @RequestParam MultipartFile file) {
        return getService(implementation)
                .uploadFile(bucketName, file, folder);
    }


    @GetMapping(value = "{implementation}/{bucketName}/{keyName}", consumes = "application/octet-stream")
    public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable("bucketName") String bucketName,
                                                          @PathVariable("implementation") String implementation,
                                                          @PathVariable("keyName") String keyName) {
        byte[] data = getService(implementation).downloadFile(bucketName, keyName);
        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity
                .ok()
                .contentLength(data.length)
                .header("Content-type", "application/octet-stream")
                .header("Content-disposition", "attachment; filename=\"" + keyName + "\"")
                .body(resource);
    }

    @DeleteMapping("{implementation}/{bucketName}/files/{keyName}")
    public void delete(@PathVariable("bucketName") String bucketName,
                       @PathVariable("implementation") String implementation,
                       @PathVariable(value = "keyName") String keyName) {
        getService(implementation).deleteFile(bucketName, keyName);
    }

    @GetMapping("{implementation}/{bucketName}")
    public List<S3ObjDto> listObjects(@PathVariable("bucketName") String bucketName,
                                      @PathVariable("implementation") String implementation) {
        return getService(implementation).listFiles(bucketName);
    }

    @GetMapping("{implementation}/buckets")
    public List<BucketDto> listBuckets(@PathVariable("implementation") String implementation) {
        return getService(implementation).listBuckets();
    }
}