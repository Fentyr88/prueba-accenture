package com.springboot.reactor.pruebaaccenture.application.port.in;

import com.springboot.reactor.pruebaaccenture.application.dto.response.FranchiseResponse;
import reactor.core.publisher.Mono;

public interface UpdateBranchNameUseCase {

    Mono<FranchiseResponse> updateBranchName(String franchiseId, String branchId, String name);
}

