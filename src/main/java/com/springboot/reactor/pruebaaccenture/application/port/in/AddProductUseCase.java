package com.springboot.reactor.pruebaaccenture.application.port.in;

import com.springboot.reactor.pruebaaccenture.application.dto.response.ProductResponse;
import reactor.core.publisher.Mono;

public interface AddProductUseCase {

    Mono<ProductResponse> addProduct(String franchiseId, String branchId, String productName, int stock);
}

