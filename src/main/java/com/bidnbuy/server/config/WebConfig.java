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
    private String UPLOAD_BASE_DIR;

    // 이미지 접근 경로
    private final String WEB_ACCESS_PREFIX = "/images/auction-products/";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){

        // 절대 경로로 변환
        String absolutePath = new java.io.File(UPLOAD_BASE_DIR).getAbsolutePath();

        // 아래 경로 대로 오면, 절대 경로인 파일:절대경로/uploaded_files/" 에서 파일을 찾도록 매핑
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + absolutePath + "/");
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        // JFIF는 JPEG File Interchange Format의 약자로, JPEG와 동일하게 처리되도록 설정합니다.
        configurer.mediaType("jfif", MediaType.IMAGE_JPEG);
    }
}
