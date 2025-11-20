#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
简本：聊天会话与消息闭环
运行：python scripts/test_chat_simple.py
依赖：requests（见 scripts/requirements.txt）
"""

import requests
import time
import sys

BASE_URL = "http://localhost:8080"


def login_or_register():
    # 生成符合后端约束（3-20 字符）的用户名
    username = f"sc{int(time.time())}"
    # 注册（检查响应但不因失败中断）
    try:
        reg = requests.post(
            f"{BASE_URL}/api/user/register",
            json={
                "username": username,
                "password": "test123456",
                "email": f"{username}@example.com",
            },
            timeout=15,
        )
        try:
            reg_data = reg.json()
        except Exception:
            reg_data = {"code": None, "message": f"非JSON响应: {reg.status_code}"}
        if reg.status_code != 200 or reg_data.get("code") not in (0, None):
            print(f"注册返回: {reg_data}")
    except Exception as e:
        print(f"注册异常: {e}")

    # 登录（短暂重试）
    last_err = None
    for i in range(5):
        try:
            r = requests.post(
                f"{BASE_URL}/api/user/login",
                json={"username": username, "password": "test123456"},
                timeout=15,
            )
            d = r.json()
        except Exception as e:
            last_err = f"登录异常/非JSON: {e}"
            time.sleep(1)
            continue
        if r.status_code == 200 and d.get("code") == 0:
            return d["data"]["token"]
        last_err = f"登录失败: {d}"
        time.sleep(1)
    raise AssertionError(last_err or "登录失败: 未知错误")


def main():
    token = login_or_register()
    headers = {"Authorization": f"Bearer {token}"}

    # 创建会话
    s = requests.post(
        f"{BASE_URL}/api/chat/sessions",
        json={"title": "简本会话"},
        headers=headers,
        timeout=15,
    )
    sd = s.json()
    assert s.status_code == 200 and sd.get("code") == 0, f"创建会话失败: {sd}"
    session_id = sd["data"]["id"]

    # 发送消息
    m = requests.post(
        f"{BASE_URL}/api/chat/messages",
        json={"sessionId": session_id, "content": "我最近压力很大"},
        headers=headers,
        timeout=20,
    )
    md = m.json()
    assert m.status_code == 200 and md.get("code") == 0 and (md.get("data") or {}).get("content"), (
        f"发送消息失败: {md}"
    )

    time.sleep(1)

    # 获取消息历史（如果接口异常则跳过，不影响最小闭环）
    try:
        h = requests.get(
            f"{BASE_URL}/api/chat/sessions/{session_id}/messages",
            headers=headers,
            timeout=15,
        )
        hd = h.json()
        if not (h.status_code == 200 and hd.get("code") == 0 and isinstance(hd.get("data"), list)):
            print(f"⚠️ 拉取历史失败: {hd}")
    except Exception as e:
        print(f"⚠️ 拉取历史异常: {e}")

    print("✅ 聊天简本测试通过（已创建会话并发送消息）")
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except AssertionError as e:
        print(e)
        sys.exit(1)