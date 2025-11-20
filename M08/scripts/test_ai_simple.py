#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
简本：AI健康检查与单轮对话
运行：python scripts/test_ai_simple.py
依赖：requests（见 scripts/requirements.txt）
"""

import requests
import sys

BASE_URL = "http://localhost:8080"


def main():
    # 健康检查
    h = requests.get(f"{BASE_URL}/api/ai/test/health", timeout=10)
    try:
        hd = h.json()
    except Exception:
        print(f"健康检查非JSON，状态码: {h.status_code}")
        return 1
    if h.status_code != 200 or hd.get("code") != 0:
        print(f"健康检查失败: {hd}")
        return 2

    # 单轮对话
    c = requests.post(
        f"{BASE_URL}/api/ai/test/chat",
        json={"message": "你好"},
        timeout=20,
    )
    try:
        cd = c.json()
    except Exception:
        print(f"单轮对话非JSON，状态码: {c.status_code}")
        return 3
    if c.status_code != 200 or cd.get("code") != 0 or not (cd.get("data") or {}).get("content"):
        print(f"单轮对话失败: {cd}")
        return 4

    print("✅ AI简本测试通过")
    return 0


if __name__ == "__main__":
    sys.exit(main())