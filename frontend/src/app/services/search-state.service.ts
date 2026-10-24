import { Injectable, signal } from '@angular/core';
import { VehicleSearchCriteria, createEmptyVehicleSearch } from '../models/vehicle.model';
import { RoomSearchCriteria, createEmptyRoomSearch } from '../models/room.model';
import { TransportSearchCriteria, createEmptyTransportSearch } from '../models/transport.model';
import { EmergencySearchCriteria, createEmptyEmergencySearch } from '../models/emergency.model';
import { RequestCategory } from '../models/request.model';

@Injectable({ providedIn: 'root' })
export class SearchStateService {
  readonly vehicleSearch = signal<VehicleSearchCriteria>(createEmptyVehicleSearch());
  readonly roomSearch = signal<RoomSearchCriteria>(createEmptyRoomSearch());
  readonly transportSearch = signal<TransportSearchCriteria>(createEmptyTransportSearch());
  readonly emergencySearch = signal<EmergencySearchCriteria>(createEmptyEmergencySearch());

  readonly selectedCategory = signal<RequestCategory>('vehicle');
  readonly selectedResourceId = signal<number>(0);
  readonly selectedResourceName = signal<string>('');
  readonly selectedProviderName = signal<string>('');

  setVehicleSearch(criteria: VehicleSearchCriteria): void {
    this.vehicleSearch.set(criteria);
  }

  setRoomSearch(criteria: RoomSearchCriteria): void {
    this.roomSearch.set(criteria);
  }

  setTransportSearch(criteria: TransportSearchCriteria): void {
    this.transportSearch.set(criteria);
  }

  setEmergencySearch(criteria: EmergencySearchCriteria): void {
    this.emergencySearch.set(criteria);
  }

  selectResource(category: RequestCategory, resourceId: number, resourceName: string, providerName: string): void {
    this.selectedCategory.set(category);
    this.selectedResourceId.set(resourceId);
    this.selectedResourceName.set(resourceName);
    this.selectedProviderName.set(providerName);
  }

  resetVehicleSearch(): void { this.vehicleSearch.set(createEmptyVehicleSearch()); }
  resetRoomSearch(): void { this.roomSearch.set(createEmptyRoomSearch()); }
  resetTransportSearch(): void { this.transportSearch.set(createEmptyTransportSearch()); }
  resetEmergencySearch(): void { this.emergencySearch.set(createEmptyEmergencySearch()); }
}
