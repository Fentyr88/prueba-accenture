package com.springboot.reactor.pruebaaccenture.application.service;

import com.springboot.reactor.pruebaaccenture.application.dto.response.FranchiseResponse;
import com.springboot.reactor.pruebaaccenture.application.dto.response.TopStockProductResponse;
import com.springboot.reactor.pruebaaccenture.application.mapper.FranchiseMapper;
import com.springboot.reactor.pruebaaccenture.application.port.in.*;
import com.springboot.reactor.pruebaaccenture.application.port.out.FranchisePersistencePort;
import com.springboot.reactor.pruebaaccenture.domain.exception.DuplicateNameException;
import com.springboot.reactor.pruebaaccenture.domain.exception.FranchiseNotFoundException;
import com.springboot.reactor.pruebaaccenture.domain.model.Branch;
import com.springboot.reactor.pruebaaccenture.domain.model.Franchise;
import com.springboot.reactor.pruebaaccenture.domain.model.Product;
import com.springboot.reactor.pruebaaccenture.domain.vo.Id;
import com.springboot.reactor.pruebaaccenture.domain.vo.Name;
import com.springboot.reactor.pruebaaccenture.domain.vo.Stock;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

@Service
public class FranchiseService implements
        CreateFranchiseUseCase, AddBranchUseCase, AddProductUseCase, DeleteProductUseCase,
        UpdateProductStockUseCase, GetTopStockByFranchiseUseCase, UpdateFranchiseNameUseCase,
        UpdateBranchNameUseCase, UpdateProductNameUseCase, GetAllFranchisesUseCase {

    private final FranchisePersistencePort persistencePort;

    public FranchiseService(FranchisePersistencePort persistencePort) {
        this.persistencePort = persistencePort;
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
    public Mono<FranchiseResponse> addBranch(String franchiseId, String branchName) {
        return loadFranchise(franchiseId)
                .flatMap(franchise -> {
                    franchise.addBranch(new Branch(Id.newId(), new Name(branchName), new ArrayList<>()));
                    return persistencePort.save(franchise);
                })
                .map(FranchiseMapper::toResponse);
    }

    @Override
    public Mono<FranchiseResponse> addProduct(String franchiseId, String branchId, String productName, int stock) {
        return loadFranchise(franchiseId)
                .flatMap(franchise -> {
                    franchise.addProduct(new Id(branchId), new Product(Id.newId(), new Name(productName), new Stock(stock)));
                    return persistencePort.save(franchise);
                })
                .map(FranchiseMapper::toResponse);
    }

    @Override
    public Mono<FranchiseResponse> deleteProduct(String franchiseId, String branchId, String productId) {
        return loadFranchise(franchiseId)
                .flatMap(franchise -> {
                    franchise.removeProduct(new Id(branchId), new Id(productId));
                    return persistencePort.save(franchise);
                })
                .map(FranchiseMapper::toResponse);
    }

    @Override
    public Mono<FranchiseResponse> updateProductStock(String franchiseId, String branchId, String productId, int stock) {
        return loadFranchise(franchiseId)
                .flatMap(franchise -> {
                    franchise.updateStock(new Id(branchId), new Id(productId), new Stock(stock));
                    return persistencePort.save(franchise);
                })
                .map(FranchiseMapper::toResponse);
    }

    @Override
    public Mono<FranchiseResponse> updateFranchiseName(String franchiseId, String name) {
        Name updatedName = new Name(name);
        return loadFranchise(franchiseId)
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
    public Mono<FranchiseResponse> updateBranchName(String franchiseId, String branchId, String name) {
        return loadFranchise(franchiseId)
                .flatMap(franchise -> {
                    franchise.updateBranchName(new Id(branchId), new Name(name));
                    return persistencePort.save(franchise);
                })
                .map(FranchiseMapper::toResponse);
    }

    @Override
    public Mono<FranchiseResponse> updateProductName(String franchiseId, String branchId, String productId, String name) {
        return loadFranchise(franchiseId)
                .flatMap(franchise -> {
                    franchise.updateProductName(new Id(branchId), new Id(productId), new Name(name));
                    return persistencePort.save(franchise);
                })
                .map(FranchiseMapper::toResponse);
    }

    @Override
    public Flux<TopStockProductResponse> getTopStockByFranchise(String franchiseId) {
        return loadFranchise(franchiseId)
                .flatMapMany(franchise -> Flux.fromIterable(franchise.getBranches())
                        .flatMap(branch -> Mono.justOrEmpty(FranchiseMapper.toTopStockResponse(franchise, branch))));
    }

    @Override
    public Flux<FranchiseResponse> getAllFranchises() {
        return persistencePort.findAll().map(FranchiseMapper::toResponse);
    }

    private Mono<Franchise> loadFranchise(String franchiseId) {
        return persistencePort.findById(new Id(franchiseId))
                .switchIfEmpty(Mono.error(new FranchiseNotFoundException(franchiseId)));
    }
}
