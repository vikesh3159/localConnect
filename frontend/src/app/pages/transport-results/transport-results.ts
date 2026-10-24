import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { SearchStateService } from '../../services/search-state.service';
import { MockDataService } from '../../services/mock-data.service';
import { DemoBadgeComponent } from '../../shared/demo-badge/demo-badge';
import { RatingComponent } from '../../shared/rating/rating';
import { TransportResult, TRANSPORT_VEHICLE_OPTIONS, GOODS_TYPE_OPTIONS } from '../../models/transport.model';

@Component({
  selector: 'app-transport-results',
  imports: [RouterLink, TranslatePipe, DemoBadgeComponent, RatingComponent],
  templateUrl: './transport-results.html',
  styleUrl: './transport-results.scss',
})
export class TransportResultsComponent implements OnInit {
  private readonly searchState = inject(SearchStateService);
  private readonly mockData = inject(MockDataService);
  private readonly router = inject(Router);

  readonly search = computed(() => this.searchState.transportSearch());
  readonly results = signal<TransportResult[]>([]);
  readonly loading = signal(true);

  readonly hasSearch = computed(() => {
    const s = this.search();
    return !!(s.pickup || s.destination || s.date);
  });

  readonly vehicleTypeLabel = computed(() => {
    const type = this.search().vehicleType;
    if (!type) return '';
    const opt = TRANSPORT_VEHICLE_OPTIONS.find((o) => o.value === type);
    return opt ? opt.labelKey : '';
  });

  readonly goodsTypeLabel = computed(() => {
    const goods = this.search().goodsType;
    if (!goods) return '';
    const opt = GOODS_TYPE_OPTIONS.find((o) => o.value === goods);
    return opt ? opt.labelKey : '';
  });

  ngOnInit(): void {
    setTimeout(() => {
      this.results.set(this.mockData.getTransportResults());
      this.loading.set(false);
    }, 700);
  }

  onEditSearch(): void {
    this.router.navigate(['/transport-search']);
  }

  onViewDetails(t: TransportResult): void {
    this.router.navigate(['/details/transport', t.id]);
  }

  onRequestTransport(t: TransportResult): void {
    this.searchState.selectResource('transport', t.id, t.vehicleName, t.providerName);
    this.router.navigate(['/request-summary']);
  }
}

