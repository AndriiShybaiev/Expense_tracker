import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {AuthService} from '@core/services/auth.service';
import {ProfileService} from './services/profile.service';
import {Router} from '@angular/router';

@Component({
  selector: 'app-profile',
  imports: [
    ReactiveFormsModule
  ],
  templateUrl: './profile.html',
  styleUrl: './profile.scss'
})
export class Profile implements OnInit {
  form!: FormGroup;
  user!: any;

  constructor(
    private fb: FormBuilder,
    private profileService: ProfileService,
    private auth: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.profileService.getProfile().subscribe(user => {
      this.user = user;
      this.form = this.fb.group({
        username: [user.username, Validators.required],
        email: [user.email, [Validators.required, Validators.email]],
        password: ['']
      });
    });
  }

  onSave() {
    if (this.form.valid) {
      const updates = this.form.value;
      // Don't send empty password
      if (!updates.password) {
        delete updates.password;
      }

      this.profileService.patchProfile(updates).subscribe(updated => {
        this.user = updated;
        alert('Profile updated');
      });
    }
  }

  onDelete() {
    if (confirm('Delete account?')) {
      this.profileService.deleteProfile().subscribe(() => {
        this.auth.logout();
        this.router.navigate(['/login']);
      });
    }
  }
}


