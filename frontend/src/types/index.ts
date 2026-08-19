export interface Brand {
  id: number;
  code: string;
  name: string;
  tagline: string;
  market: string;
  accentColor: string;
  imageUrl: string;
  ready: boolean;
}

export interface Category {
  id: number;
  code: string;
  name: string;
  description: string;
  typicalSeats: number | null;
  requiresInspection: boolean;
  requiresRoadUseFee: boolean;
  requiresCompulsoryInsurance: boolean;
}

export interface Location {
  id: number;
  code: string;
  name: string;
  nameEn: string;
  nameZh: string;
  nameJa: string;
  region: string;
  feeZone: string;
  centrallyGovernedCity: boolean;
}

export interface VehicleSummary {
  id: number;
  brand: string;
  brandCode: string;
  model: string;
  name: string;
  year: number;
  seats: number | null;
  vehicleType: string;
  listPrice: number;
  discountAmount?: number;
  salePrice?: number;
  imageUrl: string;
  category: Category;
}

export interface VehicleDetail extends VehicleSummary {
  engineCc: number | null;
  fuelType: string;
  transmission: string;
  defaultDeposit?: number;
  registrationServiceFee?: number;
  micaPlateFee?: number;
  inspectionFee?: number;
  defaultColor?: string;
  availableColors?: string;
  deliveryNote?: string;
  warrantyNote?: string;
  gifts?: string;
  specifications: Record<string, string>;
}

export interface FeeLine {
  code: string;
  name: string;
  description: string;
  mandatory: boolean;
  applicable: boolean;
  includedInTotal: boolean;
  amount: number;
  calculationNote: string;
}

export interface AccessoryItem {
  name: string;
  amount: number;
  catalogId?: string;
  imageUrl?: string;
}

export interface QuoteExtras {
  discountAmount?: number;
  deposit?: number;
  optionalBodyInsurance?: number;
  registrationServiceFee?: number;
  micaPlateFee?: number;
  inspectionFee?: number;
  accessories: AccessoryItem[];
}

export interface CostBreakdown {
  vehicleId: number;
  vehicleName: string;
  brand: string;
  model: string;
  categoryName: string;
  locationId: number;
  locationName: string;
  listPrice: number;
  discountAmount?: number;
  salePrice?: number;
  fees: FeeLine[];
  totalMandatoryFees: number;
  totalOptionalFees: number;
  accessoriesTotal?: number;
  estimatedOnRoadTotal: number;
  deposit?: number;
  accessories?: AccessoryItem[];
  currency: string;
}
