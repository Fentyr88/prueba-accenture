package com.springboot.reactor.pruebaaccenture.application.port.in;

import com.springboot.reactor.pruebaaccenture.application.dto.response.ProductResponse;
import reactor.core.publisher.Mono;

public interface UpdateProductStockUseCase {

    Mono<ProductResponse> updateProductStock(String franchiseId, String branchId, String productId, int stock);
}

