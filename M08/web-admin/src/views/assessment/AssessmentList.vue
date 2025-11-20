<template>
  <div class="assessment-list">
    <h2 class="page-title">测评管理</h2>

    <el-card>
      <template #header>
        <div class="card-header">
          <span>测评量表列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            添加量表
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="量表名称" />
        <el-table-column prop="type" label="类型" width="120" />
        <el-table-column prop="questionCount" label="题目数" width="100" />
        <el-table-column prop="totalScore" label="总分" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
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
    name: '焦虑自评量表(SAS)',
    type: 'anxiety',
    questionCount: 20,
    totalScore: 100,
    status: 1
  },
  {
    id: 2,
    name: '抑郁自评量表(SDS)',
    type: 'depression',
    questionCount: 20,
    totalScore: 100,
    status: 1
  }
])

function handleAdd() {
  ElMessage.info('功能开发中...')
}
</script>

<style lang="scss" scoped>
.assessment-list {
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

