<template>
  <div class="repair-container">
    <el-row :gutter="24">
      <!-- Left Column -->
      <el-col :span="18">
        <!-- Hero Header -->
        <el-card shadow="never" class="hero-card">
          <div class="hero-content">
            <div class="hero-text">
              <el-icon :size="28" color="var(--el-color-primary)"><component :is="Settings" /></el-icon>
              <div>
                <h2>报修处理</h2>
                <p>高效派工 · 极速维护</p>
              </div>
            </div>
            <el-button type="primary"><el-icon class="el-icon--left"><component :is="Refresh" /></el-icon>同步最新工单</el-button>
          </div>
        </el-card>

        <!-- Ticket List Section -->
        <el-card shadow="never" class="list-card">
          <div class="list-header">
            <div class="header-left">
              <span class="list-title">工单中心</span>
              <el-radio-group v-model="statusFilter" size="small" class="status-tabs">
                <el-radio-button label="all">全部</el-radio-button>
                <el-radio-button label="pending">待处理</el-radio-button>
                <el-radio-button label="processing">进行中</el-radio-button>
                <el-radio-button label="completed">已办结</el-radio-button>
              </el-radio-group>
            </div>
            <div class="list-actions">
              <el-input v-model="search" placeholder="搜索房间号/学生" prefix-icon="Search" style="width: 240px" />
            </div>
          </div>

          <div class="ticket-list">
            <div v-for="t in tickets" :key="t.id" class="ticket-item">
              <div class="ticket-icon bg-blue">
                {{ t.categoryIcon }}
              </div>
              
              <div class="ticket-info">
                <div class="ticket-main">
                  <span class="ticket-room">{{ t.room }}</span>
                  <span class="ticket-category">{{ t.category }}</span>
                  <el-tag size="small" :type="t.statusType" effect="plain" round>{{ t.status }}</el-tag>
                </div>
                <div class="ticket-desc">{{ t.description }}</div>
                <div class="ticket-meta">
                  <span class="meta-item"><el-icon><component :is="User" /></el-icon> {{ t.reporter }}</span>
                  <span class="meta-item"><el-icon><component :is="Clock" /></el-icon> {{ t.time }}</span>
                </div>
              </div>

              <div class="ticket-actions">
                <el-button v-if="t.status === '待处理'" type="primary">接单派工</el-button>
                <el-button v-if="t.status === '处理中'" type="primary">确认完工</el-button>
                <el-button type="primary" link>查看详情</el-button>
              </div>
            </div>
          </div>
          
          <div class="pagination-wrapper">
             <span class="total-text">Total 4</span>
             <el-pagination background layout="prev, pager, next" :total="4" :page-size="4" />
          </div>
        </el-card>
      </el-col>

      <!-- Right Column -->
      <el-col :span="6">
        <!-- Process Dashboard -->
        <el-card shadow="never" class="side-card">
          <template #header>
            <div class="card-header"><span>处理看板</span></div>
          </template>
          <div class="dashboard-list">
            <div class="dash-item">
              <div class="dash-icon bg-light-orange"><el-icon><component :is="Clock" /></el-icon></div>
              <div class="dash-info">
                <div class="dash-label">待处理工单</div>
                <div class="dash-value">1 <span>单</span></div>
              </div>
            </div>
            <div class="dash-item">
              <div class="dash-icon bg-light-blue"><el-icon><component :is="Wrench" /></el-icon></div>
              <div class="dash-info">
                <div class="dash-label">维修进行中</div>
                <div class="dash-value">1 <span>单</span></div>
              </div>
            </div>
            <div class="dash-item">
              <div class="dash-icon bg-gray"><el-icon><component :is="Check" /></el-icon></div>
              <div class="dash-info">
                <div class="dash-label">已修复结办</div>
                <div class="dash-value">2 <span>单</span></div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- SLA Requirements -->
        <el-card shadow="never" class="side-card">
          <template #header>
            <div class="card-header"><span>处理时效要求</span></div>
          </template>
          <div class="sla-list">
            <div class="sla-item">
              <div class="sla-dot dot-orange"></div>
              <div class="sla-info">
                <div class="sla-title">待处理工单 (2h内)</div>
                <div class="sla-desc">须在两小时内完成核实并指派专业维修人员。</div>
              </div>
            </div>
            <div class="sla-item">
              <div class="sla-dot dot-blue"></div>
              <div class="sla-info">
                <div class="sla-title">维修进行中 (24h内)</div>
                <div class="sla-desc">普通故障须在一天内修复，复杂故障须注明预计完工时间。</div>
              </div>
            </div>
            <div class="sla-item">
              <div class="sla-dot dot-blue"></div>
              <div class="sla-info">
                <div class="sla-title">完工结办</div>
                <div class="sla-desc">维修后须在线登记处理结果，系统将自动发起学生满意度回访。</div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- Hotline -->
        <el-card shadow="never" class="side-card hotline-card">
          <template #header>
            <div class="card-header">
              <span>专业维修热线</span>
              <el-icon><component :is="Phone" /></el-icon>
            </div>
          </template>
          <div class="hotline-list">
            <div class="hotline-row">
              <span>水电维修组</span>
              <span class="hotline-val text-blue">0571-8888xxx1</span>
            </div>
            <div class="hotline-row">
              <span>网络保障中心</span>
              <span class="hotline-val text-blue">0571-8888xxx2</span>
            </div>
            <div class="hotline-row">
              <span>后勤物资中心</span>
              <span class="hotline-val text-blue">0571-8888xxx3</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Settings, RefreshCw as Refresh, Search, User, Clock, Wrench, Check, Phone } from '@lucide/vue'

const search = ref('')
const statusFilter = ref('all')

const tickets = ref([
  { id: 1, categoryIcon: '水', room: '至善楼 · 101', category: '水管', status: '待处理', statusType: 'warning', description: '卫生间水龙头漏水', reporter: '陈欣', time: '2026/01/05 09:00' },
  { id: 2, categoryIcon: '网', room: '明德楼 · 101', category: '网络', status: '处理中', statusType: 'primary', description: '网络信号时断时续，无法正常上网', reporter: '李明', time: '2025/12/10 14:30' },
  { id: 3, categoryIcon: '电', room: '明德楼 · 101', category: '电器', status: '已完成', statusType: 'info', description: '空调不制冷，开机后只有风扇转动', reporter: '张伟', time: '2025/12/01 10:00' },
  { id: 4, categoryIcon: '门', room: '明德楼 · 102', category: '门窗', status: '已完成', statusType: 'info', description: '窗户关不严，有风漏进来', reporter: '吴刚', time: '2025/11/20 11:00' },
])
</script>

<style scoped>
.repair-container {
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

.ticket-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.ticket-item {
  display: flex;
  align-items: center;
  padding: 24px;
  background-color: var(--bg);
  border-radius: 12px;
  transition: background-color 0.2s;
}

.ticket-item:hover {
  background-color: var(--line);
}

.ticket-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 20px;
  font-weight: bold;
  color: white;
}

.bg-blue { background: #3b82f6; }

.ticket-info {
  flex: 1;
  margin-left: 24px;
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

.ticket-category {
  font-size: 14px;
  color: var(--sub);
}

.ticket-desc {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 12px;
}

.ticket-meta {
  display: flex;
  align-items: center;
  gap: 24px;
  font-size: 13px;
  color: var(--sub);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.ticket-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.pagination-wrapper {
  margin-top: 24px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
}

.total-text {
  font-size: 13px;
  color: var(--sub);
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
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 24px;
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

.sla-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.sla-item {
  display: flex;
  gap: 12px;
}

.sla-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-top: 6px;
  flex-shrink: 0;
}

.dot-orange { background-color: #f59e0b; }
.dot-blue { background-color: #3b82f6; }

.sla-info {
  flex: 1;
}

.sla-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text);
  margin-bottom: 4px;
}

.sla-desc {
  font-size: 13px;
  color: var(--sub);
  line-height: 1.5;
}

.hotline-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.hotline-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: var(--text-secondary);
}

.hotline-val {
  font-weight: 500;
}

.text-blue { color: #3b82f6; }
</style>
