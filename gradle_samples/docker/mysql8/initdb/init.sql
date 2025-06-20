CREATE DATABASE IF NOT EXISTS sample_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create user for application
CREATE USER IF NOT EXISTS 'test_user'@'%' IDENTIFIED BY 'test_password';
GRANT ALL PRIVILEGES ON sample_db.* TO 'test_user'@'%';
FLUSH PRIVILEGES;
