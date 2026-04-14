@echo off
title Build Museum Server
cd /d "%~dp0"
set JAVA_HOME=C:\Program Files\Amazon Corretto\jdk17.0.18_9
set PATH=%JAVA_HOME%\bin;%PATH%
echo Building Museum Server...
"C:\ProgramData\chocolatey\lib\maven\apache-maven-3.9.14\bin\mvn.cmd" clean package -DskipTests
pause