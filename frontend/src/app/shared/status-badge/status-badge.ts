import { Component, computed, input } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';
import { RequestStatus } from '../../models/request.model';

@Component({
  selector: 'app-status-badge',
  imports: [TranslatePipe],
  templateUrl: './status-badge.html',
  styleUrl: './status-badge.scss',
})
export class StatusBadgeComponent {
  readonly status = input.required<RequestStatus>();

  protected readonly statusKey = computed(() =>
    this.status() ? this.status().toUpperCase().replace(/-/g, '_') : 'SUBMITTED'
  );
}

