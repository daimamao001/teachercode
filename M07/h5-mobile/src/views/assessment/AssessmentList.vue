<template>
  <div class="assessment-page">
    <van-nav-bar title="心理测评" left-arrow @click-left="onClickLeft" />

    <div class="container">
      <van-cell-group>
        <van-cell
          v-for="item in assessments"
          :key="item.id"
          :title="item.name"
          :label="item.description"
          is-link
          @click="goToDetail(item)"
        />
      </van-cell-group>

      <van-empty v-if="assessments.length === 0" description="暂无测评" />
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
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'

const router = useRouter()
const active = ref(2)

const assessments = ref([
  {
    id: 1,
    name: '焦虑自评量表(SAS)',
    description: '评估焦虑程度'
  },
  {
    id: 2,
    name: '抑郁自评量表(SDS)',
    description: '评估抑郁程度'
  },
  {
    id: 3,
    name: '压力感知量表',
    description: '评估压力水平'
  }
])

function onClickLeft() {
  router.back()
}

function goToDetail(item) {
  showToast('功能开发中...')
}
</script>

<style scoped>
.assessment-page {
  min-height: 100vh;
  background-color: #f7f8fa;
  padding-bottom: 60px;
}

.container {
  padding: 16px;
}
</style>

