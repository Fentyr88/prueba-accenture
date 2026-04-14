package com.springboot.reactor.pruebaaccenture.infrastructure.entrypoint.rest;

import java.time.Instant;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
    public ErrorResponse(int status, String error, String message, String path) {
        this(Instant.now(), status, error, message, path);
    }
}
