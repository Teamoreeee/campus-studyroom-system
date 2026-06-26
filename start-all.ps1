# Campus Study Room System - Start All Services
$Root = $PSScriptRoot

# Try to find java.exe from JAVA_HOME, otherwise use common paths
$JavaExe = $null
if ($env:JAVA_HOME) {
    $Candidate = Join-Path $env:JAVA_HOME "bin\java.exe"
    if (Test-Path $Candidate) {
        $JavaExe = $Candidate
    }
}
if (-not $JavaExe) {
    $CommonPaths = @(
        "E:\JDK17\bin\java.exe",
        "C:\Program Files\Java\jdk-17\bin\java.exe",
        "C:\Program Files\Eclipse Adoptium\jdk-17.*\bin\java.exe",
        "C:\Program Files\Amazon Corretto\jdk17.*\bin\java.exe"
    )
    foreach ($Path in $CommonPaths) {
        $Found = Get-ChildItem -Path $Path -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($Found) {
            $JavaExe = $Found.FullName
            break
        }
    }
}
if (-not $JavaExe) {
    $JavaExe = "java"
}

Write-Host "=========================================="
Write-Host " Campus Study Room System - Start All"
Write-Host "=========================================="
Write-Host "Java executable: $JavaExe"
Write-Host "Please make sure MySQL and Redis are running!"
Write-Host ""

$Services = @(
    @{ Name = "campus-auth"; Port = 8001; JarPattern = "backend\campus-auth\target\campus-auth-*.jar" },
    @{ Name = "campus-user"; Port = 8002; JarPattern = "backend\campus-user\target\campus-user-*.jar" },
    @{ Name = "campus-room"; Port = 8003; JarPattern = "backend\campus-room\target\campus-room-*.jar" },
    @{ Name = "campus-reservation"; Port = 8004; JarPattern = "backend\campus-reservation\target\campus-reservation-*.jar" },
    @{ Name = "campus-attendance"; Port = 8005; JarPattern = "backend\campus-attendance\target\campus-attendance-*.jar" },
    @{ Name = "campus-ai"; Port = 8006; JarPattern = "backend\campus-ai\target\campus-ai-*.jar" },
    @{ Name = "campus-gateway"; Port = 8000; JarPattern = "backend\campus-gateway\target\campus-gateway-*.jar" }
)

$Index = 1
foreach ($Svc in $Services) {
    $Jar = Get-ChildItem -Path "$Root\$($Svc.JarPattern)" | Select-Object -First 1
    if (-not $Jar) {
        Write-Host "[ERROR] Jar not found: $($Svc.JarPattern)" -ForegroundColor Red
        continue
    }
    Write-Host "[$Index/$($Services.Count)] Starting $($Svc.Name):$($Svc.Port)"
    Start-Process -FilePath "cmd.exe" -ArgumentList "/k", "cd /d `"$Root`" && `"$JavaExe`" -jar `"$($Jar.FullName)`"" -WindowStyle Minimized
    Start-Sleep -Seconds 6
    $Index++
}

Write-Host ""
Write-Host "Waiting for gateway to be ready..."
$Ready = $false
for ($i = 0; $i -lt 30; $i++) {
    try {
        $Response = Invoke-WebRequest -Uri "http://localhost:8000/actuator/health" -UseBasicParsing -TimeoutSec 2 -ErrorAction Stop
        if ($Response.StatusCode -eq 200) {
            Write-Host "Gateway is ready." -ForegroundColor Green
            $Ready = $true
            break
        }
    } catch {
        Start-Sleep -Seconds 1
    }
}
if (-not $Ready) {
    Write-Host "[WARNING] Gateway health check did not return 200. Continuing anyway..." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "[$Index/8] Starting frontend http://localhost:3000"
Start-Process -FilePath "cmd.exe" -ArgumentList "/k", "cd /d `"$Root\frontend`" && npm run dev" -WindowStyle Normal

Write-Host ""
Write-Host "=========================================="
Write-Host "All services started. Backend windows are minimized."
Write-Host "Frontend: http://localhost:3000"
Write-Host "Gateway:  http://localhost:8000"
Write-Host "=========================================="
Write-Host "Press any key to close this window..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
