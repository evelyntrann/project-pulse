# Project Pulse — REST API Contracts

## How to Use This Document
- **Design together, implement separately.** All 3 members agree on this file first, then each person implements only their assigned endpoints.
- If you need a new endpoint not listed here, add it to this file and notify the team before implementing.
- Use this as the source of truth — the frontend team should code against these contracts, not against the backend implementation directly.

---

## Global Conventions

### Base URL
```
/api/v1
```
All endpoints are prefixed with `/api/v1`.

### Naming Rules
| Rule | Good | Bad |
|---|---|---|
| Plural nouns for collections | `/students` | `/student`, `/getStudents` |
| Lowercase, hyphenated | `/active-weeks`, `/peer-evaluations` | `/activeWeeks`, `/PeerEvaluations` |
| No verbs in URL | `DELETE /teams/{id}` | `/deleteTeam/{id}` |
| Actions as sub-resources | `POST /wars/{id}/submit` | `/submitWar/{id}` |
| Nest max 2 levels deep | `/teams/{id}/students` | `/sections/{id}/teams/{id}/students/{id}` |

### HTTP Methods
| Method | Use for |
|---|---|
| `GET` | Read / search / list |
| `POST` | Create a new resource |
| `PUT` | Replace/update a full resource |
| `PATCH` | Partial update (use sparingly, prefer PUT) |
| `DELETE` | Remove a resource |

### Query Parameters (for filtering & searching)
Use query params for search/filter, **not** path params.
```
GET /api/v1/students?firstName=John&sectionId=3&teamId=7
GET /api/v1/teams?sectionId=2&instructorId=5
GET /api/v1/wars?teamId=4&weekStartDate=2024-09-02
```

### Standard Response Envelope
Every response (success or error) uses this shape:
```json
{
  "success": true,
  "data": { ... },
  "message": "Optional human-readable message",
  "error": null
}
```
On error:
```json
{
  "success": false,
  "data": null,
  "message": "Validation failed",
  "error": {
    "code": "VALIDATION_ERROR",
    "details": "Section name is required"
  }
}
```

### HTTP Status Codes
| Code | When to use |
|---|---|
| `200 OK` | Successful GET, PUT, DELETE |
| `201 Created` | Successful POST that creates a resource |
| `204 No Content` | Successful DELETE with no body |
| `400 Bad Request` | Validation errors, malformed input |
| `401 Unauthorized` | Not logged in |
| `403 Forbidden` | Logged in but wrong role |
| `404 Not Found` | Resource does not exist |
| `409 Conflict` | Duplicate detected (e.g. duplicate section name) |
| `500 Internal Server Error` | Unexpected server failure |

### Authentication
All endpoints (except `/auth/*` and `/invitations/*/register`) require a JWT Bearer token in the header:
```
Authorization: Bearer <token>
```
Role-based access is enforced per endpoint (see "Access" column below).

---

## Endpoints

---

### Auth
**Owner: Evelyn**

| Method | Endpoint | Description | Access | UC |
|---|---|---|---|---|
| `POST` | `/api/v1/auth/login` | Login with email + password, returns JWT | Public | — |
| `POST` | `/api/v1/auth/logout` | Invalidate session | Any | — |
| `GET` | `/api/v1/auth/me` | Get current logged-in user details | Any | — |

**POST /api/v1/auth/login**
```json
// Request body
{
  "email": "john.doe@tcu.edu",
  "password": "secret123"
}

// Response 200
{
  "success": true,
  "data": {
    "token": "eyJhbGci...",
    "user": {
      "id": 1,
      "email": "john.doe@tcu.edu",
      "firstName": "John",
      "lastName": "Doe",
      "role": "STUDENT"
    }
  }
}
```

---

### Rubrics
**Owner: Angel** | UC-1

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `POST` | `/api/v1/rubrics` | Create a new rubric | ADMIN |
| `GET` | `/api/v1/rubrics` | List all rubrics | ADMIN |
| `GET` | `/api/v1/rubrics/{id}` | Get rubric details + criteria | ADMIN |
| `PUT` | `/api/v1/rubrics/{id}` | Edit rubric (creates a copy internally) | ADMIN |

**POST /api/v1/rubrics**
```json
// Request body
{
  "name": "Peer Eval Rubric v1",
  "criteria": [
    { "name": "Quality of work", "description": "How do you rate the quality of this teammate's work?", "maxScore": 10, "orderIndex": 1 },
    { "name": "Productivity",    "description": "How productive is this teammate?", "maxScore": 10, "orderIndex": 2 },
    { "name": "Initiative",      "description": "How proactive is this teammate?", "maxScore": 10, "orderIndex": 3 },
    { "name": "Courtesy",        "description": "Does this teammate treat others with respect?", "maxScore": 10, "orderIndex": 4 },
    { "name": "Open-mindedness", "description": "How well does this teammate handle criticism?", "maxScore": 10, "orderIndex": 5 },
    { "name": "Engagement",      "description": "How is this teammate's performance during meetings?", "maxScore": 10, "orderIndex": 6 }
  ]
}

// Response 201
{
  "success": true,
  "data": { "id": 1, "name": "Peer Eval Rubric v1", "criteria": [ ... ] }
}
```

---

### Sections
**Owner: Evelyn** | UC-2, UC-3, UC-4, UC-5, UC-6

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `GET` | `/api/v1/sections` | Find/list sections | ADMIN |
| `POST` | `/api/v1/sections` | Create a new section | ADMIN |
| `GET` | `/api/v1/sections/{id}` | View section details | ADMIN |
| `PUT` | `/api/v1/sections/{id}` | Edit a section | ADMIN |
| `PUT` | `/api/v1/sections/{id}/active-weeks` | Set active/inactive weeks | ADMIN |

**GET /api/v1/sections** — Query params: `?name=2024-2025`

**POST /api/v1/sections**
```json
// Request body
{
  "name": "2024-2025",
  "startDate": "2024-08-21",
  "endDate": "2025-05-01",
  "rubricId": 1
}

// Response 201
{
  "success": true,
  "data": { "id": 1, "name": "2024-2025", "startDate": "2024-08-21", "endDate": "2025-05-01", "rubric": { ... } }
}
```

**PUT /api/v1/sections/{id}/active-weeks**
```json
// Request body — full list of all weeks; is_active=false marks holiday weeks
{
  "weeks": [
    { "weekStartDate": "2024-08-26", "isActive": true },
    { "weekStartDate": "2024-09-02", "isActive": true },
    { "weekStartDate": "2024-12-23", "isActive": false }
  ]
}
```

---

### Teams
**Owner: Evelyn** | UC-7, UC-8, UC-9, UC-10, UC-12, UC-13, UC-14, UC-19, UC-20

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `GET` | `/api/v1/teams` | Find/list teams | ADMIN, INSTRUCTOR |
| `POST` | `/api/v1/teams` | Create a new team | ADMIN |
| `GET` | `/api/v1/teams/{id}` | View team details | ADMIN, INSTRUCTOR |
| `PUT` | `/api/v1/teams/{id}` | Edit a team | ADMIN |
| `DELETE` | `/api/v1/teams/{id}` | Delete a team | ADMIN |
| `POST` | `/api/v1/teams/{id}/students` | Assign a student to a team | ADMIN |
| `DELETE` | `/api/v1/teams/{id}/students/{studentId}` | Remove a student from a team | ADMIN |
| `POST` | `/api/v1/teams/{id}/instructors` | Assign an instructor to a team | ADMIN |
| `DELETE` | `/api/v1/teams/{id}/instructors/{instructorId}` | Remove an instructor from a team | ADMIN |

**GET /api/v1/teams** — Query params: `?sectionId=1&name=TeamA&instructorId=5`

**POST /api/v1/teams**
```json
// Request body
{
  "name": "Peer Evaluation Tool Team",
  "description": "Team building the peer eval system",
  "websiteUrl": "https://github.com/tcu/peer-eval",
  "sectionId": 1
}
```

**POST /api/v1/teams/{id}/students**
```json
// Request body
{ "studentId": 12 }
```

---

### Invitations
**Owner: Evelyn (students) | Angel (instructors)** | UC-11, UC-18

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `POST` | `/api/v1/invitations/students` | Invite students to a section | ADMIN |
| `POST` | `/api/v1/invitations/instructors` | Invite instructors to register | ADMIN |

**POST /api/v1/invitations/students**
```json
// Request body
{
  "sectionId": 1,
  "emails": ["john.doe@tcu.edu", "f.smith@tcu.edu"],
  "customMessage": "Optional override message"
}
```

**POST /api/v1/invitations/instructors**
```json
// Request body
{
  "emails": ["dr.wei@tcu.edu"]
}
```

---

### Students
**Owner: Angel** | UC-15, UC-16, UC-17

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `GET` | `/api/v1/students` | Find/list students | ADMIN, INSTRUCTOR |
| `GET` | `/api/v1/students/{id}` | View student details | ADMIN, INSTRUCTOR |
| `DELETE` | `/api/v1/students/{id}` | Delete a student | ADMIN |

**GET /api/v1/students** — Query params: `?firstName=John&lastName=Doe&email=...&sectionId=1&teamId=3`

---

### Instructors
**Owner: Angel** | UC-21, UC-22, UC-23, UC-24

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `GET` | `/api/v1/instructors` | Find/list instructors | ADMIN |
| `GET` | `/api/v1/instructors/{id}` | View instructor details | ADMIN |
| `PUT` | `/api/v1/instructors/{id}/deactivate` | Deactivate an instructor | ADMIN |
| `PUT` | `/api/v1/instructors/{id}/activate` | Reactivate an instructor | ADMIN |

---

### Account Registration (from invite link)
**Owner: Micah** | UC-25, UC-30

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `POST` | `/api/v1/students/register` | Student sets up account using invite token | Public |
| `POST` | `/api/v1/instructors/register` | Instructor sets up account using invite token | Public |

**POST /api/v1/students/register**
```json
// Request body
{
  "token": "abc123uniquetoken",
  "firstName": "Jane",
  "lastName": "Doe",
  "password": "securePassword1!"
}
```

---

### Account Profile
**Owner: Micah** | UC-26

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `PUT` | `/api/v1/users/{id}` | Edit own profile (name, password) | STUDENT, INSTRUCTOR |

---

### Weekly Activity Reports (WARs)
**Owner: Micah** | UC-27

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `GET` | `/api/v1/wars` | List WARs (filtered) | STUDENT, INSTRUCTOR, ADMIN |
| `GET` | `/api/v1/wars/{id}` | Get a specific WAR | STUDENT, INSTRUCTOR |
| `POST` | `/api/v1/wars` | Initialize a WAR for the current week | STUDENT |
| `POST` | `/api/v1/wars/{id}/submit` | Submit a draft WAR | STUDENT |
| `GET` | `/api/v1/wars/{id}/activities` | List activities in a WAR | STUDENT, INSTRUCTOR |
| `POST` | `/api/v1/wars/{id}/activities` | Add an activity to a WAR | STUDENT |
| `PUT` | `/api/v1/wars/{id}/activities/{activityId}` | Edit an activity | STUDENT |
| `DELETE` | `/api/v1/wars/{id}/activities/{activityId}` | Delete an activity | STUDENT |

**GET /api/v1/wars** — Query params: `?studentId=5&teamId=3&weekStartDate=2024-09-02`

**POST /api/v1/wars/{id}/activities**
```json
// Request body
{
  "category": "Development",
  "plannedActivity": "Implement login endpoint",
  "description": "Set up JWT auth with Spring Security",
  "plannedHours": 4.0,
  "actualHours": 5.5,
  "status": "DONE"
}
```

---

### Peer Evaluations
**Owner: Micah** | UC-28, UC-29

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `POST` | `/api/v1/peer-evaluations` | Submit a peer evaluation | STUDENT |
| `GET` | `/api/v1/peer-evaluations/my` | View own received evaluations | STUDENT |

**POST /api/v1/peer-evaluations**
```json
// Request body — one request per teammate being evaluated
{
  "evaluateeId": 8,
  "weekStartDate": "2024-09-02",
  "scores": [
    { "criterionId": 1, "score": 9 },
    { "criterionId": 2, "score": 8 },
    { "criterionId": 3, "score": 7 },
    { "criterionId": 4, "score": 10 },
    { "criterionId": 5, "score": 8 },
    { "criterionId": 6, "score": 9 }
  ],
  "publicComment": "Great work this week!",
  "privateComment": "Sometimes misses standups."
}
```

---

### Reports
**Owner: Micah** | UC-31, UC-32, UC-33, UC-34

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `GET` | `/api/v1/reports/war/teams/{teamId}` | WAR report for a team | INSTRUCTOR, ADMIN |
| `GET` | `/api/v1/reports/war/students/{studentId}` | WAR report for a student | INSTRUCTOR, ADMIN |
| `GET` | `/api/v1/reports/peer-evaluations/sections/{sectionId}` | Peer eval report (entire section) | INSTRUCTOR, ADMIN |
| `GET` | `/api/v1/reports/peer-evaluations/students/{studentId}` | Peer eval report for one student | INSTRUCTOR, ADMIN |

All report endpoints accept `?weekStartDate=2024-09-02` as an optional filter.

---

## Endpoint Ownership Summary

| Owner | Endpoints |
|---|---|
| **Evelyn** | `/auth/*`, `/sections/*`, `/teams/*`, `POST /invitations/students` |
| **Angel** | `/rubrics/*`, `/students/*`, `/instructors/*`, `POST /invitations/instructors` |
| **Micah** | `POST /students/register`, `POST /instructors/register`, `PUT /users/{id}`, `/wars/*`, `/peer-evaluations/*`, `/reports/*` |
