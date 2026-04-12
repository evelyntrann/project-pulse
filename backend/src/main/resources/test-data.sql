-- ============================================================
-- Test Data — run manually in MySQL to seed local dev database
-- Assumes section with id=1 already exists (e.g. "2024-2025")
-- ============================================================

-- Test students (password: password123)
-- BCrypt hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
INSERT INTO users (email, password_hash, first_name, last_name, role, is_active, created_at) VALUES
  ('alice.johnson@tcu.edu',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Alice',   'Johnson',  'STUDENT', 1, NOW()),
  ('bob.smith@tcu.edu',      '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Bob',     'Smith',    'STUDENT', 1, NOW()),
  ('carol.white@tcu.edu',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Carol',   'White',    'STUDENT', 1, NOW()),
  ('dan.nguyen@tcu.edu',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Dan',     'Nguyen',   'STUDENT', 1, NOW()),
  ('emily.chen@tcu.edu',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Emily',   'Chen',     'STUDENT', 1, NOW());

-- Assign test students to team id=1 (SmartCampus)
-- Replace 1 with your actual team id if different
INSERT INTO team_students (team_id, student_id)
SELECT t.id, u.id
FROM teams t, users u
WHERE t.name = 'SmartCampus'
  AND u.email IN (
    'alice.johnson@tcu.edu',
    'bob.smith@tcu.edu',
    'carol.white@tcu.edu'
  );

INSERT INTO teams (name, description, website_url, section_id, created_at) VALUES
  ('SmartCampus',
   'Building an IoT-based campus energy monitoring system to reduce electricity waste in university buildings.',
   'https://github.com/tcu-sd/smart-campus',
   1, NOW()),

  ('MediTrack',
   'A mobile app for patients to track medications, appointments, and health metrics with doctor integration.',
   'https://github.com/tcu-sd/meditrack',
   1, NOW()),

  ('SafeRoute',
   'Real-time campus safety app that provides optimal walking routes and emergency alerts for students.',
   NULL,
   1, NOW()),

  ('GreenLedger',
   'Sustainability tracking platform for organizations to log, analyze, and report their carbon footprint.',
   'https://greenledger.vercel.app',
   1, NOW()),

  ('TutorLink',
   'Peer tutoring marketplace connecting TCU students with verified tutors across all subject areas.',
   NULL,
   1, NOW());
