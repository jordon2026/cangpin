import requests, subprocess, json

# 获取验证码
r = requests.get("http://127.0.0.1:8080/api/v1/auth/captcha")
d = r.json()
u = d['data']['uuid']
print(f"UUID: {u}")

# 从Redis获取验证码
c = subprocess.run(["docker","exec","museum-redis","redis-cli","get",f"captcha:{u}"],
    capture_output=True, text=True).stdout.decode().strip()
print(f"REDIS CODE: '{c}'")

# 登录
r2 = requests.post("http://127.0.0.1:8080/api/v1/auth/login", json={
    "username": "admin",
    "password": "admin123",
    "uuid": u,
    "captchaCode": c
})
print(f"LOGIN STATUS: {r2.status_code}")
print(f"LOGIN RESP: {r2.text[:800]}")
