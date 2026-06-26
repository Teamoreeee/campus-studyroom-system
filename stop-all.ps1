# Campus Study Room System - Stop All Services
Write-Host "Stopping all Campus Study Room services..."

try {
    Get-Process -Name "java" -ErrorAction SilentlyContinue | Stop-Process -Force
    Get-Process -Name "javaw" -ErrorAction SilentlyContinue | Stop-Process -Force
    Write-Host "All Java services stopped." -ForegroundColor Green
} catch {
    Write-Host "Some processes may have already been stopped." -ForegroundColor Yellow
}

Write-Host "Press any key to close this window..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
