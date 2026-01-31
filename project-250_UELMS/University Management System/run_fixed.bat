@echo off
if not exist "bin" mkdir bin
echo Compiling Source Code...
javac -cp "dist/lib/*" -d bin src/university/management/system/models/*.java src/university/management/system/dao/*.java src/university/management/system/utils/*.java src/university/management/system/charts/*.java src/university/management/system/*.java
if %errorlevel% neq 0 (
    echo Compilation Failed!
    pause
    exit /b
)
echo Copying Configuration...
copy db.properties bin\
echo Starting University Management System...
java -cp "bin;dist/lib/*" university.management.system.Login
pause
