import { http } from '@/utils/http'

// AI 健康检查
export function health() {
  return http.get('/ai/test/health')
}

// AI 单轮对话测试
export function testChat(message) {
  return http.post('/ai/test/chat', { message })
}