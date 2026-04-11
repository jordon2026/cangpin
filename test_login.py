#!/usr/bin/env python3
import urllib.request
import urllib.error
import json
import subprocess

def get_redis_value(key):
    result = subprocess.run(
        ["docker", "exec", "museum-redis", "redis-cli", "GET", key],
        capture_output=True, text=True
    )
    return result.stdout.strip()

def test_full_login():
    base_url = "http://127.0.0.1:8080/api/v1"

    print("=== 完整登录测试 ===\n")

    # 1. 获取验证码
    print("1. 获取验证码...")
    req = urllib.request.urlopen(f"{base_url}/auth/captcha")
    resp_data = json.loads(req.read().decode())
    uuid = resp_data["data"]["uuid"]
    print(f"   UUID: {uuid}")

    # 2. 获取验证码答案
    captcha_key = f"captcha:{uuid}"
    captcha_value = get_redis_value(captcha_key)
    print(f"   验证码答案: {captcha_value}")

    # 3. 检查Redis连接
    print("\n2. 检查Redis...")
    redis_keys = subprocess.run(
        ["docker", "exec", "museum-redis", "redis-cli", "KEYS", captcha_key],
        capture_output=True, text=True
    )
    print(f"   Redis中的key: {redis_keys.stdout.strip()}")

    # 4. 发送登录请求
    print("\n3. 发送登录请求...")
    login_data = {
        "username": "admin",
        "password": "admin123",
        "captchaKey": uuid,
        "captchaCode": captcha_value
    }

    print(f"   登录数据: {json.dumps(login_data, ensure_ascii=False)}")

    req = urllib.request.Request(
        f"{base_url}/auth/login",
        data=json.dumps(login_data).encode(),
        headers={"Content-Type": "application/json"}
    )

    try:
        resp = urllib.request.urlopen(req)
        result = json.loads(resp.read().decode())
        print(f"   结果: {json.dumps(result, ensure_ascii=False, indent=4)}")

        if result.get("code") == 200:
            print("\n✅ 登录成功!")
            token = result.get("data", {}).get("token")
            print(f"   Token: {token}")

            # 5. 测试获取用户信息
            print("\n4. 测试获取用户信息...")
            req2 = urllib.request.Request(
                f"{base_url}/auth/userinfo",
                headers={"Authorization": token}
            )
            resp2 = urllib.request.urlopen(req2)
            userinfo = json.loads(resp2.read().decode())
            print(f"   用户信息: {json.dumps(userinfo, ensure_ascii=False, indent=4)}")

            # 6. 测试获取菜单
            print("\n5. 测试获取菜单...")
            req3 = urllib.request.Request(
                f"{base_url}/auth/menus",
                headers={"Authorization": token}
            )
            resp3 = urllib.request.urlopen(req3)
            menus = json.loads(resp3.read().decode())
            print(f"   菜单: {json.dumps(menus, ensure_ascii=False, indent=4)[:500]}...")

        else:
            print(f"\n❌ 登录失败: {result.get('msg')}")

    except urllib.error.HTTPError as e:
        result = json.loads(e.read().decode())
        print(f"   HTTP {e.code}: {json.dumps(result, ensure_ascii=False, indent=4)}")
        print(f"\n❌ 登录失败: {result.get('msg')}")

    except Exception as e:
        print(f"错误: {e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    test_full_login()
