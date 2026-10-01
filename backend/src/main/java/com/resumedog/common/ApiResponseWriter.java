package com.resumedog.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumedog.dto.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Spring Security 的 entry point / denied handler 运行在过滤器链上，
 * 拿不到 @RestControllerAdvice，所以需要自己写出统一 JSON 响应。
 */
@RequiredArgsConstructor
public class ApiResponseWriter {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void write(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        MAPPER.writeValue(response.getWriter(), ApiResponse.error(code, message));
    }
}
