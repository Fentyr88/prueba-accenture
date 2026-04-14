package com.springboot.reactor.pruebaaccenture.infrastructure.entrypoint.rest;

import com.springboot.reactor.pruebaaccenture.application.dto.request.*;
import com.springboot.reactor.pruebaaccenture.application.dto.response.ApiResponse;
import com.springboot.reactor.pruebaaccenture.application.dto.response.FranchiseResponse;
import com.springboot.reactor.pruebaaccenture.application.dto.response.TopStockProductResponse;
import com.springboot.reactor.pruebaaccenture.application.port.in.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/franchises")
public class FranchiseController {

    private final CreateFranchiseUseCase createFranchiseUseCase;
    private final AddBranchUseCase addBranchUseCase;
    private final AddProductUseCase addProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;
    private final UpdateProductStockUseCase updateProductStockUseCase;
    private final GetTopStockByFranchiseUseCase getTopStockByFranchiseUseCase;
    private final UpdateFranchiseNameUseCase updateFranchiseNameUseCase;
    private final UpdateBranchNameUseCase updateBranchNameUseCase;
    private final UpdateProductNameUseCase updateProductNameUseCase;
    private final GetAllFranchisesUseCase getAllFranchisesUseCase;

    public FranchiseController(
            CreateFranchiseUseCase createFranchiseUseCase,
            AddBranchUseCase addBranchUseCase,
            AddProductUseCase addProductUseCase,
            DeleteProductUseCase deleteProductUseCase,
            UpdateProductStockUseCase updateProductStockUseCase,
            GetTopStockByFranchiseUseCase getTopStockByFranchiseUseCase,
            UpdateFranchiseNameUseCase updateFranchiseNameUseCase,
            UpdateBranchNameUseCase updateBranchNameUseCase,
            UpdateProductNameUseCase updateProductNameUseCase,
            GetAllFranchisesUseCase getAllFranchisesUseCase
    ) {
        this.createFranchiseUseCase = createFranchiseUseCase;
        this.addBranchUseCase = addBranchUseCase;
        this.addProductUseCase = addProductUseCase;
        this.deleteProductUseCase = deleteProductUseCase;
        this.updateProductStockUseCase = updateProductStockUseCase;
        this.getTopStockByFranchiseUseCase = getTopStockByFranchiseUseCase;
        this.updateFranchiseNameUseCase = updateFranchiseNameUseCase;
        this.updateBranchNameUseCase = updateBranchNameUseCase;
        this.updateProductNameUseCase = updateProductNameUseCase;
        this.getAllFranchisesUseCase = getAllFranchisesUseCase;
    }

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<FranchiseResponse>>> create(@Valid @RequestBody CreateFranchiseRequest request) {
        return createFranchiseUseCase.createFranchise(request.name())
                .map(data -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(new ApiResponse<>(201, "Franchise created", data)));
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<FranchiseResponse>>>> getAll() {
        return getAllFranchisesUseCase.getAllFranchises().collectList()
                .map(data -> ResponseEntity.ok(new ApiResponse<>(200, "Franchises found", data)));
    }

    @PostMapping("/{franchiseId}/branches")
    public Mono<ResponseEntity<ApiResponse<FranchiseResponse>>> addBranch(
            @PathVariable String franchiseId,
            @Valid @RequestBody AddBranchRequest request
    ) {
        return addBranchUseCase.addBranch(franchiseId, request.name())
                .map(data -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(new ApiResponse<>(201, "Branch created", data)));
    }

    @PostMapping("/{franchiseId}/branches/{branchId}/products")
    public Mono<ResponseEntity<ApiResponse<FranchiseResponse>>> addProduct(
            @PathVariable String franchiseId,
            @PathVariable String branchId,
            @Valid @RequestBody AddProductRequest request
    ) {
        return addProductUseCase.addProduct(franchiseId, branchId, request.name(), request.stock())
                .map(data -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(new ApiResponse<>(201, "Product created", data)));
    }

    @DeleteMapping("/{franchiseId}/branches/{branchId}/products/{productId}")
    public Mono<ResponseEntity<ApiResponse<FranchiseResponse>>> deleteProduct(
            @PathVariable String franchiseId, @PathVariable String branchId, @PathVariable String productId
    ) {
        return deleteProductUseCase.deleteProduct(franchiseId, branchId, productId)
                .map(data -> ResponseEntity.ok(new ApiResponse<>(200, "Product deleted", data)));
    }

    @PatchMapping("/{franchiseId}/branches/{branchId}/products/{productId}/stock")
    public Mono<ResponseEntity<ApiResponse<FranchiseResponse>>> updateStock(
            @PathVariable String franchiseId, @PathVariable String branchId, @PathVariable String productId,
            @Valid @RequestBody UpdateStockRequest request
    ) {
        return updateProductStockUseCase.updateProductStock(franchiseId, branchId, productId, request.stock())
                .map(data -> ResponseEntity.ok(new ApiResponse<>(200, "Stock updated", data)));
    }

    @PutMapping("/{franchiseId}/name")
    public Mono<ResponseEntity<ApiResponse<FranchiseResponse>>> updateFranchiseName(
            @PathVariable String franchiseId, @Valid @RequestBody UpdateNameRequest request
    ) {
        return updateFranchiseNameUseCase.updateFranchiseName(franchiseId, request.name())
                .map(data -> ResponseEntity.ok(new ApiResponse<>(200, "Franchise name updated", data)));
    }

    @PutMapping("/{franchiseId}/branches/{branchId}/name")
    public Mono<ResponseEntity<ApiResponse<FranchiseResponse>>> updateBranchName(
            @PathVariable String franchiseId, @PathVariable String branchId, @Valid @RequestBody UpdateNameRequest request
    ) {
        return updateBranchNameUseCase.updateBranchName(franchiseId, branchId, request.name())
                .map(data -> ResponseEntity.ok(new ApiResponse<>(200, "Branch name updated", data)));
    }

    @PutMapping("/{franchiseId}/branches/{branchId}/products/{productId}/name")
    public Mono<ResponseEntity<ApiResponse<FranchiseResponse>>> updateProductName(
            @PathVariable String franchiseId, @PathVariable String branchId, @PathVariable String productId,
            @Valid @RequestBody UpdateNameRequest request
    ) {
        return updateProductNameUseCase.updateProductName(franchiseId, branchId, productId, request.name())
                .map(data -> ResponseEntity.ok(new ApiResponse<>(200, "Product name updated", data)));
    }

    @GetMapping("/{franchiseId}/top-stock-products")
    public Mono<ResponseEntity<ApiResponse<List<TopStockProductResponse>>>> getTopStock(@PathVariable String franchiseId) {
        return getTopStockByFranchiseUseCase.getTopStockByFranchise(franchiseId)
                .collectList()
                .map(data -> ResponseEntity.ok(new ApiResponse<>(200, "Top stock products", data)));
    }
}
