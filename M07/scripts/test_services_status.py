#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
独立接口连通性测试脚本（不在命令行内联执行）
- 检查后端 SpringBoot 文档页 /doc.html
- 检查 RAG 服务 /health
- 进行 RAG 原始文本入库 /ingest（小样本）
- 进行语义检索 /search
- 进行混合检索 /hybrid-search

环境变量（可选）：
- RAG_BASE_URL（默认 http://localhost:8801）
- BACKEND_BASE_URL（默认 http://localhost:8080）
- TEST_TIMEOUT（默认 8 秒）

运行：
    python scripts/test_services_status.py
"""

import os
import sys
import json
import traceback
from typing import Any, Dict, List

import requests

RAG_BASE = os.getenv("RAG_BASE_URL", "http://localhost:8801")
BACKEND_BASE = os.getenv("BACKEND_BASE_URL", "http://localhost:8080")
TIMEOUT = int(os.getenv("TEST_TIMEOUT", "8"))


def log(msg: str) -> None:
    print(f"[TEST] {msg}")


def check_backend_doc() -> None:
    url = f"{BACKEND_BASE}/doc.html"
    r = requests.get(url, timeout=TIMEOUT)
    if r.status_code != 200:
        raise AssertionError(f"Backend /doc.html HTTP {r.status_code}")
    # 可选：做轻量内容匹配
    text = r.text or ""
    if not text:
        raise AssertionError("Backend /doc.html 返回空内容")
    log(f"Backend /doc.html OK (status={r.status_code}, length={len(text)})")


def check_rag_health() -> Dict[str, Any]:
    url = f"{RAG_BASE}/health"
    r = requests.get(url, timeout=TIMEOUT)
    if r.status_code != 200:
        raise AssertionError(f"RAG /health HTTP {r.status_code}")
    data = r.json()
    if data.get("code") != 0:
        raise AssertionError(f"RAG /health 响应异常: {data}")
    model = data.get("data", {}).get("model")
    idx = data.get("data", {}).get("index_count")
    log(f"RAG /health OK, model={model}, index_count={idx}")
    return data


def test_ingest_raw() -> Dict[str, Any]:
    url = f"{RAG_BASE}/ingest"
    payload = {
        "source": "raw",
        "title": "测试文档-心理陪伴",
        "category": "教学示例",
        "text": "这是一段用于测试的心理陪伴示例文本。帮助学生缓解压力与焦虑。",
        "keywords": "心理陪伴,测试",
        "chunkSize": 200,
        "chunkOverlap": 20,
    }
    r = requests.post(url, json=payload, timeout=TIMEOUT)
    if r.status_code != 200:
        raise AssertionError(f"RAG /ingest HTTP {r.status_code}: {r.text}")
    data = r.json()
    if data.get("code") != 0:
        raise AssertionError(f"RAG /ingest 响应异常: {data}")
    ing = int(data.get("data", {}).get("ingested", 0))
    if ing <= 0:
        raise AssertionError(f"RAG /ingest 未入库分片: ingested={ing}")
    log(f"RAG /ingest OK, ingested={ing}")
    return data


def test_search() -> List[Dict[str, Any]]:
    url = f"{RAG_BASE}/search"
    params = {"q": "缓解压力", "topK": 5}
    r = requests.get(url, params=params, timeout=TIMEOUT)
    if r.status_code != 200:
        raise AssertionError(f"RAG /search HTTP {r.status_code}")
    data = r.json()
    if data.get("code") != 0:
        raise AssertionError(f"RAG /search 响应异常: {data}")
    items = data.get("data", [])
    log(f"RAG /search OK, hits={len(items)}")
    return items


def test_hybrid_search() -> List[Dict[str, Any]]:
    url = f"{RAG_BASE}/hybrid-search"
    payload = {"q": "压力缓解方法", "topK": 5, "alpha": 0.7, "beta": 0.3}
    r = requests.post(url, json=payload, timeout=TIMEOUT)
    if r.status_code != 200:
        raise AssertionError(f"RAG /hybrid-search HTTP {r.status_code}")
    data = r.json()
    if data.get("code") != 0:
        raise AssertionError(f"RAG /hybrid-search 响应异常: {data}")
    items = data.get("data", [])
    log(f"RAG /hybrid-search OK, hits={len(items)}")
    return items


def main() -> None:
    fails: List[str] = []

    for name, fn in [
        ("backend_doc", check_backend_doc),
        ("rag_health", check_rag_health),
        ("rag_ingest", test_ingest_raw),
        ("rag_search", test_search),
        ("rag_hybrid", test_hybrid_search),
    ]:
        try:
            fn()
        except Exception as e:
            fails.append(f"{name}: {e}")
            traceback.print_exc()

    if fails:
        log("以下用例失败：")
        for f in fails:
            print(" -", f)
        sys.exit(1)
    else:
        log("所有测试通过。")


if __name__ == "__main__":
    main()