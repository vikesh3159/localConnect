import { Component, signal, computed } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TranslatePipe } from '@ngx-translate/core';
import { SearchStateService } from '../../services/search-state.service';
import {
  RoomSearchCriteria,
  RoomType,
  ROOM_TYPE_OPTIONS,
  createEmptyRoomSearch
} from '../../models/room.model';

@Component({
  selector: 'app-room-search',
  templateUrl: './room-search.html',
  styleUrl: './room-search.scss',
  imports: [TranslatePipe, RouterLink, FormsModule]
})
export class RoomSearchComponent {
  readonly roomTypeOptions = ROOM_TYPE_OPTIONS;
  readonly today = new Date().toISOString().split('T')[0];

  form = signal<RoomSearchCriteria>(createEmptyRoomSearch());
  submitted = signal(false);

  readonly minCheckOut = computed(() => {
    const checkIn = this.form().checkIn;
    if (checkIn) {
      const next = new Date(checkIn);
      next.setDate(next.getDate() + 1);
      return next.toISOString().split('T')[0];
    }
    return this.today;
  });

  readonly locationError = computed(() => this.submitted() && !this.form().location.trim());
  readonly checkInError = computed(() => this.submitted() && !this.form().checkIn);
  readonly checkInPastError = computed(() => {
    const ci = this.form().checkIn;
    return this.submitted() && !!ci && ci < this.today;
  });
  readonly checkOutError = computed(() => this.submitted() && !this.form().checkOut);
  readonly checkOutBeforeError = computed(() => {
    const { checkIn, checkOut } = this.form();
    return this.submitted() && !!checkIn && !!checkOut && checkOut <= checkIn;
  });
  readonly roomTypeError = computed(() => this.submitted() && !this.form().roomType);

  constructor(
    private searchState: SearchStateService,
    private router: Router
  ) {
    // Load existing search criteria if available
    const existing = this.searchState.roomSearch();
    if (existing.location || existing.checkIn) {
      this.form.set({ ...existing });
    }
  }

  updateField<K extends keyof RoomSearchCriteria>(field: K, value: RoomSearchCriteria[K]) {
    this.form.update(f => ({ ...f, [field]: value }));
    // If check-in changes and check-out is before new check-in, reset check-out
    if (field === 'checkIn') {
      const checkOut = this.form().checkOut;
      if (checkOut && checkOut <= (value as string)) {
        this.form.update(f => ({ ...f, checkOut: '' }));
      }
    }
  }

  incrementGuests() {
    this.form.update(f => ({ ...f, guests: f.guests + 1 }));
  }

  decrementGuests() {
    this.form.update(f => ({ ...f, guests: Math.max(1, f.guests - 1) }));
  }

  incrementRooms() {
    this.form.update(f => ({ ...f, rooms: f.rooms + 1 }));
  }

  decrementRooms() {
    this.form.update(f => ({ ...f, rooms: Math.max(1, f.rooms - 1) }));
  }

  isValid(): boolean {
    const f = this.form();
    return !!(
      f.location.trim() &&
      f.checkIn &&
      f.checkIn >= this.today &&
      f.checkOut &&
      f.checkOut > f.checkIn &&
      f.roomType
    );
  }

  onSubmit() {
    this.submitted.set(true);
    if (!this.isValid()) return;
    this.searchState.setRoomSearch(this.form());
    this.router.navigate(['/room-results']);
  }

  onReset() {
    this.form.set(createEmptyRoomSearch());
    this.submitted.set(false);
    this.searchState.resetRoomSearch();
  }
}
