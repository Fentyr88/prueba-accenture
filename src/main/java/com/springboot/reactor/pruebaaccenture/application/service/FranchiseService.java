package com.springboot.reactor.pruebaaccenture.application.service;

import com.springboot.reactor.pruebaaccenture.application.dto.response.BranchResponse;
import com.springboot.reactor.pruebaaccenture.application.dto.response.FranchiseResponse;
import com.springboot.reactor.pruebaaccenture.application.dto.response.ProductResponse;
import com.springboot.reactor.pruebaaccenture.application.dto.response.TopStockProductResponse;
import com.springboot.reactor.pruebaaccenture.application.mapper.FranchiseMapper;
import com.springboot.reactor.pruebaaccenture.application.port.in.AddBranchUseCase;
import com.springboot.reactor.pruebaaccenture.application.port.in.AddProductUseCase;
import com.springboot.reactor.pruebaaccenture.application.port.in.CreateFranchiseUseCase;
import com.springboot.reactor.pruebaaccenture.application.port.in.DeleteProductUseCase;
import com.springboot.reactor.pruebaaccenture.application.port.in.GetAllFranchisesUseCase;
import com.springboot.reactor.pruebaaccenture.application.port.in.GetTopStockByFranchiseUseCase;
import com.springboot.reactor.pruebaaccenture.application.port.in.UpdateBranchNameUseCase;
import com.springboot.reactor.pruebaaccenture.application.port.in.UpdateFranchiseNameUseCase;
import com.springboot.reactor.pruebaaccenture.application.port.in.UpdateProductNameUseCase;
import com.springboot.reactor.pruebaaccenture.application.port.in.UpdateProductStockUseCase;
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
import java.util.List;

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
    public Mono<BranchResponse> addBranch(String franchiseId, String branchName) {
        return loadFranchise(franchiseId)
                .flatMap(franchise -> {
                    Branch newBranch = new Branch(Id.newId(), new Name(branchName), List.of());
                    franchise.addBranch(newBranch);
                    return persistencePort.save(franchise)
                            .map(saved -> FranchiseMapper.toResponse(newBranch));
                });
    }

    @Override
    public Mono<ProductResponse> addProduct(String franchiseId, String branchId, String productName, int stock) {
        return loadFranchise(franchiseId)
                .flatMap(franchise -> {
                    Product newProduct = new Product(Id.newId(), new Name(productName), new Stock(stock));
                    franchise.addProduct(new Id(branchId), newProduct);
                    return persistencePort.save(franchise)
                            .thenReturn(FranchiseMapper.toResponse(newProduct));
                });
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
    public Mono<ProductResponse> updateProductStock(String franchiseId, String branchId, String productId, int stock) {
        Id branch = new Id(branchId);
        Id product = new Id(productId);

        return loadFranchise(franchiseId)
                .flatMap(franchise -> {
                    franchise.updateStock(branch, product, new Stock(stock));
                    return persistencePort.save(franchise)
                            .map(saved -> FranchiseMapper.toResponse(saved.findProduct(branch, product)));
                });
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
    public Mono<BranchResponse> updateBranchName(String franchiseId, String branchId, String name) {
        Id branch = new Id(branchId);

        return loadFranchise(franchiseId)
                .flatMap(franchise -> {
                    franchise.updateBranchName(branch, new Name(name));
                    return persistencePort.save(franchise)
                            .map(saved -> FranchiseMapper.toResponse(saved.findBranch(branch)));
                });
    }

    @Override
    public Mono<ProductResponse> updateProductName(String franchiseId, String branchId, String productId, String name) {
        Id branch = new Id(branchId);
        Id product = new Id(productId);

        return loadFranchise(franchiseId)
                .flatMap(franchise -> {
                    franchise.updateProductName(branch, product, new Name(name));
                    return persistencePort.save(franchise)
                            .map(saved -> FranchiseMapper.toResponse(saved.findProduct(branch, product)));
                });
    }

    @Override
    public Flux<TopStockProductResponse> getTopStockByFranchise(String franchiseId) {
        return loadFranchise(franchiseId)
                .flatMapMany(franchise -> Flux.fromIterable(franchise.getBranches())
                        .flatMapIterable(branch -> FranchiseMapper.toTopStockResponse(franchise, branch)));
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
