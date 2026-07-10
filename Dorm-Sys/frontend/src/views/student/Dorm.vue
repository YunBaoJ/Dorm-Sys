<template>
  <div class="dorm-container">
    <el-row :gutter="24">
      <!-- Left Column -->
      <el-col :span="8">
        <!-- Room Info Card -->
        <el-card shadow="never" class="room-info-card">
          <div class="room-hero">
            <div class="room-icon-wrapper">
              <el-icon :size="48" color="#3b82f6"><component :is="Home" /></el-icon>
            </div>
            <h2>明德楼 · 101</h2>
            <div class="room-tags">
              <el-tag size="small" effect="plain">1层</el-tag>
              <el-tag size="small" effect="plain" type="info">4人间</el-tag>
              <el-tag size="small" type="primary">1号床位</el-tag>
            </div>
          </div>
          <el-divider border-style="dashed" />
          <div class="room-details">
            <div class="detail-row">
              <span class="detail-label">入住日期</span>
              <span class="detail-value">2025/9/1</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">所属校区</span>
              <span class="detail-value">主校区 · 西区</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">责任宿管</span>
              <span class="detail-value">张宿管</span>
            </div>
          </div>
        </el-card>

        <!-- Roommates Card -->
        <el-card shadow="never" class="roommates-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">我的室友</span>
              <span class="card-subtitle">4/4</span>
            </div>
          </template>
          <div class="roommates-list">
            <div v-for="rm in roommates" :key="rm.bed" class="roommate-item" :class="{'is-me': rm.isMe}">
              <el-avatar :size="40" :src="rm.avatar">{{ rm.name[0] }}</el-avatar>
              <div class="rm-info">
                <div class="rm-name-row">
                  <span class="rm-name">{{ rm.name }}</span>
                  <el-tag v-if="rm.isMe" size="small" type="primary" effect="dark" class="me-tag">我</el-tag>
                </div>
                <div class="rm-meta">
                  <span class="rm-bed">{{ rm.bed }}号床</span>
                  <el-tag size="small" type="info" effect="plain" class="status-tag">{{ rm.status }}</el-tag>
                </div>
              </div>
              <el-button v-if="!rm.isMe" circle link type="primary"><el-icon :size="20"><component :is="MessageCircle" /></el-icon></el-button>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- Right Column -->
      <el-col :span="16">
        <!-- Room Layout -->
        <el-card shadow="never" class="layout-card">
          <template #header>
            <div class="card-header"><span class="card-title">宿舍床位布局</span></div>
          </template>
          <div class="layout-diagram-wrapper">
            <div class="layout-box">
              <div class="door-label">大门</div>
              <div class="balcony-label">阳台</div>
              
              <div class="beds-grid">
                <div class="bed-item">
                  <div class="bed-box">1-1</div>
                  <div class="bed-name">张伟</div>
                </div>
                <div class="bed-item">
                  <div class="bed-box">1-2</div>
                  <div class="bed-name">李明</div>
                </div>
                <div class="bed-item">
                  <div class="bed-box">1-3</div>
                  <div class="bed-name">王芳</div>
                </div>
                <div class="bed-item">
                  <div class="bed-box">1-4</div>
                  <div class="bed-name">刘洋</div>
                </div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- Bottom Row -->
        <el-row :gutter="24">
          <!-- Facilities -->
          <el-col :span="14">
            <el-card shadow="never" class="facilities-card">
              <template #header>
                <div class="card-header">
                  <span class="card-title">设施运行状态 <el-icon class="refresh-icon"><component :is="Refresh" /></el-icon></span>
                  <el-button type="primary" link>刷新</el-button>
                </div>
              </template>
              <div class="facilities-grid">
                <div class="facility-item">
                  <div class="fac-icon bg-light-blue"><el-icon color="#3b82f6"><component :is="Lightbulb" /></el-icon></div>
                  <div class="fac-info">
                    <div class="fac-name">照明灯具</div>
                    <div class="fac-status text-gray">运行中</div>
                  </div>
                </div>
                <div class="facility-item">
                  <div class="fac-icon bg-light-blue"><el-icon color="#3b82f6"><component :is="Fan" /></el-icon></div>
                  <div class="fac-info">
                    <div class="fac-name">空调设备</div>
                    <div class="fac-status text-gray">良好</div>
                  </div>
                </div>
                <div class="facility-item">
                  <div class="fac-icon bg-light-blue"><el-icon color="#3b82f6"><component :is="Wifi" /></el-icon></div>
                  <div class="fac-info">
                    <div class="fac-name">宿舍网络</div>
                    <div class="fac-status text-gray">已连接</div>
                  </div>
                </div>
                <div class="facility-item">
                  <div class="fac-icon bg-light-orange"><el-icon color="#f59e0b"><component :is="CupSoda" /></el-icon></div>
                  <div class="fac-info">
                    <div class="fac-name">直饮水</div>
                    <div class="fac-status text-orange">滤芯待换</div>
                  </div>
                </div>
              </div>
            </el-card>
          </el-col>

          <!-- Hygiene Score -->
          <el-col :span="10">
            <el-card shadow="never" class="hygiene-card">
              <template #header>
                <div class="card-header"><span class="card-title">卫生月度指标</span></div>
              </template>
              <div class="hygiene-content">
                <div class="score-circle">
                  <el-progress type="dashboard" :percentage="95" :width="120" :stroke-width="8" color="#3b82f6">
                    <template #default="{ percentage }">
                      <span class="score-value">{{ percentage }}</span>
                      <span class="score-unit">分</span>
                    </template>
                  </el-progress>
                  <div class="score-label">本月平均得分</div>
                </div>
                <div class="score-stats">
                  <div class="stat-row">
                    <span class="stat-label">全楼排名</span>
                    <span class="stat-val text-dark">第 12 名</span>
                  </div>
                  <div class="stat-row">
                    <span class="stat-label">上次检查</span>
                    <span class="stat-val text-dark">98 <span class="text-gray">(优秀)</span></span>
                  </div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { Home, MessageCircle, RefreshCw as Refresh, Lightbulb, Fan, Wifi, CupSoda } from '@lucide/vue'

const roommates = [
  { name: '张伟', bed: '1-1', status: '外出', isMe: true, avatar: '/images/avatar.jpg' },
  { name: '李明', bed: '1-2', status: '外出', isMe: false, avatar: '' },
  { name: '王芳', bed: '1-3', status: '外出', isMe: false, avatar: '' },
  { name: '刘洋', bed: '1-4', status: '外出', isMe: false, avatar: '' },
]
</script>

<style scoped>
.dorm-container {
  max-width: 1400px;
  margin: 0 auto;
}

.room-info-card {
  margin-bottom: 24px;
  text-align: center;
}

.room-hero {
  padding: 24px 0 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.room-icon-wrapper {
  width: 80px;
  height: 80px;
  background-color: #eff6ff;
  border-radius: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 16px;
}

.room-hero h2 {
  font-size: 24px;
  color: #1f2937;
  margin: 0 0 16px 0;
}

.room-tags {
  display: flex;
  gap: 8px;
}

.room-details {
  padding: 8px 0;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  font-size: 14px;
}

.detail-label {
  color: #64748b;
}

.detail-value {
  font-weight: 600;
  color: #1f2937;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: bold;
  color: #1f2937;
}

.card-subtitle {
  color: #94a3b8;
  font-size: 14px;
}

.roommates-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.roommate-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-radius: 12px;
  transition: background-color 0.2s;
}

.roommate-item:hover {
  background-color: #f8fafc;
}

.roommate-item.is-me {
  background-color: #eff6ff;
  border: 1px solid #bfdbfe;
}

.rm-info {
  flex: 1;
  margin-left: 12px;
}

.rm-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.rm-name {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
}

.me-tag {
  transform: scale(0.9);
}

.rm-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.rm-bed {
  font-size: 13px;
  color: #94a3b8;
}

.status-tag {
  transform: scale(0.9);
}

.layout-card {
  margin-bottom: 24px;
  min-height: 400px;
}

.layout-diagram-wrapper {
  padding: 40px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.layout-box {
  width: 100%;
  max-width: 600px;
  height: 300px;
  border: 4px solid #cbd5e1;
  border-radius: 8px;
  position: relative;
  border-bottom-color: transparent;
  border-top-color: transparent;
}

.layout-box::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 40%;
  height: 4px;
  background-color: #cbd5e1;
}
.layout-box::after {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 40%;
  height: 4px;
  background-color: #cbd5e1;
}

.door-label {
  position: absolute;
  top: -12px;
  left: 50%;
  transform: translateX(-50%);
  background: white;
  padding: 0 16px;
  color: #94a3b8;
  font-size: 14px;
}

.layout-box > .balcony-label {
  position: absolute;
  bottom: -12px;
  left: 50%;
  transform: translateX(-50%);
  background: white;
  padding: 0 16px;
  color: #94a3b8;
  font-size: 14px;
}

.layout-box > .door-label ~ .beds-grid::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 40%;
  height: 4px;
  background-color: #cbd5e1;
}
.layout-box > .door-label ~ .beds-grid::before {
  content: '';
  position: absolute;
  bottom: 0;
  right: 0;
  width: 40%;
  height: 4px;
  background-color: #cbd5e1;
}

.beds-grid {
  height: 100%;
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: 1fr 1fr;
  padding: 40px 60px;
  gap: 40px;
}

.bed-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.bed-box {
  width: 100px;
  height: 60px;
  background-color: #f1f5f9;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-weight: bold;
  color: #64748b;
}

.bed-name {
  font-size: 14px;
  color: #475569;
}

.facilities-card {
  height: 100%;
}

.refresh-icon {
  margin-left: 8px;
  color: #94a3b8;
  cursor: pointer;
}

.facilities-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.facility-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
}

.fac-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.bg-light-blue { background: #eff6ff; }
.bg-light-orange { background: #fff7ed; }

.fac-name {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 4px;
}

.fac-status {
  font-size: 12px;
}

.text-gray { color: #64748b; }
.text-orange { color: #f59e0b; }

.hygiene-card {
  height: 100%;
}

.hygiene-content {
  display: flex;
  align-items: center;
  gap: 32px;
  padding: 16px 0;
}

.score-circle {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.score-value {
  font-size: 32px;
  font-weight: bold;
  color: #1f2937;
}

.score-unit {
  font-size: 14px;
  color: #64748b;
}

.score-label {
  font-size: 13px;
  color: #64748b;
}

.score-stats {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 24px;
  background: #f8fafc;
  padding: 24px;
  border-radius: 12px;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-label {
  font-size: 14px;
  color: #64748b;
}

.stat-val {
  font-size: 15px;
  font-weight: bold;
}

.text-dark { color: #1f2937; }
</style>
