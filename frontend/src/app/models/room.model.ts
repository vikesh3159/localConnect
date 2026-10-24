export type RoomType = 'private' | 'family' | 'guest-house' | 'lodge' | 'shared';

export interface RoomSearchCriteria {
  location: string;
  checkIn: string;
  checkOut: string;
  guests: number;
  rooms: number;
  roomType: RoomType | '';
}

export interface RoomResult {
  id: number;
  propertyName: string;
  type: RoomType;
  location: string;
  pricePerNight: number;
  guestCapacity: number;
  amenities: string[];
  providerName: string;
  rating: number;
  reviewCount: number;
  demoAvailable: boolean;
  description: string;
  imageEmoji: string;
  providerId: number;
}

export const ROOM_TYPE_OPTIONS: { value: RoomType; labelKey: string }[] = [
  { value: 'private', labelKey: 'ROOM_TYPES.PRIVATE' },
  { value: 'family', labelKey: 'ROOM_TYPES.FAMILY' },
  { value: 'guest-house', labelKey: 'ROOM_TYPES.GUEST_HOUSE' },
  { value: 'lodge', labelKey: 'ROOM_TYPES.LODGE' },
  { value: 'shared', labelKey: 'ROOM_TYPES.SHARED' },
];

export function createEmptyRoomSearch(): RoomSearchCriteria {
  return { location: '', checkIn: '', checkOut: '', guests: 1, rooms: 1, roomType: '' };
}
