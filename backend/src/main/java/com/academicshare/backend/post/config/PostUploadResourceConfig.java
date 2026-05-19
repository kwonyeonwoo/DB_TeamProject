package com.academicshare.backend.post.config;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class PostUploadResourceConfig implements WebMvcConfigurer {

    private final String uploadResourceLocation;

    public PostUploadResourceConfig(@Value("${app.upload.root:uploads}") String uploadRoot) {
        String location = Path.of(uploadRoot).toAbsolutePath().normalize().toUri().toString();
        this.uploadResourceLocation = location.endsWith("/") ? location : location + "/";
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadResourceLocation);
    }
}
