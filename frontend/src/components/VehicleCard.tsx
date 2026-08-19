import { Link } from "react-router-dom";
import { useI18n } from "../i18n/LanguageContext";
import { formatVnd } from "../lib/format";
import type { VehicleSummary } from "../types";

export function VehicleCard({ vehicle, brandCode }: { vehicle: VehicleSummary; brandCode: string }) {
  const { t } = useI18n();

  return (
    <Link
      to={`/brand/${brandCode}/vehicles/${vehicle.id}`}
      className="group overflow-hidden rounded-3xl border border-ink/8 bg-white shadow-card transition hover:-translate-y-1 hover:border-copper/40"
    >
      <div className="relative aspect-[16/10] overflow-hidden bg-mist">
        <img
          src={vehicle.imageUrl}
          alt={vehicle.name}
          className="h-full w-full object-cover transition duration-500 group-hover:scale-105"
        />
        <span className="absolute left-3 top-3 rounded-full bg-paper/90 px-3 py-1 text-xs font-semibold text-forest">
          {t(`category.${vehicle.category.code}`)}
        </span>
      </div>
      <div className="space-y-2 p-5">
        <p className="text-xs uppercase tracking-[0.18em] text-ink/45">{vehicle.brand}</p>
        <h3 className="font-display text-xl leading-tight text-ink">{vehicle.name}</h3>
        <div className="flex items-end justify-between pt-2">
          <div>
            <p className="text-xs text-ink/50">{t("salePrice")}</p>
            <p className="text-lg font-semibold text-copper">
              {formatVnd(vehicle.salePrice ?? vehicle.listPrice)}
            </p>
          </div>
          <p className="text-sm text-ink/50">{vehicle.year}</p>
        </div>
      </div>
    </Link>
  );
}
