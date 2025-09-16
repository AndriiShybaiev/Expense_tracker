import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <h2>Register</h2>
    <form [formGroup]="form" (ngSubmit)="onSubmit()">
      <label>
        Email:
        <input type="email" formControlName="email">
      </label>
      <div *ngIf="form.get('email')?.invalid && form.get('email')?.touched">
        Valid email is required
      </div>

      <label>
        Password:
        <input type="password" formControlName="password">
      </label>
      <div *ngIf="form.get('password')?.invalid && form.get('password')?.touched">
        Password is required (min 6 chars)
      </div>

      <label>
        Confirm Password:
        <input type="password" formControlName="confirmPassword">
      </label>
      <div *ngIf="form.get('confirmPassword')?.invalid && form.get('confirmPassword')?.touched">
        Please confirm your password
      </div>
      <div *ngIf="form.errors?.['mismatch'] && form.get('confirmPassword')?.touched">
        Passwords do not match
      </div>

      <button type="submit" [disabled]="form.invalid">Register</button>
    </form>
  `,
})
export class RegisterComponent {
  form: FormGroup;

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group(
      {
        email: ['', [Validators.required, Validators.email]],
        password: ['', [Validators.required, Validators.minLength(6)]],
        confirmPassword: ['', Validators.required],
      },
      { validators: this.passwordsMatchValidator }
    );
  }

  private passwordsMatchValidator(group: FormGroup) {
    const pass = group.get('password')?.value;
    const confirm = group.get('confirmPassword')?.value;
    return pass === confirm ? null : { mismatch: true };
  }

  onSubmit() {
    if (this.form.valid) {
      console.log('Register Value:', this.form.value);
      // здесь можно вызвать AuthService.register(...)
    }
  }
}
