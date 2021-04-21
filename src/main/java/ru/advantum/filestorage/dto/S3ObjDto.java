package ru.advantum.filestorage.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class S3ObjDto {
    String key;
    Long size;
    String bucketName;
    Instant lastModified;
    String storageClass;
    String ownerName;
    String ownerId;
    String etag;
}
