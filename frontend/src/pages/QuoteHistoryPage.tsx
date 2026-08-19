import { Search } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api/client";
import { Header } from "../components/Header";
import { useI18n } from "../i18n/LanguageContext";
import { formatVnd } from "../lib/format";
import { softIncludes } from "../lib/softSearch";
import { saveExtras } from "../lib/quoteExtras";
import { savePolicyChoices } from "../lib/quotePolicy";
import type { QuoteExtras, QuoteHistory } from "../types";

export function QuoteHistoryPage() {
  const { t, lang } = useI18n();
  const navigate = useNavigate();
  const [query, setQuery] = useState("");
  const [rows, setRows] = useState<QuoteHistory[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const visible = useMemo(() => rows.filter((row) => matchesHistoryQuery(row, query)), [rows, query]);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    api
      .listQuotes()
      .then((next) => {
        if (!cancelled) {
          setRows(next);
        }
      })
      .catch((err: Error) => {
        if (!cancelled) {
          setError(err.message);
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false);
        }
      });
    return () => {
      cancelled = true;
    };
  }, []);

  function openQuote(row: QuoteHistory) {
    if (!row.vehicleId || !row.brandCode || !row.locationId) {
      return;
    }
    const extras = extrasFromPayload(row.payload);
    const offers = offersFromPayload(row.payload);
    saveExtras(row.vehicleId, extras);
    savePolicyChoices(row.vehicleId, {
      usageType: row.usageType === "COMMERCIAL" ? "COMMERCIAL" : "PRIVATE",
      selectedOfferIds: offers.selectedOfferIds,
      forgoneOfferIds: offers.forgoneOfferIds,
    });
    const params = new URLSearchParams();
    params.set("locationId", String(row.locationId));
    if (row.categoryId) {
      params.set("categoryId", String(row.categoryId));
    }
    if (row.includeOptional) {
      params.set("optional", "1");
    }
    if (row.customerName) {
      params.set("name", row.customerName);
    }
    if (row.customerAddress) {
      params.set("address", row.customerAddress);
    }
    if (row.color) {
      params.set("color", row.color);
    }
    params.set("usage", row.usageType === "COMMERCIAL" ? "commercial" : "private");
    navigate(`/brand/${row.brandCode}/vehicles/${row.vehicleId}/on-road?${params.toString()}`);
  }

  return (
    <div className="min-h-screen">
      <Header />
      <main className="mx-auto max-w-page px-5 py-6">
        <p className="text-xs uppercase tracking-[0.18em] text-copper">{t("quoteHistory.nav")}</p>
        <h1 className="mt-1 font-display text-3xl">{t("quoteHistory.title")}</h1>

        <label className="relative mt-5 block">
          <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-ink/40" />
          <input
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder={t("quoteHistory.search")}
            className="h-11 w-full rounded-xl border border-ink/10 bg-white pl-10 pr-3 text-sm shadow-card"
          />
        </label>

        {error && <p className="mt-3 text-sm text-red-700">{error}</p>}

        <div className="mt-4 overflow-x-auto rounded-2xl border border-ink/8 bg-white shadow-card">
          {loading ? (
            <p className="px-4 py-8 text-sm text-ink/55">{t("loadingCatalog")}</p>
          ) : visible.length === 0 ? (
            <p className="px-4 py-8 text-sm text-ink/55">{t("quoteHistory.empty")}</p>
          ) : (
            <table className="min-w-full text-left text-sm">
              <thead className="bg-paper text-[11px] uppercase tracking-wide text-ink/50">
                <tr>
                  <th className="px-3 py-2 font-medium">{t("quoteHistory.date")}</th>
                  <th className="px-3 py-2 font-medium">{t("quoteHistory.customer")}</th>
                  <th className="px-3 py-2 font-medium">{t("quoteHistory.vehicle")}</th>
                  <th className="px-3 py-2 font-medium">{t("quoteHistory.location")}</th>
                  <th className="px-3 py-2 font-medium">{t("quoteHistory.total")}</th>
                  <th className="px-3 py-2 font-medium">{t("admin.actions")}</th>
                </tr>
              </thead>
              <tbody>
                {visible.map((row) => (
                  <tr key={row.id} className="border-t border-ink/6">
                    <td className="px-3 py-2">{formatQuoteDate(row.createdAt, lang)}</td>
                    <td className="px-3 py-2">
                      <p className="font-medium">{row.customerName}</p>
                      {row.customerAddress && <p className="text-xs text-ink/50">{row.customerAddress}</p>}
                    </td>
                    <td className="px-3 py-2">{row.vehicleName}</td>
                    <td className="px-3 py-2">{row.locationName}</td>
                    <td className="px-3 py-2">{formatVnd(row.onRoadTotal)}</td>
                    <td className="px-3 py-2">
                      <button type="button" onClick={() => openQuote(row)} className="text-sm font-semibold text-copper">
                        {t("quoteHistory.open")}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </main>
    </div>
  );
}

interface StoredQuotePayload {
  discountAmount?: number;
  deposit?: number;
  optionalBodyInsurance?: number;
  registrationServiceFee?: number;
  micaPlateFee?: number;
  inspectionFee?: number;
  accessories?: QuoteExtras["accessories"];
  selectedOfferIds?: string[];
  forgoneOfferIds?: string[];
}

function matchesHistoryQuery(row: QuoteHistory, query: string) {
  return softIncludes(query, row.customerName, row.customerAddress, row.vehicleName, row.locationName);
}

function extrasFromPayload(payload?: string): QuoteExtras {
  const parsed = readPayload(payload);
  return {
    discountAmount: numberOrUndefined(parsed.discountAmount),
    deposit: numberOrUndefined(parsed.deposit),
    optionalBodyInsurance: numberOrUndefined(parsed.optionalBodyInsurance),
    registrationServiceFee: numberOrUndefined(parsed.registrationServiceFee),
    micaPlateFee: numberOrUndefined(parsed.micaPlateFee),
    inspectionFee: numberOrUndefined(parsed.inspectionFee),
    accessories: Array.isArray(parsed.accessories) ? parsed.accessories : [],
  };
}

function offersFromPayload(payload?: string) {
  const parsed = readPayload(payload);
  return {
    selectedOfferIds: Array.isArray(parsed.selectedOfferIds) ? parsed.selectedOfferIds.map(String) : [],
    forgoneOfferIds: Array.isArray(parsed.forgoneOfferIds) ? parsed.forgoneOfferIds.map(String) : [],
  };
}

function readPayload(payload?: string): StoredQuotePayload {
  if (!payload) {
    return {};
  }
  try {
    return JSON.parse(payload) as StoredQuotePayload;
  } catch {
    return {};
  }
}

function numberOrUndefined(value: unknown): number | undefined {
  const next = Number(value);
  return Number.isFinite(next) ? next : undefined;
}

function formatQuoteDate(value: string, lang: string) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return new Intl.DateTimeFormat(lang === "vi" ? "vi-VN" : lang, {
    dateStyle: "short",
    timeStyle: "short",
  }).format(date);
}
