@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ========================================
echo   测试修改密码接口
echo ========================================
echo.

:: 1. 先登录获取 token
echo [步骤1] 登录获取 token...
curl -s -X POST http://localhost:8080/api/v1/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin\",\"password\":\"admin123\",\"uuid\":\"test123\",\"captchaCode\":\"1\"}" > login_response.json

type login_response.json
echo.

:: 检查登录是否成功
findstr /C:"\"code\":200" login_response.json >nul
if errorlevel 1 (
    echo 登录失败，请检查用户名密码
    pause
    exit /b 1
)

:: 提取 token
for /f "tokens=*" %%i in ('powershell -Command "(Get-Content login_response.json | ConvertFrom-Json).data.token"') do set TOKEN=%%i
echo Token: %TOKEN%
echo.

:: 2. 测试修改密码（用旧密码 admin123）
echo [步骤2] 测试修改密码接口...
echo 调用: PUT /api/v1/auth/password
echo 参数: oldPassword=admin123, newPassword=admin123

curl -s -X PUT http://localhost:8080/api/v1/auth/password ^
  -H "Content-Type: application/json" ^
  -H "Authorization: %TOKEN%" ^
  -d "{\"oldPassword\":\"admin123\",\"newPassword\":\"admin123\"}"

echo.
echo.

:: 3. 用新密码登录验证
echo [步骤3] 使用新密码登录验证...
curl -s -X POST http://localhost:8080/api/v1/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin\",\"password\":\"admin123\",\"uuid\":\"test456\",\"captchaCode\":\"1\"}"

echo.
echo.

pause
