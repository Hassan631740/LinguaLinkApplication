-- V6__add_calls_table.sql: Create calls table for call records

CREATE TABLE calls (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  booking_id BIGINT NOT NULL,
  client_id BIGINT NOT NULL,
  interpreter_id BIGINT NOT NULL,
  call_duration_seconds BIGINT NULL,
  start_time DATETIME NULL,
  end_time DATETIME NULL,
  status VARCHAR(50) NULL,
  recording_url VARCHAR(500) NULL,
  notes TEXT NULL,
  quality_rating INT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
  FOREIGN KEY (booking_id) REFERENCES bookings(id),
  FOREIGN KEY (client_id) REFERENCES users(id),
  FOREIGN KEY (interpreter_id) REFERENCES interpreters(id),
  INDEX idx_booking_id (booking_id),
  INDEX idx_client_id (client_id),
  INDEX idx_interpreter_id (interpreter_id),
  INDEX idx_status (status),
  INDEX idx_start_time (start_time)
);

