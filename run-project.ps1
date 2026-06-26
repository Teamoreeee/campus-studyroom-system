# Campus Study Room System - One-click Run
# This script checks prerequisites, builds jars if needed, starts all services, and opens the browser.
$Root = $PSScriptRoot
$LogsDir = Join-Path $Root "logs"
if (-not (Test-Path $LogsDir)) {
    New-Item -ItemType Directory -Path $LogsDir | Out-Null
}

function Write-Status($Message, $Color = "White") {
    Write-Host $Message -ForegroundColor $Color
}

# Step 1: Check and start MySQL
Write-Status "Step 1/6: Checking MySQL service..." "Cyan"
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

$MySQLRunning = $false
if (Test-MySQLPort) {
    $MySQLRunning = $true
    Write-Status "MySQL is already accessible on port 3306." "Green"
} else {
    $MySQLServices = Get-Service -Name "MySQL*" -ErrorAction SilentlyContinue
    if (-not $MySQLServices) {
        Write-Status "[WARNING] No MySQL Windows service found." "Yellow"
    } else {
        $RunningService = $MySQLServices | Where-Object { $_.Status -eq 'Running' } | Select-Object -First 1
        if ($RunningService) {
            $MySQLRunning = $true
            Write-Status "MySQL service '$($RunningService.Name)' is already running." "Green"
        } else {
            Write-Status "MySQL services found: $($MySQLServices.Name -join ', ')" "Yellow"
            foreach ($Svc in $MySQLServices) {
                Write-Status "Attempting to start service '$($Svc.Name)'..." "Yellow"
                try {
                    Start-Service $Svc.Name -ErrorAction Stop
                    Start-Sleep -Seconds 3
                    if ((Get-Service $Svc.Name).Status -eq 'Running') {
                        $MySQLRunning = $true
                        Write-Status "MySQL service '$($Svc.Name)' started successfully." "Green"
                        break
                    }
                } catch {
                    Write-Status "Failed to start '$($Svc.Name)': $($_.Exception.Message)" "Red"
                }
            }
        }
    }
}

if (-not $MySQLRunning) {
    Write-Status "[WARNING] Could not start MySQL automatically." "Yellow"
    $Continue = Read-Host "If you have already started MySQL manually, continue? (y/n)"
    if ($Continue -ne 'y') {
        exit 1
    }
}

# Step 2: Check Redis
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
    Write-Status "Redis is already accessible on port 6379." "Green"
} else {
    Write-Status "[WARNING] Redis is not running or not in PATH. Please start Redis manually." "Yellow"
    Write-Status "Common command: redis-server" "Yellow"
    $Continue = Read-Host "Continue anyway? (y/n)"
    if ($Continue -ne 'y') {
        exit 1
    }
}

# Step 3: Kill existing Java processes
Write-Status "Step 3/6: Stopping any existing Java services..." "Cyan"
try {
    # Use taskkill for forceful termination
    $null = cmd /c "taskkill /F /IM java.exe 2>nul"
    $null = cmd /c "taskkill /F /IM javaw.exe 2>nul"
    Start-Sleep -Seconds 3
    # Also try PowerShell way
    Get-Process -Name "java" -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
    Get-Process -Name "javaw" -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 2
    Write-Status "Existing Java services stopped." "Green"
} catch {
    Write-Status "[WARNING] Could not stop some Java processes. Continuing..." "Yellow"
}

# Step 4: Build jars if needed
Write-Status "Step 4/6: Checking/building backend jars..." "Cyan"
$NeedBuild = $false
$Services = @("campus-auth", "campus-user", "campus-room", "campus-reservation", "campus-attendance", "campus-ai", "campus-gateway")
foreach ($Svc in $Services) {
    $Jar = Get-ChildItem -Path "$Root\backend\$Svc\target\$Svc-*.jar" -ErrorAction SilentlyContinue | Select-Object -First 1
    if (-not $Jar) {
        $NeedBuild = $true
        Write-Status "Jar missing for $Svc. Rebuild required." "Yellow"
        break
    }
    # Rebuild if pom, config files, or Java sources are newer than jar
    $FilesToCheck = @(
        "$Root\backend\$Svc\pom.xml",
        "$Root\backend\$Svc\src\main\resources\application.yml",
        "$Root\backend\$Svc\src\main\resources\application-dev.yml"
    )
    foreach ($File in $FilesToCheck) {
        $Item = Get-Item -Path $File -ErrorAction SilentlyContinue
        if ($Item -and $Jar.LastWriteTime -lt $Item.LastWriteTime) {
            Write-Status "$($Item.Name) is newer than jar for $Svc. Rebuild required." "Yellow"
            $NeedBuild = $true
            break
        }
    }
    if (-not $NeedBuild) {
        $JavaFiles = Get-ChildItem -Path "$Root\backend\$Svc\src\main\java" -Recurse -Filter "*.java" -ErrorAction SilentlyContinue
        $NewestJava = $JavaFiles | Sort-Object LastWriteTime -Descending | Select-Object -First 1
        if ($NewestJava -and $Jar.LastWriteTime -lt $NewestJava.LastWriteTime) {
            Write-Status "Java source is newer than jar for $Svc. Rebuild required." "Yellow"
            $NeedBuild = $true
        }
    }
    if ($NeedBuild) { break }
}

if ($NeedBuild) {
    Write-Status "Building all backend modules..." "Yellow"
    $BuildLog = Join-Path $LogsDir "maven-build.log"
    $BuildCmd = "mvn clean package -DskipTests"
    $BuildProcess = Start-Process -FilePath "cmd.exe" -ArgumentList "/c", "cd /d `"$Root`" && $BuildCmd > `"$BuildLog`" 2>&1" -WindowStyle Normal -Wait -PassThru
    if ($BuildProcess.ExitCode -ne 0) {
        Write-Status "[ERROR] Maven build failed. Check $BuildLog for details." "Red"
        Read-Host "Press Enter to exit"
        exit 1
    }
    Write-Status "Build completed." "Green"
} else {
    Write-Status "Jars are up to date. Skipping build." "Green"
}

# Step 5: Start services
Write-Status "Step 5/6: Starting backend services..." "Cyan"
$ServiceConfigs = @(
    @{ Name = "campus-auth"; Port = 8001; JarPattern = "backend\campus-auth\target\campus-auth-*.jar" },
    @{ Name = "campus-user"; Port = 8002; JarPattern = "backend\campus-user\target\campus-user-*.jar" },
    @{ Name = "campus-room"; Port = 8003; JarPattern = "backend\campus-room\target\campus-room-*.jar" },
    @{ Name = "campus-reservation"; Port = 8004; JarPattern = "backend\campus-reservation\target\campus-reservation-*.jar" },
    @{ Name = "campus-attendance"; Port = 8005; JarPattern = "backend\campus-attendance\target\campus-attendance-*.jar" },
    @{ Name = "campus-ai"; Port = 8006; JarPattern = "backend\campus-ai\target\campus-ai-*.jar" },
    @{ Name = "campus-gateway"; Port = 8000; JarPattern = "backend\campus-gateway\target\campus-gateway-*.jar" }
)

# Find java executable
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
    Write-Status "[$Index/$($ServiceConfigs.Count)] Starting $($Svc.Name):$($Svc.Port)"
    Start-Process -FilePath "cmd.exe" -ArgumentList "/k", "cd /d `"$Root`" && `"$JavaExe`" -jar `"$($Jar.FullName)`" > `"$LogFile`" 2>&1" -WindowStyle Minimized
    Start-Sleep -Seconds 6
    $Index++
}

# Step 6: Wait for gateway
Write-Status "Step 6/6: Waiting for gateway to be ready..." "Cyan"
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
    Write-Status "[WARNING] Gateway health check did not return 200 within 60 seconds." "Yellow"
    Write-Status "Please check logs in: $LogsDir" "Yellow"
}

# Start frontend
Write-Status "Starting frontend..." "Cyan"
Start-Process -FilePath "cmd.exe" -ArgumentList "/k", "cd /d `"$Root\frontend`" && npm run dev" -WindowStyle Normal
Start-Sleep -Seconds 3

# Open browser
if ($Ready) {
    Write-Status "Opening browser..." "Cyan"
    Start-Process "http://localhost:3000"
}

Write-Status "" "White"
Write-Status "==========================================" "Green"
Write-Status "All services started." "Green"
Write-Status "Frontend: http://localhost:3000" "Green"
Write-Status "Gateway:  http://localhost:8000" "Green"
Write-Status "Logs:     $LogsDir" "Green"
Write-Status "==========================================" "Green"
Write-Status "Press any key to close this window..." "White"
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
