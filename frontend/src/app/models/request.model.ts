export type RequestStatus =
  | 'draft'
  | 'submitted'
  | 'broadcasted'
  | 'offers-received'
  | 'provisionally-holding'
  | 'partially-allocated'
  | 'fully-allocated'
  | 'in-progress'
  | 'completed'
  | 'expired'
  | 'cancelled'
  | 'rejected'
  | 'accepted'
  | 'awaiting-response'; // Retained for backward-compat

export type RequestCategory = 'vehicle' | 'room' | 'transport' | 'emergency';
export type UrgencyLevel = 'normal' | 'urgent' | 'emergency';

export type OfferStatus =
  | 'submitted'
  | 'provisionally-selected'
  | 'nomination-confirmed'
  | 'nomination-failed'
  | 'accepted'
  | 'rejected'
  | 'superseded'
  | 'expired'
  | 'withdrawn';

export type AllocationStatus =
  | 'confirmed'
  | 'in-transit'
  | 'fulfilled'
  | 'cancelled-by-customer'
  | 'cancelled-by-provider';

export interface ProviderOffer {
  id: string;
  requestId: string;
  providerId: string;
  providerBusinessName: string;
  providerRating: number;
  offeredQuantity: number;
  quotedAmount: number;
  includesDriver: boolean;
  status: OfferStatus;
  nominationDeadline?: string;
  expiresAt: string;
  createdAt: string;

  // Masked during quotation; populated only when confirmed
  nominatedResourceId?: string;
  nominatedResourceTitle?: string;
  nominatedVehiclePlate?: string;
  assignedDriverName?: string;
  assignedDriverPhone?: string;
}

export interface BookingAllocation {
  id: string;
  requestId: string;
  requestReferenceNumber: string;
  customerId: string;
  providerId: string;
  providerBusinessName: string;
  resourceTitle: string;
  vehicleRegistrationNumber: string;
  driverName?: string;
  driverPhone?: string;
  allocatedQuantity: number;
  agreedAmount: number;
  confirmationCode: string;
  status: AllocationStatus;
  createdAt: string;
}

export interface ServiceRequest {
  id: string;
  referenceNumber: string;
  customerId?: string;
  customerPhone?: string;
  category: RequestCategory;
  urgencyLevel: UrgencyLevel;
  quantityRequired: number;
  allocatedQuantity: number;
  pickupLocation: string;
  destinationLocation?: string;
  scheduledDate: string;
  scheduledTime: string;
  stayType?: string;
  goodsType?: string;
  specialInstructions?: string;
  status: RequestStatus;
  broadcastWindowHours: number;
  broadcastExpiresAt: string;
  isExpired: boolean;
  searchRadiusKm: number;
  offersCount: number;
  offers: ProviderOffer[];
  createdAt: string;
}

// Backward-compatible interface for legacy views
export interface UserRequest {
  id: string;
  referenceNumber: string;
  category: RequestCategory;
  status: RequestStatus;
  createdAt: string;
  summary: Record<string, string>;
  providerName: string;
  resourceName: string;
  allocatedQuantity?: number;
  quantityRequired?: number;
  broadcastExpiresAt?: string;
  isExpired?: boolean;
  offers?: ProviderOffer[];
}

export interface RequestRemedyPayload {
  action: 'EXTEND_WINDOW' | 'EXPAND_RADIUS' | 'MODIFY_DETAILS' | 'CANCEL_REQUEST';
  additionalHours?: number;
  additionalRadiusKm?: number;
  modifiedQuantity?: number;
  modifiedDate?: string;
  modifiedTime?: string;
  modifiedInstructions?: string;
}

export const REQUEST_STATUS_OPTIONS: { value: RequestStatus; labelKey: string }[] = [
  { value: 'draft', labelKey: 'REQUEST_STATUS.DRAFT' },
  { value: 'submitted', labelKey: 'REQUEST_STATUS.SUBMITTED' },
  { value: 'broadcasted', labelKey: 'REQUEST_STATUS.BROADCASTED' },
  { value: 'offers-received', labelKey: 'REQUEST_STATUS.OFFERS_RECEIVED' },
  { value: 'provisionally-holding', labelKey: 'REQUEST_STATUS.PROVISIONALLY_HOLDING' },
  { value: 'partially-allocated', labelKey: 'REQUEST_STATUS.PARTIALLY_ALLOCATED' },
  { value: 'fully-allocated', labelKey: 'REQUEST_STATUS.FULLY_ALLOCATED' },
  { value: 'in-progress', labelKey: 'REQUEST_STATUS.IN_PROGRESS' },
  { value: 'completed', labelKey: 'REQUEST_STATUS.COMPLETED' },
  { value: 'expired', labelKey: 'REQUEST_STATUS.EXPIRED' },
  { value: 'cancelled', labelKey: 'REQUEST_STATUS.CANCELLED' },
  { value: 'rejected', labelKey: 'REQUEST_STATUS.REJECTED' },
];
