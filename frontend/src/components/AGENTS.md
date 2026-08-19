# Components

## Purpose

Shared chrome and quote widgets used by pages.

## Ownership

- `Header.tsx` / `LanguageSwitcher.tsx` / `LoginScreen.tsx` — site chrome; logo stays left with no tagline; nav is hidden until sign-in; after login shows Models, Change brand, History, Data, Sign out, and Language; header stays above admin popups (`z-50`)
- `QuotePricePanel` / `QuoteAccessoriesPanel` in `QuoteAdjustments.tsx` — equal left/right editors
- `QuoteSheet.tsx` — visual replica of the dealer Excel quote; `#quote-sheet` is the PDF source
- `VehicleCard.tsx`, `CostBreakdown.tsx` — catalog / fee list
- `ProvincePicker.tsx` — type-to-filter province list; `Ha Noi` matches `Hà Nội`

## Local Contracts

- Equal two-column split (`lg:grid-cols-2`): Price one column of large fields (`h-12`) plus Recalculate; Accessories catalog on the right
- Accessories the client buys appear in the Accessories column (editable name, amount, remove)
- Accessory photos: `aspect-[16/10] object-cover`
- `QuoteSheet` follows `bang-bao-gia.xlsx` (7-column dealer grid, same row order and labels as the template)
- Sheet and export language follow the header switcher (`lib/quoteLabels.ts`); the quote-page selector can still override for one export
- Color-car photos use `h-12 w-auto object-contain` so PDF capture does not stretch them
- Color photos prefer `vehicle.colorPhotos[name]`, then `public/colors/`
- Icons from `lucide-react` only

## Work Guidance

- Keep `QuoteAdjustments` exporting both panels so `VehiclePage` and `OnRoadQuotePage` can compose them
- Do not move Export controls into `Header`

## Verification

- Visual check of on-road page: equal Price / Accessories columns, Excel and PDF under the sheet

## Child DOX Index

- No child AGENTS.md files under this folder.
