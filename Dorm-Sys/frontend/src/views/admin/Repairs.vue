<template>
  <div class="repairs-container">
    <el-row :gutter="24">
      <!-- Left Column -->
      <el-col :span="18">
        <!-- Hero Header -->
        <el-card shadow="never" class="hero-card">
          <div class="hero-content">
            <div class="hero-text">
              <el-icon :size="28" color="var(--el-color-primary)"><component :is="Settings" /></el-icon>
              <div>
                <h2>全校报修监控</h2>
                <p>工单审计 · 维护效能分析</p>
              </div>
            </div>
            <el-button type="primary"><el-icon class="el-icon--left"><component :is="Refresh" /></el-icon>同步工单池</el-button>
          </div>
        </el-card>

        <!-- Ticket List Section -->
        <el-card shadow="never" class="list-card">
          <div class="list-header">
            <div class="header-left">
              <span class="list-title">报修工单</span>
              <el-radio-group v-model="statusFilter" size="small" class="status-tabs">
                <el-radio-button label="all">全部</el-radio-button>
                <el-radio-button label="pending">待处理</el-radio-button>
                <el-radio-button label="processing">进行中</el-radio-button>
                <el-radio-button label="completed">已办结</el-radio-button>
              </el-radio-group>
            </div>
            <div class="list-actions">
              <el-select v-model="categoryFilter" placeholder="报修分类" style="width: 140px; margin-right: 12px;">
                <el-option label="全部分类" value="all" />
                <el-option label="水暖" value="water" />
                <el-option label="强弱电" value="elec" />
                <el-option label="门窗" value="door" />
              </el-select>
              <el-button circle><el-icon><component :is="Refresh" /></el-icon></el-button>
            </div>
          </div>

          <div class="ticket-list">
            <div v-for="t in tickets" :key="t.id" class="ticket-item">
              <div class="ticket-icon">
                <el-icon :size="24"><component :is="Settings" /></el-icon>
              </div>
              
              <div class="ticket-info">
                <div class="ticket-main">
                  <span class="ticket-room">{{ t.room }}</span>
                  <el-tag size="small" :type="t.statusType" effect="plain" round>{{ t.status }}</el-tag>
                  <span class="ticket-time"><el-icon><component :is="Clock" /></el-icon> {{ t.time }}</span>
                </div>
                <div class="ticket-meta-row">
                  <span class="meta-item"><el-icon><component :is="User" /></el-icon> 报修人: {{ t.reporter }}</span>
                  <el-divider direction="vertical" />
                  <span class="meta-item">分类: {{ t.category }}</span>
                </div>
                <div class="ticket-desc">{{ t.description }}</div>
                <div class="ticket-footer">
                  <div class="assignee-tag">承修: {{ t.assignee }}</div>
                  <el-button type="primary">查看档案</el-button>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- Right Column -->
      <el-col :span="6">
        <!-- Efficiency Dashboard -->
        <el-card shadow="never" class="side-card">
          <template #header>
            <div class="card-header"><span>处理效能看板</span></div>
          </template>
          <div class="dashboard-list">
            <div class="dash-item">
              <div class="dash-icon bg-light-orange"><el-icon><component :is="AlertCircle" /></el-icon></div>
              <div class="dash-info">
                <div class="dash-label">积压待办</div>
                <div class="dash-value">1 <span>单</span></div>
              </div>
            </div>
            <div class="dash-item">
              <div class="dash-icon bg-light-blue"><el-icon><component :is="Timer" /></el-icon></div>
              <div class="dash-info">
                <div class="dash-label">进行中</div>
                <div class="dash-value">1 <span>单</span></div>
              </div>
            </div>
            <div class="dash-item">
              <div class="dash-icon bg-gray"><el-icon><component :is="CheckCircle" /></el-icon></div>
              <div class="dash-info">
                <div class="dash-label">今日已结</div>
                <div class="dash-value">2 <span>单</span></div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- Average Repair Time -->
        <el-card shadow="never" class="side-card">
          <template #header>
            <div class="card-header"><span>平均修复时长</span></div>
          </template>
          <div class="avg-time-list">
            <div class="avg-item">
              <div class="avg-header">
                <span class="avg-label">强电/弱电故障</span>
                <span class="avg-val text-blue">4.2h</span>
              </div>
              <el-progress :percentage="80" :stroke-width="6" :show-text="false" color="#3b82f6" />
            </div>
            <div class="avg-item">
              <div class="avg-header">
                <span class="avg-label">水暖设施故障</span>
                <span class="avg-val text-orange">2.8h</span>
              </div>
              <el-progress :percentage="60" :stroke-width="6" :show-text="false" color="#f59e0b" />
            </div>
          </div>
        </el-card>

        <!-- Rules -->
        <el-card shadow="never" class="side-card bg-light-blue rules-card">
          <template #header>
            <div class="card-header">
              <span class="text-primary">监控准则</span>
              <el-icon><component :is="Info" /></el-icon>
            </div>
          </template>
          <ol class="rules-list">
            <li>超过 24 小时未处理的工单将自动标记为"严重超时"。</li>
            <li>管理员需定期抽查已结办工单的学生满意度回访。</li>
            <li>大型设施损坏应及时协调校后勤处进行联合维修。</li>
          </ol>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Settings, RefreshCw as Refresh, User, Clock, CheckCircle, AlertCircle, Timer, Info } from '@lucide/vue'

const categoryFilter = ref('')
const statusFilter = ref('all')

const tickets = ref([
  { id: 1, room: '至善楼 · 101', category: '水管', status: '待处理', statusType: 'warning', description: '卫生间水龙头漏水', reporter: '陈欣', assignee: '张宿管', time: '2026-01-05T09:00:00' },
  { id: 2, room: '明德楼 · 101', category: '网络', status: '维修中', statusType: 'primary', description: '网络信号时断时续，无法正常上网', reporter: '李明', assignee: '张宿管', time: '2025-12-10T14:30:00' },
  { id: 3, room: '明德楼 · 101', category: '电器', status: '已办结', statusType: 'info', description: '空调不制冷，开机后只有风扇转动', reporter: '张伟', assignee: '张宿管', time: '2025-12-01T10:00:00' },
  { id: 4, room: '明德楼 · 102', category: '门窗', status: '已办结', statusType: 'info', description: '窗户关不严，有风漏进来', reporter: '吴刚', assignee: '张宿管', time: '2025-11-20T11:00:00' },
])
</script>

<style scoped>
.repairs-container {
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
  gap: 24px;
}

.list-title {
  font-size: 16px;
  font-weight: bold;
  color: #1f2937;
}

.list-actions {
  display: flex;
  align-items: center;
}

.ticket-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.ticket-item {
  display: flex;
  align-items: flex-start;
  padding: 24px;
  background-color: #f8fafc;
  border-radius: 12px;
  transition: background-color 0.2s;
}

.ticket-item:hover {
  background-color: #f1f5f9;
}

.ticket-icon {
  margin-top: 4px;
  color: #1f2937;
}

.ticket-info {
  flex: 1;
  margin-left: 16px;
}

.ticket-main {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.ticket-room {
  font-size: 16px;
  font-weight: bold;
  color: #1f2937;
}

.ticket-time {
  margin-left: auto;
  font-size: 13px;
  color: #94a3b8;
  display: flex;
  align-items: center;
  gap: 6px;
}

.ticket-meta-row {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  color: #64748b;
  margin-bottom: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.ticket-desc {
  font-size: 14px;
  color: #475569;
  margin-bottom: 16px;
}

.ticket-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.assignee-tag {
  background: #94a3b8;
  color: white;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
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

.bg-light-blue { background: #eff6ff; color: #3b82f6; }
.bg-light-orange { background: #fff7ed; color: #f97316; }
.bg-gray { background: #f1f5f9; color: #94a3b8; }

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

.avg-time-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.avg-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.avg-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.avg-label {
  font-size: 13px;
  color: #475569;
}

.avg-val {
  font-weight: 600;
}

.text-blue { color: #3b82f6; }
.text-orange { color: #f59e0b; }

.rules-card {
  background-color: #f0f7ff;
  border: 1px solid #e0f2fe !important;
}

.text-primary {
  color: #3b82f6;
}

.rules-list {
  margin: 0;
  padding-left: 20px;
  font-size: 13px;
  color: #1e293b;
  line-height: 2;
}

.rules-list li::marker {
  color: #3b82f6;
}
</style>
