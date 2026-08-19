export interface AccessoryCatalogItem {
  id: string;
  nameKey: string;
  amount: number;
  imageUrl: string;
}

export const MOVEO_ACCESSORIES: AccessoryCatalogItem[] = [
  {
    id: "tint",
    nameKey: "accessory.tint",
    amount: 4500000,
    imageUrl: "/accessories/tint.jpg",
  },
  {
    id: "dashcam",
    nameKey: "accessory.dashcam",
    amount: 2490000,
    imageUrl: "/accessories/dashcam.jpg",
  },
  {
    id: "mats",
    nameKey: "accessory.mats",
    amount: 1190000,
    imageUrl: "/accessories/mats.jpg",
  },
  {
    id: "cover",
    nameKey: "accessory.cover",
    amount: 850000,
    imageUrl: "/accessories/cover.jpg",
  },
  {
    id: "undercoat",
    nameKey: "accessory.undercoat",
    amount: 2800000,
    imageUrl: "/accessories/undercoat.jpg",
  },
  {
    id: "steering",
    nameKey: "accessory.steering",
    amount: 450000,
    imageUrl: "/accessories/steering.jpg",
  },
  {
    id: "extinguisher",
    nameKey: "accessory.extinguisher",
    amount: 280000,
    imageUrl: "/accessories/extinguisher.jpg",
  },
  {
    id: "vetc",
    nameKey: "accessory.vetc",
    amount: 720000,
    imageUrl: "/accessories/vetc.jpg",
  },
  {
    id: "camera360",
    nameKey: "accessory.camera360",
    amount: 9500000,
    imageUrl: "/accessories/camera360.jpg",
  },
];

export function accessoryById(id?: string): AccessoryCatalogItem | undefined {
  if (!id) {
    return undefined;
  }
  return MOVEO_ACCESSORIES.find((item) => item.id === id);
}
