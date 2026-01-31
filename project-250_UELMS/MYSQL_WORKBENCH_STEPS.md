# MySQL Workbench Setup Steps

## Important: Execute Each Step Separately!

In MySQL Workbench, **DO NOT** run the entire script at once. Execute each step individually:

### Step 1: Create users table
Copy and execute this block:
```sql
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
```

### Step 2: Create forget_pass table
Copy and execute this block:
```sql
CREATE TABLE IF NOT EXISTS forget_pass (
    email VARCHAR(100) DEFAULT NULL,
    username VARCHAR(100) DEFAULT NULL,
    password VARCHAR(100) DEFAULT NULL
);
```

### Step 3: Insert admin user
Copy and execute this block:
```sql
INSERT IGNORE INTO users (username, password, role, is_active) 
VALUES ('admin', 'admin123', 'ADMIN', 1);
```

### Step 4: Verify tables exist
Execute each of these one by one:
```sql
SHOW TABLES LIKE 'users';
```
```sql
SHOW TABLES LIKE 'forget_pass';
```
```sql
SELECT * FROM users WHERE role = 'ADMIN';
```

## Expected Results:
- Step 1: Should show "1 row(s) affected"
- Step 2: Should show "1 row(s) affected" 
- Step 3: Should show "1 row(s) affected"
- Step 4: Should show both tables exist and 1 admin user

If any step fails, check that you're connected to the correct Railway database!
