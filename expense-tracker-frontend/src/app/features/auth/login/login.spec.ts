import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Login } from './login';
import { AuthService } from '../../../core/services/auth.service';
import { of } from 'rxjs';

describe('Login Component', () => {
  let component: Login;
  let fixture: ComponentFixture<Login>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    const spy = jasmine.createSpyObj('AuthService', ['login']);

    await TestBed.configureTestingModule({
      imports: [Login, ReactiveFormsModule],
      providers: [{ provide: AuthService, useValue: spy }]
    }).compileComponents();

    fixture = TestBed.createComponent(Login);
    component = fixture.componentInstance;
    authServiceSpy = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    fixture.detectChanges();
  });

  it('should create the form with email and password fields', () => {
    expect(component.form.contains('email')).toBeTrue();
    expect(component.form.contains('password')).toBeTrue();
  });

  it('should not call login if form is invalid', () => {
    component.form.setValue({ email: '', password: '' });
    component.onSubmit();
    expect(authServiceSpy.login).not.toHaveBeenCalled();
  });

  it('should call login if form is valid', () => {
    authServiceSpy.login.and.returnValue(of({ token: 'fake-jwt' }));
    component.form.setValue({ email: 'test@test.com', password: '123456' });
    component.onSubmit();
    expect(authServiceSpy.login).toHaveBeenCalledWith({ email: 'test@test.com', password: '123456' });
  });

});
