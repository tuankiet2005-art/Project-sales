import { Route, Routes } from "react-router-dom";
import { BrandPortal } from "./pages/BrandPortal";
import { HomePage } from "./pages/HomePage";
import { OnRoadQuotePage } from "./pages/OnRoadQuotePage";
import { VehiclePage } from "./pages/VehiclePage";

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<BrandPortal />} />
      <Route path="/brand/:brandCode" element={<HomePage />} />
      <Route path="/brand/:brandCode/vehicles/:vehicleId" element={<VehiclePage />} />
      <Route path="/brand/:brandCode/vehicles/:vehicleId/on-road" element={<OnRoadQuotePage />} />
    </Routes>
  );
}
