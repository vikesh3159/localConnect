import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { RequestStateService } from '../../services/request-state.service';
import { StatusBadgeComponent } from '../../shared/status-badge/status-badge';

@Component({
  selector: 'app-request-confirmation',
  templateUrl: './request-confirmation.html',
  styleUrl: './request-confirmation.scss',
  imports: [RouterLink, TranslatePipe, StatusBadgeComponent]
})
export class RequestConfirmationComponent {
  private route = inject(ActivatedRoute);
  private requestState = inject(RequestStateService);

  ref = signal('');

  request = computed(() => {
    const ref = this.ref();
    if (!ref) return undefined;
    return this.requestState.getRequestByRef(ref);
  });

  constructor() {
    this.ref.set(this.route.snapshot.paramMap.get('ref') || '');
  }

  getSummaryEntries(): { key: string; value: string }[] {
    const req = this.request();
    if (!req) return [];
    return Object.entries(req.summary).map(([key, value]) => ({ key, value }));
  }

  getCategoryIcon(): string {
    const req = this.request();
    if (!req) return '📋';
    const icons: Record<string, string> = {
      vehicle: '🚗', room: '🏠', transport: '🚚', emergency: '🚨',
    };
    return icons[req.category] || '📋';
  }
}
