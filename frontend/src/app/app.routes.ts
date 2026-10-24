import { Routes } from '@angular/router';
import { adminGuard } from './guards/admin.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/home/home').then((m) => m.HomeComponent),
  },
  {
    path: 'vehicle-search',
    loadComponent: () => import('./pages/vehicle-search/vehicle-search').then((m) => m.VehicleSearchComponent),
  },
  {
    path: 'vehicle-results',
    loadComponent: () => import('./pages/vehicle-results/vehicle-results').then((m) => m.VehicleResultsComponent),
  },
  {
    path: 'vehicles',
    redirectTo: 'vehicle-results',
    pathMatch: 'full',
  },
  {
    path: 'room-search',
    loadComponent: () => import('./pages/room-search/room-search').then((m) => m.RoomSearchComponent),
  },
  {
    path: 'room-results',
    loadComponent: () => import('./pages/room-results/room-results').then((m) => m.RoomResultsComponent),
  },
  {
    path: 'rooms',
    redirectTo: 'room-results',
    pathMatch: 'full',
  },
  {
    path: 'transport-search',
    loadComponent: () => import('./pages/transport-search/transport-search').then((m) => m.TransportSearchComponent),
  },
  {
    path: 'transport-results',
    loadComponent: () => import('./pages/transport-results/transport-results').then((m) => m.TransportResultsComponent),
  },
  {
    path: 'transport',
    redirectTo: 'transport-results',
    pathMatch: 'full',
  },
  {
    path: 'emergency',
    loadComponent: () => import('./pages/emergency/emergency').then((m) => m.EmergencyComponent),
  },
  {
    path: 'details/:category/:id',
    loadComponent: () => import('./pages/provider-details/provider-details').then((m) => m.ProviderDetailsComponent),
  },
  {
    path: 'request-summary',
    loadComponent: () => import('./pages/request-summary/request-summary').then((m) => m.RequestSummaryComponent),
  },
  {
    path: 'request-confirmation/:ref',
    loadComponent: () => import('./pages/request-confirmation/request-confirmation').then((m) => m.RequestConfirmationComponent),
  },
  {
    path: 'my-requests',
    loadComponent: () => import('./pages/my-requests/my-requests').then((m) => m.MyRequestsComponent),
  },
  {
    path: 'provider-dashboard',
    loadComponent: () => import('./pages/provider-dashboard/provider-dashboard').then((m) => m.ProviderDashboardComponent),
  },
  {
    path: 'admin',
    loadComponent: () => import('./pages/admin-dashboard/admin-dashboard').then((m) => m.AdminDashboardComponent),
    canActivate: [adminGuard],
  },
  {
    path: 'admin-dashboard',
    redirectTo: 'admin',
    pathMatch: 'full',
  },
  {
    path: 'resource-management',
    loadComponent: () => import('./pages/resource-management/resource-management').then((m) => m.ResourceManagementComponent),
  },
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login').then((m) => m.LoginComponent),
  },
  {
    path: '**',
    loadComponent: () => import('./pages/not-found/not-found').then((m) => m.NotFoundComponent),
  },
];
