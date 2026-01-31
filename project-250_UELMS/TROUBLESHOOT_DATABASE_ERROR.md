# University Management System - Troubleshooting Guide

## Error: "cannot invoke prepareStatement because c.c is null"

### What This Means
The database connection is failing, leaving the Connection object null. This prevents any database operations.

### Quick Fix Checklist

- [ ] MySQL JDBC driver (mysql-connector-java.jar) is in the `lib` folder
- [ ] Database server is running and accessible
- [ ] `db.properties` has correct connection details
- [ ] Project has been rebuilt after adding the JAR file

---

## Step-by-Step Fix

### 1. Download MySQL Connector/J

**Option A: Manual Download**
1. Go to https://dev.mysql.com/downloads/connector/j/
2. Select **8.0.28** or **8.0.33** (or latest 8.x version)
3. Click "Platform Independent" ZIP download
4. Click "No thanks, just start my download"

**Option B: Using Command Line (curl or wget)**
```bash
# For version 8.0.33
curl -o mysql-connector-java-8.0.33.zip https://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-8.0.33.zip
```

### 2. Extract and Install the JAR

After downloading:

1. **Extract the ZIP file**
   - Use Windows Explorer's built-in ZIP extraction or WinRAR/7-Zip

2. **Locate the JAR**
   - Find `mysql-connector-java-8.0.XX.jar` in the extracted folder

3. **Copy to project**
   ```
   University Management System/
   └── lib/
       └── mysql-connector-java-8.0.33.jar  ← Place it here
   ```

4. **Verify placement**
   - The file should be directly in the `lib` folder
   - Not in any subdirectories

### 3. Configure Database Connection

**Create or Edit:** `University Management System/db.properties`

**For Local MySQL:**
```properties
db.url=jdbc:mysql://localhost:3306/universitymanagementsystem?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.pass=1716504726
```

**For Railway (Cloud Database):**
```properties
db.url=jdbc:mysql://mysql-XXXXX.e.aivencloud.com:18087/universitymanagementsystem?useSSL=true&requireSSL=true&verifyServerCertificate=false&serverTimezone=UTC
db.user=avnadmin
db.pass=YOUR_PASSWORD_HERE
```

### 4. Ensure Database Exists

**For Local MySQL:**

1. Start MySQL server
2. Open MySQL command line:
   ```bash
   mysql -u root -p
   ```
3. Create database:
   ```sql
   CREATE DATABASE universitymanagementsystem;
   ```
4. Verify:
   ```sql
   SHOW DATABASES;
   ```

**For Railway:**
- Database should already exist in your Railway service
- Just copy the connection string from Railway dashboard

### 5. Rebuild the Project

**Using NetBeans:**
1. Right-click project name
2. Select **Clean and Build**
3. Wait for "BUILD SUCCESSFUL"

**Using Command Line:**
```bash
cd "University Management System"
ant clean
ant build
```

**Using IDE (VS Code, IntelliJ, etc.):**
1. Ensure Ant is installed
2. Run build task

### 6. Run the Application

**From NetBeans:**
- Right-click project → Run

**From Command Line:**
```bash
cd "University Management System"
java -jar dist/University_Management_System.jar
```

**From IDE's Built-in Run:**
- Configure Run configuration to use the built JAR

---

## Verification

### Check Console Output for Success

**✅ SUCCESS - You'll see:**
```
Loaded db.properties from: ...
Database connected successfully!
```

**❌ FAILURE - Check for:**

| Error Message | Cause | Solution |
|---------------|-------|----------|
| `MySQL JDBC Driver not found` | JAR file missing from lib folder | Download and place JAR in `lib/` folder, rebuild |
| `Connection refused` | Database server not running | Start MySQL server |
| `Unknown database 'universitymanagementsystem'` | Database doesn't exist | Create database in MySQL |
| `Access denied for user` | Wrong username/password | Update db.properties with correct credentials |
| `Communication link failure` | Can't reach database server | Check host/port, firewall, network connection |

---

## Using the Setup Scripts

### Windows - PowerShell Script
```powershell
.\setup_database.ps1
```

### Windows - Batch Script
```batch
setup_database.bat
```

These scripts will:
1. Create the `lib` folder if needed
2. Check for MySQL Connector JAR
3. Create `db.properties` with default values
4. Show you what to do next

---

## Testing Your Fix

1. **Start the application**
2. **Click "Register as Student"** or **"Register as Admin"**
3. **If you see the registration form** without errors → ✅ CONNECTION WORKS
4. **If you see error message** → ❌ Check error details and troubleshoot

---

## Database Schema

The application automatically creates these tables on first run:

```sql
-- Users table (for authentication)
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

-- Password recovery table
CREATE TABLE IF NOT EXISTS forget_pass (
    email VARCHAR(100) DEFAULT NULL,
    username VARCHAR(100) DEFAULT NULL,
    password VARCHAR(100) DEFAULT NULL
);
```

Other tables for students, teachers, departments, marks, fees, etc., are created separately.

---

## Alternative: Using Railway (Cloud Database)

Instead of local MySQL:

1. **Create Railway account** at https://railway.app/
2. **Create MySQL database** in Railway
3. **Get connection details** from Railway dashboard
4. **Update db.properties** with Railway credentials:
   ```properties
   db.url=jdbc:mysql://mysql-XXXXX.e.aivencloud.com:XXXXX/universitymanagementsystem?useSSL=true&requireSSL=true&verifyServerCertificate=false&serverTimezone=UTC
   db.user=avnadmin
   db.pass=XXXXXXXXXXXXXXX
   ```
5. **Rebuild and run**

---

## Common Issues & Solutions

### Issue: "File not found: ./lib/mysql-connector-java.jar"
**Solution:** JAR is in wrong location or has different name
- Verify path: `University Management System/lib/mysql-connector-java-8.0.33.jar`
- Check spelling (case-sensitive on Linux/Mac)
- Use Explorer to verify file is there

### Issue: "Cannot create database connection"
**Solution:** 
1. Verify MySQL server is running: `mysql -u root -p`
2. Check port number (default 3306)
3. Check firewall rules
4. For Railway: Verify connection string is correct

### Issue: "Access denied for user 'root'@'localhost'"
**Solution:**
- Password in db.properties is wrong
- MySQL password needs to be reset: `ALTER USER 'root'@'localhost' IDENTIFIED BY 'newpassword';`
- Check if MySQL installed with different default password

### Issue: "SSLException" when connecting to Railway
**Solution:**
- Ensure `useSSL=true` and `verifyServerCertificate=false` in Railway connection string
- These are required for Railway's SSL certificate

### Issue: "Timeout connecting to MySQL server"
**Solution:**
- Database server is not running
- Wrong host/port
- Firewall blocking connection
- Network unreachable

---

## Getting Help

If you still have issues:

1. **Check the console output** for the exact error message
2. **Look up the MySQL error code** at https://dev.mysql.com/doc/mysql-errors/8.0/en/
3. **Verify all steps above** are completed
4. **Check the code in** `Conn.java` for connection details
5. **Test MySQL connection manually** using MySQL Workbench or command line

---

## Files Created/Modified

- ✓ `Conn.java` - Improved error messages and handling
- ✓ `db.properties` - Database configuration (create this)
- ✓ `lib/mysql-connector-java-8.0.33.jar` - JDBC driver (must download)
- ✓ `FIX_DATABASE_CONNECTION.md` - Detailed instructions
- ✓ `setup_database.ps1` - PowerShell setup script
- ✓ `setup_database.bat` - Batch setup script

---

**Last Updated:** November 2024
**Java Version Required:** 8 or higher
**MySQL Version:** 5.7 or higher (8.0 recommended)
**MySQL Connector/J Version:** 8.0.28 or higher
