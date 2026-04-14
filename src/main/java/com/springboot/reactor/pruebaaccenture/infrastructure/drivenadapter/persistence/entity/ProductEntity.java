package com.springboot.reactor.pruebaaccenture.infrastructure.drivenadapter.persistence.entity;

import com.springboot.reactor.pruebaaccenture.domain.model.Product;
import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@Getter
@Setter
@DynamoDbBean
public class ProductEntity {

    private String pk;
    private String sk;

    private String franchiseId;
    private String branchId;
    private String productId;
    private String name;
    private Integer stock;
    private String entityType;

    public static ProductEntity fromDomain(String franchiseId, String branchId, Product product) {
        ProductEntity entity = new ProductEntity();
        entity.setFranchiseId(franchiseId);
        entity.setBranchId(branchId);
        entity.setProductId(product.getId().value());
        entity.setName(product.getName().value());
        entity.setStock(product.getStock().value());
        entity.setEntityType("PRODUCT");

        entity.setPk("FRANCHISE#" + franchiseId);
        entity.setSk("PRODUCT#" + branchId + "#" + entity.getProductId());
        return entity;
    }

    @DynamoDbAttribute("PK")
    @DynamoDbPartitionKey
    public String getPk() {
        return pk;
    }

    @DynamoDbAttribute("SK")
    @DynamoDbSortKey
    public String getSk() {
        return sk;
    }
}