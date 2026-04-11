@echo off
set JAVA_HOME=C:\Program Files\Amazon Corretto\jdk17.0.18_9
set PATH=%JAVA_HOME%\bin;%PATH%
cd /d D:\聚景科技\2026\博物馆\源码\museum-server
rmdir /s /q target 2>nul
call C:\ProgramData\chocolatey\lib\maven\apache-maven-3.9.14\bin\mvn clean package -DskipTests
echo.
echo Build complete. Press any key to exit...
pause > nul
