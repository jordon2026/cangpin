# 获取验证码
$captcha = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/captcha" -Method GET
Write-Host "UUID: $($captcha.data.uuid)"

# 登录
$body = @{
    username = "admin"
    password = "admin123"
    uuid = $captcha.data.uuid
    captchaCode = "9999"  # 随便输入一个
}

$resp = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" -Method POST -Body ($body | ConvertTo-Json) -ContentType "application/json" -TimeoutSec 10
Write-Host "Code: $($resp.code)"
Write-Host "Msg: $($resp.msg)"
