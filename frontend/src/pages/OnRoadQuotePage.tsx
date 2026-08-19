import { ArrowLeft, FileSpreadsheet, FileText, Languages, RefreshCw } from "lucide-react";
import { useEffect, useState } from "react";
import { Link, useParams, useSearchParams } from "react-router-dom";
import { api } from "../api/client";
import { Header } from "../components/Header";
import { QuoteAccessoriesPanel, QuotePricePanel } from "../components/QuoteAdjustments";
import { QuoteSheet } from "../components/QuoteSheet";
import { useI18n } from "../i18n/LanguageContext";
import { languages, type Lang } from "../i18n/translations";
import { downloadQuotePdf } from "../lib/exportQuotePdf";
import { extrasFromVehicle, loadExtras, saveExtras } from "../lib/quoteExtras";
import { defaultPolicyChoices, loadPolicyChoices } from "../lib/quotePolicy";
import type { CostBreakdown as CostBreakdownType, QuoteExtras, VehicleDetail } from "../types";

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
  const [customerName, setCustomerName] = useState(searchParams.get("name") ?? "");
  const [customerAddress, setCustomerAddress] = useState(searchParams.get("address") ?? "");
  const color = searchParams.get("color") ?? "";
  const usageType = searchParams.get("usage") === "commercial" ? "COMMERCIAL" : "PRIVATE";
  const policyChoices = id
    ? loadPolicyChoices(id, { ...defaultPolicyChoices(), usageType })
    : defaultPolicyChoices();

  const [vehicle, setVehicle] = useState<VehicleDetail | null>(null);
  const [result, setResult] = useState<CostBreakdownType | null>(null);
  const [extras, setExtras] = useState<QuoteExtras>({ accessories: [] });
  const [loading, setLoading] = useState(true);
  const [calculating, setCalculating] = useState(false);
  const [exporting, setExporting] = useState<"xlsx" | "pdf" | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);

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

    api.getVehicle(id)
      .then(async (nextVehicle) => {
        const nextExtras = loadExtras(id, extrasFromVehicle(nextVehicle));
        const breakdown = await api.calculateOnRoadCost(
          id,
          locationId,
          includeOptional,
          categoryId,
          nextExtras,
          policyChoices.usageType,
          policyChoices.selectedOfferIds,
          policyChoices.forgoneOfferIds
        );
        if (cancelled) {
          return;
        }
        setVehicle(nextVehicle);
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
    if (!id || !locationId) {
      setError(t("missingQuoteParams"));
      return;
    }
    const name = customerName.trim() || t("customerName");
    setExporting("xlsx");
    setError(null);
    setNotice(null);
    saveExtras(id, extras);
    try {
      const blob = await api.exportQuote(quotePayload(name));
      setNotice(t("quoteHistory.saved"));
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = `quote-${vehicle?.model ?? "mitsubishi"}-${exportLang}.xlsx`;
      link.click();
      URL.revokeObjectURL(url);
    } catch (err) {
      setError(err instanceof Error ? err.message : t("apiError"));
    } finally {
      setExporting(null);
    }
  }

  async function exportQuotePdf() {
    const sheet = document.getElementById("quote-sheet");
    if (!sheet) {
      setError(t("apiError"));
      return;
    }
    setExporting("pdf");
    setError(null);
    setNotice(null);
    try {
      const name = customerName.trim() || t("customerName");
      try {
        await api.saveQuote(quotePayload(name));
        setNotice(t("quoteHistory.saved"));
      } catch {
        setNotice(null);
      }
      await downloadQuotePdf(sheet, `quote-${vehicle?.model ?? "mitsubishi"}-${exportLang}.pdf`);
    } catch (err) {
      setError(err instanceof Error ? err.message : t("apiError"));
    } finally {
      setExporting(null);
    }
  }

  function quotePayload(name: string) {
    return {
      vehicleId: id,
      locationId,
      categoryId,
      includeOptionalInsurance: includeOptional,
      customerName: name,
      customerAddress: customerAddress.trim(),
      color,
      language: exportLang,
      extras,
      usageType: policyChoices.usageType,
      selectedOfferIds: policyChoices.selectedOfferIds,
      forgoneOfferIds: policyChoices.forgoneOfferIds,
    };
  }

  async function recalculate() {
    if (!id || !locationId) {
      return;
    }
    setCalculating(true);
    setError(null);
    saveExtras(id, extras);
    try {
      const breakdown = await api.calculateOnRoadCost(
        id,
        locationId,
        includeOptional,
        categoryId,
        extras,
        policyChoices.usageType,
        policyChoices.selectedOfferIds,
        policyChoices.forgoneOfferIds
      );
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
        <Header />
        <p className="mx-auto max-w-page px-5 py-16 text-ink/60">{t("calculating")}</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen">
      <Header />
      <main className="mx-auto max-w-page px-5 py-6 print:max-w-none print:px-0">
        <div className="mb-4 print:hidden">
          <Link to={detailsHref} className="inline-flex items-center gap-1.5 text-sm text-ink/55 hover:text-ink">
            <ArrowLeft className="h-4 w-4" />
            {t("backToDetails")}
          </Link>
        </div>

        {error && <p className="mb-3 text-sm text-red-700 print:hidden">{error}</p>}
        {notice && <p className="mb-3 text-sm text-forest print:hidden">{notice}</p>}

        {vehicle && (
          <div className="mb-5 grid gap-3 print:hidden lg:grid-cols-2">
            <QuotePricePanel
              extras={extras}
              onChange={setExtras}
              action={
                <button
                  type="button"
                  onClick={recalculate}
                  disabled={calculating}
                  className="mt-4 inline-flex h-12 w-full items-center justify-center gap-1.5 rounded-xl bg-ink text-sm font-semibold text-paper hover:bg-forest disabled:opacity-60"
                >
                  <RefreshCw className={`h-3.5 w-3.5 ${calculating ? "animate-spin" : ""}`} />
                  {calculating ? t("calculating") : t("recalculate")}
                </button>
              }
            />
            <QuoteAccessoriesPanel extras={extras} onChange={setExtras} />
          </div>
        )}

        {vehicle && result && (
          <QuoteSheet
            vehicle={vehicle}
            result={result}
            customerName={customerName}
            customerAddress={customerAddress}
            color={color}
            selectedAccessories={extras.accessories}
            language={exportLang}
          />
        )}

        {result && (
          <section className="mt-5 space-y-3 rounded-2xl border border-ink/8 bg-white p-3 shadow-card print:hidden">
            <div className="grid gap-2 sm:grid-cols-2">
              <label className="block text-[11px] font-medium text-ink/75">
                {t("customerName")}
                <input
                  value={customerName}
                  onChange={(event) => setCustomerName(event.target.value)}
                  className="mt-1 h-8 w-full rounded-md border border-ink/10 bg-paper px-2 text-sm"
                />
              </label>
              <label className="block text-[11px] font-medium text-ink/75">
                {t("customerAddress")}
                <input
                  value={customerAddress}
                  onChange={(event) => setCustomerAddress(event.target.value)}
                  className="mt-1 h-8 w-full rounded-md border border-ink/10 bg-paper px-2 text-sm"
                />
              </label>
            </div>
            <div className="flex flex-wrap items-center justify-between gap-2">
              <label className="inline-flex items-center gap-2 text-sm text-ink/70">
                <Languages className="h-4 w-4 text-copper" />
                <span>{t("exportLanguage")}</span>
                <select
                  value={exportLang}
                  onChange={(event) => setExportLang(event.target.value as Lang)}
                  className="h-8 rounded-md border border-ink/15 bg-paper px-2 text-sm font-semibold text-ink"
                >
                  {languages.map((item) => (
                    <option key={item.code} value={item.code}>
                      {item.label}
                    </option>
                  ))}
                </select>
              </label>
              <div className="flex flex-wrap items-center gap-2">
                <button
                  type="button"
                  onClick={exportQuote}
                  disabled={exporting !== null}
                  className="inline-flex h-8 items-center gap-1.5 rounded-md bg-ink px-4 text-sm font-semibold text-paper hover:bg-forest disabled:opacity-60"
                >
                  <FileSpreadsheet className="h-4 w-4" />
                  {exporting === "xlsx" ? t("exporting") : t("exportExcel")}
                </button>
                <button
                  type="button"
                  onClick={exportQuotePdf}
                  disabled={exporting !== null}
                  className="inline-flex h-8 items-center gap-1.5 rounded-md border border-ink/15 bg-white px-4 text-sm font-semibold text-ink hover:bg-paper disabled:opacity-60"
                >
                  <FileText className="h-4 w-4" />
                  {exporting === "pdf" ? t("exportingPdf") : t("exportPdf")}
                </button>
              </div>
            </div>
          </section>
        )}
      </main>
    </div>
  );
}
