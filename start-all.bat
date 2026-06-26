@echo off
cd /d "C:\Users\TEAM0RE\OneDrive\桌面\软件架构技术\campus-studyroom"

set "JAVA=E:\JDK17\bin\java.exe"
set "ROOT=C:\Users\TEAM0RE\OneDrive\桌面\软件架构技术\campus-studyroom"

start "campus-auth" cmd /c "%JAVA% -jar %ROOT%\backend\campus-auth\target\campus-auth-1.0-SNAPSHOT-v2.jar --server.port=8001 --logging.file.name=%ROOT%\logs\campus-auth-2026-06-25.log"
timeout /t 4 /nobreak > nul

start "campus-user" cmd /c "%JAVA% -jar %ROOT%\backend\campus-user\target\campus-user-1.0-SNAPSHOT.jar --server.port=8002 --logging.file.name=%ROOT%\logs\campus-user-2026-06-25.log"
timeout /t 3 /nobreak > nul

start "campus-room" cmd /c "%JAVA% -jar %ROOT%\backend\campus-room\target\campus-room-1.0-SNAPSHOT.jar --server.port=8003 --logging.file.name=%ROOT%\logs\campus-room-2026-06-25.log"
timeout /t 3 /nobreak > nul

start "campus-reservation" cmd /c "%JAVA% -jar %ROOT%\backend\campus-reservation\target\campus-reservation-1.0-SNAPSHOT.jar --server.port=8004 --logging.file.name=%ROOT%\logs\campus-reservation-2026-06-25.log"
timeout /t 3 /nobreak > nul

start "campus-attendance" cmd /c "%JAVA% -jar %ROOT%\backend\campus-attendance\target\campus-attendance-1.0-SNAPSHOT.jar --server.port=8005 --logging.file.name=%ROOT%\logs\campus-attendance-2026-06-25.log"
timeout /t 3 /nobreak > nul

start "campus-ai" cmd /c "%JAVA% -jar %ROOT%\backend\campus-ai\target\campus-ai-1.0-SNAPSHOT.jar --server.port=8006 --logging.file.name=%ROOT%\logs\campus-ai-2026-06-25.log"
timeout /t 3 /nobreak > nul

start "campus-gateway" cmd /c "%JAVA% -jar %ROOT%\backend\campus-gateway\target\campus-gateway-1.0-SNAPSHOT.jar --server.port=8000 --logging.file.name=%ROOT%\logs\campus-gateway-2026-06-25.log"

echo All services started!
