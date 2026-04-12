# Project Pulse

A full-stack web application that replaces manual Google Sheets WARs and Excel peer evaluations for TCU Senior Design students. Supports three roles: **Admin**, **Instructor**, and **Student**.

---

## Team

| Member | Owns | Use Cases |
|--------|------|-----------|
| **Evelyn** | Auth, Sections & Teams | UC-2–14 |
| **Angel** | Rubrics, User Management | UC-1, UC-15–24 |
| **Micah** | WAR, Peer Evaluation, Reports, Azure | UC-25–34 |

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | Vue 3 + Vuetify 3 (TypeScript) |
| Backend | Spring Boot 4.x (Java 21, Maven) |
| Database | MySQL |
| Auth | JWT (role-based: ADMIN, INSTRUCTOR, STUDENT) |
| Deployment | Microsoft Azure |

---

## Getting Started (Local Dev)

### Prerequisites
- Java 21
- Node.js 18+
- MySQL 8.0 running locally

### 1. Set up the database

```sql
CREATE DATABASE project_pulse;
```

Then run the test seed file (optional):
```bash
mysql -u root -p project_pulse < backend/src/main/resources/test-data.sql
```

### 2. Configure environment variables

Create a `.env` file or export these before running the backend:

```bash
export DB_USERNAME=root
export DB_PASSWORD=your_password
export JWT_SECRET=your_secret_key_at_least_32_chars
export MAIL_HOST=smtp.mailtrap.io
export MAIL_USERNAME=your_mailtrap_username
export MAIL_PASSWORD=your_mailtrap_password
export APP_BASE_URL=http://localhost:5173
```

> For email testing locally, sign up at [Mailtrap](https://mailtrap.io) (free) and use your SMTP credentials.

### 3. Start the backend

```bash
cd backend
mvn spring-boot:run
# Runs on http://localhost:8080
```

### 4. Start the frontend

```bash
cd frontend
npm install
npm run dev
# Runs on http://localhost:5173
```

---

## Test Accounts

> All test accounts use password: **`password123`**
> Run `backend/src/main/resources/test-data.sql` to seed all accounts (see setup steps above).

### Quick Login Accounts

| Role | Email | Password |
|------|-------|----------|
| **Admin** | `admin@tcu.edu` | `password123` |
| **Instructor** | `dr.wei@tcu.edu` | `password123` |
| **Student** | `alice.johnson@tcu.edu` | `password123` |

### All Seeded Accounts

| Email | Name | Role |
|-------|------|------|
| `admin@tcu.edu` | Admin User | ADMIN |
| `dr.wei@tcu.edu` | Wei Zhang | INSTRUCTOR |
| `dr.smith@tcu.edu` | John Smith | INSTRUCTOR |
| `alice.johnson@tcu.edu` | Alice Johnson | STUDENT |
| `bob.smith@tcu.edu` | Bob Smith | STUDENT |
| `carol.white@tcu.edu` | Carol White | STUDENT |
| `dan.nguyen@tcu.edu` | Dan Nguyen | STUDENT |
| `emily.chen@tcu.edu` | Emily Chen | STUDENT |

### Instructor (reference only)

```sql
-- Password: password123
INSERT INTO users (email, password_hash, first_name, last_name, role, is_active, created_at)
VALUES (
  'instructor@tcu.edu',
  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
  'Test', 'Instructor', 'INSTRUCTOR', 1, NOW()
);
```

---

## What Each Role Can Do

| Feature | Admin | Instructor | Student |
|---------|-------|-----------|---------|
| Manage Sections (create/edit/delete) | ✅ | — | — |
| Set Active Weeks | ✅ | — | — |
| Invite Students to Section | ✅ | — | — |
| Assign Students to Teams | ✅ | — | — |
| Manage Section Instructors | ✅ | — | — |
| Create / Edit / Delete Teams | ✅ | — | — |
| View Teams | ✅ | ✅ | — |
| Weekly Activity Reports (WAR) | — | ✅ | ✅ |
| Peer Evaluations | — | ✅ | ✅ |
| Rubrics & Criteria | ✅ | — | — |

> WAR, Peer Evaluations, and Rubrics are under active development by Micah and Angel.

---

## Project Structure

```
project-pulse/
├── backend/                   # Spring Boot (Maven)
│   └── src/main/java/com/projectpulse/
│       ├── auth/
│       ├── section/
│       ├── team/
│       ├── user/
│       ├── invitation/
│       └── common/
├── frontend/                  # Vue 3 + Vuetify
│   └── src/
│       ├── api/               # Axios API calls (one file per feature)
│       ├── components/        # AppLayout, shared components
│       ├── views/             # Page-level views
│       │   ├── sections/
│       │   └── teams/
│       ├── stores/            # Pinia stores
│       └── router/            # Vue Router + guards
└── output/                    # Planning docs (schema, API contracts)
```

---

## API Overview

Base URL: `/api/v1`

All responses follow this envelope:
```json
{ "success": true, "data": { ... }, "message": null, "error": null }
```

| Resource | Endpoint prefix |
|----------|----------------|
| Auth | `/api/v1/auth` |
| Sections | `/api/v1/sections` |
| Teams | `/api/v1/teams` |
| Invitations | `/api/v1/invitations` |

See `output/api-contracts.md` for the full endpoint list.

---

## Git Workflow

```
main    ← production only (PR required)
dev     ← integration branch
feature/<name>/<short-desc>  ← personal feature branches
```

```bash
git checkout dev
git checkout -b feature/evelyn/my-feature
# ... work ...
git push origin feature/evelyn/my-feature
# Open PR → dev
```

**Never commit directly to `main` or `dev`.**
