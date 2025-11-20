import { http } from '@/utils/http'

export function login(data) {
  return http.post('/user/login', data)
}

export function getUserInfo() {
  return http.get('/user/info')
}