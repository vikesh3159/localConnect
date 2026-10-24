import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { AuthService } from '../../services/auth.service';

type Language = 'en' | 'hi';

@Component({
  selector: 'app-header',
  imports: [RouterLink, TranslatePipe],
  templateUrl: './header.html',
  styleUrl: './header.scss',
})
export class HeaderComponent {
  private readonly translate = inject(TranslateService);
  readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly storageKey = 'localconnect-language';

  readonly currentLanguage = signal<Language>(this.getSavedLanguage());
  readonly mobileMenuOpen = signal(false);

  changeLanguage(lang: Language): void {
    this.currentLanguage.set(lang);
    this.translate.use(lang);
    localStorage.setItem(this.storageKey, lang);
  }

  toggleMobileMenu(): void {
    this.mobileMenuOpen.update((v) => !v);
  }

  closeMobileMenu(): void {
    this.mobileMenuOpen.set(false);
  }

  toggleRoleMode(): void {
    const nextRole = this.auth.isProviderMode() ? 'CUSTOMER' : 'PROVIDER';
    this.auth.switchRole(nextRole).subscribe(() => {
      this.closeMobileMenu();
      if (nextRole === 'PROVIDER') {
        this.router.navigate(['/provider-dashboard']);
      } else {
        this.router.navigate(['/my-requests']);
      }
    });
  }

  logout(): void {
    this.auth.logout();
    this.closeMobileMenu();
    this.router.navigate(['/login']);
  }

  private getSavedLanguage(): Language {
    const saved = localStorage.getItem(this.storageKey);
    return saved === 'hi' ? 'hi' : 'en';
  }
}
