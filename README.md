# Agentic Order Management System

A full-stack, AI-powered enterprise order management platform integrating a secure Spring Boot backend with an Angular 17 reactive frontend, leveraging local LLMs (Ollama) and function/tool-calling architecture for automated data retrieval.

---

## 🚀 Tech Stack

### **Backend**
* **Java 17+** / **Spring Boot**
* **Spring Security & JWT** (Stateless authentication and authorization)
* **Spring AI / Ollama Integration** (Local LLM execution with tool-calling capabilities)
* **Maven** (Dependency management)

### **Frontend**
* **Angular 17+** (Standalone components and reactive routing)
* **TypeScript** & **RxJS**
* **Angular HttpClient** (Interceptor-driven token attachment and CORS management)

---

## 📂 Project Architecture (Monorepo)

```text
OrderManagementSystem/
│
├── agent-ui/                   # Angular 17+ Frontend Application
│   ├── src/
│   ├── angular.json
│   └── package.json
│
└── AgenticAiProject/           # Spring Boot REST API & AI Agent Backend
    ├── src/
    └── pom.xml



⚙️ Getting Started & Local Installation
Prerequisites
Java JDK 17 or higher

Node.js & npm (v18+ recommended)

Ollama installed locally running llama3.2

1. Clone the Repository
Bash
git clone [https://github.com/Chaitanya9766/OrderManagementSystem.git](https://github.com/Chaitanya9766/OrderManagementSystem.git)
cd OrderManagementSystem
2. Run the Spring Boot Backend
Open AgenticAiProject in your IDE.

Ensure your MySQL database (agentic_db) is running locally on port 3306.

Run the Spring Boot application (or via terminal: ./mvnw spring-boot:run).

The backend will start on http://localhost:8080.

3. Run the Angular Frontend
Navigate to the frontend directory:

Bash
cd agent-ui
Install dependencies:

Bash
npm install
Start the development server:

Bash
ng serve
Open your browser and navigate to http://localhost:4200.

🔒 Security & CORS Configuration
Authentication: Secured via custom JWT filter ensuring stateless request validation.

CORS: Explicitly configured via Spring Security's CorsConfigurationSource to accept traffic safely from http://localhost:4200.

💡 Key Features
Secure Login Workflow: Token-based authentication saving JWT to local storage and guarding downstream dashboard routes.

Agentic Tool Calling: Intelligent prompt evaluation allowing the AI agent to interact directly with backend services to retrieve live order statuses.