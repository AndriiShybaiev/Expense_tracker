import { Routes } from '@angular/router';
import { Landing } from './landing/landing';
import {Dashboard} from './dashboard/dashboard';

export const routes: Routes = [
  {
    path: 'auth',
    loadChildren: () =>
      import('./features/auth/auth-module').then((m) => m.AuthModule),
  },
  {
    path: '',
    component: Landing
  },
  {
    path: 'dashboard',
    component: Dashboard
  },
  {
    path: '**',
    redirectTo: 'auth/login',
  },
];
