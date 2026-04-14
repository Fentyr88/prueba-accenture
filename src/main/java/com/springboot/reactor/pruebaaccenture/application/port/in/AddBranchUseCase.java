package com.springboot.reactor.pruebaaccenture.application.port.in;

import com.springboot.reactor.pruebaaccenture.application.dto.response.BranchResponse;
import reactor.core.publisher.Mono;

public interface AddBranchUseCase {

    Mono<BranchResponse> addBranch(String franchiseId, String branchName);
}

