const TOKEN_KEY = "onroad-admin-token";
export const ADMIN_AUTH_EVENT = "onroad-admin-auth";

export function getAdminToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function isAdminSignedIn(): boolean {
  return Boolean(getAdminToken());
}

export function setAdminToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token);
  window.dispatchEvent(new Event(ADMIN_AUTH_EVENT));
}

export function clearAdminToken() {
  localStorage.removeItem(TOKEN_KEY);
  window.dispatchEvent(new Event(ADMIN_AUTH_EVENT));
}
