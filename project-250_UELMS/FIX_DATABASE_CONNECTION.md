# Fixing the "null connection" Database Error

## Problem
When trying to register or login, you get an error:
```
cannot invoke "java.sql.connection.prepareStatement(String)" because "c.c" is null
```

## Root Cause
The MySQL JDBC driver (mysql-connector-java.jar) is missing from your classpath. This causes the database connection to fail, leaving the Connection object null.

## Solution

### Step 1: Download MySQL Connector/J
1. Go to: https://dev.mysql.com/downloads/connector/j/
2. Select version **8.0.28** (or compatible version like 8.0.33)
3. Download the **Platform Independent** ZIP file

### Step 2: Extract and Place the JAR
1. Extract the downloaded ZIP file
2. Find the file named `mysql-connector-java-8.0.28.jar` (version may vary)
3. Copy it to your project's `lib` folder:
   ```
   University Management System/lib/mysql-connector-java-8.0.28.jar
   ```

### Step 3: Rebuild the Project
1. **If using NetBeans:**
   - Right-click project → Clean and Build

2. **If using command line:**
   ```
   cd "University Management System"
   ant clean
   ant build
   ```

### Step 4: Update Your Database Configuration

Create or update `University Management System/db.properties`:
```properties
# For Local MySQL:
db.url=jdbc:mysql://localhost:3306/universitymanagementsystem?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.pass=your_password

# For Railway (Cloud):
db.url=jdbc:mysql://mysql-[your-service].e.aivencloud.com:[port]/[database]?useSSL=true&requireSSL=true&verifyServerCertificate=false&serverTimezone=UTC
db.user=your_username
db.pass=your_password
```

### Step 5: Verify Database Connection

Before running the application:
1. **For Local MySQL:**
   - Start your MySQL server
   - Create the database: `CREATE DATABASE universitymanagementsystem;`
   - Verify you can connect with your credentials

2. **For Railway:**
   - Copy your connection string from the Railway dashboard
   - Update db.properties with the correct credentials

### Step 6: Run the Application

**From NetBeans:**
- Right-click project → Run

**From Command Line:**
```
cd "University Management System"
java -jar dist/University_Management_System.jar
```

**From IDE with IDE's Run Configuration**
- Make sure the project build includes the lib folder

## Verification

After starting the application, check the console output:
- ✅ If you see: `Database connected successfully!` - Connection is working
- ❌ If you see: `MySQL JDBC Driver not found!` - JAR file is missing (go back to Step 2)
- ❌ If you see: `Database connection error` - Check credentials and server is running

## Common Issues

| Error | Solution |
|-------|----------|
| `MySQL JDBC Driver not found` | Check mysql-connector-java JAR is in `lib/` folder and classpath is correct |
| `Connection refused` | Database server not running or wrong host/port in db.properties |
| `Access denied for user` | Wrong username/password in db.properties |
| `Unknown database` | Database doesn't exist - create it first |
| `SSL connection error` | For Railway, ensure `useSSL=true` and `verifyServerCertificate=false` are set |

## Quick Database Setup (Local)

If using local MySQL:

```sql
-- Login to MySQL
mysql -u root -p

-- Run these commands:
CREATE DATABASE universitymanagementsystem;
USE universitymanagementsystem;

-- The application will create tables automatically on first run
```

## Testing the Fix

1. Start the application
2. Click "Register as Student" or "Register as Admin"
3. You should no longer see the "null connection" error
4. The app should display the registration form without errors

If you still have errors after following these steps, check the console output for the detailed error message.
