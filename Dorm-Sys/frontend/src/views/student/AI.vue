<template>
  <div class="assistant-page">
    <section class="chat-card">
      <header class="chat-header">
        <div class="assistant-mark"><BookOpen :size="22" /></div>
        <div><h2>宿舍自助问答</h2><p>基于校内常见事务规则提供参考，不替代人工处理。</p></div>
      </header>

      <div ref="messagePanel" class="message-panel" aria-live="polite">
        <div v-if="messages.length === 0" class="empty-state">
          <HelpCircle :size="34" />
          <strong>请选择常见问题，或输入您想了解的内容</strong>
          <div class="prompt-grid">
            <button v-for="prompt in prompts" :key="prompt" type="button" @click="ask(prompt)">{{ prompt }}</button>
          </div>
        </div>
        <div v-else class="message-list">
          <div v-for="message in messages" :key="message.id" class="message" :class="message.role">
            <div class="bubble">{{ message.content }}</div>
          </div>
          <div v-if="replying" class="message assistant"><div class="bubble muted">正在查询规则库...</div></div>
        </div>
      </div>

      <form class="input-bar" @submit.prevent="send">
        <label class="sr-only" for="question-input">宿舍问题</label>
        <input id="question-input" v-model="question" maxlength="200" placeholder="例如：调宿需要准备什么？" />
        <button type="submit" :disabled="replying || !question.trim()" aria-label="发送问题"><Send :size="19" /></button>
      </form>
      <p class="disclaimer">当前为本地规则库问答；紧急故障、安全问题或未匹配事项请联系宿管。</p>
    </section>
  </div>
</template>

<script setup>
import { nextTick, ref } from 'vue'
import { BookOpen, HelpCircle, Send } from '@lucide/vue'
import request from '../../utils/request'
import { ElMessage } from 'element-plus'

const question = ref('')
const messages = ref([])
const replying = ref(false)
const messagePanel = ref(null)

const prompts = ['宿舍门禁时间', '调宿申请流程', '如何提交报修', '水电费如何查询']
const rules = [
  { keys: ['门禁', '晚归'], answer: '宿舍门禁通常为每日 23:30。晚归请及时联系宿管，并按要求完成晚归登记。' },
  { keys: ['调宿'], answer: '请在“调宿申请”中填写原因并选择目标房间。宿管审批通过后，系统会同步更新床位和宿舍信息。' },
  { keys: ['报修', '维修'], answer: '请在“报修申请”中选择故障类型并描述具体情况。提交后可在同一页面跟踪处理进度。' },
  { keys: ['水电费', '费用', '账单'], answer: '可在“费用查询”中查看当前宿舍账单。线上缴费尚未开放，请按页面指引到服务中心办理。' }
]

const ask = (text) => {
  question.value = text
  send()
}

const send = async () => {
  const content = question.value.trim()
  if (!content || replying.value) return
  messages.value.push({ id: Date.now(), role: 'user', content })
  question.value = ''
  replying.value = true
  await nextTick()
  scrollToBottom()

  try {
    const res = await request({
      url: '/ai/chat',
      method: 'get',
      params: { question: content }
    })
    messages.value.push({ id: Date.now() + 1, role: 'assistant', content: res })
  } catch (e) {
    messages.value.push({ id: Date.now() + 1, role: 'assistant', content: '服务异常，请稍后再试。' })
  } finally {
    replying.value = false
    await nextTick()
    scrollToBottom()
  }
}

const scrollToBottom = () => {
  if (messagePanel.value) messagePanel.value.scrollTop = messagePanel.value.scrollHeight
}
</script>

<style scoped>
.assistant-page { max-width: 980px; margin: 0 auto; }
.chat-card { display: grid; grid-template-rows: auto minmax(360px, 1fr) auto auto; min-height: calc(100dvh - 136px); overflow: hidden; border: 1px solid var(--line); border-radius: 8px; background: var(--surface); }
.chat-header { display: flex; align-items: center; gap: 14px; padding: 20px 24px; border-bottom: 1px solid var(--line); }
.assistant-mark { display: grid; width: 42px; height: 42px; place-items: center; border-radius: 8px; background: var(--primary-2); color: var(--primary); }
.chat-header h2 { margin: 0 0 4px; color: var(--text); font-size: 19px; }
.chat-header p, .disclaimer { margin: 0; color: var(--sub); font-size: 13px; }
.message-panel { overflow-y: auto; padding: 24px; background: var(--bg); }
.empty-state { display: grid; min-height: 100%; place-content: center; justify-items: center; gap: 16px; color: var(--sub); text-align: center; }
.empty-state strong { color: var(--text); }
.prompt-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px; width: min(100%, 480px); }
.prompt-grid button { min-height: 44px; border: 1px solid var(--line); border-radius: 6px; background: var(--surface); color: var(--text); cursor: pointer; }
.prompt-grid button:hover { border-color: var(--primary); color: var(--primary); }
.message-list { display: grid; gap: 12px; }
.message { display: flex; }
.message.user { justify-content: flex-end; }
.bubble { max-width: min(72%, 560px); border-radius: 8px; padding: 11px 14px; background: var(--surface); color: var(--text); line-height: 1.65; }
.message.user .bubble { background: var(--primary); color: #fff; }
.bubble.muted { color: var(--sub); }
.input-bar { display: flex; gap: 10px; padding: 16px 20px; border-top: 1px solid var(--line); }
.input-bar input { flex: 1; min-width: 0; height: 44px; border: 1px solid var(--line); border-radius: 6px; padding: 0 14px; outline: none; color: var(--text); }
.input-bar input:focus { border-color: var(--primary); box-shadow: 0 0 0 3px var(--primary-2); }
.input-bar button { display: grid; width: 44px; height: 44px; place-items: center; border: 0; border-radius: 6px; background: var(--primary); color: #fff; cursor: pointer; }
.input-bar button:disabled { cursor: not-allowed; opacity: .5; }
.disclaimer { padding: 0 20px 16px; text-align: center; }
.sr-only { position: absolute; width: 1px; height: 1px; overflow: hidden; clip: rect(0, 0, 0, 0); }
@media (max-width: 600px) {
  .chat-card { min-height: calc(100dvh - 112px); border-radius: 0; }
  .prompt-grid { grid-template-columns: 1fr; }
  .bubble { max-width: 88%; }
}
</style>
