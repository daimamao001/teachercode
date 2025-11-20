<template>
  <div class="chat-page">
    <van-nav-bar title="AI对话" left-arrow @click-left="onClickLeft">
      <template #right>
        <van-icon name="like-o" @click="checkHealth" />
      </template>
    </van-nav-bar>

    <div class="chat-container" ref="chatContainer">
      <div v-for="msg in messages" :key="msg.id" :class="['message', msg.role]">
        <div class="message-content">{{ msg.content }}</div>
      </div>
    </div>

    <div class="input-bar">
      <van-field
        v-model="inputText"
        placeholder="说点什么..."
        @keyup.enter="sendMessage"
      />
      <van-button type="primary" @click="sendMessage" :loading="sending">
        发送
      </van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { createSession, getMessages, sendMessage as sendMsgApi } from '@/api/chat'
import { health as aiHealth, testChat } from '@/api/ai'

const router = useRouter()
const chatContainer = ref(null)
const messages = ref([])
const inputText = ref('')
const sending = ref(false)
const sessionId = ref(null)

function onClickLeft() {
  router.back()
}

onMounted(async () => {
  // 初始化会话
  try {
    const data = await createSession({ title: `移动端会话_${Date.now()}` })
    sessionId.value = data.id
    // 加载历史消息（可能为空）
    const list = await getMessages(sessionId.value)
    messages.value = (list || []).map((m) => ({
      id: m.id || `${m.role}_${m.timestamp || Math.random()}`,
      role: String(m.role || '').toLowerCase() === 'assistant' ? 'assistant' : 'user',
      content: m.content || ''
    }))
    if (messages.value.length === 0) {
      messages.value.push({
        id: 1,
        role: 'assistant',
        content: '你好！我是心理陪伴AI助手，有什么我可以帮助你的吗？'
      })
    }
    await nextTick();
    scrollToBottom()
  } catch (e) {
    // 会话创建失败时退化为AI单轮测试模式
    showToast('会话创建失败，已切到试聊模式')
  }
})

async function sendMessage() {
  if (!inputText.value.trim()) return

  const userMessage = {
    id: Date.now(),
    role: 'user',
    content: inputText.value
  }

  messages.value.push(userMessage)
  inputText.value = ''

  // 滚动到底部
  await nextTick()
  scrollToBottom()

  sending.value = true
  try {
    if (sessionId.value) {
      // 调用聊天消息接口（返回AI回复）
      const res = await sendMsgApi({ sessionId: sessionId.value, content: userMessage.content })
      const replyContent = res?.content || '我理解你的感受。我们可以一起梳理下。'
      messages.value.push({ id: Date.now() + 1, role: 'assistant', content: replyContent })
    } else {
      // 退化到AI单轮对话
      const data = await testChat(userMessage.content)
      const replyContent = data?.content || '我理解你的感受。我们可以一起梳理下。'
      messages.value.push({ id: Date.now() + 1, role: 'assistant', content: replyContent })
    }
  } catch (e) {
    console.error(e)
    showToast('发送失败，请稍后再试')
  } finally {
    sending.value = false
    nextTick(() => scrollToBottom())
  }
}

function scrollToBottom() {
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight
  }
}

async function checkHealth() {
  try {
    await aiHealth()
    showToast('AI服务正常')
  } catch (e) {
    showToast('AI服务异常')
  }
}
</script>

<style scoped>
.chat-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f7f8fa;
}

.chat-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.message {
  margin-bottom: 16px;
  display: flex;
}

.message.user {
  justify-content: flex-end;
}

.message.assistant {
  justify-content: flex-start;
}

.message-content {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 12px;
  word-wrap: break-word;
}

.message.user .message-content {
  background-color: #4a90e2;
  color: white;
}

.message.assistant .message-content {
  background-color: white;
  color: #333;
}

.input-bar {
  display: flex;
  align-items: center;
  padding: 12px;
  background-color: white;
  border-top: 1px solid #eee;
}

.input-bar :deep(.van-field) {
  flex: 1;
  margin-right: 12px;
}
</style>

