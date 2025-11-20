<template>
  <div class="user-list">
    <h2 class="page-title">用户管理</h2>

    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card class="table-card">
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="nickname" label="昵称" width="150" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="180" />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">
              查看
            </el-button>
            <el-button link type="warning" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const searchForm = reactive({
  username: '',
  status: null
})

const tableData = ref([
  {
    id: 1,
    username: 'user001',
    nickname: '张三',
    email: 'zhangsan@example.com',
    phone: '13800138000',
    status: 1,
    createdAt: '2025-01-01 10:00:00'
  },
  {
    id: 2,
    username: 'user002',
    nickname: '李四',
    email: 'lisi@example.com',
    phone: '13800138001',
    status: 1,
    createdAt: '2025-01-01 11:00:00'
  }
])

const pagination = reactive({
  page: 1,
  size: 10,
  total: 2
})

onMounted(() => {
  // 初始加载数据
})

function handleSearch() {
  ElMessage.info('功能开发中...')
}

function handleReset() {
  searchForm.username = ''
  searchForm.status = null
}

function handleView(row) {
  ElMessage.info(`查看用户: ${row.username}`)
}

function handleEdit(row) {
  ElMessage.info(`编辑用户: ${row.username}`)
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除用户 ${row.username} 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      ElMessage.success('删除成功')
    })
    .catch(() => {})
}

function handleSizeChange(val) {
  pagination.size = val
}

function handleCurrentChange(val) {
  pagination.page = val
}
</script>

<style lang="scss" scoped>
.user-list {
  .page-title {
    margin: 0 0 20px;
    font-size: 24px;
    color: #303133;
  }

  .search-card,
  .table-card {
    margin-bottom: 20px;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>

