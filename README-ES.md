# Spring Security Demo API

[Read in English](README.md)

API RESTful desarrollada con Spring Boot, Spring Data y Spring Security.

Incluye autenticación y autorización con JWT, base de datos PostgreSQL, pruebas unitarias e integradas (145 tests), documentación con Swagger y despliegue mediante Docker.

Es un proyecto simple creado para entender los fundamentos de la seguridad web.

## Tecnologías usadas

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

## Características principales

- Autenticación y autorización con Spring Security y JWT (access y refresh tokens).
- Persistencia con Spring Data JPA y base de datos PostgreSQL.
- Validaciones con jakarta.validation.
- Documentación automática con Swagger / OpenAPI 3.
- Mapeo DTO–Entidad con MapStruct.
- Pruebas unitarias e integradas con JUnit 5, Mockito, @DataJpaTest, @WebMvcTest, @SpringBootTest, MockMvc y TestRestTemplate (145 tests en total: 99 unitarios y 46 integrados).
- Configuración mediante variables de entorno (.env) y Docker Compose para un entorno reproducible.
- Contraseñas hasheadas con BCrypt, y los refresh tokens se hashean con SHA-256 para mayor seguridad.
- Configuración de CORS (usando la variable de entorno ALLOWED_ORIGINS).

## Endpoints

- **PÚBLICO**

```
POST /api/auth/register
POST /api/auth/authenticate
POST /api/auth/refresh
POST /api/auth/logout
```

- ROL: **USER**

```
POST   /api/user/me/logout-all
GET    /api/user/me
PATCH  /api/user/me
DELETE /api/user/me
```

- ROL: **ADMIN**

```
POST   /api/admin/users/{id}/logout-all
GET    /api/admin/users/{id}
GET    /api/admin/users
PATCH  /api/admin/users/{id}
DELETE /api/admin/users/{id}
```

Para ver todos los endpoints y sus detalles (cuerpo de request, respuestas, códigos HTTP, etc.), consultar la documentación Swagger: `http://localhost:8080/swagger-ui/index.html`

## Requisitos

- [Docker](https://www.docker.com/)
- IDE (IntelliJ IDEA recomendado: [https://www.jetbrains.com/idea/](https://www.jetbrains.com/idea/))

No es necesario tener Java, PostgreSQL o Maven instalados localmente. Toda la aplicación se ejecuta dentro de contenedores.

## Instalación y ejecución

1. Clonar el repositorio:

```
git clone https://github.com/benjamerc/spring-security-demo
```

2. Crear un archivo `.env` a partir del ejemplo (`.env.example`) y definir los valores:

Linux/macOS o Git Bash:

```
cp .env.example .env
```

Windows cmd:

```
copy .env.example .env
```

3. Levantar los contenedores:

```
docker-compose up --build
```


4. Acceder a la API (preferentemente usando un cliente HTTP como [Postman](https://www.postman.com/) o [Insomnia](https://insomnia.rest/)):

```
# API
http://localhost:8080

# Documentación Swagger
http://localhost:8080/swagger-ui/index.html
```

5. Detener los contenedores:

```
docker-compose stop
```

## Ejecución de tests

- Para ejecutar los tests unitarios:

```
./mvnw clean test
```

- Para ejecutar tests unitarios e integrados:

```
./mvnw clean verify
```


Nota: Los tests se ejecutan usando Maven Wrapper (`./mvnw`). Se recomienda IntelliJ IDEA para un entorno de desarrollo más cómodo.
