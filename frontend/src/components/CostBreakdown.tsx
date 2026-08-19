import { useI18n } from "../i18n/LanguageContext";
import { formatVnd } from "../lib/format";
import type { CostBreakdown as CostBreakdownType } from "../types";

export function CostBreakdown({
  result,
  locationLabel,
  categoryLabel,
}: {
  result: CostBreakdownType;
  locationLabel: string;
  categoryLabel: string;
}) {
  const { t } = useI18n();

  return (
    <section className="rounded-3xl border border-ink/8 bg-white p-6 shadow-card">
      <div className="mb-5 flex flex-wrap items-end justify-between gap-3">
        <div>
          <p className="text-xs uppercase tracking-[0.18em] text-ink/45">{t("onRoadEstimate")}</p>
          <h2 className="font-display text-2xl text-ink">{locationLabel}</h2>
        </div>
        <p className="text-sm text-ink/55">{categoryLabel}</p>
      </div>

      <div className="overflow-hidden rounded-2xl border border-ink/8">
        <table className="w-full text-sm">
          <thead className="bg-mist/70 text-left">
            <tr>
              <th className="px-4 py-3 font-medium">{t("cost")}</th>
              <th className="px-4 py-3 text-right font-medium">{t("amount")}</th>
            </tr>
          </thead>
          <tbody>
            <tr className="border-t border-ink/8">
              <td className="px-4 py-3">
                <p className="font-medium">{t("listPrice")}</p>
                <p className="text-xs text-ink/50">{t("advertisedPrice")}</p>
              </td>
              <td className="px-4 py-3 text-right font-semibold">{formatVnd(result.listPrice)}</td>
            </tr>
            {result.discountAmount != null && Number(result.discountAmount) > 0 && (
              <tr className="border-t border-ink/8">
                <td className="px-4 py-3 font-medium">{t("discount")}</td>
                <td className="px-4 py-3 text-right">-{formatVnd(result.discountAmount)}</td>
              </tr>
            )}
            {result.salePrice != null && (
              <tr className="border-t border-ink/8">
                <td className="px-4 py-3 font-medium">{t("salePrice")}</td>
                <td className="px-4 py-3 text-right font-semibold">{formatVnd(result.salePrice)}</td>
              </tr>
            )}
            {result.fees.map((fee) => (
              <tr key={fee.code} className="border-t border-ink/8">
                <td className="px-4 py-3">
                  <div className="flex flex-wrap items-center gap-2">
                    <p className="font-medium">{t(`fee.${fee.code}`)}</p>
                    <span
                      className={`rounded-full px-2 py-0.5 text-[11px] font-semibold ${
                        fee.mandatory ? "bg-forest/10 text-forest" : "bg-copper/10 text-copper"
                      }`}
                    >
                      {fee.mandatory ? t("mandatory") : t("optional")}
                    </span>
                    {!fee.includedInTotal && (
                      <span className="text-[11px] text-ink/45">{t("notIncluded")}</span>
                    )}
                  </div>
                  <p className="text-xs text-ink/50">{fee.calculationNote}</p>
                </td>
                <td className={`px-4 py-3 text-right ${fee.includedInTotal ? "text-ink" : "text-ink/40 line-through"}`}>
                  {formatVnd(fee.amount)}
                </td>
              </tr>
            ))}
            <tr className="border-t border-ink/8 bg-mist/40">
              <td className="px-4 py-3 text-ink/70">{t("totalMandatory")}</td>
              <td className="px-4 py-3 text-right">{formatVnd(result.totalMandatoryFees)}</td>
            </tr>
            <tr className="border-t border-ink/8 bg-mist/40">
              <td className="px-4 py-3 text-ink/70">{t("totalOptional")}</td>
              <td className="px-4 py-3 text-right">{formatVnd(result.totalOptionalFees)}</td>
            </tr>
            <tr className="border-t border-ink/15 bg-forest text-paper">
              <td className="px-4 py-4 font-display text-lg">{t("estimatedTotal")}</td>
              <td className="px-4 py-4 text-right font-display text-xl">
                {formatVnd(result.estimatedOnRoadTotal)}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <p className="mt-4 text-xs leading-relaxed text-ink/50">{t("estimateNote")}</p>
    </section>
  );
}
