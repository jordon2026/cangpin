#!/bin/bash
# 测试登录

# 1. 获取验证码
echo "=== 获取验证码 ==="
captcha=$(wget -qO- http://127.0.0.1:8080/api/v1/auth/captcha)
echo "$captcha" | head -c 200
echo ""

# 提取UUID (使用grep和sed)
uuid=$(echo "$captcha" | grep -o '"uuid":"[^"]*"' | sed 's/"uuid":"//;s/"//')
echo "UUID: $uuid"

# 2. 从Redis获取验证码
echo ""
echo "=== Redis验证码 ==="
code=$(redis-cli -h museum-redis get captcha:$uuid)
echo "CODE: $code"

# 3. 登录
echo ""
echo "=== 登录 ==="
wget -qO- --post-data="{\"username\":\"admin\",\"password\":\"admin123\",\"uuid\":\"$uuid\",\"captchaCode\":\"$code\"}" \
     --header="Content-Type:application/json" \
     http://127.0.0.1:8080/api/v1/auth/login
echo ""
