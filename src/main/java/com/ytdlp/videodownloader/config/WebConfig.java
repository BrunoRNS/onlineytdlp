package com.ytdlp.videodownloader.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.micrometer.common.lang.NonNull;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Enable CORS for all origins.
     * <p>
     * This allows JavaScript clients from any origin to make requests to the
     * server.
     * <p>
     * The {@code Content-Disposition} header is exposed to allow the filename
     * of the downloaded file to be specified.
     */
    @Override
    @NonNull
    public void addCorsMappings(@SuppressWarnings("null") @NonNull CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .exposedHeaders("Content-Disposition");
    }
}