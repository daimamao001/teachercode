<template>
  <div class="ai-config">
    <h2 class="page-title">AI配置</h2>

    <el-card>
      <el-form :model="formData" label-width="120px">
        <el-form-item label="AI提供商">
          <el-select v-model="formData.provider" placeholder="请选择AI提供商">
            <el-option label="OpenAI" value="openai" />
            <el-option label="自定义模型" value="custom" />
          </el-select>
        </el-form-item>

        <el-form-item label="模型名称">
          <el-input v-model="formData.model" placeholder="例如: gpt-3.5-turbo" />
        </el-form-item>

        <el-form-item label="API地址">
          <el-input v-model="formData.apiUrl" placeholder="例如: https://api.openai.com/v1" />
        </el-form-item>

        <el-form-item label="API密钥">
          <el-input v-model="formData.apiKey" type="password" placeholder="请输入API密钥" />
        </el-form-item>

        <el-form-item label="超时时间(ms)">
          <el-input-number v-model="formData.timeout" :min="1000" :max="300000" />
        </el-form-item>

        <el-form-item label="最大Token数">
          <el-input-number v-model="formData.maxTokens" :min="100" :max="10000" />
        </el-form-item>

        <el-form-item label="温度参数">
          <el-slider v-model="formData.temperature" :min="0" :max="2" :step="0.1" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSave">保存配置</el-button>
          <el-button @click="handleTest">测试连接</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Prompt模板管理 -->
    <el-card class="mt-20">
      <template #header>
        <div class="card-header">
          <span>Prompt模板管理</span>
          <el-button type="primary" @click="handleAddTemplate">
            <el-icon><Plus /></el-icon>
            添加模板
          </el-button>
        </div>
      </template>

      <el-table :data="templates" border>
        <el-table-column prop="name" label="模板名称" />
        <el-table-column prop="type" label="类型" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button link type="primary" size="small">查看</el-button>
            <el-button link type="warning" size="small">编辑</el-button>
            <el-button link type="danger" size="small">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 简单试聊 -->
    <el-card class="mt-20">
      <template #header>
        <div class="card-header">
          <span>AI 试聊一次</span>
        </div>
      </template>
      <el-form :inline="true">
        <el-form-item label="消息">
          <el-input v-model="testMessage" placeholder="输入一段话，与AI试聊一次" style="width: 420px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleTestChat" :loading="testing">发送</el-button>
        </el-form-item>
      </el-form>
      <div v-if="testReply" class="test-reply">
        <strong>回复：</strong>
        <div class="reply-box">{{ testReply }}</div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { health as aiHealth, testChat } from '@/api/ai'

const formData = reactive({
  provider: 'openai',
  model: 'gpt-3.5-turbo',
  apiUrl: 'https://api.openai.com/v1',
  apiKey: '',
  timeout: 60000,
  maxTokens: 2000,
  temperature: 0.7
})

const templates = ref([
  {
    id: 1,
    name: 'SFBT对话模板',
    type: 'chat',
    status: 1
  },
  {
    id: 2,
    name: '评估解读模板',
    type: 'assessment',
    status: 1
  }
])

function handleSave() {
  ElMessage.success('配置已保存')
}

function handleTest() {
  ElMessage.info('正在测试连接...')
  aiHealth()
    .then(() => ElMessage.success('连接测试成功'))
    .catch(() => ElMessage.error('连接测试失败'))
}

function handleAddTemplate() {
  ElMessage.info('功能开发中...')
}

const testMessage = ref('你好')
const testing = ref(false)
const testReply = ref('')

async function handleTestChat() {
  if (!testMessage.value.trim()) {
    ElMessage.warning('请输入消息')
    return
  }
  testing.value = true
  testReply.value = ''
  try {
    const data = await testChat(testMessage.value)
    testReply.value = data?.content || '（无回复内容）'
  } catch (e) {
    ElMessage.error('测试失败')
  } finally {
    testing.value = false
  }
}
</script>

<style lang="scss" scoped>
.ai-config {
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

  .mt-20 {
    margin-top: 20px;
  }

  .test-reply {
    margin-top: 10px;
  }

  .reply-box {
    margin-top: 6px;
    padding: 12px;
    border-radius: 6px;
    background: #f5f7fa;
    white-space: pre-wrap;
  }
}
</style>

