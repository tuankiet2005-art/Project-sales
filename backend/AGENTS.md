# Backend

## Purpose

Spring Boot REST API for OnRoad: brand/vehicle catalog, location fee rules, on-road cost calculation, and Excel quote export.

## Ownership

- Package root: `com.vehisales.platform`
- Entry: `VehicleSalesApplication.java`
- Config: `src/main/resources/application.yml`
- Fee percents: `src/main/resources/fee-policy.yml` — Thuế trước bạ only
- Plate fees: `src/main/resources/license-plate-regions.yml` — 34 tỉnh/thành grouped by NORTH/CENTRAL/SOUTH, amounts by AREA_I / AREA_II
- Dealer discounts and promotions: `src/main/resources/dealer-policy.yml`
- Excel template: `src/main/resources/templates/bang-bao-gia.xlsx`
- Schema copy: `src/main/resources/db/neon-init.sql` (operator source is `db/neon-init.sql`)

## Local Contracts

### Run and deploy

- Local: `mvn spring-boot:run` from `backend/` (port `8003`, or `PORT` / `SERVER_PORT`)
- Docker: `backend/Dockerfile` — image defaults `SPRING_PROFILES_ACTIVE=postgres`
- Render: Web Service, root directory `backend`, Docker runtime
- Health: `GET /api/health` — reports `database` and `brands` count when the datasource works

### Environment

Copy `backend/.env.example` to `backend/.env`. Never commit `.env`.

| Variable | Purpose |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `local` (H2 + seed) or `postgres` (Neon) |
| `DATABASE_URL` | JDBC or Neon/Render `postgres://` / `postgresql://` — `PostgresDataSourceConfig` normalizes |
| `DATABASE_USERNAME` / `DATABASE_PASSWORD` | Used when the URL has no userinfo; empty values must not override URL credentials |
| `APP_SEED_ENABLED` | `false` on Neon so `DataSeeder` does not wipe |
| `JPA_DDL_AUTO` | `none` on postgres; schema comes from SQL |
| `CORS_ORIGINS` | Comma list; must include `https://project-sales.vercel.app` in production |
| `PORT` | Render injects this; `server.port` reads `PORT` then `SERVER_PORT` |
| `ADMIN_USERNAME` / `ADMIN_PASSWORD` | Hard operator account for `/api/admin/**` (defaults `admin` / `Admin!!@`) |
| `ADMIN_TOKEN_SECRET` | HMAC secret used to sign the admin bearer token |

### API surface

| Controller | Path | Notes |
|---|---|---|
| `ReferenceDataController` | `/api/brands`, `/api/brands/{code}`, `/api/vehicle-categories`, `/api/locations`, `/api/dealer-policy`, `/api/health` | Catalog + health + dealer offers |
| `VehicleController` | `/api/vehicles/search`, `/api/vehicles`, `/api/vehicles/{id}` | Keyword / brand / category filters |
| `CalculationController` | `POST /api/calculate-on-road-cost`, `POST /api/export-quote` | Quote extras and accessories on both; Excel export also saves history |
| `QuoteHistoryController` | `/api/quotes` | Saved client reports: list `?q=`, get by id, POST to save |
| `AuthController` | `POST /api/auth/login` | Hard account; returns bearer token |
| `AdminCatalogController` | `/api/admin/**` | Operator CRUD; requires `Authorization: Bearer` from login |

Swagger: `/api/swagger-ui`

### Persistence

- Entities in `domain/`, repositories in `repository/`
- Postgres profile: no Hibernate schema updates; run `db/neon-init.sql` on empty Neon
- Local profile: H2 file `./data/vehisales`, `ddl-auto: update`, seed on
- Tables: `brands`, `vehicle_categories`, `locations`, `dealers`, `fee_definitions`, `vehicles`, `fee_rules`, `app_settings`, `quote_history`
- `vehicles.color_photos` is a name→image URL map; added on startup if the column is missing
- Operator edits go through `/api/admin` with the hard admin token. Saved fee/dealer/plate policies live in `app_settings` and override the YAML defaults without a restart

### Calculation and export

- `OnRoadCostService` applies location fee rules plus dealer overrides (discount, deposit, registration service, mica, inspection, optional body insurance) and accessories
- `REGISTRATION_TAX` is `percent / 100 × Giá Bán` from `fee-policy.yml`; commercial tax uses `registration-tax-commercial-percent`
- `LICENSE_PLATE` is a fixed VND amount from `license-plate-regions.yml` for the quote `locationId` (Thông tư 71/2025/TT-BTC: AREA_I 20,000,000; AREA_II 200,000)
- Usage `PRIVATE` / `COMMERCIAL` plus company offers come from `dealer-policy.yml` (`DealerPolicy`)
- `QuoteExportService` fills the Excel template; `QuoteLabels` rewrites sheet labels for `en` / `zh` / `ja` (`vi` leaves Vietnamese labels)
- `ExportQuoteRequest.customerName` is `@NotBlank` — callers must send a name (frontend falls back to `Khách hàng`)

## Work Guidance

- Controllers stay thin; fee math stays in `service/`
- Day-to-day plate/tax/discount edits use `/admin`; YAML files are defaults only
- New public origins go in `CORS_ORIGINS` / `CorsConfig`
- Do not enable `ddl-auto=update` against Neon
- Keep `APP_SEED_ENABLED=false` whenever the database already has `neon-init.sql` data

## Verification

- `mvn test` from `backend/` — `FeeRuleResolverTest`, `FeeAmountCalculatorTest`, `FeePolicyTest`, `DealerPolicyTest`, `CatalogAdminServiceTest`, `TextTranslateServiceTest`, `QuoteHistoryServiceTest`, `AdminAuthServiceTest`
- Manual: `GET /api/health` then `GET /api/brands`

## Child DOX Index

| Path | Scope |
|---|---|
| `src/main/java/com/vehisales/platform/service/AGENTS.md` | Fee resolution, on-road totals, Excel fill/labels |
