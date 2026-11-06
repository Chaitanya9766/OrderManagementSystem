import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  login(username: string, password: string): Observable<string> {
    const body = { username, password };
    return this.http.post(`${this.baseUrl}/auth/login`, body, { responseType: 'text' });
  }

  getChatResponse(message: string): Observable<string> {
    const token = localStorage.getItem('jwt_token') || '';
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
    
    return this.http.get(`${this.baseUrl}/chat?message=${encodeURIComponent(message)}`, {
      headers,
      responseType: 'text'
    });
  }
}