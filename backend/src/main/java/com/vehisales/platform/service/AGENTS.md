# Calculation and export services

## Purpose

Turn catalog + location + dealer extras into an on-road breakdown and an Excel quote.

## Ownership

- `OnRoadCostService` — calculate totals
- `FeePolicy` — registration tax percents from `fee-policy.yml`; plate amount from `license-plate-regions.yml` by location code
- `DealerPolicy` — usage discounts and optional company offers from `dealer-policy.yml`
- `FeeRuleResolver` / `FeeAmountCalculator` — pick and compute remaining fee lines
- `QuoteExportService` / `QuoteLabels` — fill `bang-bao-gia.xlsx`
- `QuoteHistoryService` — save/search client reports in `quote_history` (created on startup if missing)
- `CatalogService` / `DtoMapper` — brand, vehicle, location DTOs
- `CatalogAdminService` — operator catalog CRUD
- `PolicyAdminService` — fee-policy, dealer-policy, and plate-region editors persisted in `app_settings`
- `TextTranslateService` — Vietnamese → `en` / `zh` / `ja` for location names and dealer offers (glossary first, then MyMemory)
- `AdminAuthService` — hard operator login; signs a bearer token for `/api/admin/**`
- `DataSeeder` — local H2 only when `app.seed.enabled=true`

## Local Contracts

- Location-calculated fees stay server-owned: TNDS, road use
- `REGISTRATION_TAX` (Thuế trước bạ) comes from `fee-policy.yml` as a percent of Giá Bán — not from `fee_rules`
- `LICENSE_PLATE` (Phí bấm biển số) is a fixed amount for the selected location in `license-plate-regions.yml` (34 units after Nghị quyết 202/2025/QH15)
- Startup deletes leftover `LICENSE_PLATE` / `REGISTRATION_TAX` rows from `fee_rules`; admin fee-rules list hides those codes
- Usage type `PRIVATE` / `COMMERCIAL` sets dealer discount % and commercial registration-tax %
- Company offers: `FORGO_FOR_CREDIT` deducts the gift value from Giá Bán when the customer forgoes accessories; `EXTRA_PERCENT` stacks on the usage discount
- Adjustable extras: discount, deposit, registration service, mica plate, inspection, optional body insurance, accessories
- Accessories with empty name or zero amount are dropped before persist/calculate (`extrasPayload` on the client; service should ignore blanks)
- Export fills `templates/bang-bao-gia.xlsx` in place; Vietnamese keeps template labels (`TỔNG LĂNG BÁNH`, `TỔNG CP PHÁT SINH`, `TVBH`)
- `en` / `zh` / `ja` rewrite labels after fill
- Fill looks up template cell text (`Khách hàng:`, `Đời xe:`, `Ngày:` in-cell; amounts beside labels)
- Header logos stay visible: dealer name/address is recentered in the middle columns so it does not cover the left/right images

## Work Guidance

- Keep quote math in one place (`OnRoadCostService`); export must reuse the same calculate path
- Do not hard-code Vietnam fee tables in Java when a `fee_rules` row can express them

## Verification

- `FeeRuleResolverTest`, `FeeAmountCalculatorTest`, `FeePolicyTest`, `DealerPolicyTest`, `CatalogAdminServiceTest`, `TextTranslateServiceTest`, `QuoteHistoryServiceTest`, `AdminAuthServiceTest`
- Manual: calculate then export for one Mitsubishi + one location

## Child DOX Index

- No child AGENTS.md files under this folder.
