-- LinguaLink Database Schema
-- Use this script in MySQL Workbench to create all required tables
-- Total Tables: 7

-- 1. Users Table
CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(200),
  email VARCHAR(200) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  role VARCHAR(50),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Interpreters Table
CREATE TABLE interpreters (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  languages JSON,
  rate_per_hour DECIMAL(10,2),
  experience_years INT,
  bio TEXT,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 3. Events Table
CREATE TABLE events (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  organizer_id BIGINT NOT NULL,
  title VARCHAR(255),
  description TEXT,
  start_datetime DATETIME,
  end_datetime DATETIME,
  location VARCHAR(255),
  language VARCHAR(100),
  FOREIGN KEY (organizer_id) REFERENCES users(id)
);

-- 4. Bookings Table
CREATE TABLE bookings (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  event_id BIGINT NOT NULL,
  interpreter_id BIGINT,
  status VARCHAR(50),
  requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  confirmed_at TIMESTAMP NULL,
  price DECIMAL(10,2),
  payment_id BIGINT,
  FOREIGN KEY (event_id) REFERENCES events(id)
);

-- 5. Payments Table
CREATE TABLE payments (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  booking_id BIGINT,
  amount DECIMAL(10,2),
  currency VARCHAR(10),
  method VARCHAR(50),
  status VARCHAR(50),
  paid_at TIMESTAMP NULL,
  FOREIGN KEY (booking_id) REFERENCES bookings(id)
);

-- 6. Messages Table
CREATE TABLE messages (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  booking_id BIGINT,
  sender_id BIGINT,
  receiver_id BIGINT,
  content TEXT,
  sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (booking_id) REFERENCES bookings(id)
);

-- 7. Reviews Table
CREATE TABLE reviews (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  booking_id BIGINT,
  reviewer_id BIGINT,
  rating INT,
  comment TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (booking_id) REFERENCES bookings(id)
);

-- Summary:
-- Total Tables Created: 7
-- 1. users
-- 2. interpreters
-- 3. events
-- 4. bookings
-- 5. payments
-- 6. messages
-- 7. reviews

