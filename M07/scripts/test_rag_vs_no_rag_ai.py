#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
基于AI对话接口的 有RAG vs 无RAG 对比测试
- 无RAG：直接调用 /api/ai/test/chat 获取通用回复
- 有RAG：先从 rag-service 获取混合检索上下文，再把片段拼到消息里并强调“严格依据片段回答”，调用 /api/ai/test/chat
- 对比两者的回复长度、是否包含参考标题，以及示例输出

运行：
    python scripts/test_rag_vs_no_rag_ai.py
环境变量（可选）：
    BACKEND_BASE_URL（默认 http://localhost:8080）
    RAG_BASE_URL（默认 http://localhost:8801）
    TEST_TIMEOUT（默认 10 秒）
"""

import os
import json
from typing import Any, Dict, List

import requests

BE_BASE = os.getenv("BACKEND_BASE_URL", "http://localhost:8080")
RAG_BASE = os.getenv("RAG_BASE_URL", "http://localhost:8801")
TIMEOUT = int(os.getenv("TEST_TIMEOUT", "10"))


def log(msg: str) -> None:
    print(f"[RAG-AI] {msg}")


def be_post(path: str, payload: Dict[str, Any]) -> requests.Response:
    url = f"{BE_BASE}{path}"
    return requests.post(url, json=payload, timeout=TIMEOUT)


def rag_hybrid(q: str, topK: int = 5, category: str | None = None) -> List[Dict[str, Any]]:
    url = f"{RAG_BASE}/hybrid-search"
    payload = {"q": q, "topK": topK}
    if category:
        payload["category"] = category
    r = requests.post(url, json=payload, timeout=TIMEOUT)
    if r.status_code != 200:
        raise AssertionError(f"rag /hybrid-search HTTP {r.status_code}: {r.text}")
    data = r.json()
    if data.get("code") != 0:
        raise AssertionError(f"rag /hybrid-search 响应异常: {data}")
    items = data.get("data", [])
    log(f"rag hybrid hits={len(items)}")
    return items


def build_context(items: List[Dict[str, Any]], max_chars: int = 1200) -> str:
    parts: List[str] = []
    cur = 0
    for it in items[:5]:
        title = it.get("title") or "(无标题)"
        content = (it.get("content") or "").strip().replace("\n", " ")
        seg = f"【{title}】\n{content}\n"
        if cur + len(seg) > max_chars:
            break
        parts.append(seg)
        cur += len(seg)
    return "".join(parts)


def ai_chat(message: str) -> Dict[str, Any]:
    r = be_post("/api/ai/test/chat", {"message": message})
    if r.status_code != 200:
        raise AssertionError(f"/api/ai/test/chat HTTP {r.status_code}: {r.text}")
    data = r.json()
    if data.get("code") != 0:
        raise AssertionError(f"/api/ai/test/chat 失败: {data}")
    return data.get("data", {})


def main() -> None:
    question = "压力缓解方法"

    # 无RAG：直接提问
    no_rag_msg = (
        "你好，我最近压力比较大，经常失眠。请给我一些建议，"
        "要求简洁、可操作。"
    )
    no_rag_resp = ai_chat(no_rag_msg)
    no_rag_text = no_rag_resp.get("content", "")
    log(f"无RAG回复长度={len(no_rag_text)}")

    # 有RAG：检索并拼接上下文，强调严格依据片段回答
    items = rag_hybrid(question, topK=5, category=None)
    ctx = build_context(items, max_chars=1200)
    with_rag_msg = (
        "请严格依据以下知识库片段回答我的问题，并在结尾列出参考片段标题列表：\n"
        f"问题：{question}\n"
        f"知识库片段：\n{ctx}\n"
    )
    rag_resp = ai_chat(with_rag_msg)
    rag_text = rag_resp.get("content", "")
    log(f"有RAG回复长度={len(rag_text)}，片段字数={len(ctx)}")

    print("\n=== 有RAG vs 无RAG 对比 ===")
    print(json.dumps({
        "no_rag": {
            "question": no_rag_msg,
            "answer_len": len(no_rag_text),
            "answer_preview": no_rag_text[:200],
        },
        "with_rag": {
            "question": with_rag_msg[:200] + "...",
            "context_len": len(ctx),
            "answer_len": len(rag_text),
            "answer_preview": rag_text[:200],
            "top_titles": [it.get("title") for it in items[:3]],
        }
    }, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()