@echo off
echo Railway Database Auto-Setup Script
echo ===================================
echo.
echo This script will automatically create the required tables in your Railway database.
echo.

REM Find MySQL executable
set MYSQL_EXE=
if exist "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" (
    set MYSQL_EXE="C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
)
if exist "C:\Program Files\MySQL\MySQL Server 5.7\bin\mysql.exe" (
    set MYSQL_EXE="C:\Program Files\MySQL\MySQL Server 5.7\bin\mysql.exe"
)
if exist "C:\Program Files (x86)\MySQL\MySQL Server 8.0\bin\mysql.exe" (
    set MYSQL_EXE="C:\Program Files (x86)\MySQL\MySQL Server 8.0\bin\mysql.exe"
)
if exist "C:\Program Files (x86)\MySQL\MySQL Server 5.7\bin\mysql.exe" (
    set MYSQL_EXE="C:\Program Files (x86)\MySQL\MySQL Server 5.7\bin\mysql.exe"
)

REM Try mysql in PATH
mysql --version >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    set MYSQL_EXE=mysql
)

if "%MYSQL_EXE%"=="" (
    echo ERROR: MySQL command-line client not found!
    echo.
    echo Please either:
    echo 1. Install MySQL Server, or
    echo 2. Use Option 2 ^(Python script^), or  
    echo 3. Use MySQL Workbench manually
    echo.
    pause
    exit /b 1
)

echo Found MySQL at: %MYSQL_EXE%
echo.
echo You need to provide your Railway database connection details:
echo.

set /p DB_HOST="Enter Railway Host (e.g., containers-us-west-123.railway.app): "
set /p DB_PORT="Enter Railway Port (e.g., 6543): "
set /p DB_NAME="Enter Database Name (usually 'railway'): "
set /p DB_USER="Enter Username (usually 'root'): "
set /p DB_PASS="Enter Password: "

echo.
echo Connecting to Railway database and setting up tables...
echo.

%MYSQL_EXE% -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASS% %DB_NAME% < railway_setup.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo SUCCESS! Database setup completed.
    echo.
    echo Default admin user created:
    echo Username: admin
    echo Password: admin123
    echo.
    echo You can now run your JAR file!
) else (
    echo.
    echo ERROR: Database setup failed.
    echo Please check your connection details and try again.
    echo.
    echo Alternative: Use MySQL Workbench with the step-by-step instructions.
)

echo.
pause
