@REM Maven Wrapper for Windows
@REM Adapted from https://github.com/apache/maven-wrapper

@echo off
setlocal enabledelayedexpansion

set MAVEN_PROJECTBASEDIR=%~dp0
set MAVEN_WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.properties

set MAVEN_DIST_URL=
if exist "%MAVEN_WRAPPER_PROPERTIES%" (
    for /f "tokens=1,* delims==" %%a in ('type "%MAVEN_WRAPPER_PROPERTIES%" ^| findstr "distributionUrl"') do (
        set MAVEN_DIST_URL=%%b
    )
)

if "%MAVEN_DIST_URL%"=="" (
    set MAVEN_DIST_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.zip
)

set MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.6

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    echo Downloading Maven...
    mkdir "%MAVEN_HOME%" 2>nul
    set TMP_FILE=%MAVEN_HOME%\maven.zip
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; $ProgressPreference='SilentlyContinue'; Invoke-WebRequest -Uri '%MAVEN_DIST_URL%' -OutFile '%TMP_FILE%' -UseBasicParsing"
    powershell -Command "Expand-Archive -Path '%TMP_FILE%' -DestinationPath '%MAVEN_HOME%' -Force"
    del "%TMP_FILE%"
)

if "%JAVA_HOME%"=="" (
    set "JAVA_HOME=C:\Program Files\Amazon Corretto\jdk17.0.18_9"
)

"%MAVEN_HOME%\bin\mvn.cmd" %*

endlocal
