@echo off
cd /d "%~dp0"

set "PATH=E:\MySQL\MySQL Server 8.0\bin;C:\Program Files\MySQL\MySQL Server 8.0\bin;C:\Program Files\MySQL\MySQL Shell 8.0\bin\bin;%PATH%"

echo Initializing campus_studyroom database...

mysql --version > nul 2>&1
if errorlevel 1 (
    echo [ERROR] mysql command not found. Please check MySQL path.
    pause
    exit /b 1
)

mysql --default-character-set=utf8mb4 -uroot -p123456 -e "DROP DATABASE IF EXISTS campus_studyroom; CREATE DATABASE campus_studyroom CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2> init-db-error.log
if errorlevel 1 (
    echo [ERROR] Failed to create database. Check root password.
    type init-db-error.log
    pause
    exit /b 1
)

mysql --default-character-set=utf8mb4 -uroot -p123456 campus_studyroom < test\sql\mysql\campus_studyroom.sql 2> init-db-error.log
if errorlevel 1 (
    echo [ERROR] Failed to import SQL.
    type init-db-error.log
    pause
    exit /b 1
)

del init-db-error.log 2> nul
echo Database initialized successfully!
pause
