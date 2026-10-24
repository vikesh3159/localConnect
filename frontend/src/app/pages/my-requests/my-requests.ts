import { Component, signal, computed, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { RequestStateService } from '../../services/request-state.service';
import { ApiService } from '../../services/api.service';
import { BroadcastPolicyService } from '../../services/broadcast-policy.service';
import { StatusBadgeComponent } from '../../shared/status-badge/status-badge';
import {
  RequestStatus,
  UserRequest,
  ProviderOffer,
  RequestRemedyPayload,
} from '../../models/request.model';

@Component({
  selector: 'app-my-requests',
  templateUrl: './my-requests.html',
  styleUrl: './my-requests.scss',
  imports: [RouterLink, TranslatePipe, StatusBadgeComponent],
})
export class MyRequestsComponent implements OnInit {
  private readonly requestState = inject(RequestStateService);
  private readonly api = inject(ApiService);
  readonly broadcastPolicy = inject(BroadcastPolicyService);

  readonly filterStatus = signal<RequestStatus | ''>('');
  readonly localRequests = this.requestState.requests;
  readonly activeTab = signal<'all' | 'active' | 'confirmed' | 'expired'>('all');

  // Active requests list containing requests with their received offers
  readonly requests = signal<UserRequest[]>([]);

  readonly selectedRemedyRequestId = signal<string | null>(null);
  readonly holdCountdownMinutes = signal<number>(14);

  ngOnInit(): void {
    this.loadRequests();
  }

  loadRequests(): void {
    this.api.getMyRequests().subscribe({
      next: (backendRequests) => {
        if (backendRequests && backendRequests.length > 0) {
          const mapped: UserRequest[] = backendRequests.map((b) => ({
            id: String(b.id),
            referenceNumber: b.referenceNumber,
            category: b.category,
            status: b.status,
            createdAt: b.createdAt,
            summary: {
              Pickup: b.pickupLocation,
              Destination: b.destinationLocation || 'Local Service Area',
              Schedule: `${b.scheduledDate} at ${b.scheduledTime}`,
              Quantity: `${b.quantityRequired} requested`,
            },
            providerName: b.offers && b.offers.length > 0 ? b.offers[0].providerBusinessName : 'Local Broadcast',
            resourceName: `${b.category.toUpperCase()} Request`,
            broadcastExpiresAt: b.broadcastExpiresAt,
            isExpired: b.isExpired,
            offers: b.offers || [],
          }));
          this.requests.set(mapped);
        } else {
          this.initializeDemoRequests();
        }
      },
      error: () => {
        this.initializeDemoRequests();
      },
    });
  }

  private initializeDemoRequests(): void {
    const stored = this.localRequests();
    if (stored.length > 0) {
      // Enrich with realistic demo quotes and broadcast windows
      const enriched: UserRequest[] = stored.map((r, idx) => {
        const now = new Date();
        const exp = new Date(now.getTime() + (idx === 1 ? -10 : 3.5) * 3600000).toISOString();
        const demoOffers: ProviderOffer[] = idx === 0
          ? [
              {
                id: 'OFFER-101',
                requestId: r.id,
                providerId: '101',
                providerBusinessName: 'Rajesh Kumar Fleet Services',
                providerRating: 4.8,
                offeredQuantity: 1,
                quotedAmount: 2200,
                includesDriver: true,
                status: 'submitted',
                expiresAt: exp,
                createdAt: new Date().toISOString(),
              },
            ]
          : [];

        return {
          ...r,
          broadcastExpiresAt: exp,
          isExpired: idx === 1,
          status: idx === 1 ? 'expired' : r.status,
          offers: demoOffers,
        };
      });
      this.requests.set(enriched);
    } else {
      // Seed initial demo requests if completely empty
      const sampleReq: UserRequest = {
        id: 'REQ-DEMO-1',
        referenceNumber: 'LC-VEH-8902',
        category: 'vehicle',
        status: 'offers-received',
        createdAt: new Date().toISOString(),
        summary: {
          Pickup: 'Station Road, Khandwa',
          Destination: 'Omkareshwar Temple',
          Date: 'Tomorrow at 07:00 AM',
          Vehicle: 'Mahindra Bolero Camper (AC)',
          Quantity: '1 Vehicle (Driver Included)',
        },
        providerName: 'Rajesh Kumar Fleet Services',
        resourceName: 'Mahindra Bolero Camper (7 Seater)',
        broadcastExpiresAt: new Date(Date.now() + 3.5 * 3600000).toISOString(),
        isExpired: false,
        offers: [
          {
            id: 'OFFER-101',
            requestId: 'REQ-DEMO-1',
            providerId: '101',
            providerBusinessName: 'Rajesh Kumar Fleet Services',
            providerRating: 4.8,
            offeredQuantity: 1,
            quotedAmount: 2200,
            includesDriver: true,
            status: 'submitted',
            expiresAt: new Date(Date.now() + 3.5 * 3600000).toISOString(),
            createdAt: new Date().toISOString(),
          },
        ],
      };

      const expiredReq: UserRequest = {
        id: 'REQ-DEMO-2',
        referenceNumber: 'LC-TRP-4412',
        category: 'transport',
        status: 'expired',
        createdAt: new Date(Date.now() - 5 * 3600000).toISOString(),
        summary: {
          Pickup: 'Mandi Gate 2, Burhanpur',
          Destination: 'Indore Wholesale Market',
          Schedule: 'Today, 06:00 AM',
          Cargo: '25 Quintals Banana Cargo',
        },
        providerName: 'Local Transporters Association',
        resourceName: 'Tata 407 Mini Truck',
        broadcastExpiresAt: new Date(Date.now() - 3600000).toISOString(),
        isExpired: true,
        offers: [],
      };

      this.requests.set([sampleReq, expiredReq]);
    }
  }

  readonly filteredRequests = computed(() => {
    const tab = this.activeTab();
    const all = this.requests();
    if (tab === 'all') return all;
    if (tab === 'active') return all.filter((r) => r.status !== 'completed' && r.status !== 'cancelled' && r.status !== 'expired');
    if (tab === 'confirmed') return all.filter((r) => r.status === 'completed' || r.status === 'partially-allocated' || r.status === 'fully-allocated');
    if (tab === 'expired') return all.filter((r) => r.status === 'expired' || r.isExpired);
    return all;
  });

  // Stage 2: Customer Selects Offer Provisionally -> Starts 15-min Hold
  selectOffer(reqId: string, offer: ProviderOffer): void {
    this.api.selectOfferProvisionally(offer.id).subscribe({
      next: (updatedOffer) => {
        this.updateOfferState(reqId, offer.id, 'provisionally-selected');
      },
      error: () => {
        // Local demo update
        this.updateOfferState(reqId, offer.id, 'provisionally-selected');
      },
    });
  }

  // Simulate Provider Nominating Vehicle & Driver
  simulateProviderNomination(reqId: string, offerId: string): void {
    this.requests.update((list) =>
      list.map((r) => {
        if (r.id === reqId && r.offers) {
          const updatedOffers = r.offers.map((o) => {
            if (o.id === offerId) {
              return {
                ...o,
                nominatedResourceTitle: 'Mahindra Bolero Camper (Commercial)',
                nominatedVehiclePlate: 'MP-12-CD-5678',
                assignedDriverName: 'Suresh Sharma',
                assignedDriverPhone: '+91 98260 12345',
              };
            }
            return o;
          });
          return { ...r, offers: updatedOffers };
        }
        return r;
      })
    );
  }

  // Stage 3: Confirm Booking with ₹0 Advance
  confirmBooking(reqId: string, offer: ProviderOffer): void {
    this.api.confirmAllocation(offer.id).subscribe({
      next: () => {
        this.completeConfirmation(reqId, offer.id);
      },
      error: () => {
        this.completeConfirmation(reqId, offer.id);
      },
    });
  }

  private completeConfirmation(reqId: string, offerId: string): void {
    this.requests.update((list) =>
      list.map((r) => {
        if (r.id === reqId) {
          const updatedOffers = (r.offers || []).map((o) =>
            o.id === offerId ? { ...o, status: 'nomination-confirmed' as const } : o
          );
          return {
            ...r,
            status: 'fully-allocated' as RequestStatus,
            offers: updatedOffers,
          };
        }
        return r;
      })
    );
  }

  private updateOfferState(reqId: string, offerId: string, status: any): void {
    this.requests.update((list) =>
      list.map((r) => {
        if (r.id === reqId) {
          const updatedOffers = (r.offers || []).map((o) =>
            o.id === offerId ? { ...o, status } : o
          );
          return {
            ...r,
            status: 'provisionally-holding' as RequestStatus,
            offers: updatedOffers,
          };
        }
        return r;
      })
    );
  }

  // 4-Action Remediation for Expired Requests
  openRemedyModal(reqId: string): void {
    this.selectedRemedyRequestId.set(reqId);
  }

  closeRemedyModal(): void {
    this.selectedRemedyRequestId.set(null);
  }

  applyRemedy(action: 'EXTEND_WINDOW' | 'EXPAND_RADIUS' | 'MODIFY_DETAILS' | 'CANCEL_REQUEST'): void {
    const reqId = this.selectedRemedyRequestId();
    if (!reqId) return;

    const payload: RequestRemedyPayload = {
      action,
      additionalHours: action === 'EXTEND_WINDOW' ? 2 : undefined,
      additionalRadiusKm: action === 'EXPAND_RADIUS' ? 10 : undefined,
    };

    this.api.applyRemedy(reqId, payload).subscribe({
      next: () => this.handleRemedySuccess(reqId, action),
      error: () => this.handleRemedySuccess(reqId, action),
    });
  }

  private handleRemedySuccess(reqId: string, action: string): void {
    this.requests.update((list) =>
      list.map((r) => {
        if (r.id === reqId) {
          if (action === 'CANCEL_REQUEST') {
            return { ...r, status: 'cancelled' as RequestStatus, isExpired: false };
          } else {
            return {
              ...r,
              status: 'broadcasted' as RequestStatus,
              isExpired: false,
              broadcastExpiresAt: new Date(Date.now() + 2 * 3600000).toISOString(),
            };
          }
        }
        return r;
      })
    );
    this.closeRemedyModal();
  }

  cancelRequest(id: string): void {
    this.requests.update((list) =>
      list.map((r) => (r.id === id ? { ...r, status: 'cancelled' as RequestStatus } : r))
    );
  }

  canCancel(status: RequestStatus): boolean {
    return status === 'submitted' || status === 'broadcasted' || status === 'offers-received';
  }

  getCategoryIcon(category: string): string {
    const icons: Record<string, string> = {
      vehicle: '🚗',
      room: '🏠',
      transport: '🚚',
      emergency: '🚨',
    };
    return icons[category] || '📋';
  }

  getSummaryEntries(summary: Record<string, string>): { key: string; value: string }[] {
    return Object.entries(summary).map(([key, value]) => ({ key, value }));
  }
}
