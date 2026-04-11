$ErrorActionPreference = "Continue"
$body = @{
    username = "admin"
    password = "admin123"
    captcha = "1234"
    captchaId = "001948c15c364e4194f6cb8953f829f0"
} | ConvertTo-Json

Write-Host "Testing login..."
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" -Method POST -Body $body -ContentType "application/json"
    Write-Host "Response:" -ForegroundColor Green
    $response | ConvertTo-Json
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}
