package com.nhnacademy.gateway.common.config;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    /**
     * 문자열을 카멜 케이스가 아닌, 스네이크 케이스로 설정
     */
    @Bean
    Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer(
            PropertyNamingStrategy propertyNamingStrategy
    ) {
        return builder -> builder
                .propertyNamingStrategy(propertyNamingStrategy)
                .modules(new JavaTimeModule())
                .simpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    @Bean
    PropertyNamingStrategy propertyNamingStrategy() {
        return PropertyNamingStrategies.SNAKE_CASE;
    }
}
