import { Component, signal, computed, inject } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { UpperCasePipe } from '@angular/common';
import { TranslatePipe } from '@ngx-translate/core';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import {
  STATUTORY_HELPLINES,
  StatutoryHelpline,
  EmergencyAlertRecord,
  EmergencyType,
  EMERGENCY_TYPE_OPTIONS,
} from '../../models/emergency.model';

@Component({
  selector: 'app-emergency',
  templateUrl: './emergency.html',
  styleUrl: './emergency.scss',
  imports: [FormsModule, TranslatePipe, UpperCasePipe],
})
export class EmergencyComponent {
  private readonly api = inject(ApiService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly helplines = signal<StatutoryHelpline[]>(STATUTORY_HELPLINES);
  readonly selectedCategory = signal<string>('all');
  readonly lastDialedHelpline = signal<string | null>(null);

  readonly alertType = signal<string>('urgent-transport');
  readonly callerPhone = signal<string>(this.auth.currentUser()?.phone || '9876543210');
  readonly callerLocation = signal<string>('');
  readonly callerLandmark = signal<string>('');
  readonly callerNotes = signal<string>('');

  readonly isBroadcasting = signal<boolean>(false);
  readonly formError = signal<string | null>(null);
  readonly activeAlert = signal<EmergencyAlertRecord | null>(null);

  readonly typeOptions = EMERGENCY_TYPE_OPTIONS;

  readonly filteredHelplines = computed(() => {
    const cat = this.selectedCategory();
    if (cat === 'all') return this.helplines();
    return this.helplines().filter((h) => h.category === cat);
  });

  onDialHelpline(helpline: StatutoryHelpline): void {
    this.lastDialedHelpline.set(helpline.number);
    this.api.recordHelplineDial(helpline.number, this.callerPhone(), this.callerLocation()).subscribe({
      next: () => {},
      error: () => {},
    });
    window.location.href = `tel:${helpline.number}`;
  }

  broadcastCommunityAlert(): void {
    if (!this.callerLocation().trim()) {
      this.formError.set('Location is strictly required to coordinate local assistance.');
      return;
    }
    if (!this.callerPhone().trim()) {
      this.formError.set('Contact phone is required for providers to contact you.');
      return;
    }

    this.formError.set(null);
    this.isBroadcasting.set(true);

    const payload = {
      emergencyType: this.alertType(),
      contactPhone: this.callerPhone().trim(),
      callerLocation: this.callerLocation().trim(),
      callerLandmark: this.callerLandmark().trim(),
      notes: this.callerNotes().trim(),
    };

    this.api.broadcastEmergencyAlert(payload).subscribe({
      next: (alert) => {
        this.activeAlert.set(alert);
        this.isBroadcasting.set(false);
      },
      error: () => {
        // Fallback demo state
        const fallbackAlert: EmergencyAlertRecord = {
          id: 'ALERT-' + Date.now().toString(36).toUpperCase(),
          emergencyType: this.alertType(),
          contactPhone: this.callerPhone(),
          callerLocation: this.callerLocation(),
          callerLandmark: this.callerLandmark(),
          notes: this.callerNotes(),
          status: 'local-assistance-requested',
          createdAt: new Date().toISOString(),
        };
        this.activeAlert.set(fallbackAlert);
        this.isBroadcasting.set(false);
      },
    });
  }

  simulateProviderAck(): void {
    const curr = this.activeAlert();
    if (!curr) return;
    this.activeAlert.update((a) => (a ? { ...a, status: 'provider-acknowledged' } : null));
  }

  simulateProviderEnRoute(): void {
    const curr = this.activeAlert();
    if (!curr) return;
    this.activeAlert.update((a) =>
      a
        ? {
            ...a,
            status: 'confirmed-en-route',
            providerEtaMinutes: 12,
            providerVehiclePlate: 'MP-12-CD-5678',
          }
        : null
    );
  }

  simulateResolve(): void {
    const curr = this.activeAlert();
    if (!curr) return;
    this.activeAlert.update((a) => (a ? { ...a, status: 'resolved' } : null));
  }

  clearAlert(): void {
    this.activeAlert.set(null);
  }
}
