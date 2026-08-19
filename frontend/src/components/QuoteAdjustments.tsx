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
  const { t } = useI18n();

  function setField(field: keyof Omit<QuoteExtras, "accessories">, value: string) {
    onChange({
      ...extras,
      [field]: value === "" ? undefined : Number(value),
    });
  }

  function updateAccessory(index: number, field: "name" | "amount", value: string) {
    const accessories = extras.accessories.map((item, itemIndex) => {
      if (itemIndex !== index) {
        return item;
      }
      return field === "name" ? { ...item, name: value } : { ...item, amount: Number(value) || 0 };
    });
    onChange({ ...extras, accessories });
  }

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
    <div className="space-y-4">
      <div>
        <p className="text-sm font-semibold">{t("adjustablePrices")}</p>
        <p className="mt-1 text-xs text-ink/55">{t("adjustablePricesHint")}</p>
      </div>

      <div className="grid gap-3 sm:grid-cols-2">
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

      <div>
        <p className="text-sm font-semibold">{t("accessoriesTitle")}</p>
        <p className="mt-1 text-xs text-ink/55">{t("accessoriesHint")}</p>
        <div className="mt-4 grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
          {MOVEO_ACCESSORIES.map((item) => {
            const selected = isSelected(item.id);
            return (
              <button
                key={item.id}
                type="button"
                onClick={() => toggleCatalogItem(item.id)}
                className={`overflow-hidden rounded-2xl border text-left shadow-card ${
                  selected ? "border-copper ring-2 ring-copper/30" : "border-ink/10 bg-white"
                }`}
              >
                <img src={item.imageUrl} alt={t(item.nameKey)} className="aspect-[16/10] w-full object-cover" />
                <div className="px-3 py-3">
                  <p className="text-sm font-semibold">{t(item.nameKey)}</p>
                  <p className="mt-1 text-sm text-copper">{formatVnd(item.amount)}</p>
                  <p className="mt-1 text-[11px] uppercase tracking-wide text-ink/45">
                    {selected ? t("accessorySelected") : t("accessoryAdd")}
                  </p>
                </div>
              </button>
            );
          })}
        </div>

        <div className="mt-4 space-y-2">
          {extras.accessories.map((item, index) => (
            <div key={`${item.catalogId ?? item.name}-${index}`} className="grid grid-cols-[auto_1fr_8rem_auto] items-center gap-2">
              {item.imageUrl ? (
                <img src={item.imageUrl} alt="" className="h-11 w-14 rounded-lg object-cover" />
              ) : (
                <div className="h-11 w-14 rounded-lg bg-mist" />
              )}
              <input
                value={item.name}
                onChange={(event) => updateAccessory(index, "name", event.target.value)}
                placeholder={t("accessoryName")}
                className="h-11 rounded-xl border border-ink/10 bg-paper px-3 text-sm"
              />
              <input
                type="number"
                min="0"
                step="1000"
                value={item.amount || ""}
                onChange={(event) => updateAccessory(index, "amount", event.target.value)}
                placeholder={t("amount")}
                className="h-11 rounded-xl border border-ink/10 bg-paper px-3 text-sm"
              />
              <button
                type="button"
                onClick={() =>
                  onChange({
                    ...extras,
                    accessories: extras.accessories.filter((_, itemIndex) => itemIndex !== index),
                  })
                }
                className="h-11 rounded-xl border border-ink/10 px-3 text-xs font-semibold text-ink/70 hover:bg-mist"
              >
                {t("removeAccessory")}
              </button>
            </div>
          ))}
        </div>
        <button
          type="button"
          onClick={() => onChange({ ...extras, accessories: [...extras.accessories, { name: "", amount: 0 }] })}
          className="mt-3 h-10 rounded-xl border border-dashed border-ink/20 px-4 text-sm font-semibold text-ink hover:bg-mist"
        >
          + {t("addAccessory")}
        </button>
      </div>
    </div>
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
      <span className="font-medium">{label}</span>
      <input
        type="number"
        min="0"
        step="1000"
        value={value ?? ""}
        onChange={(event) => onChange(event.target.value)}
        className="mt-1 h-11 w-full rounded-xl border border-ink/10 bg-paper px-3"
      />
    </label>
  );
}
