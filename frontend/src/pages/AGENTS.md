# Pages

## Purpose

Route-level screens for brand pick, catalog search, vehicle confirm, and on-road quote.

## Ownership

- `BrandPortal.tsx` — `/`; each ready brand box is a full-card link (photo included)
- `HomePage.tsx` — `/brand/:brandCode`; live soft search for cars (accents ignored)
- `VehiclePage.tsx` — confirm details, usage (private / commercial), company policies, and a soft province search
- `OnRoadQuotePage.tsx` — load extras, calculate, equal left Price / right Accessories, sheet, Excel + PDF export
- `AdminDataPage.tsx` — `/admin` easy forms; catalog and plate lists have live soft search
- App-wide login: `LoginScreen` in `App.tsx` — hard `admin` account; form centered; pages render only after sign-in
- Page titles stand alone — no lead/tagline under the title
- `QuoteHistoryPage.tsx` — `/quotes` live-filters saved reports; Vietnamese accents are ignored (`lib/softSearch.ts`)

## Local Contracts

- Confirm form, usage, and company policies stay on `VehiclePage`; totals and export live on `OnRoadQuotePage`
- On-road query: `locationId` (required), `categoryId`, `optional`, `name`, `address`, `color`, `usage`
- Policy choices persist in `sessionStorage` key `onroad-policy-{vehicleId}`
- `OnRoadQuotePage` keeps customer name/address in component state so export works without `?name=`
- Recalculate sits at the bottom of the Price column; Excel and PDF sit below `QuoteSheet`; each export stores the client name and quote in `quote_history`
- Header language (`vi` / `en` / `zh` / `ja`) drives the whole UI, including `/admin` tables, quote sheet, and Excel/PDF. Location rows use `name` / `nameEn` / `nameZh` / `nameJa`; categories and fees use `category.*` / `fee.*` keys; plate provinces reuse location names
- Admin multilingual fields: type Vietnamese first; leaving the box calls `POST /api/admin/translate` and fills empty `en` / `zh` / `ja`. Save also fills copies of Vietnamese. Manual edits to those languages are kept
- Add/edit catalog rows open in a popup, not under the table
- Each vehicle color has its own photo URL; quote and confirm pages use that URL for the matching color
- Fee policy, dealer policy, and plate regions save through `/api/admin` and persist in `app_settings`
- Fee rules tab lists only remaining rule-based fees (one row per fee + vehicle type). `LICENSE_PLATE` and `REGISTRATION_TAX` stay on their own tabs
- License plate fees tab uses a 6 / 4 split (Area I vs Area II, and province name vs area)

## Work Guidance

- Show the real API error under the generic `apiError` banner
- After extras change, user must Recalculate before the sheet updates

## Verification

- Manual walk of brand → vehicle (details + policies) → quote with a Mitsubishi id from Neon

## Child DOX Index

- No child AGENTS.md files under this folder.
