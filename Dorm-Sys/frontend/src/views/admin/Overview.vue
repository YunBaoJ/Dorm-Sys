<template>
  <div class="overview-container">
    <el-row :gutter="24">
      <!-- Left Column -->
      <el-col :span="16">
        <!-- Hero Card -->
        <el-card shadow="never" class="hero-card">
          <div class="hero-content">
            <div class="hero-text">
              <el-icon :size="28" color="var(--el-color-primary)"><component :is="Monitor" /></el-icon>
              <div>
                <h2>系统管理看板</h2>
                <p>全局数据监控 · 资源动态调度</p>
              </div>
            </div>
            <div class="hero-actions" style="display: flex; align-items: center; gap: 24px;">
              <WeatherWidget />
              <div class="hero-time">
                <div class="time-main">22:18</div>
                <div class="time-sub">3月27日星期五</div>
              </div>
            </div>
          </div>
        </el-card>

        <el-alert v-if="loadError" type="error" :closable="false" show-icon class="load-error">
          <template #title>
            <span>管理看板数据加载失败</span>
            <el-button type="danger" link @click="fetchData">重新加载</el-button>
          </template>
        </el-alert>

        <!-- Alerts -->
        <div class="alerts-wrapper" v-loading="loading">
          <el-alert
            v-for="(alert, index) in alerts"
            :key="index"
            :title="alert.title"
            :type="alert.type"
            show-icon
            :closable="false"
            class="custom-alert"
          >
            <template #title>
              <span class="alert-text">{{ alert.title }}</span>
              <el-button size="small" :type="alert.type" plain class="alert-btn" @click="$router.push(alert.url)">{{ alert.action }}</el-button>
            </template>
          </el-alert>
          <el-empty v-if="alerts.length === 0" description="暂无系统异常预警" :image-size="60"></el-empty>
        </div>

        <!-- Building Distribution -->
        <el-card shadow="never" class="distribution-card">
          <template #header>
            <div class="card-header">
              <span>楼栋入住实时分布</span>
              <el-radio-group v-model="viewMode" size="small">
                <el-radio-button label="grid">网格视图</el-radio-button>
                <el-radio-button label="list">列表视图</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          
          <el-row :gutter="20" v-loading="loading">
            <el-col :span="viewMode === 'grid' ? 12 : 24" v-for="b in pagedBuildings" :key="b.id" style="margin-bottom: 16px;">
              <div class="building-item" :style="viewMode === 'list' ? 'display: flex; align-items: center; gap: 20px;' : ''">
                <div class="building-head" :style="viewMode === 'list' ? 'margin-bottom: 0; min-width: 120px;' : ''">
                  <span class="building-name">{{ b.name }}</span>
                  <span class="building-percent">{{ b.percentage }}%</span>
                </div>
                <el-progress :percentage="b.percentage" :show-text="false" :stroke-width="8" color="#3b82f6" :style="viewMode === 'list' ? 'flex: 1;' : ''" />
                <div class="building-foot" :style="viewMode === 'list' ? 'margin-top: 0; min-width: 150px; justify-content: flex-end; gap: 12px;' : ''">
                  <span class="building-meta">{{ b.occupiedBeds }}/{{ b.totalBeds }} 床</span>
                  <el-tag size="small" type="primary" effect="plain">{{ b.status }}</el-tag>
                </div>
              </div>
            </el-col>
          </el-row>
          <div style="display: flex; justify-content: flex-end; margin-top: 8px;" v-if="buildings.length > pageSize">
            <el-pagination
              background
              layout="prev, pager, next"
              :total="buildings.length"
              :page-size="pageSize"
              v-model:current-page="currentPage"
              @current-change="handleCurrentChange"
            />
          </div>
        </el-card>

      </el-col>

      <!-- Right Column -->
      <el-col :span="8">
        
        <!-- Business Overview -->
        <el-card shadow="never" class="side-card">
          <template #header>
            <div class="card-header"><span>业务概览</span></div>
          </template>
          <div class="biz-list" v-loading="loading">
            <div class="biz-item">
              <div class="biz-icon bg-blue"><el-icon><component :is="User" /></el-icon></div>
              <div class="biz-info">
                <div class="biz-label">注册学生</div>
                <div class="biz-value">{{ stats.studentCount || 0 }} <span>人</span></div>
              </div>
            </div>
            <div class="biz-item">
              <div class="biz-icon bg-orange"><el-icon><component :is="Users" /></el-icon></div>
              <div class="biz-info">
                <div class="biz-label">宿管团队</div>
                <div class="biz-value">{{ stats.managerCount || 0 }} <span>位</span></div>
              </div>
            </div>
            <div class="biz-item">
              <div class="biz-icon bg-cyan"><el-icon><component :is="Building" /></el-icon></div>
              <div class="biz-info">
                <div class="biz-label">管辖楼栋</div>
                <div class="biz-value">{{ stats.buildingCount || 0 }} <span>栋</span></div>
              </div>
            </div>
            <div class="biz-item">
              <div class="biz-icon bg-yellow"><el-icon><component :is="Home" /></el-icon></div>
              <div class="biz-info">
                <div class="biz-label">宿舍房间</div>
                <div class="biz-value">{{ stats.roomCount || 0 }} <span>间</span></div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- Resource Monitoring -->
        <el-card shadow="never" class="side-card">
          <template #header>
            <div class="card-header"><span>资源监控</span></div>
          </template>
          <div class="resource-list">
            <div class="res-item">
              <div class="res-head">
                <span>数据库连接</span>
                <span class="res-status text-blue">已连接</span>
              </div>
              <div class="resource-note">业务统计由 MySQL 实时汇总</div>
            </div>
            <div class="res-item">
              <div class="res-head">
                <span>自助问答</span>
                <span class="res-status text-blue">可用</span>
              </div>
              <div class="resource-note">本地宿舍业务规则库</div>
            </div>
            <div class="res-item">
              <div class="res-head">
                <span>接口鉴权</span>
                <span class="res-status text-blue">已启用</span>
              </div>
              <div class="resource-note">JWT 与角色数据范围校验</div>
            </div>
          </div>
        </el-card>

        <!-- Management Workbench -->
        <el-card shadow="never" class="side-card">
          <template #header>
            <div class="card-header"><span>管理工作台</span></div>
          </template>
          <div class="workbench-grid">
            <div class="wb-btn" @click="router.push('/admin/users/list')">
              <el-icon :size="24" color="#3b82f6"><component :is="User" /></el-icon>
              <span>用户</span>
            </div>
            <div class="wb-btn" @click="router.push('/admin/resources/buildings')">
              <el-icon :size="24" color="#3b82f6"><component :is="Building" /></el-icon>
              <span>楼栋</span>
            </div>
            <div class="wb-btn" @click="router.push('/admin/resources/rooms')">
              <el-icon :size="24" color="#3b82f6"><component :is="Home" /></el-icon>
              <span>房间</span>
            </div>
          </div>
        </el-card>

      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Monitor, User, Users, Building, Home } from '@lucide/vue'
import request from '../../utils/request'
import WeatherWidget from '../../components/WeatherWidget.vue'
import { getStats, getBuildingStats, getAlerts } from '../../api/dashboard'

const router = useRouter()
const viewMode = ref('grid')
const loading = ref(false)
const loadError = ref(false)

const stats = ref({})
const buildings = ref([])
const alerts = ref([])

const currentPage = ref(1)
const pageSize = ref(4)

const pagedBuildings = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return buildings.value.slice(start, end)
})

const handleCurrentChange = (val) => {
  currentPage.value = val
}

const fetchData = async () => {
  loading.value = true
  loadError.value = false
  try {
    const [statsRes, buildingsRes, alertsRes] = await Promise.all([
      getStats(),
      getBuildingStats(),
      getAlerts()
    ])
    stats.value = statsRes || {}
    buildings.value = (buildingsRes || []).map((building) => {
      const occupiedBeds = Number(building.occupiedBeds) || 0
      const totalBeds = Number(building.totalBeds) || 0
      const percentage = Number.isFinite(Number(building.percentage))
        ? Number(building.percentage)
        : (totalBeds ? Math.round((occupiedBeds / totalBeds) * 100) : 0)

      return {
        ...building,
        occupiedBeds,
        totalBeds,
        percentage: Math.min(100, Math.max(0, percentage)),
        status: building.status || (percentage >= 100 ? '已满' : percentage >= 80 ? '紧张' : '正常')
      }
    })
    alerts.value = alertsRes || []
  } catch (error) {
    console.error('Failed to fetch dashboard data', error)
    loadError.value = true
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.overview-container {
  max-width: 1200px;
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

.hero-time {
  text-align: right;
}

.time-main {
  font-size: 32px;
  font-weight: bold;
  color: var(--text);
  line-height: 1;
  margin-bottom: 4px;
}

.time-sub {
  font-size: 13px;
  color: var(--sub);
}

.alerts-wrapper {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
}

.custom-alert {
  border-radius: 8px;
}

.custom-alert :deep(.el-alert__content) {
  width: 100%;
}

.custom-alert :deep(.el-alert__title) {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.distribution-card {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.building-item {
  background: var(--bg);
  padding: 16px;
  border-radius: 12px;
}

.building-head {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.building-name {
  font-weight: 600;
  color: var(--text);
}

.building-percent {
  font-weight: bold;
  color: var(--el-color-primary);
}

.building-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.building-meta {
  font-size: 13px;
  color: var(--sub);
}

.side-card {
  margin-bottom: 24px;
}

.biz-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.biz-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: var(--bg);
  border-radius: 12px;
}

.biz-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 24px;
}

.bg-blue { background: var(--primary-2); color: #3b82f6; }
.bg-orange { background: var(--orange-2); color: #f97316; }
.bg-cyan { background: var(--info-2); color: #06b6d4; }
.bg-yellow { background: var(--warn-2); color: #eab308; }

.biz-info {
  flex: 1;
}

.biz-label {
  font-size: 13px;
  color: var(--sub);
  margin-bottom: 4px;
}

.biz-value {
  font-size: 20px;
  font-weight: bold;
  color: var(--text);
}

.biz-value span {
  font-size: 13px;
  font-weight: normal;
  color: var(--sub);
}

.resource-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.res-head {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.load-error {
  margin-bottom: 16px;
}

.resource-note {
  font-size: 12px;
  color: var(--sub);
  line-height: 1.5;
}

.text-blue { color: #3b82f6; }

.workbench-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.wb-btn {
  background: var(--bg);
  border-radius: 12px;
  padding: 16px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.wb-btn:hover {
  background: var(--line);
}

.wb-btn span {
  font-size: 13px;
  color: var(--text-secondary);
}
</style>
