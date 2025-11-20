<template>
  <div class="dashboard">
    <h2 class="page-title">数据概览</h2>

    <!-- 系统状态 -->
    <el-row :gutter="20" class="status-row">
      <el-col :span="24">
        <el-card>
          <div class="status-bar">
            <div>
              <strong>AI服务状态：</strong>
              <el-tag :type="aiOk ? 'success' : 'danger'">{{ aiOk ? '可用' : '不可用' }}</el-tag>
              <el-button class="ml-10" size="small" @click="checkAI">刷新</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#409eff"><User /></el-icon>
            <div class="stat-info">
              <div class="stat-value">1,234</div>
              <div class="stat-label">总用户数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#67c23a"><ChatDotSquare /></el-icon>
            <div class="stat-info">
              <div class="stat-value">5,678</div>
              <div class="stat-label">对话次数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#e6a23c"><Document /></el-icon>
            <div class="stat-info">
              <div class="stat-value">890</div>
              <div class="stat-label">测评完成数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#f56c6c"><Warning /></el-icon>
            <div class="stat-info">
              <div class="stat-value">12</div>
              <div class="stat-label">预警用户</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>用户增长趋势</span>
            </div>
          </template>
          <div ref="userChart" style="height: 300px"></div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>对话类型分布</span>
            </div>
          </template>
          <div ref="chatChart" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近动态 -->
    <el-card class="recent-activities">
      <template #header>
        <div class="card-header">
          <span>最近动态</span>
        </div>
      </template>

      <el-timeline>
        <el-timeline-item timestamp="2025-01-01 10:30" placement="top">
          <p>用户 张三 完成了焦虑自评量表</p>
        </el-timeline-item>
        <el-timeline-item timestamp="2025-01-01 10:15" placement="top">
          <p>用户 李四 开始了AI对话</p>
        </el-timeline-item>
        <el-timeline-item timestamp="2025-01-01 10:00" placement="top">
          <p>系统添加了新的心理测评量表</p>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { health as aiHealth } from '@/api/ai'

const userChart = ref(null)
const chatChart = ref(null)
const aiOk = ref(false)

onMounted(() => {
  // 这里可以使用 ECharts 初始化图表
  // 由于是简化版本，暂不实现图表
  console.log('Dashboard mounted')
  checkAI()
})

async function checkAI() {
  try {
    await aiHealth()
    aiOk.value = true
  } catch (e) {
    aiOk.value = false
  }
}
</script>

<style lang="scss" scoped>
.dashboard {
  .page-title {
    margin: 0 0 20px;
    font-size: 24px;
    color: #303133;
  }

  .stats-row {
    margin-bottom: 20px;
  }

  .status-row {
    margin-bottom: 20px;
  }

  .status-bar {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .ml-10 { margin-left: 10px; }

  .stat-card {
    .stat-content {
      display: flex;
      align-items: center;

      .stat-icon {
        font-size: 48px;
        margin-right: 20px;
      }

      .stat-info {
        .stat-value {
          font-size: 28px;
          font-weight: bold;
          color: #303133;
          margin-bottom: 5px;
        }

        .stat-label {
          font-size: 14px;
          color: #909399;
        }
      }
    }
  }

  .charts-row {
    margin-bottom: 20px;
  }

  .card-header {
    font-size: 16px;
    font-weight: bold;
  }

  .recent-activities {
    :deep(.el-timeline-item__timestamp) {
      color: #909399;
    }
  }
}
</style>

