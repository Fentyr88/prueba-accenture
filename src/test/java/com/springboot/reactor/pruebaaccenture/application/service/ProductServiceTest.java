package com.springboot.reactor.pruebaaccenture.application.service;

import com.springboot.reactor.pruebaaccenture.application.port.out.FranchisePersistencePort;
import com.springboot.reactor.pruebaaccenture.application.service.helper.FranchiseLoader;
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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    private static final String FRANCHISE_ID_1 = "11111111-1111-1111-1111-111111111111";
    private static final String BRANCH_ID_EAST = "aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaa1";
    private static final String PRODUCT_ID_COFFEE = "bbbbbbb1-bbbb-bbbb-bbbb-bbbbbbbbbbb1";

    @Mock
    FranchisePersistencePort persistencePort;

    @Mock
    FranchiseLoader franchiseLoader;

    ProductService service;

    @BeforeEach
    void setUp() {
        service = new ProductService(persistencePort, franchiseLoader);
    }

    @Test
    void shouldAddProduct() {
        Branch branch = new Branch(new Id(BRANCH_ID_EAST), new Name("Sucursal Oriente"), List.of());
        Franchise franchise = new Franchise(new Id(FRANCHISE_ID_1), new Name("Franquicia Uno"), List.of(branch));

        when(franchiseLoader.loadFranchise(FRANCHISE_ID_1)).thenReturn(Mono.just(franchise));
        when(persistencePort.save(any(Franchise.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.addProduct(FRANCHISE_ID_1, BRANCH_ID_EAST, "Cafe", 10))
                .assertNext(response -> {
                    assertEquals("Cafe", response.name());
                    assertEquals(10, response.stock());
                })
                .verifyComplete();
    }

    @Test
    void shouldDeleteProduct() {
        Product coffeeProduct = new Product(new Id(PRODUCT_ID_COFFEE), new Name("Cafe"), new Stock(5));
        Branch branch = new Branch(new Id(BRANCH_ID_EAST), new Name("Sucursal Oriente"), List.of(coffeeProduct));
        Franchise franchise = new Franchise(new Id(FRANCHISE_ID_1), new Name("Franquicia Uno"), List.of(branch));

        when(franchiseLoader.loadFranchise(FRANCHISE_ID_1)).thenReturn(Mono.just(franchise));
        when(persistencePort.save(any(Franchise.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.deleteProduct(FRANCHISE_ID_1, BRANCH_ID_EAST, PRODUCT_ID_COFFEE))
                .assertNext(response -> assertEquals("Franquicia Uno", response.name()))
                .verifyComplete();
    }

    @Test
    void shouldUpdateProductStock() {
        Product coffeeProduct = new Product(new Id(PRODUCT_ID_COFFEE), new Name("Cafe"), new Stock(5));
        Branch branch = new Branch(new Id(BRANCH_ID_EAST), new Name("Sucursal Oriente"), List.of(coffeeProduct));
        Franchise franchise = new Franchise(new Id(FRANCHISE_ID_1), new Name("Franquicia Uno"), List.of(branch));

        when(franchiseLoader.loadFranchise(FRANCHISE_ID_1)).thenReturn(Mono.just(franchise));
        when(persistencePort.save(any(Franchise.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.updateProductStock(FRANCHISE_ID_1, BRANCH_ID_EAST, PRODUCT_ID_COFFEE, 99))
                .assertNext(response -> assertEquals(99, response.stock()))
                .verifyComplete();
    }

    @Test
    void shouldUpdateProductName() {
        Product coffeeProduct = new Product(new Id(PRODUCT_ID_COFFEE), new Name("Cafe"), new Stock(5));
        Branch branch = new Branch(new Id(BRANCH_ID_EAST), new Name("Sucursal Oriente"), List.of(coffeeProduct));
        Franchise franchise = new Franchise(new Id(FRANCHISE_ID_1), new Name("Franquicia Uno"), List.of(branch));

        when(franchiseLoader.loadFranchise(FRANCHISE_ID_1)).thenReturn(Mono.just(franchise));
        when(persistencePort.save(any(Franchise.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.updateProductName(FRANCHISE_ID_1, BRANCH_ID_EAST, PRODUCT_ID_COFFEE, "Cafe Premium"))
                .assertNext(response -> assertEquals("Cafe Premium", response.name()))
                .verifyComplete();
    }
}
