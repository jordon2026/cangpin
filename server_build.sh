#!/bin/bash
# 在服务器上增量编译后端（需要服务器有 JDK）

echo "========================================"
echo "服务器增量编译"
echo "========================================"

# 1. 检查 Java
java -version

# 2. 检查 Maven
mvn -version

echo ""
echo "如果上面显示 Java 和 Maven 版本，说明可以编译"
echo "然后执行："
echo "cd /root/museum-server"
echo "mvn clean package -DskipTests -o"  # -o 表示离线模式，不下载依赖
