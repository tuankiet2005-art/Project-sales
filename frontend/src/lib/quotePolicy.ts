import type { Lang } from "../i18n/translations";
import type { UsageType } from "../types";

export interface QuotePolicyChoices {
  usageType: UsageType;
  selectedOfferIds: string[];
  forgoneOfferIds: string[];
}

export function policyStorageKey(vehicleId: number): string {
  return `onroad-policy-${vehicleId}`;
}

export function defaultPolicyChoices(): QuotePolicyChoices {
  return { usageType: "PRIVATE", selectedOfferIds: [], forgoneOfferIds: [] };
}

export function savePolicyChoices(vehicleId: number, choices: QuotePolicyChoices) {
  sessionStorage.setItem(policyStorageKey(vehicleId), JSON.stringify(choices));
}

export function loadPolicyChoices(vehicleId: number, fallback: QuotePolicyChoices): QuotePolicyChoices {
  const raw = sessionStorage.getItem(policyStorageKey(vehicleId));
  if (!raw) {
    return fallback;
  }
  try {
    const parsed = JSON.parse(raw) as QuotePolicyChoices;
    return {
      usageType: parsed.usageType === "COMMERCIAL" ? "COMMERCIAL" : "PRIVATE",
      selectedOfferIds: Array.isArray(parsed.selectedOfferIds) ? parsed.selectedOfferIds : [],
      forgoneOfferIds: Array.isArray(parsed.forgoneOfferIds) ? parsed.forgoneOfferIds : [],
    };
  } catch {
    return fallback;
  }
}

export function localizedPolicyText(map: Record<string, string> | undefined, lang: Lang): string {
  if (!map) {
    return "";
  }
  return map[lang] || map.vi || map.en || Object.values(map)[0] || "";
}
