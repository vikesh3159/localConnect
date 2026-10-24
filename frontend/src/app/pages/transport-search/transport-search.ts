import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TranslatePipe } from '@ngx-translate/core';
import { SearchStateService } from '../../services/search-state.service';
import {
  TransportSearchCriteria,
  TRANSPORT_VEHICLE_OPTIONS,
  GOODS_TYPE_OPTIONS,
  createEmptyTransportSearch,
} from '../../models/transport.model';

@Component({
  selector: 'app-transport-search',
  imports: [RouterLink, FormsModule, TranslatePipe],
  templateUrl: './transport-search.html',
  styleUrl: './transport-search.scss',
})
export class TransportSearchComponent {
  private readonly searchState = inject(SearchStateService);
  private readonly router = inject(Router);

  readonly vehicleTypes = TRANSPORT_VEHICLE_OPTIONS;
  readonly goodsTypes = GOODS_TYPE_OPTIONS;
  readonly todayDate = new Date().toISOString().split('T')[0];

  readonly searchForm = signal<TransportSearchCriteria>({
    ...this.searchState.transportSearch(),
  });

  readonly formErrors = signal<Record<string, string>>({});
  readonly isSubmitted = signal(false);

  updateField<K extends keyof TransportSearchCriteria>(field: K, value: TransportSearchCriteria[K]): void {
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
      errors['pickup'] = 'TRANSPORT_SEARCH.ERRORS.PICKUP_REQUIRED';
    }

    if (!form.destination.trim()) {
      errors['destination'] = 'TRANSPORT_SEARCH.ERRORS.DESTINATION_REQUIRED';
    }

    if (!form.date) {
      errors['date'] = 'TRANSPORT_SEARCH.ERRORS.DATE_REQUIRED';
    } else if (form.date < this.todayDate) {
      errors['date'] = 'TRANSPORT_SEARCH.ERRORS.DATE_PAST';
    }

    if (!form.time) {
      errors['time'] = 'TRANSPORT_SEARCH.ERRORS.TIME_REQUIRED';
    }

    if (!form.goodsType) {
      errors['goodsType'] = 'TRANSPORT_SEARCH.ERRORS.GOODS_TYPE_REQUIRED';
    }

    if (!form.vehicleType) {
      errors['vehicleType'] = 'TRANSPORT_SEARCH.ERRORS.VEHICLE_TYPE_REQUIRED';
    }

    this.formErrors.set(errors);
    return Object.keys(errors).length === 0;
  }

  searchTransport(): void {
    this.isSubmitted.set(true);
    if (!this.validate()) {
      return;
    }

    this.searchState.setTransportSearch(this.searchForm());
    this.router.navigate(['/transport-results']);
  }

  resetForm(): void {
    const empty = createEmptyTransportSearch();
    this.searchForm.set(empty);
    this.searchState.setTransportSearch(empty);
    this.formErrors.set({});
    this.isSubmitted.set(false);
  }
}

