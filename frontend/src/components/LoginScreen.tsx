import { useState, type FormEvent } from "react";
import { api } from "../api/client";
import { useAdminAuth } from "../auth/AdminAuthContext";
import { useI18n } from "../i18n/LanguageContext";
import { Header } from "./Header";

export function LoginScreen() {
  const { t } = useI18n();
  const { signIn } = useAdminAuth();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function submitLogin(event: FormEvent) {
    event.preventDefault();
    setSaving(true);
    setError(null);
    try {
      const session = await api.login(username, password);
      signIn(session.token);
    } catch (err) {
      setError(err instanceof Error ? err.message : t("login.invalid"));
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="flex min-h-screen flex-col">
      <Header />
      <main className="flex flex-1 items-center justify-center px-5 py-10">
        <div className="w-full max-w-md text-center">
          <p className="text-xs uppercase tracking-[0.18em] text-copper">{t("login.kicker")}</p>
          <h1 className="mt-1 font-display text-3xl">{t("login.title")}</h1>
          <form onSubmit={submitLogin} className="mt-8 space-y-4 rounded-2xl bg-white p-6 text-left shadow-card">
            <label className="block text-sm font-medium text-ink">
              {t("login.username")}
              <input
                autoComplete="username"
                value={username}
                onChange={(event) => setUsername(event.target.value)}
                className="mt-1 h-12 w-full rounded-lg border border-ink/10 bg-paper px-3 text-base"
              />
            </label>
            <label className="block text-sm font-medium text-ink">
              {t("login.password")}
              <input
                type="password"
                autoComplete="current-password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                className="mt-1 h-12 w-full rounded-lg border border-ink/10 bg-paper px-3 text-base"
              />
            </label>
            {error && <p className="text-sm text-red-700">{error}</p>}
            <button
              type="submit"
              disabled={saving}
              className="h-12 w-full rounded-full bg-ink text-base font-semibold text-paper disabled:opacity-60"
            >
              {saving ? t("login.signingIn") : t("login.submit")}
            </button>
          </form>
        </div>
      </main>
    </div>
  );
}
