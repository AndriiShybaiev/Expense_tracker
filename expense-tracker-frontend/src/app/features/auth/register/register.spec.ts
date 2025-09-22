import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Register } from './register';
import { AuthService } from '../../../core/services/auth.service';
import {of} from 'rxjs';

class MockAuthService {
  register() {
    return { subscribe: () => {} };
  }
}

describe('Register Component', () => {
  let component: Register;
  let fixture: ComponentFixture<Register>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, Register],
      providers: [{ provide: AuthService, useClass: MockAuthService }]
    }).compileComponents();

    fixture = TestBed.createComponent(Register);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should have invalid form initially', () => {
    expect(component.form.valid).toBeFalse();
  });

  it('should require email, password and confirmPassword', () => {
    const form = component.form;
    expect(form.get('email')?.hasError('required')).toBeTrue();
    expect(form.get('password')?.hasError('required')).toBeTrue();
    expect(form.get('confirmPassword')?.hasError('required')).toBeTrue();
  });

  it('should validate email format', () => {
    component.form.get('email')?.setValue('invalid-email');
    expect(component.form.get('email')?.hasError('email')).toBeTrue();

    component.form.get('email')?.setValue('valid@example.com');
    expect(component.form.get('email')?.hasError('email')).toBeFalse();
  });

  it('should validate password match', () => {
    component.form.get('password')?.setValue('123456');
    component.form.get('confirmPassword')?.setValue('654321');
    expect(component.form.hasError('mismatch')).toBeTrue();

    component.form.get('confirmPassword')?.setValue('123456');
    expect(component.form.hasError('mismatch')).toBeFalse();
  });

  it('should call authService.register if form is valid', () => {
    const authService = TestBed.inject(AuthService);
    spyOn(authService, 'register').and.returnValue(of({ token: 'dummy' }));

    component.form.setValue({
      email: 'test@example.com',
      username: 'testuser',
      password: '123456',
      confirmPassword: '123456'
    });
    component.form.updateValueAndValidity();
    fixture.detectChanges();

    component.onSubmit();

    expect(authService.register).toHaveBeenCalledWith({
      email: 'test@example.com',
      username: 'testuser',
      password: '123456'
    });
  });
});
