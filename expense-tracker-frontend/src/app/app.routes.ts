import { Routes } from '@angular/router';
import { Landing } from './landing/landing';

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
    path: '**',
    redirectTo: 'auth/login',
  },
];
