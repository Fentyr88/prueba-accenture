package com.springboot.reactor.pruebaaccenture.domain.model;

import com.springboot.reactor.pruebaaccenture.domain.exception.BranchNotFoundException;
import com.springboot.reactor.pruebaaccenture.domain.exception.DuplicateNameException;
import com.springboot.reactor.pruebaaccenture.domain.vo.Id;
import com.springboot.reactor.pruebaaccenture.domain.vo.Name;
import com.springboot.reactor.pruebaaccenture.domain.vo.Stock;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Franchise {

    private final Id id;
    private Name name;
    private final List<Branch> branches;

    public Franchise(Id id, Name name, List<Branch> branches) {
        this.id = id;
        this.name = name;
        this.branches = branches == null ? new ArrayList<>() : new ArrayList<>(branches);
    }

    public void updateName(Name name) {
        this.name = name;
    }

    public void addBranch(Branch branch) {
        boolean duplicated = branches.stream()
                .anyMatch(current -> current.getName().canonical().equals(branch.getName().canonical()));
        if (duplicated) {
            throw new DuplicateNameException("Branch already exists in franchise");
        }
        branches.add(branch);
    }

    public Branch findBranch(Id branchId) {
        return branches.stream()
                .filter(branch -> branch.getId().equals(branchId))
                .findFirst()
                .orElseThrow(() -> new BranchNotFoundException(branchId.value()));
    }

    public void addProduct(Id branchId, Product product) {
        findBranch(branchId).addProduct(product);
    }

    public void removeProduct(Id branchId, Id productId) {
        findBranch(branchId).removeProduct(productId);
    }

    public void updateStock(Id branchId, Id productId, Stock stock) {
        findBranch(branchId).findProduct(productId).updateStock(stock);
    }

    public void updateBranchName(Id branchId, Name updatedName) {
        Branch target = findBranch(branchId);
        boolean duplicated = branches.stream()
                .filter(branch -> !branch.getId().equals(branchId))
                .anyMatch(branch -> branch.getName().canonical().equals(updatedName.canonical()));
        if (duplicated) {
            throw new DuplicateNameException("Branch already exists in franchise");
        }
        target.updateName(updatedName);
    }

    public void updateProductName(Id branchId, Id productId, Name updatedName) {
        findBranch(branchId).updateProductName(productId, updatedName);
    }
}
