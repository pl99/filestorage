package ru.advantum.filestorage.converters;

import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import io.minio.ObjectWriteResponse;
import io.minio.messages.Item;
import ru.advantum.filestorage.dto.S3ObjDto;

import java.util.List;

public interface S3ObjDtoConverter {
    S3ObjDto toDto(S3ObjectSummary objectSummary);
    S3ObjDto toDto(S3Object object);
    S3ObjDto toDto(ObjectWriteResponse object);


    List<S3ObjDto> toDtos(List<S3ObjectSummary> objectSummaries);

    List<S3ObjDto> toDtos(ObjectListing objectListing);

    public S3ObjDto toDto(Item item);
}
