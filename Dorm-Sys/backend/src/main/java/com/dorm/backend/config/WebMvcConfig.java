package com.dorm.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtAuthInterceptor jwtAuthInterceptor;
    private final OperationAuditInterceptor operationAuditInterceptor;

    public WebMvcConfig(JwtAuthInterceptor jwtAuthInterceptor, OperationAuditInterceptor operationAuditInterceptor) {
        this.jwtAuthInterceptor = jwtAuthInterceptor;
        this.operationAuditInterceptor = operationAuditInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/error");
        registry.addInterceptor(operationAuditInterceptor).addPathPatterns("/api/**");
    }
}
