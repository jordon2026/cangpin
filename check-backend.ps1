Write-Host "=== Backend Check ==="
Write-Host "8080:" (Test-NetConnection localhost -Port 8080).TcpTestSucceeded
Write-Host "6379:" (Test-NetConnection localhost -Port 6379).TcpTestSucceeded
Write-Host "3306:" (Test-NetConnection localhost -Port 3306).TcpTestSucceeded
Write-Host "3000:" (Test-NetConnection localhost -Port 3000).TcpTestSucceeded
