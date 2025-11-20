<template>
  <div class="user-page">
    <van-nav-bar title="个人中心" />

    <div class="user-header">
      <van-image
        round
        width="80"
        height="80"
        :src="userInfo?.avatar || defaultAvatar"
      />
      <div class="user-info">
        <h3>{{ userInfo?.nickname || '未登录' }}</h3>
        <p>@{{ userInfo?.username }}</p>
      </div>
    </div>

    <van-cell-group>
      <van-cell title="我的对话记录" is-link @click="goToHistory" />
      <van-cell title="测评记录" is-link @click="goToRecords" />
      <van-cell title="个人信息" is-link @click="goToProfile" />
      <van-cell title="设置" is-link @click="goToSettings" />
    </van-cell-group>

    <div class="logout-btn">
      <van-button type="danger" block @click="handleLogout">退出登录</van-button>
    </div>

    <van-tabbar v-model="active" fixed>
      <van-tabbar-item icon="wap-home-o" to="/home">首页</van-tabbar-item>
      <van-tabbar-item icon="chat-o" to="/chat">对话</van-tabbar-item>
      <van-tabbar-item icon="orders-o" to="/assessment">测评</van-tabbar-item>
      <van-tabbar-item icon="user-o" to="/user">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { showToast, showConfirmDialog } from 'vant'

const router = useRouter()
const userStore = useUserStore()
const active = ref(3)

const userInfo = computed(() => userStore.userInfo)
const defaultAvatar = 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'

function goToHistory() {
  showToast('功能开发中...')
}

function goToRecords() {
  showToast('功能开发中...')
}

function goToProfile() {
  showToast('功能开发中...')
}

function goToSettings() {
  showToast('功能开发中...')
}

async function handleLogout() {
  try {
    await showConfirmDialog({
      title: '提示',
      message: '确定要退出登录吗？'
    })
    userStore.logout()
    showToast('已退出登录')
    router.push('/login')
  } catch {
    // 取消
  }
}
</script>

<style scoped>
.user-page {
  min-height: 100vh;
  background-color: #f7f8fa;
  padding-bottom: 60px;
}

.user-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px;
  display: flex;
  align-items: center;
  color: white;
}

.user-info {
  margin-left: 20px;
}

.user-info h3 {
  font-size: 22px;
  margin-bottom: 8px;
}

.user-info p {
  font-size: 14px;
  opacity: 0.9;
}

.logout-btn {
  margin: 20px 16px;
}
</style>

