export type EmergencyType =
  | 'urgent-transport'
  | 'medical-transport'
  | 'vehicle-assistance'
  | 'temporary-accommodation'
  | 'other';

export type EmergencyUrgency = 'now' | 'scheduled';

export type EmergencyOperationalStatus =
  | 'helpline-dialed'
  | 'local-assistance-requested'
  | 'provider-acknowledged'
  | 'confirmed-en-route'
  | 'on-scene'
  | 'resolved'
  | 'cancelled';

export interface StatutoryHelpline {
  number: string;
  serviceKey: string;
  descriptionKey: string;
  category: 'all' | 'medical' | 'police' | 'fire' | 'highway' | 'women';
  priority: number;
}

export const STATUTORY_HELPLINES: StatutoryHelpline[] = [
  {
    number: '112',
    serviceKey: 'EMERGENCY.HELPLINE_112_NAME',
    descriptionKey: 'EMERGENCY.HELPLINE_112_DESC',
    category: 'all',
    priority: 1,
  },
  {
    number: '108',
    serviceKey: 'EMERGENCY.HELPLINE_108_NAME',
    descriptionKey: 'EMERGENCY.HELPLINE_108_DESC',
    category: 'medical',
    priority: 2,
  },
  {
    number: '102',
    serviceKey: 'EMERGENCY.HELPLINE_102_NAME',
    descriptionKey: 'EMERGENCY.HELPLINE_102_DESC',
    category: 'medical',
    priority: 3,
  },
  {
    number: '101',
    serviceKey: 'EMERGENCY.HELPLINE_101_NAME',
    descriptionKey: 'EMERGENCY.HELPLINE_101_DESC',
    category: 'fire',
    priority: 4,
  },
  {
    number: '100',
    serviceKey: 'EMERGENCY.HELPLINE_100_NAME',
    descriptionKey: 'EMERGENCY.HELPLINE_100_DESC',
    category: 'police',
    priority: 5,
  },
  {
    number: '1073',
    serviceKey: 'EMERGENCY.HELPLINE_1073_NAME',
    descriptionKey: 'EMERGENCY.HELPLINE_1073_DESC',
    category: 'highway',
    priority: 6,
  },
  {
    number: '1091',
    serviceKey: 'EMERGENCY.HELPLINE_1091_NAME',
    descriptionKey: 'EMERGENCY.HELPLINE_1091_DESC',
    category: 'women',
    priority: 7,
  },
];

export interface EmergencySearchCriteria {
  location: string;
  assistanceType: EmergencyType | '';
  description: string;
  urgency: EmergencyUrgency;
  scheduledTime: string;
  contactPreference: 'call' | 'whatsapp' | 'any';
}

export interface EmergencyResult {
  id: number;
  providerName: string;
  assistanceType: EmergencyType;
  distanceKm: number;
  estimatedResponse: string;
  rating: number;
  reviewCount: number;
  demoAvailable: boolean;
  description: string;
  providerId: number;
}

export interface EmergencyAlertRecord {
  id: string;
  emergencyType: string;
  contactPhone: string;
  callerLocation: string;
  callerLandmark?: string;
  notes?: string;
  dialedHelpline?: string;
  status: EmergencyOperationalStatus;
  providerEtaMinutes?: number;
  providerVehiclePlate?: string;
  createdAt: string;
}

export const EMERGENCY_TYPE_OPTIONS: { value: EmergencyType; labelKey: string }[] = [
  { value: 'urgent-transport', labelKey: 'EMERGENCY_TYPES.URGENT_TRANSPORT' },
  { value: 'medical-transport', labelKey: 'EMERGENCY_TYPES.MEDICAL_TRANSPORT' },
  { value: 'vehicle-assistance', labelKey: 'EMERGENCY_TYPES.VEHICLE_ASSISTANCE' },
  { value: 'temporary-accommodation', labelKey: 'EMERGENCY_TYPES.TEMPORARY_ACCOMMODATION' },
  { value: 'other', labelKey: 'EMERGENCY_TYPES.OTHER' },
];

export function createEmptyEmergencySearch(): EmergencySearchCriteria {
  return {
    location: '',
    assistanceType: '',
    description: '',
    urgency: 'now',
    scheduledTime: '',
    contactPreference: 'any',
  };
}
