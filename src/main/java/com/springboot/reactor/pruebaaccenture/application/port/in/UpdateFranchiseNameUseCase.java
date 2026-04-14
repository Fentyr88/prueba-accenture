package com.springboot.reactor.pruebaaccenture.application.port.in;

import com.springboot.reactor.pruebaaccenture.application.dto.response.FranchiseResponse;
import reactor.core.publisher.Mono;

public interface UpdateFranchiseNameUseCase {

    Mono<FranchiseResponse> updateFranchiseName(String franchiseId, String name);
}

