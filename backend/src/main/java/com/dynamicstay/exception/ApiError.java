package com.dynamicstay.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class ApiError {
    private final int status;
    private final String error;
    private final List<String> messages;
    private final LocalDateTime timestamp;

    public ApiError(int status, String error, List<String> messages) {
        this(status, error, messages, LocalDateTime.now());
    }
}
