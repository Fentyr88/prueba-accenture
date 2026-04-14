package com.springboot.reactor.pruebaaccenture.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateFranchiseRequest(@NotBlank(message = "name is required") String name) {}
