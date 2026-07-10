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
            <div class="hero-time">
              <div class="time-main">22:18</div>
              <div class="time-sub">3月27日星期五</div>
            </div>
          </div>
        </el-card>

        <!-- Alerts -->
        <div class="alerts-wrapper">
          <el-alert
            title="3号楼当前有 12 个电力故障报修超过 24 小时未处理。"
            type="warning"
            show-icon
            :closable="false"
            class="custom-alert"
          >
            <template #title>
              <span class="alert-text">3号楼当前有 12 个电力故障报修超过 24 小时未处理。</span>
              <el-button size="small" type="warning" plain class="alert-btn">立即处理</el-button>
            </template>
          </el-alert>
          <el-alert
            title="异常晚归预警"
            type="error"
            show-icon
            :closable="false"
            class="custom-alert"
          >
            <template #title>
              <span class="alert-text">异常晚归预警</span>
              <el-button size="small" type="danger" plain class="alert-btn">立即处理</el-button>
            </template>
          </el-alert>
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
          
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="building-item">
                <div class="building-head">
                  <span class="building-name">明德楼</span>
                  <span class="building-percent">60%</span>
                </div>
                <el-progress :percentage="60" :show-text="false" :stroke-width="8" color="#3b82f6" />
                <div class="building-foot">
                  <span class="building-meta">12/20 床</span>
                  <el-tag size="small" type="primary" effect="plain">运行正常</el-tag>
                </div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="building-item">
                <div class="building-head">
                  <span class="building-name">至善楼</span>
                  <span class="building-percent">33%</span>
                </div>
                <el-progress :percentage="33" :show-text="false" :stroke-width="8" color="#3b82f6" />
                <div class="building-foot">
                  <span class="building-meta">4/12 床</span>
                  <el-tag size="small" type="primary" effect="plain">运行正常</el-tag>
                </div>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <!-- System Dynamics -->
        <el-card shadow="never" class="timeline-card">
          <template #header>
            <div class="card-header">
              <span>最近系统动态</span>
              <el-button type="primary" link>查看全部</el-button>
            </div>
          </template>
          <el-timeline>
            <el-timeline-item
              v-for="(activity, index) in activities"
              :key="index"
              :type="activity.type"
              :color="activity.color"
              :size="activity.size"
            >
              <div class="timeline-content">
                <div class="timeline-main">
                  <div class="timeline-title">{{ activity.content }}</div>
                  <div class="timeline-time">{{ activity.timestamp }}</div>
                </div>
                <el-tag size="small" type="info" class="timeline-tag">{{ activity.tag }}</el-tag>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-card>

      </el-col>

      <!-- Right Column -->
      <el-col :span="8">
        
        <!-- Business Overview -->
        <el-card shadow="never" class="side-card">
          <template #header>
            <div class="card-header"><span>业务概览</span></div>
          </template>
          <div class="biz-list">
            <div class="biz-item">
              <div class="biz-icon bg-blue"><el-icon><component :is="User" /></el-icon></div>
              <div class="biz-info">
                <div class="biz-label">注册学生</div>
                <div class="biz-value">16 <span>人</span></div>
              </div>
            </div>
            <div class="biz-item">
              <div class="biz-icon bg-orange"><el-icon><component :is="Users" /></el-icon></div>
              <div class="biz-info">
                <div class="biz-label">宿管团队</div>
                <div class="biz-value">2 <span>位</span></div>
              </div>
            </div>
            <div class="biz-item">
              <div class="biz-icon bg-cyan"><el-icon><component :is="Building" /></el-icon></div>
              <div class="biz-info">
                <div class="biz-label">管辖楼栋</div>
                <div class="biz-value">2 <span>栋</span></div>
              </div>
            </div>
            <div class="biz-item">
              <div class="biz-icon bg-yellow"><el-icon><component :is="Home" /></el-icon></div>
              <div class="biz-info">
                <div class="biz-label">宿舍房间</div>
                <div class="biz-value">20 <span>间</span></div>
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
                <span class="res-status text-blue">正常</span>
              </div>
              <el-progress :percentage="30" :show-text="false" color="#3b82f6" />
            </div>
            <div class="res-item">
              <div class="res-head">
                <span>AI 服务负载</span>
                <span class="res-status text-blue">低负荷</span>
              </div>
              <el-progress :percentage="15" :show-text="false" color="#f59e0b" />
            </div>
            <div class="res-item">
              <div class="res-head">
                <span>系统存储空间</span>
                <span class="res-status text-blue">充足</span>
              </div>
              <el-progress :percentage="45" :show-text="false" color="#3b82f6" />
            </div>
          </div>
        </el-card>

        <!-- Management Workbench -->
        <el-card shadow="never" class="side-card">
          <template #header>
            <div class="card-header"><span>管理工作台</span></div>
          </template>
          <div class="workbench-grid">
            <div class="wb-btn">
              <el-icon :size="24" color="#3b82f6"><component :is="User" /></el-icon>
              <span>用户</span>
            </div>
            <div class="wb-btn">
              <el-icon :size="24" color="#3b82f6"><component :is="Building" /></el-icon>
              <span>楼栋</span>
            </div>
            <div class="wb-btn">
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
import { ref } from 'vue'
import { Monitor, User, Users, Building, Home } from '@lucide/vue'

const viewMode = ref('grid')

const activities = [
  {
    content: '管理员修改了 3号楼 的入住配置',
    timestamp: '10:24',
    color: '#3b82f6',
    tag: 'admin'
  },
  {
    content: '系统自动拦截了一次异常登录尝试',
    timestamp: '09:45',
    color: '#f59e0b',
    tag: 'system'
  },
  {
    content: '新增了 5 名宿管人员账号',
    timestamp: '08:30',
    color: '#3b82f6',
    tag: 'admin'
  },
  {
    content: '发布了关于寒假留校的通知公告',
    timestamp: '昨天',
    color: '#3b82f6',
    tag: 'admin'
  }
]
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

.timeline-card {
  margin-bottom: 24px;
}

.timeline-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.timeline-main {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.timeline-title {
  color: var(--text-secondary);
  font-size: 14px;
}

.timeline-time {
  color: var(--sub);
  font-size: 12px;
}

.timeline-tag {
  background-color: var(--line);
  border-color: var(--line);
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
