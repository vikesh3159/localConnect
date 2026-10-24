export type TransportVehicleType = 'small-carrier' | 'pickup-truck' | 'mini-truck' | 'truck' | 'tractor-trolley';
export type GoodsType = 'furniture' | 'crops' | 'construction' | 'luggage' | 'general' | 'other';

export interface TransportSearchCriteria {
  pickup: string;
  destination: string;
  date: string;
  time: string;
  goodsType: GoodsType | '';
  loadDescription: string;
  vehicleType: TransportVehicleType | '';
  vehiclesRequired: number;
}

export interface TransportResult {
  id: number;
  vehicleName: string;
  type: TransportVehicleType;
  loadCapacity: string;
  priceIndicative: number;
  pricingBasis: string;
  providerName: string;
  rating: number;
  reviewCount: number;
  distanceFromPickup: number;
  demoAvailable: boolean;
  description: string;
  imageEmoji: string;
  providerId: number;
}

export const TRANSPORT_VEHICLE_OPTIONS: { value: TransportVehicleType; labelKey: string }[] = [
  { value: 'small-carrier', labelKey: 'TRANSPORT_TYPES.SMALL_CARRIER' },
  { value: 'pickup-truck', labelKey: 'TRANSPORT_TYPES.PICKUP_TRUCK' },
  { value: 'mini-truck', labelKey: 'TRANSPORT_TYPES.MINI_TRUCK' },
  { value: 'truck', labelKey: 'TRANSPORT_TYPES.TRUCK' },
  { value: 'tractor-trolley', labelKey: 'TRANSPORT_TYPES.TRACTOR_TROLLEY' },
];

export const GOODS_TYPE_OPTIONS: { value: GoodsType; labelKey: string }[] = [
  { value: 'furniture', labelKey: 'GOODS_TYPES.FURNITURE' },
  { value: 'crops', labelKey: 'GOODS_TYPES.CROPS' },
  { value: 'construction', labelKey: 'GOODS_TYPES.CONSTRUCTION' },
  { value: 'luggage', labelKey: 'GOODS_TYPES.LUGGAGE' },
  { value: 'general', labelKey: 'GOODS_TYPES.GENERAL' },
  { value: 'other', labelKey: 'GOODS_TYPES.OTHER' },
];

export function createEmptyTransportSearch(): TransportSearchCriteria {
  return { pickup: '', destination: '', date: '', time: '', goodsType: '', loadDescription: '', vehicleType: '', vehiclesRequired: 1 };
}
