"""
Demo: FastAPI 向量检索服务（基于 data/kb/index.faiss + meta.json）

运行：
  python -m uvicorn scripts.kb_service_demo:app --reload

接口：
  - GET /health        健康检查
  - GET /search?q=...&topK=5  语义检索，返回 Top-K 引用

说明：
  - 索引与元数据可通过 scripts/kb_build_demo.py 生成。
  - 实际工程中建议缓存模型与索引，避免重复加载带来的时延。
"""

import json
import os
from typing import List, Dict, Any

import faiss
from fastapi import FastAPI, HTTPException
from sentence_transformers import SentenceTransformer


DATA_DIR = os.path.join("data", "kb")
INDEX_PATH = os.path.join(DATA_DIR, "index.faiss")
META_PATH = os.path.join(DATA_DIR, "meta.json")


def load_resources():
    if not (os.path.exists(INDEX_PATH) and os.path.exists(META_PATH)):
        raise FileNotFoundError("索引或元数据不存在，请先运行 scripts/kb_build_demo.py")
    model = SentenceTransformer("BAAI/bge-small-zh-v1.5")
    index = faiss.read_index(INDEX_PATH)
    with open(META_PATH, "r", encoding="utf-8") as f:
        meta = json.load(f)
    return model, index, meta


app = FastAPI(title="KB Search Demo", version="0.1")
model, index, meta = load_resources()


@app.get("/health")
def health() -> Dict[str, Any]:
    return {"code": 0, "message": "OK", "data": {"meta_count": len(meta)}}


@app.get("/search")
def search(q: str, topK: int = 5):
    if not q:
        raise HTTPException(status_code=400, detail="参数 q 不能为空")
    # 生成查询向量（归一化以使用内积近似余弦）
    qv = model.encode([q], normalize_embeddings=True)
    D, I = index.search(qv, topK)
    res = []
    # D: 相似度分数（内积）；I: 索引位置
    for score, idx in zip(D[0].tolist(), I[0].tolist()):
        item = dict(meta[idx])
        item["score"] = float(score)
        res.append(item)
    return {"code": 0, "message": "OK", "data": res}