-- Railway Database Setup Script for University Management System
-- Execute each section separately in MySQL Workbench

-- Step 1: Create users table for authentication
CREATE TABLE IF NOT EXISTS users (
    id INT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) DEFAULT NULL,
    registration_no VARCHAR(20) DEFAULT NULL,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(10) NOT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_registration_no (registration_no)
);

-- Step 2: Create forget_pass table for password recovery  
CREATE TABLE IF NOT EXISTS forget_pass (
    email VARCHAR(100) DEFAULT NULL,
    username VARCHAR(100) DEFAULT NULL,
    password VARCHAR(100) DEFAULT NULL
);

-- Step 3: Insert default admin user (run this after tables are created)
INSERT IGNORE INTO users (username, password, role, is_active) 
VALUES ('admin', 'admin123', 'ADMIN', 1);

-- Step 4: Verify setup (run these one by one to check)
SHOW TABLES LIKE 'users';
SHOW TABLES LIKE 'forget_pass';
SELECT * FROM users WHERE role = 'ADMIN';
