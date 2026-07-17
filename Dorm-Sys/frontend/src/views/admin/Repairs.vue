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
            <el-button type="primary" :loading="loading" @click="fetchTickets"><el-icon class="el-icon--left"><component :is="Refresh" /></el-icon>同步工单池</el-button>
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
                <el-option v-for="category in categories" :key="category" :label="category" :value="category" />
              </el-select>
              <el-button circle @click="fetchTickets"><el-icon><component :is="Refresh" /></el-icon></el-button>
            </div>
          </div>

          <div class="ticket-list" v-loading="loading">
            <el-empty v-if="tickets.length === 0" description="暂无符合条件的报修工单" />
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
                <div class="dash-value">{{ pendingCount }} <span>单</span></div>
              </div>
            </div>
            <div class="dash-item">
              <div class="dash-icon bg-light-blue"><el-icon><component :is="Timer" /></el-icon></div>
              <div class="dash-info">
                <div class="dash-label">进行中</div>
                <div class="dash-value">{{ processingCount }} <span>单</span></div>
              </div>
            </div>
            <div class="dash-item">
              <div class="dash-icon bg-gray"><el-icon><component :is="CheckCircle" /></el-icon></div>
              <div class="dash-info">
                <div class="dash-label">今日已结</div>
                <div class="dash-value">{{ todayCompletedCount }} <span>单</span></div>
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
import { ref, computed, onMounted } from 'vue'
import { Settings, RefreshCw as Refresh, User, Clock, CheckCircle, AlertCircle, Timer, Info } from '@lucide/vue'
import { getRepairs } from '../../api/repair'
import { ElMessage } from 'element-plus'

const categoryFilter = ref('all')
const statusFilter = ref('all')
const records = ref([])
const loading = ref(false)

const statusMeta = {
  PENDING: { label: '待处理', type: 'warning' },
  PROCESSING: { label: '维修中', type: 'primary' },
  COMPLETED: { label: '已办结', type: 'info' }
}

const categories = computed(() => [...new Set(records.value.map(item => item.type).filter(Boolean))])
const tickets = computed(() => records.value
  .filter(item => statusFilter.value === 'all' || item.status?.toLowerCase() === statusFilter.value)
  .filter(item => categoryFilter.value === 'all' || item.type === categoryFilter.value)
  .map(item => ({
    ...item,
    room: item.roomName || '未关联房间',
    category: item.type || '其他',
    status: statusMeta[item.status]?.label || item.status,
    statusType: statusMeta[item.status]?.type || 'info',
    reporter: item.submitterName || '未知',
    assignee: item.handlerName || '待分配',
    time: item.createTime?.replace('T', ' ').slice(0, 16) || '-'
  })))

const pendingCount = computed(() => records.value.filter(item => item.status === 'PENDING').length)
const processingCount = computed(() => records.value.filter(item => item.status === 'PROCESSING').length)
const todayCompletedCount = computed(() => records.value.filter(item => item.status === 'COMPLETED' && new Date(item.updateTime || item.createTime).toDateString() === new Date().toDateString()).length)

async function fetchTickets() {
  loading.value = true
  try {
    records.value = await getRepairs()
  } catch (error) { console.error(error);
    ElMessage.error('获取报修工单失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchTickets)
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
  color: var(--text);
}

.hero-text p {
  margin: 0;
  font-size: 14px;
  color: var(--sub);
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
  border-bottom: 1px solid var(--line);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 24px;
}

.list-title {
  font-size: 16px;
  font-weight: bold;
  color: var(--text);
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
  background-color: var(--bg);
  border-radius: 12px;
  transition: background-color 0.2s;
}

.ticket-item:hover {
  background-color: var(--line);
}

.ticket-icon {
  margin-top: 4px;
  color: var(--text);
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
  color: var(--text);
}

.ticket-time {
  margin-left: auto;
  font-size: 13px;
  color: var(--sub);
  display: flex;
  align-items: center;
  gap: 6px;
}

.ticket-meta-row {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  color: var(--sub);
  margin-bottom: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.ticket-desc {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 16px;
}

.ticket-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.assignee-tag {
  background: var(--sub);
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
  background: var(--bg);
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

.bg-light-blue { background: var(--primary-2); color: #3b82f6; }
.bg-light-orange { background: var(--orange-2); color: #f97316; }
.bg-gray { background: var(--line); color: var(--sub); }

.dash-info {
  flex: 1;
}

.dash-label {
  font-size: 13px;
  color: var(--sub);
  margin-bottom: 4px;
}

.dash-value {
  font-size: 20px;
  font-weight: bold;
  color: var(--text);
}

.dash-value span {
  font-size: 13px;
  font-weight: normal;
  color: var(--sub);
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
  color: var(--text-secondary);
}

.avg-val {
  font-weight: 600;
}

.text-blue { color: #3b82f6; }
.text-orange { color: #f59e0b; }

.rules-card {
  background-color: var(--primary-2);
  border: 1px solid var(--primary-2) !important;
}

.text-primary {
  color: #3b82f6;
}

.rules-list {
  margin: 0;
  padding-left: 20px;
  font-size: 13px;
  color: var(--text);
  line-height: 2;
}

.rules-list li::marker {
  color: #3b82f6;
}
</style>
