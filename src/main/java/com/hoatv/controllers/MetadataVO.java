package com.hoatv.controllers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetadataVO {

    private String columnId;

    private List<ColumnMetadataVO> columnMetadata;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ColumnMetadataVO {
        private String fieldPath;

        private String mappingColumnName;

        private String decryptFunctionName;
    }
}
