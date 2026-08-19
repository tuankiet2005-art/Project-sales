import type { Lang } from "../i18n/translations";

export function locationLabel(
  location: { name: string; nameEn?: string; nameZh?: string; nameJa?: string },
  lang: Lang
): string {
  const named =
    lang === "en" ? location.nameEn : lang === "zh" ? location.nameZh : lang === "ja" ? location.nameJa : location.name;
  return named || location.name || location.nameEn || "";
}

export function codedOption(value: string | undefined | null, t: (key: string) => string): string {
  if (!value) {
    return "—";
  }
  const key = `admin.opt.${value}`;
  const translated = t(key);
  return translated === key ? value : translated;
}
