<template>
  <div>
    <div class="grid col-2-right">
      <div class="card chat-shell">
        <div class="chat-panel">
          <div v-if="messages.length === 0" class="chat-empty-state">
            <div class="bulb">
              <el-icon :size="44"><component :is="Lightbulb" /></el-icon>
            </div>
            <h2>您好，我是您的智慧宿舍助手</h2>
            <p class="subtitle">您可以询问我任何关于宿舍生活、规定、费用或报修的问题</p>

            <div class="prompt-grid">
              <button v-for="prompt in prompts" :key="prompt.text" @click="ask(prompt.text)">
                <span class="tile-icon" :style="prompt.style">
                  <el-icon><component :is="prompt.icon" /></el-icon>
                </span>
                <span>{{ prompt.text }}</span>
              </button>
            </div>
          </div>

          <div v-else class="message-list">
            <div v-for="message in messages" :key="message.id" class="message" :class="message.role">
              <div class="message-bubble">{{ message.content }}</div>
            </div>
          </div>
        </div>

        <div class="chat-input-bar">
          <div class="input-shell">
            <input
              v-model="question"
              type="text"
              placeholder="询问我关于宿舍的任何问题..."
              @keyup.enter="send"
            />
            <button class="primary-btn send-btn" @click="send">
              <el-icon :size="20"><component :is="Send" /></el-icon>
            </button>
          </div>
          <div class="ai-note">由智慧宿舍知识库提供本地问答支持</div>
        </div>
      </div>

      <div class="side-stack">
        <div class="card">
          <div class="card-head side-head">
            <h2>智慧锦囊</h2>
            <el-icon color="var(--primary)"><component :is="BookOpen" /></el-icon>
          </div>
          <div class="card-body list">
            <div class="row" style="align-items: flex-start;">
              <span class="tile-icon" style="background: var(--warn-2); color: var(--warn)">
                <el-icon><component :is="AlertCircle" /></el-icon>
              </span>
              <div class="row-main">
                <div class="row-title" style="margin-bottom: 8px;">核心管理规定</div>
                <div class="rule-list">
                  <div>门禁：每日 23:30 关闭大门</div>
                  <div>用电：严禁使用 800W 以上电器</div>
                  <div>访客：需在 22:00 前离开楼栋</div>
                  <div>卫生：每周四下午进行宿舍检查</div>
                </div>
              </div>
            </div>
            <div class="row clickable" @click="ask('报修指引')">
              <span class="tile-icon"><el-icon><component :is="Settings" /></el-icon></span>
              <div class="row-main"><div class="row-title">报修小贴士</div></div>
              <el-icon color="var(--sub)"><component :is="ChevronRight" /></el-icon>
            </div>
            <div class="row clickable" @click="ask('用电安全指南')">
              <span class="tile-icon"><el-icon><component :is="Zap" /></el-icon></span>
              <div class="row-main"><div class="row-title">用电安全指南</div></div>
              <el-icon color="var(--sub)"><component :is="ChevronRight" /></el-icon>
            </div>
          </div>
        </div>

        <div class="card">
          <div class="card-head">
            <h2>热门咨询</h2>
          </div>
          <div class="card-body list">
            <div v-for="item in hotQuestions" :key="item" class="row clickable" @click="ask(item)">
              <div class="row-main"><div class="row-title">{{ item }}</div></div>
              <el-icon color="var(--sub)"><component :is="ChevronRight" /></el-icon>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Lightbulb, Clock, Repeat, Settings, Wallet, Send, BookOpen, AlertCircle, ChevronRight, Zap } from '@lucide/vue'

const question = ref('')
const messages = ref([])

const prompts = [
  { text: '门禁查询', icon: Clock, style: 'background: var(--info-2); color: var(--info)' },
  { text: '调宿流程', icon: Repeat, style: 'background: var(--primary-2); color: var(--primary)' },
  { text: '报修指引', icon: Settings, style: 'background: var(--orange-2); color: var(--orange)' },
  { text: '缴费咨询', icon: Wallet, style: 'background: var(--ok-2); color: var(--ok)' }
]

const hotQuestions = ['水电费账单查询', '遥控器遗失处理', '调宿申请资格']

const answers = [
  { keys: ['门禁'], answer: '宿舍门禁通常为每日 23:30。晚归需要及时联系宿管，并在晚归登记中说明原因。' },
  { keys: ['调宿'], answer: '调宿需要先提交申请，说明原因和目标宿舍；宿管审批通过后再进行床位调整。' },
  { keys: ['报修'], answer: '报修时请填写房间、故障类型和详细描述。紧急水电故障建议同时联系宿管加急处理。' },
  { keys: ['缴费', '水电费', '账单'], answer: '水电费可在费用查询中查看账单状态。未缴账单建议在截止日期前完成缴纳。' },
  { keys: ['用电'], answer: '宿舍内禁止使用大功率电器，离开宿舍前请关闭插排和电源。' },
  { keys: ['遥控器'], answer: '遥控器遗失可先在宿舍和公共区域查找，确认遗失后向宿管登记并按规定补办。' }
]

const ask = (text) => {
  question.value = text
  send()
}

const send = () => {
  const content = question.value.trim()
  if (!content) return
  messages.value.push({ id: Date.now(), role: 'user', content })
  question.value = ''
  window.setTimeout(() => {
    messages.value.push({ id: Date.now() + 1, role: 'assistant', content: replyFor(content) })
  }, 200)
}

const replyFor = (content) => {
  const matched = answers.find((item) => item.keys.some((key) => content.includes(key)))
  return matched?.answer || '这个问题已记录。建议补充房间号、时间和具体情况，宿管会更容易判断处理路径。'
}
</script>

<style scoped>
.chat-shell {
  padding: 0;
  display: grid;
  grid-template-rows: 1fr auto;
  overflow: hidden;
  height: calc(100vh - 132px);
  border: none;
}

.chat-panel {
  background: var(--surface);
  overflow-y: auto;
  padding: 32px;
}

.chat-empty-state {
  min-height: 100%;
  display: grid;
  place-items: center;
  align-content: center;
  text-align: center;
}

.chat-empty-state h2 {
  margin: 18px 0 8px;
  color: var(--text);
}

.prompt-grid {
  margin-top: 24px;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.message {
  display: flex;
}

.message.user {
  justify-content: flex-end;
}

.message-bubble {
  max-width: 68%;
  padding: 12px 16px;
  border-radius: 14px;
  background: var(--bg);
  color: var(--text);
  line-height: 1.6;
}

.message.user .message-bubble {
  background: var(--primary);
  color: #fff;
}

.chat-input-bar {
  padding: 24px;
  border-top: 1px solid var(--line);
  background: var(--surface);
}

.input-shell {
  display: flex;
  gap: 12px;
  align-items: center;
  background: var(--bg);
  border-radius: 28px;
  padding: 6px 6px 6px 20px;
  border: 1px solid var(--line);
}

.input-shell input {
  flex: 1;
  border: none;
  background: transparent;
  outline: none;
  color: var(--text);
  font-size: 15px;
}

.send-btn {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  padding: 0;
  display: grid;
  place-items: center;
  cursor: pointer;
  flex-shrink: 0;
  border: none;
}

.ai-note {
  text-align: center;
  font-size: 12px;
  color: var(--sub);
  margin-top: 16px;
}

.side-stack {
  display: grid;
  gap: 24px;
  align-content: start;
}

.side-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.rule-list {
  font-size: 13px;
  color: var(--sub);
  line-height: 1.8;
}

.clickable {
  cursor: pointer;
}
</style>
