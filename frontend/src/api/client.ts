import type {
  AdminBrand,
  AdminCategory,
  AdminDealer,
  AdminDealerPolicy,
  AdminFeeDefinition,
  AdminFeePolicy,
  AdminFeeRule,
  AdminLocation,
  AdminPlateRegions,
  AdminVehicle,
  Brand,
  CatalogSnapshot,
  Category,
  CostBreakdown,
  DealerPolicy,
  ImportResult,
  Location,
  QuoteExtras,
  QuoteHistory,
  UsageType,
  VehicleDetail,
  VehicleSummary,
} from "../types";
import { extrasPayload } from "../lib/quoteExtras";
import { clearAdminToken, getAdminToken } from "../lib/adminAuth";

const API_BASE = (import.meta.env.VITE_API_BASE as string | undefined)?.replace(/\/$/, "") ?? "";

export class UnauthorizedError extends Error {
  constructor(message = "Sign in required") {
    super(message);
    this.name = "UnauthorizedError";
  }
}

function apiUrl(path: string): string {
  return `${API_BASE}${path}`;
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...((init?.headers as Record<string, string> | undefined) ?? {}),
  };
  const token = getAdminToken();
  if (token && (path.startsWith("/api/admin") || path.startsWith("/api/auth"))) {
    headers.Authorization = `Bearer ${token}`;
  }
  const response = await fetch(apiUrl(path), {
    ...init,
    headers,
  });
  if (!response.ok) {
    const body = await response.json().catch(() => ({}));
    if (response.status === 401) {
      clearAdminToken();
      throw new UnauthorizedError(body.message ?? "Sign in required");
    }
    throw new Error(body.message ?? `Request failed (${response.status})`);
  }
  if (response.status === 204) {
    return undefined as T;
  }
  return response.json() as Promise<T>;
}

export const api = {
  login(username: string, password: string) {
    return request<{ token: string; username: string }>("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ username, password }),
    });
  },
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
  getDealerPolicy() {
    return request<DealerPolicy>("/api/dealer-policy");
  },
  calculateOnRoadCost(
    vehicleId: number,
    locationId: number,
    includeOptionalInsurance: boolean,
    categoryId?: number,
    extras?: QuoteExtras,
    usageType?: UsageType,
    selectedOfferIds?: string[],
    forgoneOfferIds?: string[]
  ) {
    return request<CostBreakdown>("/api/calculate-on-road-cost", {
      method: "POST",
      body: JSON.stringify({
        vehicleId,
        locationId,
        categoryId,
        includeOptionalInsurance,
        usageType,
        selectedOfferIds,
        forgoneOfferIds,
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
    usageType?: UsageType;
    selectedOfferIds?: string[];
    forgoneOfferIds?: string[];
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
  listQuotes(query?: string) {
    const params = new URLSearchParams();
    if (query?.trim()) {
      params.set("q", query.trim());
    }
    const suffix = params.toString();
    return request<QuoteHistory[]>(`/api/quotes${suffix ? `?${suffix}` : ""}`);
  },
  getQuote(id: number) {
    return request<QuoteHistory>(`/api/quotes/${id}`);
  },
  saveQuote(payload: {
    vehicleId: number;
    locationId: number;
    categoryId?: number;
    includeOptionalInsurance: boolean;
    customerName: string;
    customerAddress?: string;
    color?: string;
    language?: string;
    extras?: QuoteExtras;
    usageType?: UsageType;
    selectedOfferIds?: string[];
    forgoneOfferIds?: string[];
  }) {
    return request<QuoteHistory>("/api/quotes", {
      method: "POST",
      body: JSON.stringify({
        ...payload,
        extras: undefined,
        ...(payload.extras ? extrasPayload(payload.extras) : {}),
      }),
    });
  },
  exportCatalog() {
    return request<CatalogSnapshot>("/api/admin/catalog");
  },
  importCatalog(snapshot: CatalogSnapshot) {
    return request<ImportResult>("/api/admin/import", {
      method: "POST",
      body: JSON.stringify(snapshot),
    });
  },
  listAdminBrands() {
    return request<AdminBrand[]>("/api/admin/brands");
  },
  saveAdminBrand(item: AdminBrand) {
    return item.id
      ? request<AdminBrand>(`/api/admin/brands/${item.id}`, { method: "PUT", body: JSON.stringify(item) })
      : request<AdminBrand>("/api/admin/brands", { method: "POST", body: JSON.stringify(item) });
  },
  deleteAdminBrand(id: number) {
    return request<void>(`/api/admin/brands/${id}`, { method: "DELETE" });
  },
  listAdminCategories() {
    return request<AdminCategory[]>("/api/admin/categories");
  },
  saveAdminCategory(item: AdminCategory) {
    return item.id
      ? request<AdminCategory>(`/api/admin/categories/${item.id}`, { method: "PUT", body: JSON.stringify(item) })
      : request<AdminCategory>("/api/admin/categories", { method: "POST", body: JSON.stringify(item) });
  },
  deleteAdminCategory(id: number) {
    return request<void>(`/api/admin/categories/${id}`, { method: "DELETE" });
  },
  listAdminLocations() {
    return request<AdminLocation[]>("/api/admin/locations");
  },
  saveAdminLocation(item: AdminLocation) {
    return item.id
      ? request<AdminLocation>(`/api/admin/locations/${item.id}`, { method: "PUT", body: JSON.stringify(item) })
      : request<AdminLocation>("/api/admin/locations", { method: "POST", body: JSON.stringify(item) });
  },
  deleteAdminLocation(id: number) {
    return request<void>(`/api/admin/locations/${id}`, { method: "DELETE" });
  },
  listAdminDealers() {
    return request<AdminDealer[]>("/api/admin/dealers");
  },
  saveAdminDealer(item: AdminDealer) {
    return item.id
      ? request<AdminDealer>(`/api/admin/dealers/${item.id}`, { method: "PUT", body: JSON.stringify(item) })
      : request<AdminDealer>("/api/admin/dealers", { method: "POST", body: JSON.stringify(item) });
  },
  deleteAdminDealer(id: number) {
    return request<void>(`/api/admin/dealers/${id}`, { method: "DELETE" });
  },
  listAdminFeeDefinitions() {
    return request<AdminFeeDefinition[]>("/api/admin/fee-definitions");
  },
  saveAdminFeeDefinition(item: AdminFeeDefinition) {
    return item.id
      ? request<AdminFeeDefinition>(`/api/admin/fee-definitions/${item.id}`, { method: "PUT", body: JSON.stringify(item) })
      : request<AdminFeeDefinition>("/api/admin/fee-definitions", { method: "POST", body: JSON.stringify(item) });
  },
  deleteAdminFeeDefinition(id: number) {
    return request<void>(`/api/admin/fee-definitions/${id}`, { method: "DELETE" });
  },
  listAdminVehicles() {
    return request<AdminVehicle[]>("/api/admin/vehicles");
  },
  saveAdminVehicle(item: AdminVehicle) {
    return item.id
      ? request<AdminVehicle>(`/api/admin/vehicles/${item.id}`, { method: "PUT", body: JSON.stringify(item) })
      : request<AdminVehicle>("/api/admin/vehicles", { method: "POST", body: JSON.stringify(item) });
  },
  deleteAdminVehicle(id: number) {
    return request<void>(`/api/admin/vehicles/${id}`, { method: "DELETE" });
  },
  listAdminFeeRules() {
    return request<AdminFeeRule[]>("/api/admin/fee-rules");
  },
  saveAdminFeeRule(item: AdminFeeRule) {
    return item.id
      ? request<AdminFeeRule>(`/api/admin/fee-rules/${item.id}`, { method: "PUT", body: JSON.stringify(item) })
      : request<AdminFeeRule>("/api/admin/fee-rules", { method: "POST", body: JSON.stringify(item) });
  },
  deleteAdminFeeRule(id: number) {
    return request<void>(`/api/admin/fee-rules/${id}`, { method: "DELETE" });
  },
  translateFromVietnamese(text: string) {
    return request<{ vi: string; en: string; zh: string; ja: string }>("/api/admin/translate", {
      method: "POST",
      body: JSON.stringify({ text }),
    });
  },
  getAdminFeePolicy() {
    return request<AdminFeePolicy>("/api/admin/fee-policy");
  },
  saveAdminFeePolicy(item: AdminFeePolicy) {
    return request<AdminFeePolicy>("/api/admin/fee-policy", { method: "PUT", body: JSON.stringify(item) });
  },
  getAdminDealerPolicy() {
    return request<AdminDealerPolicy>("/api/admin/dealer-policy");
  },
  saveAdminDealerPolicy(item: AdminDealerPolicy) {
    return request<AdminDealerPolicy>("/api/admin/dealer-policy", { method: "PUT", body: JSON.stringify(item) });
  },
  getAdminPlateRegions() {
    return request<AdminPlateRegions>("/api/admin/license-plate-regions");
  },
  saveAdminPlateRegions(item: AdminPlateRegions) {
    return request<AdminPlateRegions>("/api/admin/license-plate-regions", { method: "PUT", body: JSON.stringify(item) });
  },
};
