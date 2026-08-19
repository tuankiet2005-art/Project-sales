import { Link, NavLink, useParams } from "react-router-dom";
import { useAdminAuth } from "../auth/AdminAuthContext";
import { useI18n } from "../i18n/LanguageContext";
import { LanguageSwitcher } from "./LanguageSwitcher";

export function Header() {
  const { t } = useI18n();
  const { signedIn, signOut } = useAdminAuth();
  const { brandCode } = useParams();
  const catalogCode = brandCode || "MITSUBISHI";

  return (
    <header className="sticky top-0 z-50 border-b border-ink/10 bg-paper/85 backdrop-blur">
      <div className="mx-auto flex max-w-page flex-wrap items-center justify-between gap-x-8 gap-y-3 px-6 py-5">
        {signedIn ? (
          <Link to={`/brand/${catalogCode}`} className="font-display text-3xl font-semibold tracking-tight text-ink">
            {t("appName")}
          </Link>
        ) : (
          <span className="font-display text-3xl font-semibold tracking-tight text-ink">{t("appName")}</span>
        )}
        <nav className="flex flex-wrap items-center gap-x-7 gap-y-2 text-base font-semibold">
          {signedIn && (
            <>
              <NavLink
                to={`/brand/${catalogCode}`}
                className={({ isActive }) => (isActive ? "px-1 py-1 text-copper" : "px-1 py-1 text-ink/70 hover:text-ink")}
              >
                {t("browse")}
              </NavLink>
              <NavLink to="/" className={({ isActive }) => (isActive ? "px-1 py-1 text-copper" : "px-1 py-1 text-ink/70 hover:text-ink")}>
                {t("changeBrand")}
              </NavLink>
              <NavLink to="/quotes" className={({ isActive }) => (isActive ? "px-1 py-1 text-copper" : "px-1 py-1 text-ink/70 hover:text-ink")}>
                {t("quoteHistory.nav")}
              </NavLink>
              <NavLink to="/admin" className={({ isActive }) => (isActive ? "px-1 py-1 text-copper" : "px-1 py-1 text-ink/70 hover:text-ink")}>
                {t("admin.nav")}
              </NavLink>
              <button type="button" onClick={signOut} className="px-1 py-1 text-ink/70 hover:text-ink">
                {t("login.logout")}
              </button>
            </>
          )}
          <LanguageSwitcher />
        </nav>
      </div>
    </header>
  );
}
