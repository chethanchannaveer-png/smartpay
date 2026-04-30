# SmartPay Environment Variable Setup Script (Windows PowerShell)
# Run this script as Administrator to set system-wide production credentials

$AppID = Read-Host "Enter Cashfree App ID"
$SecretKey = Read-Host "Enter Cashfree Secret Key"
$DBUrl = Read-Host "Enter DB URL (Default: jdbc:mysql://localhost:3306/smartpay)"
$DBUser = Read-Host "Enter DB User (Default: root)"
$DBPass = Read-Host "Enter DB Password"
$AiUrl = Read-Host "Enter AI Server URL (Default: http://localhost:5000/match)"
$AiKey = Read-Host "Enter AI API Key"

if ($AppID -and $SecretKey) {
    [Environment]::SetEnvironmentVariable("CASHFREE_APP_ID", $AppID, "User")
    [Environment]::SetEnvironmentVariable("CASHFREE_SECRET_KEY", $SecretKey, "User")
    if ($DBUrl) { [Environment]::SetEnvironmentVariable("DB_URL", $DBUrl, "User") }
    if ($DBUser) { [Environment]::SetEnvironmentVariable("DB_USER", $DBUser, "User") }
    if ($DBPass) { [Environment]::SetEnvironmentVariable("DB_PASSWORD", $DBPass, "User") }
    if ($AiUrl) { [Environment]::SetEnvironmentVariable("AI_SERVER_URL", $AiUrl, "User") }
    if ($AiKey) { [Environment]::SetEnvironmentVariable("AI_API_KEY", $AiKey, "User") }
    
    Write-Host "`n[SUCCESS] Environment variables set successfully for the current user." -ForegroundColor Green
    Write-Host "[ACTION] Please restart your IDE or Terminal for the changes to take effect." -ForegroundColor Yellow
} else {
    Write-Host "[ERROR] Both App ID and Secret Key are required." -ForegroundColor Red
}
