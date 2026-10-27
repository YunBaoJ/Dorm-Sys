<template>
  <div class="ai-settings-page">
    <el-card shadow="never" class="page-header">
      <div>
        <h2>AI 助手系统配置</h2>
        <p>配置全校智能管家的性格人设、回复风格与大语言模型核心参数，实现生动灵动的交互体验</p>
      </div>
      <el-button type="primary" :loading="saving" @click="saveConfig">
        保存并应用配置
      </el-button>
    </el-card>

    <div class="settings-grid">
      <!-- 左侧：参数与人设配置 -->
      <el-card shadow="never" class="form-card" v-loading="loading">
        <template #header>
          <div class="card-title">
            <span class="icon">✨</span>
            <strong>助手人设与运行参数</strong>
          </div>
        </template>

        <el-form :model="form" label-width="120px" label-position="left">
          <el-form-item label="助手名称">
            <el-input v-model="form.botName" placeholder="例如：小寝管家、知心学长" maxlength="20" show-word-limit />
            <div class="field-hint">展示在学生聊天窗口与回答落款中的称呼</div>
          </el-form-item>

          <el-form-item label="性格风格类型">
            <el-select v-model="form.personaType" placeholder="请选择性格风格" style="width: 100%">
              <el-option label="🌟 灵动活泼（推荐 · 高情商/接地气/生动有趣/拒绝呆板）" value="vibrant" />
              <el-option label="🌸 温柔贴心（大姐姐型 · 善解人意/耐心体贴/治愈系）" value="gentle" />
              <el-option label="😄 幽默风趣（搞笑室友风 · 轻松接地气/恰到好处抛梗）" value="humorous" />
              <el-option label="📋 专业严谨（官方智能顾问 · 逻辑缜密/条理分明/规范）" value="formal" />
              <el-option label="🛠️ 自定义风格（根据下方自定义提示词完全自由控制）" value="custom" />
            </el-select>
            <div class="persona-preview-box">
              <div v-if="form.personaType === 'vibrant'" class="persona-desc vibrant">
                <strong>🌟 灵动活泼模式：</strong>
                具备高情商与活泼亲切的学长/学姐气质，适度融入 Emoji 表情与生动语气词，能机智应对生活闲聊、学习娱乐与宿舍常识，回答自然生动绝不呆板！
              </div>
              <div v-else-if="form.personaType === 'gentle'" class="persona-desc gentle">
                <strong>🌸 温柔贴心模式：</strong>
                像一位知心暖心的大姐姐，语调温和、富有同理心，在解答规章与日常解惑时给予充分的关怀与情绪价值。
              </div>
              <div v-else-if="form.personaType === 'humorous'" class="persona-desc humorous">
                <strong>😄 幽默风趣模式：</strong>
                说话幽默风趣、富有校园梗与生活气息，像同宿舍的机智室友，聊天氛围轻松愉快。
              </div>
              <div v-else-if="form.personaType === 'formal'" class="persona-desc formal">
                <strong>📋 专业严谨模式：</strong>
                用词规范标准、分点列明、条理清晰，严格传达后勤各项规章制度。
              </div>
              <div v-else class="persona-desc custom">
                <strong>🛠️ 自定义模式：</strong>
                由您在下方的“管理员特别补充设定”中自由编写 system prompt 提示词规则。
              </div>
            </div>
          </el-form-item>

          <el-form-item label="驱动大模型">
            <el-select v-model="form.model" filterable allow-create default-first-option style="width: 100%">
              <el-option label="Qwen/Qwen2.5-7B-Instruct（通义千问 7B · 毫秒级极速响应/推荐）" value="Qwen/Qwen2.5-7B-Instruct" />
              <el-option label="Qwen/Qwen2.5-14B-Instruct（通义千问 14B · 推理能力更强）" value="Qwen/Qwen2.5-14B-Instruct" />
              <el-option label="deepseek-ai/DeepSeek-V3（深度求索通用大模型）" value="deepseek-ai/DeepSeek-V3" />
            </el-select>
            <div class="field-hint">支持 SiliconFlow 平台已接入的各款开源大模型</div>
          </el-form-item>

          <el-form-item label="灵动度 / 创造力">
            <div class="slider-wrapper">
              <el-slider v-model="form.temperature" :min="0.1" :max="1.0" :step="0.05" show-input />
            </div>
            <div class="field-hint">
              Temperature 参数：推荐 0.8 ~ 0.9。数值越高 AI 越灵动多变、富有创造力；数值越低回答越固定死板。
            </div>
          </el-form-item>

          <el-form-item label="补充特别设定">
            <el-input
              v-model="form.customPrompt"
              type="textarea"
              :rows="4"
              placeholder="例如：可补充学校特有信息，如“本周校迎新晚会在大礼堂举行”、“食堂三楼新开了兰州拉面窗口”、“近期降温提醒同学们多穿衣”等..."
            />
            <div class="field-hint">此设定将被无缝注入 AI 认知中，影响全校同学提问时的回答细节</div>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 右侧：实时效果预览沙盒 -->
      <el-card shadow="never" class="preview-card">
        <template #header>
          <div class="card-title">
            <span class="icon">💬</span>
            <strong>即时效果体验沙盒</strong>
            <el-tag size="small" type="success" effect="plain">即改即试</el-tag>
          </div>
        </template>

        <div class="sandbox-chat">
          <div ref="sandboxMessages" class="sandbox-messages">
            <div class="bubble assistant">
              👋 管理员老师您好！我是当前配置的 <strong>「{{ form.botName || 'AI助手' }}」</strong>。<br>
              您可以在左侧调整我的性格人设或灵动度，点击上方保存后，直接在下方输入问题测试我的回复表现！
            </div>
            <div 
              v-for="(msg, idx) in testMessages" 
              :key="idx" 
              class="bubble" 
              :class="[msg.role, msg.role === 'assistant' ? 'markdown-content' : '']"
            >
              <div v-if="msg.role === 'assistant'" v-html="renderMarkdown(msg.content)"></div>
              <div v-else>{{ msg.content }}</div>
            </div>
            <div v-if="testing" class="bubble assistant thinking">
              <span>●</span><span>●</span><span>●</span> 正在思考回复中...
            </div>
          </div>

          <div class="sandbox-input-area">
            <div class="quick-tags">
              <button type="button" @click="testAsk('你是谁呀？平时能帮我做些什么？')">问身份</button>
              <button type="button" @click="testAsk('你知道我是谁吗？我在哪个宿舍？')">测试身份感知</button>
              <button type="button" @click="testAsk('今天天气怎么样？')">测试多轮对话</button>
              <button type="button" @click="testAsk('推荐几个清淡低脂的晚饭！')">测试灵动建议</button>
            </div>
            <div class="input-row">
              <el-input
                v-model="testInput"
                placeholder="输入测试问题，按 Enter 发送..."
                @keyup.enter="sendTest"
                :disabled="testing"
              />
              <el-button type="primary" :disabled="!testInput.trim() || testing" @click="sendTest">
                发送
              </el-button>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'
import { marked } from 'marked'

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

const loading = ref(false)
const saving = ref(false)
const testing = ref(false)

const form = ref({
  id: 1,
  botName: '小寝管家',
  personaType: 'vibrant',
  customPrompt: '',
  temperature: 0.85,
  model: 'Qwen/Qwen2.5-7B-Instruct'
})

const testInput = ref('')
const testMessages = ref([])
const sandboxMessages = ref(null)

const scrollToBottom = () => {
  if (sandboxMessages.value) {
    sandboxMessages.value.scrollTop = sandboxMessages.value.scrollHeight
  }
}

const loadConfig = async () => {
  loading.value = true
  try {
    const res = await request({
      url: '/ai/config',
      method: 'get'
    })
    if (res) {
      form.value = {
        id: res.id || 1,
        botName: res.botName || '小寝管家',
        personaType: res.personaType || 'vibrant',
        customPrompt: res.customPrompt || '',
        temperature: res.temperature != null ? res.temperature : 0.85,
        model: res.model || 'Qwen/Qwen2.5-7B-Instruct'
      }
    }
  } catch (e) {
    console.error('加载 AI 配置失败:', e)
  } finally {
    loading.value = false
  }
}

const saveConfig = async () => {
  saving.value = true
  try {
    await request({
      url: '/ai/config/save',
      method: 'post',
      data: form.value
    })
    ElMessage.success('AI 助手参数配置已更新，全校学生即刻生效！')
  } catch (e) {
    console.error('保存失败:', e)
    ElMessage.error('保存失败，请检查网络或后端日志')
  } finally {
    saving.value = false
  }
}

const testAsk = (text) => {
  testInput.value = text
  sendTest()
}

const sendTest = async () => {
  const content = testInput.value.trim()
  if (!content || testing.value) return

  testMessages.value.push({ role: 'user', content })
  testInput.value = ''
  testing.value = true
  await nextTick()
  scrollToBottom()

  try {
    const history = testMessages.value.slice(-6).map(m => ({
      role: m.role === 'assistant' ? 'assistant' : 'user',
      content: m.content
    }))

    const answer = await request({
      url: '/ai/chat',
      method: 'post',
      data: {
        question: content,
        history
      },
      timeout: 40000
    })

    testMessages.value.push({ role: 'assistant', content: answer })
  } catch (e) {
    console.error(e)
    testMessages.value.push({ role: 'assistant', content: '测试对话遇到异常或超时，请确保已保存最新配置。' })
  } finally {
    testing.value = false
    await nextTick()
    scrollToBottom()
  }
}

onMounted(() => {
  loadConfig()
})
</script>

<style scoped>
.ai-settings-page {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header :deep(.el-card__body) {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-header h2 {
  margin: 0 0 6px;
  font-size: 20px;
  color: var(--text);
}

.page-header p {
  margin: 0;
  color: var(--sub);
  font-size: 14px;
}

.settings-grid {
  display: grid;
  grid-template-columns: 1.15fr 0.85fr;
  gap: 20px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  color: var(--text);
}

.card-title .icon {
  font-size: 18px;
}

.field-hint {
  font-size: 12px;
  color: var(--sub);
  margin-top: 5px;
  line-height: 1.5;
}

.persona-preview-box {
  margin-top: 10px;
  width: 100%;
}

.persona-desc {
  padding: 10px 14px;
  border-radius: 6px;
  font-size: 13px;
  line-height: 1.6;
}

.persona-desc.vibrant {
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  color: #166534;
}

.persona-desc.gentle {
  background: #fdf2f8;
  border: 1px solid #fbcfe8;
  color: #9d174d;
}

.persona-desc.humorous {
  background: #fefce8;
  border: 1px solid #fef08a;
  color: #854d0e;
}

.persona-desc.formal {
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  color: #1e40af;
}

.persona-desc.custom {
  background: #f3f4f6;
  border: 1px solid #e5e7eb;
  color: #374151;
}

.slider-wrapper {
  width: 100%;
  padding-right: 14px;
}

/* 右侧沙盒样式 */
.preview-card :deep(.el-card__body) {
  height: calc(100% - 60px);
  padding: 16px;
}

.sandbox-chat {
  display: flex;
  flex-direction: column;
  height: 600px;
}

.sandbox-messages {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 12px;
  background: var(--bg);
  border: 1px solid var(--line);
  border-radius: 8px;
}

.bubble {
  max-width: 88%;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 13.5px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.bubble.assistant {
  align-self: flex-start;
  background: var(--surface);
  color: var(--text);
  border: 1px solid var(--line);
  border-bottom-left-radius: 2px;
}

.bubble.markdown-content {
  white-space: normal;
}

/* Markdown 排版精细化美化 */
.markdown-content :deep(p) { margin: 0 0 8px 0; }
.markdown-content :deep(p:last-child) { margin-bottom: 0; }
.markdown-content :deep(ul), .markdown-content :deep(ol) { margin: 4px 0 8px 0; padding-left: 20px; }
.markdown-content :deep(li) { margin-bottom: 4px; line-height: 1.6; }
.markdown-content :deep(li:last-child) { margin-bottom: 0; }
.markdown-content :deep(strong) { color: var(--primary); font-weight: 600; }
.markdown-content :deep(h1), .markdown-content :deep(h2), .markdown-content :deep(h3), .markdown-content :deep(h4) {
  margin: 10px 0 4px 0; color: var(--text); font-weight: 600;
}
.markdown-content :deep(h3) { font-size: 15px; }
.markdown-content :deep(h4) { font-size: 13.5px; }
.markdown-content :deep(code) {
  background: var(--bg); padding: 2px 6px; border-radius: 4px; font-family: monospace; font-size: 12.5px; color: var(--primary);
}
.markdown-content :deep(pre) {
  background: #1e293b; color: #f8fafc; padding: 10px; border-radius: 6px; overflow-x: auto; margin: 6px 0;
}
.markdown-content :deep(pre code) { background: transparent; padding: 0; color: inherit; }
.markdown-content :deep(blockquote) {
  border-left: 3px solid var(--primary); margin: 6px 0; padding-left: 10px; color: var(--sub); background: var(--bg); border-radius: 0 4px 4px 0; padding-top: 3px; padding-bottom: 3px;
}
.markdown-content :deep(hr) { border: 0; border-top: 1px solid var(--line); margin: 10px 0; }

.bubble.user {
  align-self: flex-end;
  background: var(--primary);
  color: #fff;
  border-bottom-right-radius: 2px;
}

.bubble.thinking {
  color: var(--sub);
  font-size: 12px;
}

.sandbox-input-area {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.quick-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.quick-tags button {
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 4px;
  border: 1px solid var(--line);
  background: var(--surface);
  color: var(--sub);
  cursor: pointer;
  transition: all .2s;
}

.quick-tags button:hover {
  border-color: var(--primary);
  color: var(--primary);
}

.input-row {
  display: flex;
  gap: 8px;
}

@media (max-width: 1100px) {
  .settings-grid {
    grid-template-columns: 1fr;
  }
  .sandbox-chat {
    height: 450px;
  }
}
</style>
