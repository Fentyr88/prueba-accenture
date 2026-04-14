package com.springboot.reactor.pruebaaccenture.application.service;

import com.springboot.reactor.pruebaaccenture.application.dto.response.FranchiseResponse;
import com.springboot.reactor.pruebaaccenture.application.dto.response.TopStockProductResponse;
import com.springboot.reactor.pruebaaccenture.application.mapper.FranchiseMapper;
import com.springboot.reactor.pruebaaccenture.application.port.in.CreateFranchiseUseCase;
import com.springboot.reactor.pruebaaccenture.application.port.in.GetAllFranchisesUseCase;
import com.springboot.reactor.pruebaaccenture.application.port.in.GetTopStockByFranchiseUseCase;
import com.springboot.reactor.pruebaaccenture.application.port.in.UpdateFranchiseNameUseCase;
import com.springboot.reactor.pruebaaccenture.application.port.out.FranchisePersistencePort;
import com.springboot.reactor.pruebaaccenture.application.service.helper.FranchiseLoader;
import com.springboot.reactor.pruebaaccenture.domain.exception.DuplicateNameException;
import com.springboot.reactor.pruebaaccenture.domain.model.Franchise;
import com.springboot.reactor.pruebaaccenture.domain.vo.Id;
import com.springboot.reactor.pruebaaccenture.domain.vo.Name;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

@Service
public class FranchiseService implements
        CreateFranchiseUseCase,
        UpdateFranchiseNameUseCase,
        GetTopStockByFranchiseUseCase,
        GetAllFranchisesUseCase {

    private final FranchisePersistencePort persistencePort;
    private final FranchiseLoader franchiseLoader;

    public FranchiseService(FranchisePersistencePort persistencePort, FranchiseLoader franchiseLoader) {
        this.persistencePort = persistencePort;
        this.franchiseLoader = franchiseLoader;
    }

    @Override
    public Mono<FranchiseResponse> createFranchise(String name) {
        Name newName = new Name(name);
        return persistencePort.existsByName(newName)
                .flatMap(exists -> exists
                        ? Mono.error(new DuplicateNameException("Franchise already exists"))
                        : persistencePort.save(new Franchise(Id.newId(), newName, new ArrayList<>())))
                .map(FranchiseMapper::toResponse);
    }

    @Override
    public Mono<FranchiseResponse> updateFranchiseName(String franchiseId, String name) {
        Name updatedName = new Name(name);
        return franchiseLoader.loadFranchise(franchiseId)
                .flatMap(franchise -> persistencePort.existsByName(updatedName).flatMap(exists -> {
                    boolean sameCurrentName = franchise.getName().canonical().equals(updatedName.canonical());
                    if (exists && !sameCurrentName) {
                        return Mono.error(new DuplicateNameException("Franchise already exists"));
                    }
                    franchise.updateName(updatedName);
                    return persistencePort.save(franchise);
                }))
                .map(FranchiseMapper::toResponse);
    }

    @Override
    public Flux<TopStockProductResponse> getTopStockByFranchise(String franchiseId) {
        return franchiseLoader.loadFranchise(franchiseId)
                .flatMapMany(franchise -> Flux.fromIterable(franchise.getBranches())
                        .flatMapIterable(branch -> FranchiseMapper.toTopStockResponse(franchise, branch)));
    }

    @Override
    public Flux<FranchiseResponse> getAllFranchises() {
        return persistencePort.findAll().map(FranchiseMapper::toResponse);
    }
}
