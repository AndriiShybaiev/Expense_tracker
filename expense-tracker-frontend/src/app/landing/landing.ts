import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-landing',
  templateUrl: './landing.html',
  styleUrls: ['./landing.scss']
})
export class Landing {
  constructor(private router: Router) {}

  goLogin() { this.router.navigate(['/auth/login']); }
  goRegister() { this.router.navigate(['/auth/register']); }
}
