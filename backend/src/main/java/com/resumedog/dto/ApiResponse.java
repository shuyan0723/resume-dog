package com.resumedog.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * 统一响应包装。前后端约定：{@code code == 0} 表示成功。
 */
@Data
@Builder
public class ApiResponse<T> {

    private int code;
    private String message;
    private T data;
    private Instant timestamp;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .code(0)
                .message("ok")
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }
}
