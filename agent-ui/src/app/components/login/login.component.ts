import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div style="display: flex; justify-content: center; align-items: center; height: 100vh; background: #f4f7f6; font-family: Arial, sans-serif;">
      <div style="background: white; padding: 30px; border-radius: 8px; box-shadow: 0 4px 10px rgba(0,0,0,0.1); width: 300px;">
        <h2>Agentic AI Login</h2>
        <div *ngIf="errorMessage" style="color: red; margin-bottom: 15px; font-size: 14px;">{{ errorMessage }}</div>
        <form (ngSubmit)="onLogin()">
          <div style="margin-bottom: 15px;">
            <label style="display: block; margin-bottom: 5px;">Username</label>
            <input type="text" [(ngModel)]="username" name="username" required style="width: 100%; padding: 8px; box-sizing: border-box;" />
          </div>
          <div style="margin-bottom: 20px;">
            <label style="display: block; margin-bottom: 5px;">Password</label>
            <input type="password" [(ngModel)]="password" name="password" required style="width: 100%; padding: 8px; box-sizing: border-box;" />
          </div>
          <button type="submit" style="width: 100%; padding: 10px; background: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer;">Sign In</button>
        </form>
      </div>
    </div>
  `
})
export class LoginComponent {
  username = 'admin';
  password = 'password123';
  errorMessage = '';

  constructor(private apiService: ApiService, private router: Router) {}

  onLogin() {
    this.errorMessage = '';
    this.apiService.login(this.username, this.password).subscribe({
      next: (token) => {
        localStorage.setItem('jwt_token', token);
        this.router.navigate(['/chat']);
      },
      error: () => {
        this.errorMessage = 'Invalid username or password.';
      }
    });
  }
}