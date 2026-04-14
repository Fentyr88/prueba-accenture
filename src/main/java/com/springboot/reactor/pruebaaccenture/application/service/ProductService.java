package com.springboot.reactor.pruebaaccenture.application.service;

import com.springboot.reactor.pruebaaccenture.application.dto.response.FranchiseResponse;
import com.springboot.reactor.pruebaaccenture.application.dto.response.ProductResponse;
import com.springboot.reactor.pruebaaccenture.application.mapper.FranchiseMapper;
import com.springboot.reactor.pruebaaccenture.application.port.in.AddProductUseCase;
import com.springboot.reactor.pruebaaccenture.application.port.in.DeleteProductUseCase;
import com.springboot.reactor.pruebaaccenture.application.port.in.UpdateProductNameUseCase;
import com.springboot.reactor.pruebaaccenture.application.port.in.UpdateProductStockUseCase;
import com.springboot.reactor.pruebaaccenture.application.port.out.FranchisePersistencePort;
import com.springboot.reactor.pruebaaccenture.application.service.helper.FranchiseLoader;
import com.springboot.reactor.pruebaaccenture.domain.model.Product;
import com.springboot.reactor.pruebaaccenture.domain.vo.Id;
import com.springboot.reactor.pruebaaccenture.domain.vo.Name;
import com.springboot.reactor.pruebaaccenture.domain.vo.Stock;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ProductService implements
        AddProductUseCase,
        DeleteProductUseCase,
        UpdateProductStockUseCase,
        UpdateProductNameUseCase {

    private final FranchisePersistencePort persistencePort;
    private final FranchiseLoader franchiseLoader;


    public ProductService(FranchisePersistencePort persistencePort, FranchiseLoader franchiseLoader) {
        this.persistencePort = persistencePort;
        this.franchiseLoader = franchiseLoader;
    }

    @Override
    public Mono<ProductResponse> addProduct(String franchiseId, String branchId, String productName, int stock) {
        return franchiseLoader.loadFranchise(franchiseId)
                .flatMap(franchise -> {
                    Product newProduct = new Product(Id.newId(), new Name(productName), new Stock(stock));
                    franchise.addProduct(new Id(branchId), newProduct);
                    return persistencePort.save(franchise)
                            .map(saved -> FranchiseMapper.toResponse(newProduct));
                });
    }

    @Override
    public Mono<FranchiseResponse> deleteProduct(String franchiseId, String branchId, String productId) {
        return franchiseLoader.loadFranchise(franchiseId)
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

        return franchiseLoader.loadFranchise(franchiseId)
                .flatMap(franchise -> {
                    franchise.updateStock(branch, product, new Stock(stock));
                    return persistencePort.save(franchise)
                            .map(saved -> FranchiseMapper.toResponse(saved.findProduct(branch, product)));
                });
    }

    @Override
    public Mono<ProductResponse> updateProductName(String franchiseId, String branchId, String productId, String name) {
        Id branch = new Id(branchId);
        Id product = new Id(productId);

        return franchiseLoader.loadFranchise(franchiseId)
                .flatMap(franchise -> {
                    franchise.updateProductName(branch, product, new Name(name));
                    return persistencePort.save(franchise)
                            .map(saved -> FranchiseMapper.toResponse(saved.findProduct(branch, product)));
                });
    }
}
