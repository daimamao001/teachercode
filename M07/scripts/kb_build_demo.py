"""
Demo: 构建向量知识库索引（FAISS + bge-small-zh）

运行：
  python scripts/kb_build_demo.py

效果：
  - 在 data/kb/index.faiss 写入索引
  - 在 data/kb/meta.json 写入对应的 chunk 元数据

说明：
  - 教学示例使用内置示例文档；实际工程可改为从数据库/后端API拉取文档，先分段再嵌入。
"""

import json
import os
from typing import List, Dict

import faiss
from sentence_transformers import SentenceTransformer


def ensure_dir(path: str):
    os.makedirs(path, exist_ok=True)


def build_demo_docs() -> List[Dict]:
    # 示例数据：真实项目请替换为数据库/接口读取
    return [
        {
            "id": 1,
            "title": "压力管理基础",
            "chunks": [
                "压力是对挑战或威胁的适应性反应，短期有助于提升表现，但长期过度会损害健康。",
                "常见缓解方法包括腹式呼吸、渐进式肌肉放松、规律运动与睡眠卫生等。",
            ],
        },
        {
            "id": 2,
            "title": "CBT 认知行为疗法",
            "chunks": [
                "CBT 强调识别与修正非理性思维，关注触发-自动想法-情绪-行为的链条。",
                "练习包括记录思维、证据搜集、替代性思维与行为实验，逐步改善负性模式。",
            ],
        },
        {
            "id": 3,
            "title": "考试焦虑的应对",
            "chunks": [
                "考试焦虑常见表现为紧张、心跳加快、回避复习等，可通过暴露练习与自我接纳减轻。",
                "准备计划建议采用小步快跑与番茄钟，结合正念训练提高注意与情绪调节。",
            ],
        },
    ]


def flatten_docs(docs: List[Dict]):
    meta, texts = [], []
    for d in docs:
        for c in d["chunks"]:
            meta.append({"docId": d["id"], "title": d["title"], "chunk": c})
            texts.append(c)
    return meta, texts


def build_index(texts: List[str], out_index_path: str):
    # 选型：BAAI/bge-small-zh-v1.5（中文效果好，维度512）
    model = SentenceTransformer("BAAI/bge-small-zh-v1.5")
    emb = model.encode(texts, normalize_embeddings=True)

    # 使用内积（IP）近似余弦相似度（前提：normalize_embeddings=True）
    index = faiss.IndexFlatIP(emb.shape[1])
    index.add(emb)
    faiss.write_index(index, out_index_path)


def main():
    out_dir = os.path.join("data", "kb")
    ensure_dir(out_dir)
    out_index = os.path.join(out_dir, "index.faiss")
    out_meta = os.path.join(out_dir, "meta.json")

    docs = build_demo_docs()
    meta, texts = flatten_docs(docs)
    build_index(texts, out_index)

    with open(out_meta, "w", encoding="utf-8") as f:
        json.dump(meta, f, ensure_ascii=False, indent=2)

    print("索引构建完成：")
    print(f"- index: {out_index}")
    print(f"- meta : {out_meta}")


if __name__ == "__main__":
    main()