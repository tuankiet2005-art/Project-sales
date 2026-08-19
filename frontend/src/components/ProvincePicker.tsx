import { Search } from "lucide-react";
import { useMemo, useState } from "react";
import { useI18n } from "../i18n/LanguageContext";
import { locationLabel } from "../lib/labels";
import { softIncludes } from "../lib/softSearch";
import type { Location } from "../types";

export function ProvincePicker({
  locations,
  value,
  onChange,
}: {
  locations: Location[];
  value?: number;
  onChange: (id: number) => void;
}) {
  const { t, lang } = useI18n();
  const [query, setQuery] = useState("");
  const [open, setOpen] = useState(false);

  const selected = locations.find((item) => item.id === value);
  const selectedLabel = selected ? locationLabel(selected, lang) : "";
  const matches = useMemo(
    () =>
      locations.filter((item) =>
        softIncludes(query, locationLabel(item, lang), item.name, item.nameEn, item.nameZh, item.nameJa, item.code)
      ),
    [locations, query, lang]
  );

  return (
    <div className="relative">
      <Search className="pointer-events-none absolute left-3 top-3.5 h-4 w-4 text-ink/40" />
      <input
        value={open ? query : selectedLabel}
        placeholder={t("provinceSearch")}
        onFocus={() => {
          setQuery("");
          setOpen(true);
        }}
        onChange={(event) => {
          setQuery(event.target.value);
          setOpen(true);
        }}
        onBlur={() => {
          window.setTimeout(() => setOpen(false), 120);
        }}
        className="mt-1 h-12 w-full rounded-xl border border-ink/10 bg-paper pl-10 pr-3 text-ink"
      />
      {open && (
        <ul className="absolute z-20 mt-1 max-h-56 w-full overflow-y-auto rounded-xl border border-ink/10 bg-white py-1 shadow-card">
          {matches.length === 0 ? (
            <li className="px-3 py-2 text-sm text-ink/50">{t("emptySearch")}</li>
          ) : (
            matches.map((item) => {
              const label = locationLabel(item, lang);
              const active = item.id === value;
              return (
                <li key={item.id}>
                  <button
                    type="button"
                    onMouseDown={(event) => event.preventDefault()}
                    onClick={() => {
                      onChange(item.id);
                      setQuery("");
                      setOpen(false);
                    }}
                    className={`block w-full px-3 py-2 text-left text-sm ${
                      active ? "bg-mist font-semibold text-ink" : "text-ink/80 hover:bg-paper"
                    }`}
                  >
                    {label}
                  </button>
                </li>
              );
            })
          )}
        </ul>
      )}
    </div>
  );
}
