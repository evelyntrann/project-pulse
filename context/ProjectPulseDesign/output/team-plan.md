# Project Pulse — Team Plan (Evelyn, Angel, Micah)

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | Vue.js + Vuetify (no ElementPlus) |
| Backend | Spring Boot 4.x (REST API) |
| Database | MySQL or PostgreSQL |
| Deployment | Microsoft Azure |

---

## Phase 0 — Project Setup (All 3, ~2–3 days)

Do this together before splitting into parallel tracks.

**Tasks (everyone contributes):**
- Create GitHub repo with agreed branch strategy (`main`, `dev`, feature branches per person)
- Scaffold Spring Boot project (Spring Initializr: Web, JPA, Security, MySQL/PostgreSQL driver)
- Scaffold Vue.js + Vuetify project (`npm create vue@latest` + Vuetify plugin)
- Design database schema together (entities: User, Section, Team, WAR, Activity, PeerEvaluation, Rubric, Criterion)

> **Critical early meeting:** Get all 3 of you together to finalize the database schema and REST API naming conventions before anyone writes feature code.

---

## Phase 1 — Feature Development (Parallel, ~3–4 weeks)

Each person owns the **full vertical slice** (backend API + frontend UI) for their use cases.

---

### Evelyn — Auth + Admin Section & Team Management

| UC | Description |
|---|---|
| Auth | Login / logout for all roles; JWT setup + role-based route guards |
| UC-2 | Find senior design sections |
| UC-3 | View a senior design section |
| UC-4 | Create a senior design section |
| UC-5 | Edit a senior design section |
| UC-6 | Set up active weeks for a section |
| UC-7 | Find senior design teams |
| UC-8 | View a senior design team |
| UC-9 | Create a senior design team |
| UC-10 | Edit a senior design team |
| UC-11 | Admin invites students to join a section (sends email) |
| UC-12 | Assign students to teams |
| UC-13 | Remove a student from a team |
| UC-14 | Delete a senior design team |

**Why this grouping:** Auth is foundational and you want full control over it. Sections and Teams are tightly related (a Team belongs to a Section), and student invites/assignments revolve around the same models.

**Backend endpoints:**
- `POST /auth/login`, `POST /auth/register`, `GET /auth/me`
- `GET /sections`, `POST /sections`
- `GET /sections/{id}`, `PUT /sections/{id}`
- `PUT /sections/{id}/active-weeks`
- `GET /teams`, `POST /teams`
- `GET /teams/{id}`, `PUT /teams/{id}`, `DELETE /teams/{id}`
- `POST /invitations/students`
- `POST /teams/{id}/students`, `DELETE /teams/{id}/students/{studentId}`

**Frontend pages:**
- Login page
- Section list + search page
- Section detail / create / edit forms
- Active weeks configuration UI
- Team list + search page
- Team detail / create / edit forms
- Invite student form
- Assign / remove student from team

---

### Angel — Admin User Management + Rubric

| UC | Description |
|---|---|
| UC-1 | Admin creates a rubric |
| UC-15 | Find students |
| UC-16 | View a student |
| UC-17 | Delete a student |
| UC-18 | Admin invites instructors (sends email) |
| UC-19 | Assign instructors to senior design teams |
| UC-20 | Remove an instructor from a senior design team |
| UC-21 | Find instructors |
| UC-22 | View an instructor |
| UC-23 | Deactivate an instructor |
| UC-24 | Reactivate an instructor |

**Why this grouping:** Rubric and user management (students and instructors) are straightforward CRUD operations that build on top of Evelyn's Auth foundation.

**Backend endpoints:**
- `POST /rubrics`, `GET /rubrics/{id}`, `PUT /rubrics/{id}`
- `POST /invitations/instructors`
- `GET /students`, `DELETE /students/{id}`, `GET /students/{id}`
- `GET /instructors`, `DELETE /instructors/{id}`, `GET /instructors/{id}`
- `PUT /instructors/{id}/deactivate`, `PUT /instructors/{id}/reactivate`
- `POST /teams/{id}/instructors`, `DELETE /teams/{id}/instructors/{instructorId}`

**Frontend pages:**
- Rubric builder form (dynamic criterion rows)
- Student list + search + detail (Admin)
- Instructor list + search + detail (Admin)
- Invite instructor form
- Assign / remove instructor from team

---

### Micah — Student + Instructor Features + Deployment

| UC | Description |
|---|---|
| UC-25 | Student sets up account (from email invite link) |
| UC-26 | Student / Instructor edits their profile |
| UC-27 | Student manages WAR activities (add, edit, delete) |
| UC-28 | Student submits a peer evaluation |
| UC-29 | Student views own peer evaluation report |
| UC-30 | Instructor sets up account (from email invite link) |
| UC-31 | Instructor generates peer eval report (entire section) |
| UC-32 | Instructor generates a WAR report of a senior design team |
| UC-33 | Instructor generates peer eval report (one student) |
| UC-34 | Instructor generates a WAR report of a student |
| Azure | Deploy backend + frontend + DB |

**Why this grouping:** Student and Instructor are the end-users of the system. WAR and peer evaluation are the two core workflows they interact with, and the reports are the output of those workflows.

**Backend endpoints:**
- `POST /students/register`, `PUT /students/{id}` (profile)
- `POST /instructors/register`, `PUT /instructors/{id}` (profile)
- `GET /wars/{weekId}/activities`, `POST /wars/{weekId}/activities`
- `PUT /wars/{weekId}/activities/{id}`, `DELETE /wars/{weekId}/activities/{id}`
- `POST /peer-evaluations`, `GET /peer-evaluations/my`
- `GET /reports/peer-eval/section/{sectionId}`
- `GET /reports/peer-eval/student/{studentId}`
- `GET /reports/war/team/{teamId}?weekId={weekId}`
- `GET /reports/war/student/{studentId}?weekId={weekId}`

**Frontend pages:**
- Student / Instructor account registration page
- Student / Instructor profile edit page
- WAR entry form (weekly activity table with add/edit/delete)
- Peer evaluation submission form (one form per teammate, scored by rubric criteria)
- Student: view own peer evaluation scores + public comments
- Instructor: peer eval report (section-wide)
- Instructor: peer eval report (per student)
- Instructor: WAR report (per team)
- Instructor: WAR report (per student)

**Azure deployment:**
- Spring Boot → Azure App Service
- Vue.js build → Azure Static Web Apps
- Database → Azure Database for MySQL / PostgreSQL
- Environment config via Azure App Settings

---

## Use Case Ownership Summary

| Owner | Use Cases | Count |
|---|---|---|
| **Evelyn** | Auth, UC-2–14 | 13 + auth |
| **Angel** | UC-1, UC-15–24 | 11 |
| **Micah** | UC-25–34 + Azure | 10 |

---

## Phase 2 — Integration Testing (~1 week)

Cross-feature flows to test together after individual features are done:

| Flow | Who to coordinate |
|---|---|
| Admin invites student → student registers account (UC-11 + UC-25) | Evelyn + Micah |
| Admin invites instructor → instructor registers account (UC-18 + UC-30) | Angel (invite) + Micah (registration) |
| Student assigned to team → submits WAR → instructor views WAR report (UC-12 + UC-27 + UC-32) | Evelyn + Micah |
| Student submits peer eval → instructor views report (UC-28 + UC-31/33) | Angel + Micah (rubric) + Micah (submission + report) |

---

## Phase 3 — Polish, Testing & Submission (~1 week)

| Task | Owner |
|---|---|
| End-to-end manual testing (walk all 34 UCs as each role) | All 3 |
| Bug fixes | UC owner |
| AI Interaction Log (3–4 total: document prompts, AI response, what you accepted/rejected/changed) | All 3, one example each |
| Reflection report (3 AI strengths, 3 limitations, 2 issues AI caused + how resolved) | All 3, compile together |
| Demo preparation (rehearse live walkthrough of all major flows) | All 3 |
| Demo recording (Zoom Cloud) | All 3 |
| Confirm live Azure URL is working | Micah |

---

## Coordination Rules

1. **Branch strategy:** `feature/<name>/<short-desc>` → PR to `dev` → merge to `main` before demo
2. **API contracts first:** Before writing any frontend, agree on JSON shapes for shared endpoints in a `api-contracts.md` file in the repo
3. **Weekly sync:** 30-minute standup every Monday — what did you do, what's next, blockers
4. **Shared dependency:** Evelyn's Auth is needed by everyone — build and merge it in Week 1 so Angel and Micah can use it
5. **Git hygiene:** Commit under your own name — graders check git history for individual contribution (10% of grade)
6. **Ask, don't guess:** Per the project description, if requirements are unclear, ask Dr. Wei immediately

---

## Build Priority Order

```
Week 1:  DB schema + GitHub setup (all) → Auth (Evelyn merges first)
Week 2:  Evelyn: Section/Team CRUD  | Angel: Rubric + User mgmt  | Micah: Student/Instructor account + WAR
Week 3:  Evelyn: Student invite/assign UCs | Angel: Instructor mgmt UCs | Micah: Peer eval submission
Week 4:  Micah: Reports (WAR + peer eval) | Evelyn + Micah: Integration flows | Micah: Azure setup
Week 5:  Azure deployment finalized | Full end-to-end testing | Bug fixes
Week 6:  AI log + reflection + demo recording + final submission
```
