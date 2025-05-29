package com.nhnacademy.gateway.common.parser.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.gateway.common.parser.DataParser;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/// TODO: 임시
@Component
public final class JsonDataParser implements DataParser {

    private static final String FILE_TYPE = "JSON";

    private final ObjectMapper objectMapper;

    public JsonDataParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String getFileType() {
        return FILE_TYPE;
    }

    @Override
    public boolean matchDataType(String payload) {
        return (payload.startsWith("{") && payload.endsWith("}"))
                || (payload.startsWith("[") && payload.endsWith("]"));
    }

    @Override
    public Map<String, Object> parsing(String payload) throws IOException {
        return objectMapper.readValue(
                payload,
                new TypeReference<>() {
                }
        );
    }

    @Override
    public List<Map<String, Object>> parsing(File file) throws IOException {
        return objectMapper.readValue(
                file,
                new TypeReference<>() {
                }
        );
    }
}
