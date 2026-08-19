# OnRoad — Vehicle Sales & On-Road Cost Platform

Full-stack foundation for browsing vehicles and calculating location-based on-road costs in Vietnam.

The frontend never hard-codes fees. It searches the catalog, lets the user confirm a vehicle category and province/city, then asks the backend to apply the current fee rules.

## Architecture

```
Search vehicle → Select vehicle → Confirm category → Choose province/city → POST /api/calculate-on-road-cost → Fee breakdown + estimated total
```

| Layer | Choice |
| --- | --- |
| Frontend | React 18, TypeScript, Vite, Tailwind CSS |
| Backend | Spring Boot 3.3, Java 17, JPA, validation, OpenAPI |
| Database | PostgreSQL in production (`postgres` profile). Local default is H2 in PostgreSQL mode so the app runs without a server. |

Fee amounts live in `fee_definitions` + `fee_rules`, not in UI code. New vehicle types, models, provinces, fee types and calculation rules can be added as data.

## Domain

- **Vehicle** — brand, model, name, year, seats, specs, list price, image, category
- **Vehicle category** — motorcycle, bicycle, 4-seat car, 7-seat car, pickup, truck, van, other
- **Location** — Vietnam’s 34 provinces/cities (2026), with region and fee zone (`SPECIAL`, `MAJOR`, `STANDARD`)
- **Fee definition** — named cost (plate, registration tax, road use, inspection, compulsory insurance, optional body insurance)
- **Fee rule** — how a fee is calculated for a category, location or zone: fixed amount, % of list price, or % with min/max, plus date range and engine/price filters

Rules are resolved by specificity: a Hanoi-only car rule beats a national car rule, which beats an all-category rule.

## API

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/api/vehicles/search?keyword=&categoryId=` | Search catalog |
| GET | `/api/vehicles/{id}` | Vehicle detail |
| GET | `/api/vehicle-categories` | Categories |
| GET | `/api/locations` | Provinces / cities |
| POST | `/api/calculate-on-road-cost` | On-road breakdown |
| GET | `/api/swagger-ui` | OpenAPI UI |

Calculate body:

```json
{
  "vehicleId": 7,
  "locationId": 1,
  "categoryId": 3,
  "includeOptionalInsurance": true
}
```

The response returns list price, each applicable fee, mandatory total, optional total, and estimated on-road total in VND.

Seeded amounts are **illustrative** Vietnam-style defaults (for example 12% registration tax and a 20 million VND plate fee in Hà Nội / TP.HCM). Update rows in `fee_rules` when official circulars change.

## Run locally

Backend (H2, port 8003):

```bash
cd backend
mvn spring-boot:run
```

Frontend (port 5174, proxies `/api` to 8003):

```bash
cd frontend
npm install
npm run dev
```

Open http://localhost:5174

### Neon PostgreSQL (dynamic catalog)

1. Open the Neon SQL Editor.
2. Paste and run `db/neon-init.sql` (same file as `backend/src/main/resources/db/neon-init.sql`).
3. Copy `backend/.env.example` to `backend/.env` and fill your Neon URL, user, and password.
4. Set `SPRING_PROFILES_ACTIVE=postgres` in `.env`.
5. Restart the backend from the `backend` folder:

```bash
cd backend
mvn spring-boot:run
```

The Java seeder stays off. Vehicles, discounts, gifts, and fees come from the database. Export on a vehicle page fills `Bảng báo giá.xlsx` for the client.

## Project layout

```
backend/src/main/java/com/vehisales/platform/
  api/          REST controllers and DTOs
  config/       CORS + catalog/fee seeder
  domain/       JPA entities
  repository/   Spring Data
  service/      catalog + rule-based calculator
frontend/src/
  pages/        search and vehicle + calculator
  components/   cards and cost table
  api/          backend client
```
