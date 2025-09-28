import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '@core/services/auth.service';

@Component({
  selector: 'app-dashboard',
  imports: [],
  templateUrl: 'dashboard.html',
  styleUrl: 'dashboard.scss'
})
export class Dashboard {
  username = '';

  constructor(private authService: AuthService, private router: Router) {
    this.username = this.authService.getUsername() ?? 'User';
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/']);
  }
}
