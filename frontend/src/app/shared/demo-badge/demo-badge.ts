import { Component } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-demo-badge',
  standalone: true,
  imports: [TranslatePipe],
  templateUrl: './demo-badge.html',
  styleUrl: './demo-badge.scss',
})
export class DemoBadgeComponent {}
