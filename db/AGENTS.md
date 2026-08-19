# Database

## Purpose

Operator SQL that creates and seeds the Neon (and optional local Postgres) schema for OnRoad.

## Ownership

- `db/neon-init.sql` — source of truth
- Duplicate for the JAR: `backend/src/main/resources/db/neon-init.sql` (keep in sync when the operator file changes)

## Local Contracts

- Safe to re-run: drops demo tables first (`brands`, `vehicle_categories`, `locations`, `dealers`, `fee_definitions`, `vehicles`, `fee_rules`)
- Production: `JPA_DDL_AUTO=none`, `APP_SEED_ENABLED=false`
- Neon: use the **pooled** host and `sslmode=require`
- Current seed: Mitsubishi Vietnam vehicles, 34 VN locations, fee rules from the Excel “Dữ liệu nguồn”, dealer MITSUBISHI MOVEO NEW CITY (Bình Dương)
- Day-to-day catalog and policy changes use `/admin` after operator login; `app_settings`, `vehicles.color_photos`, and `quote_history` are created automatically if missing. Do not drop `quote_history` when re-running this SQL.
- Do not re-run this SQL just to add a vehicle
- Thuế trước bạ percents are not in this SQL — edit `/admin` Registration tax (defaults in `fee-policy.yml`)
- Phí bấm biển số is not in this SQL — edit `/admin` License plate fees (defaults in `license-plate-regions.yml`)
- `fee_rules` seed has one row per remaining fee + vehicle type; plate and tax rules are not seeded

## Work Guidance

- Change schema in this SQL first, then update JPA entities to match
- Do not rely on Hibernate to migrate Neon

## Verification

- After apply: `SELECT COUNT(*) FROM brands;` and `SELECT COUNT(*) FROM vehicles;`
- Render `GET /api/health` should show `database: UP` and a non-zero `brands` count

## Child DOX Index

- No child AGENTS.md files under this folder.
