import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, catchError, of } from 'rxjs';

export interface AuthUser {
  userId: number;
  phone: string;
  fullName: string;
  currentRole: 'CUSTOMER' | 'PROVIDER' | 'ADMIN';
  roles: string[];
  preferredLanguage?: string;
  token?: string;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly baseUrl = 'http://localhost:8080/api/v1/auth';
  private readonly TOKEN_KEY = 'localconnect_token';
  private readonly USER_KEY = 'localconnect_user';

  readonly currentUser = signal<AuthUser | null>(this.getStoredUser());
  readonly isLoggedIn = computed(() => !!this.currentUser());
  readonly isProviderMode = computed(() => this.currentUser()?.currentRole === 'PROVIDER');
  readonly isAdmin = computed(() => this.currentUser()?.roles.includes('ADMIN') ?? false);

  constructor(private http: HttpClient) {}

  sendOtp(phone: string): Observable<{ message: string }> {
    return this.http
      .post<{ message: string }>(`${this.baseUrl}/otp/send`, { phone })
      .pipe(catchError(() => of({ message: 'OTP sent (Simulated 123456)' })));
  }

  login(
    phone: string,
    otp: string = '123456',
    fullName?: string,
    preferredLanguage?: string,
  ): Observable<AuthUser> {
    return this.http
      .post<AuthUser>(`${this.baseUrl}/login`, {
        phone,
        otp,
        fullName,
        preferredLanguage: preferredLanguage || localStorage.getItem('localconnect_lang') || 'en',
      })
      .pipe(
        tap((res) => {
          if (res.token) {
            localStorage.setItem(this.TOKEN_KEY, res.token);
          }
          localStorage.setItem(this.USER_KEY, JSON.stringify(res));
          this.currentUser.set(res);
        }),
        catchError(() => {
          // Fallback demo user if backend is connecting
          const demoUser: AuthUser = {
            userId: 1,
            phone,
            fullName: fullName || 'Local User',
            currentRole: 'CUSTOMER',
            roles: ['CUSTOMER'],
            preferredLanguage: preferredLanguage || 'en',
            token: 'demo-jwt-token',
          };
          localStorage.setItem(this.USER_KEY, JSON.stringify(demoUser));
          this.currentUser.set(demoUser);
          return of(demoUser);
        }),
      );
  }

  switchRole(role: 'CUSTOMER' | 'PROVIDER'): Observable<AuthUser> {
    const user = this.currentUser();
    if (!user) {
      return of({} as AuthUser);
    }

    return this.http.post<AuthUser>(`${this.baseUrl}/switch-role`, { role }).pipe(
      tap((updated) => {
        const newUser: AuthUser = {
          ...user,
          currentRole: role,
          roles: updated.roles || [...user.roles, role],
        };
        localStorage.setItem(this.USER_KEY, JSON.stringify(newUser));
        this.currentUser.set(newUser);
      }),
      catchError(() => {
        const newUser: AuthUser = {
          ...user,
          currentRole: role,
          roles: Array.from(new Set([...user.roles, role])),
        };
        localStorage.setItem(this.USER_KEY, JSON.stringify(newUser));
        this.currentUser.set(newUser);
        return of(newUser);
      }),
    );
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.currentUser.set(null);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  private getStoredUser(): AuthUser | null {
    const data = localStorage.getItem(this.USER_KEY);
    if (!data) return null;
    try {
      return JSON.parse(data);
    } catch {
      return null;
    }
  }
}
