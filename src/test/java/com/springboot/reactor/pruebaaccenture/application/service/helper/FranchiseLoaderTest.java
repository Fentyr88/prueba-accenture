package com.springboot.reactor.pruebaaccenture.application.service.helper;

import com.springboot.reactor.pruebaaccenture.application.port.out.FranchisePersistencePort;
import com.springboot.reactor.pruebaaccenture.domain.exception.FranchiseNotFoundException;
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

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FranchiseLoaderTest {

    private static final String FRANCHISE_ID_1 = "11111111-1111-1111-1111-111111111111";
    private static final String FRANCHISE_ID_NOT_FOUND = "99999999-9999-9999-9999-999999999999";

    @Mock
    FranchisePersistencePort persistencePort;

    FranchiseLoader loader;

    @BeforeEach
    void setUp() {
        loader = new FranchiseLoader(persistencePort);
    }

    @Test
    void shouldLoadFranchiseWhenExists() {
        Franchise franchise = new Franchise(new Id(FRANCHISE_ID_1), new Name("Franquicia Uno"), List.of());
        when(persistencePort.findById(new Id(FRANCHISE_ID_1))).thenReturn(Mono.just(franchise));

        StepVerifier.create(loader.loadFranchise(FRANCHISE_ID_1))
                .expectNext(franchise)
                .verifyComplete();
    }

    @Test
    void shouldFailWhenFranchiseDoesNotExist() {
        when(persistencePort.findById(new Id(FRANCHISE_ID_NOT_FOUND))).thenReturn(Mono.empty());

        StepVerifier.create(loader.loadFranchise(FRANCHISE_ID_NOT_FOUND))
                .expectError(FranchiseNotFoundException.class)
                .verify();
    }
}
