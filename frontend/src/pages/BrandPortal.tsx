import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api/client";
import { Header } from "../components/Header";
import { useI18n } from "../i18n/LanguageContext";
import type { Brand } from "../types";

export function BrandPortal() {
  const { t } = useI18n();
  const [brands, setBrands] = useState<Brand[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api.getBrands().then(setBrands).catch((err: Error) => setError(err.message));
  }, []);

  return (
    <div className="min-h-screen">
      <Header />
      <main className="mx-auto max-w-page px-5 py-14">
        <p className="text-sm font-semibold uppercase tracking-[0.22em] text-copper">{t("marketVietnam")}</p>
        <h1 className="mt-3 font-display text-5xl text-ink">{t("chooseBrand")}</h1>

        {error && (
          <div className="mt-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">
            <p>{t("apiError")}</p>
            <p className="mt-1 text-xs text-red-700/80">{error}</p>
          </div>
        )}

        <div className="mt-10 grid gap-5 sm:grid-cols-2">
          {brands.map((brand) => {
            const card = (
              <>
                <div className="relative aspect-[16/8] bg-mist">
                  <img src={brand.imageUrl} alt={brand.name} className="h-full w-full object-cover" />
                  <div className="absolute inset-0 bg-gradient-to-t from-ink/70 to-transparent" />
                  <div className="absolute bottom-4 left-5">
                    <p className="text-xs uppercase tracking-[0.18em] text-white/70">
                      {brand.market === "Vietnam" ? t("marketVietnam") : brand.market}
                    </p>
                    <h2 className="font-display text-3xl text-white">{brand.name}</h2>
                  </div>
                </div>
                <div className="flex items-end justify-end gap-4 p-5">
                  {brand.ready ? (
                    <span
                      className="shrink-0 rounded-full px-5 py-2 text-sm font-semibold text-white"
                      style={{ backgroundColor: brand.accentColor }}
                    >
                      {t("enterDashboard")}
                    </span>
                  ) : (
                    <span className="shrink-0 rounded-full bg-mist px-5 py-2 text-sm font-semibold text-ink/50">
                      {t("comingSoon")}
                    </span>
                  )}
                </div>
              </>
            );
            const boxClass = "block overflow-hidden rounded-3xl border border-ink/8 bg-white shadow-card";
            return brand.ready ? (
              <Link key={brand.id} to={`/brand/${brand.code}`} className={`${boxClass} transition hover:-translate-y-1 hover:border-copper/40`}>
                {card}
              </Link>
            ) : (
              <article key={brand.id} className={boxClass}>
                {card}
              </article>
            );
          })}
        </div>
      </main>
    </div>
  );
}
