import { Briefcase, Gift, Percent, User } from "lucide-react";
import { FormEvent, useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { api } from "../api/client";
import { Header } from "../components/Header";
import { ProvincePicker } from "../components/ProvincePicker";
import { useI18n } from "../i18n/LanguageContext";
import type { Lang } from "../i18n/translations";
import { formatVnd } from "../lib/format";
import { codedOption } from "../lib/labels";
import { extrasFromVehicle, loadExtras, saveExtras } from "../lib/quoteExtras";
import { colorPhoto } from "../lib/vehicleColor";
import { defaultPolicyChoices, loadPolicyChoices, localizedPolicyText, savePolicyChoices } from "../lib/quotePolicy";
import type { Category, DealerOffer, DealerPolicy, Location, QuoteExtras, UsageType, VehicleDetail } from "../types";

export function VehiclePage() {
  const { vehicleId, brandCode = "" } = useParams();
  const id = Number(vehicleId);
  const navigate = useNavigate();
  const { t, lang } = useI18n();

  const [vehicle, setVehicle] = useState<VehicleDetail | null>(null);
  const [categories, setCategories] = useState<Category[]>([]);
  const [locations, setLocations] = useState<Location[]>([]);
  const [categoryId, setCategoryId] = useState<number | undefined>();
  const [locationId, setLocationId] = useState<number | undefined>();
  const [includeOptional, setIncludeOptional] = useState(false);
  const [customerName, setCustomerName] = useState("");
  const [customerAddress, setCustomerAddress] = useState("");
  const [color, setColor] = useState("");
  const [usageType, setUsageType] = useState<UsageType>("PRIVATE");
  const [policy, setPolicy] = useState<DealerPolicy | null>(null);
  const [selectedOfferIds, setSelectedOfferIds] = useState<string[]>([]);
  const [forgoneOfferIds, setForgoneOfferIds] = useState<string[]>([]);
  const [extras, setExtras] = useState<QuoteExtras>({ accessories: [] });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) {
      return;
    }
    setLoading(true);
    const stored = loadPolicyChoices(id, defaultPolicyChoices());
    setUsageType(stored.usageType);
    setSelectedOfferIds(stored.selectedOfferIds);
    setForgoneOfferIds(stored.forgoneOfferIds);
    Promise.all([api.getVehicle(id), api.getCategories(), api.getLocations(), api.getDealerPolicy()])
      .then(([nextVehicle, nextCategories, nextLocations, nextPolicy]) => {
        setVehicle(nextVehicle);
        setCategories(nextCategories);
        setLocations(nextLocations);
        setPolicy(nextPolicy);
        setCategoryId(nextVehicle.category.id);
        setColor(nextVehicle.defaultColor ?? "");
        setExtras(loadExtras(id, extrasFromVehicle(nextVehicle)));
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
    params.set("usage", usageType === "COMMERCIAL" ? "commercial" : "private");
    saveExtras(id, extras);
    savePolicyChoices(id, { usageType, selectedOfferIds, forgoneOfferIds });
    navigate(`/brand/${brandCode}/vehicles/${id}/on-road?${params.toString()}`);
  }

  if (loading) {
    return (
      <div className="min-h-screen">
        <Header />
        <p className="mx-auto max-w-page px-5 py-16 text-ink/60">{t("loadingVehicle")}</p>
      </div>
    );
  }

  if (!vehicle) {
    return (
      <div className="min-h-screen">
        <Header />
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
      <Header />
      <main className="mx-auto max-w-page px-5 py-10">
        <Link to={`/brand/${brandCode}`} className="text-sm text-ink/55 hover:text-ink">
          ← {t("backCatalog")}
        </Link>

        <form onSubmit={goToQuote}>
        <div className="mt-6 grid gap-8 lg:grid-cols-[1.15fr_0.85fr]">
          <section>
            <div className="overflow-hidden rounded-3xl bg-mist shadow-card">
              <img src={vehicle.imageUrl} alt={vehicle.name} className="aspect-[16/10] w-full object-cover" />
            </div>
            <div className="mt-6">
              <p className="text-xs uppercase tracking-[0.18em] text-ink/45">{vehicle.brand}</p>
              <h1 className="mt-1 font-display text-4xl text-ink">{vehicle.name}</h1>
              <p className="mt-2 text-ink/60">
                {vehicle.model} · {vehicle.year} · {codedOption(vehicle.vehicleType, t)}
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
              <Spec label={t("specEngine")} value={vehicle.engineCc ? `${vehicle.engineCc} cc` : codedOption(vehicle.fuelType, t)} />
              <Spec label={t("specFuel")} value={codedOption(vehicle.fuelType, t)} />
              <Spec label={t("specTransmission")} value={codedOption(vehicle.transmission, t)} />
              <Spec label={t("specYear")} value={String(vehicle.year)} />
              {specEntries.map(([key, value]) => (
                <Spec key={key} label={key} value={value} />
              ))}
            </dl>
          </section>

          <aside className="space-y-5">
            <div className="rounded-3xl border border-ink/8 bg-white p-6 shadow-card">
              <p className="text-xs uppercase tracking-[0.18em] text-copper">{t("calculateTitle")}</p>
              <h2 className="mt-2 font-display text-2xl">{t("confirmDetails")}</h2>

              <p className="mt-5 text-sm font-medium">{t("usageType")}</p>
              <div className="mt-2 grid grid-cols-2 gap-2">
                <button
                  type="button"
                  onClick={() => setUsageType("PRIVATE")}
                  className={`rounded-xl border px-3 py-2 text-left text-sm ${
                    usageType === "PRIVATE" ? "border-ink bg-mist" : "border-ink/10 bg-paper"
                  }`}
                >
                  <User className="mb-1 h-4 w-4 text-copper" />
                  <span className="block font-semibold">{t("usagePrivate")}</span>
                </button>
                <button
                  type="button"
                  onClick={() => setUsageType("COMMERCIAL")}
                  className={`rounded-xl border px-3 py-2 text-left text-sm ${
                    usageType === "COMMERCIAL" ? "border-ink bg-mist" : "border-ink/10 bg-paper"
                  }`}
                >
                  <Briefcase className="mb-1 h-4 w-4 text-copper" />
                  <span className="block font-semibold">{t("usageCommercial")}</span>
                </button>
              </div>

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
              <div className="mt-2 flex items-center gap-3">
                <img
                  src={colorPhoto(color, vehicle.colorPhotos)}
                  alt={color}
                  className="h-14 w-auto rounded-lg border border-ink/10 bg-paper object-contain"
                />
                <select
                  value={color}
                  onChange={(event) => setColor(event.target.value)}
                  className="h-12 flex-1 rounded-xl border border-ink/10 bg-paper px-3 text-ink"
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
              </div>

              <label className="mt-5 block text-sm font-medium">{t("provinceCity")}</label>
              <ProvincePicker locations={locations} value={locationId} onChange={setLocationId} />

              <p className="mt-5 text-sm font-medium">{t("dealerPolicyTitle")}</p>
              {policy && (
                <p className="mt-2 text-xs font-medium text-ink/70">
                  {t("usageDiscount")}:{" "}
                  {usageType === "COMMERCIAL" ? policy.commercialDiscountPercent : policy.privateDiscountPercent}%
                </p>
              )}
              <div className="mt-3 space-y-2">
                {(policy?.offers ?? []).map((offer) => (
                  <OfferCard
                    key={offer.id}
                    offer={offer}
                    selected={selectedOfferIds.includes(offer.id)}
                    forgone={forgoneOfferIds.includes(offer.id)}
                    onTake={() => {
                      setForgoneOfferIds((current) => current.filter((item) => item !== offer.id));
                      setSelectedOfferIds((current) => current.filter((item) => item !== offer.id));
                    }}
                    onForgo={() => {
                      setSelectedOfferIds((current) => current.filter((item) => item !== offer.id));
                      setForgoneOfferIds((current) => [...current.filter((item) => item !== offer.id), offer.id]);
                    }}
                    onToggle={(checked) => {
                      setSelectedOfferIds((current) =>
                        checked
                          ? [...current.filter((item) => item !== offer.id), offer.id]
                          : current.filter((item) => item !== offer.id)
                      );
                    }}
                    t={t}
                    lang={lang}
                  />
                ))}
              </div>

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

              {error && <p className="mt-4 text-sm text-red-700">{error}</p>}
            </div>
          </aside>
        </div>

        <button
          type="submit"
          disabled={!locationId}
          className="mt-5 h-12 w-full rounded-xl bg-ink text-sm font-semibold text-paper hover:bg-forest disabled:opacity-60"
        >
          {t("calculateButton")}
        </button>
        </form>
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

function OfferCard({
  offer,
  selected,
  forgone,
  onTake,
  onForgo,
  onToggle,
  t,
  lang,
}: {
  offer: DealerOffer;
  selected: boolean;
  forgone: boolean;
  onTake: () => void;
  onForgo: () => void;
  onToggle: (checked: boolean) => void;
  t: (key: string) => string;
  lang: Lang;
}) {
  const title = localizedPolicyText(offer.title, lang);
  const description = localizedPolicyText(offer.description, lang);
  if (offer.kind === "FORGO_FOR_CREDIT") {
    return (
      <div className="rounded-xl border border-ink/10 bg-paper px-3 py-2">
        <p className="inline-flex items-center gap-1.5 text-sm font-semibold">
          <Gift className="h-3.5 w-3.5 text-copper" />
          {title}
        </p>
        <p className="mt-1 text-[11px] text-ink/55">{description}</p>
        <div className="mt-2 grid gap-1.5">
          <label className="text-sm">
            <input type="radio" className="mr-2" checked={!forgone} onChange={onTake} />
            {t("offerTakeGift")}
          </label>
          <label className="text-sm">
            <input type="radio" className="mr-2" checked={forgone} onChange={onForgo} />
            {t("offerForgoGift")}
            {offer.amount ? ` · ${formatVnd(offer.amount)}` : ""}
          </label>
        </div>
      </div>
    );
  }
  return (
    <label className="flex items-start gap-2 rounded-xl border border-ink/10 bg-paper px-3 py-2">
      <input type="checkbox" className="mt-1" checked={selected} onChange={(event) => onToggle(event.target.checked)} />
      <span>
        <span className="inline-flex items-center gap-1.5 text-sm font-semibold">
          <Percent className="h-3.5 w-3.5 text-copper" />
          {title}
        </span>
        <span className="mt-1 block text-[11px] text-ink/55">{description}</span>
      </span>
    </label>
  );
}
