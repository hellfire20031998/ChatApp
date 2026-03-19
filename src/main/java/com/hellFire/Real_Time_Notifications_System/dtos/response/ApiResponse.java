package com.hellFire.Real_Time_Notifications_System.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private String errorCode;

    @Builder.Default
    private Instant timestamp = Instant.now();

    private T data;

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Success");
    }

    public static ApiResponse<?> error(String errorCode, String message) {
        return ApiResponse.builder()
                .success(false)
                .errorCode(errorCode)
                .message(message)
                .build();
    }

    public static ApiResponse<?> error(String errorCode, Map<String, String> errors) {
        return ApiResponse.builder()
                .success(false)
                .errorCode(errorCode)
                .data(errors)
                .build();
    }

}
