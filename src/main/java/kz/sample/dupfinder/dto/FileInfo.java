package kz.sample.dupfinder.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileInfo {
    private String name;
    private Long size;
    private String hash;
}
