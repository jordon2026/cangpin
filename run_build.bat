@echo off
setlocal
set "JAVA_HOME=C:\Program Files\Amazon Corretto\jdk17.0.18_9"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d "d:\聚景科技\2026\博物馆\源码\museum-server"
echo Starting build...
call mvnw.cmd clean package -DskipTests
echo Build finished
endlocal
