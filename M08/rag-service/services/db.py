from typing import List, Dict, Optional

import pymysql

try:
    from rag_service.config import DB_HOST, DB_PORT, DB_USER, DB_PASS, DB_NAME
except ImportError:
    from config import DB_HOST, DB_PORT, DB_USER, DB_PASS, DB_NAME


def get_conn():
    return pymysql.connect(
        host=DB_HOST, port=DB_PORT, user=DB_USER, password=DB_PASS,
        database=DB_NAME, charset='utf8mb4', autocommit=True
    )


def like_search(question: str, category: Optional[str], topK: int) -> List[Dict]:
    sql = (
        "SELECT id, title, content, category, keywords, source, created_at "
        "FROM knowledge "
        "WHERE is_deleted=0 AND content LIKE %s "
        + (" AND category=%s" if category else "") +
        " ORDER BY created_at DESC LIMIT %s"
    )
    params = [f"%{question}%"]
    if category:
        params.append(category)
    params.append(topK)
    try:
        with get_conn() as conn:
            with conn.cursor() as cur:
                cur.execute(sql, params)
                rows = cur.fetchall()
    except Exception:
        # 数据库不可用时，返回空集合以保证服务可用
        rows = []
    res = []
    for r in rows:
        # tuple order must match select
        res.append({
            'id': r[0], 'title': r[1], 'content': r[2], 'category': r[3],
            'keywords': r[4], 'source': r[5], 'created_at': r[6]
        })
    return res