package com.springboot.reactor.pruebaaccenture.application.port.in;

import com.springboot.reactor.pruebaaccenture.application.dto.response.BranchResponse;
import reactor.core.publisher.Mono;

public interface UpdateBranchNameUseCase {

    Mono<BranchResponse> updateBranchName(String franchiseId, String branchId, String name);
}

