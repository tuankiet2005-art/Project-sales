import type { Brand, Category, CostBreakdown, Location, QuoteExtras, VehicleDetail, VehicleSummary } from "../types";
import { extrasPayload } from "../lib/quoteExtras";

const API_BASE = (import.meta.env.VITE_API_BASE as string | undefined)?.replace(/\/$/, "") ?? "";

function apiUrl(path: string): string {
  return `${API_BASE}${path}`;
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(apiUrl(path), {
    headers: { "Content-Type": "application/json", ...(init?.headers ?? {}) },
    ...init,
  });
  if (!response.ok) {
    const body = await response.json().catch(() => ({}));
    throw new Error(body.message ?? `Request failed (${response.status})`);
  }
  return response.json() as Promise<T>;
}

export const api = {
  getBrands() {
    return request<Brand[]>("/api/brands");
  },
  getBrand(code: string) {
    return request<Brand>(`/api/brands/${code}`);
  },
  searchVehicles(keyword?: string, brandCode?: string, categoryId?: number) {
    const params = new URLSearchParams();
    if (keyword?.trim()) {
      params.set("keyword", keyword.trim());
    }
    if (brandCode) {
      params.set("brand", brandCode);
    }
    if (categoryId) {
      params.set("categoryId", String(categoryId));
    }
    const query = params.toString();
    return request<VehicleSummary[]>(`/api/vehicles/search${query ? `?${query}` : ""}`);
  },
  getVehicle(id: number) {
    return request<VehicleDetail>(`/api/vehicles/${id}`);
  },
  getCategories() {
    return request<Category[]>("/api/vehicle-categories");
  },
  getLocations() {
    return request<Location[]>("/api/locations");
  },
  calculateOnRoadCost(
    vehicleId: number,
    locationId: number,
    includeOptionalInsurance: boolean,
    categoryId?: number,
    extras?: QuoteExtras
  ) {
    return request<CostBreakdown>("/api/calculate-on-road-cost", {
      method: "POST",
      body: JSON.stringify({
        vehicleId,
        locationId,
        categoryId,
        includeOptionalInsurance,
        ...(extras ? extrasPayload(extras) : {}),
      }),
    });
  },
  async exportQuote(payload: {
    vehicleId: number;
    locationId: number;
    categoryId?: number;
    includeOptionalInsurance: boolean;
    customerName: string;
    customerAddress?: string;
    color?: string;
    language?: string;
    extras?: QuoteExtras;
  }) {
    const response = await fetch(apiUrl("/api/export-quote"), {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        ...payload,
        extras: undefined,
        ...(payload.extras ? extrasPayload(payload.extras) : {}),
      }),
    });
    if (!response.ok) {
      const body = await response.json().catch(() => ({}));
      throw new Error(body.message ?? `Export failed (${response.status})`);
    }
    return response.blob();
  },
};
