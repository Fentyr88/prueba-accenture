package com.springboot.reactor.pruebaaccenture.infrastructure.drivenadapter.persistence.entity;

import com.springboot.reactor.pruebaaccenture.domain.model.Franchise;
import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@Getter
@Setter
@DynamoDbBean
public class FranchiseEntity {

    private String pk;
    private String sk;
    private String franchiseId;
    private String name;
    private String entityType;

    public static FranchiseEntity fromDomain(Franchise franchise) {
        FranchiseEntity entity = new FranchiseEntity();
        entity.setFranchiseId(franchise.getId().value());
        entity.setName(franchise.getName().value());
        entity.setEntityType("FRANCHISE");
        entity.setPk("FRANCHISE#" + entity.getFranchiseId());
        entity.setSk("FRANCHISE#" + entity.getFranchiseId());
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
