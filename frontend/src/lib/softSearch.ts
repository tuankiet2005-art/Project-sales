export function foldVietnamese(value: string) {
  return value
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/đ/gi, "d")
    .toLowerCase();
}

export function softIncludes(query: string, ...values: unknown[]) {
  const needle = foldVietnamese(query.trim());
  if (!needle) {
    return true;
  }
  return values.some((value) => value != null && foldVietnamese(String(value)).includes(needle));
}
