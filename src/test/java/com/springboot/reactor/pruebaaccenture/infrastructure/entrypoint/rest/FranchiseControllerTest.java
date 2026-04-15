package com.springboot.reactor.pruebaaccenture.infrastructure.entrypoint.rest;

import com.springboot.reactor.pruebaaccenture.application.dto.response.BranchResponse;
import com.springboot.reactor.pruebaaccenture.application.dto.response.FranchiseResponse;
import com.springboot.reactor.pruebaaccenture.application.dto.response.ProductResponse;
import com.springboot.reactor.pruebaaccenture.application.dto.response.TopStockProductResponse;
import com.springboot.reactor.pruebaaccenture.application.port.in.AddBranchUseCase;
import com.springboot.reactor.pruebaaccenture.application.port.in.AddProductUseCase;
import com.springboot.reactor.pruebaaccenture.application.port.in.CreateFranchiseUseCase;
import com.springboot.reactor.pruebaaccenture.application.port.in.DeleteProductUseCase;
import com.springboot.reactor.pruebaaccenture.application.port.in.GetAllFranchisesUseCase;
import com.springboot.reactor.pruebaaccenture.application.port.in.GetTopStockByFranchiseUseCase;
import com.springboot.reactor.pruebaaccenture.application.port.in.UpdateBranchNameUseCase;
import com.springboot.reactor.pruebaaccenture.application.port.in.UpdateFranchiseNameUseCase;
import com.springboot.reactor.pruebaaccenture.application.port.in.UpdateProductNameUseCase;
import com.springboot.reactor.pruebaaccenture.application.port.in.UpdateProductStockUseCase;
import com.springboot.reactor.pruebaaccenture.domain.exception.DuplicateNameException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = FranchiseController.class)
@Import(GlobalExceptionHandler.class)
class FranchiseControllerTest {

    private static final String FRANCHISE_ID_1 = "11111111-1111-1111-1111-111111111111";
    private static final String FRANCHISE_ID_2 = "22222222-2222-2222-2222-222222222222";
    private static final String BRANCH_ID_EAST = "aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaa1";
    private static final String PRODUCT_ID_COFFEE = "bbbbbbb1-bbbb-bbbb-bbbb-bbbbbbbbbbb1";

    @Autowired
    WebTestClient webTestClient;

    @MockitoBean CreateFranchiseUseCase createFranchiseUseCase;
    @MockitoBean AddBranchUseCase addBranchUseCase;
    @MockitoBean AddProductUseCase addProductUseCase;
    @MockitoBean DeleteProductUseCase deleteProductUseCase;
    @MockitoBean UpdateProductStockUseCase updateProductStockUseCase;
    @MockitoBean GetTopStockByFranchiseUseCase getTopStockByFranchiseUseCase;
    @MockitoBean UpdateFranchiseNameUseCase updateFranchiseNameUseCase;
    @MockitoBean UpdateBranchNameUseCase updateBranchNameUseCase;
    @MockitoBean UpdateProductNameUseCase updateProductNameUseCase;
    @MockitoBean GetAllFranchisesUseCase getAllFranchisesUseCase;

    @Test
    void shouldCreateFranchiseWith201() {
        FranchiseResponse response = new FranchiseResponse(FRANCHISE_ID_1, "Franquicia Uno", List.of());
        when(createFranchiseUseCase.createFranchise("Franquicia Uno")).thenReturn(Mono.just(response));

        webTestClient.post()
                .uri("/api/v1/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"name":"Franquicia Uno"}
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.status").isEqualTo(201)
                .jsonPath("$.message").isEqualTo("Franchise created")
                .jsonPath("$.data.id").isEqualTo(FRANCHISE_ID_1)
                .jsonPath("$.data.name").isEqualTo("Franquicia Uno");
    }

    @Test
    void shouldReturn400WhenPayloadIsInvalid() {
        webTestClient.post()
                .uri("/api/v1/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"name":""}
                        """)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message").value(containsString("name"));
    }

    @Test
    void shouldReturn409WhenFranchiseNameIsDuplicated() {
        when(createFranchiseUseCase.createFranchise("Franquicia Uno"))
                .thenReturn(Mono.error(new DuplicateNameException("Franchise already exists")));

        webTestClient.post()
                .uri("/api/v1/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"name":"Franquicia Uno"}
                        """)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.status").isEqualTo(409)
                .jsonPath("$.message").isEqualTo("Franchise already exists");
    }

    @Test
    void shouldReturnAllFranchisesWith200() {
        when(getAllFranchisesUseCase.getAllFranchises()).thenReturn(Flux.just(
                new FranchiseResponse(FRANCHISE_ID_1, "Franquicia Uno", List.of()),
                new FranchiseResponse(FRANCHISE_ID_2, "Franquicia Dos", List.of())
        ));

        webTestClient.get()
                .uri("/api/v1/franchises")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo(200)
                .jsonPath("$.data.length()").isEqualTo(2);
    }

    @Test
    void shouldAddBranchWith201() {
        BranchResponse response = new BranchResponse(BRANCH_ID_EAST, "Sucursal Oriente", List.of());
        when(addBranchUseCase.addBranch(FRANCHISE_ID_1, "Sucursal Oriente")).thenReturn(Mono.just(response));

        webTestClient.post()
                .uri("/api/v1/franchises/{franchiseId}/branches", FRANCHISE_ID_1)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"name":"Sucursal Oriente"}
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.status").isEqualTo(201)
                .jsonPath("$.data.id").isEqualTo(BRANCH_ID_EAST)
                .jsonPath("$.data.name").isEqualTo("Sucursal Oriente");
    }

    @Test
    void shouldUpdateProductStockWith200() {
        when(updateProductStockUseCase.updateProductStock(anyString(), anyString(), anyString(), anyInt()))
                .thenReturn(Mono.just(new ProductResponse(PRODUCT_ID_COFFEE, "Cafe", 77)));

        webTestClient.patch()
                .uri("/api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}/stock",
                        FRANCHISE_ID_1, BRANCH_ID_EAST, PRODUCT_ID_COFFEE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"stock":77}
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo(200)
                .jsonPath("$.data.stock").isEqualTo(77);
    }

    @Test
    void shouldReturnTopStockProductsWith200() {
        when(getTopStockByFranchiseUseCase.getTopStockByFranchise(FRANCHISE_ID_1))
                .thenReturn(Flux.just(
                        new TopStockProductResponse(
                                FRANCHISE_ID_1, "Franquicia Uno",
                                BRANCH_ID_EAST, "Sucursal Oriente",
                                PRODUCT_ID_COFFEE, "Cafe", 77
                        )
                ));

        webTestClient.get()
                .uri("/api/v1/franchises/{franchiseId}/top-stock-products", FRANCHISE_ID_1)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo(200)
                .jsonPath("$.data.length()").isEqualTo(1)
                .jsonPath("$.data[0].productName").isEqualTo("Cafe");
    }
}
