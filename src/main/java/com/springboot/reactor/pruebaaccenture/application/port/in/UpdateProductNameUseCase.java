package com.springboot.reactor.pruebaaccenture.application.port.in;

import com.springboot.reactor.pruebaaccenture.application.dto.response.FranchiseResponse;
import reactor.core.publisher.Mono;

public interface UpdateProductNameUseCase {

    Mono<FranchiseResponse> updateProductName(String franchiseId, String branchId, String productId, String name);
}

