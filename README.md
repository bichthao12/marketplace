# Marketplace Ecommerce MVP

Multi-vendor marketplace: Spring Boot API + React (buyer/seller/admin).

## Quick start

```bash
# Infrastructure
docker compose up -d

# Backend (port 8080, context /api/v1)
cd backend
mvn spring-boot:run

# Frontend
cd frontend
npm install
npm run dev:buyer    # http://localhost:5173
npm run dev:seller   # http://localhost:5174
npm run dev:admin    # http://localhost:5175
```

## Default accounts

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@marketplace.local | Admin1234 |

Buyers register via `/register`. Sellers apply via seller portal after login.

## Architecture

```
marketplace/
  backend/          Spring Boot 3.3, Java 21
  frontend/         React monorepo (buyer, seller, admin)
  e2e/              Playwright tests
  docker-compose.yml
```

## API modules

- **auth** — JWT, refresh, password reset
- **catalog** — categories, products, shops
- **cart** — Redis cart + inventory reservation
- **order** — checkout, multi-seller order groups
- **payment** — Stripe, VNPay, COD + webhooks
- **seller** — onboarding, product CRUD, fulfillment
- **admin** — seller approval, categories, orders, audit

## Environment variables

| Variable | Description |
|----------|-------------|
| `JWT_SECRET` | Min 256-bit secret for HS256 |
| `STRIPE_API_KEY` | Stripe secret key (optional) |
| `STRIPE_WEBHOOK_SECRET` | Stripe webhook signing secret |
| `VNPAY_TMN_CODE` | VNPay merchant code |
| `VNPAY_HASH_SECRET` | VNPay hash secret |

## Testing

```bash
cd backend && mvn verify
cd frontend && npm run build
cd e2e && npm install && npx playwright install chromium && npm test
```

## Deploy

CI runs on push via `.github/workflows/ci.yml`. Backend packages as JAR; frontend builds static assets for CDN/hosting.
