export type VehicleType = 'hatchback' | 'sedan' | 'suv' | 'muv' | 'tempo-traveller';

export interface VehicleSearchCriteria {
  pickup: string;
  destination: string;
  date: string;
  time: string;
  vehiclesRequired: number;
  vehicleType: VehicleType | '';
}

export interface VehicleResult {
  id: number;
  name: string;
  type: VehicleType;
  seatingCapacity: number;
  pricePerTrip: number;
  providerName: string;
  providerRating: number;
  reviewCount: number;
  distanceFromPickup: number;
  demoAvailable: boolean;
  description: string;
  features: string[];
  imageEmoji: string;
  providerId: number;
}

export const VEHICLE_TYPE_OPTIONS: { value: VehicleType; labelKey: string }[] = [
  { value: 'hatchback', labelKey: 'VEHICLE_TYPES.HATCHBACK' },
  { value: 'sedan', labelKey: 'VEHICLE_TYPES.SEDAN' },
  { value: 'suv', labelKey: 'VEHICLE_TYPES.SUV' },
  { value: 'muv', labelKey: 'VEHICLE_TYPES.MUV' },
  { value: 'tempo-traveller', labelKey: 'VEHICLE_TYPES.TEMPO_TRAVELLER' },
];

export function createEmptyVehicleSearch(): VehicleSearchCriteria {
  return { pickup: '', destination: '', date: '', time: '', vehiclesRequired: 1, vehicleType: '' };
}
