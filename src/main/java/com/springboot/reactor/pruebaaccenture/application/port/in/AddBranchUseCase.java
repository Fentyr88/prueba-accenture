package com.springboot.reactor.pruebaaccenture.application.port.in;

import com.springboot.reactor.pruebaaccenture.application.dto.response.FranchiseResponse;
import reactor.core.publisher.Mono;

public interface AddBranchUseCase {

    Mono<FranchiseResponse> addBranch(String franchiseId, String branchName);
}

