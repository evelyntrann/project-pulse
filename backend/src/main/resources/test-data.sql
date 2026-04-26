-- ============================================================
-- Test Data — run manually in MySQL to seed local dev database
-- Start the backend first so JPA creates the tables, then run this.
--
-- All accounts use password: password123
-- BCrypt hash: $2a$10$abcdefghijklmnopqrstuOaPlXTPMzX5z7hPXo0G5jpEnSQQizaeG
-- ============================================================

-- ── Users ──────────────────────────────────────────────────────────────────

INSERT IGNORE INTO users (email, password_hash, first_name, last_name, role, is_active, created_at) VALUES
  -- Admin
  ('admin@tcu.edu',            '$2a$10$abcdefghijklmnopqrstuOaPlXTPMzX5z7hPXo0G5jpEnSQQizaeG', 'Admin',   'User',     'ADMIN',      1, NOW()),

  -- Instructors
  ('dr.wei@tcu.edu',           '$2a$10$abcdefghijklmnopqrstuOaPlXTPMzX5z7hPXo0G5jpEnSQQizaeG', 'Wei',     'Zhang',    'INSTRUCTOR', 1, NOW()),
  ('dr.smith@tcu.edu',         '$2a$10$abcdefghijklmnopqrstuOaPlXTPMzX5z7hPXo0G5jpEnSQQizaeG', 'John',    'Smith',    'INSTRUCTOR', 1, NOW()),

  -- Students
  ('alice.johnson@tcu.edu',    '$2a$10$abcdefghijklmnopqrstuOaPlXTPMzX5z7hPXo0G5jpEnSQQizaeG', 'Alice',   'Johnson',  'STUDENT',    1, NOW()),
  ('bob.smith@tcu.edu',        '$2a$10$abcdefghijklmnopqrstuOaPlXTPMzX5z7hPXo0G5jpEnSQQizaeG', 'Bob',     'Smith',    'STUDENT',    1, NOW()),
  ('carol.white@tcu.edu',      '$2a$10$abcdefghijklmnopqrstuOaPlXTPMzX5z7hPXo0G5jpEnSQQizaeG', 'Carol',   'White',    'STUDENT',    1, NOW()),
  ('dan.nguyen@tcu.edu',       '$2a$10$abcdefghijklmnopqrstuOaPlXTPMzX5z7hPXo0G5jpEnSQQizaeG', 'Dan',     'Nguyen',   'STUDENT',    1, NOW()),
  ('emily.chen@tcu.edu',       '$2a$10$abcdefghijklmnopqrstuOaPlXTPMzX5z7hPXo0G5jpEnSQQizaeG', 'Emily',   'Chen',     'STUDENT',    1, NOW());

-- ── Section ─────────────────────────────────────────────────────────────────

INSERT IGNORE INTO sections (name, start_date, end_date, rubric_id, is_active, created_at) VALUES
  ('2024-2025', '2024-08-26', '2025-05-09', NULL, 1, NOW());

-- ── Teams ───────────────────────────────────────────────────────────────────

INSERT IGNORE INTO teams (name, description, website_url, section_id, created_at)
SELECT
  t.name, t.description, t.website_url, s.id, NOW()
FROM (
  SELECT 'SmartCampus' AS name,
         'IoT-based campus energy monitoring system to reduce electricity waste.' AS description,
         'https://github.com/tcu-sd/smart-campus' AS website_url
  UNION ALL
  SELECT 'MediTrack',
         'Mobile app for patients to track medications, appointments, and health metrics.',
         'https://github.com/tcu-sd/meditrack'
  UNION ALL
  SELECT 'SafeRoute',
         'Real-time campus safety app with optimal walking routes and emergency alerts.',
         NULL
  UNION ALL
  SELECT 'GreenLedger',
         'Sustainability tracking platform to log, analyze, and report carbon footprint.',
         'https://greenledger.vercel.app'
  UNION ALL
  SELECT 'TutorLink',
         'Peer tutoring marketplace connecting TCU students with verified tutors.',
         NULL
) t
CROSS JOIN sections s
WHERE s.name = '2024-2025';

-- ── Enroll students in section ───────────────────────────────────────────────

INSERT IGNORE INTO section_students (section_id, student_id)
SELECT s.id, u.id
FROM sections s, users u
WHERE s.name = '2024-2025'
  AND u.role = 'STUDENT';

-- ── Assign students to SmartCampus team ──────────────────────────────────────

INSERT IGNORE INTO team_students (team_id, student_id)
SELECT t.id, u.id
FROM teams t, users u
WHERE t.name = 'SmartCampus'
  AND u.email IN ('alice.johnson@tcu.edu', 'bob.smith@tcu.edu', 'carol.white@tcu.edu');

-- ── Assign instructor to SmartCampus team ────────────────────────────────────

INSERT IGNORE INTO team_instructors (team_id, instructor_id)
SELECT t.id, u.id
FROM teams t, users u
WHERE t.name = 'SmartCampus'
  AND u.email = 'dr.wei@tcu.edu';

-- ── Enroll instructor in section ─────────────────────────────────────────────

INSERT IGNORE INTO section_instructors (section_id, instructor_id)
SELECT s.id, u.id
FROM sections s, users u
WHERE s.name = '2024-2025'
  AND u.email = 'dr.wei@tcu.edu';

-- ── Active weeks for 2024-2025 section (Spring 2026 semester) ────────────────

INSERT IGNORE INTO active_weeks (section_id, week_start_date, is_active)
SELECT s.id, week_date, 1
FROM sections s
CROSS JOIN (
  SELECT '2026-01-12' AS week_date UNION ALL
  SELECT '2026-01-19' UNION ALL
  SELECT '2026-01-26' UNION ALL
  SELECT '2026-02-02' UNION ALL
  SELECT '2026-02-09' UNION ALL
  SELECT '2026-02-16' UNION ALL
  SELECT '2026-02-23' UNION ALL
  SELECT '2026-03-02' UNION ALL
  SELECT '2026-03-09' UNION ALL
  SELECT '2026-03-16' UNION ALL
  SELECT '2026-03-23' UNION ALL
  SELECT '2026-03-30' UNION ALL
  SELECT '2026-04-06' UNION ALL
  SELECT '2026-04-13' UNION ALL
  SELECT '2026-04-20'
) weeks
WHERE s.name = '2024-2025';
