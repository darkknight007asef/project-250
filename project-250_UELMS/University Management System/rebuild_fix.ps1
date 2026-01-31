# PowerShell script to rebuild the application after database fixes
Write-Host "Rebuilding University Management System..." -ForegroundColor Green
Write-Host ""

$projectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$srcDir = Join-Path $projectDir "src"
$buildDir = Join-Path $projectDir "build\classes"
$distDir = Join-Path $projectDir "dist"
$libDir = Join-Path $distDir "lib"

# Create build directory
New-Item -ItemType Directory -Force -Path $buildDir | Out-Null

# Build classpath from lib directory
$classpath = ""
$jars = Get-ChildItem -Path $libDir -Filter "*.jar"
foreach ($jar in $jars) {
    if ($classpath -ne "") { $classpath += ";" }
    $classpath += $jar.FullName
}

Write-Host "Classpath: $classpath" -ForegroundColor Yellow
Write-Host ""

# Find all Java files
$javaFiles = Get-ChildItem -Path $srcDir -Filter "*.java" -Recurse

Write-Host "Compiling Java files..." -ForegroundColor Cyan
javac -d $buildDir -cp $classpath -encoding UTF-8 $javaFiles.FullName

if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilation successful!" -ForegroundColor Green
    
    # Copy resources
    Write-Host "Copying resources..." -ForegroundColor Cyan
    $resourceFiles = Get-ChildItem -Path $srcDir -Include "*.properties","*.xml","*.jpg","*.png" -Recurse
    foreach ($file in $resourceFiles) {
        $relativePath = $file.FullName.Substring($srcDir.Length + 1)
        $destPath = Join-Path $buildDir $relativePath
        $destDir = Split-Path -Parent $destPath
        New-Item -ItemType Directory -Force -Path $destDir | Out-Null
        Copy-Item $file.FullName $destPath -Force
    }
    
    # Create JAR
    Write-Host "Creating JAR file..." -ForegroundColor Cyan
    $jarFile = Join-Path $distDir "University_Management_System.jar"
    
    # Check if JAR is locked (application might be running)
    $jarLocked = $false
    try {
        $fileStream = [System.IO.File]::Open($jarFile, 'Open', 'ReadWrite', 'None')
        $fileStream.Close()
    } catch {
        $jarLocked = $true
        Write-Host "Warning: JAR file is locked (application may be running). Creating backup..." -ForegroundColor Yellow
        $backupFile = $jarFile + ".backup"
        if (Test-Path $backupFile) { Remove-Item $backupFile -Force }
        if (Test-Path $jarFile) { Move-Item $jarFile $backupFile -Force }
    }
    
    # Create manifest
    $manifestFile = Join-Path $buildDir "MANIFEST.MF"
    @"
Manifest-Version: 1.0
Main-Class: university.management.system.Login
Class-Path: lib/activation-1.1.1.jar lib/javax.mail-1.6.2.jar lib/jcalendar-tz-1.3.3-4.jar lib/mysql-connector-java-8.0.28.jar lib/rs2xml.jar
"@ | Out-File -FilePath $manifestFile -Encoding ASCII
    
    # Create JAR
    jar cfm $jarFile $manifestFile -C $buildDir .
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "JAR created successfully!" -ForegroundColor Green
        Write-Host ""
        Write-Host "Build complete! JAR location: $jarFile" -ForegroundColor Green
        if ($jarLocked) {
            Write-Host ""
            Write-Host "Note: The old JAR was locked. Please close the application and run this script again to update the JAR." -ForegroundColor Yellow
        }
    } else {
        Write-Host "JAR creation failed!" -ForegroundColor Red
        if ($jarLocked) {
            Write-Host "Please close the application and try again." -ForegroundColor Yellow
        }
    }
} else {
    Write-Host "Compilation failed!" -ForegroundColor Red
    Write-Host "Please check the errors above." -ForegroundColor Yellow
}

