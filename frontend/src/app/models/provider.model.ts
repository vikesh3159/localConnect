export type ResourceCategory = 'vehicle' | 'room' | 'transport' | 'emergency';
export type VerificationStatus = 'PENDING_APPROVAL' | 'APPROVED' | 'REJECTED';

export interface Driver {
  id: number;
  providerId: number;
  fullName: string;
  phone: string;
  licenseNumber: string;
  licenseExpiry?: string;
  licenseExpiryDate?: string;
  licenseType?: string;
  experienceYears?: number;
  isVerified?: boolean;
  isActive?: boolean;
  isAvailable?: boolean;
  verificationStatus?: VerificationStatus;
}

export interface Provider {
  id: number;
  name: string;
  rating: number;
  reviewCount: number;
  area: string;
  description: string;
  category: ResourceCategory;
  joinedYear: number;
  responseRate: string;
  verified: boolean;
  verificationStatus?: VerificationStatus;
}

export interface ProviderResource {
  id: number;
  providerId: number;
  category: ResourceCategory;
  name: string;
  type: string;
  description: string;
  pricing: string;
  pricingBasis: string;
  serviceArea: string;
  capacity: string;
  specifications: Record<string, string>;
  isActive: boolean;
  demoAvailable: boolean;
  approvalStatus?: VerificationStatus;
}
