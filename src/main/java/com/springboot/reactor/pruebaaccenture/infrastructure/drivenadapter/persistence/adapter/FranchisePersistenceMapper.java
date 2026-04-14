package com.springboot.reactor.pruebaaccenture.infrastructure.drivenadapter.persistence.adapter;

import com.springboot.reactor.pruebaaccenture.domain.model.Branch;
import com.springboot.reactor.pruebaaccenture.domain.model.Franchise;
import com.springboot.reactor.pruebaaccenture.domain.model.Product;
import com.springboot.reactor.pruebaaccenture.domain.vo.Id;
import com.springboot.reactor.pruebaaccenture.domain.vo.Name;
import com.springboot.reactor.pruebaaccenture.domain.vo.Stock;
import com.springboot.reactor.pruebaaccenture.infrastructure.drivenadapter.persistence.entity.BranchEntity;
import com.springboot.reactor.pruebaaccenture.infrastructure.drivenadapter.persistence.entity.FranchiseEntity;
import com.springboot.reactor.pruebaaccenture.infrastructure.drivenadapter.persistence.entity.ProductEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FranchisePersistenceMapper {

    public Franchise toDomain(
            FranchiseEntity franchiseEntity,
            List<BranchEntity> branchEntities,
            List<ProductEntity> productEntities
    ) {
        List<BranchEntity> safeBranches = branchEntities == null ? List.of() : branchEntities;
        List<ProductEntity> safeProducts = productEntities == null ? List.of() : productEntities;

        Map<String, List<Product>> productsByBranch = safeProducts.stream()
                .collect(Collectors.groupingBy(
                        ProductEntity::getBranchId,
                        Collectors.mapping(this::toProductDomain, Collectors.toList())
                ));

        List<Branch> branches = safeBranches.stream()
                .map(branch -> new Branch(
                        new Id(branch.getBranchId()),
                        new Name(branch.getName()),
                        productsByBranch.getOrDefault(branch.getBranchId(), List.of())
                ))
                .toList();

        return new Franchise(
                new Id(franchiseEntity.getFranchiseId()),
                new Name(franchiseEntity.getName()),
                branches
        );
    }

    public FranchiseEntity toFranchiseEntity(Franchise franchise) {
        return FranchiseEntity.fromDomain(franchise);
    }

    public List<BranchEntity> toBranchEntities(Franchise franchise) {
        String franchiseId = franchise.getId().value();
        return franchise.getBranches().stream()
                .map(branch -> BranchEntity.fromDomain(franchiseId, branch))
                .toList();
    }

    public List<ProductEntity> toProductEntities(Franchise franchise) {
        String franchiseId = franchise.getId().value();
        return franchise.getBranches().stream()
                .flatMap(branch -> branch.getProducts().stream()
                        .map(product -> ProductEntity.fromDomain(franchiseId, branch.getId().value(), product)))
                .toList();
    }

    private Product toProductDomain(ProductEntity entity) {
        return new Product(
                new Id(entity.getProductId()),
                new Name(entity.getName()),
                new Stock(entity.getStock())
        );
    }
}
