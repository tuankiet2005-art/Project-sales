import { Link, NavLink, useParams } from "react-router-dom";
import { useI18n } from "../i18n/LanguageContext";
import { LanguageSwitcher } from "./LanguageSwitcher";

export function Header({ brandName }: { brandName?: string }) {
  const { t } = useI18n();
  const { brandCode } = useParams();

  return (
    <header className="sticky top-0 z-20 border-b border-ink/10 bg-paper/85 backdrop-blur">
      <div className="mx-auto flex max-w-page items-center justify-between gap-4 px-5 py-4">
        <Link to={brandCode ? `/brand/${brandCode}` : "/"} className="flex items-baseline gap-2">
          <span className="font-display text-2xl font-semibold tracking-tight text-ink">{t("appName")}</span>
          <span className="hidden text-sm text-ink/55 sm:inline">
            {brandName ?? t("appTag")}
          </span>
        </Link>
        <div className="flex items-center gap-4 text-sm font-medium">
          {brandCode && (
            <>
              <NavLink
                to={`/brand/${brandCode}`}
                className={({ isActive }) => (isActive ? "text-copper" : "text-ink/70 hover:text-ink")}
              >
                {t("browse")}
              </NavLink>
              <Link to="/" className="text-ink/70 hover:text-ink">
                {t("changeBrand")}
              </Link>
            </>
          )}
          <LanguageSwitcher />
        </div>
      </div>
    </header>
  );
}
