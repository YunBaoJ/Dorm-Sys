<template>
  <div class="visitor-container">
    <el-row :gutter="24">
      <!-- Left Column -->
      <el-col :span="18">
        <!-- Hero Header -->
        <el-card shadow="never" class="hero-card">
          <div class="hero-content">
            <div class="hero-text">
              <el-icon :size="28" color="var(--el-color-primary)"><component :is="UserCheck" /></el-icon>
              <div>
                <h2>访客管理</h2>
                <p>安全登记 · 实时管控</p>
              </div>
            </div>
            <el-button type="primary" @click="fetchVisitors"><el-icon class="el-icon--left"><component :is="Refresh" /></el-icon>刷新记录</el-button>
          </div>
        </el-card>

        <!-- Visitor List Section -->
        <el-card shadow="never" class="list-card">
          <div class="list-header">
            <div class="header-left">
              <span class="list-title">来访日志</span>
              <el-radio-group v-model="statusFilter" size="small" class="status-tabs">
                <el-radio-button label="all">全部</el-radio-button>
                <el-radio-button label="pending">待审批</el-radio-button>
                <el-radio-button label="active">在馆中</el-radio-button>
              </el-radio-group>
            </div>
            <div class="list-actions">
              <el-input v-model="search" placeholder="访客姓名/联系电话" prefix-icon="Search" style="width: 240px" />
            </div>
          </div>

          <div class="visitor-list" v-loading="loading">
            <el-empty v-if="filteredVisitors.length === 0" description="暂无访客记录" />
            <div v-for="v in pagedVisitors" :key="v.id" class="visitor-item">
              <div class="visitor-avatar" :class="v.avatarClass">
                {{ v.name?.[0] }}
              </div>
              
              <div class="visitor-info">
                <div class="visitor-main">
                  <span class="visitor-name">{{ v.name }}</span>
                  <el-tag size="small" :type="v.status === 'APPROVED' ? 'success' : v.status === 'LEFT' ? 'info' : 'warning'" effect="plain" round>
                    {{ v.status === 'APPROVED' ? '已批准' : v.status === 'LEFT' ? '已离开' : '待审批' }}
                  </el-tag>
                </div>
                <div class="visitor-desc">
                  <span class="relation-tag text-blue">{{ v.relation }}</span>
                  访问：{{ v.target }}
                </div>
                <div class="visitor-meta">
                  <span class="meta-item"><el-icon><component :is="Clock" /></el-icon> 预计来访: {{ v.time }}</span>
                </div>
              </div>

              <div class="visitor-actions">
                <el-button v-if="v.status === 'PENDING'" type="primary" size="small" @click="handleApprove(v)">批准进入</el-button>
                <el-button v-if="v.status === 'APPROVED'" type="success" size="small" @click="handleLeave(v)">登记离开</el-button>
                <el-button type="primary" link>详情</el-button>
              </div>
            </div>
          </div>
          
          <div class="pagination-wrapper">
             <span class="total-text">共 {{ filteredVisitors.length }} 条</span>
             <el-pagination v-model:current-page="currentPage" background layout="prev, pager, next" :total="filteredVisitors.length" :page-size="pageSize" />
          </div>
        </el-card>
      </el-col>

      <!-- Right Column -->
      <el-col :span="6">
        <!-- Traffic Dashboard -->
        <el-card shadow="never" class="side-card">
          <template #header>
            <div class="card-header"><span>今日流量</span></div>
          </template>
          <div class="dashboard-list">
            <div class="dash-item">
              <div class="dash-icon bg-light-orange"><el-icon><component :is="Clock" /></el-icon></div>
              <div class="dash-info">
                <div class="dash-label">待审批</div>
                <div class="dash-value">{{ pendingCount }} <span>人</span></div>
              </div>
            </div>
            <div class="dash-item">
              <div class="dash-icon bg-light-blue"><el-icon><component :is="User" /></el-icon></div>
              <div class="dash-info">
                <div class="dash-label">当前在馆</div>
                <div class="dash-value">{{ activeCount }} <span>位</span></div>
              </div>
            </div>
            <div class="dash-item">
              <div class="dash-icon bg-gray"><el-icon><component :is="Check" /></el-icon></div>
              <div class="dash-info">
                <div class="dash-label">今日访客</div>
                <div class="dash-value">{{ todayCount }} <span>位</span></div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- Recent Check-ins -->
        <el-card shadow="never" class="side-card recent-card">
          <template #header>
            <div class="card-header"><span>最近签入</span></div>
          </template>
          <div v-if="recentCheckins.length" class="recent-list">
            <div v-for="item in recentCheckins" :key="item.id" class="recent-item">
              <strong>{{ item.name }}</strong>
              <span>{{ item.target }} · {{ item.time }}</span>
            </div>
          </div>
          <el-empty v-else description="暂无签入记录" :image-size="60" />
        </el-card>

        <!-- Visitor Rules -->
        <el-card shadow="never" class="side-card bg-light-blue rules-card">
          <template #header>
            <div class="card-header">
              <span>访客管理规定</span>
              <el-icon><component :is="Info" /></el-icon>
            </div>
          </template>
          <ol class="rules-list">
            <li>访客时间: 08:00 - 22:00，严禁留宿过夜。</li>
            <li>进入楼栋须查验并登记有效身份证件。</li>
            <li>宿管人员有权拒绝无预约或身份不明人员进入。</li>
            <li>离开时须进行签离登记，确保人员出入闭环。</li>
          </ol>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { UserCheck, RefreshCw as Refresh, Search, Clock, User, Check, Info } from '@lucide/vue'
import { getVisitorRecords, saveVisitorRecord } from '../../api/visitor'
import { ElMessage } from 'element-plus'

const search = ref('')
const statusFilter = ref('all')
const currentPage = ref(1)
const pageSize = 3

const visitors = ref([])
const loading = ref(false)

const fetchVisitors = async () => {
  loading.value = true
  try {
    const res = await getVisitorRecords()
    visitors.value = (Array.isArray(res) ? res : (res.data || [])).map(v => ({
      ...v,
      name: v.visitorName,
      target: v.studentName || '未知学生',
      time: v.visitTime ? v.visitTime.replace('T', ' ').substring(0, 16) : '',
      avatarClass: ['av-blue', 'av-green', 'av-yellow', 'av-red'][v.id % 4]
    }))
  } catch (e) { console.error(e);
    ElMessage.error('获取访客记录失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => fetchVisitors())

const handleApprove = async (v) => {
  try {
    await saveVisitorRecord({ id: v.id, status: 'APPROVED' })
    ElMessage.success('已批准进入')
    fetchVisitors()
  } catch (e) { console.error(e);
    ElMessage.error('操作失败')
  }
}

const handleLeave = async (v) => {
  try {
    await saveVisitorRecord({ id: v.id, status: 'LEFT', leaveTime: new Date().toISOString() })
    ElMessage.success('已登记离开')
    fetchVisitors()
  } catch (e) { console.error(e);
    ElMessage.error('操作失败')
  }
}

const pendingCount = computed(() => visitors.value.filter(v => v.status === 'PENDING').length)
const activeCount = computed(() => visitors.value.filter(v => v.status === 'APPROVED' && !v.leaveTime).length)
const todayCount = computed(() => visitors.value.filter(v => new Date(v.createTime).toDateString() === new Date().toDateString()).length)
const filteredVisitors = computed(() => visitors.value.filter(v => {
  const keyword = search.value.trim().toLowerCase()
  const matchesKeyword = !keyword || v.name?.toLowerCase().includes(keyword) || v.phone?.includes(keyword)
  const matchesStatus = statusFilter.value === 'all'
    || (statusFilter.value === 'pending' && v.status === 'PENDING')
    || (statusFilter.value === 'active' && v.status === 'APPROVED' && !v.leaveTime)
  return matchesKeyword && matchesStatus
}))
const pagedVisitors = computed(() => filteredVisitors.value.slice((currentPage.value - 1) * pageSize, currentPage.value * pageSize))
const recentCheckins = computed(() => visitors.value.filter(v => v.status === 'APPROVED' || v.status === 'LEFT').slice(0, 3))
</script>

<style scoped>
.visitor-container {
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

.visitor-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.visitor-item {
  display: flex;
  align-items: center;
  padding: 24px;
  background-color: var(--bg);
  border-radius: 12px;
  transition: background-color 0.2s;
}

.visitor-item:hover {
  background-color: var(--line);
}

.visitor-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 16px;
  font-weight: bold;
  color: white;
  text-align: center;
  line-height: 1.2;
}

.av-yellow { background: #eab308; }
.av-green { background: #10b981; }
.av-red { background: #ef4444; }

.visitor-info {
  flex: 1;
  margin-left: 24px;
}

.visitor-main {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.visitor-name {
  font-size: 16px;
  font-weight: bold;
  color: var(--text);
}

.visitor-phone {
  font-size: 14px;
  color: var(--sub);
}

.visitor-desc {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.relation-tag {
  font-size: 13px;
}

.text-blue { color: #3b82f6; }

.visitor-meta {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: var(--sub);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.visitor-actions {
  display: flex;
  align-items: center;
  gap: 12px;
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

.recent-card {
  min-height: 200px;
}

.recent-list { display: grid; gap: 10px; }
.recent-item { display: grid; gap: 3px; padding: 10px 12px; border: 1px solid var(--line); border-radius: 8px; }
.recent-item strong { color: var(--text); font-size: 13px; }
.recent-item span { color: var(--sub); font-size: 12px; }

.rules-card {
  background-color: var(--primary-2);
  border: 1px solid var(--primary-2) !important;
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
