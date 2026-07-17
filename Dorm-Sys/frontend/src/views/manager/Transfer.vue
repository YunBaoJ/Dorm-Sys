<template>
  <div class="transfer-container">
    <!-- Hero Header -->
    <el-card shadow="never" class="hero-card">
      <div class="hero-content">
        <div class="hero-text">
          <el-icon :size="28" color="var(--el-color-primary)"><component :is="Repeat2" /></el-icon>
          <div>
            <h2>调宿审批</h2>
            <p>规范流转 · 动态调整</p>
          </div>
        </div>
        <el-button type="primary" @click="fetchTransfers"><el-icon class="el-icon--left"><component :is="Refresh" /></el-icon>同步申请单</el-button>
      </div>
    </el-card>

    <el-row :gutter="24">
      <!-- Left Column: Application List -->
      <el-col :span="18">
        <el-card shadow="never" class="list-card">
          <div class="list-header">
            <div class="header-left">
              <span class="list-title">待办列表</span>
              <el-radio-group v-model="statusFilter" size="small" class="status-tabs">
                <el-radio-button label="all">全部</el-radio-button>
                <el-radio-button label="pending">待审批</el-radio-button>
                <el-radio-button label="approved">已通过</el-radio-button>
              </el-radio-group>
            </div>
            <div class="list-actions">
              <el-tag type="info" effect="plain" class="count-tag">共 2 项</el-tag>
            </div>
          </div>

          <div class="transfer-list" v-loading="loading">
            <div v-for="app in applications" :key="app.id" class="transfer-item">
              <div class="item-header">
                <div class="applicant-info">
                  <el-avatar :size="32" :src="app.avatar">{{ app.name?.[0] }}</el-avatar>
                  <span class="applicant-name">{{ app.name }}</span>
                  <span class="applicant-id">{{ app.studentId }}</span>
                </div>
                <el-tag size="small" :type="app.statusType" effect="plain" round>{{ app.statusLabel }}</el-tag>
              </div>

              <div class="transfer-route-box">
                <div class="route-point">
                  <div class="route-label">当前</div>
                  <div class="route-value">{{ app.current }}</div>
                </div>
                <div class="route-arrow">
                  <el-icon color="var(--sub)"><component :is="ArrowRight" /></el-icon>
                </div>
                <div class="route-point">
                  <div class="route-label">期望</div>
                  <div class="route-value">{{ app.target }}</div>
                </div>
              </div>

              <div class="transfer-details">
                <div class="detail-row">
                  <span class="detail-label">申请原因：</span>
                  <span class="detail-content">{{ app.reason }}</span>
                </div>
                <div v-if="app.comment" class="comment-bubble">
                  <el-icon color="#3b82f6"><component :is="MessageSquare" /></el-icon>
                  <span>{{ app.comment }}</span>
                </div>
              </div>

              <div class="item-footer">
                <div class="time-info"><el-icon><component :is="Clock" /></el-icon> {{ app.time }}</div>
                <div v-if="app.status === 'PENDING'">
                  <el-button type="primary" size="small" @click="handleApprove(app)">批准</el-button>
                  <el-button type="danger" size="small" @click="handleReject(app)">驳回</el-button>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- Right Column: Sidebar -->
      <el-col :span="6">
        <!-- Dashboard -->
        <el-card shadow="never" class="side-card">
          <template #header>
            <div class="card-header"><span>申请动态</span></div>
          </template>
          <div class="dashboard-list">
            <div class="dash-item">
              <div class="dash-icon bg-light-orange"><el-icon color="#f59e0b"><component :is="Timer" /></el-icon></div>
              <div class="dash-info">
                <div class="dash-label">等待处理</div>
                <div class="dash-value">{{ pendingCount }} <span>单</span></div>
              </div>
            </div>
            <div class="dash-item">
              <div class="dash-icon bg-light-blue"><el-icon color="#3b82f6"><component :is="CheckCircle2" /></el-icon></div>
              <div class="dash-info">
                <div class="dash-label">已处理</div>
                <div class="dash-value">{{ processedCount }} <span>单</span></div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- Process Timeline -->
        <el-card shadow="never" class="side-card">
          <template #header>
            <div class="card-header"><span>调宿办理流程</span></div>
          </template>
          <div class="process-steps">
            <div class="step-item">
              <div class="step-num">1</div>
              <div class="step-content">
                <div class="step-title">原因核实</div>
                <div class="step-desc">确认调宿理由是否符合规定，核实学生近期表现。</div>
              </div>
            </div>
            <div class="step-item">
              <div class="step-num">2</div>
              <div class="step-content">
                <div class="step-title">目标确认</div>
                <div class="step-desc">检查目标房间是否有空余床位及当前入住学生情况。</div>
              </div>
            </div>
            <div class="step-item">
              <div class="step-num">3</div>
              <div class="step-content">
                <div class="step-title">办理搬离</div>
                <div class="step-desc">指引学生完成原宿舍卫生清扫及钥匙交接。</div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- Notes -->
        <el-card shadow="never" class="side-card bg-light-blue tips-card">
          <template #header>
            <div class="card-header">
              <span>管理备注</span>
              <el-icon><component :is="Info" /></el-icon>
            </div>
          </template>
          <ul class="tips-list">
            <li>调宿周期一般为每学期开学两周内。</li>
            <li>因宿舍矛盾申请者，须先经辅导员调解。</li>
            <li>调宿完成后，水费、电费须及时清算。</li>
          </ul>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Repeat2, RefreshCw as Refresh, ArrowRight, MessageSquare, Clock, Timer, CheckCircle2, Info } from '@lucide/vue'
import { getTransfers, saveTransfer } from '../../api/transfer'
import { ElMessage } from 'element-plus'

const statusFilter = ref('all')
const applications = ref([])
const loading = ref(false)

const statusMap = { 'PENDING': '待审批', 'APPROVED': '已通过', 'REJECTED': '已拒绝' }
const statusTypeMap = { 'PENDING': 'warning', 'APPROVED': 'primary', 'REJECTED': 'danger' }

const fetchTransfers = async () => {
  loading.value = true
  try {
    const statusParam = statusFilter.value === 'all' ? '' : statusFilter.value.toUpperCase()
    const res = await getTransfers(null, statusParam)
    applications.value = (res || []).map(a => ({
      ...a,
      name: a.studentName || '未知',
      current: a.currentBedName || '未知',
      target: a.targetRoomName || '由宿管分配',
      statusLabel: statusMap[a.status] || a.status,
      statusType: statusTypeMap[a.status] || 'info',
      time: a.createTime ? a.createTime.replace('T', ' ').substring(0, 16) : '',
      avatar: ''
    }))
  } catch (e) { console.error(e);
    ElMessage.error('获取调宿申请列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => fetchTransfers())

const handleApprove = async (app) => {
  try {
    await saveTransfer({ ...app, status: 'APPROVED' })
    ElMessage.success('已批准')
    fetchTransfers()
  } catch (e) { console.error(e); ElMessage.error('操作失败') }
}

const handleReject = async (app) => {
  try {
    await saveTransfer({ ...app, status: 'REJECTED' })
    ElMessage.success('已驳回')
    fetchTransfers()
  } catch (e) { console.error(e); ElMessage.error('操作失败') }
}

const pendingCount = computed(() => applications.value.filter(a => a.status === 'PENDING').length)
const processedCount = computed(() => applications.value.filter(a => a.status !== 'PENDING').length)
</script>

<style scoped>
.transfer-container {
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

.count-tag {
  background: var(--surface);
}

.transfer-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.transfer-item {
  border: 1px solid var(--line);
  border-radius: 12px;
  padding: 24px;
  transition: box-shadow 0.2s;
}

.transfer-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.applicant-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.applicant-name {
  font-size: 16px;
  font-weight: bold;
  color: var(--text);
}

.applicant-id {
  font-size: 13px;
  color: var(--sub);
}

.transfer-route-box {
  background-color: var(--bg);
  border-radius: 8px;
  padding: 20px 24px;
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.route-point {
  flex: 1;
}

.route-label {
  font-size: 13px;
  color: var(--sub);
  margin-bottom: 4px;
}

.route-value {
  font-size: 15px;
  font-weight: bold;
  color: var(--text);
}

.route-arrow {
  padding: 0 24px;
  display: flex;
  align-items: center;
}

.transfer-details {
  margin-bottom: 20px;
}

.detail-row {
  font-size: 14px;
  margin-bottom: 12px;
}

.detail-label {
  color: var(--sub);
}

.detail-content {
  color: var(--text-secondary);
}

.comment-bubble {
  display: flex;
  align-items: center;
  gap: 8px;
  background-color: var(--primary-2);
  color: #3b82f6;
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 13px;
}

.item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px dashed var(--line);
}

.time-info {
  display: flex;
  align-items: center;
  gap: 6px;
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
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 20px;
}

.bg-light-blue { background: var(--primary-2); }
.bg-light-orange { background: var(--orange-2); }

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

.process-steps {
  position: relative;
  padding-left: 12px;
}

.process-steps::before {
  content: '';
  position: absolute;
  top: 16px;
  bottom: 16px;
  left: 23px;
  width: 2px;
  background: var(--line);
}

.step-item {
  position: relative;
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.step-item:last-child {
  margin-bottom: 0;
}

.step-num {
  position: relative;
  z-index: 2;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: var(--primary-2);
  color: #3b82f6;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  flex-shrink: 0;
}

.step-content {
  flex: 1;
  padding-top: 2px;
}

.step-title {
  font-size: 14px;
  font-weight: bold;
  color: var(--text);
  margin-bottom: 4px;
}

.step-desc {
  font-size: 13px;
  color: var(--sub);
  line-height: 1.5;
}

.tips-card {
  background-color: var(--primary-2);
  border: 1px solid var(--primary-2) !important;
}

.tips-list {
  margin: 0;
  padding-left: 20px;
  font-size: 13px;
  color: var(--text);
  line-height: 2;
}

.tips-list li::marker {
  color: #3b82f6;
}
</style>
