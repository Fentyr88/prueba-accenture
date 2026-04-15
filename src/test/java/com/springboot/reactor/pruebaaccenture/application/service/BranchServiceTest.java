package com.springboot.reactor.pruebaaccenture.application.service;

import com.springboot.reactor.pruebaaccenture.application.port.out.FranchisePersistencePort;
import com.springboot.reactor.pruebaaccenture.application.service.helper.FranchiseLoader;
import com.springboot.reactor.pruebaaccenture.domain.exception.DuplicateNameException;
import com.springboot.reactor.pruebaaccenture.domain.model.Branch;
import com.springboot.reactor.pruebaaccenture.domain.model.Franchise;
import com.springboot.reactor.pruebaaccenture.domain.vo.Id;
import com.springboot.reactor.pruebaaccenture.domain.vo.Name;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BranchServiceTest {

    private static final String FRANCHISE_ID_1 = "11111111-1111-1111-1111-111111111111";
    private static final String BRANCH_ID_EAST = "aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaa1";

    @Mock
    FranchisePersistencePort persistencePort;

    @Mock
    FranchiseLoader franchiseLoader;

    BranchService service;

    @BeforeEach
    void setUp() {
        service = new BranchService(persistencePort, franchiseLoader);
    }

    @Test
    void shouldAddBranch() {
        Franchise franchise = new Franchise(new Id(FRANCHISE_ID_1), new Name("Franquicia Uno"), List.of());

        when(franchiseLoader.loadFranchise(FRANCHISE_ID_1)).thenReturn(Mono.just(franchise));
        when(persistencePort.save(any(Franchise.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.addBranch(FRANCHISE_ID_1, "Sucursal Oriente"))
                .assertNext(response -> {
                    assertNotNull(response.id());
                    assertEquals("Sucursal Oriente", response.name());
                })
                .verifyComplete();
    }

    @Test
    void shouldFailAddBranchWhenNameAlreadyExists() {
        Branch existingBranch = new Branch(new Id(BRANCH_ID_EAST), new Name("Sucursal Oriente"), List.of());
        Franchise franchise = new Franchise(
                new Id(FRANCHISE_ID_1),
                new Name("Franquicia Uno"),
                List.of(existingBranch)
        );

        when(franchiseLoader.loadFranchise(FRANCHISE_ID_1)).thenReturn(Mono.just(franchise));

        StepVerifier.create(service.addBranch(FRANCHISE_ID_1, "sucursal oriente"))
                .expectErrorSatisfies(error -> assertInstanceOf(DuplicateNameException.class, error))
                .verify();
    }

    @Test
    void shouldUpdateBranchName() {
        Branch existingBranch = new Branch(new Id(BRANCH_ID_EAST), new Name("Sucursal Oriente"), List.of());
        Franchise franchise = new Franchise(
                new Id(FRANCHISE_ID_1),
                new Name("Franquicia Uno"),
                List.of(existingBranch)
        );

        when(franchiseLoader.loadFranchise(FRANCHISE_ID_1)).thenReturn(Mono.just(franchise));
        when(persistencePort.save(any(Franchise.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.updateBranchName(FRANCHISE_ID_1, BRANCH_ID_EAST, "Sucursal Occidente"))
                .assertNext(response -> assertEquals("Sucursal Occidente", response.name()))
                .verifyComplete();
    }
}
