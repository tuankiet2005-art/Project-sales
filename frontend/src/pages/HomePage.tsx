import { Search } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { useParams, useSearchParams } from "react-router-dom";
import { api } from "../api/client";
import { Header } from "../components/Header";
import { VehicleCard } from "../components/VehicleCard";
import { useI18n } from "../i18n/LanguageContext";
import { softIncludes } from "../lib/softSearch";
import type { Brand, Category, VehicleSummary } from "../types";

export function HomePage() {
  const { brandCode = "" } = useParams();
  const { t } = useI18n();
  const [searchParams, setSearchParams] = useSearchParams();
  const [keyword, setKeyword] = useState(searchParams.get("q") ?? "");
  const [categoryId, setCategoryId] = useState<number | undefined>(
    searchParams.get("category") ? Number(searchParams.get("category")) : undefined
  );
  const [brand, setBrand] = useState<Brand | null>(null);
  const [categories, setCategories] = useState<Category[]>([]);
  const [vehicles, setVehicles] = useState<VehicleSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api.getCategories().then(setCategories).catch(() => undefined);
    api.getBrand(brandCode).then(setBrand).catch(() => setBrand(null));
  }, [brandCode]);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);
    api
      .searchVehicles("", brandCode)
      .then((data) => {
        if (!cancelled) {
          setVehicles(data);
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
  }, [brandCode]);

  const selectedCategory = useMemo(
    () => categories.find((item) => item.id === categoryId),
    [categories, categoryId]
  );

  const visibleVehicles = useMemo(
    () =>
      vehicles.filter((vehicle) => {
        const inCategory = !categoryId || vehicle.category.id === categoryId;
        return (
          inCategory &&
          softIncludes(keyword, vehicle.name, vehicle.model, vehicle.brand, vehicle.vehicleType, vehicle.year)
        );
      }),
    [vehicles, categoryId, keyword]
  );

  function selectCategory(id?: number) {
    setCategoryId(id);
    const next = new URLSearchParams(searchParams);
    if (id) {
      next.set("category", String(id));
    } else {
      next.delete("category");
    }
    setSearchParams(next);
  }

  return (
    <div className="min-h-screen">
      <Header />
      <main>
        <section className="mx-auto max-w-page px-5 pb-8 pt-12">
          <p className="text-sm font-semibold uppercase tracking-[0.22em] text-copper">
            {brand?.name ?? t("heroKicker")} · {t("marketVietnam")}
          </p>
          <h1 className="mt-3 max-w-3xl font-display text-5xl leading-[1.05] text-ink sm:text-6xl">
            {t("heroTitle")}
          </h1>

          <label className="relative mt-8 block">
            <Search className="pointer-events-none absolute left-5 top-1/2 h-5 w-5 -translate-y-1/2 text-ink/40" />
            <input
              value={keyword}
              onChange={(event) => setKeyword(event.target.value)}
              placeholder={t("searchPlaceholder")}
              className="h-14 w-full rounded-2xl border border-ink/10 bg-white pl-14 pr-5 text-base outline-none ring-copper/30 focus:ring-4"
            />
          </label>

          <div className="mt-6 flex flex-wrap gap-2">
            <button
              type="button"
              onClick={() => selectCategory(undefined)}
              className={`rounded-full px-4 py-2 text-sm ${
                !categoryId ? "bg-ink text-paper" : "bg-white text-ink/70 ring-1 ring-ink/10"
              }`}
            >
              {t("allTypes")}
            </button>
            {categories.map((category) => (
              <button
                key={category.id}
                type="button"
                onClick={() => selectCategory(category.id)}
                className={`rounded-full px-4 py-2 text-sm ${
                  categoryId === category.id
                    ? "bg-ink text-paper"
                    : "bg-white text-ink/70 ring-1 ring-ink/10"
                }`}
              >
                {t(`category.${category.code}`)}
              </button>
            ))}
          </div>
        </section>

        <section className="mx-auto max-w-page px-5 pb-16">
          <div className="mb-6">
            <h2 className="font-display text-3xl text-ink">
              {selectedCategory ? t(`category.${selectedCategory.code}`) : t("availableVehicles")}
            </h2>
            <p className="mt-1 text-sm text-ink/55">
              {loading ? t("loadingCatalog") : t("modelsCount", { n: visibleVehicles.length })}
            </p>
          </div>

          {error && (
            <div className="rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">
              <p>{t("apiError")}</p>
              <p className="mt-1 text-xs text-red-700/80">{error}</p>
            </div>
          )}

          {!loading && !error && visibleVehicles.length === 0 && (
            <p className="rounded-2xl bg-white px-5 py-10 text-center text-ink/60">
              {brand && !brand.ready ? t("emptyBrand") : t("emptySearch")}
            </p>
          )}

          <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
            {visibleVehicles.map((vehicle) => (
              <VehicleCard key={vehicle.id} vehicle={vehicle} brandCode={brandCode} />
            ))}
          </div>
        </section>

        <section id="how-it-works" className="border-t border-ink/10 bg-white/70">
          <div className="mx-auto grid max-w-page gap-6 px-5 py-14 md:grid-cols-4">
            {[
              ["01", "step1Title"],
              ["02", "step2Title"],
              ["03", "step3Title"],
              ["04", "step4Title"],
            ].map(([step, title]) => (
              <div key={step}>
                <p className="text-xs font-semibold tracking-[0.2em] text-copper">{step}</p>
                <h3 className="mt-2 font-display text-2xl">{t(title)}</h3>
              </div>
            ))}
          </div>
        </section>
      </main>
    </div>
  );
}
