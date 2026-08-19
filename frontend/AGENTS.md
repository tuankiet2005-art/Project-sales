# Frontend

## Purpose

React SPA for OnRoad: pick a brand, search vehicles, confirm details, edit prices/accessories, view the quote sheet, export Excel or PDF.

## Ownership

- Dev server: port `5174` (`vite.config.ts` proxies `/api` → `http://localhost:8003`)
- Build output: `dist/`
- Deploy: Vercel, root directory `frontend`, output `dist`
- Accessory photos: `public/accessories/`
- Color-car photos: `public/colors/`; header logos: `public/brand/`

## Local Contracts

### Stack

- React 18, TypeScript, Vite, Tailwind, React Router
- `lucide-react` for icons (shadcn icon set)
- i18n: `src/i18n/` — languages `vi` (default), `en`, `zh`, `ja`; storage key `onroad-lang`; missing keys fall back to `vi` then `en`; `document.documentElement.lang` follows the switcher. Quote sheet / Excel / PDF follow the same language.
- Operator session: `src/auth/AdminAuthContext.tsx` + `src/lib/adminAuth.ts` (`onroad-admin-token`)

### Environment

| Variable | Purpose |
|---|---|
| `VITE_API_BASE` | Production Render origin, no trailing slash. Empty in local so `/api` uses the Vite proxy |

Baked in at build time. Live value: `https://project-sales.onrender.com`

SPA fallback: `vercel.json` rewrites non-`/api` paths to `index.html`.

### API

- Shared client: `src/api/client.ts` — prefix every path with `VITE_API_BASE`
- Admin Vietnamese fields call `POST /api/admin/translate` (`lib/fromVietnamese.ts`) to fill empty `en` / `zh` / `ja`
- Operator login: `POST /api/auth/login`; token in `localStorage` key `onroad-admin-token`; every page requires sign-in; `/api/admin/**` calls send `Authorization: Bearer`
- Quote extras persist in `sessionStorage` key `onroad-extras-{vehicleId}` (`lib/quoteExtras.ts`)
- Excel: `POST /api/export-quote` also writes `quote_history`. PDF: browser capture of `#quote-sheet` (`lib/exportQuotePdf.ts`) then `POST /api/quotes`. Search old quotes at `/quotes`.

### Routes

| Path | Page |
|---|---|
| `/` | `BrandPortal` |
| `/brand/:brandCode` | `HomePage` |
| `/brand/:brandCode/vehicles/:vehicleId` | `VehiclePage` (details, usage, company policies) |
| `/brand/:brandCode/vehicles/:vehicleId/on-road` | `OnRoadQuotePage` (price, accessories, sheet, export) |
| `/admin` | `AdminDataPage` — operator forms for catalog, tax, dealer offers, and plate fees |
| `/quotes` | `QuoteHistoryPage` — search saved client quotes and reopen them |

## Work Guidance

- New screens in `src/pages/`; shared UI in `src/components/`
- Quote and vehicle pages: two equal sites — Price left, Accessories right; items the client buys list in the Accessories column
- Accessory catalog cards use `aspect-[16/10]` cover photos in a 3-column grid
- Export must work when the URL has no `name`; keep name/address fields on the quote page and send a fallback name
- Default language Vietnamese for UI and export
- Do not add the full shadcn component kit unless asked

## Verification

- `npm run build` from `frontend/`
- Manual: brand → vehicle → calculate → edit extras → recalculate → export xlsx and pdf

## Child DOX Index

| Path | Scope |
|---|---|
| `src/pages/AGENTS.md` | Screen flow and quote-page layout |
| `src/components/AGENTS.md` | Header, quote sheet, price/accessories panels, PDF source |
