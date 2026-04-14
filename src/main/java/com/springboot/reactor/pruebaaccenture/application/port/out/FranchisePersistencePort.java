package com.springboot.reactor.pruebaaccenture.application.port.out;

import com.springboot.reactor.pruebaaccenture.domain.model.Franchise;
import com.springboot.reactor.pruebaaccenture.domain.vo.Id;
import com.springboot.reactor.pruebaaccenture.domain.vo.Name;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FranchisePersistencePort {
    Mono<Franchise> save(Franchise franchise);
    Mono<Franchise> findById(Id franchiseId);
    Mono<Boolean> existsByName(Name name);
    Flux<Franchise> findAll();
}
