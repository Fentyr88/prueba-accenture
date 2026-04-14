package com.springboot.reactor.pruebaaccenture.domain.vo;

public record Name(String value) {
    public Name {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        String normalized = value.trim();
        if (normalized.length() < 2 || normalized.length() > 30) {
            throw new IllegalArgumentException("Name must be between 2 and 30 characters");
        }
        if (!normalized.matches("^[\\p{L}0-9\\s,.-]+$")) {
            throw new IllegalArgumentException("Name contains invalid characters");
        }

        value = normalized;
    }

    public String canonical() {
        return value.trim().toLowerCase();
    }
}
