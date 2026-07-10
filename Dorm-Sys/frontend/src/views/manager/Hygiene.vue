<template>
  <div class="hygiene-container">
    <!-- Hero Header -->
    <el-card shadow="never" class="hero-card">
      <div class="hero-content">
        <div class="hero-text">
          <el-icon :size="28" color="var(--el-color-primary)"><component :is="Medal" /></el-icon>
          <div>
            <h2>卫生检查</h2>
            <p>营造整洁环境 · 共建文明宿舍</p>
          </div>
        </div>
        <el-button type="primary"><el-icon class="el-icon--left"><component :is="Plus" /></el-icon>录入检查结果</el-button>
      </div>
    </el-card>

    <el-row :gutter="24">
      <!-- Left Column: Overview -->
      <el-col :span="8">
        <!-- Weekly Overview -->
        <el-card shadow="never" class="side-card">
          <template #header>
            <div class="card-header">
              <span>本周概览</span>
              <el-icon><component :is="BarChart2" /></el-icon>
            </div>
          </template>
          
          <div class="stats-container">
            <div class="dash-icon bg-light-blue"><el-icon><component :is="Medal" /></el-icon></div>
            <div class="dash-info">
              <div class="dash-label">平均得分</div>
              <div class="dash-value">{{ avgScore }} <span>分</span></div>
            </div>
            <div class="stat-item">
              <div class="stat-label">合格率</div>
              <div class="stat-val text-orange">98%</div>
              <div class="stat-trend trend-down">
                <el-icon><component :is="ArrowDown" /></el-icon> 0.5%
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-label">优秀宿舍</div>
              <div class="stat-val text-dark">15</div>
              <div class="stat-trend text-gray">间</div>
            </div>
          </div>

          <el-divider border-style="dashed" />

          <!-- Scoring Criteria -->
          <div class="criteria-section">
            <div class="section-title">评分参考标准</div>
            <el-collapse v-model="activeCriteria" accordion class="custom-collapse">
              <el-collapse-item title="地面卫生 (30分)" name="1">
                <div class="criteria-desc">地面无纸屑、积水、痰迹；鞋架摆放整齐。</div>
              </el-collapse-item>
              <el-collapse-item title="内务整理 (40分)" name="2">
                <div class="criteria-desc">床铺平整，被褥叠放统一；桌面物品摆放有序。</div>
              </el-collapse-item>
              <el-collapse-item title="安全用电 (30分)" name="3">
                <div class="criteria-desc">无违规电器，人走断电；插座无私拉乱接现象。</div>
              </el-collapse-item>
            </el-collapse>
          </div>
        </el-card>

        <!-- Management Tips -->
        <el-card shadow="never" class="side-card bg-light-blue tips-card">
          <template #header>
            <div class="card-header">
              <span class="text-primary">管理贴士</span>
            </div>
          </template>
          <ol class="tips-list">
            <li>检查时请至少两名宿管人员或学生干部在场。</li>
            <li>对于评分低于 60 分的宿舍，需下发整改通知。</li>
            <li>连续三次优秀的宿舍可获"文明寝室"称号。</li>
          </ol>
        </el-card>
      </el-col>

      <!-- Right Column: Records -->
      <el-col :span="16">
        <el-card shadow="never" class="list-card">
          <div class="list-header">
            <div class="header-left">
              <span class="list-title">检查记录</span>
              <el-select v-model="buildingFilter" placeholder="筛选楼栋" style="width: 140px; margin-left: 16px;">
                <el-option label="全部楼栋" value="all" />
                <el-option label="明德楼" value="mingde" />
                <el-option label="至善楼" value="zhishan" />
              </el-select>
            </div>
            <div class="list-actions">
              <el-button icon="Refresh" circle style="margin-left: 12px" @click="fetchRecords" />
            </div>
          </div>

          <div class="record-list" v-loading="loading">
            <el-empty v-if="records.length === 0" description="暂无检查记录" />
            <div v-for="record in records" :key="record.id" class="record-item">
              <div class="score-badge" :class="getScoreClass(record.score)">
                <span class="score-val">{{ record.score }}</span>
                <span class="score-unit">分</span>
              </div>
              
              <div class="record-info">
                <div class="record-main">
                  <span class="record-room">{{ record.room }}</span>
                  <el-tag size="small" :type="record.score >= 90 ? 'primary' : 'warning'" effect="plain" round>
                    {{ record.score >= 90 ? '优秀' : (record.score >= 80 ? '良好' : '及格') }}
                  </el-tag>
                </div>
                <div class="record-desc">{{ record.comment || record.description || '无备注' }}</div>
                <div class="record-meta">
                  <span class="meta-item"><el-icon><component :is="User" /></el-icon> {{ record.inspector }}</span>
                  <span class="meta-item"><el-icon><component :is="Clock" /></el-icon> {{ record.time }}</span>
                </div>
              </div>

              <div class="record-actions">
                <el-button type="danger" link @click="handleDelete(record.id)"><el-icon class="el-icon--left"><component :is="Trash2" /></el-icon>删除</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { Medal, Plus, BarChart2, ArrowUp, ArrowDown, RefreshCw as Refresh, User, Clock, Trash2 } from '@lucide/vue'
import { getHygieneRecords, deleteHygieneRecord } from '../../api/hygiene'
import { ElMessage, ElMessageBox } from 'element-plus'

const activeCriteria = ref('1')
const buildingFilter = ref('')

const records = ref([])
const loading = ref(false)

const fetchRecords = async () => {
  loading.value = true
  try {
    const res = await getHygieneRecords()
    records.value = (Array.isArray(res) ? res : (res.data || [])).map(r => ({
      ...r,
      room: r.roomName || '未知房间',
      inspector: r.inspectorName || '未知',
      time: r.checkDate ? r.checkDate.replace('T', ' ') : ''
    }))
  } catch (e) {
    ElMessage.error('获取卫生检查记录失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => fetchRecords())

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条记录吗？', '提示', { type: 'warning' })
    await deleteHygieneRecord(id)
    ElMessage.success('删除成功')
    fetchRecords()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const avgScore = computed(() => {
  if (!records.value.length) return 0
  const sum = records.value.reduce((acc, curr) => acc + curr.score, 0)
  return (sum / records.value.length).toFixed(1)
})

const getScoreClass = (score) => {
  if (score >= 90) return 'score-blue'
  if (score >= 80) return 'score-orange'
  return 'score-red'
}
</script>

<style scoped>
.hygiene-container {
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

.side-card {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.stats-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding: 16px 0;
  align-items: center;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.stat-label {
  font-size: 14px;
  color: var(--sub);
}

.stat-val {
  font-size: 32px;
  font-weight: bold;
  line-height: 1;
}

.text-blue { color: #3b82f6; }
.text-orange { color: #f59e0b; }
.text-dark { color: var(--text); }

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 500;
}

.trend-up { color: #3b82f6; }
.trend-down { color: #f59e0b; }
.text-gray { color: var(--sub); font-weight: normal; }

.section-title {
  font-size: 15px;
  font-weight: bold;
  color: var(--text);
  margin-bottom: 16px;
}

.custom-collapse {
  border-top: none;
  border-bottom: none;
}

.custom-collapse :deep(.el-collapse-item__header) {
  border-bottom: none;
  font-size: 14px;
  color: var(--text-secondary);
}

.custom-collapse :deep(.el-collapse-item__wrap) {
  border-bottom: none;
}

.criteria-desc {
  font-size: 13px;
  color: var(--sub);
  line-height: 1.6;
  background-color: var(--bg);
  padding: 12px;
  border-radius: 8px;
}

.tips-card {
  background-color: var(--primary-2);
  border: 1px solid var(--primary-2) !important;
}

.text-primary {
  color: #3b82f6;
  font-weight: bold;
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
}

.list-title {
  font-size: 16px;
  font-weight: bold;
  color: var(--text);
}

.record-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.record-item {
  display: flex;
  align-items: center;
  padding: 24px;
  background-color: var(--bg);
  border-radius: 12px;
  transition: background-color 0.2s;
}

.record-item:hover {
  background-color: var(--line);
}

.score-badge {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: var(--surface);
  border: 2px solid;
}

.score-val {
  font-size: 24px;
  font-weight: bold;
  line-height: 1;
}

.score-unit {
  font-size: 12px;
  margin-top: 4px;
}

.score-blue {
  border-color: var(--primary-2);
  color: #3b82f6;
}

.score-orange {
  border-color: var(--orange-2);
  color: #f59e0b;
}

.score-red {
  border-color: #fef2f2;
  color: #ef4444;
}

.record-info {
  flex: 1;
  margin-left: 24px;
}

.record-main {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.record-room {
  font-size: 16px;
  font-weight: bold;
  color: var(--text);
}

.record-desc {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 12px;
}

.record-meta {
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
</style>
