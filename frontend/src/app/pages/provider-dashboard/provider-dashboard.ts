import { Component, computed, inject, signal, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TranslatePipe } from '@ngx-translate/core';
import { MockDataService } from '../../services/mock-data.service';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { RequestStateService } from '../../services/request-state.service';
import { BroadcastPolicyService } from '../../services/broadcast-policy.service';
import { StatusBadgeComponent } from '../../shared/status-badge/status-badge';
import { Driver, ProviderResource } from '../../models/provider.model';
import { UserRequest, ProviderOffer, RequestStatus } from '../../models/request.model';

@Component({
  selector: 'app-provider-dashboard',
  imports: [RouterLink, FormsModule, TranslatePipe, StatusBadgeComponent],
  templateUrl: './provider-dashboard.html',
  styleUrl: './provider-dashboard.scss',
})
export class ProviderDashboardComponent implements OnInit {
  private readonly mockData = inject(MockDataService);
  private readonly api = inject(ApiService);
  readonly auth = inject(AuthService);
  private readonly requestState = inject(RequestStateService);
  readonly broadcastPolicy = inject(BroadcastPolicyService);
  private readonly router = inject(Router);

  readonly providerId = 101;
  readonly providerName = 'Rajesh Kumar';

  readonly activeTab = signal<'incoming' | 'holds' | 'confirmed' | 'fleet'>('incoming');

  readonly resources = signal<ProviderResource[]>([]);
  readonly drivers = signal<Driver[]>([]);
  readonly incomingRequests = signal<UserRequest[]>([]);
  readonly providerOffers = signal<ProviderOffer[]>([]);

  // Stage 1 Quotation Modal state
  readonly showQuoteModal = signal(false);
  readonly quotingRequest = signal<UserRequest | null>(null);
  readonly quoteQty = signal<number>(1);
  readonly quoteAmount = signal<number>(2200);
  readonly quoteDriverIncluded = signal<boolean>(true);

  // Stage 2 Nomination Modal state
  readonly showNominateModal = signal(false);
  readonly nominatingOffer = signal<ProviderOffer | null>(null);
  readonly selectedResourceId = signal<number | null>(null);
  readonly selectedDriverId = signal<number | null>(null);

  ngOnInit(): void {
    this.loadProviderData();
  }

  loadProviderData(): void {
    // 1. Load resources
    this.api.getProviderResources().subscribe({
      next: (res) => {
        if (res && res.length > 0) {
          this.resources.set(res);
        } else {
          this.resources.set(this.mockData.getProviderResources(this.providerId));
        }
      },
      error: () => {
        this.resources.set(this.mockData.getProviderResources(this.providerId));
      },
    });

    // 2. Load drivers
    this.api.getProviderDrivers().subscribe({
      next: (drv) => {
        if (drv && drv.length > 0) {
          this.drivers.set(drv);
        } else {
          this.drivers.set([
            {
              id: 201,
              providerId: this.providerId,
              fullName: 'Suresh Sharma',
              phone: '+91 98260 12345',
              licenseNumber: 'MP-09-2015-0044123',
              licenseType: 'Commercial Transport (HMV/LMV)',
              licenseExpiryDate: '2029-12-31',
              experienceYears: 12,
              verificationStatus: 'APPROVED',
              isAvailable: true,
            },
            {
              id: 202,
              providerId: this.providerId,
              fullName: 'Manoj Verma',
              phone: '+91 98260 54321',
              licenseNumber: 'MP-09-2018-0099881',
              licenseType: 'Commercial LMV',
              licenseExpiryDate: '2030-05-15',
              experienceYears: 8,
              verificationStatus: 'APPROVED',
              isAvailable: true,
            },
          ]);
        }
      },
      error: () => {
        this.drivers.set([
          {
            id: 201,
            providerId: this.providerId,
            fullName: 'Suresh Sharma',
            phone: '+91 98260 12345',
            licenseNumber: 'MP-09-2015-0044123',
            licenseType: 'Commercial Transport (HMV/LMV)',
            licenseExpiryDate: '2029-12-31',
            experienceYears: 12,
            verificationStatus: 'APPROVED',
            isAvailable: true,
          },
        ]);
      },
    });

    // 3. Load incoming requests (Customer-created requests + broadcasted defaults)
    const storedRequests: UserRequest[] = this.requestState.requests()
      .filter((r) => r.status !== 'cancelled')
      .map((r) => ({
        ...r,
        status: (r.status === 'submitted' ? 'broadcasted' : r.status) as RequestStatus,
        broadcastExpiresAt: r.broadcastExpiresAt || new Date(Date.now() + 4 * 3600000).toISOString(),
        quantityRequired: r.quantityRequired || 1,
      }));

    const defaultRequests: UserRequest[] = [
      {
        id: 'REQ-VEH-101',
        referenceNumber: 'LC-VEH-9014',
        category: 'vehicle',
        status: 'broadcasted',
        createdAt: new Date().toISOString(),
        summary: {
          Pickup: 'Khandwa Railway Station',
          Destination: 'Omkareshwar Jyotirlinga',
          Date: 'Tomorrow at 07:30 AM',
          VehicleRequired: '7 Seater SUV / Bolero with Driver',
          Urgency: 'Normal Broadcast (3h 45m left)',
        },
        providerName: 'Customer: Ramesh Patel',
        resourceName: 'SUV with Driver',
        broadcastExpiresAt: new Date(Date.now() + 3.75 * 3600000).toISOString(),
        quantityRequired: 1,
      },
      {
        id: 'REQ-TRP-202',
        referenceNumber: 'LC-TRP-5582',
        category: 'transport',
        status: 'broadcasted',
        createdAt: new Date().toISOString(),
        summary: {
          Pickup: 'Burhanpur Mandi',
          Destination: 'Indore Mandi',
          Date: 'Tomorrow at 05:00 AM',
          Cargo: 'Banana Crop 20 Quintals',
          Urgency: 'Normal Broadcast (3h 10m left)',
        },
        providerName: 'Customer: Sunil Patidar',
        resourceName: 'Tata Ace / 407 Cargo',
        broadcastExpiresAt: new Date(Date.now() + 3.16 * 3600000).toISOString(),
        quantityRequired: 1,
      },
    ];

    this.incomingRequests.set([...storedRequests, ...defaultRequests]);

    // 4. Initial demo offers
    this.providerOffers.set([
      {
        id: 'OFFER-HOLD-1',
        requestId: 'REQ-VEH-89',
        providerId: '101',
        providerBusinessName: 'Rajesh Kumar Fleet Services',
        providerRating: 4.8,
        offeredQuantity: 1,
        quotedAmount: 2400,
        includesDriver: true,
        status: 'provisionally-selected',
        nominationDeadline: new Date(Date.now() + 13 * 60000).toISOString(),
        expiresAt: new Date(Date.now() + 13 * 60000).toISOString(),
        createdAt: new Date(Date.now() - 2 * 60000).toISOString(),
      },
    ]);
  }

  readonly activeResourcesCount = computed(() =>
    this.resources().filter((r) => r.isActive).length
  );

  readonly pendingRequestsCount = computed(() => this.incomingRequests().length);
  readonly activeHoldsCount = computed(
    () => this.providerOffers().filter((o) => o.status === 'provisionally-selected').length
  );

  // Switch persona to provider if not active
  ensureProviderMode(): void {
    this.auth.switchRole('PROVIDER').subscribe(() => {
      this.loadProviderData();
    });
  }

  // Stage 1 Quotation Modal
  openQuoteModal(req: UserRequest): void {
    this.quotingRequest.set(req);
    this.quoteQty.set(req.quantityRequired || 1);
    this.quoteAmount.set(req.category === 'vehicle' ? 2200 : 1800);
    this.quoteDriverIncluded.set(true);
    this.showQuoteModal.set(true);
  }

  closeQuoteModal(): void {
    this.showQuoteModal.set(false);
    this.quotingRequest.set(null);
  }

  submitQuotation(): void {
    const req = this.quotingRequest();
    if (!req) return;

    const payload = {
      requestId: parseInt(req.id.replace(/\D/g, '') || '101', 10),
      offeredQuantity: this.quoteQty(),
      quotedAmount: this.quoteAmount(),
      includesDriver: this.quoteDriverIncluded(),
    };

    this.api.submitQuotation(payload).subscribe({
      next: (offer) => this.handleQuoteSuccess(req.id, offer),
      error: () => {
        const demoOffer: ProviderOffer = {
          id: 'OFFER-' + Date.now().toString(36).toUpperCase(),
          requestId: req.id,
          providerId: String(this.providerId),
          providerBusinessName: this.providerName,
          providerRating: 4.8,
          offeredQuantity: this.quoteQty(),
          quotedAmount: this.quoteAmount(),
          includesDriver: this.quoteDriverIncluded(),
          status: 'submitted',
          expiresAt: new Date(Date.now() + 4 * 3600000).toISOString(),
          createdAt: new Date().toISOString(),
        };
        this.handleQuoteSuccess(req.id, demoOffer);
      },
    });
  }

  private handleQuoteSuccess(reqId: string, offer: ProviderOffer): void {
    this.providerOffers.update((list) => [offer, ...list]);
    this.incomingRequests.update((list) => list.filter((r) => r.id !== reqId));
    this.closeQuoteModal();
  }

  // Stage 2 Nomination Modal
  openNominateModal(offer: ProviderOffer): void {
    this.nominatingOffer.set(offer);
    const firstRes = this.resources()[0];
    const firstDrv = this.drivers()[0];
    this.selectedResourceId.set(firstRes ? firstRes.id : 1);
    this.selectedDriverId.set(firstDrv ? firstDrv.id : 201);
    this.showNominateModal.set(true);
  }

  closeNominateModal(): void {
    this.showNominateModal.set(false);
    this.nominatingOffer.set(null);
  }

  submitNomination(): void {
    const offer = this.nominatingOffer();
    const resId = this.selectedResourceId();
    const drvId = this.selectedDriverId();
    if (!offer || !resId || !drvId) return;

    const selectedVehicle = this.resources().find((r) => r.id === resId);
    const selectedDrv = this.drivers().find((d) => d.id === drvId);

    const payload = {
      resourceId: resId,
      driverId: drvId,
    };

    this.api.nominateResource(offer.id, payload).subscribe({
      next: () => this.handleNominationSuccess(offer.id, selectedVehicle, selectedDrv),
      error: () => this.handleNominationSuccess(offer.id, selectedVehicle, selectedDrv),
    });
  }

  private handleNominationSuccess(
    offerId: string,
    vehicle?: ProviderResource,
    driver?: Driver
  ): void {
    this.providerOffers.update((list) =>
      list.map((o) => {
        if (o.id === offerId) {
          return {
            ...o,
            status: 'nomination-confirmed' as const,
            nominatedResourceId: String(vehicle?.id || 1),
            nominatedResourceTitle: vehicle?.name || 'Mahindra Bolero Camper',
            nominatedVehiclePlate: 'MP-12-CD-5678',
            assignedDriverName: driver?.fullName || 'Suresh Sharma',
            assignedDriverPhone: driver?.phone || '+91 98260 12345',
          };
        }
        return o;
      })
    );
    this.closeNominateModal();
  }

  getCategoryIcon(cat: string): string {
    const icons: Record<string, string> = {
      vehicle: '🚗',
      room: '🏠',
      transport: '🚚',
      emergency: '🚨',
    };
    return icons[cat] || '📋';
  }

  getSummaryEntries(summary: Record<string, string>): { key: string; value: string }[] {
    return Object.entries(summary).map(([key, value]) => ({ key, value }));
  }
}
