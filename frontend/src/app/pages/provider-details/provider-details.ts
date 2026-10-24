import { Component, OnInit, signal, computed } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { MockDataService } from '../../services/mock-data.service';
import { SearchStateService } from '../../services/search-state.service';
import { DemoBadgeComponent } from '../../shared/demo-badge/demo-badge';
import { RatingComponent } from '../../shared/rating/rating';
import {
  ResourceCategory,
  VehicleResult,
  RoomResult,
  TransportResult,
  Provider,
} from '../../models';

@Component({
  selector: 'app-provider-details',
  templateUrl: './provider-details.html',
  styleUrl: './provider-details.scss',
  imports: [TranslatePipe, DemoBadgeComponent, RatingComponent],
})
export class ProviderDetailsComponent implements OnInit {
  category = signal<ResourceCategory>('vehicle');
  resource = signal<VehicleResult | RoomResult | TransportResult | null>(null);
  provider = signal<Provider | null>(null);

  categoryEmoji = computed(() => {
    switch (this.category()) {
      case 'vehicle':
        return '🚗';
      case 'room':
        return '🏠';
      case 'transport':
        return '🚚';
      case 'emergency':
        return '🚨';
    }
  });

  categoryLabel = computed(() => {
    switch (this.category()) {
      case 'vehicle':
        return 'CATEGORIES.VEHICLES';
      case 'room':
        return 'CATEGORIES.ROOMS';
      case 'transport':
        return 'CATEGORIES.TRANSPORT';
      case 'emergency':
        return 'CATEGORIES.EMERGENCY';
    }
  });

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private mockData: MockDataService,
    private searchState: SearchStateService,
  ) {}

  ngOnInit(): void {
    const cat = this.route.snapshot.paramMap.get('category') as ResourceCategory;
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.category.set(cat);

    let res: VehicleResult | RoomResult | TransportResult | undefined;
    switch (cat) {
      case 'vehicle':
        res = this.mockData.getVehicleById(id);
        break;
      case 'room':
        res = this.mockData.getRoomById(id);
        break;
      case 'transport':
        res = this.mockData.getTransportById(id);
        break;
    }

    if (res) {
      this.resource.set(res);
      const providerId = (res as any).providerId;
      if (providerId) {
        const prov = this.mockData.getProviderById(providerId);
        if (prov) {
          this.provider.set(prov);
        }
      }
    }
  }

  get vehicleResource(): VehicleResult | null {
    return this.category() === 'vehicle' ? (this.resource() as VehicleResult) : null;
  }

  get roomResource(): RoomResult | null {
    return this.category() === 'room' ? (this.resource() as RoomResult) : null;
  }

  get transportResource(): TransportResult | null {
    return this.category() === 'transport' ? (this.resource() as TransportResult) : null;
  }

  goBack(): void {
    const cat = this.category();
    if (cat === 'vehicle') this.router.navigate(['/vehicle-results']);
    else if (cat === 'room') this.router.navigate(['/room-results']);
    else if (cat === 'transport') this.router.navigate(['/transport-results']);
    else this.router.navigate(['/']);
  }

  requestResource(): void {
    const res = this.resource();
    if (!res) return;

    const cat = this.category();
    let name = '';
    let providerName = '';

    if (cat === 'vehicle') {
      const v = res as VehicleResult;
      name = v.name;
      providerName = v.providerName;
    } else if (cat === 'room') {
      const r = res as RoomResult;
      name = r.propertyName;
      providerName = r.providerName;
    } else if (cat === 'transport') {
      const t = res as TransportResult;
      name = t.vehicleName;
      providerName = t.providerName;
    }

    this.searchState.selectResource(cat, res.id, name, providerName);
    this.router.navigate(['/request-summary']);
  }
}
