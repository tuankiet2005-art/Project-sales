import { FormEvent, useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { api } from "../api/client";
import { Header } from "../components/Header";
import { QuoteAdjustments } from "../components/QuoteAdjustments";
import { useI18n } from "../i18n/LanguageContext";
import { formatVnd } from "../lib/format";
import { locationLabel } from "../lib/labels";
import { extrasFromVehicle, saveExtras } from "../lib/quoteExtras";
import type { Brand, Category, Location, QuoteExtras, VehicleDetail } from "../types";

export function VehiclePage() {
  const { vehicleId, brandCode = "" } = useParams();
  const id = Number(vehicleId);
  const navigate = useNavigate();
  const { t, lang } = useI18n();

  const [brand, setBrand] = useState<Brand | null>(null);
  const [vehicle, setVehicle] = useState<VehicleDetail | null>(null);
  const [categories, setCategories] = useState<Category[]>([]);
  const [locations, setLocations] = useState<Location[]>([]);
  const [categoryId, setCategoryId] = useState<number | undefined>();
  const [locationId, setLocationId] = useState<number | undefined>();
  const [includeOptional, setIncludeOptional] = useState(false);
  const [customerName, setCustomerName] = useState("");
  const [customerAddress, setCustomerAddress] = useState("");
  const [color, setColor] = useState("");
  const [extras, setExtras] = useState<QuoteExtras>({ accessories: [] });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) {
      return;
    }
    setLoading(true);
    Promise.all([api.getVehicle(id), api.getCategories(), api.getLocations(), api.getBrand(brandCode)])
      .then(([nextVehicle, nextCategories, nextLocations, nextBrand]) => {
        setVehicle(nextVehicle);
        setCategories(nextCategories);
        setLocations(nextLocations);
        setBrand(nextBrand);
        setCategoryId(nextVehicle.category.id);
        setColor(nextVehicle.defaultColor ?? "");
        setExtras(extrasFromVehicle(nextVehicle));
        const hanoi = nextLocations.find((item) => item.code === "HN");
        setLocationId(hanoi?.id ?? nextLocations[0]?.id);
      })
      .catch((err: Error) => setError(err.message))
      .finally(() => setLoading(false));
  }, [id, brandCode]);

  function goToQuote(event: FormEvent) {
    event.preventDefault();
    if (!id || !locationId) {
      return;
    }
    const params = new URLSearchParams();
    params.set("locationId", String(locationId));
    if (categoryId) {
      params.set("categoryId", String(categoryId));
    }
    if (includeOptional) {
      params.set("optional", "1");
    }
    if (customerName.trim()) {
      params.set("name", customerName.trim());
    }
    if (customerAddress.trim()) {
      params.set("address", customerAddress.trim());
    }
    if (color) {
      params.set("color", color);
    }
    saveExtras(id, extras);
    navigate(`/brand/${brandCode}/vehicles/${id}/on-road?${params.toString()}`);
  }

  if (loading) {
    return (
      <div className="min-h-screen">
        <Header brandName={brand?.name} />
        <p className="mx-auto max-w-page px-5 py-16 text-ink/60">{t("loadingVehicle")}</p>
      </div>
    );
  }

  if (!vehicle) {
    return (
      <div className="min-h-screen">
        <Header brandName={brand?.name} />
        <div className="mx-auto max-w-page px-5 py-16">
          <p className="text-ink/70">{error ?? t("vehicleNotFound")}</p>
          <Link to={`/brand/${brandCode}`} className="mt-4 inline-block text-copper">
            {t("backCatalog")}
          </Link>
        </div>
      </div>
    );
  }

  const specEntries = Object.entries(vehicle.specifications ?? {});

  return (
    <div className="min-h-screen">
      <Header brandName={brand?.name} />
      <main className="mx-auto max-w-page px-5 py-10">
        <Link to={`/brand/${brandCode}`} className="text-sm text-ink/55 hover:text-ink">
          ← {t("backCatalog")}
        </Link>

        <div className="mt-6 grid gap-8 lg:grid-cols-[1.15fr_0.85fr]">
          <section>
            <div className="overflow-hidden rounded-3xl bg-mist shadow-card">
              <img src={vehicle.imageUrl} alt={vehicle.name} className="aspect-[16/10] w-full object-cover" />
            </div>
            <div className="mt-6">
              <p className="text-xs uppercase tracking-[0.18em] text-ink/45">{vehicle.brand}</p>
              <h1 className="mt-1 font-display text-4xl text-ink">{vehicle.name}</h1>
              <p className="mt-2 text-ink/60">
                {vehicle.model} · {vehicle.year} · {vehicle.vehicleType}
              </p>
              <div className="mt-5 rounded-2xl bg-white px-5 py-4 shadow-card">
                <p className="text-xs uppercase tracking-[0.16em] text-ink/45">{t("listPrice")}</p>
                <p className="font-display text-3xl text-copper">
                  {formatVnd(vehicle.salePrice ?? vehicle.listPrice)}
                </p>
                <p className="mt-1 text-sm text-ink/50">
                  {t("listPrice")}: {formatVnd(vehicle.listPrice)}
                  {vehicle.discountAmount ? ` · ${t("discount")}: ${formatVnd(vehicle.discountAmount)}` : ""}
                </p>
              </div>
            </div>

            <dl className="mt-6 grid grid-cols-2 gap-3 sm:grid-cols-3">
              <Spec label={t("specCategory")} value={t(`category.${vehicle.category.code}`)} />
              <Spec label={t("specSeats")} value={vehicle.seats ? String(vehicle.seats) : "—"} />
              <Spec label={t("specEngine")} value={vehicle.engineCc ? `${vehicle.engineCc} cc` : vehicle.fuelType} />
              <Spec label={t("specFuel")} value={vehicle.fuelType} />
              <Spec label={t("specTransmission")} value={vehicle.transmission} />
              <Spec label={t("specYear")} value={String(vehicle.year)} />
              {specEntries.map(([key, value]) => (
                <Spec key={key} label={key} value={value} />
              ))}
            </dl>
          </section>

          <aside className="space-y-5">
            <form onSubmit={goToQuote} className="rounded-3xl border border-ink/8 bg-white p-6 shadow-card">
              <p className="text-xs uppercase tracking-[0.18em] text-copper">{t("calculateTitle")}</p>
              <h2 className="mt-2 font-display text-2xl">{t("confirmDetails")}</h2>

              <label className="mt-5 block text-sm font-medium">{t("vehicleCategory")}</label>
              <select
                value={categoryId ?? ""}
                onChange={(event) => setCategoryId(Number(event.target.value))}
                className="mt-1 h-12 w-full rounded-xl border border-ink/10 bg-paper px-3"
              >
                {categories.map((category) => (
                  <option key={category.id} value={category.id}>
                    {t(`category.${category.code}`)}
                  </option>
                ))}
              </select>
              {categoryId !== vehicle.category.id && (
                <p className="mt-2 text-xs text-copper">{t("categoryOverride")}</p>
              )}

              <label className="mt-5 block text-sm font-medium">{t("customerName")}</label>
              <input
                value={customerName}
                onChange={(event) => setCustomerName(event.target.value)}
                className="mt-1 h-12 w-full rounded-xl border border-ink/10 bg-paper px-3"
              />

              <label className="mt-5 block text-sm font-medium">{t("customerAddress")}</label>
              <input
                value={customerAddress}
                onChange={(event) => setCustomerAddress(event.target.value)}
                className="mt-1 h-12 w-full rounded-xl border border-ink/10 bg-paper px-3"
              />

              <label className="mt-5 block text-sm font-medium">{t("vehicleColor")}</label>
              <select
                value={color}
                onChange={(event) => setColor(event.target.value)}
                className="mt-1 h-12 w-full rounded-xl border border-ink/10 bg-paper px-3"
              >
                {(vehicle.availableColors ?? vehicle.defaultColor ?? "Trắng")
                  .split(",")
                  .map((item) => item.trim())
                  .filter(Boolean)
                  .map((item) => (
                    <option key={item} value={item}>
                      {item}
                    </option>
                  ))}
              </select>

              <label className="mt-5 block text-sm font-medium">{t("provinceCity")}</label>
              <select
                value={locationId ?? ""}
                onChange={(event) => setLocationId(Number(event.target.value))}
                className="mt-1 h-12 w-full rounded-xl border border-ink/10 bg-paper px-3"
              >
                {locations.map((location) => (
                  <option key={location.id} value={location.id}>
                    {locationLabel(location, lang)}
                  </option>
                ))}
              </select>

              <label className="mt-5 flex items-start gap-3 rounded-2xl bg-paper px-4 py-3 text-sm">
                <input
                  type="checkbox"
                  checked={includeOptional}
                  onChange={(event) => setIncludeOptional(event.target.checked)}
                  className="mt-1"
                />
                <span>
                  <span className="font-medium">{t("optionalInsurance")}</span>
                  <span className="mt-1 block text-xs text-ink/55">{t("optionalInsuranceHint")}</span>
                </span>
              </label>

              <div className="mt-6 border-t border-ink/8 pt-5">
                <QuoteAdjustments extras={extras} onChange={setExtras} />
              </div>

              {error && <p className="mt-4 text-sm text-red-700">{error}</p>}

              <button
                type="submit"
                disabled={!locationId}
                className="mt-5 h-12 w-full rounded-xl bg-ink text-sm font-semibold text-paper hover:bg-forest disabled:opacity-60"
              >
                {t("calculateButton")}
              </button>
            </form>
          </aside>
        </div>
      </main>
    </div>
  );
}

function Spec({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-2xl bg-white px-4 py-3 shadow-card">
      <dt className="text-xs uppercase tracking-[0.14em] text-ink/45">{label}</dt>
      <dd className="mt-1 text-sm font-medium">{value}</dd>
    </div>
  );
}
