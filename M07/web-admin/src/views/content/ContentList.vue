<template>
  <div class="content-list">
    <h2 class="page-title">内容管理</h2>

    <el-card>
      <template #header>
        <div class="card-header">
          <span>文章列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新建文章
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="author" label="作者" width="120" />
        <el-table-column prop="viewCount" label="阅读量" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishedAt" label="发布时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small">查看</el-button>
            <el-button link type="warning" size="small">编辑</el-button>
            <el-button link type="danger" size="small">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const tableData = ref([
  {
    id: 1,
    title: '如何缓解焦虑情绪',
    category: '心理健康',
    author: '编辑部',
    viewCount: 1234,
    status: 1,
    publishedAt: '2025-01-01 10:00:00'
  },
  {
    id: 2,
    title: '大学生压力管理指南',
    category: '心理健康',
    author: '编辑部',
    viewCount: 567,
    status: 1,
    publishedAt: '2025-01-02 11:00:00'
  }
])

function handleAdd() {
  ElMessage.info('功能开发中...')
}
</script>

<style lang="scss" scoped>
.content-list {
  .page-title {
    margin: 0 0 20px;
    font-size: 24px;
    color: #303133;
  }

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
}
</style>

