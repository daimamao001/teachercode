import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAdminStore = defineStore('admin', () => {
  // State
  const adminInfo = ref(null)
  const token = ref(localStorage.getItem('admin_token') || '')

  // Getters
  const isLoggedIn = computed(() => !!token.value)
  const adminId = computed(() => adminInfo.value?.id)
  const username = computed(() => adminInfo.value?.username)
  const nickname = computed(() => adminInfo.value?.nickname)

  // Actions
  function setToken(newToken) {
    token.value = newToken
    if (newToken) {
      localStorage.setItem('admin_token', newToken)
    } else {
      localStorage.removeItem('admin_token')
    }
  }

  function setAdminInfo(info) {
    adminInfo.value = info
    if (info) {
      localStorage.setItem('admin_info', JSON.stringify(info))
    } else {
      localStorage.removeItem('admin_info')
    }
  }

  function login(loginData) {
    setToken(loginData.token)
    setAdminInfo(loginData)
  }

  function logout() {
    setToken('')
    setAdminInfo(null)
    localStorage.clear()
  }

  function initAdmin() {
    const storedAdmin = localStorage.getItem('admin_info')
    if (storedAdmin) {
      try {
        adminInfo.value = JSON.parse(storedAdmin)
      } catch (e) {
        console.error('解析管理员信息失败:', e)
      }
    }
  }

  return {
    adminInfo,
    token,
    isLoggedIn,
    adminId,
    username,
    nickname,
    setToken,
    setAdminInfo,
    login,
    logout,
    initAdmin
  }
})

