package com.springboot.reactor.pruebaaccenture.application.dto.response;

public record ApiResponse<T>(int status, String message, T data) {}
