import { languages } from "../i18n/translations";
import { useI18n } from "../i18n/LanguageContext";

export function LanguageSwitcher() {
  const { lang, setLang } = useI18n();

  return (
    <label className="flex items-center gap-2 text-sm">
      <select
        value={lang}
        onChange={(event) => setLang(event.target.value as typeof lang)}
        className="rounded-full border border-ink/15 bg-white px-3 py-1.5 text-sm text-ink"
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
