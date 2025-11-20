<template>
  <div class="login-page">
    <div class="header">
      <h1>心理陪伴</h1>
      <p>您的心理健康助手</p>
    </div>

    <div class="login-form">
      <van-form @submit="onSubmit">
        <van-cell-group inset>
          <van-field
            v-model="formData.username"
            name="username"
            label="用户名"
            placeholder="请输入用户名"
            :rules="[{ required: true, message: '请输入用户名' }]"
          />
          <van-field
            v-model="formData.password"
            type="password"
            name="password"
            label="密码"
            placeholder="请输入密码"
            :rules="[{ required: true, message: '请输入密码' }]"
          />
        </van-cell-group>

        <div class="btn-group">
          <van-button round block type="primary" native-type="submit" :loading="loading">
            登录
          </van-button>
          <van-button round block @click="goToRegister">
            注册账号
          </van-button>
        </div>
      </van-form>
      <div class="hint">测试账号：testuser / admin123</div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { login } from '@/api/user'
import { showToast } from 'vant'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const formData = ref({
  username: '',
  password: ''
})

async function onSubmit() {
  try {
    loading.value = true
    const data = await login(formData.value)
    
    // 保存用户信息和token
    userStore.login(data)
    localStorage.setItem('userInfo', JSON.stringify(data))
    
    showToast('登录成功')
    
    // 跳转到之前的页面或首页
    const redirect = route.query.redirect || '/home'
    router.replace(redirect)
  } catch (error) {
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}

function goToRegister() {
  showToast('建议使用测试账号 testuser / admin123 登录；如需注册，请在Web端操作。')
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px;
}

.header {
  text-align: center;
  color: white;
  margin-bottom: 60px;
}

.header h1 {
  font-size: 36px;
  font-weight: bold;
  margin-bottom: 12px;
}

.header p {
  font-size: 16px;
  opacity: 0.9;
}

.login-form {
  background: white;
  border-radius: 16px;
  padding: 30px 20px;
}

.btn-group {
  padding: 20px 16px;
}

.btn-group .van-button {
  margin-bottom: 12px;
}

.hint {
  text-align: center;
  font-size: 12px;
  color: #909399;
}
</style>

