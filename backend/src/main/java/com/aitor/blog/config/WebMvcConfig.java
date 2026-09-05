package com.aitor.blog.config;

import com.aitor.blog.common.interceptor.JwtInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration 
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new JwtInterceptor())
        .addPathPatterns("/**")                // intercept all requests
        .excludePathPatterns("/auth/login");  // exclude login endpoint from interception
    }
}

