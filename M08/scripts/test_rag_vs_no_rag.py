#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
有RAG vs 无RAG 对比测试脚本
- 通过后端 /api/kb/build 构建测试知识库（可选同步向量）
- 使用 /api/kb/query 分别以 keyword / vector / hybrid 模式查询同一问题
- 对比匹配条数、上下文长度、答案长度，并打印Top标题

运行：
    python scripts/test_rag_vs_no_rag.py
环境变量（可选）：
    BACKEND_BASE_URL（默认 http://localhost:8080）
    RAG_BASE_URL（默认 http://localhost:8801，用于健康检查）
    TEST_TIMEOUT（默认 10 秒）
"""

import os
import time
import json
from typing import Any, Dict, List

import requests

BE_BASE = os.getenv("BACKEND_BASE_URL", "http://localhost:8080")
RAG_BASE = os.getenv("RAG_BASE_URL", "http://localhost:8801")
TIMEOUT = int(os.getenv("TEST_TIMEOUT", "10"))


def log(msg: str) -> None:
    print(f"[RAG-TEST] {msg}")


def be_post(path: str, payload: Dict[str, Any]) -> requests.Response:
    url = f"{BE_BASE}{path}"
    return requests.post(url, json=payload, timeout=TIMEOUT)


def be_get(path: str) -> requests.Response:
    url = f"{BE_BASE}{path}"
    return requests.get(url, timeout=TIMEOUT)


def rag_health_ok() -> bool:
    try:
        r = requests.get(f"{RAG_BASE}/health", timeout=TIMEOUT)
        if r.status_code == 200:
            data = r.json()
            ok = (data.get("code") == 0)
            log(f"rag /health status={r.status_code}, ok={ok}, index_count={data.get('data',{}).get('index_count')}")
            return ok
    except Exception:
        pass
    log("rag /health 不可用或未启动（vector/hybrid 仍可回退，但建议启动）")
    return False


def build_kb_sample(to_vector: bool = True) -> None:
    payload = {
        "title": "心理陪伴示例：压力缓解技巧",
        "content": (
            "当你感觉压力过大时，可以尝试以下方法：\n"
            "1. 进行短暂的深呼吸练习；\n"
            "2. 适当进行轻度运动，例如散步；\n"
            "3. 和朋友交流，获得支持与陪伴；\n"
            "4. 记录你的感受，进行情绪表达；\n"
            "5. 将任务拆分为小步骤，逐步完成。\n"
            "这段文本用于测试RAG与非RAG的检索差异。关键短语：压力缓解方法。"
        ),
        "category": "教学示例",
        "keywords": "压力缓解,心理陪伴,测试",
        "source": "脚本测试",
        "chunkSize": 300,
        "chunkOverlap": 30,
        "toVector": bool(to_vector),
    }
    r = be_post("/api/kb/build", payload)
    if r.status_code != 200:
        raise AssertionError(f"/api/kb/build HTTP {r.status_code}: {r.text}")
    data = r.json()
    if data.get("code") != 0:
        raise AssertionError(f"/api/kb/build 失败: {data}")
    log(f"构建知识库完成，分片数={data.get('data')}，toVector={to_vector}")


def query_mode(question: str, mode: str, with_answer: bool = True, topK: int = 5, category: str | None = None) -> Dict[str, Any]:
    payload = {
        "question": question,
        "category": category,
        "topK": topK,
        "withAnswer": with_answer,
        "mode": mode,
        "alpha": 0.7,
        "beta": 0.3,
    }
    r = be_post("/api/kb/query", payload)
    if r.status_code != 200:
        raise AssertionError(f"/api/kb/query({mode}) HTTP {r.status_code}: {r.text}")
    data = r.json()
    if data.get("code") != 0:
        raise AssertionError(f"/api/kb/query({mode}) 失败: {data}")
    return data.get("data", {})


def summarize(label: str, vo: Dict[str, Any]) -> Dict[str, Any]:
    matches = vo.get("matches") or []
    ctx = vo.get("context") or ""
    ans = vo.get("answer") or ""
    titles = [m.get("title") for m in matches][:3]
    summary = {
        "mode": label,
        "hits": len(matches),
        "context_len": len(ctx),
        "answer_len": len(ans),
        "top_titles": titles,
    }
    log(f"{label} -> hits={summary['hits']}, ctx={summary['context_len']}, ans={summary['answer_len']}, titles={titles}")
    return summary


def main() -> None:
    log("开始：有RAG vs 无RAG 对比测试")

    # 1) 健康检查
    rag_ok = rag_health_ok()

    # 2) 构建样本，默认同时写向量库（便于 vector/hybrid 检索）
    try:
        build_kb_sample(to_vector=True)
    except Exception as e:
        log(f"构建知识库失败（继续对比测试）：{e}")

    question = "压力缓解方法"

    # 3) keyword（无RAG）
    t0 = time.time()
    vo_keyword = query_mode(question, mode="keyword", with_answer=True, topK=5, category="教学示例")
    tm_keyword = round((time.time() - t0) * 1000)

    # 4) vector（有RAG）
    t1 = time.time()
    vo_vector = query_mode(question, mode="vector", with_answer=True, topK=5, category="教学示例")
    tm_vector = round((time.time() - t1) * 1000)

    # 5) hybrid（有RAG）
    t2 = time.time()
    vo_hybrid = query_mode(question, mode="hybrid", with_answer=True, topK=5, category="教学示例")
    tm_hybrid = round((time.time() - t2) * 1000)

    # 6) 汇总与打印对比
    s_keyword = summarize("keyword(无RAG)", vo_keyword)
    s_vector = summarize("vector(有RAG)", vo_vector)
    s_hybrid = summarize("hybrid(有RAG)", vo_hybrid)

    print("\n=== 差异对比 ===")
    print(json.dumps({
        "keyword": {**s_keyword, "time_ms": tm_keyword},
        "vector": {**s_vector, "time_ms": tm_vector, "rag_health": rag_ok},
        "hybrid": {**s_hybrid, "time_ms": tm_hybrid, "rag_health": rag_ok},
    }, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()