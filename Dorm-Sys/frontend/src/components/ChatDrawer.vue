<template>
  <el-dialog
    v-model="visible"
    width="min(980px, calc(100vw - 64px))"
    align-center
    append-to-body
    :destroy-on-close="true"
    :show-close="false"
    :close-on-click-modal="true"
    @close="onClose"
    class="chat-dialog"
    modal-class="chat-dialog-overlay"
  >
    <div class="chat-wrapper">
      <aside class="chat-sidebar">
        <div class="chat-brand">
          <span class="brand-mark"><MessageCircle :size="19" aria-hidden="true" /></span>
          <span>宿舍通讯</span>
        </div>

        <div class="sidebar-profile">
          <div class="header-avatar">
            <span v-if="mode === 'private'">{{ activeTarget?.name?.[0] || '?' }}</span>
            <Users v-else :size="22" aria-hidden="true" />
          </div>
          <div class="header-info">
            <div class="header-title">{{ drawerTitle }}</div>
            <div class="header-status">
              <span class="status-dot"></span>
              {{ mode === 'private' ? '在线' : `${roomMemberCount}人在此宿舍` }}
            </div>
          </div>
        </div>

        <nav class="chat-tabs" aria-label="聊天会话">
          <button type="button" class="tab" :class="{ active: mode === 'group' }" @click="switchMode('group')">
            <Users :size="18" aria-hidden="true" /> 宿舍群聊
          </button>
          <div class="conversation-section">
            <div class="conversation-section-title">最近私聊</div>
            <button
              v-for="conversation in displayConversations"
              :key="conversation.id"
              type="button"
              class="conversation-item"
              :class="{ active: mode === 'private' && activeTarget?.id == conversation.id }"
              @click="openConversation(conversation)"
            >
              <span class="conversation-avatar">{{ conversation.name?.[0] || '?' }}</span>
              <span class="conversation-copy">
                <strong>{{ conversation.name }}</strong>
                <small>{{ conversation.lastMessage || '开始聊天' }}</small>
              </span>
            </button>
            <div v-if="displayConversations.length === 0" class="conversation-empty">暂无私聊</div>
          </div>
        </nav>
      </aside>

      <section class="chat-main">
        <header class="chat-header">
          <div>
            <div class="conversation-label">当前会话</div>
            <h2>{{ drawerTitle }}</h2>
          </div>
          <button type="button" class="close-btn" aria-label="关闭聊天" @click="visible = false">
            <X :size="20" aria-hidden="true" />
          </button>
        </header>

        <div class="messages-area" ref="messagesRef">
        <div v-if="messages.length === 0" class="empty-state">
          <div class="empty-icon"><MessageCircle :size="28" aria-hidden="true" /></div>
          <div class="empty-title">暂无消息</div>
          <div class="empty-desc">发一条消息打个招呼吧</div>
        </div>

        <template v-for="(msg, idx) in messages" :key="msg.id">
          <!-- Date separator -->
          <div v-if="shouldShowDate(idx)" class="date-separator">
            <span>{{ formatDate(msg.createTime) }}</span>
          </div>

          <div class="msg-row" :class="{ 'is-self': msg.senderId == myId }">
            <div class="msg-avatar">
              <div class="avatar-circle" :class="{ 'self-avatar': msg.senderId == myId }">
                {{ msg.senderName?.[0] || '?' }}
              </div>
            </div>
            <div class="msg-body">
              <div class="msg-meta">
                <span class="msg-name">{{ msg.senderId == myId ? '我' : (msg.senderName || '未知') }}</span>
                <span class="msg-time">{{ formatTime(msg.createTime) }}</span>
              </div>
              <div class="msg-bubble">{{ msg.content }}</div>
            </div>
          </div>
        </template>
        </div>

        <div class="input-area">
        <div class="input-row">
          <el-input
            v-model="inputText"
            placeholder="输入消息..."
            aria-label="聊天消息"
            type="textarea"
            :autosize="{ minRows: 1, maxRows: 4 }"
            resize="none"
            @keydown.enter.exact.prevent="sendMessage"
            :disabled="sending"
            class="chat-input"
          />
          <button
            type="button"
            class="send-btn"
            :class="{ 'can-send': inputText.trim() }"
            @click="sendMessage"
            :disabled="!inputText.trim() || sending"
            aria-label="发送消息"
          >
            <Send :size="20" aria-hidden="true" />
          </button>
        </div>
        </div>
      </section>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch, nextTick, onUnmounted } from 'vue'
import { MessageCircle, Send, Users, X } from '@lucide/vue'
import request from '../utils/request'

const props = defineProps({
  modelValue: Boolean,
  targetUser: { type: Object, default: null },
  roomId: { type: [Number, String], default: null },
  myId: { type: [Number, String], default: null },
  roomMemberCount: { type: Number, default: 4 }
})

const emit = defineEmits(['update:modelValue'])

const visible = ref(props.modelValue)
const mode = ref(props.targetUser ? 'private' : 'group')
const activeTarget = ref(props.targetUser)
const conversations = ref([])
const messages = ref([])
const inputText = ref('')
const sending = ref(false)
const messagesRef = ref(null)
let pollTimer = null

const drawerTitle = ref('')
const displayConversations = computed(() => {
  if (!activeTarget.value) return conversations.value
  const exists = conversations.value.some(item => item.id == activeTarget.value.id)
  return exists ? conversations.value : [activeTarget.value, ...conversations.value]
})

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    activeTarget.value = props.targetUser
    mode.value = props.targetUser ? 'private' : 'group'
    updateTitle()
    fetchConversations()
    fetchMessages()
    startPolling()
  } else {
    stopPolling()
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

function updateTitle() {
  if (mode.value === 'private' && activeTarget.value) {
    drawerTitle.value = activeTarget.value.name
  } else {
    drawerTitle.value = '宿舍群聊'
  }
}

function switchMode(m) {
  mode.value = m
  updateTitle()
  fetchMessages()
}

function openConversation(conversation) {
  activeTarget.value = { id: conversation.id, name: conversation.name, avatar: conversation.avatar }
  mode.value = 'private'
  updateTitle()
  fetchMessages()
}

async function fetchConversations() {
  try {
    const res = await request({ url: '/chat/conversations', method: 'get' })
    conversations.value = res || []
  } catch (e) {
    console.error('Failed to fetch conversations', e)
  }
}

async function fetchMessages() {
  try {
    let url = ''
    if (mode.value === 'private' && activeTarget.value) {
      url = `/chat/private/${activeTarget.value.id}`
    } else if (props.roomId) {
      url = `/chat/group/${props.roomId}`
    } else {
      return
    }
    const res = await request({ url, method: 'get' })
    messages.value = res || []
    await nextTick()
    scrollToBottom()
  } catch (e) {
    console.error('Failed to fetch messages', e)
  }
}

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text) return
  sending.value = true
  try {
    const payload = {
      content: text,
      type: mode.value === 'private' ? 'PRIVATE' : 'GROUP',
      receiverId: mode.value === 'private' ? activeTarget.value?.id : null,
      roomId: props.roomId
    }
    await request({ url: '/chat/send', method: 'post', data: payload })
    inputText.value = ''
    await fetchMessages()
    await fetchConversations()
  } catch (e) {
    console.error('Failed to send message', e)
  } finally {
    sending.value = false
  }
}

function scrollToBottom() {
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

function startPolling() {
  stopPolling()
  pollTimer = setInterval(() => {
    fetchMessages()
    fetchConversations()
  }, 3000)
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

function onClose() {
  stopPolling()
}

function shouldShowDate(idx) {
  if (idx === 0) return true
  const curr = new Date(messages.value[idx].createTime)
  const prev = new Date(messages.value[idx - 1].createTime)
  return curr.toDateString() !== prev.toDateString()
}

function formatDate(t) {
  if (!t) return ''
  const d = new Date(t)
  const today = new Date()
  if (d.toDateString() === today.toDateString()) return '今天'
  const yesterday = new Date(today)
  yesterday.setDate(today.getDate() - 1)
  if (d.toDateString() === yesterday.toDateString()) return '昨天'
  return `${d.getMonth() + 1}月${d.getDate()}日`
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  const hh = String(d.getHours()).padStart(2, '0')
  const mm = String(d.getMinutes()).padStart(2, '0')
  return `${hh}:${mm}`
}

onUnmounted(() => stopPolling())
</script>

<style scoped>
.chat-wrapper {
  display: flex;
  height: min(590px, calc(100dvh - 64px));
  min-height: 0;
  background: rgba(241, 247, 255, 0.62);
  backdrop-filter: blur(34px) saturate(138%);
  -webkit-backdrop-filter: blur(34px) saturate(138%);
}

:global(.chat-dialog.el-dialog) {
  margin: 0;
  padding: 0;
  overflow: hidden;
  border: 1px solid rgba(125, 155, 201, 0.34);
  border-radius: 14px;
  background: rgba(232, 241, 253, 0.4);
  box-shadow: 0 26px 72px rgba(15, 23, 42, 0.18), inset 0 1px 0 rgba(255, 255, 255, 0.42);
  backdrop-filter: blur(34px) saturate(150%);
  -webkit-backdrop-filter: blur(34px) saturate(150%);
}

:global(.chat-dialog .el-dialog__header) {
  display: none;
}

:global(.chat-dialog .el-dialog__body) {
  padding: 0;
  overflow: hidden;
}

:global(.chat-dialog-overlay) {
  background: rgba(30, 41, 59, 0.2);
  backdrop-filter: blur(5px) saturate(108%);
  -webkit-backdrop-filter: blur(5px) saturate(108%);
}

:global(.chat-dialog-overlay .el-overlay-dialog) {
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  padding: 32px;
  overflow: hidden;
}

.chat-sidebar {
  display: flex;
  flex-direction: column;
  width: 218px;
  flex: 0 0 218px;
  padding: 20px 16px;
  color: #203552;
  background: rgba(218, 232, 252, 0.46);
  border-right: 1px solid rgba(133, 157, 194, 0.28);
  backdrop-filter: blur(26px) saturate(145%);
  -webkit-backdrop-filter: blur(26px) saturate(145%);
  box-shadow: inset -1px 0 rgba(255, 255, 255, 0.34);
}

.chat-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 6px 20px;
  font-size: 14px;
  font-weight: 700;
}

.brand-mark {
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  border: 1px solid rgba(94, 134, 195, 0.24);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.36);
  color: #2f6fed;
  box-shadow: inset 0 1px rgba(255, 255, 255, 0.46);
}

.sidebar-profile {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 12px;
  border: 1px solid rgba(109, 143, 194, 0.24);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.3);
  box-shadow: inset 0 1px rgba(255, 255, 255, 0.42);
}

.chat-main {
  display: flex;
  flex: 1;
  min-width: 0;
  min-height: 0;
  flex-direction: column;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  min-height: 72px;
  padding: 14px 22px 12px 26px;
  color: #172033;
  background: rgba(255, 255, 255, 0.38);
  border-bottom: 1px solid rgba(190, 207, 232, 0.46);
  backdrop-filter: blur(22px);
  -webkit-backdrop-filter: blur(22px);
}

.header-avatar {
  width: 42px;
  height: 42px;
  border-radius: 8px;
  background: rgba(47, 111, 237, 0.12);
  border: 1px solid rgba(47, 111, 237, 0.16);
  color: #245bc4;
  backdrop-filter: blur(8px);
  display: grid;
  place-items: center;
  font-size: 20px;
  font-weight: 700;
}

.header-title {
  font-size: 17px;
  font-weight: 700;
  letter-spacing: 0;
}

.conversation-label {
  margin-bottom: 3px;
  color: #74829a;
  font-size: 11px;
  font-weight: 600;
}

.chat-header h2 {
  margin: 0;
  color: #172033;
  font-size: 18px;
  line-height: 1.2;
}

.header-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #5f7089;
  margin-top: 2px;
}

.status-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #4ade80;
  box-shadow: 0 0 6px rgba(74, 222, 128, 0.6);
}

.close-btn {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  border: 1px solid rgba(151, 171, 203, 0.28);
  background: rgba(255, 255, 255, 0.48);
  color: #52627a;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.2s;
  display: grid;
  place-items: center;
}

.close-btn:hover {
  color: #1d4ed8;
  border-color: #b8caf0;
  background: rgba(255, 255, 255, 0.78);
}

.close-btn:focus-visible,
.tab:focus-visible,
.send-btn:focus-visible {
  outline: 3px solid rgba(47, 111, 237, 0.28);
  outline-offset: 2px;
}

/* ---- Tabs ---- */
.chat-tabs {
  display: grid;
  align-content: start;
  min-height: 0;
  gap: 6px;
  margin-top: 18px;
  overflow-y: auto;
}

.tab {
  min-height: 44px;
  border: 0;
  width: 100%;
  padding: 0 12px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  color: #60738f;
  border-radius: 8px;
  transition: all 0.25s ease;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 6px;
}

.tab.active {
  color: #245bc4;
  background: rgba(47, 111, 237, 0.12);
  box-shadow: inset 0 0 0 1px rgba(47, 111, 237, 0.14), 0 8px 20px rgba(43, 70, 110, 0.06);
}

.tab:not(.active):hover {
  color: #245bc4;
  background: rgba(255, 255, 255, 0.3);
}

.conversation-section {
  min-height: 0;
  margin-top: 12px;
}

.conversation-section-title {
  padding: 0 10px 8px;
  color: #71829a;
  font-size: 11px;
  font-weight: 600;
}

.conversation-item {
  width: 100%;
  min-height: 54px;
  padding: 7px 9px;
  display: flex;
  align-items: center;
  gap: 9px;
  border: 1px solid transparent;
  border-radius: 8px;
  background: transparent;
  color: #203552;
  cursor: pointer;
  text-align: left;
}

.conversation-item:hover {
  background: rgba(255, 255, 255, 0.3);
}

.conversation-item.active {
  border-color: rgba(47, 111, 237, 0.14);
  background: rgba(47, 111, 237, 0.12);
}

.conversation-avatar {
  width: 34px;
  height: 34px;
  flex: 0 0 34px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.46);
  color: #245bc4;
  font-weight: 700;
}

.conversation-copy {
  min-width: 0;
  display: grid;
  gap: 2px;
}

.conversation-copy strong,
.conversation-copy small {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conversation-copy strong { font-size: 13px; }
.conversation-copy small { color: #71829a; font-size: 11px; }
.conversation-empty { padding: 12px 10px; color: #8391a6; font-size: 12px; }

/* ---- Messages ---- */
.messages-area {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 26px 30px 18px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  scroll-behavior: smooth;
  background: rgba(246, 249, 255, 0.28);
}

.messages-area::-webkit-scrollbar {
  width: 4px;
}
.messages-area::-webkit-scrollbar-thumb {
  background: rgba(100, 116, 139, 0.28);
  border-radius: 4px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  gap: 8px;
}

.empty-icon {
  width: 52px;
  height: 52px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: #eaf1ff;
  color: #2563eb;
}

.empty-title {
  font-size: 16px;
  font-weight: 600;
  color: #64748b;
}

.empty-desc {
  font-size: 13px;
  color: #94a3b8;
}

/* Date separator */
.date-separator {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 8px 0;
}

.date-separator span {
  background: rgba(226, 232, 240, 0.62);
  color: #64748b;
  font-size: 11px;
  font-weight: 500;
  padding: 3px 12px;
  border-radius: 10px;
}

/* Message rows */
.msg-row {
  display: flex;
  gap: 10px;
  align-items: flex-end;
  animation: msgFadeIn 0.3s ease;
}

@keyframes msgFadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.msg-row.is-self {
  flex-direction: row-reverse;
}

.avatar-circle {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: rgba(226, 232, 240, 0.72);
  border: 1px solid rgba(255, 255, 255, 0.58);
  color: #475569;
  display: grid;
  place-items: center;
  font-weight: 700;
  font-size: 14px;
  flex-shrink: 0;
}

.avatar-circle.self-avatar {
  background: rgba(37, 99, 235, 0.88);
  color: #fff;
}

.msg-body {
  max-width: min(62%, 560px);
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.msg-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 11px;
  padding: 0 4px;
}

.msg-row.is-self .msg-meta {
  flex-direction: row-reverse;
}

.msg-name {
  font-weight: 600;
  color: #64748b;
}

.msg-time {
  color: #94a3b8;
}

.msg-bubble {
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  background: rgba(255, 255, 255, 0.7);
  color: #1e293b;
  word-break: break-word;
  box-shadow: 0 6px 18px rgba(43, 70, 110, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.62);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
}

.msg-row.is-self .msg-bubble {
  background: rgba(37, 99, 235, 0.9);
  color: #fff;
  border: none;
  border-bottom-right-radius: 4px;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.2);
}

.msg-row:not(.is-self) .msg-bubble {
  border-bottom-left-radius: 4px;
}

/* ---- Input Area ---- */
.input-area {
  padding: 10px 24px 22px;
  background: transparent;
  border-top: 0;
}

.input-row {
  display: flex;
  max-width: 760px;
  margin: 0 auto;
  gap: 10px;
  align-items: flex-end;
  padding: 8px;
  border: 1px solid rgba(133, 157, 194, 0.3);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.5);
  box-shadow: 0 12px 30px rgba(43, 70, 110, 0.08), inset 0 1px rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(20px) saturate(135%);
  -webkit-backdrop-filter: blur(20px) saturate(135%);
}

.input-row:focus-within {
  border-color: rgba(47, 111, 237, 0.58);
  box-shadow: 0 12px 30px rgba(43, 70, 110, 0.1), 0 0 0 3px rgba(47, 111, 237, 0.12);
}

.chat-input :deep(.el-textarea__inner) {
  min-height: 44px !important;
  border-radius: 6px;
  padding: 11px 10px;
  color: #172033;
  background: transparent;
  box-shadow: none !important;
  transition: background-color 0.2s, box-shadow 0.2s;
}

.chat-input :deep(.el-textarea__inner:focus) {
  background: rgba(255, 255, 255, 0.34);
  box-shadow: none !important;
}

.send-btn {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  border: none;
  border: 1px solid rgba(151, 171, 203, 0.24);
  background: rgba(226, 232, 240, 0.58);
  color: #94a3b8;
  cursor: not-allowed;
  display: grid;
  place-items: center;
  transition: all 0.25s ease;
  flex-shrink: 0;
}

.send-btn.can-send {
  border-color: rgba(37, 99, 235, 0.72);
  background: rgba(37, 99, 235, 0.9);
  color: #fff;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
}

.send-btn.can-send:hover {
  background: #1d4ed8;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
}

.send-btn:focus-visible {
  outline-color: rgba(37, 99, 235, 0.28);
}

@media (max-width: 720px) {
  :global(.chat-dialog.el-dialog) { width: calc(100vw - 32px) !important; }
  .chat-wrapper {
    height: calc(100dvh - 32px);
    flex-direction: column;
  }
  .chat-sidebar {
    width: auto;
    flex: 0 0 auto;
    padding: 12px 14px;
  }
  .chat-brand { display: none; }
  .sidebar-profile { padding: 8px 10px; }
  .chat-tabs {
    display: flex;
    margin-top: 10px;
  }
  .tab { justify-content: center; }
  .chat-header { min-height: 64px; }
  .messages-area { padding-inline: 18px; }
  .input-area { padding-inline: 16px; }
}

@media (max-width: 520px) {
  :global(.chat-dialog-overlay .el-overlay-dialog) { padding: 16px; }
  :global(.chat-dialog.el-dialog) { border-radius: 12px; }
  .messages-area { padding-inline: 14px; }
  .msg-body { max-width: 78%; }
}

@media (prefers-reduced-motion: reduce) {
  .msg-row { animation: none; }
  .messages-area { scroll-behavior: auto; }
}
</style>
