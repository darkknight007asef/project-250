@echo off
setlocal
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqldump.exe" -h localhost -P 3306 -u root -p1716504726 universitymanagementsystem --single-transaction --quick --routines --triggers --events --set-gtid-purged=OFF --default-character-set=utf8mb4 | "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -h sql12.freesqldatabase.com -P 3306 -u sql12806001 -pS4kGajYk4j -D sql12806001
endlocal

