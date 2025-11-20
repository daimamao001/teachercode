import { http } from '@/utils/http'

export function health() {
  return http.get('/ai/test/health')
}

export function testChat(message) {
  return http.post('/ai/test/chat', { message })
}