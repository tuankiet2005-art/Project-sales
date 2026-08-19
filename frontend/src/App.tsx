import { Route, Routes } from "react-router-dom";
import { useAdminAuth } from "./auth/AdminAuthContext";
import { LoginScreen } from "./components/LoginScreen";
import { AdminDataPage } from "./pages/AdminDataPage";
import { BrandPortal } from "./pages/BrandPortal";
import { HomePage } from "./pages/HomePage";
import { OnRoadQuotePage } from "./pages/OnRoadQuotePage";
import { QuoteHistoryPage } from "./pages/QuoteHistoryPage";
import { VehiclePage } from "./pages/VehiclePage";

export default function App() {
  const { signedIn } = useAdminAuth();
  if (!signedIn) {
    return <LoginScreen />;
  }

  return (
    <Routes>
      <Route path="/" element={<BrandPortal />} />
      <Route path="/admin" element={<AdminDataPage />} />
      <Route path="/quotes" element={<QuoteHistoryPage />} />
      <Route path="/brand/:brandCode" element={<HomePage />} />
      <Route path="/brand/:brandCode/vehicles/:vehicleId" element={<VehiclePage />} />
      <Route path="/brand/:brandCode/vehicles/:vehicleId/on-road" element={<OnRoadQuotePage />} />
    </Routes>
  );
}
