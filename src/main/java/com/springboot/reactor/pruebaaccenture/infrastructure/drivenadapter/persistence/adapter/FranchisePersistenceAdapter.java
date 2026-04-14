package com.springboot.reactor.pruebaaccenture.infrastructure.drivenadapter.persistence.adapter;

import com.springboot.reactor.pruebaaccenture.application.port.out.FranchisePersistencePort;
import com.springboot.reactor.pruebaaccenture.domain.model.Franchise;
import com.springboot.reactor.pruebaaccenture.domain.vo.Id;
import com.springboot.reactor.pruebaaccenture.domain.vo.Name;
import com.springboot.reactor.pruebaaccenture.infrastructure.drivenadapter.persistence.repository.FranchiseRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class FranchisePersistenceAdapter implements FranchisePersistencePort {

    private final FranchiseRepository repository;
    private final FranchisePersistenceMapper mapper;

    public FranchisePersistenceAdapter(FranchiseRepository repository, FranchisePersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<Franchise> save(Franchise franchise) {
        return repository.saveAggregate(
                        mapper.toFranchiseEntity(franchise),
                        mapper.toBranchEntities(franchise),
                        mapper.toProductEntities(franchise)
                )
                .thenReturn(franchise);
    }

    @Override
    public Mono<Franchise> findById(Id franchiseId) {
        String id = franchiseId.value();

        return Mono.zip(
                        repository.findFranchiseById(id),
                        repository.findBranchesByFranchiseId(id).collectList(),
                        repository.findProductsByFranchiseId(id).collectList()
                )
                .map(tuple -> mapper.toDomain(tuple.getT1(), tuple.getT2(), tuple.getT3()));
    }

    @Override
    public Mono<Boolean> existsByName(Name name) {
        return repository.existsFranchiseByName(name.value());
    }

    @Override
    public Flux<Franchise> findAll() {
        return repository.findAllFranchises()
                .flatMap(entity -> Mono.zip(
                                        Mono.just(entity),
                                        repository.findBranchesByFranchiseId(entity.getFranchiseId()).collectList(),
                                        repository.findProductsByFranchiseId(entity.getFranchiseId()).collectList()
                                )
                                .map(tuple -> mapper.toDomain(tuple.getT1(), tuple.getT2(), tuple.getT3()))
                );
    }
}
