package com.springboot.reactor.pruebaaccenture.infrastructure.drivenadapter.persistence.config;

import com.springboot.reactor.pruebaaccenture.infrastructure.drivenadapter.persistence.entity.FranchiseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

@Configuration
public class DynamoDbConfig {

    @Bean
    public DynamoDbAsyncClient dynamoDbAsyncClient(
            @Value("${aws.region}") String region,
            @Value("${aws.credentials.access-key}") String accessKey,
            @Value("${aws.credentials.secret-key}") String secretKey
    ) {
        var credentials = AwsBasicCredentials.create(accessKey, secretKey);

        return DynamoDbAsyncClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    @Bean
    public DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient(DynamoDbAsyncClient dynamoDbAsyncClient) {
        return DynamoDbEnhancedAsyncClient.builder()
                .dynamoDbClient(dynamoDbAsyncClient)
                .build();
    }

    @Bean(name = "franchiseTable")
    public DynamoDbAsyncTable<FranchiseEntity> franchiseTable(
            DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
            @Value("${aws.dynamodb.table-name}") String tableName
    ) {
        return dynamoDbEnhancedAsyncClient.table(tableName, TableSchema.fromBean(FranchiseEntity.class));
    }
}
