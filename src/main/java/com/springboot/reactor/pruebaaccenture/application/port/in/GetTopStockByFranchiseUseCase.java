package com.springboot.reactor.pruebaaccenture.application.port.in;

import com.springboot.reactor.pruebaaccenture.application.dto.response.TopStockProductResponse;
import reactor.core.publisher.Flux;

public interface GetTopStockByFranchiseUseCase {

    Flux<TopStockProductResponse> getTopStockByFranchise(String franchiseId);
}

