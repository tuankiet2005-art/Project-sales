import type { QuoteExtras, VehicleDetail } from "../types";

export function extrasStorageKey(vehicleId: number): string {
  return `onroad-extras-${vehicleId}`;
}

export function extrasFromVehicle(vehicle: VehicleDetail): QuoteExtras {
  return {
    discountAmount: vehicle.discountAmount,
    deposit: vehicle.defaultDeposit,
    registrationServiceFee: vehicle.registrationServiceFee,
    micaPlateFee: vehicle.micaPlateFee,
    inspectionFee: vehicle.inspectionFee,
    accessories: [],
  };
}

export function saveExtras(vehicleId: number, extras: QuoteExtras) {
  sessionStorage.setItem(extrasStorageKey(vehicleId), JSON.stringify(extras));
}

export function loadExtras(vehicleId: number, fallback: QuoteExtras): QuoteExtras {
  const raw = sessionStorage.getItem(extrasStorageKey(vehicleId));
  if (!raw) {
    return fallback;
  }
  try {
    const parsed = JSON.parse(raw) as QuoteExtras;
    return {
      ...fallback,
      ...parsed,
      accessories: Array.isArray(parsed.accessories) ? parsed.accessories : [],
    };
  } catch {
    return fallback;
  }
}

export function extrasPayload(extras: QuoteExtras) {
  return {
    discountAmount: extras.discountAmount,
    deposit: extras.deposit,
    optionalBodyInsurance: extras.optionalBodyInsurance,
    registrationServiceFee: extras.registrationServiceFee,
    micaPlateFee: extras.micaPlateFee,
    inspectionFee: extras.inspectionFee,
    accessories: extras.accessories.filter((item) => item.name.trim() && Number(item.amount) > 0),
  };
}
