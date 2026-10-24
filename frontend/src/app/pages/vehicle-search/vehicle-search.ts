import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TranslatePipe } from '@ngx-translate/core';
import { SearchStateService } from '../../services/search-state.service';
import {
  VehicleSearchCriteria,
  VehicleType,
  VEHICLE_TYPE_OPTIONS,
  createEmptyVehicleSearch,
} from '../../models/vehicle.model';

@Component({
  selector: 'app-vehicle-search',
  imports: [RouterLink, FormsModule, TranslatePipe],
  templateUrl: './vehicle-search.html',
  styleUrl: './vehicle-search.scss',
})
export class VehicleSearchComponent {
  private readonly searchState = inject(SearchStateService);
  private readonly router = inject(Router);

  readonly vehicleTypes = VEHICLE_TYPE_OPTIONS;
  readonly todayDate = new Date().toISOString().split('T')[0];

  readonly searchForm = signal<VehicleSearchCriteria>({
    ...this.searchState.vehicleSearch(),
  });

  readonly formErrors = signal<Record<string, string>>({});
  readonly isSubmitted = signal(false);

  updateField<K extends keyof VehicleSearchCriteria>(field: K, value: VehicleSearchCriteria[K]): void {
    this.searchForm.update((current) => ({
      ...current,
      [field]: value,
    }));
  }

  changeVehiclesRequired(delta: number): void {
    this.searchForm.update((current) => ({
      ...current,
      vehiclesRequired: Math.max(1, (current.vehiclesRequired || 1) + delta),
    }));
  }

  validate(): boolean {
    const form = this.searchForm();
    const errors: Record<string, string> = {};

    if (!form.pickup.trim()) {
      errors['pickup'] = 'VEHICLE_SEARCH.ERRORS.PICKUP_REQUIRED';
    }

    if (!form.destination.trim()) {
      errors['destination'] = 'VEHICLE_SEARCH.ERRORS.DESTINATION_REQUIRED';
    }

    if (!form.date) {
      errors['date'] = 'VEHICLE_SEARCH.ERRORS.DATE_REQUIRED';
    } else if (form.date < this.todayDate) {
      errors['date'] = 'VEHICLE_SEARCH.ERRORS.DATE_PAST';
    }

    if (!form.time) {
      errors['time'] = 'VEHICLE_SEARCH.ERRORS.TIME_REQUIRED';
    }

    if (!form.vehicleType) {
      errors['vehicleType'] = 'VEHICLE_SEARCH.ERRORS.TYPE_REQUIRED';
    }

    this.formErrors.set(errors);
    return Object.keys(errors).length === 0;
  }

  searchVehicles(): void {
    this.isSubmitted.set(true);
    if (!this.validate()) {
      return;
    }

    this.searchState.setVehicleSearch(this.searchForm());
    this.router.navigate(['/vehicle-results']);
  }

  resetForm(): void {
    const empty = createEmptyVehicleSearch();
    this.searchForm.set(empty);
    this.searchState.setVehicleSearch(empty);
    this.formErrors.set({});
    this.isSubmitted.set(false);
  }
}

