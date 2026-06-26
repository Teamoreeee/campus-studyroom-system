import subprocess
import os
import time

java = r"E:\JDK17\bin\java.exe"
root = r"C:\Users\TEAM0RE\OneDrive\桌面\软件架构技术\campus-studyroom"
log_dir = os.path.join(root, "logs")
os.makedirs(log_dir, exist_ok=True)

# 从 Windows 用户环境变量读取智谱 API Key（避免当前 shell 没继承新 key）
api_key = os.environ.get("ZHIPU_API_KEY")
if not api_key and os.name == "nt":
    result = subprocess.run(
        ["powershell.exe", "-Command", "[Environment]::GetEnvironmentVariable('ZHIPU_API_KEY', 'User')"],
        capture_output=True, text=True
    )
    api_key = result.stdout.strip()

services = [
    ("campus-auth", 8001, "campus-auth-1.0-SNAPSHOT-v2.jar"),
    ("campus-user", 8002, "campus-user-1.0-SNAPSHOT.jar"),
    ("campus-room", 8003, "campus-room-1.0-SNAPSHOT.jar"),
    ("campus-reservation", 8004, "campus-reservation-1.0-SNAPSHOT.jar"),
    ("campus-attendance", 8005, "campus-attendance-1.0-SNAPSHOT.jar"),
    ("campus-ai", 8006, "campus-ai-1.0-SNAPSHOT.jar"),
    ("campus-gateway", 8000, "campus-gateway-1.0-SNAPSHOT.jar"),
]

# Windows 下默认最小化启动，避免弹出大量控制台窗口
startupinfo = None
if os.name == "nt":
    startupinfo = subprocess.STARTUPINFO()
    startupinfo.dwFlags |= subprocess.STARTF_USESHOWWINDOW
    startupinfo.wShowWindow = 7  # SW_SHOWMINNOACTIVE

base_env = os.environ.copy()
if api_key:
    base_env["ZHIPU_API_KEY"] = api_key

for name, port, jar in services:
    jar_path = os.path.join(root, "backend", name, "target", jar)
    log_path = os.path.join(log_dir, f"{name}-2026-06-25.log")
    cmd = [
        java,
        "-jar",
        jar_path,
        f"--server.port={port}",
        f"--logging.file.name={log_path}",
    ]
    log_file = open(log_path, "a", encoding="utf-8")
    subprocess.Popen(
        cmd,
        stdout=log_file,
        stderr=subprocess.STDOUT,
        startupinfo=startupinfo,
        env=base_env,
    )
    print(f"Started {name} on port {port}")
    time.sleep(4 if name == "campus-auth" else 3)

print("All services started!")
