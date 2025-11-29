-- V3__seed_admin_user.sql: Seed initial administrator user
-- Password: admin
-- 
-- IMPORTANT: Replace the password hash below with a proper BCrypt hash for "admin"
-- To generate the hash:
--   1. Compile the project: mvn compile
--   2. Run: java -cp "target/classes:$(mvn dependency:build-classpath -q | tail -1)" com.lingualink.util.PasswordHashGenerator admin
--   3. Copy the generated hash and replace the placeholder below
--
-- Note: Change this password immediately after first login in production!

INSERT INTO users (name, email, password, role, created_at, updated_at) 
VALUES (
  'Administrator',
  'admin@lingualink.local',
  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
  'ADMINISTRATOR',
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
)
ON DUPLICATE KEY UPDATE email=email;

