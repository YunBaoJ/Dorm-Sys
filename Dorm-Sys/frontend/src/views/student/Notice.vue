<template>
  <div class="notice-page">
    <el-card shadow="never" class="hero-card">
      <div class="hero-content">
        <div>
          <h2>校园公告</h2>
          <p>查看宿管发布的最新通知</p>
        </div>
        <el-button @click="fetchNotices">刷新</el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="list-card">
      <div v-loading="loading">
        <el-empty v-if="notices.length === 0" description="暂无公告" />
        <div v-else class="notice-list">
          <div v-for="notice in notices" :key="notice.id" class="notice-item">
            <div class="notice-head">
              <strong>{{ notice.title }}</strong>
              <el-tag size="small" effect="plain">{{ notice.status }}</el-tag>
            </div>
            <p>{{ notice.description }}</p>
            <div class="notice-meta">
              <span>{{ notice.owner || '全体学生' }}</span>
              <span>{{ formatTime(notice.createTime) }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getBusinessRecords } from '../../api/businessRecord'

const notices = ref([])
const loading = ref(false)

const fetchNotices = async () => {
  loading.value = true
  try {
    notices.value = await getBusinessRecords('manager_messages', '已发布')
  } catch (error) {
    ElMessage.error('获取公告失败')
  } finally {
    loading.value = false
  }
}

const formatTime = (value) => {
  if (!value) return ''
  return value.replace('T', ' ').slice(0, 16)
}

onMounted(() => fetchNotices())
</script>

<style scoped>
.notice-page {
  max-width: 1200px;
  margin: 0 auto;
}

.hero-card,
.list-card {
  border-radius: 12px;
}

.hero-card {
  margin-bottom: 24px;
}

.hero-content,
.notice-head,
.notice-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.hero-content h2 {
  margin: 0 0 4px;
  color: var(--text);
  font-size: 20px;
}

.hero-content p,
.notice-item p,
.notice-meta {
  color: var(--sub);
}

.hero-content p,
.notice-item p {
  margin: 0;
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.notice-item {
  padding: 18px;
  border-radius: 10px;
  background: var(--bg);
}

.notice-head {
  margin-bottom: 10px;
}

.notice-item p {
  line-height: 1.7;
  margin-bottom: 12px;
}

.notice-meta {
  font-size: 13px;
}
</style>
