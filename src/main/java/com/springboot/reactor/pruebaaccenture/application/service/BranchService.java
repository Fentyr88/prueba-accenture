package com.springboot.reactor.pruebaaccenture.application.service;

import com.springboot.reactor.pruebaaccenture.application.dto.response.BranchResponse;
import com.springboot.reactor.pruebaaccenture.application.mapper.FranchiseMapper;
import com.springboot.reactor.pruebaaccenture.application.port.in.AddBranchUseCase;
import com.springboot.reactor.pruebaaccenture.application.port.in.UpdateBranchNameUseCase;
import com.springboot.reactor.pruebaaccenture.application.port.out.FranchisePersistencePort;
import com.springboot.reactor.pruebaaccenture.application.service.helper.FranchiseLoader;
import com.springboot.reactor.pruebaaccenture.domain.model.Branch;
import com.springboot.reactor.pruebaaccenture.domain.vo.Id;
import com.springboot.reactor.pruebaaccenture.domain.vo.Name;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class BranchService implements AddBranchUseCase, UpdateBranchNameUseCase {

    private final FranchisePersistencePort persistencePort;
    private final FranchiseLoader franchiseLoader;

    public BranchService(FranchisePersistencePort persistencePort, FranchiseLoader franchiseLoader) {
        this.persistencePort = persistencePort;
        this.franchiseLoader = franchiseLoader;
    }

    @Override
    public Mono<BranchResponse> addBranch(String franchiseId, String branchName) {
        return franchiseLoader.loadFranchise(franchiseId)
                .flatMap(franchise -> {
                    Branch newBranch = new Branch(Id.newId(), new Name(branchName), List.of());
                    franchise.addBranch(newBranch);
                    return persistencePort.save(franchise)
                            .map(saved -> FranchiseMapper.toResponse(newBranch));
                });
    }

    @Override
    public Mono<BranchResponse> updateBranchName(String franchiseId, String branchId, String name) {
        Id branch = new Id(branchId);

        return franchiseLoader.loadFranchise(franchiseId)
                .flatMap(franchise -> {
                    franchise.updateBranchName(branch, new Name(name));
                    return persistencePort.save(franchise)
                            .map(saved -> FranchiseMapper.toResponse(saved.findBranch(branch)));
                });
    }
}
