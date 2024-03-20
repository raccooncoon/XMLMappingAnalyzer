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
    private String xmlid;
    private String namespace;
    private String subtag;
    private String fileName;
//    private String filePath;
    private String context;
    @JsonInclude(JsonInclude.Include.NON_NULL) // null이 아닌 경 우에만 직렬화
    private List<MethodModels> methodModels = new ArrayList<>(); // 빈 리스트로 초기화
    private Number urlCount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MethodModels {
        private String className;
        private String methodName;
//        private String filePath;
        private String url;
    }
}
