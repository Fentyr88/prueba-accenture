package com.springboot.reactor.pruebaaccenture.domain.vo;

import java.util.UUID;

public record Id(String value) {

    public Id {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Id cannot be empty");
        }
    }

    public static Id newId() {
        return new Id(UUID.randomUUID().toString());
    }
}
