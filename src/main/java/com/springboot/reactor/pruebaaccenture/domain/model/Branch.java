package com.springboot.reactor.pruebaaccenture.domain.model;

import com.springboot.reactor.pruebaaccenture.domain.exception.DuplicateNameException;
import com.springboot.reactor.pruebaaccenture.domain.exception.ProductNotFoundException;
import com.springboot.reactor.pruebaaccenture.domain.vo.Id;
import com.springboot.reactor.pruebaaccenture.domain.vo.Name;
import lombok.Getter;

import java.util.*;

@Getter
public class Branch {
    private final Id id;
    private Name name;
    private final List<Product> products;

    public Branch(Id id, Name name, List<Product> products) {
        this.id = id;
        this.name = name;
        this.products = products == null ? new ArrayList<>() : new ArrayList<>(products);
    }

    public void updateName(Name name) {
        this.name = name;
    }

    public void addProduct(Product product) {
        boolean duplicated = products.stream()
                .anyMatch(current -> current.getName().canonical().equals(product.getName().canonical()));
        if (duplicated) {
            throw new DuplicateNameException("Product already exists in branch");
        }
        products.add(product);
    }

    public void removeProduct(Id productId) {
        boolean removed = products.removeIf(product -> product.getId().equals(productId));
        if (!removed) {
            throw new ProductNotFoundException(productId.value());
        }
    }

    public Product findProduct(Id productId) {
        return products.stream()
                .filter(product -> product.getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ProductNotFoundException(productId.value()));
    }

    public void updateProductName(Id productId, Name updatedName) {
        Product target = findProduct(productId);
        boolean duplicated = products.stream()
                .filter(product -> !product.getId().equals(productId))
                .anyMatch(product -> product.getName().canonical().equals(updatedName.canonical()));
        if (duplicated) {
            throw new DuplicateNameException("Product already exists in branch");
        }
        target.updateName(updatedName);
    }

    public List<Product> topStockProducts() {
        if (products.isEmpty()) {
            return Collections.emptyList();
        }

        int maxStock = products.stream()
                .mapToInt(product -> product.getStock().value())
                .max()
                .orElse(0);

        return products.stream()
                .filter(product -> product.getStock().value() == maxStock)
                .toList();
    }
}
