import { Component, signal, computed, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { SearchStateService } from '../../services/search-state.service';
import { MockDataService } from '../../services/mock-data.service';
import { DemoBadgeComponent } from '../../shared/demo-badge/demo-badge';
import { RatingComponent } from '../../shared/rating/rating';
import { RoomResult, RoomSearchCriteria, ROOM_TYPE_OPTIONS } from '../../models/room.model';

@Component({
  selector: 'app-room-results',
  templateUrl: './room-results.html',
  styleUrl: './room-results.scss',
  imports: [TranslatePipe, RouterLink, DemoBadgeComponent, RatingComponent]
})
export class RoomResultsComponent implements OnInit {
  readonly search = computed(() => this.searchState.roomSearch());
  results = signal<RoomResult[]>([]);
  loading = signal(true);

  readonly hasSearch = computed(() => {
    const s = this.search();
    return !!(s.location || s.checkIn);
  });

  readonly roomTypeLabel = computed(() => {
    const type = this.search().roomType;
    if (!type) return '';
    const opt = ROOM_TYPE_OPTIONS.find(o => o.value === type);
    return opt ? opt.labelKey : '';
  });

  constructor(
    private searchState: SearchStateService,
    private mockData: MockDataService,
    private router: Router
  ) {}

  ngOnInit() {
    // Simulate loading delay
    setTimeout(() => {
      this.results.set(this.mockData.getRoomResults());
      this.loading.set(false);
    }, 800);
  }

  onEditSearch() {
    this.router.navigate(['/room-search']);
  }

  onViewDetails(room: RoomResult) {
    this.router.navigate(['/details/room', room.id]);
  }

  onRequestBooking(room: RoomResult) {
    this.searchState.selectResource('room', room.id, room.propertyName, room.providerName);
    this.router.navigate(['/request-summary']);
  }

  formatPrice(price: number): string {
    return '₹' + price.toLocaleString('en-IN');
  }
}
