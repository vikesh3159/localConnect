import { Component, computed, inject } from '@angular/core';
import { Router } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { SearchStateService } from '../../services/search-state.service';
import { RequestStateService } from '../../services/request-state.service';
import { ApiService } from '../../services/api.service';
import { RequestCategory } from '../../models/types';

@Component({
  selector: 'app-request-summary',
  templateUrl: './request-summary.html',
  styleUrl: './request-summary.scss',
  imports: [TranslatePipe]
})
export class RequestSummaryComponent {
  private searchState = inject(SearchStateService);
  private requestState = inject(RequestStateService);
  private api = inject(ApiService);
  private router = inject(Router);

  category = this.searchState.selectedCategory;
  resourceName = this.searchState.selectedResourceName;
  providerName = this.searchState.selectedProviderName;

  summaryItems = computed(() => {
    const cat = this.category();
    if (!cat) return [];

    switch (cat) {
      case 'vehicle': {
        const s = this.searchState.vehicleSearch();
        return [
          { label: 'REQUEST_SUMMARY.PICKUP', value: s.pickup },
          { label: 'REQUEST_SUMMARY.DESTINATION', value: s.destination },
          { label: 'REQUEST_SUMMARY.DATE', value: s.date },
          { label: 'REQUEST_SUMMARY.TIME', value: s.time },
          { label: 'REQUEST_SUMMARY.VEHICLES_REQUIRED', value: String(s.vehiclesRequired) },
          { label: 'REQUEST_SUMMARY.VEHICLE_TYPE', value: s.vehicleType || '-' },
          { label: 'REQUEST_SUMMARY.WITH_DRIVER', value: 'Yes (With Driver)' },
        ];
      }
      case 'room': {
        const s = this.searchState.roomSearch();
        return [
          { label: 'REQUEST_SUMMARY.LOCATION', value: s.location },
          { label: 'REQUEST_SUMMARY.CHECK_IN', value: s.checkIn },
          { label: 'REQUEST_SUMMARY.CHECK_OUT', value: s.checkOut },
          { label: 'REQUEST_SUMMARY.GUESTS', value: String(s.guests) },
          { label: 'REQUEST_SUMMARY.ROOMS', value: String(s.rooms) },
          { label: 'REQUEST_SUMMARY.ROOM_TYPE', value: s.roomType || '-' },
        ];
      }
      case 'transport': {
        const s = this.searchState.transportSearch();
        return [
          { label: 'REQUEST_SUMMARY.PICKUP', value: s.pickup },
          { label: 'REQUEST_SUMMARY.DESTINATION', value: s.destination },
          { label: 'REQUEST_SUMMARY.DATE', value: s.date },
          { label: 'REQUEST_SUMMARY.TIME', value: s.time },
          { label: 'REQUEST_SUMMARY.GOODS_TYPE', value: s.goodsType },
          { label: 'REQUEST_SUMMARY.LOAD', value: s.loadDescription },
          { label: 'REQUEST_SUMMARY.VEHICLE_TYPE', value: s.vehicleType || '-' },
          { label: 'REQUEST_SUMMARY.VEHICLES_REQUIRED', value: String(s.vehiclesRequired) },
        ];
      }
      case 'emergency': {
        const s = this.searchState.emergencySearch();
        return [
          { label: 'REQUEST_SUMMARY.LOCATION', value: s.location },
          { label: 'REQUEST_SUMMARY.ASSISTANCE_TYPE', value: s.assistanceType || '-' },
          { label: 'REQUEST_SUMMARY.URGENCY', value: s.urgency },
          { label: 'REQUEST_SUMMARY.DESCRIPTION', value: s.description || '-' },
        ];
      }
      default:
        return [];
    }
  });

  getCategoryIcon(): string {
    const icons: Record<string, string> = {
      vehicle: '🚗', room: '🏠', transport: '🚚', emergency: '🚨',
    };
    return icons[this.category() || ''] || '📋';
  }

  confirmRequest() {
    const cat = this.category();
    if (!cat) return;

    const summaryRecord: Record<string, string> = {};
    for (const item of this.summaryItems()) {
      summaryRecord[item.label] = item.value;
    }

    const request = this.requestState.createRequest(
      cat as RequestCategory,
      summaryRecord,
      this.providerName(),
      this.resourceName()
    );

    // Also attempt backend creation if connected
    try {
      const today = new Date().toISOString().split('T')[0];
      const payload = {
        category: cat.toUpperCase(),
        urgencyLevel: 'NORMAL',
        quantityRequired: 1,
        pickupLocation: summaryRecord['REQUEST_SUMMARY.PICKUP'] || summaryRecord['REQUEST_SUMMARY.LOCATION'] || 'Local Pickup',
        destinationLocation: summaryRecord['REQUEST_SUMMARY.DESTINATION'] || 'Local Destination',
        scheduledDate: today,
        scheduledTime: '10:00:00',
        stayType: summaryRecord['REQUEST_SUMMARY.ROOM_TYPE'],
        goodsType: summaryRecord['REQUEST_SUMMARY.GOODS_TYPE'],
        specialInstructions: `Booking for ${this.resourceName()} via ${this.providerName()}`
      };
      this.api.createRequest(payload).subscribe({
        next: (res) => console.log('Backend request created successfully', res),
        error: (err) => console.warn('Backend request sync deferred, stored locally:', err)
      });
    } catch (e) {
      console.warn('API sync caught exception', e);
    }

    this.router.navigate(['/request-confirmation', request.referenceNumber]);
  }

  goBack() {
    const cat = this.category();
    const routes: Record<string, string> = {
      vehicle: '/vehicle-results',
      room: '/room-results',
      transport: '/transport-results',
      emergency: '/emergency',
    };
    this.router.navigate([routes[cat || ''] || '/']);
  }
}
