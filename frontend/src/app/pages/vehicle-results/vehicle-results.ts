import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { SearchStateService } from '../../services/search-state.service';
import { MockDataService } from '../../services/mock-data.service';
import { DemoBadgeComponent } from '../../shared/demo-badge/demo-badge';
import { RatingComponent } from '../../shared/rating/rating';
import { VehicleResult, VEHICLE_TYPE_OPTIONS } from '../../models/vehicle.model';

@Component({
  selector: 'app-vehicle-results',
  imports: [RouterLink, TranslatePipe, DemoBadgeComponent, RatingComponent],
  templateUrl: './vehicle-results.html',
  styleUrl: './vehicle-results.scss',
})
export class VehicleResultsComponent implements OnInit {
  private readonly searchState = inject(SearchStateService);
  private readonly mockData = inject(MockDataService);
  private readonly router = inject(Router);

  readonly search = computed(() => this.searchState.vehicleSearch());
  readonly results = signal<VehicleResult[]>([]);
  readonly loading = signal(true);

  readonly hasSearch = computed(() => {
    const s = this.search();
    return !!(s.pickup || s.destination || s.date);
  });

  readonly vehicleTypeLabel = computed(() => {
    const type = this.search().vehicleType;
    if (!type) return '';
    const opt = VEHICLE_TYPE_OPTIONS.find((o) => o.value === type);
    return opt ? opt.labelKey : '';
  });

  ngOnInit(): void {
    // Simulate loading delay for realistic experience
    setTimeout(() => {
      this.results.set(this.mockData.getVehicleResults());
      this.loading.set(false);
    }, 700);
  }

  onEditSearch(): void {
    this.router.navigate(['/vehicle-search']);
  }

  onViewDetails(vehicle: VehicleResult): void {
    this.router.navigate(['/details/vehicle', vehicle.id]);
  }

  onRequestVehicle(vehicle: VehicleResult): void {
    this.searchState.selectResource('vehicle', vehicle.id, vehicle.name, vehicle.providerName);
    this.router.navigate(['/request-summary']);
  }
}

