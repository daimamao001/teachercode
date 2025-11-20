#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
完整用户流程测试脚本：注册 → 登录 → 获取信息 → 更新资料 → 修改密码 → 旧密码失败校验 → 新密码重新登录。

使用方法：
  python scripts/test_user_full.py

可选环境变量：
  BASE_URL: 后端基础地址，默认 http://localhost:8080

该脚本基于统一响应格式：{"code":0,"message":"操作成功","data":{...}}
"""

import os
import sys
import time
import json
import random
import string
from dataclasses import dataclass
import requests

BASE_URL = os.getenv("BASE_URL", "http://localhost:8080")


def _url(path: str) -> str:
    return BASE_URL.rstrip("/") + path


def _headers(token: str | None = None) -> dict:
    h = {"Content-Type": "application/json"}
    if token:
        h["Authorization"] = f"Bearer {token}"
    return h


def _post(path: str, payload: dict, token: str | None = None) -> requests.Response:
    return requests.post(_url(path), headers=_headers(token), json=payload, timeout=10)


def _get(path: str, token: str | None = None) -> requests.Response:
    return requests.get(_url(path), headers=_headers(token), timeout=10)


def assert_ok(resp: requests.Response, code: int = 0):
    try:
        body = resp.json()
    except Exception as e:
        raise AssertionError(f"响应非JSON或解析失败: status={resp.status_code}, error={e}, text={resp.text[:200]}")
    if body.get("code") != code:
        raise AssertionError(f"期望 code={code}，实际 code={body.get('code')}，message={body.get('message')}，status={resp.status_code}")
    return body.get("data")


def expect_fail_login(resp: requests.Response):
    """登录旧密码应失败：可能是 HTTP 401 或 JSON code!=0。"""
    try:
        body = resp.json()
    except Exception:
        body = None
    if resp.status_code == 401:
        return True
    if body and body.get("code") != 0:
        return True
    raise AssertionError(f"旧密码登录未失败: status={resp.status_code}, body={body}")


def rand_suffix() -> str:
    return str(int(time.time())) + "_" + "".join(random.choices(string.ascii_lowercase, k=4))


def main():
    print(f"BASE_URL = {BASE_URL}")

    username = f"full_user_{rand_suffix()}"
    old_pwd = "test123456"
    new_pwd = "newpass123456"
    email = f"{username}@example.com"

    # 1) 注册
    print("[1] 注册用户…")
    r1 = _post("/api/user/register", {
        "username": username,
        "password": old_pwd,
        "email": email
    })
    data1 = assert_ok(r1)
    print("注册成功")

    # 2) 登录（旧密码）
    print("[2] 登录(旧密码)…")
    r2 = _post("/api/user/login", {"username": username, "password": old_pwd})
    data2 = assert_ok(r2)
    token = data2.get("token")
    assert token, "登录返回未包含token"
    print("登录成功，token获取到")

    # 3) 获取信息
    print("[3] 获取当前用户信息…")
    r3 = _get("/api/user/info", token)
    me = assert_ok(r3)
    assert me.get("username") == username, "info.username 与注册用户名不一致"
    print(f"信息获取成功：{json.dumps(me, ensure_ascii=False)}")

    # 4) 更新资料（昵称/手机号/头像）
    print("[4] 更新资料…")
    new_profile = {
        "nickname": f"nick_{username[-6:]}",
        "phone": "13800001234",
        "avatar": "https://example.com/avatar.png"
    }
    r4 = _post("/api/user/profile", new_profile, token)
    updated = assert_ok(r4)
    assert updated.get("nickname") == new_profile["nickname"], "昵称未更新"
    print("资料更新成功")

    # 再次获取信息校验资料
    r3b = _get("/api/user/info", token)
    me2 = assert_ok(r3b)
    assert me2.get("nickname") == new_profile["nickname"], "info 未反映最新昵称"

    # 5) 修改密码
    print("[5] 修改密码…")
    r5 = _post("/api/user/password/change", {"oldPassword": old_pwd, "newPassword": new_pwd}, token)
    assert_ok(r5)
    print("密码修改成功")

    # 6) 旧密码应登录失败
    print("[6] 旧密码登录应失败…")
    r6 = _post("/api/user/login", {"username": username, "password": old_pwd})
    expect_fail_login(r6)
    print("旧密码登录失败校验通过")

    # 7) 新密码登录成功
    print("[7] 新密码登录…")
    r7 = _post("/api/user/login", {"username": username, "password": new_pwd})
    data7 = assert_ok(r7)
    token_new = data7.get("token")
    assert token_new, "新密码登录未返回token"
    print("新密码登录成功")

    # 8) 再次获取信息
    print("[8] 再次获取信息…")
    r8 = _get("/api/user/info", token_new)
    me3 = assert_ok(r8)
    assert me3.get("username") == username, "重新登录后的用户信息异常"
    print("信息获取成功，完整流程已通过 ✅")

    print("\n总结：完整用户流程测试通过。")
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except AssertionError as e:
        print(f"[断言失败] {e}")
        sys.exit(1)
    except requests.RequestException as e:
        print(f"[网络异常] {e}")
        sys.exit(1)
    except Exception as e:
        print(f"[未知异常] {e}")
        sys.exit(1)