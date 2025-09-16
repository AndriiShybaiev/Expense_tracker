import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <h2>Login</h2>
    <form [formGroup]="form" (ngSubmit)="onSubmit()">
      <label>
        Email:
        <input type="email" formControlName="email">
      </label>
      <div *ngIf="form.get('email')?.invalid && form.get('email')?.touched">
        Email is required
      </div>

      <label>
        Password:
        <input type="password" formControlName="password">
      </label>
      <div *ngIf="form.get('password')?.invalid && form.get('password')?.touched">
        Password is required
      </div>

      <button type="submit" [disabled]="form.invalid">Login</button>
    </form>
  `,
})
export class LoginComponent {
  form: FormGroup;

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
    });
  }

  onSubmit() {
    if (this.form.valid) {
      console.log('Form Value:', this.form.value);
      // late to invoke AuthService.login(...) here
    }
  }
}
