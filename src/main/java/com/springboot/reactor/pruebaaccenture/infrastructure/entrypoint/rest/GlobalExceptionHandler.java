package com.springboot.reactor.pruebaaccenture.infrastructure.entrypoint.rest;

import com.springboot.reactor.pruebaaccenture.domain.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ErrorResponse> handleValidation(WebExchangeBindException ex, ServerWebExchange exchange) {
        String message = ex.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Invalid request");
        return build(HttpStatus.BAD_REQUEST, message, exchange);
    }

    @ExceptionHandler({IllegalArgumentException.class, InvalidStockException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex, ServerWebExchange exchange) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), exchange);
    }

    @ExceptionHandler(DuplicateNameException.class)
    public ResponseEntity<ErrorResponse> handleConflict(DuplicateNameException ex, ServerWebExchange exchange) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), exchange);
    }

    @ExceptionHandler({FranchiseNotFoundException.class, BranchNotFoundException.class, ProductNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(Exception ex, ServerWebExchange exchange) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), exchange);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleInternal(Exception ex, ServerWebExchange exchange) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error: " + ex.getMessage(), exchange);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().value();
        ErrorResponse body = new ErrorResponse(status.value(), status.getReasonPhrase(), message, path);
        return ResponseEntity.status(status).body(body);
    }
}
