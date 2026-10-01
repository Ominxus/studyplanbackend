package com.studentplansystem.studyplangym.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(
            ResponseStatusException exception
    ) {
        Map<String, Object> body = new LinkedHashMap<>();

        body.put(
                "status",
                exception.getStatusCode().value()
        );

        body.put(
                "message",
                exception.getReason() != null
                        ? exception.getReason()
                        : "Request failed."
        );

        return ResponseEntity
                .status(exception.getStatusCode().value())
                .body(body);
    }
}
