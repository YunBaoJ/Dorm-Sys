<template>
  <div class="rooms-container">
    <el-row :gutter="24">
      <!-- Left Column -->
      <el-col :span="18">
        <!-- Hero Header -->
        <el-card shadow="never" class="hero-card">
          <div class="hero-content">
            <div class="hero-text">
              <el-icon :size="28" color="var(--el-color-primary)"><component :is="Home" /></el-icon>
              <div>
                <h2>宿舍房间配置</h2>
                <p>精细化分配 · 状态实时追踪</p>
              </div>
            </div>
            <div class="hero-actions">
              <el-button type="primary"><el-icon class="el-icon--left"><component :is="Plus" /></el-icon>新增单间</el-button>
              <el-button plain type="warning"><el-icon class="el-icon--left"><component :is="Files" /></el-icon>批量创建</el-button>
            </div>
          </div>
        </el-card>

        <!-- Room List Section -->
        <el-card shadow="never" class="list-card">
          <div class="list-header">
            <div class="header-left">
              <span class="list-title">房间名录</span>
              <el-select v-model="buildingFilter" placeholder="所属楼栋" style="width: 140px; margin-left: 16px;">
                <el-option label="全部楼栋" value="all" />
                <el-option label="明德楼" value="mingde" />
                <el-option label="至善楼" value="zhishan" />
              </el-select>
              <el-input v-model="search" placeholder="房间号搜索" prefix-icon="Search" style="width: 180px; margin-left: 12px;" />
            </div>
            <div class="list-actions">
              <el-button circle><el-icon><component :is="Refresh" /></el-icon></el-button>
            </div>
          </div>

          <div class="room-list">
            <div v-for="room in rooms" :key="room.id" class="room-item">
              <div class="room-badge bg-light-blue">
                <span class="room-no">{{ room.number }}</span>
                <span class="room-type">{{ room.capacity }}人间</span>
              </div>
              
              <div class="room-info">
                <div class="room-main">
                  <span class="room-bldg"><el-icon><component :is="Building" /></el-icon> {{ room.building }}</span>
                  <span class="room-floor">{{ room.floor }}层</span>
                </div>
                
                <div class="progress-section">
                  <div class="progress-header">
                    <span class="progress-label">入住进度</span>
                    <span class="progress-val">{{ room.occupied }}/{{ room.capacity }}</span>
                  </div>
                  <el-progress 
                    :percentage="(room.occupied / room.capacity) * 100" 
                    :stroke-width="8" 
                    :color="room.occupied === room.capacity ? '#f59e0b' : '#e2e8f0'"
                    :show-text="false"
                  />
                </div>
                
                <div class="room-tags">
                  <el-tag v-if="room.occupied === room.capacity" size="small" type="warning" effect="plain">全部满员</el-tag>
                  <el-tag v-else size="small" type="info" effect="plain">整间空闲</el-tag>
                  <el-tag v-if="room.status === '维护中'" size="small" type="warning" effect="plain">维护中</el-tag>
                  <el-tag v-else size="small" type="primary" effect="plain">可用</el-tag>
                </div>
              </div>

              <div class="room-actions-col">
                <div class="action-buttons">
                  <el-button type="primary" link><el-icon class="el-icon--left"><component :is="Edit" /></el-icon>编辑</el-button>
                  <el-button type="primary" link><el-icon class="el-icon--left"><component :is="Eye" /></el-icon>床位</el-button>
                  <el-button type="danger" link><el-icon class="el-icon--left"><component :is="Trash2" /></el-icon>删除</el-button>
                </div>
                <div class="room-time"><el-icon><component :is="Clock" /></el-icon> 09/01</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- Right Column -->
      <el-col :span="6">
        <!-- Status Distribution -->
        <el-card shadow="never" class="side-card">
          <template #header>
            <div class="card-header"><span>状态分布</span></div>
          </template>
          <div class="dashboard-list">
            <div class="dash-item">
              <div class="dash-icon bg-light-blue"><el-icon color="#3b82f6"><component :is="Check" /></el-icon></div>
              <div class="dash-info">
                <div class="dash-label">空闲房间</div>
                <div class="dash-value">42 <span>间</span></div>
              </div>
            </div>
            <div class="dash-item">
              <div class="dash-icon bg-light-orange"><el-icon color="#f59e0b"><component :is="AlertCircle" /></el-icon></div>
              <div class="dash-info">
                <div class="dash-label">满员房间</div>
                <div class="dash-value">1,208 <span>间</span></div>
              </div>
            </div>
            <div class="dash-item">
              <div class="dash-icon bg-gray"><el-icon color="#64748b"><component :is="Settings" /></el-icon></div>
              <div class="dash-info">
                <div class="dash-label">维修中</div>
                <div class="dash-value">15 <span>间</span></div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- Allocation Guide -->
        <el-card shadow="never" class="side-card">
          <template #header>
            <div class="card-header"><span>配额说明</span></div>
          </template>
          <div class="guide-list">
            <div class="guide-item">
              <div class="guide-num">4</div>
              <div class="guide-info">
                <div class="guide-title">四人间 (标配)</div>
                <div class="guide-desc">适合本科生，配套书桌、衣柜齐全。</div>
              </div>
            </div>
            <div class="guide-item">
              <div class="guide-num bg-indigo">6</div>
              <div class="guide-info">
                <div class="guide-title">六人间/八人间</div>
                <div class="guide-desc">主要用于公共区域或特殊公寓楼栋。</div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- Management Tips -->
        <el-card shadow="never" class="side-card bg-light-blue rules-card">
          <template #header>
            <div class="card-header">
              <span class="text-primary">房源管理建议</span>
              <el-icon><component :is="Info" /></el-icon>
            </div>
          </template>
          <ul class="tips-list">
            <li>批量创建时请核对起始房号与实际物理排布。</li>
            <li>房间状态设为"禁用"将导致其下所有床位不可选。</li>
            <li>维修中的房间不会出现在学生的调宿申请可选列表中。</li>
          </ul>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Home, Plus, Files, Search, RefreshCw as Refresh, Building, Edit, Eye, Trash2, Clock, Check, AlertCircle, Settings, Info } from '@lucide/vue'

const buildingFilter = ref('')
const search = ref('')

const rooms = ref([
  { id: 1, number: '101', capacity: 4, building: '明德楼', floor: 1, occupied: 4, status: '维护中' },
  { id: 2, number: '102', capacity: 4, building: '明德楼', floor: 1, occupied: 4, status: '正常' },
  { id: 3, number: '103', capacity: 4, building: '明德楼', floor: 1, occupied: 0, status: '正常' },
  { id: 4, number: '104', capacity: 4, building: '明德楼', floor: 1, occupied: 0, status: '正常' },
  { id: 5, number: '105', capacity: 4, building: '明德楼', floor: 1, occupied: 0, status: '正常' },
  { id: 6, number: '201', capacity: 4, building: '明德楼', floor: 2, occupied: 0, status: '正常' },
])
</script>

<style scoped>
.rooms-container {
  max-width: 1400px;
  margin: 0 auto;
}

.hero-card {
  margin-bottom: 24px;
  border-radius: 12px;
}

.hero-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.hero-text {
  display: flex;
  align-items: center;
  gap: 16px;
}

.hero-text h2 {
  margin: 0 0 4px 0;
  font-size: 20px;
  color: #1f2937;
}

.hero-text p {
  margin: 0;
  font-size: 14px;
  color: #64748b;
}

.hero-actions {
  display: flex;
  gap: 12px;
}

.list-card {
  min-height: 600px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f1f5f9;
}

.header-left {
  display: flex;
  align-items: center;
}

.list-title {
  font-size: 16px;
  font-weight: bold;
  color: #1f2937;
}

.room-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.room-item {
  display: flex;
  align-items: center;
  padding: 24px;
  background-color: #f8fafc;
  border-radius: 12px;
  transition: background-color 0.2s;
}

.room-item:hover {
  background-color: #f1f5f9;
}

.room-badge {
  width: 80px;
  height: 80px;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #3b82f6;
}

.bg-light-blue { background: #eff6ff; }

.room-no {
  font-size: 24px;
  font-weight: bold;
}

.room-type {
  font-size: 12px;
  opacity: 0.8;
}

.room-info {
  flex: 1;
  margin-left: 24px;
}

.room-main {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  font-size: 14px;
  color: #1f2937;
}

.room-bldg {
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
}

.room-floor {
  color: #64748b;
}

.progress-section {
  max-width: 400px;
  margin-bottom: 16px;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 13px;
}

.progress-label {
  color: #64748b;
}

.progress-val {
  font-weight: 500;
  color: #1f2937;
}

.room-tags {
  display: flex;
  gap: 8px;
}

.room-actions-col {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 32px;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.room-time {
  display: flex;
  align-items: center;
  gap: 4px;
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

.dashboard-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.dash-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
}

.dash-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 20px;
}

.bg-light-orange { background: #fff7ed; }
.bg-gray { background: #f1f5f9; }

.dash-info {
  flex: 1;
}

.dash-label {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 4px;
}

.dash-value {
  font-size: 20px;
  font-weight: bold;
  color: #1f2937;
}

.dash-value span {
  font-size: 13px;
  font-weight: normal;
  color: #94a3b8;
}

.guide-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.guide-item {
  display: flex;
  gap: 16px;
}

.guide-num {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #eff6ff;
  color: #3b82f6;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  flex-shrink: 0;
}

.bg-indigo {
  background: #eef2ff;
  color: #6366f1;
}

.guide-info {
  flex: 1;
}

.guide-title {
  font-size: 14px;
  font-weight: bold;
  color: #1f2937;
  margin-bottom: 4px;
}

.guide-desc {
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
}

.rules-card {
  background-color: #f0f7ff;
  border: 1px solid #e0f2fe !important;
}

.text-primary {
  color: #3b82f6;
}

.tips-list {
  margin: 0;
  padding-left: 16px;
  font-size: 13px;
  color: #1e293b;
  line-height: 2;
}

.tips-list li::marker {
  color: #3b82f6;
}
</style>
