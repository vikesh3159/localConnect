import { Injectable, signal, computed } from '@angular/core';
import { UserRequest, RequestCategory, RequestStatus } from '../models/request.model';

@Injectable({ providedIn: 'root' })
export class RequestStateService {
  private readonly STORAGE_KEY = 'localconnect-requests';
  private readonly requestsSignal = signal<UserRequest[]>(this.loadFromStorage());

  readonly requests = this.requestsSignal.asReadonly();
  readonly requestCount = computed(() => this.requestsSignal().length);

  createRequest(category: RequestCategory, summary: Record<string, string>, providerName: string, resourceName: string): UserRequest {
    const request: UserRequest = {
      id: crypto.randomUUID(),
      referenceNumber: 'LC-' + Date.now().toString(36).toUpperCase(),
      category,
      status: 'submitted',
      createdAt: new Date().toISOString(),
      summary,
      providerName,
      resourceName,
    };
    this.requestsSignal.update(list => [request, ...list]);
    this.saveToStorage();
    return request;
  }

  getRequestById(id: string): UserRequest | undefined {
    return this.requestsSignal().find(r => r.id === id);
  }

  getRequestByRef(ref: string): UserRequest | undefined {
    return this.requestsSignal().find(r => r.referenceNumber === ref);
  }

  cancelRequest(id: string): void {
    this.requestsSignal.update(list =>
      list.map(r => r.id === id ? { ...r, status: 'cancelled' as RequestStatus } : r)
    );
    this.saveToStorage();
  }

  private loadFromStorage(): UserRequest[] {
    try {
      const data = localStorage.getItem(this.STORAGE_KEY);
      return data ? JSON.parse(data) : [];
    } catch {
      return [];
    }
  }

  private saveToStorage(): void {
    localStorage.setItem(this.STORAGE_KEY, JSON.stringify(this.requestsSignal()));
  }
}
