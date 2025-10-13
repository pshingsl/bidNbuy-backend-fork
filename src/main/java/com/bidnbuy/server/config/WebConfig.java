package com.bidnbuy.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 이미지 접근 경로
    private final String WEB_ACCESS_PREFIX = "/images/auction-products/";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        String actualUploadPath = "file:" + uploadDir + "/auction-products/";
        registry.addResourceHandler(WEB_ACCESS_PREFIX + "**")
                .addResourceLocations(actualUploadPath);
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        // JFIF는 JPEG File Interchange Format의 약자로, JPEG와 동일하게 처리되도록 설정합니다.
        configurer.mediaType("jfif", MediaType.IMAGE_JPEG);
    }
}
