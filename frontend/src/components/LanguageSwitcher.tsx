import { Languages } from "lucide-react";
import { languages } from "../i18n/translations";
import { useI18n } from "../i18n/LanguageContext";

export function LanguageSwitcher() {
  const { lang, setLang } = useI18n();

  return (
    <label className="flex items-center gap-2.5 text-base">
      <Languages className="h-5 w-5 text-ink/55" />
      <select
        value={lang}
        onChange={(event) => setLang(event.target.value as typeof lang)}
        className="h-10 rounded-full border border-ink/15 bg-white px-4 text-base text-ink"
      >
        {languages.map((item) => (
          <option key={item.code} value={item.code}>
            {item.label}
          </option>
        ))}
      </select>
    </label>
  );
}
