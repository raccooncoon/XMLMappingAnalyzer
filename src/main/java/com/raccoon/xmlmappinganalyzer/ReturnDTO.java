package com.raccoon.xmlmappinganalyzer;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnDTO {

    private String moduleName;
    private String id;
    private String subtag;
    private String namespace;
    private String fileName;
    private String filePath;
    private String context;
    @JsonInclude(JsonInclude.Include.NON_NULL) // null이 아닌 경우에만 직렬화
    private List<TopCaller> topCallingMethods = new ArrayList<>(); // 빈 리스트로 초기화


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopCaller {
        private String filePath;
        private String methodName;
        private String url;
    }
}
