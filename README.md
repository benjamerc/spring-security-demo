# Spring Security Demo API

[Leer en español](README-ES.md)

RESTful API developed with Spring Boot, Spring Data, and Spring Security.

Includes authentication and authorization with JWT, PostgreSQL database, unit and integration tests (145 tests), Swagger documentation, and deployment via Docker.

This is a simple project created to understand the fundamentals of web security.

## Technologies Used

- [Java 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- Spring Boot 3.5.5
- Spring Security
- Spring Data JPA
- PostgreSQL 16
- H2 Database
- [JJWT 0.13.0](https://github.com/jwtk/jjwt)
- [MapStruct 1.6.3](https://mapstruct.org/)
- [Apache Commons Lang 3.18.0](https://commons.apache.org/proper/commons-lang/)
- Springdoc OpenAPI 2.8.13
- [JUnit 5](https://junit.org/junit5/) + Maven Surefire/Failsafe (3.2.5)
- [Lombok](https://projectlombok.org/)
- Docker & Docker Compose
- Maven Wrapper ([mvnw](https://maven.apache.org/wrapper/))

## Main Features

- Authentication and authorization with Spring Security and JWT (access and refresh tokens).
- Persistence with Spring Data JPA and PostgreSQL database.
- Validations using jakarta.validation.
- Automatic documentation with Swagger / OpenAPI 3.
- DTO–Entity mapping with MapStruct.
- Unit and integration testing with JUnit 5, Mockito, @DataJpaTest, @WebMvcTest, @SpringBootTest, MockMvc, and TestRestTemplate (145 tests in total: 99 unit tests and 46 integration tests).
- Configuration via environment variables (.env) and Docker Compose for a reproducible environment.
- Passwords hashed using BCrypt and refresh tokens hashed using SHA-256 for security.
- CORS configuration (using ALLOWED_ORIGINS environment variable).

## Endpoints

- **PUBLIC**

```
POST /api/auth/register
POST /api/auth/authenticate
POST /api/auth/refresh
POST /api/auth/logout
```

- ROLE: **USER**

```
POST   /api/user/me/logout-all
GET    /api/user/me
PATCH  /api/user/me
DELETE /api/user/me
```

- ROLE: **ADMIN**

```
POST   /api/admin/users/{id}/logout-all
GET    /api/admin/users/{id}
GET    /api/admin/users
PATCH  /api/admin/users/{id}
DELETE /api/admin/users/{id}
```

To see all endpoints and their details (request body, responses, HTTP codes, etc.), check the Swagger documentation: `http://localhost:8080/swagger-ui/index.html`

## Requirements

- [Docker](https://www.docker.com/)
- IDE (IntelliJ IDEA recommended: [https://www.jetbrains.com/idea/](https://www.jetbrains.com/idea/))

No need to have Java, PostgreSQL or Maven installed locally. The entire application runs inside containers.

## Installation and Execution

1. Clone the repository:

```
git clone https://github.com/benjamerc/spring-security-demo
```

2. Create a `.env` file from the example (`.env.example`) and set the values:

Linux/macOS or Git Bash:

```
cp .env.example .env
```

Windows cmd:

```
copy .env.example .env
```

3. Start the containers:

```
docker-compose up --build
```


4. Access the API (preferably using an HTTP client like [Postman](https://www.postman.com/) or [Insomnia](https://insomnia.rest/)):

```
# API
http://localhost:8080

# Swagger Documentation
http://localhost:8080/swagger-ui/index.html
```

5. Stop the containers:

```
docker-compose stop
```

## Running Tests

- To run unit tests:

```
./mvnw clean test
```

- To run both unit and integration tests:

```
./mvnw clean verify
```


Note: Tests are executed using the Maven Wrapper (`./mvnw`). IntelliJ IDEA is recommended for a more convenient development environment.


