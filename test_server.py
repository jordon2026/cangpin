#!/usr/bin/env python3
import requests
import json

# 获取验证码
print("=== 测试验证码接口 ===")
r = requests.get("http://localhost:30080/api/v1/auth/captcha")
print(f"状态码: {r.status_code}")
print(f"响应: {r.text[:500]}")

data = r.json()
if data.get('success'):
    uuid = data['data']['uuid']
    code = data['data']['code']
    print(f"\nUUID: {uuid}")
    print(f"验证码: {code}")
    
    # 检查Redis中的验证码
    import subprocess
    redis_result = subprocess.run(['docker', 'exec', 'museum-redis', 'redis-cli', 'get', f'captcha:{uuid}'], 
                                  capture_output=True, text=True)
    print(f"Redis验证码: {redis_result.stdout.strip()}")
    
    # 登录
    print("\n=== 测试登录接口 ===")
    login_data = {
        "username": "admin",
        "password": "admin123",
        "uuid": uuid,
        "captchaCode": code
    }
    r2 = requests.post("http://localhost:30080/api/v1/auth/login", json=login_data)
    print(f"状态码: {r2.status_code}")
    print(f"响应: {r2.text[:1000]}")
else:
    print("获取验证码失败")
