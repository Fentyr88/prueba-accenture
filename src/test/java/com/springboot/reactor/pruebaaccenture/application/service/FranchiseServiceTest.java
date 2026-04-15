package com.springboot.reactor.pruebaaccenture.application.service;

import com.springboot.reactor.pruebaaccenture.application.port.out.FranchisePersistencePort;
import com.springboot.reactor.pruebaaccenture.application.service.helper.FranchiseLoader;
import com.springboot.reactor.pruebaaccenture.domain.exception.DuplicateNameException;
import com.springboot.reactor.pruebaaccenture.domain.model.Branch;
import com.springboot.reactor.pruebaaccenture.domain.model.Franchise;
import com.springboot.reactor.pruebaaccenture.domain.model.Product;
import com.springboot.reactor.pruebaaccenture.domain.vo.Id;
import com.springboot.reactor.pruebaaccenture.domain.vo.Name;
import com.springboot.reactor.pruebaaccenture.domain.vo.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FranchiseServiceTest {

    private static final String FRANCHISE_ID_1 = "11111111-1111-1111-1111-111111111111";
    private static final String FRANCHISE_ID_2 = "22222222-2222-2222-2222-222222222222";
    private static final String BRANCH_ID_EAST = "aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaa1";
    private static final String PRODUCT_ID_1 = "bbbbbbb1-bbbb-bbbb-bbbb-bbbbbbbbbbb1";
    private static final String PRODUCT_ID_2 = "bbbbbbb2-bbbb-bbbb-bbbb-bbbbbbbbbbb2";
    private static final String PRODUCT_ID_3 = "bbbbbbb3-bbbb-bbbb-bbbb-bbbbbbbbbbb3";

    @Mock
    FranchisePersistencePort persistencePort;

    @Mock
    FranchiseLoader franchiseLoader;

    FranchiseService service;

    @BeforeEach
    void setUp() {
        service = new FranchiseService(persistencePort, franchiseLoader);
    }

    @Test
    void shouldCreateFranchiseWhenNameIsAvailable() {
        when(persistencePort.existsByName(any(Name.class))).thenReturn(Mono.just(false));
        when(persistencePort.save(any(Franchise.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.createFranchise("Franquicia Uno"))
                .assertNext(response -> {
                    assertEquals("Franquicia Uno", response.name());
                    assertNotNull(response.id());
                    assertTrue(response.branches().isEmpty());
                })
                .verifyComplete();

        verify(persistencePort).existsByName(any(Name.class));
        verify(persistencePort).save(any(Franchise.class));
    }

    @Test
    void shouldFailCreateFranchiseWhenNameAlreadyExists() {
        when(persistencePort.existsByName(any(Name.class))).thenReturn(Mono.just(true));

        StepVerifier.create(service.createFranchise("Franquicia Uno"))
                .expectErrorSatisfies(error -> {
                    assertInstanceOf(DuplicateNameException.class, error);
                    assertEquals("Franchise already exists", error.getMessage());
                })
                .verify();

        verify(persistencePort).existsByName(any(Name.class));
        verify(persistencePort, never()).save(any());
    }

    @Test
    void shouldUpdateFranchiseNameWhenCanonicalIsSame() {
        Franchise franchise = new Franchise(new Id(FRANCHISE_ID_1), new Name("Franquicia Uno"), List.of());

        when(franchiseLoader.loadFranchise(FRANCHISE_ID_1)).thenReturn(Mono.just(franchise));
        when(persistencePort.existsByName(any(Name.class))).thenReturn(Mono.just(true));
        when(persistencePort.save(any(Franchise.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.updateFranchiseName(FRANCHISE_ID_1, "franquicia uno"))
                .assertNext(response -> assertEquals("franquicia uno", response.name()))
                .verifyComplete();

        verify(persistencePort).save(any(Franchise.class));
    }

    @Test
    void shouldReturnAllFranchises() {
        Franchise franchiseOne = new Franchise(new Id(FRANCHISE_ID_1), new Name("Franquicia Uno"), List.of());
        Franchise franchiseTwo = new Franchise(new Id(FRANCHISE_ID_2), new Name("Franquicia Dos"), List.of());

        when(persistencePort.findAll()).thenReturn(Flux.just(franchiseOne, franchiseTwo));

        StepVerifier.create(service.getAllFranchises())
                .assertNext(response -> assertEquals("Franquicia Uno", response.name()))
                .assertNext(response -> assertEquals("Franquicia Dos", response.name()))
                .verifyComplete();
    }

    @Test
    void shouldReturnTopStockProductsByBranch() {
        Product coffeeProduct = new Product(new Id(PRODUCT_ID_1), new Name("Cafe"), new Stock(10));
        Product breadProduct = new Product(new Id(PRODUCT_ID_2), new Name("Pan"), new Stock(20));
        Product teaProduct = new Product(new Id(PRODUCT_ID_3), new Name("Te"), new Stock(20));

        Branch eastBranch = new Branch(
                new Id(BRANCH_ID_EAST),
                new Name("Sucursal Oriente"),
                List.of(coffeeProduct, breadProduct, teaProduct)
        );

        Franchise franchise = new Franchise(
                new Id(FRANCHISE_ID_1),
                new Name("Franquicia Uno"),
                List.of(eastBranch)
        );

        when(franchiseLoader.loadFranchise(FRANCHISE_ID_1)).thenReturn(Mono.just(franchise));

        StepVerifier.create(service.getTopStockByFranchise(FRANCHISE_ID_1))
                .expectNextCount(2)
                .verifyComplete();
    }
}
