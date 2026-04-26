# Backend — Claude Code Instructions

## Stack
- Java 21, Spring Boot 4.x, Maven
- Spring Web, Spring Data JPA, Spring Security (JWT)
- MySQL (local dev) → Azure Database for MySQL (production)
- Validation: `spring-boot-starter-validation`

## Commands
```bash
mvn spring-boot:run        # Start dev server on port 8080
mvn test                   # Run all tests
mvn clean package          # Build JAR
mvn clean package -DskipTests  # Build JAR without running tests
```

## Package Structure
Feature-based packaging — one package per domain, not per layer.
```
com.projectpulse/
├── auth/
│   ├── AuthController.java
│   ├── AuthService.java
│   ├── JwtUtil.java
│   └── JwtAuthFilter.java
├── section/
│   ├── SectionController.java
│   ├── SectionService.java
│   ├── SectionRepository.java
│   ├── SectionEntity.java
│   └── dto/
│       ├── SectionRequest.java
│       └── SectionResponse.java
├── team/
├── user/
├── war/
├── peerevaluation/
├── rubric/
├── report/
├── invitation/
└── common/
    ├── ApiResponse.java      # Standard response envelope
    ├── GlobalExceptionHandler.java
    └── SecurityConfig.java
```

## Naming Conventions
| Type | Pattern | Example |
|---|---|---|
| Controller | `<Feature>Controller` | `SectionController` |
| Service | `<Feature>Service` | `SectionService` |
| Repository | `<Feature>Repository` | `SectionRepository` |
| Entity | `<Feature>Entity` | `SectionEntity` |
| Request DTO | `<Feature>Request` | `CreateSectionRequest` |
| Response DTO | `<Feature>Response` | `SectionResponse` |

## Coding Rules
- **Always use DTOs** — never return or accept `@Entity` classes in controllers
- **Always annotate request bodies** with `@Valid` and add `@NotNull`/`@NotBlank` constraints on DTO fields
- **Use `BigDecimal`** for all score and hours fields — never `float` or `double`
- **Use `LocalDate`** for `weekStartDate` — always a Monday
- **Use `LocalDateTime`** for `createdAt`, `submittedAt`, `acceptedAt`
- Keep controllers thin — no business logic, only call service methods
- Keep services transactional — annotate with `@Transactional` where appropriate
- Return `ResponseEntity<ApiResponse<T>>` from all controllers

## Standard Response Envelope
Always wrap responses in `ApiResponse`:
```java
// ApiResponse.java
public record ApiResponse<T>(boolean success, T data, String message, String error) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, null);
    }
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message, message);
    }
}
```

## JWT Auth
- Role claim in JWT: `ADMIN`, `INSTRUCTOR`, `STUDENT`
- Protect routes with `@PreAuthorize("hasRole('ADMIN')")` etc.
- Public endpoints (no token needed): `POST /api/v1/auth/login`, `POST /api/v1/students/register`, `POST /api/v1/instructors/register`

## Error Handling
Handle all exceptions in `GlobalExceptionHandler.java`:
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)  // 400
    @ExceptionHandler(EntityNotFoundException.class)          // 404
    @ExceptionHandler(DataIntegrityViolationException.class)  // 409 (duplicates)
}
```

## application.properties (local dev)
```properties
server.port=8080
spring.datasource.url=jdbc:mysql://localhost:3306/project_pulse
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
app.jwt.secret=${JWT_SECRET}
app.jwt.expiration=86400000
```
Never hardcode credentials — use environment variables.

## Database Rules
- Entity class names are singular (`SectionEntity`) — JPA maps to plural table names via `@Table(name = "sections")`
- Always define `@Table(name = "...")` explicitly — don't rely on JPA defaults
- Junction tables (`team_students`, `team_instructors`) use `@ManyToMany` with `@JoinTable`
- Add `@UniqueConstraint` annotations to match DB schema unique indexes
