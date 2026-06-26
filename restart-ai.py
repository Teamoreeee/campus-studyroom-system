import subprocess
import os
import time

java = r"E:\JDK17\bin\java.exe"
root = r"C:\Users\TEAM0RE\OneDrive\桌面\软件架构技术\campus-studyroom"
log_dir = os.path.join(root, "logs")
os.makedirs(log_dir, exist_ok=True)

# 从 Windows 用户环境变量读取智谱 API Key
api_key = os.environ.get("ZHIPU_API_KEY")
if not api_key:
    # 尝试从 PowerShell 读取
    result = subprocess.run(
        ["powershell.exe", "-Command", "[Environment]::GetEnvironmentVariable('ZHIPU_API_KEY', 'User')"],
        capture_output=True, text=True
    )
    api_key = result.stdout.strip()

jar_path = os.path.join(root, "backend", "campus-ai", "target", "campus-ai-1.0-SNAPSHOT.jar")
log_path = os.path.join(log_dir, "campus-ai-2026-06-25.log")
cmd = [
    java,
    "-jar",
    jar_path,
    "--server.port=8006",
    f"--logging.file.name={log_path}",
]

env = os.environ.copy()
if api_key:
    env["ZHIPU_API_KEY"] = api_key
    print(f"Starting campus-ai with ZHIPU_API_KEY={api_key[:8]}...")
else:
    print("Warning: ZHIPU_API_KEY not found, starting without it")

# Windows 下默认最小化启动，避免弹出大量控制台窗口
startupinfo = None
if os.name == "nt":
    startupinfo = subprocess.STARTUPINFO()
    startupinfo.dwFlags |= subprocess.STARTF_USESHOWWINDOW
    startupinfo.wShowWindow = 7  # SW_SHOWMINNOACTIVE

log_file = open(log_path, "a", encoding="utf-8")
subprocess.Popen(
    cmd,
    stdout=log_file,
    stderr=subprocess.STDOUT,
    env=env,
    startupinfo=startupinfo,
)
print("campus-ai started on port 8006")
