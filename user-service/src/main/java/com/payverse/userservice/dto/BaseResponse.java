package com.payverse.userservice.dto;

import java.time.LocalDateTime;

public class BaseResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public BaseResponse() {
        
    }

    public BaseResponse(boolean success, String message, T data, LocalDateTime timestamp) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
    }




    // Static factory methods improve readability, reduce duplicate object creation code, and centralize object initialization. If the response structure changes later (for example, adding metadata or trace IDs), only the factory methods need to be updated instead of every controller.
  public static <T> BaseResponse<T> success(T data, String message) {
    return new BaseResponse<>(
            true,
            message,
            data,
            LocalDateTime.now()
    );
}

public static <T> BaseResponse<T> error(T data, String message) {
    return new BaseResponse<>(
            false,
            message,
            data,
            LocalDateTime.now()
    );
}

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

}
