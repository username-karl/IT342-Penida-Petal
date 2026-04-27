# 🌸 Petal — Floral Marketplace

A specialized e-commerce platform designed to modernize the floral gifting industry. Petal connects users with local artisan florists through a unique "Mood-Based" search engine and a "Forget-Me-Not" automation system.

## 🏗️ Vertical Slice Architecture
The Petal platform (across backend, web, and mobile) is structured using **Vertical Slice Architecture**. 
Instead of organizing code by technical layers (e.g., all controllers in one folder, all repositories in another), code is organized by **features**.
- `features/<feature_name>`: Contains all code required for a specific feature to function (UI, state, network, logic).
- `shared/`: Contains cross-cutting concerns, reusable UI components, and global utilities (e.g., Auth interceptors, Design systems).

## 📁 Project Structure

```
Petal/
├── /backend          # Spring Boot REST API (Java 17, Maven)
├── /web              # React Web Dashboard (Vite + React 18)
├── /mobile           # Android App (Kotlin, Jetpack Compose)
├── /docs             # Documentation (ERD, UML, Screenshots)
├── README.md
└── TASK_CHECKLIST.md
```

## 🛠️ Technology Stack

| Layer    | Technology                         |
|----------|------------------------------------|
| Backend  | Java 17, Spring Boot 3.2, Spring Security, JPA, JUnit 5, Mockito |
| Database | MySQL 8+                          |
| Web App  | React 18, Vite, Axios, React Router |
| Auth     | JWT (jjwt), BCrypt                 |
| Mobile   | Kotlin, Jetpack Compose, Retrofit  |

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8+
- Android Studio (for Mobile)

### Backend Setup

```bash
# 1. Create MySQL database
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS petal_db;"

# 2. Navigate to backend
cd backend

# 3. Run the Spring Boot API
mvn spring-boot:run

# 4. Run tests
mvn test
```
The API will start on `http://localhost:8080`

### Web App Setup

```bash
# 1. Navigate to web
cd web

# 2. Install dependencies
npm install

# 3. Start development server
npm run dev
```
The web app will start on `http://localhost:5173`

### Mobile App Setup

1. Open the `/mobile` directory in **Android Studio**.
2. Sync Project with Gradle Files.
3. Run `app` on an Android Emulator or physical device.

## 🔐 API Endpoints (Current)

| Method | Endpoint             | Auth     | Description          |
|--------|----------------------|----------|----------------------|
| POST   | `/api/auth/register` | Public   | Register a new user  |
| POST   | `/api/auth/login`    | Public   | Login & get JWT      |
| POST   | `/api/auth/logout`   | Bearer   | Logout user          |
| GET    | `/api/user/me`       | Bearer   | Get current user     |

## 👤 Author
- **Karl Miguel C. Penida**
- IT342 – System Integration and Architecture
