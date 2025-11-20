import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  // State
  const userInfo = ref(null)
  const token = ref(localStorage.getItem('token') || '')

  // Getters
  const isLoggedIn = computed(() => !!token.value)
  const userId = computed(() => userInfo.value?.id)
  const username = computed(() => userInfo.value?.username)
  const nickname = computed(() => userInfo.value?.nickname)

  // Actions
  function setToken(newToken) {
    token.value = newToken
    if (newToken) {
      localStorage.setItem('token', newToken)
    } else {
      localStorage.removeItem('token')
    }
  }

  function setUserInfo(info) {
    userInfo.value = info
  }

  function login(loginData) {
    // 登录逻辑在API调用中处理
    setToken(loginData.token)
    setUserInfo(loginData)
  }

  function logout() {
    setToken('')
    setUserInfo(null)
    localStorage.clear()
  }

  function initUser() {
    // 从本地存储初始化用户信息
    const storedUser = localStorage.getItem('userInfo')
    if (storedUser) {
      try {
        userInfo.value = JSON.parse(storedUser)
      } catch (e) {
        console.error('解析用户信息失败:', e)
      }
    }
  }

  return {
    userInfo,
    token,
    isLoggedIn,
    userId,
    username,
    nickname,
    setToken,
    setUserInfo,
    login,
    logout,
    initUser
  }
})

