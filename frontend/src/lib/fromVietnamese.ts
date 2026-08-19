import { api } from "../api/client";
import type { Lang } from "../i18n/translations";

export async function fillFromVietnamese(
  vietnamese: string,
  current: Partial<Record<Lang, string>>
): Promise<Record<Lang, string>> {
  const source = vietnamese.trim();
  const next: Record<Lang, string> = {
    vi: source,
    en: current.en ?? "",
    zh: current.zh ?? "",
    ja: current.ja ?? "",
  };
  if (!source) {
    return next;
  }
  const translated = await api.translateFromVietnamese(source);
  for (const lang of ["en", "zh", "ja"] as const) {
    const existing = (current[lang] ?? "").trim();
    if (!existing || existing === source) {
      next[lang] = translated[lang] || source;
    }
  }
  return next;
}
