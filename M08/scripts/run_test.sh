#!/bin/bash
# API测试脚本启动器（Linux/Mac）

echo "============================================"
echo "API Test Script"
echo "============================================"
echo ""

# 检查Python
if ! command -v python3 &> /dev/null; then
    echo "[ERROR] Python3 not found"
    echo "Please install Python 3.7+"
    exit 1
fi

echo "[INFO] Python found: $(python3 --version)"
echo ""

# 检查依赖
echo "[INFO] Checking dependencies..."
if ! python3 -c "import requests" &> /dev/null; then
    echo "[INFO] Installing dependencies..."
    pip3 install -r requirements.txt
    if [ $? -ne 0 ]; then
        echo "[ERROR] Failed to install dependencies"
        exit 1
    fi
fi

echo "[INFO] Dependencies OK"
echo ""

# 运行测试
echo "[INFO] Running tests..."
echo "============================================"
echo ""
python3 test_all_apis.py

echo ""
echo "============================================"
echo "[INFO] Test completed"
echo "============================================"

