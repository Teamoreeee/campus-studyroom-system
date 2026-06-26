# Campus Study Room System - VSCode 一键开发启动脚本
# 启动 MySQL/Redis 检查、后端微服务、前端 dev server

$Root = $PSScriptRoot
$LogsDir = Join-Path $Root "logs"
if (-not (Test-Path $LogsDir)) {
    New-Item -ItemType Directory -Path $LogsDir | Out-Null
}

function Write-Status($Message, $Color = "White") {
    Write-Host $Message -ForegroundColor $Color
}

# ========================
# 1. Check MySQL
# ========================
Write-Status "Step 1/6: Checking MySQL..." "Cyan"
function Test-MySQLPort {
    try {
        $Socket = New-Object Net.Sockets.TcpClient
        $Socket.Connect("localhost", 3306)
        $Socket.Close()
        return $true
    } catch {
        return $false
    }
}

if (Test-MySQLPort) {
    Write-Status "MySQL is running on port 3306." "Green"
} else {
    Write-Status "[WARN] MySQL is not running on port 3306. Please start it first." "Yellow"
    exit 1
}

# ========================
# 2. Check Redis
# ========================
Write-Status "Step 2/6: Checking Redis..." "Cyan"
function Test-RedisPort {
    try {
        $Socket = New-Object Net.Sockets.TcpClient
        $Socket.Connect("localhost", 6379)
        $Socket.Close()
        return $true
    } catch {
        return $false
    }
}

if (Test-RedisPort) {
    Write-Status "Redis is running on port 6379." "Green"
} else {
    Write-Status "[WARN] Redis is not running on port 6379. Please start it first." "Yellow"
    exit 1
}

# ========================
# 3. Check Database
# ========================
Write-Status "Step 3/6: Checking database campus_studyroom..." "Cyan"
try {
    $result = mysql -uroot -p123456 -e "USE campus_studyroom; SELECT COUNT(*) FROM user;" 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Status "Database campus_studyroom is ready." "Green"
    } else {
        throw
    }
} catch {
    Write-Status "Database not initialized, running init-db.bat..." "Yellow"
    & "$Root\init-db.bat"
    if ($LASTEXITCODE -ne 0) {
        Write-Status "[ERROR] Database initialization failed." "Red"
        exit 1
    }
}

# ========================
# 4. Stop existing Java processes
# ========================
Write-Status "Step 4/6: Stopping existing Java services..." "Cyan"
try {
    Get-Process -Name "java", "javaw" -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 2
    Write-Status "Cleaned up existing Java processes." "Green"
} catch {
    Write-Status "No existing Java processes." "Green"
}

# ========================
# 5. Build backend
# ========================
Write-Status "Step 5/6: Building backend..." "Cyan"
$BuildLog = Join-Path $LogsDir "maven-build.log"
$BuildErrLog = Join-Path $LogsDir "maven-build-error.log"
$BuildProcess = Start-Process -FilePath "mvn" -ArgumentList "clean", "package", "-DskipTests" -WorkingDirectory $Root -RedirectStandardOutput $BuildLog -RedirectStandardError $BuildErrLog -WindowStyle Normal -Wait -PassThru
if ($BuildProcess.ExitCode -ne 0) {
    Write-Status "[ERROR] Maven build failed, see $BuildLog and $BuildErrLog" "Red"
    exit 1
}
Write-Status "Backend build completed." "Green"

# ========================
# 6. Start backend services
# ========================
Write-Status "Step 6/6: Starting backend services..." "Cyan"
$ServiceConfigs = @(
    @{ Name = "campus-auth"; Port = 8001; JarPattern = "backend\campus-auth\target\campus-auth-*.jar" },
    @{ Name = "campus-user"; Port = 8002; JarPattern = "backend\campus-user\target\campus-user-*.jar" },
    @{ Name = "campus-room"; Port = 8003; JarPattern = "backend\campus-room\target\campus-room-*.jar" },
    @{ Name = "campus-reservation"; Port = 8004; JarPattern = "backend\campus-reservation\target\campus-reservation-*.jar" },
    @{ Name = "campus-attendance"; Port = 8005; JarPattern = "backend\campus-attendance\target\campus-attendance-*.jar" },
    @{ Name = "campus-ai"; Port = 8006; JarPattern = "backend\campus-ai\target\campus-ai-*.jar" },
    @{ Name = "campus-gateway"; Port = 8000; JarPattern = "backend\campus-gateway\target\campus-gateway-*.jar" }
)

$JavaExe = $null
if ($env:JAVA_HOME) {
    $Candidate = Join-Path $env:JAVA_HOME "bin\java.exe"
    if (Test-Path $Candidate) { $JavaExe = $Candidate }
}
if (-not $JavaExe) {
    $CommonPaths = @(
        "E:\JDK17\bin\java.exe",
        "C:\Program Files\Java\jdk-17\bin\java.exe",
        "C:\Program Files\Eclipse Adoptium\jdk-17.*\bin\java.exe"
    )
    foreach ($Path in $CommonPaths) {
        $Found = Get-ChildItem -Path $Path -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($Found) { $JavaExe = $Found.FullName; break }
    }
}
if (-not $JavaExe) { $JavaExe = "java" }

$Index = 1
foreach ($Svc in $ServiceConfigs) {
    $Jar = Get-ChildItem -Path "$Root\$($Svc.JarPattern)" | Select-Object -First 1
    if (-not $Jar) {
        Write-Status "[ERROR] Jar not found: $($Svc.JarPattern)" "Red"
        continue
    }
    $LogFile = Join-Path $LogsDir "$($Svc.Name).log"
    $ErrLogFile = Join-Path $LogsDir "$($Svc.Name)-error.log"
    Write-Status "[$Index/$($ServiceConfigs.Count)] Starting $($Svc.Name):$($Svc.Port)"
    $JarArg = '"{0}"' -f $Jar.FullName
    Start-Process -FilePath $JavaExe -ArgumentList "-jar", $JarArg, "--spring.profiles.active=dev" -WorkingDirectory $Root -RedirectStandardOutput $LogFile -RedirectStandardError $ErrLogFile -WindowStyle Minimized
    Start-Sleep -Seconds 5
    $Index++
}

# Wait for gateway ready
Write-Status "Waiting for gateway ready..." "Cyan"
$Ready = $false
for ($i = 0; $i -lt 60; $i++) {
    try {
        $Response = Invoke-WebRequest -Uri "http://localhost:8000/actuator/health" -UseBasicParsing -TimeoutSec 2 -ErrorAction Stop
        if ($Response.StatusCode -eq 200) {
            Write-Status "Gateway is ready." "Green"
            $Ready = $true
            break
        }
    } catch {
        Start-Sleep -Seconds 1
    }
}
if (-not $Ready) {
    Write-Status "[WARN] Gateway health check did not return 200 within 60 seconds." "Yellow"
}

# ========================
# 7. Start frontend
# ========================
Write-Status "Starting frontend..." "Cyan"
$NpmInstallProcess = Start-Process -FilePath "cmd.exe" -ArgumentList "/c", "npm install" -WorkingDirectory "$Root\frontend" -WindowStyle Normal -Wait -PassThru
if ($NpmInstallProcess.ExitCode -ne 0) {
    Write-Status "[ERROR] npm install failed." "Red"
    exit 1
}

Start-Process -FilePath "cmd.exe" -ArgumentList "/k", "npm run dev" -WorkingDirectory "$Root\frontend" -WindowStyle Normal
Start-Sleep -Seconds 3

# Open browser
if ($Ready) {
    Start-Process "http://localhost:3000"
}

Write-Status "" "White"
Write-Status "==========================================" "Green"
Write-Status "Startup complete!" "Green"
Write-Status "Frontend: http://localhost:3000" "Green"
Write-Status "Gateway:  http://localhost:8000" "Green"
Write-Status "Logs:     $LogsDir" "Green"
Write-Status "==========================================" "Green"
Write-Status "Press any key to close this window..." "White"
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
