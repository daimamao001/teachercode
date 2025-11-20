#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
简本：用户注册/登录/获取个人信息
运行：python scripts/test_user_simple.py
依赖：requests（见 scripts/requirements.txt）
"""

import requests
import time
import sys

BASE_URL = "http://localhost:8080"


def main():
    # 生成符合后端约束（3-20 字符）的用户名
    username = f"su{int(time.time())}"

    # 注册并检查响应（不因失败退出，但打印提示）
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

    # 登录（增加短暂重试，兼容注册后立即查询的延迟）
    last_err = None
    for i in range(5):
        try:
            r = requests.post(
                f"{BASE_URL}/api/user/login",
                json={"username": username, "password": "test123456"},
                timeout=15,
            )
            data = r.json()
        except Exception as e:
            last_err = f"登录异常/非JSON: {e} (status={getattr(r, 'status_code', 'n/a')})"
            time.sleep(1)
            continue

        if r.status_code == 200 and data.get("code") == 0:
            token = (data.get("data") or {}).get("token")
            if not token:
                print("未获取到token")
                return 4
            # 获取当前用户信息
            me = requests.get(
                f"{BASE_URL}/api/user/info",
                headers={"Authorization": f"Bearer {token}"},
                timeout=15,
            )
            try:
                me_data = me.json()
            except Exception:
                print(f"用户信息响应非JSON，状态码: {me.status_code}")
                return 5

            if me.status_code != 200 or me_data.get("code") != 0:
                print(f"获取用户信息失败: {me_data}")
                return 6

            if (me_data.get("data") or {}).get("username") != username:
                print(f"用户名不匹配: {me_data.get('data', {}).get('username')} != {username}")
                return 7

            print("✅ 用户简本测试通过")
            return 0

        last_err = f"登录失败: {data}"
        time.sleep(1)

    print(last_err or "登录失败: 未知错误")
    return 3


if __name__ == "__main__":
    sys.exit(main())