import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  BookingAllocation,
  ProviderOffer,
  RequestRemedyPayload,
  ServiceRequest,
} from '../models/request.model';
import { Driver, Provider, ProviderResource } from '../models/provider.model';
import { EmergencyAlertRecord, StatutoryHelpline } from '../models/emergency.model';

@Injectable({
  providedIn: 'root',
})
export class ApiService {
  private readonly baseUrl = 'http://localhost:8080/api/v1';

  constructor(private http: HttpClient) {}

  // 1. Resources (Public)
  searchResources(category: string, subType?: string): Observable<ProviderResource[]> {
    let params = new HttpParams().set('category', category.toUpperCase());
    if (subType) {
      params = params.set('subType', subType);
    }
    return this.http.get<ProviderResource[]>(`${this.baseUrl}/resources/search`, { params });
  }

  // 2. Service Requests
  createRequest(payload: any): Observable<ServiceRequest> {
    return this.http.post<ServiceRequest>(`${this.baseUrl}/requests`, payload);
  }

  getMyRequests(): Observable<ServiceRequest[]> {
    return this.http.get<ServiceRequest[]>(`${this.baseUrl}/requests/my`);
  }

  getRequestById(id: string | number): Observable<ServiceRequest> {
    return this.http.get<ServiceRequest>(`${this.baseUrl}/requests/${id}`);
  }

  applyRemedy(requestId: string | number, remedy: RequestRemedyPayload): Observable<ServiceRequest> {
    return this.http.post<ServiceRequest>(`${this.baseUrl}/requests/${requestId}/remedy`, remedy);
  }

  // 3. Provider Quotations & Two-Stage Nomination
  submitQuotation(offerPayload: {
    requestId: number;
    offeredQuantity: number;
    quotedAmount: number;
    includesDriver: boolean;
  }): Observable<ProviderOffer> {
    return this.http.post<ProviderOffer>(`${this.baseUrl}/offers`, offerPayload);
  }

  getOffersForRequest(requestId: string | number): Observable<ProviderOffer[]> {
    return this.http.get<ProviderOffer[]>(`${this.baseUrl}/offers/request/${requestId}`);
  }

  getProviderOffers(): Observable<ProviderOffer[]> {
    return this.http.get<ProviderOffer[]>(`${this.baseUrl}/offers/provider`);
  }

  selectOfferProvisionally(offerId: string | number): Observable<ProviderOffer> {
    return this.http.post<ProviderOffer>(`${this.baseUrl}/offers/${offerId}/select`, {});
  }

  nominateResource(
    offerId: string | number,
    nomination: { resourceId: number; driverId: number }
  ): Observable<ProviderOffer> {
    return this.http.post<ProviderOffer>(`${this.baseUrl}/offers/${offerId}/nominate`, nomination);
  }

  // 4. Booking Allocations (Stage 3 Confirmation)
  confirmAllocation(offerId: string | number): Observable<BookingAllocation> {
    return this.http.post<BookingAllocation>(`${this.baseUrl}/allocations/confirm/${offerId}`, {});
  }

  getMyAllocations(): Observable<BookingAllocation[]> {
    return this.http.get<BookingAllocation[]>(`${this.baseUrl}/allocations/my`);
  }

  getProviderAllocations(): Observable<BookingAllocation[]> {
    return this.http.get<BookingAllocation[]>(`${this.baseUrl}/allocations/provider`);
  }

  // 5. Emergency Services (Truthful States & Direct Helplines)
  getStatutoryHelplines(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/emergency/helplines`);
  }

  recordHelplineDial(number: string, phone?: string, location?: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.baseUrl}/emergency/dial`, { number, phone, location });
  }

  broadcastEmergencyAlert(payload: {
    emergencyType: string;
    contactPhone: string;
    callerLocation: string;
    callerLandmark?: string;
    notes?: string;
  }): Observable<EmergencyAlertRecord> {
    return this.http.post<EmergencyAlertRecord>(`${this.baseUrl}/emergency/broadcast`, payload);
  }

  acknowledgeEmergencyAlert(id: string | number): Observable<EmergencyAlertRecord> {
    return this.http.post<EmergencyAlertRecord>(`${this.baseUrl}/emergency/${id}/ack`, {});
  }

  confirmEmergencyDispatch(
    id: string | number,
    dispatch: { etaMinutes: number; vehiclePlate: string; notes?: string }
  ): Observable<EmergencyAlertRecord> {
    return this.http.post<EmergencyAlertRecord>(`${this.baseUrl}/emergency/${id}/dispatch`, dispatch);
  }

  // 6. Provider Fleet & Drivers
  getProviderProfile(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/provider/profile`);
  }

  updateProviderProfile(profile: any): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/provider/profile`, profile);
  }

  addResource(resource: any): Observable<ProviderResource> {
    return this.http.post<ProviderResource>(`${this.baseUrl}/provider/resources`, resource);
  }

  getProviderResources(): Observable<ProviderResource[]> {
    return this.http.get<ProviderResource[]>(`${this.baseUrl}/provider/resources`);
  }

  addDriver(driver: any): Observable<Driver> {
    return this.http.post<Driver>(`${this.baseUrl}/provider/drivers`, driver);
  }

  getProviderDrivers(): Observable<Driver[]> {
    return this.http.get<Driver[]>(`${this.baseUrl}/provider/drivers`);
  }

  uploadDocument(docPayload: {
    documentType: string;
    fileName: string;
    resourceId?: number;
    driverId?: number;
  }): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.baseUrl}/provider/documents`, docPayload);
  }

  // 7. Administrator Management & Review
  getPendingDocuments(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/admin/documents/pending`);
  }

  reviewDocument(documentId: number, approved: boolean, rejectionReason?: string): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/admin/documents/${documentId}/review`, {
      approved,
      rejectionReason,
    });
  }

  getAdminProviders(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/admin/providers`);
  }

  getAdminCustomers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/admin/customers`);
  }

  verifyProvider(providerId: number, status: string = 'APPROVED'): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/admin/providers/${providerId}/verify`, { status });
  }
}

