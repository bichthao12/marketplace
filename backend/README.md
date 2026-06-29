# Marketplace API — Auth module skeleton

Spring Boot 3.3 + Java 21. Auth module theo plan (JWT access/refresh, RBAC, password reset).

## Structure

```
backend/src/main/java/com/marketplace/
  auth/
    config/          SecurityConfig, AuthProperties, PasswordEncoderConfig
    controller/      AuthController
    dto/             Request/response records
    entity/          RefreshToken, PasswordResetToken
    exception/       Auth-specific ApiException subclasses
    repository/      JPA repositories
    security/        JwtAuthFilter, JwtTokenProvider, UserPrincipal
    service/         AuthService, TokenService, PasswordResetService, LoginRateLimiter
    util/            TokenHasher
  user/
    entity/          User, Role
    repository/      UserRepository
    service/         UserService
  common/
    dto/             ErrorResponse
    exception/       GlobalExceptionHandler, ApiException
```

## Quick start

```bash
# 1. Start Postgres + Redis
cd marketplace
docker compose up -d

# 2. Run API
cd backend
mvn spring-boot:run
```

Base URL: `http://localhost:8080/api/v1`

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/auth/register` | Register + token pair |
| POST | `/auth/login` | Login |
| POST | `/auth/refresh` | Rotate refresh token |
| POST | `/auth/logout` | Revoke refresh token |
| POST | `/auth/forgot-password` | Send reset link (logged to console in dev) |
| POST | `/auth/reset-password` | Reset password |
| GET | `/categories` | Category tree |
| GET | `/products` | Search/list products (public) |
| GET | `/products/{slug}` | Product detail |
| GET | `/shops/{slug}` | Shop page |
| POST | `/seller/products` | Create product (SELLER) |
| PATCH | `/seller/products/{id}/status` | Publish/unpublish |
| POST | `/seller/products/{id}/images` | Upload image |
| CRUD | `/admin/categories` | Category management (ADMIN) |

## Example

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"buyer@example.com","password":"securePass1","fullName":"Nguyen Van A"}'
```

## Env vars

| Variable | Default |
|----------|---------|
| `JWT_SECRET` | dev placeholder in application.yml |
| `DB_HOST` | localhost |
| `REDIS_HOST` | localhost |

## TODO (next steps)

- [ ] Email service (thay `log.info` trong PasswordResetService)
- [ ] Integration tests với Testcontainers
- [ ] `POST /auth/logout-all`
- [ ] httpOnly cookie cho refresh token
- [ ] Seed admin user migration `V2__seed_admin.sql`
