package com.projectpulse.common;

public record ApiResponse<T>(boolean success, T data, String message, String error) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message, message);
    }
}
