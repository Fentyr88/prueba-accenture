package com.springboot.reactor.pruebaaccenture.application.port.in;

import com.springboot.reactor.pruebaaccenture.application.dto.response.FranchiseResponse;
import reactor.core.publisher.Mono;

public interface UpdateProductStockUseCase {

    Mono<FranchiseResponse> updateProductStock(String franchiseId, String branchId, String productId, int stock);
}

