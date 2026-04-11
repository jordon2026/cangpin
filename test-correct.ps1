# 获取新验证码
$captcha = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/captcha" -Method GET
Write-Host "Captcha UUID: $($captcha.data.uuid)"

# 模拟前端获取验证码图片（base64）
$img = $captcha.data.img.Substring(22)  # 去掉 "data:image/png;base64," 前缀
Write-Host "Captcha image length: $($img.Length)"

# 检查 Redis 中是否存在这个验证码
Write-Host "Redis check - using tcp connection"

# 测试登录 - 用随机4位数字（实际应该由OCR识别或用户输入）
$randomCode = "5678"  # 随便输入一个错误的验证码测试

$loginBody = @{
    username = "admin"
    password = "admin123"
    uuid = $captcha.data.uuid
    captchaCode = $randomCode
}

Write-Host "Testing login with code: $randomCode"
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" `
    -Method POST `
    -ContentType "application/json; charset=utf-8" `
    -Body ($loginBody | ConvertTo-Json -Compress)

Write-Host "Response code: $($response.code)"
Write-Host "Response msg: $($response.msg)"
