@echo off
cd /d "%~dp0"

set "PATH=E:\JDK17\bin;C:\Program Files\Java\jdk-17\bin;E:\MySQL\MySQL Server 8.0\bin;C:\Program Files\MySQL\MySQL Server 8.0\bin;%PATH%"
set "BUILD_DIR=C:\temp\campus-studyroom-build"

echo ==========================================
echo Campus Study Room - Backend Startup
echo ==========================================
echo.

java -version > nul 2>&1
if errorlevel 1 (
    echo [ERROR] java command not found. Please install JDK17.
    pause
    exit /b 1
)

echo Please confirm:
echo 1. MySQL is running on port 3306, root password is 123456
echo 2. Redis is running on port 6379
echo 3. init-db.bat has been executed successfully
echo.
pause

echo Starting campus-auth ...
start "campus-auth" cmd /c "java -jar %BUILD_DIR%\backend\campus-auth\target\campus-auth-1.0-SNAPSHOT-v2.jar"
timeout /t 4 /nobreak > nul

echo Starting campus-user ...
start "campus-user" cmd /c "java -jar %BUILD_DIR%\backend\campus-user\target\campus-user-1.0-SNAPSHOT.jar"
timeout /t 3 /nobreak > nul

echo Starting campus-room ...
start "campus-room" cmd /c "java -jar %BUILD_DIR%\backend\campus-room\target\campus-room-1.0-SNAPSHOT.jar"
timeout /t 3 /nobreak > nul

echo Starting campus-reservation ...
start "campus-reservation" cmd /c "java -jar %BUILD_DIR%\backend\campus-reservation\target\campus-reservation-1.0-SNAPSHOT.jar"
timeout /t 3 /nobreak > nul

echo Starting campus-attendance ...
start "campus-attendance" cmd /c "java -jar %BUILD_DIR%\backend\campus-attendance\target\campus-attendance-1.0-SNAPSHOT.jar"
timeout /t 3 /nobreak > nul

echo Starting campus-ai ...
start "campus-ai" cmd /c "java -jar %BUILD_DIR%\backend\campus-ai\target\campus-ai-1.0-SNAPSHOT.jar"
timeout /t 3 /nobreak > nul

echo Starting campus-gateway ...
start "campus-gateway" cmd /c "java -jar %BUILD_DIR%\backend\campus-gateway\target\campus-gateway-1.0-SNAPSHOT.jar"

echo.
echo All services started!
echo Gateway: http://localhost:8000
echo API Docs: http://localhost:8000/swagger-ui.html
echo.
pause
