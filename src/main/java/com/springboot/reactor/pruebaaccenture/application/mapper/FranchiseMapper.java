package com.springboot.reactor.pruebaaccenture.application.mapper;

import com.springboot.reactor.pruebaaccenture.application.dto.response.*;
import com.springboot.reactor.pruebaaccenture.domain.model.Branch;
import com.springboot.reactor.pruebaaccenture.domain.model.Franchise;
import com.springboot.reactor.pruebaaccenture.domain.model.Product;

import java.util.List;

public final class FranchiseMapper {
    private FranchiseMapper() {}

    public static FranchiseResponse toResponse(Franchise franchise) {
        return new FranchiseResponse(
                franchise.getId().value(),
                franchise.getName().value(),
                franchise.getBranches().stream().map(FranchiseMapper::toResponse).toList()
        );
    }

    public static BranchResponse toResponse(Branch branch) {
        return new BranchResponse(
                branch.getId().value(),
                branch.getName().value(),
                branch.getProducts().stream().map(FranchiseMapper::toResponse).toList()
        );
    }

    public static ProductResponse toResponse(Product product) {
        return new ProductResponse(product.getId().value(), product.getName().value(), product.getStock().value());
    }

    public static List<TopStockProductResponse> toTopStockResponse(Franchise franchise, Branch branch) {
        return branch.topStockProducts().stream()
                .map(product -> new TopStockProductResponse(
                        franchise.getId().value(), franchise.getName().value(),
                        branch.getId().value(), branch.getName().value(),
                        product.getId().value(), product.getName().value(),
                        product.getStock().value()
                ))
                .toList();
    }
}
