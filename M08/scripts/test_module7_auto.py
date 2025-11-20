"""
Module 7 自动化联调测试（简本）

覆盖范围：
- RAG 微服务：/health, /ingest(raw), /search, /hybrid-search
- 后端API：/api/kb/build（可选向量入库）、/api/kb/query（keyword/vector/hybrid）

运行：
  python scripts/test_module7_auto.py

环境变量（可选）：
  RAG_BASE=http://localhost:8801
  BE_BASE=http://localhost:8080
"""

import os
import time
import json
from typing import Dict, Any

import requests


RAG_BASE = os.getenv("RAG_BASE", "http://localhost:8801")
BE_BASE = os.getenv("BE_BASE", "http://localhost:8080")


def _print(title: str, ok: bool, detail: str = ""):
    status = "PASS" if ok else "FAIL"
    print(f"[{status}] {title} {('- ' + detail) if detail else ''}")


def _get(url: str, params: Dict[str, Any] | None = None, timeout: float = 5.0):
    try:
        r = requests.get(url, params=params or {}, timeout=timeout)
        return True, r.status_code, r.json()
    except Exception as e:
        return False, 0, {"error": str(e)}


def _post(url: str, payload: Dict[str, Any] | None = None, timeout: float = 8.0):
    try:
        r = requests.post(url, json=payload or {}, timeout=timeout)
        return True, r.status_code, r.json()
    except Exception as e:
        return False, 0, {"error": str(e)}


def test_rag_health():
    ok, code, body = _get(f"{RAG_BASE}/health")
    ok = ok and code == 200 and body.get("code") == 0
    _print("RAG /health", ok, json.dumps(body, ensure_ascii=False)[:200])
    return ok


def test_rag_ingest_raw():
    payload = {
        "source": "raw",
        "title": "测试文章-压力缓解",
        "category": "心理教育",
        "text": "压力来源多样，建议进行有氧运动、规律睡眠与正念冥想。对于考试焦虑，可采用呼吸训练与分阶段复习计划。",
        "keywords": "压力, 焦虑, 睡眠, 冥想",
        "chunkSize": 100,
        "chunkOverlap": 20,
    }
    ok, code, body = _post(f"{RAG_BASE}/ingest", payload)
    ok = ok and code == 200 and body.get("code") == 0 and (body.get("data", {}).get("ingested", 0) > 0)
    _print("RAG /ingest(raw)", ok, json.dumps(body, ensure_ascii=False)[:200])
    return ok


def test_rag_search():
    params = {"q": "压力缓解", "topK": 3, "category": "心理教育"}
    ok, code, body = _get(f"{RAG_BASE}/search", params)
    ok = ok and code == 200 and body.get("code") == 0 and isinstance(body.get("data"), list)
    _print("RAG /search", ok, f"items={len(body.get('data', []))}")
    return ok


def test_rag_hybrid():
    payload = {"q": "压力缓解", "topK": 3, "category": "心理教育", "alpha": 0.7, "beta": 0.3}
    ok, code, body = _post(f"{RAG_BASE}/hybrid-search", payload)
    ok = ok and code == 200 and body.get("code") == 0 and isinstance(body.get("data"), list)
    _print("RAG /hybrid-search", ok, f"items={len(body.get('data', []))}")
    return ok


def test_backend_build(to_vector: bool = True):
    payload = {
        "title": "考试焦虑的应对策略",
        "content": "考试焦虑可通过呼吸放松、分块学习、模拟练习与正念觉察进行缓解。必要时寻求辅导员或心理咨询帮助。",
        "category": "心理教育",
        "keywords": "考试, 焦虑, 放松训练",
        "chunkSize": 120,
        "chunkOverlap": 20,
        "toVector": to_vector,
    }
    ok, code, body = _post(f"{BE_BASE}/api/kb/build", payload)
    # ApiResponse 格式：{"code":200,"message":"成功","data": count}
    ok = ok and code == 200 and (body.get("code") in (200, 0)) and (body.get("data", 0) >= 1)
    _print("BE /api/kb/build", ok, json.dumps(body, ensure_ascii=False)[:200])
    return ok


def test_backend_query(mode: str):
    payload = {
        "question": "如何缓解压力？",
        "category": "心理教育",
        "topK": 3,
        "withAnswer": False,
        "mode": mode,
        "alpha": 0.7,
        "beta": 0.3,
    }
    ok, code, body = _post(f"{BE_BASE}/api/kb/query", payload)
    ok = ok and code == 200 and (body.get("code") in (200, 0))
    data = body.get("data", {})
    matches = data.get("matches", []) if isinstance(data, dict) else []
    _print(f"BE /api/kb/query ({mode})", ok, f"matches={len(matches)}")
    return ok


def main():
    print("==== Module 7 简本自动化测试开始 ====")
    print(f"RAG_BASE={RAG_BASE}")
    print(f"BE_BASE={BE_BASE}")
    results = []

    # RAG 微服务
    results.append(("RAG /health", test_rag_health()))
    # 若健康检查失败，尽量继续后续，但可能失败
    results.append(("RAG /ingest(raw)", test_rag_ingest_raw()))
    results.append(("RAG /search", test_rag_search()))
    results.append(("RAG /hybrid-search", test_rag_hybrid()))

    # 后端API
    results.append(("BE /api/kb/build", test_backend_build(to_vector=True)))
    results.append(("BE /api/kb/query keyword", test_backend_query("keyword")))
    results.append(("BE /api/kb/query vector", test_backend_query("vector")))
    results.append(("BE /api/kb/query hybrid", test_backend_query("hybrid")))

    # 汇总
    total = len(results)
    passed = sum(1 for _, ok in results if ok)
    print("==== 测试汇总 ====")
    for name, ok in results:
        print(f"- {name}: {'PASS' if ok else 'FAIL'}")
    print(f"总计 {passed}/{total} 通过")


if __name__ == "__main__":
    main()