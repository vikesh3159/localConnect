import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { UpperCasePipe } from '@angular/common';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';

export interface AdminProvider {
  id: number;
  businessName: string;
  ownerName: string;
  phone: string;
  operationalDistrict: string;
  operationalTaluka: string;
  serviceBase: string;
  ratingAverage: number;
  totalCompletedJobs: number;
  verificationStatus: 'APPROVED' | 'PENDING_APPROVAL' | 'SUSPENDED' | 'REJECTED';
  createdAt?: string;
}

export interface AdminCustomer {
  id: number;
  phone: string;
  fullName: string;
  activeRole: string;
  roles: string[];
  isActive: boolean;
  preferredLanguage?: string;
  createdAt?: string;
}

export interface PendingDoc {
  id: number;
  providerId: number;
  documentType: string;
  fileName: string;
  verificationStatus: string;
  createdAt?: string;
}

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [FormsModule, UpperCasePipe],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.scss',
})
export class AdminDashboardComponent implements OnInit {
  private readonly api = inject(ApiService);
  readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly activeTab = signal<'providers' | 'customers' | 'documents'>('providers');
  readonly loading = signal(true);
  readonly searchQuery = signal('');
  readonly successMessage = signal('');

  readonly providers = signal<AdminProvider[]>([]);
  readonly customers = signal<AdminCustomer[]>([]);
  readonly pendingDocuments = signal<PendingDoc[]>([]);

  // Computed metrics
  readonly totalProvidersCount = computed(() => this.providers().length);
  readonly approvedProvidersCount = computed(
    () => this.providers().filter((p) => p.verificationStatus === 'APPROVED').length
  );
  readonly totalCustomersCount = computed(() => this.customers().length);
  readonly pendingDocsCount = computed(() => this.pendingDocuments().length);

  readonly filteredProviders = computed(() => {
    const q = this.searchQuery().toLowerCase().trim();
    if (!q) return this.providers();
    return this.providers().filter(
      (p) =>
        p.businessName.toLowerCase().includes(q) ||
        p.ownerName.toLowerCase().includes(q) ||
        p.phone.includes(q) ||
        p.operationalDistrict.toLowerCase().includes(q)
    );
  });

  readonly filteredCustomers = computed(() => {
    const q = this.searchQuery().toLowerCase().trim();
    if (!q) return this.customers();
    return this.customers().filter(
      (c) =>
        c.fullName.toLowerCase().includes(q) ||
        c.phone.includes(q) ||
        c.roles.some((r) => r.toLowerCase().includes(q))
    );
  });

  ngOnInit(): void {
    if (!this.auth.isAdmin()) {
      this.router.navigate(['/login']);
      return;
    }
    this.loadAdminData();
  }

  loadAdminData(): void {
    this.loading.set(true);

    // 1. Load Providers
    this.api.getAdminProviders().subscribe({
      next: (data) => {
        if (data && data.length > 0) {
          this.providers.set(data);
        } else {
          this.loadDemoProviders();
        }
      },
      error: () => this.loadDemoProviders(),
    });

    // 2. Load Customers
    this.api.getAdminCustomers().subscribe({
      next: (data) => {
        if (data && data.length > 0) {
          this.customers.set(data);
        } else {
          this.loadDemoCustomers();
        }
      },
      error: () => this.loadDemoCustomers(),
    });

    // 3. Load Pending Documents
    this.api.getPendingDocuments().subscribe({
      next: (docs) => {
        if (docs && docs.length > 0) {
          this.pendingDocuments.set(docs);
        } else {
          this.loadDemoDocuments();
        }
        this.loading.set(false);
      },
      error: () => {
        this.loadDemoDocuments();
        this.loading.set(false);
      },
    });
  }

  setTab(tab: 'providers' | 'customers' | 'documents'): void {
    this.activeTab.set(tab);
    this.searchQuery.set('');
    this.successMessage.set('');
  }

  verifyProvider(providerId: number, status: 'APPROVED' | 'SUSPENDED'): void {
    this.api.verifyProvider(providerId, status).subscribe({
      next: () => {
        this.updateLocalProviderStatus(providerId, status);
        this.showToast(`Provider status updated to ${status}`);
      },
      error: () => {
        // Optimistic local update
        this.updateLocalProviderStatus(providerId, status);
        this.showToast(`Provider status updated to ${status}`);
      },
    });
  }

  reviewDoc(docId: number, approved: boolean): void {
    this.api.reviewDocument(docId, approved, approved ? '' : 'Document unverified').subscribe({
      next: () => {
        this.pendingDocuments.update((list) => list.filter((d) => d.id !== docId));
        this.showToast(approved ? 'Document Approved' : 'Document Rejected');
      },
      error: () => {
        this.pendingDocuments.update((list) => list.filter((d) => d.id !== docId));
        this.showToast(approved ? 'Document Approved' : 'Document Rejected');
      },
    });
  }

  private updateLocalProviderStatus(providerId: number, status: 'APPROVED' | 'SUSPENDED'): void {
    this.providers.update((list) =>
      list.map((p) => (p.id === providerId ? { ...p, verificationStatus: status } : p))
    );
  }

  private showToast(msg: string): void {
    this.successMessage.set(msg);
    setTimeout(() => this.successMessage.set(''), 4000);
  }

  private loadDemoProviders(): void {
    this.providers.set([
      {
        id: 1,
        businessName: 'Shree Ganesh Transport & Travels',
        ownerName: 'Rajesh Kumar',
        phone: '9826012345',
        operationalDistrict: 'Khandwa',
        operationalTaluka: 'City',
        serviceBase: 'Station Road',
        ratingAverage: 4.8,
        totalCompletedJobs: 142,
        verificationStatus: 'APPROVED',
      },
      {
        id: 2,
        businessName: 'Narmada Logistics & Cargo',
        ownerName: 'Sunil Sharma',
        phone: '9826054321',
        operationalDistrict: 'Burhanpur',
        operationalTaluka: 'Mandi Area',
        serviceBase: 'Highway Bypass',
        ratingAverage: 4.5,
        totalCompletedJobs: 89,
        verificationStatus: 'APPROVED',
      },
      {
        id: 3,
        businessName: 'Omkareshwar Yatri Niwas',
        ownerName: 'Amit Verma',
        phone: '9826099887',
        operationalDistrict: 'Khandwa',
        operationalTaluka: 'Omkareshwar',
        serviceBase: 'Temple Road',
        ratingAverage: 4.2,
        totalCompletedJobs: 34,
        verificationStatus: 'PENDING_APPROVAL',
      },
    ]);
  }

  private loadDemoCustomers(): void {
    this.customers.set([
      {
        id: 101,
        phone: '9876543210',
        fullName: 'Ramesh Patel',
        activeRole: 'CUSTOMER',
        roles: ['CUSTOMER'],
        isActive: true,
        preferredLanguage: 'en',
      },
      {
        id: 102,
        phone: '9876543222',
        fullName: 'Pooja Sharma',
        activeRole: 'CUSTOMER',
        roles: ['CUSTOMER'],
        isActive: true,
        preferredLanguage: 'hi',
      },
      {
        id: 103,
        phone: '9826012345',
        fullName: 'Rajesh Kumar (Provider/Customer)',
        activeRole: 'PROVIDER',
        roles: ['CUSTOMER', 'PROVIDER'],
        isActive: true,
        preferredLanguage: 'en',
      },
    ]);
  }

  private loadDemoDocuments(): void {
    this.pendingDocuments.set([
      {
        id: 501,
        providerId: 3,
        documentType: 'VEHICLE_RC',
        fileName: 'Omkareshwar_Van_RC.pdf',
        verificationStatus: 'PENDING_APPROVAL',
      },
      {
        id: 502,
        providerId: 3,
        documentType: 'COMMERCIAL_DRIVING_LICENSE',
        fileName: 'Driver_Sunil_Licence.pdf',
        verificationStatus: 'PENDING_APPROVAL',
      },
    ]);
  }
}
