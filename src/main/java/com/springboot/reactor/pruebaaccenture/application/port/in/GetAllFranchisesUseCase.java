package com.springboot.reactor.pruebaaccenture.application.port.in;

import com.springboot.reactor.pruebaaccenture.application.dto.response.FranchiseResponse;
import reactor.core.publisher.Flux;

public interface GetAllFranchisesUseCase {

    Flux<FranchiseResponse> getAllFranchises();
}