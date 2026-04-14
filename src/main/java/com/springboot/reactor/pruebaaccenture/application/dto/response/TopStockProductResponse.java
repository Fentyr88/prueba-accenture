package com.springboot.reactor.pruebaaccenture.application.dto.response;

public record TopStockProductResponse(
        String franchiseId, String franchiseName,
        String branchId, String branchName,
        String productId, String productName,
        int stock
) {}