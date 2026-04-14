package com.springboot.reactor.pruebaaccenture.domain.vo;

public record Stock(int value) {

    public Stock {
        if (value < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
    }
}
