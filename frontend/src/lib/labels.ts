import type { Lang } from "../i18n/translations";
import type { Location } from "../types";

export function locationLabel(location: Location, lang: Lang): string {
  switch (lang) {
    case "en":
      return location.nameEn;
    case "zh":
      return location.nameZh;
    case "ja":
      return location.nameJa;
    default:
      return location.name;
  }
}
