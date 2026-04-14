package com.springboot.reactor.pruebaaccenture.application.service.helper;

import com.springboot.reactor.pruebaaccenture.application.port.out.FranchisePersistencePort;
import com.springboot.reactor.pruebaaccenture.domain.exception.FranchiseNotFoundException;
import com.springboot.reactor.pruebaaccenture.domain.model.Franchise;
import com.springboot.reactor.pruebaaccenture.domain.vo.Id;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class FranchiseLoader {

    private final FranchisePersistencePort persistencePort;

    public FranchiseLoader(FranchisePersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    public Mono<Franchise> loadFranchise(String franchiseId) {
        return persistencePort.findById(new Id(franchiseId))
                .switchIfEmpty(Mono.error(new FranchiseNotFoundException(franchiseId)));
    }

}