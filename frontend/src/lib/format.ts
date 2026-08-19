const vnd = new Intl.NumberFormat("vi-VN", {
  style: "currency",
  currency: "VND",
  maximumFractionDigits: 0,
});

export function formatVnd(amount: number | string): string {
  return vnd.format(Number(amount));
}

export function formatQuoteAmount(amount: number | string | null | undefined): string {
  if (amount == null || amount === "") {
    return "";
  }
  const value = Number(amount);
  if (!Number.isFinite(value)) {
    return "";
  }
  return new Intl.NumberFormat("vi-VN", { maximumFractionDigits: 0 }).format(value);
}

export function formatRegion(region: string): string {
  switch (region) {
    case "NORTH":
      return "North";
    case "CENTRAL":
      return "Central";
    case "SOUTH":
      return "South";
    default:
      return region;
  }
}
