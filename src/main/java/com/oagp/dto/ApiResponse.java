package com.oagp.dto;

/**
 * ===============================================================
 * ApiResponse
 * ===============================================================
 *
 * PURPOSE: This class is a generic wrapper used to standardise all API
 * responses returned by the backend REST controllers. It ensures that every
 * response follows a consistent structure, making it easier for frontend
 * applications  to process and display data.
 *
 * 
 *
 * 1. success → Indicates if the request was successful (true/false) 2. message
 * → A human-readable message (useful for UI alerts/logging) 3. data → The
 * actual payload (generic type T)
 *
 * GENERIC DESIGN: The <T> allows this class to be reused for
 * any type of data.
 * ===============================================================
 */

public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
