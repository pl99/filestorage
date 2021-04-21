package ru.advantum.filestorage.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class BucketDto {
    String name;
    String ownerName;
    String ownerId;
    Instant creationDate;
}
