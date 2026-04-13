# Project Pulse — Database Schema Notes

## Tables Overview

| Table | Purpose |
|---|---|
| `users` | All user types (Admin, Instructor, Student) in one table |
| `rubrics` | Peer evaluation rubrics created by Admin |
| `criteria` | Individual scoring criteria within a rubric |
| `sections` | Academic year offerings (e.g., 2024-2025) |
| `active_weeks` | Which weeks require WAR + peer eval submissions |
| `teams` | Senior design teams, each belonging to a section |
| `section_students` | Students enrolled in a section (pre-team assignment) |
| `team_students` | Students assigned to a specific team |
| `team_instructors` | Instructors assigned to a specific team |
| `invitations` | Email invite tokens for students and instructors |
| `wars` | Weekly Activity Reports (one per student per week) |
| `activities` | Individual activity line items within a WAR |
| `peer_evaluations` | One evaluation per evaluator-evaluatee pair per week |
| `peer_evaluation_scores` | Score per criterion per peer evaluation |

---

## Key Design Decisions

### 1. Single `users` table (not separate tables per role)
All user types share the same `users` table with a `role` column (`ADMIN`, `INSTRUCTOR`, `STUDENT`). This simplifies authentication — one login endpoint, one JWT claim for role. Spring Boot Security can use the role to enforce access rules.

### 2. Two-step student enrollment: `section_students` → `team_students`
- When a student accepts an invite (UC-25), they are added to `section_students`.
- When Admin assigns them to a team (UC-12), they are added to `team_students`.
- This models the real state: a student can be in a section but not yet on a team.

### 3. `sections.rubric_id` — one rubric per section
When Admin creates a section, they select a rubric. Per UC-4 details: editing a rubric during section setup duplicates it behind the scenes, so the section always points to its own frozen rubric version.

### 4. `active_weeks.week_start_date` — Monday date as week ID
Per the glossary: "A week starts on Monday. We may use Monday's date as the Id for a week." This date is the foreign key across `wars` and `peer_evaluations` to tie everything to the correct week.

### 5. `wars` unique constraint on `(student_id, week_start_date)`
Enforces one WAR per student per week at the database level.

### 6. `peer_evaluations` unique constraint on `(evaluator_id, evaluatee_id, week_start_date)`
Enforces one evaluation per pair per week at the database level.

### 7. `invitations.section_id` is nullable
STUDENT invites are always tied to a section. INSTRUCTOR invites are not — instructors are later assigned to teams by the Admin (UC-19). Hence `section_id` is optional.

### 8. `peer_evaluation_scores` normalizes scores
Instead of storing score columns per criterion (which would break if criteria change), each score is a separate row referencing both the evaluation and the criterion. Makes report generation flexible and schema-stable.

---

## Entity Relationships (Summary)

```
users ──< team_students >── teams ──< section (via section_id)
users ──< team_instructors >── teams
users ──< section_students >── sections
sections ──< active_weeks
sections ──── rubrics ──< criteria
users (student) ──< wars ──< activities
users (evaluator) ──< peer_evaluations >── users (evaluatee)
peer_evaluations ──< peer_evaluation_scores >── criteria
invitations ──── sections (optional)
```

---

## Spring Boot / JPA Notes (for implementation)

- Use `@Inheritance(strategy = SINGLE_TABLE)` or just a `role` enum field on `User` entity
- `section_students` and `team_students` are `@ManyToMany` join tables — map them as `@JoinTable` in `Section` and `Team` entities respectively
- `week_start_date` should be `LocalDate` in Java
- `score` and `max_score` should be `BigDecimal` in Java (never `float`/`double` for money/scores)
- Add `@UniqueConstraint` annotations matching the DBML `unique` indexes
