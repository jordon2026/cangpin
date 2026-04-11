import subprocess
import os
import sys

# 设置环境变量
env = dict(os.environ)
env['JAVA_HOME'] = r'C:\Program Files\Amazon Corretto\jdk17.0.18_9'
env['PATH'] = env['JAVA_HOME'] + r'\bin;' + env.get('PATH', '')

project_dir = r'd:\jujing2026\museum\src\museum-server'
maven = r'C:\ProgramData\chocolatey\lib\maven\apache-maven-3.9.14\bin\mvn.cmd'

print(f"Project dir: {project_dir}")
print(f"Maven: {maven}")
print(f"JAVA_HOME: {env['JAVA_HOME']}")
print()

# 执行
proc = subprocess.Popen(
    [maven, 'clean', 'package', '-DskipTests'],
    cwd=project_dir,
    env=env,
    stdout=subprocess.PIPE,
    stderr=subprocess.STDOUT,
    universal_newlines=True
)

for line in proc.stdout:
    print(line, end='')

proc.wait()
print(f"\nExit code: {proc.returncode}")
