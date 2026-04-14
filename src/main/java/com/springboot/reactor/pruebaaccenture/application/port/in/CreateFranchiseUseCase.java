package com.springboot.reactor.pruebaaccenture.application.port.in;

import com.springboot.reactor.pruebaaccenture.application.dto.response.FranchiseResponse;
import reactor.core.publisher.Mono;

public interface CreateFranchiseUseCase {

    Mono<FranchiseResponse> createFranchise(String name);
}
