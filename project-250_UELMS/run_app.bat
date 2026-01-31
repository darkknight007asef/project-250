@echo off
echo Starting University Management System...
echo.
echo Make sure you have:
echo 1. Run the SQL script in Railway (railway_setup.sql)
echo 2. Updated dist/db.properties with your Railway credentials
echo.
pause
echo.
echo Launching application...
cd "University Management System\dist"
java -jar University_Management_System.jar
pause
