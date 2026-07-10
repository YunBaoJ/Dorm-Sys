<template>
  <div class="ai-container">
    <el-row :gutter="24" class="full-height">
      <!-- Left Column: Chat Window -->
      <el-col :span="18" class="full-height">
        <el-card shadow="never" class="chat-card">
          <!-- Chat Header -->
          <div class="chat-header">
            <div class="header-text">
              <el-icon :size="28" color="var(--el-color-primary)"><component :is="MessageCircle" /></el-icon>
              <div>
                <h2>智慧助手</h2>
                <p>24/7 全天候宿舍管家</p>
              </div>
            </div>
            <el-button type="primary"><el-icon class="el-icon--left"><component :is="Plus" /></el-icon>新建对话</el-button>
          </div>

          <!-- Chat Area (Empty State) -->
          <div class="chat-area">
            <div class="empty-state">
              <div class="ai-avatar">
                <el-icon :size="40" color="white"><component :is="Lightbulb" /></el-icon>
              </div>
              <h3 class="welcome-text">您好，我是您的智慧宿舍助手</h3>
              <p class="welcome-sub">您可以询问我任何关于宿舍生活、规定、费用或报修的问题</p>
              
              <div class="quick-actions">
                <div class="quick-btn">
                  <el-icon color="#3b82f6"><component :is="Clock" /></el-icon>
                  <span>门禁查询</span>
                </div>
                <div class="quick-btn">
                  <el-icon color="#3b82f6"><component :is="Repeat" /></el-icon>
                  <span>调宿流程</span>
                </div>
                <div class="quick-btn">
                  <el-icon color="#3b82f6"><component :is="Settings" /></el-icon>
                  <span>报修指引</span>
                </div>
                <div class="quick-btn">
                  <el-icon color="#3b82f6"><component :is="Wallet" /></el-icon>
                  <span>缴费咨询</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Chat Input -->
          <div class="chat-input-wrapper">
            <div class="input-box">
              <input type="text" placeholder="询问我关于宿舍的任何问题..." class="chat-input" />
              <button class="send-btn">
                <el-icon :size="20"><component :is="Send" /></el-icon>
              </button>
            </div>
            <div class="input-footer">由智慧宿舍大模型提供技术支持</div>
          </div>
        </el-card>
      </el-col>

      <!-- Right Column: Sidebar -->
      <el-col :span="6">
        <!-- AI Tips Accordion -->
        <el-card shadow="never" class="side-card">
          <template #header>
            <div class="card-header">
              <span>智慧锦囊</span>
              <el-icon color="#3b82f6"><component :is="BookOpen" /></el-icon>
            </div>
          </template>
          <div class="accordion">
            <div class="acc-item active">
              <div class="acc-header">
                <div class="acc-title">
                  <el-icon color="#94a3b8"><component :is="AlertCircle" /></el-icon>
                  <span>核心管理规定</span>
                </div>
                <el-icon><component :is="ChevronDown" /></el-icon>
              </div>
              <div class="acc-content">
                <ul>
                  <li><strong>门禁:</strong> 每日 23:30 关闭大门</li>
                  <li><strong>用电:</strong> 严禁使用 800W 以上电器</li>
                  <li><strong>访客:</strong> 需在 22:00 前离开楼栋</li>
                  <li><strong>卫生:</strong> 每周四下午进行宿舍检查</li>
                </ul>
              </div>
            </div>
            <div class="acc-item">
              <div class="acc-header">
                <div class="acc-title">
                  <el-icon color="#94a3b8"><component :is="Settings" /></el-icon>
                  <span>报修小贴士</span>
                </div>
                <el-icon><component :is="ChevronRight" /></el-icon>
              </div>
            </div>
            <div class="acc-item">
              <div class="acc-header">
                <div class="acc-title">
                  <el-icon color="#94a3b8"><component :is="Zap" /></el-icon>
                  <span>用电安全指南</span>
                </div>
                <el-icon><component :is="ChevronRight" /></el-icon>
              </div>
            </div>
          </div>
        </el-card>

        <!-- Hot Questions -->
        <el-card shadow="never" class="side-card">
          <template #header>
            <div class="card-header"><span>热门咨询</span></div>
          </template>
          <div class="hot-list">
            <div class="hot-item highlighted">
              <span>水电费账单查询</span>
              <el-icon><component :is="ChevronRight" /></el-icon>
            </div>
            <div class="hot-item">
              <span>遥控器遗失处理</span>
              <el-icon><component :is="ChevronRight" /></el-icon>
            </div>
            <div class="hot-item">
              <span>调宿申请资格</span>
              <el-icon><component :is="ChevronRight" /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { MessageCircle, Plus, Lightbulb, Clock, Repeat, Settings, Wallet, Send, BookOpen, AlertCircle, ChevronDown, ChevronRight, Zap } from '@lucide/vue'
</script>

<style scoped>
.ai-container {
  max-width: 1400px;
  margin: 0 auto;
  height: calc(100vh - 120px);
}

.full-height {
  height: 100%;
}

.chat-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  border-radius: 12px;
}

.chat-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 0;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  border-bottom: 1px solid #f1f5f9;
}

.header-text {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-text h2 {
  margin: 0 0 4px 0;
  font-size: 20px;
  color: #1f2937;
}

.header-text p {
  margin: 0;
  font-size: 14px;
  color: #64748b;
}

.chat-area {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f8fafc;
  padding: 40px;
}

.empty-state {
  text-align: center;
  max-width: 600px;
}

.ai-avatar {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
  border-radius: 24px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0 auto 24px;
  box-shadow: 0 10px 25px -5px rgba(59, 130, 246, 0.4);
}

.welcome-text {
  font-size: 24px;
  color: #1f2937;
  margin: 0 0 12px 0;
}

.welcome-sub {
  color: #64748b;
  font-size: 15px;
  margin: 0 0 40px 0;
}

.quick-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.quick-btn {
  background: white;
  border: 1px solid #e2e8f0;
  padding: 16px 24px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.2s;
  color: #334155;
  font-weight: 500;
}

.quick-btn:hover {
  border-color: #3b82f6;
  background-color: #eff6ff;
  color: #3b82f6;
}

.chat-input-wrapper {
  padding: 24px;
  background: white;
  border-top: 1px solid #f1f5f9;
}

.input-box {
  display: flex;
  align-items: center;
  background: #f1f5f9;
  border-radius: 24px;
  padding: 8px 8px 8px 24px;
  margin-bottom: 12px;
}

.chat-input {
  flex: 1;
  border: none;
  background: transparent;
  outline: none;
  font-size: 15px;
  color: #1f2937;
}

.chat-input::placeholder {
  color: #94a3b8;
}

.send-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: #3b82f6;
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: background 0.2s;
}

.send-btn:hover {
  background: #2563eb;
}

.input-footer {
  text-align: center;
  font-size: 12px;
  color: #94a3b8;
}

.side-card {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.accordion {
  display: flex;
  flex-direction: column;
}

.acc-item {
  border-bottom: 1px solid #f1f5f9;
}

.acc-item:last-child {
  border-bottom: none;
}

.acc-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  cursor: pointer;
  color: #334155;
}

.acc-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  font-weight: 500;
}

.acc-content {
  padding-bottom: 16px;
  padding-left: 32px;
}

.acc-content ul {
  margin: 0;
  padding: 0;
  list-style: none;
  font-size: 13px;
  color: #64748b;
  line-height: 2;
}

.acc-content ul li::before {
  content: "•";
  color: #cbd5e1;
  display: inline-block;
  width: 1em;
  margin-left: -1em;
}

.hot-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.hot-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-radius: 8px;
  color: #334155;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
}

.hot-item:hover {
  background: #f8fafc;
}

.hot-item.highlighted {
  background: #eff6ff;
  color: #3b82f6;
  font-weight: 500;
}
</style>
