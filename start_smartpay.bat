@echo off
TITLE SmartPay System Launcher

echo ******************************************
echo *    SmartPay Palm Payment System        *
echo ******************************************
echo.

:: Check for environment variables
if "%CASHFREE_APP_ID%"=="" (
    echo [WARNING] CASHFREE_APP_ID is not set. Using test defaults.
)

:: Check if AI server is running (optional but recommended)
echo [1/2] Launching AI Matching Service in new window...
start "SmartPay AI Service" cmd /c "cd ai && python palm_matcher.py"

echo [2/2] Launching Java Desktop Application...
java -cp "bin;lib/*" com.smartpay.ui.LoginApp

echo.
echo System session ended.
pause
