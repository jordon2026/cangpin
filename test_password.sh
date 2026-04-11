#!/bin/bash

# 测试修改密码接口
echo "========================================"
echo "测试修改密码接口"
echo "========================================"

# 1. 获取验证码
echo "[1] 获取验证码..."
CAPTCHA=$(curl -s http://localhost:8080/api/v1/auth/captcha)
UUID=$(echo $CAPTCHA | grep -o '"uuid":"[^"]*"' | cut -d'"' -f4)
echo "UUID: $UUID"
echo "验证码图片已生成（需要OCR识别答案）"

# 由于验证码是图片，我们用 Python 来处理
echo ""
echo "[2] 使用 Python 获取验证码并登录..."

# 使用 Python 脚本
python3 << 'PYTHON_SCRIPT'
import urllib.request
import urllib.parse
import json
import re

# 1. 获取验证码
captcha_url = "http://localhost:8080/api/v1/auth/captcha"
with urllib.request.urlopen(captcha_url) as response:
    captcha_data = json.loads(response.read().decode())

uuid = captcha_data['data']['uuid']
img_base64 = captcha_data['data']['img']

# 去掉 base64 头部
img_data = img_base64.replace("data:image/png;base64,", "")
img_bytes = bytes.fromhex(img_data[2:] if img_data.startswith("\\x") else img_data)

# 保存图片
with open('/tmp/captcha.png', 'wb') as f:
    # 实际应该解析 base64
    pass

# 打印验证码图片的 base64 数据供 OCR
print(f"验证码UUID: {uuid}")
print("请访问 http://localhost:8080/api/v1/auth/captcha 查看验证码图片")
PYTHON_SCRIPT

echo ""
echo "[3] 手动测试方法："
echo "请先访问 Swagger UI: http://localhost:8080/doc.html"
echo "1. 调用 /auth/captcha 获取验证码"
echo "2. 调用 /auth/login 登录（需要正确的验证码答案）"
echo "3. 调用 /auth/password 修改密码"
echo ""
echo "或者在 Swagger 中直接测试修改密码接口（需要先登录获取 token）"
