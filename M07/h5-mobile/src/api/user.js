import { http } from '@/utils/http'

/**
 * 用户注册
 */
export function register(data) {
  return http.post('/user/register', data)
}

/**
 * 用户登录
 */
export function login(data) {
  return http.post('/user/login', data)
}

/**
 * 获取用户信息
 */
export function getUserInfo() {
  return http.get('/user/info')
}

/**
 * 更新用户信息
 */
export function updateUserInfo(data) {
  return http.put('/user/info', data)
}

