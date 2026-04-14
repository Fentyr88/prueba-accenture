package com.springboot.reactor.pruebaaccenture.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateStockRequest(
        @NotNull(message = "stock is required") @Min(value = 0, message = "stock cannot be negative") Integer stock
) {}