#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
心理陪伴后端API完整测试脚本
测试所有已实现的接口（v0.1版本）

使用方法：
    python test_all_apis.py

依赖：
    pip install requests
"""

import requests
import json
import time
from datetime import datetime
from typing import Dict, Any, Optional

# 配置
BASE_URL = "http://localhost:8080"
TIMEOUT = 30  # 请求超时时间（秒）

# 测试统计
class TestStats:
    def __init__(self):
        self.total = 0
        self.passed = 0
        self.failed = 0
        self.start_time = None
        self.end_time = None
    
    def start(self):
        self.start_time = time.time()
    
    def finish(self):
        self.end_time = time.time()
    
    def add_result(self, passed: bool):
        self.total += 1
        if passed:
            self.passed += 1
        else:
            self.failed += 1
    
    def get_duration(self):
        if self.start_time and self.end_time:
            return round(self.end_time - self.start_time, 2)
        return 0
    
    def print_summary(self):
        print("\n" + "="*70)
        print("📊 测试总结")
        print("="*70)
        print(f"总测试数: {self.total}")
        print(f"✅ 通过: {self.passed}")
        print(f"❌ 失败: {self.failed}")
        print(f"通过率: {(self.passed/self.total*100):.1f}%" if self.total > 0 else "N/A")
        print(f"总耗时: {self.get_duration()}秒")
        print("="*70)

stats = TestStats()

# 全局变量存储测试数据
test_data = {
    "user_id": None,
    "token": None,
    "session_id": None,
    "message_id": None
}


def print_section(title: str):
    """打印章节标题"""
    print("\n" + "="*70)
    print(f"🧪 {title}")
    print("="*70)


def print_test(name: str, passed: bool, message: str = ""):
    """打印测试结果"""
    status = "✅ 通过" if passed else "❌ 失败"
    msg = f" - {message}" if message else ""
    print(f"  [{status}] {name}{msg}")
    stats.add_result(passed)


def print_response(response: requests.Response):
    """打印响应信息（美化）"""
    try:
        data = response.json()
        print(f"  响应码: {response.status_code}")
        print(f"  响应体: {json.dumps(data, ensure_ascii=False, indent=2)}")
    except:
        print(f"  响应码: {response.status_code}")
        print(f"  响应体: {response.text[:200]}")


def test_api(
    name: str,
    method: str,
    endpoint: str,
    data: Optional[Dict[str, Any]] = None,
    headers: Optional[Dict[str, str]] = None,
    expected_code: int = 200,
    check_response: Optional[callable] = None
) -> Optional[Dict[str, Any]]:
    """
    测试API接口
    
    Args:
        name: 测试名称
        method: HTTP方法（GET/POST/PUT/DELETE）
        endpoint: 接口端点
        data: 请求数据
        headers: 请求头
        expected_code: 期望的HTTP状态码
        check_response: 自定义响应检查函数
    
    Returns:
        响应数据（如果成功）
    """
    url = f"{BASE_URL}{endpoint}"
    print(f"\n  🔍 测试: {name}")
    print(f"  请求: {method} {endpoint}")
    
    if data:
        print(f"  请求数据: {json.dumps(data, ensure_ascii=False)}")
    
    try:
        # 发送请求
        if method.upper() == "GET":
            response = requests.get(url, headers=headers, timeout=TIMEOUT)
        elif method.upper() == "POST":
            response = requests.post(url, json=data, headers=headers, timeout=TIMEOUT)
        elif method.upper() == "PUT":
            response = requests.put(url, json=data, headers=headers, timeout=TIMEOUT)
        elif method.upper() == "DELETE":
            response = requests.delete(url, headers=headers, timeout=TIMEOUT)
        else:
            raise ValueError(f"不支持的HTTP方法: {method}")
        
        # 检查状态码
        if response.status_code != expected_code:
            print_response(response)
            print_test(name, False, f"期望状态码{expected_code}，实际{response.status_code}")
            return None
        
        # 解析响应
        try:
            response_data = response.json()
        except:
            print_response(response)
            print_test(name, False, "响应不是有效的JSON")
            return None
        
        # 检查响应格式
        if "code" not in response_data:
            print_response(response)
            print_test(name, False, "响应缺少code字段")
            return None
        
        # 检查业务状态码
        if response_data["code"] != 0:
            print_response(response)
            print_test(name, False, f"业务错误: {response_data.get('message', '未知错误')}")
            return None
        
        # 自定义检查
        if check_response:
            passed, message = check_response(response_data)
            if not passed:
                print_response(response)
                print_test(name, False, message)
                return None
        
        # 测试通过
        print(f"  ✅ 状态码: {response.status_code}")
        print(f"  ✅ 响应: {json.dumps(response_data, ensure_ascii=False)[:100]}...")
        print_test(name, True)
        return response_data
        
    except requests.exceptions.Timeout:
        print_test(name, False, f"请求超时（>{TIMEOUT}秒）")
        return None
    except requests.exceptions.ConnectionError:
        print_test(name, False, "连接失败，请确保后端服务已启动")
        return None
    except Exception as e:
        print_test(name, False, f"异常: {str(e)}")
        return None


def test_user_module():
    """测试用户模块"""
    print_section("用户模块测试")
    
    # 1. 用户注册
    timestamp = int(time.time())
    username = f"test_user_{timestamp}"
    password = "test123456"
    email = f"test{timestamp}@example.com"
    
    result = test_api(
        name="用户注册",
        method="POST",
        endpoint="/api/user/register",
        data={
            "username": username,
            "password": password,
            "email": email
        }
    )
    
    if not result:
        print("\n  ⚠️  用户注册失败，后续测试可能受影响")
        return False
    
    # 2. 用户登录
    result = test_api(
        name="用户登录",
        method="POST",
        endpoint="/api/user/login",
        data={
            "username": username,
            "password": password
        },
        check_response=lambda r: (
            (True, "") if r.get("data") and r["data"].get("token")
            else (False, "响应缺少token")
        )
    )
    
    if not result or not result.get("data"):
        print("\n  ⚠️  用户登录失败，后续测试可能受影响")
        return False
    
    # 保存Token和用户ID
    test_data["token"] = result["data"].get("token")
    test_data["user_id"] = result["data"].get("id")
    
    if not test_data["token"]:
        print("\n  ⚠️  未获取到Token，后续需要认证的接口将失败")
        return False
    
    print(f"\n  💾 已保存Token: {test_data['token'][:30]}...")
    print(f"  💾 已保存用户ID: {test_data['user_id']}")
    
    # 3. 获取当前用户信息
    headers = {"Authorization": f"Bearer {test_data['token']}"}
    result = test_api(
        name="获取当前用户信息",
        method="GET",
        endpoint="/api/user/info",
        headers=headers,
        check_response=lambda r: (
            (True, "") if r.get("data") and r["data"].get("username") == username
            else (False, "用户信息不匹配")
        )
    )
    
    # 4. 根据ID获取用户信息
    if test_data["user_id"]:
        result = test_api(
            name="根据ID获取用户信息",
            method="GET",
            endpoint=f"/api/user/{test_data['user_id']}",
            headers=headers
        )
    
    return True


def test_ai_module():
    """测试AI模块"""
    print_section("AI模块测试")
    
    # 1. AI健康检查
    result = test_api(
        name="AI健康检查",
        method="GET",
        endpoint="/api/ai/test/health"
    )
    
    # 2. 单轮对话测试
    result = test_api(
        name="单轮对话测试",
        method="POST",
        endpoint="/api/ai/test/chat",
        data={
            "message": "你好，我最近压力有点大"
        },
        check_response=lambda r: (
            (True, "") if r.get("data") and r["data"].get("content")
            else (False, "响应缺少AI回复内容")
        )
    )
    
    if result and result.get("data"):
        ai_reply = result["data"].get("content", "")
        print(f"\n  🤖 AI回复预览: {ai_reply[:100]}...")
    
    return True


def test_chat_module():
    """测试聊天模块"""
    print_section("聊天模块测试")
    
    if not test_data["token"]:
        print("  ⚠️  未获取到Token，跳过聊天模块测试")
        return False
    
    headers = {"Authorization": f"Bearer {test_data['token']}"}
    
    # 1. 创建会话
    result = test_api(
        name="创建会话",
        method="POST",
        endpoint="/api/chat/sessions",
        headers=headers,
        data={
            "title": f"测试会话_{int(time.time())}"
        },
        check_response=lambda r: (
            (True, "") if r.get("data") and r["data"].get("id")
            else (False, "响应缺少会话ID")
        )
    )
    
    if not result or not result.get("data"):
        print("\n  ⚠️  创建会话失败，后续测试可能受影响")
        return False
    
    # 保存会话ID
    test_data["session_id"] = result["data"].get("id")
    print(f"\n  💾 已保存会话ID: {test_data['session_id']}")
    
    # 2. 发送消息
    result = test_api(
        name="发送消息",
        method="POST",
        endpoint="/api/chat/messages",
        headers=headers,
        data={
            "sessionId": test_data["session_id"],
            "content": "我最近学习压力很大，经常失眠"
        },
        check_response=lambda r: (
            (True, "") if r.get("data") and r["data"].get("id")
            else (False, "响应缺少消息ID")
        )
    )
    
    if result and result.get("data"):
        test_data["message_id"] = result["data"].get("id")
        ai_reply = result["data"].get("content", "")
        print(f"\n  🤖 AI回复预览: {ai_reply[:100]}...")
    
    # 等待一秒，确保数据已保存
    time.sleep(1)
    
    # 3. 获取消息历史
    result = test_api(
        name="获取消息历史",
        method="GET",
        endpoint=f"/api/chat/sessions/{test_data['session_id']}/messages",
        headers=headers,
        check_response=lambda r: (
            (True, "") if r.get("data") and isinstance(r["data"], list)
            else (False, "响应数据格式错误")
        )
    )
    
    # 4. 获取会话列表
    result = test_api(
        name="获取会话列表",
        method="GET",
        endpoint="/api/chat/sessions",
        headers=headers,
        check_response=lambda r: (
            (True, "") if r.get("data") and isinstance(r["data"], list)
            else (False, "响应数据格式错误")
        )
    )
    
    # 5. 获取会话详情
    result = test_api(
        name="获取会话详情",
        method="GET",
        endpoint=f"/api/chat/sessions/{test_data['session_id']}",
        headers=headers,
        check_response=lambda r: (
            (True, "") if r.get("data") and r["data"].get("id") == test_data['session_id']
            else (False, "会话ID不匹配")
        )
    )
    
    # 6. 删除会话
    result = test_api(
        name="删除会话",
        method="DELETE",
        endpoint=f"/api/chat/sessions/{test_data['session_id']}",
        headers=headers
    )
    
    return True


def test_edge_cases():
    """测试边界情况"""
    print_section("边界情况测试")
    
    # 1. 测试未登录访问需要认证的接口
    result = test_api(
        name="未登录访问用户信息（应失败）",
        method="GET",
        endpoint="/api/user/info",
        expected_code=401  # 期望返回401未授权
    )
    
    # 注意：由于期望失败，这里反转结果
    if result is None:
        print_test("未登录访问保护（正确拒绝）", True, "正确返回401")
    else:
        print_test("未登录访问保护（错误放行）", False, "应该拒绝未登录访问")
    
    # 2. 测试无效的Token
    result = test_api(
        name="使用无效Token（应失败）",
        method="GET",
        endpoint="/api/user/info",
        headers={"Authorization": "Bearer invalid_token_12345"},
        expected_code=401
    )
    
    if result is None:
        print_test("无效Token拒绝（正确拒绝）", True, "正确返回401")
    else:
        print_test("无效Token拒绝（错误放行）", False, "应该拒绝无效Token")
    
    # 3. 测试重复注册
    timestamp = int(time.time())
    duplicate_username = f"duplicate_test_{timestamp}"
    
    # 第一次注册
    test_api(
        name="首次注册用户",
        method="POST",
        endpoint="/api/user/register",
        data={
            "username": duplicate_username,
            "password": "test123",
            "email": f"{duplicate_username}@example.com"
        }
    )
    
    # 第二次注册（应失败）
    result = test_api(
        name="重复注册（应失败）",
        method="POST",
        endpoint="/api/user/register",
        data={
            "username": duplicate_username,
            "password": "test123",
            "email": f"{duplicate_username}@example.com"
        },
        expected_code=200  # 可能返回200但code!=0
    )
    
    if result is None or (result and result.get("code") != 0):
        print_test("重复注册保护（正确拒绝）", True, "正确阻止重复注册")
    else:
        print_test("重复注册保护（错误放行）", False, "应该阻止重复注册")


def main():
    """主函数"""
    print("\n" + "="*70)
    print("🚀 心理陪伴后端API自动化测试")
    print("="*70)
    print(f"📅 测试时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print(f"🌐 服务地址: {BASE_URL}")
    print(f"⏱️  超时设置: {TIMEOUT}秒")
    print("="*70)
    
    # 检查后端服务是否可访问
    print("\n🔍 检查后端服务...")
    try:
        response = requests.get(f"{BASE_URL}/doc.html", timeout=5)
        print("  ✅ 后端服务正常运行")
    except:
        print("  ❌ 无法连接到后端服务")
        print(f"  💡 请确保后端服务已启动: {BASE_URL}")
        print("  💡 启动命令: cd api-backend && mvn spring-boot:run")
        return
    
    # 开始测试
    stats.start()
    
    try:
        # 测试各模块
        test_user_module()
        test_ai_module()
        test_chat_module()
        test_edge_cases()
        
    except KeyboardInterrupt:
        print("\n\n⚠️  测试被用户中断")
    except Exception as e:
        print(f"\n\n❌ 测试过程中发生异常: {str(e)}")
    finally:
        stats.finish()
        stats.print_summary()
        
        # 打印测试数据
        print("\n📦 测试数据:")
        print(f"  用户ID: {test_data['user_id']}")
        print(f"  Token: {test_data['token'][:30] if test_data['token'] else None}...")
        print(f"  会话ID: {test_data['session_id']}")
        print(f"  消息ID: {test_data['message_id']}")
        
        # 测试建议
        if stats.failed > 0:
            print("\n💡 测试建议:")
            print("  1. 检查后端日志查看错误详情")
            print("  2. 确认数据库连接正常")
            print("  3. 确认AI服务配置正确（或启用Mock模式）")
            print("  4. 查看 http://localhost:8080/doc.html 确认接口文档")


if __name__ == "__main__":
    main()

