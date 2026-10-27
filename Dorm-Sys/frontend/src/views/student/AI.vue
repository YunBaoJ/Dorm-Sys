<template>
  <div class="assistant-page">
    <section class="chat-card">
      <header class="chat-header">
        <div class="assistant-mark"><component :is="currentConfig.headerIcon" :size="22" /></div>
        <div>
          <h2>{{ currentConfig.title }}</h2>
          <p>{{ currentConfig.subtitle }}</p>
        </div>
      </header>

      <div ref="messagePanel" class="message-panel" aria-live="polite">
        <div v-if="messages.length === 0" class="empty-state">
          <HelpCircle :size="34" />
          <strong>{{ currentConfig.emptyTip }}</strong>
          <div class="prompt-grid">
            <button v-for="prompt in currentConfig.prompts" :key="prompt" type="button" @click="ask(prompt)">{{ prompt }}</button>
          </div>
        </div>
        <div v-else class="message-list">
          <div v-for="message in messages" :key="message.id" class="message" :class="message.role">
            <div 
              v-if="message.role === 'assistant'" 
              class="bubble assistant-bubble markdown-content" 
              v-html="renderMarkdown(message.content)"
            ></div>
            <div v-else class="bubble user-bubble">{{ message.content }}</div>
          </div>
          <div v-if="replying" class="message assistant">
            <div class="bubble muted typing-indicator">
              <span>●</span><span>●</span><span>●</span>
              <span class="typing-text">通义千问 Qwen 正在思考与编写回答...</span>
            </div>
          </div>
        </div>
      </div>

      <form class="input-bar" @submit.prevent="send">
        <label class="sr-only" for="question-input">提问内容</label>
        <input id="question-input" v-model="question" maxlength="300" :placeholder="currentConfig.placeholder" />
        <button type="submit" :disabled="replying || !question.trim()" aria-label="发送问题"><Send :size="19" /></button>
      </form>
      <p class="disclaimer">{{ currentConfig.disclaimer }}</p>
    </section>

    <aside class="assistant-sidebar">
      <section class="guide-card">
        <header><Lightbulb :size="20" /><h3>{{ currentConfig.guideTitle }}</h3></header>
        <div class="guide-primary">
          <div class="guide-title"><CircleAlert :size="16" /><strong>{{ currentConfig.rulesTitle }}</strong></div>
          <ul>
            <li v-for="rule in currentConfig.rules" :key="rule">{{ rule }}</li>
          </ul>
        </div>
        <button 
          v-for="link in currentConfig.guideLinks" 
          :key="link.title" 
          type="button" 
          class="guide-link" 
          @click="ask(link.question)"
        >
          <component :is="link.icon" :size="17" /><span>{{ link.title }}</span><ChevronRight :size="17" />
        </button>
      </section>

      <section class="popular-card">
        <header><h3>{{ currentConfig.popularTitle }}</h3></header>
        <div class="popular-list">
          <button v-for="(item, index) in currentConfig.popularQuestions" :key="item" type="button" :class="{ featured: index === 0 }" @click="ask(item)">
            <span>{{ item }}</span><ChevronRight :size="17" />
          </button>
        </div>
      </section>
    </aside>
  </div>
</template>

<script setup>
import { nextTick, ref, computed, onMounted } from 'vue'
import { BookOpen, ChevronRight, CircleAlert, HelpCircle, Lightbulb, Send, ShieldCheck, Wrench, BarChart2, UserCheck } from '@lucide/vue'
import request from '../../utils/request'
import { useUserStore } from '../../store/user'
import { marked } from 'marked'

const userStore = useUserStore()

// 配置 marked 解析规则
marked.setOptions({
  gfm: true,
  breaks: true
})

const renderMarkdown = (text) => {
  if (!text) return ''
  try {
    return marked.parse(text)
  } catch (e) {
    return text
  }
}

const botName = ref('小寝管家')
const question = ref('')
const messages = ref([])
const replying = ref(false)
const messagePanel = ref(null)

// 宿管亲切称呼计算（男为王叔/某叔，女为张阿姨/某阿姨）
const managerSalutation = computed(() => {
  const name = userStore.userInfo?.name || ''
  const gender = userStore.userInfo?.gender || ''
  const surname = name ? name.charAt(0) : ''
  if (gender === '女') {
    return surname ? `${surname}阿姨` : '张阿姨'
  }
  return surname ? `${surname}叔` : '王叔'
})

// 学生称呼计算
const studentSalutation = computed(() => {
  const name = userStore.userInfo?.name
  return name ? `${name}同学` : '同学'
})

// 各角色差异化配置
const currentConfig = computed(() => {
  const role = userStore.role
  if (role === 'admin') {
    return {
      title: `系统全局 AI 超级助理（${botName.value}）`,
      subtitle: '面向系统总控 · 赋能数据洞察、房源调度、全局安防与运维保障',
      headerIcon: ShieldCheck,
      placeholder: '管理员大大，请问今天有什么系统运维或数据分析需要我协助？',
      emptyTip: '管理员大大，请选择管理建议，或直接输入管理指令与分析需求',
      prompts: ['全校宿舍入住率现状分析', '违规用电高发楼栋排查', '本学期报修工单处理时效分析', '大模型与系统安全配置建议'],
      guideTitle: '系统运维管理锦囊',
      rulesTitle: '系统核心管理规范',
      rules: [
        '安全：定期进行数据库离线冷备份与权限复核',
        '风控：密切监控异常批量调宿与晚归联动告警',
        '权限：严格遵循最小必要与全操作审计原则',
        '应急：服务器高负载时优先保障核心业务通道'
      ],
      guideLinks: [
        { title: '指标监控体系', question: '请帮我梳理一套宿舍系统数据分析与安全监控的关键指标体系', icon: BarChart2 },
        { title: '应急协同预案', question: '如果楼栋发生停水停电或网络中断等突发事件，系统与宿管的应急协同方案是什么？', icon: ShieldCheck }
      ],
      popularTitle: '热点管理议题',
      popularQuestions: ['高并发调宿审批处理建议', '近30天水电异常波动排查', '晚归预警机制与数据联动', '宿舍资源统筹与楼栋分配策略'],
      disclaimer: '由通义千问 Qwen2.5 大模型强力驱动 · 辅助全局决策与数据分析 · 请以学校最新行政规定为准'
    }
  }

  if (role === 'dormmanager') {
    const greeting = `${managerSalutation.value}您好`
    return {
      title: `楼栋智能宿管助手（${botName.value}）`,
      subtitle: '面向宿管老师 · 协助楼栋查寝、报修派工、访客登记与晚归处理',
      headerIcon: ShieldCheck,
      placeholder: `${greeting}，请问今天有什么楼务工作需要我协助？`,
      emptyTip: `${greeting}，请选择日常楼务，或直接输入您想了解的内容`,
      prompts: ['学生晚归登记与通知流程', '报修工单如何快速派工', '学生违规电器排查技巧', '如何开导有寝室矛盾的同学'],
      guideTitle: '宿管工作锦囊',
      rulesTitle: '楼栋日常巡查要点',
      rules: [
        '查寝：每晚 23:30 按时巡查公共区域与锁闭大门',
        '安全：重点排查私拉乱接电线与违规大功率电器',
        '访客：校外人员必须实名核验并在 22:00 前离楼',
        '关怀：耐心倾听学生诉求，遇到突发事件第一时间上报'
      ],
      guideLinks: [
        { title: '晚归沟通规范', question: '遇到经常晚归的学生，宿管老师应该如何沟通疏导？', icon: UserCheck },
        { title: '消防安全排查', question: '楼栋日常巡检中，哪些消防隐患和违规物品最需要重点排查？', icon: Wrench }
      ],
      popularTitle: '楼务高频咨询',
      popularQuestions: ['学生钥匙遗失应急开门规范', '宿舍公共设施报修跟进流程', '大件物品搬出楼栋核验要求', '调宿搬迁前房屋验收核对清单'],
      disclaimer: '由通义千问 Qwen2.5 大模型强力驱动 · 辅助日常楼务与应急处理 · 突发危急事件请立即联络校保卫处'
    }
  }

  // 默认 student
  return {
    title: `宿舍智能 AI 助手（${botName.value}）`,
    subtitle: '基于通义千问 Qwen2.5 大语言模型与高校宿舍知识库提供智能解答',
    headerIcon: BookOpen,
    placeholder: `${studentSalutation.value}，问我任何问题，例如：调宿需要准备什么？或者今天吃什么？`,
    emptyTip: `${studentSalutation.value}，请选择常见问题，或直接输入您想了解的内容`,
    prompts: ['宿舍门禁时间', '调宿申请流程', '如何提交报修', '今天晚饭吃什么推荐一下'],
    guideTitle: '智慧锦囊',
    rulesTitle: '核心管理规定',
    rules: [
      '门禁：每日 23:30 关闭大门',
      '用电：严禁使用 800W 以上电器',
      '访客：需在 22:00 前离开楼栋',
      '卫生：每周四下午进行宿舍检查'
    ],
    guideLinks: [
      { title: '报修小贴士', question: '宿舍报修有哪些注意事项？', icon: Wrench },
      { title: '用电安全指南', question: '宿舍安全用电规定有哪些？', icon: ShieldCheck }
    ],
    popularTitle: '热门咨询',
    popularQuestions: ['水电费账单查询', '遥控器遗失处理', '调宿申请资格', '现在时间是多少'],
    disclaimer: '由通义千问 Qwen2.5 大模型强力驱动 · 支持 Markdown 排版 · 紧急事务请直接联系一楼宿管值班室'
  }
})

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
    // 携带最近 6 条历史消息形成多轮对话上下文记忆
    const history = messages.value.slice(-7, -1).map(m => ({
      role: m.role === 'assistant' ? 'assistant' : 'user',
      content: m.content
    }))

    const res = await request({
      url: '/ai/chat',
      method: 'post',
      data: {
        question: content,
        history
      },
      timeout: 40000
    })
    messages.value.push({ id: Date.now() + 1, role: 'assistant', content: res })
  } catch (e) {
    console.error(e)
    messages.value.push({ id: Date.now() + 1, role: 'assistant', content: '连接超时或服务繁忙，请稍后再试。' })
  } finally {
    replying.value = false
    await nextTick()
    scrollToBottom()
  }
}

const scrollToBottom = () => {
  if (messagePanel.value) messagePanel.value.scrollTop = messagePanel.value.scrollHeight
}

onMounted(async () => {
  try {
    const config = await request({ url: '/ai/config', method: 'get' })
    if (config && config.botName) {
      botName.value = config.botName
    }
  } catch (ignored) {}
})
</script>

<style scoped>
.assistant-page { display: grid; grid-template-columns: minmax(0, 1fr) 300px; gap: 20px; max-width: 1280px; margin: 0 auto; }
.chat-card { display: grid; grid-template-rows: auto 1fr auto auto; height: calc(100dvh - 136px); overflow: hidden; border: 1px solid var(--line); border-radius: 8px; background: var(--surface); }
.chat-header { display: flex; align-items: center; gap: 14px; padding: 20px 24px; border-bottom: 1px solid var(--line); }
.assistant-mark { display: grid; width: 42px; height: 42px; place-items: center; border-radius: 8px; background: var(--primary-2); color: var(--primary); }
.chat-header h2 { margin: 0 0 4px; color: var(--text); font-size: 19px; }
.chat-header p, .disclaimer { margin: 0; color: var(--sub); font-size: 13px; }
.message-panel { overflow-y: auto; padding: 24px; background: var(--bg); min-height: 0; }
.empty-state { display: grid; min-height: 100%; place-content: center; justify-items: center; gap: 16px; color: var(--sub); text-align: center; }
.empty-state strong { color: var(--text); }
.prompt-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px; width: min(100%, 480px); }
.prompt-grid button { min-height: 44px; border: 1px solid var(--line); border-radius: 6px; background: var(--surface); color: var(--text); cursor: pointer; transition: all .2s; }
.prompt-grid button:hover { border-color: var(--primary); color: var(--primary); }
.message-list { display: grid; gap: 14px; }
.message { display: flex; }
.message.user { justify-content: flex-end; }
.bubble { max-width: min(85%, 680px); border-radius: 10px; padding: 12px 16px; line-height: 1.65; font-size: 14px; }
.user-bubble { background: var(--primary); color: #fff; border-bottom-right-radius: 2px; }
.assistant-bubble { background: var(--surface); color: var(--text); border: 1px solid var(--line); border-bottom-left-radius: 2px; box-shadow: 0 1px 3px rgba(0,0,0,0.03); }
.bubble.muted { color: var(--sub); background: var(--surface); border: 1px solid var(--line); display: flex; align-items: center; gap: 8px; }

/* Typing animation */
.typing-indicator span:nth-child(1) { animation: bounce 1.4s infinite ease-in-out both; }
.typing-indicator span:nth-child(2) { animation: bounce 1.4s infinite ease-in-out both 0.2s; }
.typing-indicator span:nth-child(3) { animation: bounce 1.4s infinite ease-in-out both 0.4s; }
.typing-text { font-size: 12px; margin-left: 4px; }
@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); opacity: 0.3; }
  40% { transform: scale(1); opacity: 1; }
}

/* Markdown 排版精细化美化 */
.markdown-content :deep(p) { margin: 0 0 10px 0; }
.markdown-content :deep(p:last-child) { margin-bottom: 0; }
.markdown-content :deep(ul), .markdown-content :deep(ol) { margin: 6px 0 10px 0; padding-left: 22px; }
.markdown-content :deep(li) { margin-bottom: 6px; line-height: 1.65; }
.markdown-content :deep(li:last-child) { margin-bottom: 0; }
.markdown-content :deep(strong) { color: var(--primary); font-weight: 600; }
.markdown-content :deep(h1), .markdown-content :deep(h2), .markdown-content :deep(h3), .markdown-content :deep(h4) {
  margin: 12px 0 6px 0; color: var(--text); font-weight: 600;
}
.markdown-content :deep(h3) { font-size: 16px; }
.markdown-content :deep(h4) { font-size: 14px; }
.markdown-content :deep(code) {
  background: var(--bg); padding: 2px 6px; border-radius: 4px; font-family: monospace; font-size: 13px; color: var(--primary);
}
.markdown-content :deep(pre) {
  background: #1e293b; color: #f8fafc; padding: 12px; border-radius: 8px; overflow-x: auto; margin: 8px 0;
}
.markdown-content :deep(pre code) { background: transparent; padding: 0; color: inherit; }
.markdown-content :deep(blockquote) {
  border-left: 4px solid var(--primary); margin: 8px 0; padding-left: 12px; color: var(--sub); background: var(--bg); border-radius: 0 6px 6px 0; padding-top: 4px; padding-bottom: 4px;
}
.markdown-content :deep(hr) { border: 0; border-top: 1px solid var(--line); margin: 12px 0; }

.input-bar { display: flex; gap: 10px; padding: 16px 20px; border-top: 1px solid var(--line); background: var(--surface); }
.input-bar input { flex: 1; min-width: 0; height: 44px; border: 1px solid var(--line); border-radius: 6px; padding: 0 14px; outline: none; color: var(--text); background: var(--bg); }
.input-bar input:focus { border-color: var(--primary); box-shadow: 0 0 0 3px var(--primary-2); }
.input-bar button { display: grid; width: 44px; height: 44px; place-items: center; border: 0; border-radius: 6px; background: var(--primary); color: #fff; cursor: pointer; transition: opacity .2s; }
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
  .chat-card { height: calc(100dvh - 112px); border-radius: 0; }
  .prompt-grid { grid-template-columns: 1fr; }
  .bubble { max-width: 90%; }
}
</style>
