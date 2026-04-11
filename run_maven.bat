@echo off
set "JAVA_HOME=C:\Program Files\Amazon Corretto\jdk17.0.18_9"
set "MAVEN_HOME=C:\ProgramData\chocolatey\lib\maven\apache-maven-3.9.14"
cd /d "D:\聚景科技\2026\博物馆\源码\museum-server"
rmdir /s /q target 2>nul
call "%MAVEN_HOME%\bin\mvn.cmd" clean package -DskipTests
