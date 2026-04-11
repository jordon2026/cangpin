#!/usr/bin/env python3
import subprocess

def test_redis():
    # 测试从后端容器访问Redis
    print("1. 从后端容器测试Redis连接...")
    result = subprocess.run(
        ["docker", "exec", "museum-backend", "sh", "-c",
         "java -cp /app/app.jar org.springframework.boot.loader.launch.PropertiesLauncher 2>/dev/null || "
         "wget -O- http://redis:6379 2>/dev/null || "
         "nc -zv redis 6379 2>&1 || "
         "echo 'Redis connectivity test failed'"],
        capture_output=True, text=True, timeout=10
    )
    print(f"   stdout: {result.stdout}")
    print(f"   stderr: {result.stderr}")

    # 测试Redis get/set
    print("\n2. 直接测试Redis get/set...")
    subprocess.run(["docker", "exec", "museum-redis", "redis-cli", "SET", "test_key", "test_value"])
    result = subprocess.run(
        ["docker", "exec", "museum-redis", "redis-cli", "GET", "test_key"],
        capture_output=True, text=True
    )
    print(f"   test_key = {result.stdout.strip()}")

    # 测试使用keys模式
    print("\n3. 测试keys模式...")
    result = subprocess.run(
        ["docker", "exec", "museum-redis", "redis-cli", "KEYS", "captcha:*"],
        capture_output=True, text=True
    )
    print(f"   keys: {result.stdout.strip()}")

    # 测试TTL
    print("\n4. 检查Redis中的所有key...")
    result = subprocess.run(
        ["docker", "exec", "museum-redis", "redis-cli", "KEYS", "*"],
        capture_output=True, text=True
    )
    print(f"   all keys: {result.stdout.strip()}")

if __name__ == "__main__":
    test_redis()
