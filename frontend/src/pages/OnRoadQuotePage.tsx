import { useEffect, useState } from "react";
import { Link, useParams, useSearchParams } from "react-router-dom";
import { api } from "../api/client";
import { Header } from "../components/Header";
import { QuoteAdjustments } from "../components/QuoteAdjustments";
import { QuoteSheet } from "../components/QuoteSheet";
import { useI18n } from "../i18n/LanguageContext";
import { languages, type Lang } from "../i18n/translations";
import { extrasFromVehicle, loadExtras, saveExtras } from "../lib/quoteExtras";
import type { Brand, CostBreakdown as CostBreakdownType, QuoteExtras, VehicleDetail } from "../types";

export function OnRoadQuotePage() {
  const { vehicleId, brandCode = "" } = useParams();
  const id = Number(vehicleId);
  const [searchParams] = useSearchParams();
  const { t, lang } = useI18n();
  const [exportLang, setExportLang] = useState<Lang>(lang);

  useEffect(() => {
    setExportLang(lang);
  }, [lang]);

  const locationId = Number(searchParams.get("locationId"));
  const categoryId = searchParams.get("categoryId") ? Number(searchParams.get("categoryId")) : undefined;
  const includeOptional = searchParams.get("optional") === "1";
  const customerName = searchParams.get("name") ?? "";
  const customerAddress = searchParams.get("address") ?? "";
  const color = searchParams.get("color") ?? "";

  const [brand, setBrand] = useState<Brand | null>(null);
  const [vehicle, setVehicle] = useState<VehicleDetail | null>(null);
  const [result, setResult] = useState<CostBreakdownType | null>(null);
  const [extras, setExtras] = useState<QuoteExtras>({ accessories: [] });
  const [loading, setLoading] = useState(true);
  const [calculating, setCalculating] = useState(false);
  const [exporting, setExporting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const detailsHref = `/brand/${brandCode}/vehicles/${id}`;

  useEffect(() => {
    if (!id || !locationId) {
      setLoading(false);
      setError(t("missingQuoteParams"));
      return;
    }

    let cancelled = false;
    setLoading(true);
    setError(null);

    Promise.all([api.getVehicle(id), api.getBrand(brandCode)])
      .then(async ([nextVehicle, nextBrand]) => {
        const nextExtras = loadExtras(id, extrasFromVehicle(nextVehicle));
        const breakdown = await api.calculateOnRoadCost(
          id,
          locationId,
          includeOptional,
          categoryId,
          nextExtras
        );
        if (cancelled) {
          return;
        }
        setVehicle(nextVehicle);
        setBrand(nextBrand);
        setExtras(nextExtras);
        setResult(breakdown);
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
  }, [id, brandCode, locationId, categoryId, includeOptional, t]);

  async function exportQuote() {
    if (!id || !locationId || !customerName.trim()) {
      setError(t("customerNameRequired"));
      return;
    }
    setExporting(true);
    setError(null);
    saveExtras(id, extras);
    try {
      const blob = await api.exportQuote({
        vehicleId: id,
        locationId,
        categoryId,
        includeOptionalInsurance: includeOptional,
        customerName: customerName.trim(),
        customerAddress: customerAddress.trim(),
        color,
        language: exportLang,
        extras,
      });
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = `quote-${vehicle?.model ?? "mitsubishi"}-${exportLang}.xlsx`;
      link.click();
      URL.revokeObjectURL(url);
    } catch (err) {
      setError(err instanceof Error ? err.message : t("apiError"));
    } finally {
      setExporting(false);
    }
  }

  async function recalculate() {
    if (!id || !locationId) {
      return;
    }
    setCalculating(true);
    setError(null);
    saveExtras(id, extras);
    try {
      const breakdown = await api.calculateOnRoadCost(id, locationId, includeOptional, categoryId, extras);
      setResult(breakdown);
    } catch (err) {
      setError(err instanceof Error ? err.message : t("apiError"));
    } finally {
      setCalculating(false);
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen">
        <Header brandName={brand?.name} />
        <p className="mx-auto max-w-page px-5 py-16 text-ink/60">{t("calculating")}</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen">
      <Header brandName={brand?.name} />
      <main className="mx-auto max-w-page px-5 py-8 print:max-w-none print:px-0">
        <div className="mb-5 flex flex-wrap items-center justify-between gap-3 print:hidden">
          <Link to={detailsHref} className="text-sm text-ink/55 hover:text-ink">
            ← {t("backToDetails")}
          </Link>
          {result && (
            <div className="flex flex-wrap items-center gap-3">
              <label className="flex items-center gap-2 text-sm text-ink/70">
                <span>{t("exportLanguage")}</span>
                <select
                  value={exportLang}
                  onChange={(event) => setExportLang(event.target.value as Lang)}
                  className="h-11 rounded-xl border border-ink/15 bg-white px-3 text-sm font-semibold text-ink"
                >
                  {languages.map((item) => (
                    <option key={item.code} value={item.code}>
                      {item.label}
                    </option>
                  ))}
                </select>
              </label>
              <button
                type="button"
                onClick={exportQuote}
                disabled={exporting}
                className="h-11 rounded-xl border border-ink/15 bg-white px-5 text-sm font-semibold text-ink hover:bg-mist disabled:opacity-60"
              >
                {exporting ? t("exporting") : t("exportQuote")}
              </button>
              <Link
                to={detailsHref}
                className="inline-flex h-11 items-center rounded-xl bg-ink px-5 text-sm font-semibold text-paper hover:bg-forest"
              >
                {t("backToDetails")}
              </Link>
            </div>
          )}
        </div>

        {error && <p className="mb-4 text-sm text-red-700 print:hidden">{error}</p>}

        {vehicle && (
          <section className="mb-6 rounded-3xl border border-ink/8 bg-white p-6 shadow-card print:hidden">
            <QuoteAdjustments extras={extras} onChange={setExtras} />
            <button
              type="button"
              onClick={recalculate}
              disabled={calculating}
              className="mt-5 h-11 rounded-xl bg-ink px-5 text-sm font-semibold text-paper hover:bg-forest disabled:opacity-60"
            >
              {calculating ? t("calculating") : t("recalculate")}
            </button>
          </section>
        )}

        {vehicle && result && (
          <QuoteSheet
            vehicle={vehicle}
            result={result}
            customerName={customerName}
            customerAddress={customerAddress}
            color={color}
            selectedAccessories={extras.accessories}
          />
        )}
      </main>
    </div>
  );
}
