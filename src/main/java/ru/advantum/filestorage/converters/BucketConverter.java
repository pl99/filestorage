package ru.advantum.filestorage.converters;

import com.amazonaws.services.s3.model.Bucket;
import ru.advantum.filestorage.dto.BucketDto;

import java.util.List;

public interface BucketConverter {

    BucketDto toDto(Bucket bucket);

    List<BucketDto> toDtos(List<Bucket> buckets);

}
