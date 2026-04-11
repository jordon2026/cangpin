# 获取新验证码
$captcha = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/captcha" -Method GET
Write-Host "Captcha UUID: $($captcha.data.uuid)"

# 测试登录
$loginBody = @{
    username = "admin"
    password = "admin123"
    uuid = $captcha.data.uuid
    captchaCode = "1234"
}

Write-Host "Login request:"
$loginBody | ConvertTo-Json | Write-Host

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" `
        -Method POST `
        -ContentType "application/json; charset=utf-8" `
        -Body ($loginBody | ConvertTo-Json -Compress)
    Write-Host "Response:" -ForegroundColor Green
    $response | ConvertTo-Json | Write-Host
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}
