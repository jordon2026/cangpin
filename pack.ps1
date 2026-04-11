# 设置环境变量
$env:JAVA_HOME = "C:\Program Files\Amazon Corretto\jdk17.0.18_9"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# 切换到项目目录
Set-Location "d:\聚景科技\2026\博物馆\源码\museum-server"

Write-Host "========================================"
Write-Host "开始打包 museum-server..."
Write-Host "========================================"

# 执行 Maven 打包
& "C:\ProgramData\chocolatey\lib\maven\apache-maven-3.9.14\bin\mvn.cmd" clean package -DskipTests

# 检查结果
$jarFile = Get-ChildItem "target\museum-server-1.0.0-SNAPSHOT.jar"
Write-Host ""
Write-Host "========================================"
Write-Host "打包完成！"
Write-Host "文件: $($jarFile.Name)"
Write-Host "大小: $([math]::Round($jarFile.Length/1MB, 2)) MB"
Write-Host "时间: $($jarFile.LastWriteTime)"
Write-Host "========================================"

Read-Host "按回车键退出"
