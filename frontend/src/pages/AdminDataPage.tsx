import { Pencil, Plus, Trash2, X } from "lucide-react";
import { colorPhoto } from "../lib/vehicleColor";
import { useEffect, useState } from "react";
import { Header } from "../components/Header";
import { api, UnauthorizedError } from "../api/client";
import { useI18n } from "../i18n/LanguageContext";
import type { Lang } from "../i18n/translations";
import { fillFromVietnamese } from "../lib/fromVietnamese";
import { locationLabel } from "../lib/labels";
import type {
  AdminBrand,
  AdminCategory,
  AdminDealer,
  AdminDealerPolicy,
  AdminFeeDefinition,
  AdminFeePolicy,
  AdminFeeRule,
  AdminLocation,
  AdminPlateRegions,
  AdminVehicle,
  DealerOffer,
} from "../types";

type CatalogTab = "brands" | "categories" | "locations" | "dealers" | "vehicles" | "feeDefinitions" | "feeRules";
type Tab = CatalogTab | "feePolicy" | "dealerPolicy" | "plateRegions";

type FieldType = "text" | "number" | "boolean" | "textarea" | "select" | "langs" | "specs" | "colors";

interface Field {
  key: string;
  type: FieldType;
  options?: string[];
  ref?: "brand" | "category" | "location" | "fee";
}

const TABS: { id: Tab; labelKey: string }[] = [
  { id: "vehicles", labelKey: "admin.vehicles" },
  { id: "brands", labelKey: "admin.brands" },
  { id: "categories", labelKey: "admin.categories" },
  { id: "locations", labelKey: "admin.locations" },
  { id: "dealers", labelKey: "admin.dealers" },
  { id: "feePolicy", labelKey: "admin.feePolicy" },
  { id: "dealerPolicy", labelKey: "admin.dealerPolicy" },
  { id: "plateRegions", labelKey: "admin.plateRegions" },
  { id: "feeDefinitions", labelKey: "admin.feeDefinitions" },
  { id: "feeRules", labelKey: "admin.feeRules" },
];

const FIELDS: Record<CatalogTab, Field[]> = {
  brands: [
    { key: "name", type: "text" },
    { key: "tagline", type: "text" },
    { key: "imageUrl", type: "text" },
    { key: "ready", type: "boolean" },
  ],
  categories: [
    { key: "name", type: "text" },
    { key: "description", type: "textarea" },
    { key: "typicalSeats", type: "number" },
    { key: "requiresInspection", type: "boolean" },
    { key: "requiresRoadUseFee", type: "boolean" },
    { key: "requiresCompulsoryInsurance", type: "boolean" },
  ],
  locations: [
    { key: "name", type: "langs" },
    { key: "region", type: "select", options: ["NORTH", "CENTRAL", "SOUTH"] },
    { key: "feeZone", type: "select", options: ["SPECIAL", "MAJOR", "STANDARD"] },
    { key: "centrallyGovernedCity", type: "boolean" },
  ],
  dealers: [
    { key: "brandCode", type: "select", ref: "brand" },
    { key: "name", type: "text" },
    { key: "address", type: "text" },
    { key: "active", type: "boolean" },
  ],
  vehicles: [
    { key: "brandCode", type: "select", ref: "brand" },
    { key: "categoryCode", type: "select", ref: "category" },
    { key: "model", type: "text" },
    { key: "name", type: "text" },
    { key: "listPrice", type: "number" },
    { key: "year", type: "number" },
    { key: "seats", type: "number" },
    { key: "vehicleType", type: "select", options: ["Sedan", "SUV", "CUV", "MPV", "Pickup", "Van", "Truck", "ICE"] },
    { key: "fuelType", type: "select", options: ["Gasoline", "Diesel", "Hybrid", "Electric"] },
    { key: "transmission", type: "select", options: ["Automatic", "CVT", "Manual"] },
    { key: "colorPhotos", type: "colors" },
    { key: "imageUrl", type: "text" },
    { key: "specifications", type: "specs" },
    { key: "active", type: "boolean" },
  ],
  feeDefinitions: [
    { key: "name", type: "text" },
    { key: "description", type: "textarea" },
    { key: "mandatory", type: "boolean" },
    { key: "active", type: "boolean" },
  ],
  feeRules: [
    { key: "feeDefinitionCode", type: "select", ref: "fee" },
    { key: "categoryCode", type: "select", ref: "category" },
    { key: "locationCode", type: "select", ref: "location" },
    { key: "feeZone", type: "select", options: ["", "SPECIAL", "MAJOR", "STANDARD"] },
    { key: "calculationType", type: "select", options: ["FIXED", "PERCENT_OF_LIST_PRICE", "PERCENT_WITH_BOUNDS"] },
    { key: "fixedAmount", type: "number" },
    { key: "percentage", type: "number" },
    { key: "active", type: "boolean" },
  ],
};

const COLUMNS: Record<CatalogTab, string[]> = {
  brands: ["name", "ready"],
  categories: ["name"],
  locations: ["name", "region"],
  dealers: ["brandCode", "name"],
  vehicles: ["name", "brandCode", "categoryCode", "listPrice"],
  feeDefinitions: ["name", "mandatory"],
  feeRules: ["feeDefinitionCode", "categoryCode", "feeZone", "calculationType", "fixedAmount", "percentage"],
};

export function AdminDataPage() {
  const { t, lang } = useI18n();
  const [tab, setTab] = useState<Tab>("vehicles");
  const [brands, setBrands] = useState<AdminBrand[]>([]);
  const [categories, setCategories] = useState<AdminCategory[]>([]);
  const [locations, setLocations] = useState<AdminLocation[]>([]);
  const [fees, setFees] = useState<AdminFeeDefinition[]>([]);
  const [rows, setRows] = useState<Record<string, unknown>[]>([]);
  const [draft, setDraft] = useState<Record<string, unknown> | null>(null);
  const [feePolicy, setFeePolicy] = useState<AdminFeePolicy | null>(null);
  const [dealerPolicy, setDealerPolicy] = useState<AdminDealerPolicy | null>(null);
  const [plates, setPlates] = useState<AdminPlateRegions | null>(null);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);

  async function loadLookups() {
    const [nextBrands, nextCategories, nextLocations, nextFees] = await Promise.all([
      api.listAdminBrands(),
      api.listAdminCategories(),
      api.listAdminLocations(),
      api.listAdminFeeDefinitions(),
    ]);
    setBrands(nextBrands);
    setCategories(nextCategories);
    setLocations(nextLocations);
    setFees(nextFees);
  }

  async function load(nextTab = tab) {
    setLoading(true);
    setError(null);
    try {
      await loadLookups();
      if (nextTab === "feePolicy") {
        setFeePolicy(await api.getAdminFeePolicy());
      } else if (nextTab === "dealerPolicy") {
        setDealerPolicy(await api.getAdminDealerPolicy());
      } else if (nextTab === "plateRegions") {
        setPlates(await api.getAdminPlateRegions());
      } else {
        setRows((await listFor(nextTab)) as Record<string, unknown>[]);
      }
    } catch (err) {
      if (err instanceof UnauthorizedError) {
        return;
      }
      setError(err instanceof Error ? err.message : t("apiError"));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void load(tab);
    setDraft(null);
  }, [tab]);

  function startNew() {
    const firstBrand = brands[0]?.code ?? "";
    const firstCategory = categories.find((item) => item.code === "PASSENGER_CAR_4")?.code ?? categories[0]?.code ?? "";
    const firstFee = fees[0]?.code ?? "";
    const base: Record<string, unknown> = {
      active: true,
      ready: true,
      mandatory: true,
      market: "Vietnam",
      name: "",
      nameEn: "",
      nameZh: "",
      nameJa: "",
    };
    if (tab === "vehicles") {
      Object.assign(base, {
        brandCode: firstBrand,
        categoryCode: firstCategory,
        vehicleType: "SUV",
        fuelType: "Gasoline",
        transmission: "Automatic",
        specifications: {},
        colorPhotos: {},
        defaultColor: "",
      });
    } else if (tab === "dealers") {
      Object.assign(base, { brandCode: firstBrand });
    } else if (tab === "locations") {
      Object.assign(base, { region: "SOUTH", feeZone: "STANDARD" });
    } else if (tab === "feeRules") {
      Object.assign(base, { feeDefinitionCode: firstFee, calculationType: "FIXED", categoryCode: firstCategory });
    }
    setDraft(base);
  }

  async function saveDraft() {
    if (!draft || !isCatalog(tab)) {
      return;
    }
    setSaving(true);
    setError(null);
    try {
      const payload = { ...draft };
      if (tab === "vehicles") {
        const photos = (payload.colorPhotos as Record<string, string>) ?? {};
        const names = Object.keys(photos).map((name) => name.trim()).filter(Boolean);
        payload.availableColors = names.join(", ");
        payload.defaultColor = String(payload.defaultColor || names[0] || "");
      }
      await saveFor(tab, payload);
      setDraft(null);
      setNotice(t("admin.saved"));
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : t("apiError"));
    } finally {
      setSaving(false);
    }
  }

  async function remove(id: number) {
    if (!isCatalog(tab) || !window.confirm(t("admin.confirmDelete"))) {
      return;
    }
    try {
      await deleteFor(tab, id);
      setNotice(t("admin.deleted"));
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : t("apiError"));
    }
  }

  function optionLabel(value: string, field: Field) {
    if (!value) {
      return t("admin.any");
    }
    if (field.ref === "brand") {
      return brands.find((item) => item.code === value)?.name ?? value;
    }
    if (field.ref === "category") {
      const key = `category.${value}`;
      return t(key) === key ? categories.find((item) => item.code === value)?.name ?? value : t(key);
    }
    if (field.ref === "location") {
      const location = locations.find((item) => item.code === value);
      return locationName(location, lang) || value;
    }
    if (field.ref === "fee") {
      const key = `fee.${value}`;
      return t(key) === key ? fees.find((item) => item.code === value)?.name ?? value : t(key);
    }
    const key = `admin.opt.${value}`;
    return t(key) === key ? value : t(key);
  }

  function fieldOptions(field: Field) {
    if (field.ref === "brand") {
      return brands.map((item) => item.code ?? "").filter(Boolean);
    }
    if (field.ref === "category") {
      return ["", ...categories.map((item) => item.code)];
    }
    if (field.ref === "location") {
      return ["", ...locations.map((item) => item.code)];
    }
    if (field.ref === "fee") {
      return fees.map((item) => item.code).filter((code) => code !== "LICENSE_PLATE" && code !== "REGISTRATION_TAX");
    }
    return field.options ?? [];
  }

  function displayCell(column: string, row: Record<string, unknown>) {
    if (column === "name") {
      return localizedName(tab, row, lang, t);
    }
    const value = row[column];
    if (column === "brandCode") {
      return optionLabel(String(value ?? ""), { key: column, type: "select", ref: "brand" });
    }
    if (column === "categoryCode") {
      return optionLabel(String(value ?? ""), { key: column, type: "select", ref: "category" });
    }
    if (column === "feeDefinitionCode") {
      return optionLabel(String(value ?? ""), { key: column, type: "select", ref: "fee" });
    }
    if (column === "region" || column === "feeZone" || column === "calculationType") {
      return optionLabel(String(value ?? ""), { key: column, type: "select" });
    }
    if (typeof value === "boolean") {
      return value ? t("admin.yes") : t("admin.no");
    }
    if (value == null || value === "") {
      return "—";
    }
    return String(value);
  }

  return (
    <div className="min-h-screen">
      <Header />
      <main className="mx-auto max-w-page px-5 py-6">
        <p className="text-xs uppercase tracking-[0.18em] text-copper">{t("admin.kicker")}</p>
        <h1 className="mt-1 font-display text-3xl">{t("admin.title")}</h1>

        <div className="mt-4 flex flex-wrap gap-2">
          {TABS.map((item) => (
            <button
              key={item.id}
              type="button"
              onClick={() => setTab(item.id)}
              className={`rounded-full px-3 py-1.5 text-sm ${
                tab === item.id ? "bg-ink text-paper" : "bg-white text-ink/70 shadow-card"
              }`}
            >
              {t(item.labelKey)}
            </button>
          ))}
        </div>

        {error && <p className="mt-3 text-sm text-red-700">{error}</p>}
        {notice && <p className="mt-3 text-sm text-forest">{notice}</p>}

        {tab === "feePolicy" && feePolicy && (
          <FeePolicyForm
            value={feePolicy}
            saving={saving}
            t={t}
            onChange={setFeePolicy}
            onSave={async () => {
              setSaving(true);
              try {
                setFeePolicy(await api.saveAdminFeePolicy(feePolicy));
                setNotice(t("admin.saved"));
              } catch (err) {
                setError(err instanceof Error ? err.message : t("apiError"));
              } finally {
                setSaving(false);
              }
            }}
          />
        )}

        {tab === "dealerPolicy" && dealerPolicy && (
          <DealerPolicyForm
            value={dealerPolicy}
            saving={saving}
            t={t}
            onChange={setDealerPolicy}
            onSave={async () => {
              setSaving(true);
              try {
                setDealerPolicy(await api.saveAdminDealerPolicy(dealerPolicy));
                setNotice(t("admin.saved"));
              } catch (err) {
                setError(err instanceof Error ? err.message : t("apiError"));
              } finally {
                setSaving(false);
              }
            }}
          />
        )}

        {tab === "plateRegions" && plates && (
          <PlateRegionsForm
            value={plates}
            locations={locations}
            lang={lang}
            saving={saving}
            t={t}
            onChange={setPlates}
            onSave={async () => {
              setSaving(true);
              try {
                setPlates(await api.saveAdminPlateRegions(plates));
                setNotice(t("admin.saved"));
              } catch (err) {
                setError(err instanceof Error ? err.message : t("apiError"));
              } finally {
                setSaving(false);
              }
            }}
          />
        )}

        {isCatalog(tab) && (
          <>
            <div className="mt-4 overflow-x-auto rounded-2xl border border-ink/8 bg-white shadow-card">
              <div className="flex items-center justify-between border-b border-ink/8 px-4 py-3">
                <p className="text-sm font-semibold">
                  {t(TABS.find((item) => item.id === tab)?.labelKey ?? "")} · {rows.length}
                </p>
                <button type="button" onClick={startNew} className="inline-flex items-center gap-1.5 text-sm font-semibold text-copper">
                  <Plus className="h-4 w-4" />
                  {t("admin.new")}
                </button>
              </div>
              {loading ? (
                <p className="px-4 py-8 text-sm text-ink/55">{t("loadingCatalog")}</p>
              ) : (
                <table className="min-w-full text-left text-sm">
                  <thead className="bg-paper text-[11px] uppercase tracking-wide text-ink/50">
                    <tr>
                      {COLUMNS[tab].map((column) => (
                        <th key={column} className="px-3 py-2 font-medium">
                          {t(`admin.field.${column}`)}
                        </th>
                      ))}
                      <th className="px-3 py-2 font-medium">{t("admin.actions")}</th>
                    </tr>
                  </thead>
                  <tbody>
                    {rows.map((row) => (
                      <tr key={String(row.id)} className="border-t border-ink/6">
                        {COLUMNS[tab].map((column) => (
                          <td key={column} className="px-3 py-2">
                            {displayCell(column, row)}
                          </td>
                        ))}
                        <td className="px-3 py-2">
                          <div className="flex gap-2">
                            <button type="button" onClick={() => setDraft(prepareDraft(row))} className="text-ink/60 hover:text-ink">
                              <Pencil className="h-4 w-4" />
                            </button>
                            {typeof row.id === "number" && (
                              <button type="button" onClick={() => void remove(row.id as number)} className="text-ink/60 hover:text-red-700">
                                <Trash2 className="h-4 w-4" />
                              </button>
                            )}
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>

            {draft && (
              <div className="fixed inset-0 z-40 flex items-start justify-center overflow-y-auto bg-ink/45 p-4 pt-16" onClick={() => setDraft(null)}>
                <form
                  className="w-full max-w-3xl rounded-2xl border border-ink/8 bg-white p-5 shadow-card"
                  onClick={(event) => event.stopPropagation()}
                  onSubmit={(event) => {
                    event.preventDefault();
                    void saveDraft();
                  }}
                >
                  <div className="flex items-center justify-between gap-3">
                    <p className="text-sm font-semibold">{draft.id ? t("admin.edit") : t("admin.new")}</p>
                    <button type="button" onClick={() => setDraft(null)} className="text-ink/50 hover:text-ink">
                      <X className="h-5 w-5" />
                    </button>
                  </div>
                  <div className="mt-3 grid max-h-[70vh] gap-3 overflow-y-auto pr-1 md:grid-cols-2">
                    {FIELDS[tab].map((field) => (
                      <FieldInput
                        key={field.key}
                        field={field}
                        draft={draft}
                        setDraft={setDraft}
                        t={t}
                        options={fieldOptions(field)}
                        optionLabel={(value) => optionLabel(value, field)}
                      />
                    ))}
                  </div>
                  <div className="mt-4 flex gap-2">
                    <button type="submit" disabled={saving} className="h-10 rounded-lg bg-ink px-4 text-sm font-semibold text-paper disabled:opacity-60">
                      {saving ? t("admin.saving") : t("admin.save")}
                    </button>
                    <button type="button" onClick={() => setDraft(null)} className="h-10 rounded-lg px-4 text-sm text-ink/60">
                      {t("admin.cancel")}
                    </button>
                  </div>
                </form>
              </div>
            )}
          </>
        )}
      </main>
    </div>
  );
}

function LangFields({
  draft,
  setDraft,
  t,
}: {
  draft: Record<string, unknown>;
  setDraft: (next: Record<string, unknown>) => void;
  t: (key: string) => string;
}) {
  const [translating, setTranslating] = useState(false);

  async function translateFromVietnamese() {
    const vietnamese = String(draft.name ?? "").trim();
    if (!vietnamese) {
      return;
    }
    setTranslating(true);
    try {
      const filled = await fillFromVietnamese(vietnamese, {
        vi: vietnamese,
        en: String(draft.nameEn ?? ""),
        zh: String(draft.nameZh ?? ""),
        ja: String(draft.nameJa ?? ""),
      });
      setDraft({ ...draft, name: filled.vi, nameEn: filled.en, nameZh: filled.zh, nameJa: filled.ja });
    } catch {
      setDraft({
        ...draft,
        nameEn: String(draft.nameEn || vietnamese),
        nameZh: String(draft.nameZh || vietnamese),
        nameJa: String(draft.nameJa || vietnamese),
      });
    } finally {
      setTranslating(false);
    }
  }

  return (
    <div className="md:col-span-2 grid gap-3 md:grid-cols-2">
      <label className="block text-xs font-medium text-ink/70 md:col-span-2">
        {t("admin.field.name_vi")}
        <input
          value={String(draft.name ?? "")}
          onChange={(event) => setDraft({ ...draft, name: event.target.value })}
          onBlur={() => void translateFromVietnamese()}
          className="mt-1 h-10 w-full rounded-lg border border-ink/10 bg-paper px-3 text-sm"
        />
        <span className="mt-1 block text-[11px] font-normal text-ink/45">
          {translating ? t("admin.translating") : t("admin.viHint")}
        </span>
      </label>
      {(["en", "zh", "ja"] as Lang[]).map((code) => {
        const key = `name${code[0].toUpperCase()}${code.slice(1)}`;
        return (
          <label key={code} className="block text-xs font-medium text-ink/70">
            {t(`admin.field.name_${code}`)}
            <input
              value={String(draft[key] ?? "")}
              onChange={(event) => setDraft({ ...draft, [key]: event.target.value })}
              className="mt-1 h-10 w-full rounded-lg border border-ink/10 bg-paper px-3 text-sm"
            />
          </label>
        );
      })}
    </div>
  );
}

function FieldInput({
  field,
  draft,
  setDraft,
  t,
  options,
  optionLabel,
}: {
  field: Field;
  draft: Record<string, unknown>;
  setDraft: (next: Record<string, unknown>) => void;
  t: (key: string) => string;
  options: string[];
  optionLabel: (value: string) => string;
}) {
  if (field.type === "langs") {
    return (
      <LangFields
        draft={draft}
        setDraft={setDraft}
        t={t}
      />
    );
  }

  if (field.type === "colors") {
    const photos = (draft.colorPhotos as Record<string, string>) ?? {};
    const rows = Object.keys(photos).length ? Object.entries(photos) : [["", ""]];
    return (
      <div className="md:col-span-2">
        <p className="text-xs font-medium text-ink/70">{t("admin.field.colorPhotos")}</p>
        <div className="mt-2 space-y-2">
          {rows.map(([name, url], index) => (
            <div key={index} className="grid gap-2 md:grid-cols-[8rem_1fr_4.5rem_auto]">
              <input
                value={name}
                placeholder={t("admin.colorName")}
                onChange={(event) => {
                  const next = Object.fromEntries(rows.map((row, rowIndex) => (rowIndex === index ? [event.target.value, row[1]] : row)));
                  setDraft({ ...draft, colorPhotos: next, defaultColor: draft.defaultColor === name ? event.target.value : draft.defaultColor });
                }}
                className="h-10 rounded-lg border border-ink/10 bg-paper px-3 text-sm"
              />
              <input
                value={url}
                placeholder={t("admin.colorPhotoUrl")}
                onChange={(event) => {
                  const next = Object.fromEntries(rows.map((row, rowIndex) => (rowIndex === index ? [row[0], event.target.value] : row)));
                  setDraft({ ...draft, colorPhotos: next });
                }}
                className="h-10 rounded-lg border border-ink/10 bg-paper px-3 text-sm"
              />
              <img src={colorPhoto(name, photos)} alt="" className="h-10 w-full rounded-md object-contain bg-paper" />
              <button
                type="button"
                className="text-sm text-red-700"
                onClick={() => {
                  const next = Object.fromEntries(rows.filter((_, rowIndex) => rowIndex !== index));
                  setDraft({ ...draft, colorPhotos: next });
                }}
              >
                {t("admin.remove")}
              </button>
            </div>
          ))}
        </div>
        <button
          type="button"
          className="mt-2 text-sm font-semibold text-copper"
          onClick={() => setDraft({ ...draft, colorPhotos: { ...photos, "": "" } })}
        >
          {t("admin.addColor")}
        </button>
        <label className="mt-3 block text-xs font-medium text-ink/70">
          {t("admin.field.defaultColor")}
          <select
            value={String(draft.defaultColor ?? "")}
            onChange={(event) => setDraft({ ...draft, defaultColor: event.target.value })}
            className="mt-1 h-10 w-full rounded-lg border border-ink/10 bg-paper px-3 text-sm"
          >
            {rows.filter(([name]) => name).map(([name]) => (
              <option key={name} value={name}>
                {name}
              </option>
            ))}
          </select>
        </label>
      </div>
    );
  }

  if (field.type === "specs") {
    const specs = (draft.specifications as Record<string, string>) ?? {};
    const rows = Object.entries(specs);
    if (rows.length === 0) {
      rows.push(["", ""]);
    }
    return (
      <div className="md:col-span-2">
        <p className="text-xs font-medium text-ink/70">{t("admin.field.specifications")}</p>
        <div className="mt-1 space-y-2">
          {rows.map(([key, value], index) => (
            <div key={index} className="grid grid-cols-2 gap-2">
              <input
                value={key}
                placeholder={t("admin.specName")}
                onChange={(event) => {
                  const next = Object.fromEntries(rows.map((row, rowIndex) => (rowIndex === index ? [event.target.value, row[1]] : row)));
                  setDraft({ ...draft, specifications: next });
                }}
                className="h-10 rounded-lg border border-ink/10 bg-paper px-3 text-sm"
              />
              <input
                value={value}
                placeholder={t("admin.specValue")}
                onChange={(event) => {
                  const next = Object.fromEntries(rows.map((row, rowIndex) => (rowIndex === index ? [row[0], event.target.value] : row)));
                  setDraft({ ...draft, specifications: next });
                }}
                className="h-10 rounded-lg border border-ink/10 bg-paper px-3 text-sm"
              />
            </div>
          ))}
        </div>
        <button
          type="button"
          className="mt-2 text-sm font-semibold text-copper"
          onClick={() => setDraft({ ...draft, specifications: { ...specs, "": "" } })}
        >
          {t("admin.addSpec")}
        </button>
      </div>
    );
  }

  return (
    <label className="block text-xs font-medium text-ink/70">
      {t(`admin.field.${field.key}`)}
      {field.type === "boolean" ? (
        <input
          type="checkbox"
          checked={Boolean(draft[field.key])}
          onChange={(event) => setDraft({ ...draft, [field.key]: event.target.checked })}
          className="ml-2 align-middle"
        />
      ) : field.type === "select" ? (
        <select
          value={String(draft[field.key] ?? "")}
          onChange={(event) => setDraft({ ...draft, [field.key]: event.target.value || null })}
          className="mt-1 h-10 w-full rounded-lg border border-ink/10 bg-paper px-3 text-sm"
        >
          {options.map((option) => (
            <option key={option || "any"} value={option}>
              {optionLabel(option)}
            </option>
          ))}
        </select>
      ) : field.type === "textarea" ? (
        <textarea
          value={String(draft[field.key] ?? "")}
          onChange={(event) => setDraft({ ...draft, [field.key]: event.target.value })}
          rows={3}
          className="mt-1 w-full rounded-lg border border-ink/10 bg-paper px-3 py-2 text-sm"
        />
      ) : (
        <input
          type={field.type === "number" ? "number" : "text"}
          value={draft[field.key] == null ? "" : String(draft[field.key])}
          onChange={(event) =>
            setDraft({
              ...draft,
              [field.key]: field.type === "number" ? (event.target.value === "" ? null : Number(event.target.value)) : event.target.value,
            })
          }
          className="mt-1 h-10 w-full rounded-lg border border-ink/10 bg-paper px-3 text-sm"
        />
      )}
    </label>
  );
}

function FeePolicyForm({
  value,
  onChange,
  onSave,
  saving,
  t,
}: {
  value: AdminFeePolicy;
  onChange: (next: AdminFeePolicy) => void;
  onSave: () => void;
  saving: boolean;
  t: (key: string) => string;
}) {
  return (
    <section className="mt-4 rounded-2xl border border-ink/8 bg-white p-4 shadow-card">
      <div className="grid gap-3 md:grid-cols-2">
        <NumberField label={t("admin.field.registrationTaxPercent")} value={value.registrationTaxPercent} onChange={(next) => onChange({ ...value, registrationTaxPercent: next })} />
        <NumberField label={t("admin.field.registrationTaxCommercialPercent")} value={value.registrationTaxCommercialPercent} onChange={(next) => onChange({ ...value, registrationTaxCommercialPercent: next })} />
      </div>
      <div className="mt-4">
        <SaveButton saving={saving} t={t} onClick={onSave} />
      </div>
    </section>
  );
}

function DealerPolicyForm({
  value,
  onChange,
  onSave,
  saving,
  t,
}: {
  value: AdminDealerPolicy;
  onChange: (next: AdminDealerPolicy) => void;
  onSave: () => void;
  saving: boolean;
  t: (key: string) => string;
}) {
  function updateOffer(index: number, next: DealerOffer) {
    const offers = value.offers.map((item, itemIndex) => (itemIndex === index ? next : item));
    onChange({ ...value, offers });
  }

  return (
    <section className="mt-4 space-y-4">
      <div className="rounded-2xl border border-ink/8 bg-white p-4 shadow-card">
        <div className="grid gap-3 md:grid-cols-2">
          <NumberField label={t("admin.field.privateDiscountPercent")} value={value.privateDiscountPercent} onChange={(next) => onChange({ ...value, privateDiscountPercent: next })} />
          <NumberField label={t("admin.field.commercialDiscountPercent")} value={value.commercialDiscountPercent} onChange={(next) => onChange({ ...value, commercialDiscountPercent: next })} />
        </div>
      </div>
      {value.offers.map((offer, index) => (
        <div key={offer.id || index} className="rounded-2xl border border-ink/8 bg-white p-4 shadow-card">
          <div className="flex items-center justify-between">
            <p className="text-sm font-semibold">{t("admin.offer")} {index + 1}</p>
            <button type="button" onClick={() => onChange({ ...value, offers: value.offers.filter((_, itemIndex) => itemIndex !== index) })} className="text-sm text-red-700">
              {t("admin.remove")}
            </button>
          </div>
          <label className="mt-3 block text-xs font-medium text-ink/70">
            {t("admin.field.offerKind")}
            <select
              value={offer.kind}
              onChange={(event) => updateOffer(index, { ...offer, kind: event.target.value })}
              className="mt-1 h-10 w-full rounded-lg border border-ink/10 bg-paper px-3 text-sm"
            >
              {["FORGO_FOR_CREDIT", "EXTRA_PERCENT", "PRICE_CREDIT"].map((kind) => (
                <option key={kind} value={kind}>
                  {t(`admin.opt.${kind}`)}
                </option>
              ))}
            </select>
          </label>
          <div className="mt-3 grid gap-3 md:grid-cols-2">
            <NumberField label={t("admin.field.amount")} value={offer.amount ?? 0} onChange={(next) => updateOffer(index, { ...offer, amount: next })} />
            <NumberField label={t("admin.field.percent")} value={offer.percent ?? 0} onChange={(next) => updateOffer(index, { ...offer, percent: next })} />
          </div>
          <OfferLangFields
            offer={offer}
            t={t}
            onChange={(next) => updateOffer(index, next)}
          />
        </div>
      ))}
      <div className="flex flex-col items-start gap-5 pt-2">
        <button
          type="button"
          className="inline-flex h-10 items-center gap-1.5 text-sm font-semibold text-copper"
          onClick={() =>
            onChange({
              ...value,
              offers: [
                ...value.offers,
                {
                  id: "",
                  kind: "FORGO_FOR_CREDIT",
                  amount: 0,
                  percent: 0,
                  title: { vi: "", en: "", zh: "", ja: "" },
                  description: { vi: "", en: "", zh: "", ja: "" },
                },
              ],
            })
          }
        >
          <Plus className="h-4 w-4" />
          {t("admin.addOffer")}
        </button>
        <SaveButton saving={saving} t={t} onClick={onSave} />
      </div>
    </section>
  );
}

function OfferLangFields({
  offer,
  onChange,
  t,
}: {
  offer: DealerOffer;
  onChange: (next: DealerOffer) => void;
  t: (key: string) => string;
}) {
  const [translating, setTranslating] = useState<"title" | "description" | null>(null);

  async function translateField(field: "title" | "description") {
    const current = { vi: "", en: "", zh: "", ja: "", ...offer[field] };
    const vietnamese = current.vi.trim();
    if (!vietnamese) {
      return;
    }
    setTranslating(field);
    try {
      const filled = await fillFromVietnamese(vietnamese, current);
      onChange({ ...offer, [field]: filled });
    } catch {
      onChange({
        ...offer,
        [field]: {
          ...current,
          en: current.en || vietnamese,
          zh: current.zh || vietnamese,
          ja: current.ja || vietnamese,
        },
      });
    } finally {
      setTranslating(null);
    }
  }

  return (
    <div className="mt-3 grid gap-3 md:grid-cols-2">
      <label className="block text-xs font-medium text-ink/70 md:col-span-2">
        {t("admin.field.offerTitle_vi")}
        <input
          value={offer.title?.vi ?? ""}
          onChange={(event) => onChange({ ...offer, title: { vi: "", en: "", zh: "", ja: "", ...offer.title, vi: event.target.value } })}
          onBlur={() => void translateField("title")}
          className="mt-1 h-10 w-full rounded-lg border border-ink/10 bg-paper px-3 text-sm"
        />
        <span className="mt-1 block text-[11px] font-normal text-ink/45">
          {translating === "title" ? t("admin.translating") : t("admin.viHint")}
        </span>
      </label>
      {(["en", "zh", "ja"] as Lang[]).map((code) => (
        <label key={`title-${code}`} className="block text-xs font-medium text-ink/70">
          {t(`admin.field.offerTitle_${code}`)}
          <input
            value={offer.title?.[code] ?? ""}
            onChange={(event) => onChange({ ...offer, title: { vi: "", en: "", zh: "", ja: "", ...offer.title, [code]: event.target.value } })}
            className="mt-1 h-10 w-full rounded-lg border border-ink/10 bg-paper px-3 text-sm"
          />
        </label>
      ))}
      <label className="block text-xs font-medium text-ink/70 md:col-span-2">
        {t("admin.field.offerDesc_vi")}
        <textarea
          value={offer.description?.vi ?? ""}
          onChange={(event) => onChange({ ...offer, description: { vi: "", en: "", zh: "", ja: "", ...offer.description, vi: event.target.value } })}
          onBlur={() => void translateField("description")}
          rows={2}
          className="mt-1 w-full rounded-lg border border-ink/10 bg-paper px-3 py-2 text-sm"
        />
        <span className="mt-1 block text-[11px] font-normal text-ink/45">
          {translating === "description" ? t("admin.translating") : t("admin.viHint")}
        </span>
      </label>
      {(["en", "zh", "ja"] as Lang[]).map((code) => (
        <label key={`desc-${code}`} className="block text-xs font-medium text-ink/70">
          {t(`admin.field.offerDesc_${code}`)}
          <textarea
            value={offer.description?.[code] ?? ""}
            onChange={(event) => onChange({ ...offer, description: { vi: "", en: "", zh: "", ja: "", ...offer.description, [code]: event.target.value } })}
            rows={2}
            className="mt-1 w-full rounded-lg border border-ink/10 bg-paper px-3 py-2 text-sm"
          />
        </label>
      ))}
    </div>
  );
}

function PlateRegionsForm({
  value,
  locations,
  lang,
  onChange,
  onSave,
  saving,
  t,
}: {
  value: AdminPlateRegions;
  locations: AdminLocation[];
  lang: Lang;
  onChange: (next: AdminPlateRegions) => void;
  onSave: () => void;
  saving: boolean;
  t: (key: string) => string;
}) {
  const areaI = value.areas?.AREA_I?.amount ?? 0;
  const areaII = value.areas?.AREA_II?.amount ?? 0;

  function setAmount(area: string, amount: number) {
    onChange({
      ...value,
      areas: {
        ...value.areas,
        [area]: { amount },
      },
    });
  }

  function setUnit(region: string, index: number, unit: { code?: string; name: string; area: string }) {
    const units = [...(value.regions[region] ?? [])];
    units[index] = unit;
    onChange({ ...value, regions: { ...value.regions, [region]: units } });
  }

  return (
    <section className="mt-4 space-y-4">
      <div className="rounded-2xl border border-ink/8 bg-white p-4 shadow-card">
        <div className="grid gap-3 md:grid-cols-10">
          <div className="md:col-span-6">
            <NumberField label={t("admin.field.areaIAmount")} value={areaI} onChange={(next) => setAmount("AREA_I", next)} />
          </div>
          <div className="md:col-span-4">
            <NumberField label={t("admin.field.areaIIAmount")} value={areaII} onChange={(next) => setAmount("AREA_II", next)} />
          </div>
        </div>
      </div>
      {["NORTH", "CENTRAL", "SOUTH"].map((region) => (
        <div key={region} className="rounded-2xl border border-ink/8 bg-white p-4 shadow-card">
          <p className="text-sm font-semibold">{t(`admin.opt.${region}`)}</p>
          <div className="mt-3 space-y-2">
            {(value.regions[region] ?? []).map((unit, index) => (
              <div key={`${region}-${index}`} className="grid gap-2 md:grid-cols-[6fr_4fr_auto]">
                {locations.find((item) => item.code === unit.code) ? (
                  <p className="flex h-10 items-center rounded-lg border border-ink/10 bg-paper px-3 text-sm">
                    {plateUnitName(unit, locations, lang)}
                  </p>
                ) : (
                  <input
                    value={unit.name}
                    onChange={(event) => setUnit(region, index, { ...unit, name: event.target.value })}
                    className="h-10 rounded-lg border border-ink/10 bg-paper px-3 text-sm"
                  />
                )}
                <select
                  value={unit.area}
                  onChange={(event) => setUnit(region, index, { ...unit, area: event.target.value })}
                  className="h-10 rounded-lg border border-ink/10 bg-paper px-3 text-sm"
                >
                  <option value="AREA_I">{t("admin.opt.AREA_I")}</option>
                  <option value="AREA_II">{t("admin.opt.AREA_II")}</option>
                </select>
                <button
                  type="button"
                  onClick={() =>
                    onChange({
                      ...value,
                      regions: { ...value.regions, [region]: (value.regions[region] ?? []).filter((_, itemIndex) => itemIndex !== index) },
                    })
                  }
                  className="text-sm text-red-700"
                >
                  {t("admin.remove")}
                </button>
              </div>
            ))}
          </div>
          <button
            type="button"
            className="mt-3 text-sm font-semibold text-copper"
            onClick={() =>
              onChange({
                ...value,
                regions: {
                  ...value.regions,
                  [region]: [...(value.regions[region] ?? []), { name: "", area: "AREA_II" }],
                },
              })
            }
          >
            {t("admin.addProvince")}
          </button>
        </div>
      ))}
      <div className="pt-1">
        <SaveButton saving={saving} t={t} onClick={onSave} />
      </div>
    </section>
  );
}

function NumberField({ label, value, onChange }: { label: string; value: number; onChange: (value: number) => void }) {
  return (
    <label className="block text-xs font-medium text-ink/70">
      {label}
      <input
        type="number"
        value={Number.isFinite(value) ? value : 0}
        onChange={(event) => onChange(Number(event.target.value))}
        className="mt-1 h-10 w-full rounded-lg border border-ink/10 bg-paper px-3 text-sm"
      />
    </label>
  );
}

function SaveButton({ saving, t, onClick }: { saving: boolean; t: (key: string) => string; onClick: () => void }) {
  return (
    <button type="button" disabled={saving} onClick={onClick} className="h-10 rounded-lg bg-ink px-4 text-sm font-semibold text-paper disabled:opacity-60">
      {saving ? t("admin.saving") : t("admin.save")}
    </button>
  );
}

function prepareDraft(row: Record<string, unknown>): Record<string, unknown> {
  const photos = { ...((row.colorPhotos as Record<string, string>) ?? {}) };
  const listed = String(row.availableColors ?? "")
    .split(",")
    .map((item) => item.trim())
    .filter(Boolean);
  for (const name of listed) {
    if (!photos[name]) {
      photos[name] = colorPhoto(name);
    }
  }
  if (row.defaultColor && !photos[String(row.defaultColor)]) {
    photos[String(row.defaultColor)] = colorPhoto(String(row.defaultColor));
  }
  return { ...row, colorPhotos: photos };
}

function isCatalog(tab: Tab): tab is CatalogTab {
  return !["feePolicy", "dealerPolicy", "plateRegions"].includes(tab);
}

function localizedName(
  tab: Tab,
  row: Record<string, unknown>,
  lang: Lang,
  t: (key: string) => string
) {
  if (tab === "locations") {
    return locationName(
      {
        name: String(row.name ?? ""),
        nameEn: String(row.nameEn ?? ""),
        nameZh: String(row.nameZh ?? ""),
        nameJa: String(row.nameJa ?? ""),
      } as AdminLocation,
      lang
    );
  }
  const code = String(row.code ?? "");
  if (tab === "categories" && code) {
    const key = `category.${code}`;
    return t(key) === key ? String(row.name ?? code) : t(key);
  }
  if (tab === "feeDefinitions" && code) {
    const key = `fee.${code}`;
    return t(key) === key ? String(row.name ?? code) : t(key);
  }
  return String(row.name ?? "—");
}

function plateUnitName(
  unit: { code?: string; name: string },
  locations: AdminLocation[],
  lang: Lang
) {
  const location = locations.find((item) => item.code === unit.code);
  return location ? locationName(location, lang) : unit.name;
}

function locationName(location: AdminLocation | undefined, lang: Lang) {
  if (!location) {
    return "";
  }
  return locationLabel(location, lang);
}

function listFor(tab: CatalogTab) {
  switch (tab) {
    case "brands":
      return api.listAdminBrands();
    case "categories":
      return api.listAdminCategories();
    case "locations":
      return api.listAdminLocations();
    case "dealers":
      return api.listAdminDealers();
    case "vehicles":
      return api.listAdminVehicles();
    case "feeDefinitions":
      return api.listAdminFeeDefinitions();
    case "feeRules":
      return api.listAdminFeeRules();
  }
}

function saveFor(tab: CatalogTab, item: Record<string, unknown>) {
  switch (tab) {
    case "brands":
      return api.saveAdminBrand(item as unknown as AdminBrand);
    case "categories":
      return api.saveAdminCategory(item as unknown as AdminCategory);
    case "locations":
      return api.saveAdminLocation(item as unknown as AdminLocation);
    case "dealers":
      return api.saveAdminDealer(item as unknown as AdminDealer);
    case "vehicles":
      return api.saveAdminVehicle(item as unknown as AdminVehicle);
    case "feeDefinitions":
      return api.saveAdminFeeDefinition(item as unknown as AdminFeeDefinition);
    case "feeRules":
      return api.saveAdminFeeRule(item as unknown as AdminFeeRule);
  }
}

function deleteFor(tab: CatalogTab, id: number) {
  switch (tab) {
    case "brands":
      return api.deleteAdminBrand(id);
    case "categories":
      return api.deleteAdminCategory(id);
    case "locations":
      return api.deleteAdminLocation(id);
    case "dealers":
      return api.deleteAdminDealer(id);
    case "vehicles":
      return api.deleteAdminVehicle(id);
    case "feeDefinitions":
      return api.deleteAdminFeeDefinition(id);
    case "feeRules":
      return api.deleteAdminFeeRule(id);
  }
}
