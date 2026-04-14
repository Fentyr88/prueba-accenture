package com.springboot.reactor.pruebaaccenture.infrastructure.drivenadapter.persistence.entity;

import com.springboot.reactor.pruebaaccenture.domain.model.Branch;
import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@Getter
@Setter
@DynamoDbBean
public class BranchEntity {

    private String pk;
    private String sk;

    private String franchiseId;
    private String branchId;
    private String name;
    private String entityType;

    public static BranchEntity fromDomain(String franchiseId, Branch branch) {
        BranchEntity entity = new BranchEntity();
        entity.setFranchiseId(franchiseId);
        entity.setBranchId(branch.getId().value());
        entity.setName(branch.getName().value());
        entity.setEntityType("BRANCH");

        entity.setPk("FRANCHISE#" + franchiseId);
        entity.setSk("BRANCH#" + entity.getBranchId());
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
