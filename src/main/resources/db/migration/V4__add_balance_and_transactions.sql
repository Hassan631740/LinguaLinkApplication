-- V4__add_balance_and_transactions.sql: Add balance to users and create transactions table

-- Add balance column to users table
ALTER TABLE users ADD COLUMN balance DECIMAL(10,2) DEFAULT 0.00 NOT NULL;

-- Create transactions table
CREATE TABLE transactions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  booking_id BIGINT,
  transaction_type VARCHAR(50) NOT NULL,
  amount DECIMAL(10,2) NOT NULL,
  currency VARCHAR(10) NOT NULL DEFAULT 'USD',
  balance_before DECIMAL(10,2),
  balance_after DECIMAL(10,2),
  status VARCHAR(50),
  payment_method VARCHAR(50),
  payment_reference VARCHAR(255),
  withdrawal_account_details TEXT,
  processed_at TIMESTAMP NULL,
  notes TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (booking_id) REFERENCES bookings(id),
  INDEX idx_user_id (user_id),
  INDEX idx_booking_id (booking_id),
  INDEX idx_transaction_type (transaction_type),
  INDEX idx_status (status),
  INDEX idx_created_at (created_at)
);

