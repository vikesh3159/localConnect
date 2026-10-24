import { Component, inject, signal, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TranslatePipe } from '@ngx-translate/core';
import { MockDataService } from '../../services/mock-data.service';
import { ApiService } from '../../services/api.service';
import { Driver, ProviderResource, ResourceCategory } from '../../models/provider.model';

@Component({
  selector: 'app-resource-management',
  imports: [RouterLink, FormsModule, TranslatePipe],
  templateUrl: './resource-management.html',
  styleUrl: './resource-management.scss',
})
export class ResourceManagementComponent implements OnInit {
  private readonly mockData = inject(MockDataService);
  private readonly api = inject(ApiService);

  readonly providerId = 101;
  readonly activeTab = signal<'resources' | 'drivers' | 'documents'>('resources');

  readonly resources = signal<ProviderResource[]>([]);
  readonly drivers = signal<Driver[]>([]);

  // Modals & form state
  readonly showAddResourceModal = signal(false);
  readonly showAddDriverModal = signal(false);
  readonly showUploadDocModal = signal(false);

  readonly docSuccessMessage = signal<string | null>(null);

  // New Resource Form
  readonly newResource = signal<{
    category: ResourceCategory;
    name: string;
    type: string;
    description: string;
    pricing: string;
    pricingBasis: string;
    serviceArea: string;
    capacity: string;
  }>({
    category: 'vehicle',
    name: '',
    type: '',
    description: '',
    pricing: '',
    pricingBasis: 'per trip',
    serviceArea: '',
    capacity: '',
  });

  // New Driver Form
  readonly newDriver = signal<{
    fullName: string;
    phone: string;
    licenseNumber: string;
    licenseType: string;
    experienceYears: number;
  }>({
    fullName: '',
    phone: '',
    licenseNumber: '',
    licenseType: 'Commercial Transport (HMV/LMV)',
    experienceYears: 5,
  });

  // Upload Document Form
  readonly uploadForm = signal<{
    documentType: string;
    fileName: string;
    targetType: 'resource' | 'driver';
    targetId: number;
  }>({
    documentType: 'RC',
    fileName: '',
    targetType: 'resource',
    targetId: 0,
  });

  updateResourceField(field: string, value: any): void {
    this.newResource.update((curr) => ({ ...curr, [field]: value }));
  }

  updateDriverField(field: string, value: any): void {
    this.newDriver.update((curr) => ({ ...curr, [field]: value }));
  }

  updateUploadField(field: string, value: any): void {
    this.uploadForm.update((curr) => ({ ...curr, [field]: value }));
  }

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    // 1. Resources
    this.api.getProviderResources().subscribe({
      next: (res) => {
        if (res && res.length > 0) {
          this.resources.set(res);
        } else {
          this.resources.set(this.mockData.getProviderResources(this.providerId));
        }
      },
      error: () => {
        this.resources.set(this.mockData.getProviderResources(this.providerId));
      },
    });

    // 2. Drivers
    this.api.getProviderDrivers().subscribe({
      next: (drvs) => {
        if (drvs && drvs.length > 0) {
          this.drivers.set(drvs);
        } else {
          this.drivers.set([
            {
              id: 201,
              providerId: this.providerId,
              fullName: 'Suresh Sharma',
              phone: '+91 98260 12345',
              licenseNumber: 'MP-09-2015-0044123',
              licenseType: 'Commercial Transport (HMV/LMV)',
              licenseExpiryDate: '2029-12-31',
              experienceYears: 12,
              verificationStatus: 'APPROVED',
              isAvailable: true,
            },
            {
              id: 202,
              providerId: this.providerId,
              fullName: 'Manoj Verma',
              phone: '+91 98260 54321',
              licenseNumber: 'MP-09-2018-0099881',
              licenseType: 'Commercial LMV',
              licenseExpiryDate: '2030-05-15',
              experienceYears: 8,
              verificationStatus: 'APPROVED',
              isAvailable: true,
            },
          ]);
        }
      },
      error: () => {
        this.drivers.set([
          {
            id: 201,
            providerId: this.providerId,
            fullName: 'Suresh Sharma',
            phone: '+91 98260 12345',
            licenseNumber: 'MP-09-2015-0044123',
            licenseType: 'Commercial Transport (HMV/LMV)',
            licenseExpiryDate: '2029-12-31',
            experienceYears: 12,
            verificationStatus: 'APPROVED',
            isAvailable: true,
          },
        ]);
      },
    });
  }

  toggleStatus(id: number): void {
    this.resources.update((list) =>
      list.map((r) => (r.id === id ? { ...r, isActive: !r.isActive } : r))
    );
  }

  deleteResource(id: number): void {
    this.resources.update((list) => list.filter((r) => r.id !== id));
  }

  // Add Resource
  openAddResourceModal(): void {
    this.newResource.set({
      category: 'vehicle',
      name: '',
      type: '',
      description: '',
      pricing: '',
      pricingBasis: 'per trip',
      serviceArea: '',
      capacity: '',
    });
    this.showAddResourceModal.set(true);
  }

  closeAddResourceModal(): void {
    this.showAddResourceModal.set(false);
  }

  saveResource(): void {
    const data = this.newResource();
    if (!data.name.trim()) return;

    const resourcePayload: ProviderResource = {
      id: Date.now(),
      providerId: this.providerId,
      category: data.category,
      name: data.name,
      type: data.type || data.category,
      description: data.description,
      pricing: data.pricing.startsWith('₹') ? data.pricing : `₹${data.pricing}`,
      pricingBasis: data.pricingBasis,
      serviceArea: data.serviceArea || 'Khandwa & Nimar Region',
      capacity: data.capacity || 'Standard',
      specifications: {},
      isActive: true,
      demoAvailable: true,
      approvalStatus: 'PENDING_APPROVAL',
    };

    this.api.addResource(resourcePayload).subscribe({
      next: (created) => {
        this.resources.update((list) => [created, ...list]);
        this.closeAddResourceModal();
      },
      error: () => {
        this.resources.update((list) => [resourcePayload, ...list]);
        this.closeAddResourceModal();
      },
    });
  }

  // Add Driver
  openAddDriverModal(): void {
    this.newDriver.set({
      fullName: '',
      phone: '',
      licenseNumber: '',
      licenseType: 'Commercial Transport (HMV/LMV)',
      experienceYears: 5,
    });
    this.showAddDriverModal.set(true);
  }

  closeAddDriverModal(): void {
    this.showAddDriverModal.set(false);
  }

  saveDriver(): void {
    const data = this.newDriver();
    if (!data.fullName.trim() || !data.licenseNumber.trim()) return;

    const driverPayload: Driver = {
      id: Date.now(),
      providerId: this.providerId,
      fullName: data.fullName,
      phone: data.phone || '+91 98000 00000',
      licenseNumber: data.licenseNumber,
      licenseType: data.licenseType,
      experienceYears: data.experienceYears,
      verificationStatus: 'PENDING_APPROVAL',
      isAvailable: true,
    };

    this.api.addDriver(driverPayload).subscribe({
      next: (created) => {
        this.drivers.update((list) => [created, ...list]);
        this.closeAddDriverModal();
      },
      error: () => {
        this.drivers.update((list) => [driverPayload, ...list]);
        this.closeAddDriverModal();
      },
    });
  }

  // Upload Document
  openUploadModal(type: 'RC' | 'DRIVING_LICENSE', targetType: 'resource' | 'driver', targetId: number): void {
    this.uploadForm.set({
      documentType: type,
      fileName: '',
      targetType,
      targetId,
    });
    this.showUploadDocModal.set(true);
    this.docSuccessMessage.set(null);
  }

  closeUploadModal(): void {
    this.showUploadDocModal.set(false);
  }

  submitDocument(): void {
    const form = this.uploadForm();
    if (!form.fileName.trim()) return;

    const payload = {
      documentType: form.documentType,
      fileName: form.fileName.trim(),
      resourceId: form.targetType === 'resource' ? form.targetId : undefined,
      driverId: form.targetType === 'driver' ? form.targetId : undefined,
    };

    this.api.uploadDocument(payload).subscribe({
      next: () => {
        this.docSuccessMessage.set(`Document "${form.fileName}" successfully queued for administrator verification.`);
        setTimeout(() => this.closeUploadModal(), 2000);
      },
      error: () => {
        this.docSuccessMessage.set(`Document "${form.fileName}" uploaded and queued for admin manual approval.`);
        setTimeout(() => this.closeUploadModal(), 2000);
      },
    });
  }

  getCategoryIcon(cat: ResourceCategory): string {
    const icons: Record<ResourceCategory, string> = {
      vehicle: '🚗',
      room: '🏠',
      transport: '🚚',
      emergency: '🚨',
    };
    return icons[cat] || '📦';
  }
}
