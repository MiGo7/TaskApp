# TaskApp

A REST API for managing tasks, built with Java 25 and Spring Boot 4.1.0.

## Stack

- Java 25, Spring Boot 4.1.0, Maven
- Spring Web, Spring Data JPA, Bean Validation
- H2 (local dev) / PostgreSQL (Docker)
- JUnit 5, Mockito, AssertJ, MockMvc, `@DataJpaTest`

## Architecture

```
controller  -> HTTP concerns only (status codes, request/response mapping)
service     -> business logic, transactions
repository  -> Spring Data JPA, derived queries for filtering
domain      -> Task entity + enums
dto         -> TaskRequest / TaskResponse (records) -- API contract, decoupled from the entity
mapper      -> entity <-> DTO conversion
exception   -> centralized error handling (@RestControllerAdvice)
```

## Running locally (H2)

```bash
mvn spring-boot:run
```

- Defaults to the `dev` profile (in-memory H2). 
- API available at `http://localhost:8080`.
- H2 console: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:taskdb;DB_CLOSE_DELAY=-1`, user `sa`, no password).

## Running with Docker + PostgreSQL

```bash
mvn clean package -DskipTests
docker-compose up --build
```

- Starts the app (`docker` profile) alongside a real PostgreSQL container, networked together.

## Running on Kubernetes

- Manifests are in `k8s/`. 
- Assumes a local cluster (minikube/kind) with the image loaded directly, no registry:

```bash
minikube start
docker build -t taskapp:latest .
minikube image load taskapp:latest
kubectl apply -f k8s/
minikube service taskapp --url
```

## API

| Method | Endpoint | Description |
|---|---|---|
| POST | `/tasks` | Create a task |
| GET | `/tasks/{id}` | Get a task by id |
| GET | `/tasks?status=&priority=` | List tasks, optionally filtered |
| PUT | `/tasks/{id}` | Replace a task (all fields required) |
| DELETE | `/tasks/{id}` | Delete a task |

- Full OpenAPI 3.1 spec, generated automatically from the code: `GET /v3/api-docs`
- Swagger UI page is not loaded due to incompatibility with Spring Boot 4.1.0 /
- Health check: `GET /actuator/health`
- Postman collection is provided `TaskApp.postman_collection.json`