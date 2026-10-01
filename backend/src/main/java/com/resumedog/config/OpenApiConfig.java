package com.resumedog.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI resumeDogOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("简历狗 Resume Dog API")
                        .version("0.1.0")
                        .description("AI 驱动的简历生成工具。核心接口是 `POST /api/resumes/{id}/generate`，"
                                + "把经历碎片交给 Claude 改写成结构化简历；`GET /api/resumes/{id}/export` 导出 PDF。"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
