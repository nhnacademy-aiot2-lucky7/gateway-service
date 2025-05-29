package com.nhnacademy.gateway.common.parser;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface DataParser {

    /**
     * @return Parser가 호환되는 Data Format Type을 반환합니다.
     */
    String getFileType();

    boolean matchDataType(String payload);

    /**
     * @param payload Broker가 보낸 페이로드 데이터
     * @return Parsing Data
     */
    Map<String, Object> parsing(String payload) throws IOException;

    /**
     * @param file Broker가 보낸 페이로드 파일
     * @return Parsing Data
     */
    List<Map<String, Object>> parsing(File file) throws IOException;

    default boolean matchFileType(String fileName) {
        return fileName.trim().toLowerCase().endsWith(getFileType().toLowerCase());
    }
}
