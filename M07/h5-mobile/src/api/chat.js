import { http } from '@/utils/http'

// 创建会话
export function createSession(data) {
  return http.post('/chat/sessions', data)
}

// 会话列表
export function listSessions() {
  return http.get('/chat/sessions')
}

// 会话详情
export function getSession(sessionId) {
  return http.get(`/chat/sessions/${sessionId}`)
}

// 消息历史
export function getMessages(sessionId) {
  return http.get(`/chat/sessions/${sessionId}/messages`)
}

// 发送消息（返回AI回复）
export function sendMessage(payload) {
  return http.post('/chat/messages', payload)
}