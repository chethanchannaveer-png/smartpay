# SmartPay Execution Script

Write-Host "--- SmartPay Launch System ---" -ForegroundColor Cyan

# 1. Create directories
if (!(Test-Path bin)) { New-Item -ItemType Directory -Path bin }
if (!(Test-Path lib)) { New-Item -ItemType Directory -Path lib }

# 2. Compile Java
Write-Host "Compiling Java source files..."
$sources = Get-ChildItem -Path src -Filter *.java -Recurse | ForEach-Object { $_.FullName }
$sources | Out-File -FilePath sources.txt -Encoding ascii

$classpath = "bin;lib/*"
javac -d bin -cp $classpath "@sources.txt"

if ($LASTEXITCODE -ne 0) {
    Write-Host "Compilation failed! Please ensure 'mysql-connector-java' and 'json' JARs are in the 'lib' folder." -ForegroundColor Red
    exit
}

# 3. Load Environment Variables from .env
if (Test-Path .env) {
    Get-Content .env | ForEach-Object {
        $line = $_.Trim()
        if ($line -and !$line.StartsWith("#")) {
            $parts = $line.Split("=", 2)
            if ($parts.Count -eq 2) {
                $key = $parts[0].Trim()
                $value = $parts[1].Trim()
                [System.Environment]::SetEnvironmentVariable($key, $value, [System.EnvironmentVariableTarget]::Process)
            }
        }
    }
}

# 4. Start Python AI if not running (simulated check)
Write-Host "Starting Python AI Server..."
Start-Process python -ArgumentList "ai/app.py" -WindowStyle Hidden

# 4. Run Dashboard
Write-Host "------------------------------------------------" -ForegroundColor Yellow
Write-Host "LAUNCHING SMARTPAY DASHBOARD" -ForegroundColor Yellow
Write-Host "Simulation Mode: Enabled (Scanner & DB Bypass)" -ForegroundColor Cyan
Write-Host "Note: Data will NOT be saved while MySQL is offline." -ForegroundColor Cyan
Write-Host "------------------------------------------------" -ForegroundColor Yellow

java -cp $classpath "-Dsimulate.scanner=true" com.smartpay.ui.LoginApp
