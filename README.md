# 🌸 Petal — Floral Marketplace

A specialized e-commerce platform designed to modernize the floral gifting industry. Petal connects users with local artisan florists through a unique "Mood-Based" search engine and a "Forget-Me-Not" automation system.

## 📁 Project Structure

```
Petal/
├── /backend          # Spring Boot REST API (Java 17, Maven)
├── /web              # React Web Dashboard (Vite + React 18)
├── /mobile           # Android App (Kotlin) — Phase 2
├── /docs             # Documentation (ERD, UML, Screenshots)
├── README.md
└── TASK_CHECKLIST.md
```

## 🛠️ Technology Stack

| Layer    | Technology                         |
|----------|------------------------------------|
| Backend  | Java 17, Spring Boot 3.2, Spring Security, JPA |
| Database | PostgreSQL 14+ (Supabase Postgres for demo/prod) |
| Web App  | React 18, Vite, Axios, React Router |
| Auth     | JWT (jjwt), BCrypt                 |
| Mobile   | Kotlin, Jetpack Compose (Phase 2)  |

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- Node.js 18+
- PostgreSQL 14+ or a Supabase project

### Backend Setup

```bash
# 1. Create local PostgreSQL database, or configure Supabase env vars below
createdb petal_db

# 2. Navigate to backend
cd backend

# 3. Run the Spring Boot API
mvn spring-boot:run
```
The API will start on `http://localhost:8080`

Backend configuration reads secrets from environment variables:

| Variable | Default | Notes |
|----------|---------|-------|
| `DB_URL` | Local PostgreSQL `petal_db` URL | Use Supabase direct or Session Pooler JDBC URL with `sslmode=require` |
| `DB_USERNAME` | `postgres` | For Supabase Session Pooler this is usually `postgres.<project-ref>` |
| `DB_PASSWORD` | empty | Set this in your shell, IDE run config, or local env loader |
| `JWT_SECRET` | local development placeholder | Replace for any shared or deployed environment |
| `STORAGE_PROVIDER` | `local` | Set to `supabase` to upload through Supabase Storage |
| `SUPABASE_URL` | empty | Required when `STORAGE_PROVIDER=supabase` |
| `SUPABASE_SERVICE_ROLE_KEY` | empty | Server-only key; never expose it to React or commit it |
| `SUPABASE_FLORIST_LOGOS_BUCKET` | `florist-logos` | Public bucket for shop logos |
| `SUPABASE_ORDER_PHOTOS_BUCKET` | `order-photos` | Private bucket for preparation/proof photos |
| `SUPABASE_SIGNED_URL_EXPIRES_SECONDS` | `300` | Lifetime for backend-generated private image URLs |

Use `.env.example` as a template for local environment values. Real `.env` files are ignored and should stay local.

### Supabase Setup

Petal uses Supabase only for PostgreSQL hosting and Storage. Authentication remains the Spring Boot JWT flow, and React never calls Supabase directly.

1. Create a Supabase project.
2. In Supabase, copy either the direct database JDBC details or the Session Pooler details. For this persistent Spring Boot backend, prefer direct connection when IPv6 works, or Session Pooler when IPv4 is needed. Include `sslmode=require` in `DB_URL`.
3. Set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `JWT_SECRET` in your backend environment. Do not commit real values.
4. Run `docs/supabase-storage-setup.sql` in the Supabase SQL editor to create:
   - `florist-logos` as a public bucket
   - `order-photos` as a private bucket
5. Set `STORAGE_PROVIDER=supabase`, `SUPABASE_URL`, and `SUPABASE_SERVICE_ROLE_KEY` only on the backend.
6. Keep Supabase Data API/Auth unused for Petal runtime access. If public schema tables are exposed in Supabase, do not grant broad `anon`/`authenticated` table privileges; Spring Boot should remain the API and security boundary.

Local tests and dev can keep `STORAGE_PROVIDER=local`, which stores files under `uploads/` and does not require real Supabase credentials.

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

## 🔐 API Endpoints (Session 1)

| Method | Endpoint             | Auth     | Description          |
|--------|----------------------|----------|----------------------|
| POST   | `/api/auth/register` | Public   | Register a new user  |
| POST   | `/api/auth/login`    | Public   | Login & get JWT      |
| GET    | `/api/user/me`       | Bearer   | Get current user     |

## 👤 Author
- **Karl Miguel C. Penida**
- IT342 – System Integration and Architecture
