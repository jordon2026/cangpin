import json
import urllib.request

# Get captcha
resp = urllib.request.urlopen('http://127.0.0.1:8080/api/v1/auth/captcha')
data = json.loads(resp.read())
uuid = data['data']['uuid']
print(f"UUID: {uuid}")

# Try login with wrong captcha to see error
login_data = {
    "username": "admin",
    "password": "admin123", 
    "uuid": uuid,
    "captchaCode": "wrong"
}
req = urllib.request.Request(
    'http://127.0.0.1:8080/api/v1/auth/login',
    data=json.dumps(login_data).encode(),
    headers={'Content-Type': 'application/json'}
)
try:
    resp = urllib.request.urlopen(req)
    print(resp.read().decode()[:500])
except Exception as e:
    print(f"Error: {e}")
