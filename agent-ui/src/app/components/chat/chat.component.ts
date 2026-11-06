import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div style="max-width: 600px; margin: 40px auto; font-family: Arial, sans-serif;">
      <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #ccc; padding-bottom: 10px;">
        <h2>Agentic AI Assistant</h2>
        <button (click)="logout()" style="padding: 6px 12px; background: #dc3545; color: white; border: none; border-radius: 4px; cursor: pointer;">Logout</button>
      </div>

      <div style="border: 1px solid #ccc; height: 400px; overflow-y: scroll; padding: 15px; margin: 20px 0; background: #f9f9f9; display: flex; flex-direction: column; gap: 10px;">
        <div *ngFor="let msg of messages" [style.align-self]="msg.sender === 'user' ? 'flex-end' : 'flex-start'"
             [style.background]="msg.sender === 'user' ? '#007bff' : '#e2e3e5'"
             [style.color]="msg.sender === 'user' ? 'white' : 'black'"
             style="padding: 10px 15px; border-radius: 8px; max-width: 75%; word-break: break-word;">
          {{ msg.text }}
        </div>
      </div>

      <form (ngSubmit)="sendMessage()" style="display: flex; gap: 10px;">
        <input [(ngModel)]="userInput" name="userInput" placeholder="Ask AI about your orders..." style="flex: 1; padding: 10px; border: 1px solid #ccc; border-radius: 4px;" />
        <button type="submit" style="padding: 10px 20px; background: #28a745; color: white; border: none; border-radius: 4px; cursor: pointer;">Send</button>
      </form>
    </div>
  `
})
export class ChatComponent {
  messages = [
    { sender: 'ai', text: 'Hello! I am your store AI assistant powered by Ollama. How can I help you today?' }
  ];
  userInput = '';

  constructor(private apiService: ApiService, private router: Router) {}

  sendMessage() {
    if (!this.userInput.trim()) return;
    const query = this.userInput;
    this.messages.push({ sender: 'user', text: query });
    this.userInput = '';

    this.apiService.getChatResponse(query).subscribe({
      next: (res) => {
        this.messages.push({ sender: 'ai', text: res });
      },
      error: () => {
        this.messages.push({ sender: 'ai', text: 'Error: Session expired or backend unreachable.' });
      }
    });
  }

  logout() {
    localStorage.removeItem('jwt_token');
    this.router.navigate(['/login']);
  }
}