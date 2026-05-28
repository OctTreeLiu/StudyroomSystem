<template>
  <div class="guang-agent">
    <el-card class="agent-card">
      <template #header>
        <div class="card-header">
          <span>小光智能体</span>
          <el-button text @click="handleClear">清空对话</el-button>
        </div>
      </template>

      <div class="chat-body" ref="chatBodyRef">
        <div v-if="displayMessages.length === 0" class="empty-tip">
          还没有对话。你可以先问我：怎么制定学习计划？
        </div>

        <div
          v-for="(m, idx) in displayMessages"
          :key="idx"
          :class="['msg', m.role === 'user' ? 'user' : 'assistant']"
        >
          <div class="bubble">{{ m.content }}</div>
        </div>

        <div v-if="sending" class="msg assistant thinking-msg">
          <div class="bubble">
            <span class="thinking-label">等待思考中</span>
            <span class="thinking-ellipsis" aria-hidden="true">
              <span class="dot d1"></span>
              <span class="dot d2"></span>
              <span class="dot d3"></span>
            </span>
          </div>
        </div>
      </div>

      <div class="chat-input">
        <el-input
          v-model="input"
          type="textarea"
          :rows="3"
          placeholder="请输入你的问题（例如：帮我规划一周自习任务）"
          maxlength="2000"
          show-word-limit
          :disabled="sending"
        />
        <div class="actions">
          <el-button type="primary" :loading="sending" :disabled="!input.trim()" @click="handleSend">
            发送
          </el-button>
          <el-button :disabled="sending" @click="handleClear">重置</el-button>
        </div>
      </div>

      <div class="agent-tip">
        小光智能体是学习助手。请勿输入隐私或敏感信息。<br />
        出于隐私保护，本系统不会保留您的对话信息，请保存好关键信息后刷新。
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { chatWithGuangAi } from '@/api/aiAgent'

const getTodayText = () => {
  // 模型无法直接读取系统时间，所以把“当前日期”注入到 system prompt
  const d = new Date()
  const year = d.getFullYear()
  const month = d.getMonth() + 1
  const day = d.getDate()
  const week = ['日', '一', '二', '三', '四', '五', '六'][d.getDay()]
  return `${year}年${month}月${day}日，星期${week}`
}

const buildSystemPrompt = () => {
  return `你是逐光自习室的学习助手“小光智能体”。请用中文回答，给出可执行、分步骤的建议，语气友好。
当前日期：${getTodayText()}
当用户问到“今天/现在/当前日期”相关内容时，请以“当前日期”为依据。`
}

const sending = ref(false)
const input = ref('')
const chatBodyRef = ref(null)

// 为了让模型有上下文，messages 里保留 system / user / assistant
const messages = ref([
  {
    role: 'system',
    content: buildSystemPrompt()
  }
])

const displayMessages = computed(() => {
  return messages.value.filter(m => m.role !== 'system')
})

const scrollToBottom = async () => {
  await nextTick()
  const el = chatBodyRef.value
  if (el) {
    el.scrollTop = el.scrollHeight
  }
}

const handleClear = () => {
  messages.value = [{ role: 'system', content: buildSystemPrompt() }]
  input.value = ''
  nextTick(() => scrollToBottom())
}

const handleSend = async () => {
  const text = input.value.trim()
  if (!text || sending.value) return

  sending.value = true
  input.value = ''

  // 每次发送前刷新“当前日期”，避免页面长时间不刷新跨天后提示不准
  if (messages.value[0]?.role === 'system') {
    messages.value[0].content = buildSystemPrompt()
  }

  // 追加用户消息
  messages.value.push({
    role: 'user',
    content: text
  })

  await scrollToBottom()

  try {
    const res = await chatWithGuangAi({
      messages: messages.value
    })

    if (res?.data?.reply) {
      messages.value.push({
        role: 'assistant',
        content: res.data.reply
      })
    } else {
      ElMessage.error('AI 返回数据格式错误')
    }
  } catch (error) {
    ElMessage.error(error.message || '请求失败')
  } finally {
    sending.value = false
    await scrollToBottom()
  }
}

onMounted(() => {
  scrollToBottom()
})
</script>

<style scoped>
.guang-agent {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.agent-card :deep(.el-card__header) {
  padding: 18px 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.chat-body {
  height: 420px;
  overflow-y: auto;
  background: #f9fafb;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px;
}

.empty-tip {
  color: #909399;
  text-align: center;
  margin-top: 120px;
  font-size: 14px;
}

.msg {
  display: flex;
  margin: 10px 0;
}

.msg.user {
  justify-content: flex-end;
}

.msg.assistant {
  justify-content: flex-start;
}

.bubble {
  max-width: 80%;
  padding: 10px 12px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.msg.user .bubble {
  background: #409eff;
  color: white;
  border-bottom-right-radius: 4px;
}

.msg.assistant .bubble {
  background: white;
  color: #303133;
  border: 1px solid #ebeef5;
  border-bottom-left-radius: 4px;
}

.chat-input {
  margin-top: 14px;
  background: white;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px;
}

.actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 10px;
}

.agent-tip {
  margin-top: 10px;
  color: #909399;
  font-size: 12px;
}

.thinking-msg .bubble {
  position: relative;
  overflow: hidden; /* 流光不撑出圆角 */
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.thinking-msg .bubble > * {
  position: relative;
  z-index: 1;
}

.thinking-msg .thinking-label {
  color: #909399;
  font-size: 14px;
  font-weight: 500;
}

.thinking-msg .thinking-ellipsis {
  display: inline-flex;
  gap: 4px;
  align-items: flex-end;
}

.thinking-msg .dot {
  width: 6px;
  height: 6px;
  border-radius: 999px;
  background: #909399;
  animation: thinking-dot-bounce 1s infinite ease-in-out;
  will-change: transform;
}

.thinking-msg .dot.d2 {
  animation-delay: 0.15s;
}

.thinking-msg .dot.d3 {
  animation-delay: 0.3s;
}

@keyframes thinking-dot-bounce {
  0%,
  80%,
  100% {
    transform: translateY(0);
    opacity: 0.55;
  }
  40% {
    transform: translateY(-5px);
    opacity: 1;
  }
}

/* 气泡内轻微流光，提升“等待中”观感 */
.thinking-msg .bubble::after {
  content: '';
  position: absolute;
  top: -50%;
  left: -60%;
  width: 220%;
  height: 200%;
  background: linear-gradient(
    90deg,
    transparent 0%,
    rgba(64, 158, 255, 0.12) 40%,
    rgba(64, 158, 255, 0.28) 50%,
    rgba(64, 158, 255, 0.12) 60%,
    transparent 100%
  );
  transform: rotate(10deg);
  animation: thinking-shimmer 1.6s infinite ease-in-out;
  pointer-events: none;
  z-index: 0;
}

@keyframes thinking-shimmer {
  0% {
    transform: rotate(10deg) translateX(-25%);
  }
  100% {
    transform: rotate(10deg) translateX(25%);
  }
}
</style>

