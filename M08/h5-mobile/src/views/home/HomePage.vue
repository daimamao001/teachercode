<template>
  <div class="home-page">
    <!-- 头部 -->
    <div class="header">
      <h1 class="title">心理陪伴</h1>
      <p class="subtitle">您的心理健康助手</p>
    </div>

    <!-- 功能卡片 -->
    <div class="feature-cards">
      <div class="card" @click="goToChat">
        <van-icon name="chat-o" size="40" color="#4a90e2" />
        <h3>聊一聊</h3>
        <p>AI智能对话，随时倾诉</p>
      </div>

      <div class="card" @click="goToAssessment">
        <van-icon name="orders-o" size="40" color="#52c41a" />
        <h3>测一测</h3>
        <p>心理健康评估</p>
      </div>

      <div class="card" @click="goToPractice">
        <van-icon name="smile-o" size="40" color="#faad14" />
        <h3>练一练</h3>
        <p>心理成长训练</p>
      </div>
    </div>

    <!-- 底部导航 -->
    <van-tabbar v-model="active" fixed>
      <van-tabbar-item icon="wap-home-o" to="/home">首页</van-tabbar-item>
      <van-tabbar-item icon="chat-o" to="/chat">对话</van-tabbar-item>
      <van-tabbar-item icon="orders-o" to="/assessment">测评</van-tabbar-item>
      <van-tabbar-item icon="user-o" to="/user">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'

const router = useRouter()
const active = ref(0)

function goToChat() {
  router.push('/chat')
}

function goToAssessment() {
  router.push('/assessment')
}

function goToPractice() {
  showToast('功能开发中...')
}
</script>

<style scoped>
.home-page {
  padding-bottom: 60px;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px;
  text-align: center;
  color: white;
}

.title {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 10px;
}

.subtitle {
  font-size: 16px;
  opacity: 0.9;
}

.feature-cards {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  padding: 20px;
}

.card {
  background: white;
  border-radius: 12px;
  padding: 30px 20px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: transform 0.2s;
}

.card:active {
  transform: scale(0.98);
}

.card h3 {
  font-size: 18px;
  margin: 12px 0 8px;
  color: #333;
}

.card p {
  font-size: 14px;
  color: #999;
}
</style>

