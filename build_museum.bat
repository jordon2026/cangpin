@echo off
chcp 65001 >nul
set JAVA_HOME=C:\Program Files\Amazon Corretto\jdk17.0.18_9
set PATH=%JAVA_HOME%\bin;%PATH%
cd /d d:\聚景科技\2026\博物馆\源码\museum-server
echo 开始打包...
call C:\ProgramData\chocolatey\lib\maven\apache-maven-3.9.14\bin\mvn.cmd clean package -DskipTests
echo 打包完成！
pause
