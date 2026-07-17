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

    <aside class="assistant-sidebar">
      <section class="guide-card">
        <header><Lightbulb :size="20" /><h3>智慧锦囊</h3></header>
        <div class="guide-primary">
          <div class="guide-title"><CircleAlert :size="16" /><strong>核心管理规定</strong></div>
          <ul>
            <li>门禁：每日 23:30 关闭大门</li>
            <li>用电：严禁使用 800W 以上电器</li>
            <li>访客：需在 22:00 前离开楼栋</li>
            <li>卫生：每周四下午进行宿舍检查</li>
          </ul>
        </div>
        <button type="button" class="guide-link" @click="ask('宿舍报修有哪些注意事项？')">
          <Wrench :size="17" /><span>报修小贴士</span><ChevronRight :size="17" />
        </button>
        <button type="button" class="guide-link" @click="ask('宿舍安全用电规定有哪些？')">
          <ShieldCheck :size="17" /><span>用电安全指南</span><ChevronRight :size="17" />
        </button>
      </section>

      <section class="popular-card">
        <header><h3>热门咨询</h3></header>
        <div class="popular-list">
          <button v-for="(item, index) in popularQuestions" :key="item" type="button" :class="{ featured: index === 0 }" @click="ask(item)">
            <span>{{ item }}</span><ChevronRight :size="17" />
          </button>
        </div>
      </section>
    </aside>
  </div>
</template>

<script setup>
import { nextTick, ref } from 'vue'
import { BookOpen, ChevronRight, CircleAlert, HelpCircle, Lightbulb, Send, ShieldCheck, Wrench } from '@lucide/vue'
import request from '../../utils/request'
import { ElMessage } from 'element-plus'

const question = ref('')
const messages = ref([])
const replying = ref(false)
const messagePanel = ref(null)

const prompts = ['宿舍门禁时间', '调宿申请流程', '如何提交报修', '水电费如何查询']
const popularQuestions = ['水电费账单查询', '遥控器遗失处理', '调宿申请资格']
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
  } catch (e) { console.error(e);
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
.assistant-page { display: grid; grid-template-columns: minmax(0, 1fr) 300px; gap: 20px; max-width: 1280px; margin: 0 auto; }
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
.assistant-sidebar { display: grid; align-content: start; gap: 18px; }
.guide-card, .popular-card { overflow: hidden; border: 1px solid var(--line); border-radius: 8px; background: var(--surface); }
.guide-card > header, .popular-card > header { display: flex; align-items: center; gap: 8px; min-height: 58px; padding: 0 18px; border-bottom: 1px solid var(--line); color: var(--text); }
.guide-card h3, .popular-card h3 { margin: 0; font-size: 16px; }
.guide-card > header svg { color: var(--primary); }
.guide-primary { padding: 18px; }
.guide-title { display: flex; align-items: center; gap: 8px; color: var(--text); font-size: 14px; }
.guide-title svg { color: #8ca0ba; }
.guide-primary ul { margin: 14px 0 0; padding-left: 20px; color: #5f7390; font-size: 13px; line-height: 2.15; }
.guide-primary li::marker { color: #8ca0ba; }
.guide-link { width: 100%; min-height: 48px; padding: 0 18px; display: grid; grid-template-columns: 20px 1fr 18px; align-items: center; gap: 8px; border: 0; background: transparent; color: #526780; text-align: left; cursor: pointer; }
.guide-link:hover { color: var(--primary); background: var(--primary-2); }
.guide-link svg:first-child { color: #8ca0ba; }
.popular-list { display: grid; gap: 4px; padding: 12px; }
.popular-list button { width: 100%; min-height: 44px; padding: 0 12px; display: flex; align-items: center; justify-content: space-between; border: 0; border-radius: 6px; background: transparent; color: #526780; cursor: pointer; text-align: left; }
.popular-list button:hover, .popular-list button.featured { color: var(--primary); background: var(--primary-2); }
.guide-link:focus-visible, .popular-list button:focus-visible { outline: 3px solid var(--primary-2); outline-offset: -2px; }
@media (max-width: 980px) {
  .assistant-page { grid-template-columns: 1fr; }
  .assistant-sidebar { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
@media (max-width: 600px) {
  .assistant-sidebar { grid-template-columns: 1fr; }
  .chat-card { min-height: calc(100dvh - 112px); border-radius: 0; }
  .prompt-grid { grid-template-columns: 1fr; }
  .bubble { max-width: 88%; }
}
</style>
