#!/bin/bash
cd /tmp
# 获取验证码
wget -qO captcha.json http://127.0.0.1:8080/api/v1/auth/captcha
uuid=$(cat captcha.json | grep -o '"uuid":"[^"]*"' | sed 's/"uuid":"//;s/"//')
echo "UUID: $uuid"

# 获取Redis验证码
code=$(redis-cli -h museum-redis get captcha:$uuid)
echo "CODE: $code"

# 登录
wget -qO- --post-file=<(cat <<EOF
{"username":"admin","password":"admin123","uuid":"$uuid","captchaCode":"$code"}
EOF
) --header="Content-Type:application/json" http://127.0.0.1:8080/api/v1/auth/login
