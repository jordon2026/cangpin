import subprocess
import os

# 设置环境变量
env = os.environ.copy()
env['JAVA_HOME'] = r'C:\Program Files\Amazon Corretto\jdk17.0.18_9'
env['PATH'] = env['JAVA_HOME'] + r'\bin;' + env.get('PATH', '')

# 切换到项目目录
project_dir = r'd:\jujing\2026\museum\src\museum-server'
maven_cmd = r'C:\ProgramData\chocolatey\lib\maven\apache-maven-3.9.14\bin\mvn.cmd'

print("=" * 50)
print("Starting Maven build...")
print("=" * 50)

# 执行 Maven
result = subprocess.run(
    [maven_cmd, 'clean', 'package', '-DskipTests'],
    cwd=project_dir,
    env=env,
    capture_output=True,
    text=True
)

print(result.stdout)
if result.stderr:
    print("STDERR:", result.stderr)
print("Exit code:", result.returncode)

# 检查 jar 文件
jar_path = os.path.join(project_dir, 'target', 'museum-server-1.0.0-SNAPSHOT.jar')
if os.path.exists(jar_path):
    size = os.path.getsize(jar_path) / (1024 * 1024)
    mtime = os.path.getmtime(jar_path)
    print("=" * 50)
    print(f"JAR file: {jar_path}")
    print(f"Size: {size:.2f} MB")
    print(f"Modified: {mtime}")
    print("=" * 50)
else:
    print("JAR file not found!")

input("Press Enter to exit...")
