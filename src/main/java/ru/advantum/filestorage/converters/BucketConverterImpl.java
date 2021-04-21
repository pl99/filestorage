package ru.advantum.filestorage.converters;

import com.amazonaws.services.s3.model.Bucket;
import org.springframework.stereotype.Component;
import ru.advantum.filestorage.dto.BucketDto;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BucketConverterImpl implements BucketConverter{

    @Override
    public BucketDto toDto(Bucket bucket) {
        return BucketDto.builder()
                .name(bucket.getName())
                .ownerId(bucket.getOwner().getId())
                .ownerName(bucket.getOwner().getDisplayName())
                .creationDate(bucket.getCreationDate().toInstant())
                .build();
    }

    @Override
    public List<BucketDto> toDtos(List<Bucket> buckets) {
        return buckets.stream().map(this::toDto)
                .collect(Collectors.toList());
    }
}
