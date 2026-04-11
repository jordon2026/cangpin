$env:JAVA_HOME = "C:\Program Files\Amazon Corretto\jdk17.0.18_9"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
Write-Host "JAVA_HOME: $env:JAVA_HOME"
Write-Host "Starting Museum Server..."
cd "d:\聚景科技\2026\博物馆\源码\museum-server"
& "C:\ProgramData\chocolatey\lib\maven\apache-maven-3.9.14\bin\mvn.cmd" spring-boot:run
