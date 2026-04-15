# prueba-accenture

API reactiva para gestionar franquicias, sucursales y productos con Spring Boot WebFlux y DynamoDB.

---

## Requisitos

- Java 21
- Docker Desktop
- AWS CLI (solo para despliegue con CloudFormation)
- Maven Wrapper (`./mvnw`)
- Git Bash

---

## ⚙️ Variables de entorno

Crea un archivo `.env` basado en `.env.example`:

AWS_ACCESS_KEY_ID=local  
AWS_SECRET_ACCESS_KEY=local  
AWS_REGION=us-east-1  
AWS_DYNAMODB_TABLE_NAME=franchise-table  
AWS_DYNAMODB_ENDPOINT=http://localhost:8000

---

## ☁Despliegue con CloudFormation (AWS)

aws cloudformation deploy \
--stack-name prueba-accenture-dynamodb \
--template-file ./src/main/java/com/springboot/reactor/pruebaaccenture/infrastructure/drivenadapter/persistence/cloudformation/dynamodb.yml \
--parameter-overrides TableName=franchise-table \
--region us-east-1

---

## Endpoints principales

Base path: /api/v1/franchises

POST /api/v1/franchises  
GET /api/v1/franchises  
PUT /api/v1/franchises/{franchiseId}/name

POST /api/v1/franchises/{franchiseId}/branches  
PUT /api/v1/franchises/{franchiseId}/branches/{branchId}/name

POST /api/v1/franchises/{franchiseId}/branches/{branchId}/products  
DELETE /api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}  
PATCH /api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}/stock  
PUT /api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}/name

GET /api/v1/franchises/{franchiseId}/top-stock-products

---

## Ejecutar tests

./mvnw clean test

---

## Ejecutar con Docker

docker compose up --build
