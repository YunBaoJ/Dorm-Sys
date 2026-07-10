<template>
  <div class="desk-container">
    <!-- Hero Section -->
    <div class="welcome-card">
      <div class="welcome-content">
        <h1 class="welcome-title">早安，张伟</h1>
        <p class="welcome-subtitle">今天是 2026年3月27日星期五，开启高效的一天吧！</p>
        
        <div class="hero-banner">
          <div class="banner-text">
            <h2>文明寝室 · 共同维护</h2>
            <p>本周将进行月度卫生大检查</p>
          </div>
        </div>
      </div>
      <div class="weather-widget">
        <el-icon :size="20" color="#f59e0b"><component :is="Sun" /></el-icon>
        <span class="weather-temp">22℃ 晴</span>
        <span class="weather-separator">·</span>
        <span class="weather-desc">校园空气优</span>
      </div>
    </div>

    <!-- Main Grid -->
    <el-row :gutter="24">
      <!-- Left Column -->
      <el-col :span="16">
        <div class="service-grid">
          <el-card shadow="hover" class="service-card" @click="router.push('/student/dorm')">
            <div class="service-header">
              <div class="icon-box icon-blue"><component :is="Home" /></div>
              <span class="service-label">我的宿舍</span>
            </div>
            <div class="service-body">
              <div class="service-value">明德楼 101</div>
              <div class="service-action">
                <span>查看详情</span>
                <el-icon><component :is="ChevronRight" /></el-icon>
              </div>
            </div>
          </el-card>

          <el-card shadow="hover" class="service-card" @click="router.push('/student/fees')">
            <div class="service-header">
              <div class="icon-box icon-orange"><component :is="Wallet" /></div>
              <span class="service-label">账户余额</span>
            </div>
            <div class="service-body">
              <div class="service-value">¥ 0.00</div>
              <div class="service-action">
                <span>立即充值</span>
                <el-icon><component :is="ChevronRight" /></el-icon>
              </div>
            </div>
          </el-card>

          <el-card shadow="hover" class="service-card" @click="router.push('/student/repair')">
            <div class="service-header">
              <div class="icon-box icon-cyan"><component :is="Settings" /></div>
              <span class="service-label">报修申请</span>
            </div>
            <div class="service-body">
              <div class="service-value"><strong>1</strong> 件</div>
              <div class="service-action">
                <span>申请报修</span>
                <el-icon><component :is="ChevronRight" /></el-icon>
              </div>
            </div>
          </el-card>

          <el-card shadow="hover" class="service-card" @click="router.push('/student/dorm')">
            <div class="service-header">
              <div class="icon-box icon-blue"><component :is="Medal" /></div>
              <span class="service-label">卫生评分</span>
            </div>
            <div class="service-body">
              <div class="service-value"><strong>92</strong> 分</div>
              <div class="service-action">
                <span>查看详情</span>
                <el-icon><component :is="ChevronRight" /></el-icon>
              </div>
            </div>
          </el-card>
        </div>

        <el-card shadow="never" class="tracking-card">
          <template #header>
            <div class="card-header">
              <span>业务追踪</span>
              <el-button type="primary" link>查看历史</el-button>
            </div>
          </template>
          <el-empty description="暂无最近动态" :image-size="100"></el-empty>
        </el-card>
      </el-col>

      <!-- Right Column -->
      <el-col :span="8">
        <el-card shadow="never" class="side-card">
          <template #header>
            <div class="card-header">
              <span>校园公告</span>
              <el-button type="primary" link>更多</el-button>
            </div>
          </template>
          <div class="notice-list">
            <div v-for="n in notices" :key="n.id" class="notice-item">
              <div class="notice-dot" :class="{ 'is-pinned': n.pinned }"></div>
              <div class="notice-content">
                <div class="notice-title">{{ n.title }}</div>
                <div class="notice-date">{{ n.date }}</div>
              </div>
            </div>
          </div>
        </el-card>

        <el-card shadow="never" class="side-card">
          <template #header>
            <div class="card-header">
              <span>我的室友</span>
            </div>
          </template>
          <div class="roommate-list">
            <div v-for="(rm, idx) in roommates" :key="idx" class="roommate-item">
              <el-avatar :size="40" :src="rm.avatar">{{ rm.name[0] }}</el-avatar>
              <div class="roommate-info">
                <div class="roommate-name">{{ rm.name }}</div>
                <div class="roommate-bed">1-{{ idx + 2 }}号床</div>
              </div>
              <el-button type="primary" link class="chat-btn">
                <el-icon :size="18"><component :is="MessageCircle" /></el-icon>
              </el-button>
            </div>
          </div>
        </el-card>

        <el-card shadow="never" class="side-card quick-entry-card">
          <template #header>
            <div class="card-header">
              <span>快速入口</span>
            </div>
          </template>
          <div class="quick-entry-grid">
            <div class="quick-btn" @click="router.push('/student/visitor')">
              <el-icon :size="24"><component :is="UserRoundCheck" /></el-icon>
              <span>访客预约</span>
            </div>
            <div class="quick-btn" @click="router.push('/student/transfer')">
              <el-icon :size="24"><component :is="Repeat2" /></el-icon>
              <span>调宿申请</span>
            </div>
            <div class="quick-btn" @click="router.push('/student/ai')">
              <el-icon :size="24"><component :is="MessageCircle" /></el-icon>
              <span>AI 助手</span>
            </div>
            <div class="quick-btn">
              <el-icon :size="24"><component :is="Pencil" /></el-icon>
              <span>意见反馈</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { dataStore } from '../../store/data'
import { Sun, Home, Wallet, Settings, Medal, ChevronRight, MessageCircle, UserRoundCheck, Repeat2, Pencil } from '@lucide/vue'

const router = useRouter()
const notices = computed(() => dataStore.notices)

const roommates = [
  { name: '李明', avatar: '' },
  { name: '王芳', avatar: '' },
  { name: '刘洋', avatar: '' }
]
</script>

<style scoped>
.desk-container {
  max-width: 1200px;
  margin: 0 auto;
}

.welcome-card {
  position: relative;
  margin-bottom: 24px;
}

.welcome-title {
  font-size: 24px;
  font-weight: bold;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.welcome-subtitle {
  color: #64748b;
  font-size: 14px;
  margin: 0 0 20px 0;
}

.weather-widget {
  position: absolute;
  top: 0;
  right: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  background-color: #fff;
  padding: 8px 16px;
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  font-size: 14px;
  color: #475569;
}

.weather-separator {
  color: #cbd5e1;
}

.hero-banner {
  height: 200px;
  background: linear-gradient(135deg, #d1d5db 0%, #e5e7eb 100%); /* Placeholder gray gradient */
  border-radius: 16px;
  position: relative;
  display: flex;
  align-items: flex-end;
  padding: 32px;
  color: #fff;
  overflow: hidden;
}

.hero-banner::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(0,0,0,0.4) 0%, transparent 100%);
}

.banner-text {
  position: relative;
  z-index: 1;
}

.banner-text h2 {
  font-size: 24px;
  margin: 0 0 8px 0;
  text-shadow: 0 2px 4px rgba(0,0,0,0.2);
}

.banner-text p {
  margin: 0;
  opacity: 0.9;
}

.service-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.service-card {
  cursor: pointer;
  transition: transform 0.2s;
  border-radius: 16px !important;
}

.service-card:hover {
  transform: translateY(-2px);
}

.service-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.icon-box {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-blue { background: #e0f2fe; color: #0284c7; }
.icon-orange { background: #ffedd5; color: #ea580c; }
.icon-cyan { background: #cffafe; color: #0891b2; }

.service-label {
  color: #64748b;
  font-size: 14px;
}

.service-body {
  display: flex;
  flex-direction: column;
}

.service-value {
  font-size: 20px;
  color: #1f2937;
  margin-bottom: 16px;
}

.service-value strong {
  font-size: 24px;
}

.service-action {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #94a3b8;
  font-size: 13px;
  border-top: 1px solid #f1f5f9;
  padding-top: 12px;
}

.tracking-card {
  min-height: 250px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.side-card {
  margin-bottom: 20px;
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.notice-item {
  display: flex;
  gap: 12px;
}

.notice-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #cbd5e1;
  margin-top: 6px;
  flex-shrink: 0;
}

.notice-dot.is-pinned {
  background-color: var(--el-color-danger);
}

.notice-content {
  flex: 1;
}

.notice-title {
  color: #334155;
  font-size: 14px;
  margin-bottom: 4px;
}

.notice-date {
  color: #94a3b8;
  font-size: 12px;
}

.roommate-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.roommate-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.roommate-info {
  flex: 1;
}

.roommate-name {
  font-size: 14px;
  color: #1f2937;
  font-weight: 500;
  margin-bottom: 4px;
}

.roommate-bed {
  font-size: 12px;
  color: #64748b;
  background-color: #f1f5f9;
  padding: 2px 8px;
  border-radius: 4px;
  display: inline-block;
}

.chat-btn {
  color: var(--el-color-primary-light-3);
}

.quick-entry-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.quick-btn {
  background-color: #f8fafc;
  border-radius: 12px;
  padding: 20px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
  color: #475569;
}

.quick-btn:hover {
  background-color: #f1f5f9;
  color: var(--el-color-primary);
}

.quick-btn span {
  font-size: 13px;
}
</style>
