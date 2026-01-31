@echo off
REM Setup script for University Management System Database Configuration

echo.
echo ========================================
echo University Management System - Setup
echo ========================================
echo.

REM Step 1: Create lib folder
if not exist "University Management System\lib" (
    echo Creating lib folder...
    mkdir "University Management System\lib"
    echo [OK] lib folder created
) else (
    echo [OK] lib folder exists
)

echo.

REM Step 2: Check for MySQL Connector
setlocal enabledelayedexpansion
set "jar_found="
for %%f in ("University Management System\lib\mysql-connector-java*.jar") do (
    set "jar_found=%%~nf"
)

if defined jar_found (
    echo [OK] MySQL Connector found: !jar_found!
) else (
    echo.
    echo [WARNING] MySQL Connector/J NOT FOUND
    echo.
    echo To fix this:
    echo 1. Download mysql-connector-java from:
    echo    https://dev.mysql.com/downloads/connector/j/
    echo.
    echo 2. Extract the ZIP and copy mysql-connector-java-X.X.XX.jar
    echo    to: University Management System\lib\
    echo.
    echo 3. Run this script again or rebuild the project
    echo.
)

echo.

REM Step 3: Create or check db.properties
if exist "University Management System\db.properties" (
    echo [OK] db.properties already exists
    echo      Review and update credentials if needed
) else (
    echo Creating db.properties...
    (
        echo # Database configuration for University Management System
        echo # Keep this file private. Do NOT commit it to version control.
        echo.
        echo # Local MySQL Configuration
        echo db.url=jdbc:mysql://localhost:3306/universitymanagementsystem?useSSL=false^&allowPublicKeyRetrieval=true^&serverTimezone=UTC
        echo db.user=root
        echo db.pass=1716504726
        echo.
        echo # Railway Cloud Configuration (uncomment to use^):
        echo # db.url=jdbc:mysql://mysql-[your-id].e.aivencloud.com:[port]/universitymanagementsystem?useSSL=true^&requireSSL=true^&verifyServerCertificate=false^&serverTimezone=UTC
        echo # db.user=avnadmin
        echo # db.pass=your_password
    ) > "University Management System\db.properties"
    echo [OK] db.properties created
)

echo.
echo ========================================
echo Setup Summary
echo ========================================
echo.
echo [OK] Project structure checked
if defined jar_found (
    echo [OK] MySQL Connector JAR present
) else (
    echo [ERROR] MySQL Connector JAR not found
)
echo [OK] db.properties configured
echo.
echo Next Steps:
echo 1. If JAR is missing, download from MySQL official site
echo 2. Ensure your MySQL database is running
echo 3. Update db.properties with your credentials if needed
echo 4. Rebuild the project using NetBeans or:
echo    cd "University Management System"
echo    ant clean
echo    ant build
echo 5. Run: java -jar dist\University_Management_System.jar
echo.
echo ========================================
pause
