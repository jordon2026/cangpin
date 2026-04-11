# 测试登录
$body = @{
    username = "admin"
    password = "admin123"
    uuid = "test-uuid"
    captchaCode = "1234"
}

try {
    $resp = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" -Method POST -Body ($body | ConvertTo-Json) -ContentType "application/json" -TimeoutSec 10
    Write-Host "Code: $($resp.code)"
    Write-Host "Msg: $($resp.msg)"
    if ($resp.data) {
        Write-Host "Token: $($resp.data.token)" -ForegroundColor Green
        Write-Host "Login SUCCESS!" -ForegroundColor Green
    }
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}
