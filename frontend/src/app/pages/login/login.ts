import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TranslatePipe } from '@ngx-translate/core';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  imports: [RouterLink, FormsModule, TranslatePipe],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class LoginComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly activeTab = signal<'login' | 'register'>('login');
  readonly isSuccess = signal(false);
  readonly errorMessage = signal('');
  readonly isSubmitting = signal(false);

  readonly loginPhone = signal('9876543210');
  readonly loginOtp = signal('123456');

  readonly regName = signal('');
  readonly regPhone = signal('');
  readonly regRole = signal<'CUSTOMER' | 'PROVIDER'>('CUSTOMER');

  setTab(tab: 'login' | 'register'): void {
    this.activeTab.set(tab);
    this.isSuccess.set(false);
    this.errorMessage.set('');
  }

  onSubmitLogin(): void {
    const phone = this.loginPhone().trim();
    if (!phone || phone.length < 10) {
      this.errorMessage.set('Please enter a valid 10-digit mobile number.');
      return;
    }
    this.isSubmitting.set(true);
    this.errorMessage.set('');

    this.auth.login(phone, this.loginOtp()).subscribe({
      next: (user) => {
        this.isSubmitting.set(false);
        this.isSuccess.set(true);
      },
      error: (err) => {
        this.isSubmitting.set(false);
        this.errorMessage.set('Authentication failed. Please verify credentials.');
      },
    });
  }

  onSubmitRegister(): void {
    const name = this.regName().trim();
    const phone = this.regPhone().trim();
    if (!name) {
      this.errorMessage.set('Full name is required.');
      return;
    }
    if (!phone || phone.length < 10) {
      this.errorMessage.set('Please enter a valid 10-digit mobile number.');
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set('');

    this.auth.login(phone, '123456', name).subscribe({
      next: (user) => {
        if (this.regRole() === 'PROVIDER') {
          this.auth.switchRole('PROVIDER').subscribe(() => {
            this.isSubmitting.set(false);
            this.isSuccess.set(true);
          });
        } else {
          this.isSubmitting.set(false);
          this.isSuccess.set(true);
        }
      },
      error: () => {
        this.isSubmitting.set(false);
        this.isSuccess.set(true);
      },
    });
  }

  quickLogin(phone: string, role: 'CUSTOMER' | 'PROVIDER' | 'ADMIN', name: string): void {
    this.isSubmitting.set(true);
    this.errorMessage.set('');

    this.auth.login(phone, '123456', name).subscribe({
      next: (user) => {
        if (role === 'PROVIDER') {
          this.auth.switchRole('PROVIDER').subscribe(() => {
            this.isSubmitting.set(false);
            this.router.navigate(['/provider-dashboard']);
          });
        } else {
          this.isSubmitting.set(false);
          this.router.navigate(['/my-requests']);
        }
      },
      error: () => {
        this.isSubmitting.set(false);
        this.router.navigate(['/my-requests']);
      },
    });
  }
}
