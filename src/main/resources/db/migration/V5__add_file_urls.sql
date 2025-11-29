-- V5__add_file_urls.sql: Add file URL columns for avatars, icons, and certificates

-- Add avatar_url column to users table
ALTER TABLE users ADD COLUMN avatar_url VARCHAR(500) NULL;

-- Add icon_url column to events table
ALTER TABLE events ADD COLUMN icon_url VARCHAR(500) NULL;

-- Add certificate_urls column to interpreters table (JSON array)
ALTER TABLE interpreters ADD COLUMN certificate_urls JSON NULL;

