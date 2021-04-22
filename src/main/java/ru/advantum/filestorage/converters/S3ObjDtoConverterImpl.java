package ru.advantum.filestorage.converters;

import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import io.minio.ObjectWriteResponse;
import io.minio.Result;
import io.minio.messages.Item;
import org.springframework.stereotype.Component;
import ru.advantum.filestorage.dto.S3ObjDto;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class S3ObjDtoConverterImpl implements S3ObjDtoConverter {
    @Override
    public S3ObjDto toDto(S3ObjectSummary objectSummary) {
        return S3ObjDto.builder()
                .bucketName(objectSummary.getBucketName())
                .etag(objectSummary.getETag())
                .key(objectSummary.getKey())
                .lastModified(objectSummary.getLastModified().toInstant())
                .ownerId(objectSummary.getOwner().getId())
                .ownerName(objectSummary.getOwner().getDisplayName())
                .build();
    }

    @Override
    public S3ObjDto toDto(S3Object object) {
        return S3ObjDto.builder()
                .key(object.getKey())
                .bucketName(object.getBucketName())
                .size(object.getObjectMetadata().getContentLength())
                .storageClass(object.getObjectMetadata().getStorageClass())
                .etag(object.getObjectMetadata().getETag())
                .lastModified(object.getObjectMetadata().getLastModified().toInstant())
                .build();
    }

    @Override
    public S3ObjDto toDto(ObjectWriteResponse object) {
        return S3ObjDto.builder()
                .key(object.object())
                .bucketName(object.bucket())
                .etag(object.etag())
                .size(object.headers().byteCount())
                .lastModified(object.headers().getInstant("lastModified"))
                .build();
    }

    @Override
    public List<S3ObjDto> toDtos(List<S3ObjectSummary> objectSummaries) {
        return objectSummaries.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<S3ObjDto> toDtos(ObjectListing objectListing) {
        return toDtos(objectListing.getObjectSummaries());
    }

    @Override
    public S3ObjDto toDto(Item item) {
        return S3ObjDto.builder()
                .lastModified(item.lastModified().toInstant())
                .etag(item.etag())
                .ownerName(item.owner().displayName())
                .size(item.size())
                .storageClass(item.storageClass())
                .key(item.objectName())
                .build();
    }
}
