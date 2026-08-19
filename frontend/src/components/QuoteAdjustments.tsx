import { Check, CircleDollarSign, Package, Plus, Trash2 } from "lucide-react";
import type { ReactNode } from "react";
import { MOVEO_ACCESSORIES } from "../lib/accessories";
import { useI18n } from "../i18n/LanguageContext";
import { formatVnd } from "../lib/format";
import type { QuoteExtras } from "../types";

export function QuoteAdjustments({
  extras,
  onChange,
}: {
  extras: QuoteExtras;
  onChange: (next: QuoteExtras) => void;
}) {
  return (
    <div className="grid gap-3 lg:grid-cols-2">
      <QuotePricePanel extras={extras} onChange={onChange} />
      <QuoteAccessoriesPanel extras={extras} onChange={onChange} />
    </div>
  );
}

export function QuotePricePanel({
  extras,
  onChange,
  action,
}: {
  extras: QuoteExtras;
  onChange: (next: QuoteExtras) => void;
  action?: ReactNode;
}) {
  const { t } = useI18n();

  function setField(field: keyof Omit<QuoteExtras, "accessories">, value: string) {
    onChange({
      ...extras,
      [field]: value === "" ? undefined : Number(value),
    });
  }

  return (
    <section className="rounded-2xl border border-ink/8 bg-white p-4 shadow-card">
      <div className="flex items-start gap-2">
        <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-mist text-copper">
          <CircleDollarSign className="h-4 w-4" />
        </span>
        <div>
          <p className="text-sm font-semibold leading-5">{t("adjustablePrices")}</p>
        </div>
      </div>

      <div className="mt-4 grid grid-cols-1 gap-3">
        <MoneyField label={t("discount")} value={extras.discountAmount} onChange={(value) => setField("discountAmount", value)} />
        <MoneyField label={t("deposit")} value={extras.deposit} onChange={(value) => setField("deposit", value)} />
        <MoneyField
          label={t("registrationServiceFee")}
          value={extras.registrationServiceFee}
          onChange={(value) => setField("registrationServiceFee", value)}
        />
        <MoneyField label={t("micaPlateFee")} value={extras.micaPlateFee} onChange={(value) => setField("micaPlateFee", value)} />
        <MoneyField label={t("inspectionFee")} value={extras.inspectionFee} onChange={(value) => setField("inspectionFee", value)} />
        <MoneyField
          label={t("optionalInsuranceAmount")}
          value={extras.optionalBodyInsurance}
          onChange={(value) => setField("optionalBodyInsurance", value)}
        />
      </div>
      {action}
    </section>
  );
}

export function QuoteAccessoriesPanel({
  extras,
  onChange,
}: {
  extras: QuoteExtras;
  onChange: (next: QuoteExtras) => void;
}) {
  const { t } = useI18n();

  function isSelected(catalogId: string) {
    return extras.accessories.some((item) => item.catalogId === catalogId);
  }

  function toggleCatalogItem(catalogId: string) {
    const catalog = MOVEO_ACCESSORIES.find((item) => item.id === catalogId);
    if (!catalog) {
      return;
    }
    if (isSelected(catalogId)) {
      onChange({
        ...extras,
        accessories: extras.accessories.filter((item) => item.catalogId !== catalogId),
      });
      return;
    }
    onChange({
      ...extras,
      accessories: [
        ...extras.accessories,
        {
          name: t(catalog.nameKey),
          amount: catalog.amount,
          catalogId: catalog.id,
          imageUrl: catalog.imageUrl,
        },
      ],
    });
  }

  return (
    <section className="rounded-2xl border border-ink/8 bg-white p-4 shadow-card">
      <div className="flex items-start gap-2">
        <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-mist text-copper">
          <Package className="h-4 w-4" />
        </span>
        <div>
          <p className="text-sm font-semibold leading-5">{t("accessoriesTitle")}</p>
        </div>
      </div>

      <div className="mt-3 grid grid-cols-3 gap-2">
        {MOVEO_ACCESSORIES.map((item) => {
          const selected = isSelected(item.id);
          return (
            <button
              key={item.id}
              type="button"
              onClick={() => toggleCatalogItem(item.id)}
              className={`overflow-hidden rounded-xl border text-left ${
                selected ? "border-copper ring-1 ring-copper/30" : "border-ink/10 bg-white"
              }`}
            >
              <div className="relative">
                <img src={item.imageUrl} alt={t(item.nameKey)} className="aspect-[16/10] w-full object-cover" />
                {selected && (
                  <span className="absolute right-1 top-1 flex h-4 w-4 items-center justify-center rounded-full bg-copper text-white">
                    <Check className="h-3 w-3" />
                  </span>
                )}
              </div>
              <div className="px-1.5 py-1.5">
                <p className="line-clamp-2 text-[11px] font-semibold leading-4">{t(item.nameKey)}</p>
                <p className="mt-0.5 text-[11px] text-copper">{formatVnd(item.amount)}</p>
              </div>
            </button>
          );
        })}
      </div>

      {extras.accessories.length > 0 && (
        <div className="mt-4 space-y-2 border-t border-ink/8 pt-3">
          {extras.accessories.map((item, index) => (
            <div key={`${item.catalogId ?? item.name}-${index}`} className="grid grid-cols-[3.5rem_1fr_7.5rem_auto] items-center gap-2">
              {item.imageUrl ? (
                <img src={item.imageUrl} alt="" className="h-12 w-14 rounded-xl object-cover" />
              ) : (
                <div className="h-12 w-14 rounded-xl bg-mist" />
              )}
              <input
                value={item.name}
                onChange={(event) => {
                  const accessories = extras.accessories.map((row, rowIndex) =>
                    rowIndex === index ? { ...row, name: event.target.value } : row
                  );
                  onChange({ ...extras, accessories });
                }}
                placeholder={t("accessoryName")}
                className="h-12 rounded-xl border border-ink/10 bg-paper px-3 text-base"
              />
              <input
                type="number"
                min="0"
                step="1000"
                value={item.amount || ""}
                onChange={(event) => {
                  const accessories = extras.accessories.map((row, rowIndex) =>
                    rowIndex === index ? { ...row, amount: Number(event.target.value) || 0 } : row
                  );
                  onChange({ ...extras, accessories });
                }}
                placeholder={t("amount")}
                className="h-12 rounded-xl border border-ink/10 bg-paper px-3 text-base"
              />
              <button
                type="button"
                onClick={() =>
                  onChange({
                    ...extras,
                    accessories: extras.accessories.filter((_, rowIndex) => rowIndex !== index),
                  })
                }
                className="inline-flex h-12 w-12 items-center justify-center rounded-xl border border-ink/10 text-ink/60 hover:bg-mist hover:text-red-700"
                aria-label={t("removeAccessory")}
              >
                <Trash2 className="h-4 w-4" />
              </button>
            </div>
          ))}
        </div>
      )}

      <button
        type="button"
        onClick={() => onChange({ ...extras, accessories: [...extras.accessories, { name: "", amount: 0 }] })}
        className="mt-3 inline-flex h-8 items-center gap-1.5 rounded-lg border border-dashed border-ink/20 px-2.5 text-xs font-semibold text-ink hover:bg-mist"
      >
        <Plus className="h-3.5 w-3.5" />
        {t("addAccessory")}
      </button>
    </section>
  );
}

function MoneyField({
  label,
  value,
  onChange,
}: {
  label: string;
  value?: number;
  onChange: (value: string) => void;
}) {
  return (
    <label className="block text-sm">
      <span className="font-medium text-ink/80">{label}</span>
      <input
        type="number"
        min="0"
        step="1000"
        value={value ?? ""}
        onChange={(event) => onChange(event.target.value)}
        className="mt-1.5 h-12 w-full rounded-xl border border-ink/10 bg-paper px-3 text-base"
      />
    </label>
  );
}
