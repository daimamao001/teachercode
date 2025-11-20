@echo off
chcp 65001 >nul
echo ============================================
echo API Test Script
echo ============================================
echo.

REM Check Python
python --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Python not found
    echo Please install Python 3.7+
    echo Download: https://www.python.org/downloads/
    pause
    exit /b 1
)

echo [INFO] Python found
echo.

REM Check dependencies
echo [INFO] Checking dependencies...
pip show requests >nul 2>&1
if errorlevel 1 (
    echo [INFO] Installing dependencies...
    pip install -r requirements.txt
    if errorlevel 1 (
        echo [ERROR] Failed to install dependencies
        pause
        exit /b 1
    )
)

echo [INFO] Dependencies OK
echo.

REM Run tests
echo [INFO] Running tests...
echo ============================================
echo.
python test_all_apis.py

echo.
echo ============================================
echo [INFO] Test completed
echo ============================================
pause

