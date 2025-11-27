-- V2__sample_data.sql: sample users and interpreters
INSERT INTO users (name, email, password, role) VALUES
('Admin User', 'admin@lingualink.local', 'password-hash', 'ADMIN'),
('Alice Organizer', 'alice@org.com', 'password-hash', 'ORGANIZER'),
('Bob Interpreter', 'bob@interp.com', 'password-hash', 'INTERPRETER');

INSERT INTO interpreters (user_id, languages, rate_per_hour, experience_years, bio) VALUES
(3, JSON_ARRAY('English','Korean'), 25.00, 5, 'Experienced conference interpreter.');
