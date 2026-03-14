package com.smartmedical.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login", "/login.html",
                        "/auth/**",
                        "/error",
                        "/favicon.ico",
                        "/css/**", "/js/**", "/images/**",
                        "/static/**", "/webjars/**",
                        "/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg", "/**/*.gif", "/**/*.svg"
                );
    }
}