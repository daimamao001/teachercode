"""
简易测试：构建索引 → 启动检索服务（手动）→ 请求搜索接口

运行：
  1) 安装依赖：pip install -r scripts/requirements.txt
  2) 构建索引：python scripts/kb_build_demo.py
  3) 启动服务：python -m uvicorn scripts.kb_service_demo:app --reload
  4) 运行本测试：python scripts/test_kb_simple.py
"""

import time
import requests


BASE = "http://localhost:8000"


def must_ok(resp, msg=""):
    try:
        data = resp.json()
    except Exception as e:
        raise AssertionError(f"{msg} 非JSON响应: {resp.status_code}, {resp.text}")
    assert data.get("code") == 0, f"{msg} 失败: {data}"
    return data


def test_health():
    r = requests.get(f"{BASE}/health")
    data = must_ok(r, "健康检查")
    assert data["data"]["meta_count"] > 0, "元数据为空，请先构建索引"


def test_search():
    r = requests.get(f"{BASE}/search", params={"q": "压力缓解方法", "topK": 3})
    data = must_ok(r, "检索接口")
    assert len(data["data"]) > 0, "检索结果为空"
    print("Top-1:", data["data"][0])


def main():
    # 给服务启动一点缓冲时间（如在自动化管线中调用）
    time.sleep(0.2)
    test_health()
    test_search()
    print("KB 检索测试通过")


if __name__ == "__main__":
    main()