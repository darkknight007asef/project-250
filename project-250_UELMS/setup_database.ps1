# Script to download MySQL Connector/J and set up the database connection
# Run this from the project root directory

param(
    [string]$version = "8.0.33",
    [string]$dbUrl = "jdbc:mysql://localhost:3306/universitymanagementsystem?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
    [string]$dbUser = "root",
    [string]$dbPass = "1716504726"
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "University Management System - Setup" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Create lib folder if it doesn't exist
$libFolder = ".\University Management System\lib"
if (-not (Test-Path $libFolder)) {
    Write-Host "Creating lib folder..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Path $libFolder -Force | Out-Null
    Write-Host "✓ lib folder created" -ForegroundColor Green
} else {
    Write-Host "✓ lib folder exists" -ForegroundColor Green
}

# Step 2: Check for MySQL Connector JAR
$jarFile = Get-ChildItem -Path $libFolder -Filter "mysql-connector-java*.jar" -ErrorAction SilentlyContinue
if ($jarFile) {
    Write-Host "✓ MySQL Connector found: $($jarFile.Name)" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "MySQL Connector/J NOT FOUND" -ForegroundColor Red
    Write-Host ""
    Write-Host "⚠️  WARNING: The MySQL JDBC driver is missing!" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "To fix this:" -ForegroundColor Yellow
    Write-Host "1. Download mysql-connector-java-$version.jar" -ForegroundColor Yellow
    Write-Host "   From: https://dev.mysql.com/downloads/connector/j/" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "2. Extract the ZIP and copy mysql-connector-java-$version.jar" -ForegroundColor Yellow
    Write-Host "   to: $libFolder" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "3. Run this script again or rebuild the project" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Alternative: Place any mysql-connector-java-*.jar in $libFolder" -ForegroundColor Yellow
}

Write-Host ""

# Step 3: Create or update db.properties
$dbPropsPath = ".\University Management System\db.properties"
$dbProps = @"
# Database configuration for University Management System
# Keep this file private. Do NOT commit it to version control.

# Local MySQL Configuration
db.url=$dbUrl
db.user=$dbUser
db.pass=$dbPass

# Railway Cloud Configuration (uncomment to use):
# db.url=jdbc:mysql://mysql-[your-id].e.aivencloud.com:[port]/universitymanagementsystem?useSSL=true&requireSSL=true&verifyServerCertificate=false&serverTimezone=UTC
# db.user=avnadmin
# db.pass=your_password
"@

if (Test-Path $dbPropsPath) {
    Write-Host "db.properties already exists at: $dbPropsPath" -ForegroundColor Yellow
    Write-Host "Review and update if needed (especially database credentials)" -ForegroundColor Yellow
} else {
    Write-Host "Creating db.properties..." -ForegroundColor Yellow
    Set-Content -Path $dbPropsPath -Value $dbProps
    Write-Host "✓ db.properties created at: $dbPropsPath" -ForegroundColor Green
}

Write-Host ""

# Step 4: Verify build.xml
$buildXmlPath = ".\University Management System\build.xml"
if (Test-Path $buildXmlPath) {
    Write-Host "✓ build.xml found" -ForegroundColor Green
    Write-Host "  Run 'ant clean' and 'ant build' to rebuild with library path" -ForegroundColor Cyan
} else {
    Write-Host "⚠️  build.xml not found at expected location" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Setup Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "✓ Project structure checked" -ForegroundColor Green
Write-Host "$(if ($jarFile) { '✓' } else { '✗' }) MySQL Connector JAR present" -ForegroundColor $(if ($jarFile) { 'Green' } else { 'Red' })
Write-Host "✓ db.properties configured" -ForegroundColor Green
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Cyan
Write-Host "1. If JAR is missing, download and place it in: $libFolder" -ForegroundColor Cyan
Write-Host "2. Ensure your MySQL database is running and accessible" -ForegroundColor Cyan
Write-Host "3. Update db.properties with your database credentials if needed" -ForegroundColor Cyan
Write-Host "4. Rebuild the project (ant clean && ant build)" -ForegroundColor Cyan
Write-Host "5. Run: java -jar dist/University_Management_System.jar" -ForegroundColor Cyan
Write-Host ""
