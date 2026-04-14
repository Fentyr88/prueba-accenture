package com.springboot.reactor.pruebaaccenture.infrastructure.drivenadapter.persistence.repository;

import com.springboot.reactor.pruebaaccenture.infrastructure.drivenadapter.persistence.entity.BranchEntity;
import com.springboot.reactor.pruebaaccenture.infrastructure.drivenadapter.persistence.entity.FranchiseEntity;
import com.springboot.reactor.pruebaaccenture.infrastructure.drivenadapter.persistence.entity.ProductEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.Delete;
import software.amazon.awssdk.services.dynamodb.model.Put;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class FranchiseRepository {

    private static final String ENTITY_TYPE = "entityType";
    private static final String ENTITY_TYPE_FRANCHISE = "FRANCHISE";
    private static final String ENTITY_TYPE_BRANCH = "BRANCH";
    private static final String ENTITY_TYPE_PRODUCT = "PRODUCT";

    private static final String BRANCH_SK_PREFIX = "BRANCH#";
    private static final String PRODUCT_SK_PREFIX = "PRODUCT#";
    private static final int MAX_TRANSACTION_ITEMS = 100;

    private final DynamoDbAsyncClient dynamoDbAsyncClient;
    private final String tableName;
    private final DynamoDbAsyncTable<FranchiseEntity> franchiseTable;
    private final DynamoDbAsyncTable<BranchEntity> branchTable;
    private final DynamoDbAsyncTable<ProductEntity> productTable;

    public FranchiseRepository(
            DynamoDbEnhancedAsyncClient enhancedClient,
            DynamoDbAsyncClient dynamoDbAsyncClient,
            @Value("${aws.dynamodb.table-name}") String tableName
    ) {
        this.dynamoDbAsyncClient = dynamoDbAsyncClient;
        this.tableName = tableName;
        this.franchiseTable = enhancedClient.table(tableName, TableSchema.fromBean(FranchiseEntity.class));
        this.branchTable = enhancedClient.table(tableName, TableSchema.fromBean(BranchEntity.class));
        this.productTable = enhancedClient.table(tableName, TableSchema.fromBean(ProductEntity.class));
    }

    public Mono<Void> saveAggregate(
            FranchiseEntity franchise,
            List<BranchEntity> branches,
            List<ProductEntity> products
    ) {
        List<BranchEntity> safeBranches = branches == null ? List.of() : branches;
        List<ProductEntity> safeProducts = products == null ? List.of() : products;
        String franchiseId = franchise.getFranchiseId();

        return loadExistingAggregate(franchiseId)
                .flatMap(existing -> {
                    List<TransactWriteItem> tx = new ArrayList<>();
                    tx.add(put(toFranchiseMap(franchise)));
                    safeBranches.forEach(branch -> tx.add(put(toBranchMap(branch))));
                    safeProducts.forEach(product -> tx.add(put(toProductMap(product))));
                    tx.addAll(deleteOrphanBranches(existing.branches(), safeBranches));
                    tx.addAll(deleteOrphanProducts(existing.products(), safeProducts));
                    return executeTransaction(tx);
                });
    }

    public Mono<FranchiseEntity> findFranchiseById(String franchiseId) {
        Key key = Key.builder()
                .partitionValue(pk(franchiseId))
                .sortValue("FRANCHISE#" + franchiseId)
                .build();

        return Mono.fromFuture(franchiseTable.getItem(key))
                .flatMap(item -> item == null ? Mono.empty() : Mono.just(item));
    }

    public Flux<BranchEntity> findBranchesByFranchiseId(String franchiseId) {
        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.sortBeginsWith(
                        Key.builder().partitionValue(pk(franchiseId)).sortValue(BRANCH_SK_PREFIX).build()
                ))
                .build();

        return Flux.from(branchTable.query(request)).flatMapIterable(Page::items);
    }

    public Flux<ProductEntity> findProductsByFranchiseId(String franchiseId) {
        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.sortBeginsWith(
                        Key.builder().partitionValue(pk(franchiseId)).sortValue(PRODUCT_SK_PREFIX).build()
                ))
                .build();

        return Flux.from(productTable.query(request)).flatMapIterable(Page::items);
    }

    public Flux<FranchiseEntity> findAllFranchises() {
        ScanEnhancedRequest request = ScanEnhancedRequest.builder()
                .filterExpression(Expression.builder()
                        .expression(ENTITY_TYPE + " = :type")
                        .expressionValues(Map.of(
                                ":type", AttributeValue.builder().s(ENTITY_TYPE_FRANCHISE).build()
                        ))
                        .build())
                .build();

        return Flux.from(franchiseTable.scan(request)).flatMapIterable(Page::items);
    }

    public Mono<Boolean> existsFranchiseByName(String name) {
        if (name == null || name.isBlank()) {
            return Mono.just(false);
        }

        String trimmed = name.trim();

        Expression expression = Expression.builder()
                .expression(ENTITY_TYPE + " = :type AND #nm = :name")
                .expressionNames(Map.of("#nm", "name"))
                .expressionValues(Map.of(
                        ":type", AttributeValue.builder().s(ENTITY_TYPE_FRANCHISE).build(),
                        ":name", AttributeValue.builder().s(trimmed).build()
                ))
                .build();

        ScanEnhancedRequest request = ScanEnhancedRequest.builder()
                .filterExpression(expression)
                .limit(1)
                .build();

        return Flux.from(franchiseTable.scan(request))
                .flatMapIterable(Page::items)
                .hasElements();
    }

    private Mono<ExistingAggregate> loadExistingAggregate(String franchiseId) {
        Mono<Optional<FranchiseEntity>> franchiseMono = findFranchiseById(franchiseId)
                .map(Optional::of)
                .defaultIfEmpty(Optional.empty());

        Mono<List<BranchEntity>> branchesMono = findBranchesByFranchiseId(franchiseId).collectList();
        Mono<List<ProductEntity>> productsMono = findProductsByFranchiseId(franchiseId).collectList();

        return Mono.zip(franchiseMono, branchesMono, productsMono)
                .map(tuple -> new ExistingAggregate(tuple.getT1(), tuple.getT2(), tuple.getT3()));
    }

    private Mono<Void> executeTransaction(List<TransactWriteItem> items) {
        if (items.isEmpty()) {
            return Mono.empty();
        }

        int batchCount = (items.size() + MAX_TRANSACTION_ITEMS - 1) / MAX_TRANSACTION_ITEMS;

        return Flux.range(0, batchCount)
                .concatMap(batchIndex -> {
                    int fromIndex = batchIndex * MAX_TRANSACTION_ITEMS;
                    int toIndex = Math.min(fromIndex + MAX_TRANSACTION_ITEMS, items.size());

                    TransactWriteItemsRequest request = TransactWriteItemsRequest.builder()
                            .transactItems(items.subList(fromIndex, toIndex))
                            .build();

                    return Mono.fromFuture(dynamoDbAsyncClient.transactWriteItems(request));
                })
                .then();
    }

    private List<TransactWriteItem> deleteOrphanBranches(List<BranchEntity> oldItems, List<BranchEntity> newItems) {
        Set<String> wanted = newItems.stream().map(BranchEntity::getSk).collect(Collectors.toSet());
        return oldItems.stream()
                .filter(item -> !wanted.contains(item.getSk()))
                .map(item -> delete(item.getPk(), item.getSk()))
                .toList();
    }

    private List<TransactWriteItem> deleteOrphanProducts(List<ProductEntity> oldItems, List<ProductEntity> newItems) {
        Set<String> wanted = newItems.stream().map(ProductEntity::getSk).collect(Collectors.toSet());
        return oldItems.stream()
                .filter(item -> !wanted.contains(item.getSk()))
                .map(item -> delete(item.getPk(), item.getSk()))
                .toList();
    }

    private TransactWriteItem put(Map<String, AttributeValue> item) {
        return TransactWriteItem.builder()
                .put(Put.builder().tableName(tableName).item(item).build())
                .build();
    }

    private TransactWriteItem delete(String pk, String sk) {
        return TransactWriteItem.builder()
                .delete(Delete.builder().tableName(tableName).key(keyMap(pk, sk)).build())
                .build();
    }

    private Map<String, AttributeValue> toFranchiseMap(FranchiseEntity entity) {
        return Map.of(
                "PK", AttributeValue.builder().s(entity.getPk()).build(),
                "SK", AttributeValue.builder().s(entity.getSk()).build(),
                "franchiseId", AttributeValue.builder().s(entity.getFranchiseId()).build(),
                "name", AttributeValue.builder().s(entity.getName()).build(),
                "entityType", AttributeValue.builder().s(ENTITY_TYPE_FRANCHISE).build()
        );
    }

    private Map<String, AttributeValue> toBranchMap(BranchEntity entity) {
        return Map.of(
                "PK", AttributeValue.builder().s(entity.getPk()).build(),
                "SK", AttributeValue.builder().s(entity.getSk()).build(),
                "franchiseId", AttributeValue.builder().s(entity.getFranchiseId()).build(),
                "branchId", AttributeValue.builder().s(entity.getBranchId()).build(),
                "name", AttributeValue.builder().s(entity.getName()).build(),
                "entityType", AttributeValue.builder().s(ENTITY_TYPE_BRANCH).build()
        );
    }

    private Map<String, AttributeValue> toProductMap(ProductEntity entity) {
        return Map.of(
                "PK", AttributeValue.builder().s(entity.getPk()).build(),
                "SK", AttributeValue.builder().s(entity.getSk()).build(),
                "franchiseId", AttributeValue.builder().s(entity.getFranchiseId()).build(),
                "branchId", AttributeValue.builder().s(entity.getBranchId()).build(),
                "productId", AttributeValue.builder().s(entity.getProductId()).build(),
                "name", AttributeValue.builder().s(entity.getName()).build(),
                "stock", AttributeValue.builder().n(String.valueOf(entity.getStock())).build(),
                "entityType", AttributeValue.builder().s(ENTITY_TYPE_PRODUCT).build()
        );
    }

    private static Map<String, AttributeValue> keyMap(String pk, String sk) {
        return Map.of(
                "PK", AttributeValue.builder().s(pk).build(),
                "SK", AttributeValue.builder().s(sk).build()
        );
    }

    private static String pk(String franchiseId) {
        return "FRANCHISE#" + franchiseId;
    }

    private record ExistingAggregate(
            Optional<FranchiseEntity> franchise,
            List<BranchEntity> branches,
            List<ProductEntity> products
    ) {}
}
