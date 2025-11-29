-- V7__add_admins_table.sql: Create admins table for administrative user details

CREATE TABLE admins (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL UNIQUE,
  department VARCHAR(255) NULL,
  employee_id VARCHAR(255) UNIQUE NULL,
  access_level VARCHAR(50) NULL,
  last_login DATETIME NULL,
  is_active BOOLEAN DEFAULT TRUE,
  permissions TEXT NULL,
  notes TEXT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_user_id (user_id),
  INDEX idx_employee_id (employee_id),
  INDEX idx_access_level (access_level),
  INDEX idx_is_active (is_active)
);

