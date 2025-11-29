-- REPAIR_FLYWAY_CHECKSUMS.sql
-- Run this SQL script to manually repair Flyway checksums after modifying migration files
-- This updates the checksums in flyway_schema_history to match the new migration files
-- 
-- INSTRUCTIONS:
-- 1. Connect to your MySQL database: mysql -u root -p lingualink
-- 2. Run this script: source REPAIR_FLYWAY_CHECKSUMS.sql
-- 3. Re-enable validation in application.yml: validate-on-migrate: true
-- 
-- Alternatively, if this is a development database, you can drop and recreate it:
-- DROP DATABASE lingualink; CREATE DATABASE lingualink;

USE lingualink;

-- First, let's see what's in the history table
SELECT version, description, checksum FROM flyway_schema_history ORDER BY installed_rank;

-- Update checksum for V2__seed_roles.sql (new checksum: -909283661)
UPDATE flyway_schema_history 
SET checksum = -909283661 
WHERE version = '2';

-- Update checksum for V3__seed_admin_user.sql (new checksum: 1592380823)
UPDATE flyway_schema_history 
SET checksum = 1592380823 
WHERE version = '3';

-- Verify the updates
SELECT version, description, checksum FROM flyway_schema_history ORDER BY installed_rank;

