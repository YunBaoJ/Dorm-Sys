<template>
  <div class="feedback-manager-container">
    <el-card shadow="never" class="hero-card">
      <div class="hero-content">
        <div class="hero-text">
          <el-icon :size="28" color="var(--el-color-primary)"><component :is="MessageCircle" /></el-icon>
          <div>
            <h2>意见反馈处理</h2>
            <p>查看并回复学生的意见与投诉</p>
          </div>
        </div>
        <div class="hero-actions">
          <el-button type="primary" @click="fetchFeedbacks" :loading="loading">
            <el-icon class="el-icon--left"><component :is="RefreshCw" /></el-icon>刷新列表
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="list-card">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="待回复" name="PENDING"></el-tab-pane>
        <el-tab-pane label="已回复" name="REPLIED"></el-tab-pane>
      </el-tabs>

      <el-table :data="filteredFeedbacks" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="反馈类型" width="120" />
        <el-table-column prop="description" label="反馈内容" min-width="250" show-overflow-tooltip />
        <el-table-column prop="createTime" label="提交时间" width="180">
          <template #default="scope">{{ scope.row.createTime ? scope.row.createTime.replace('T', ' ') : '' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button v-if="scope.row.status === 'PENDING'" type="primary" link @click="openReplyDialog(scope.row)">回复</el-button>
            <el-button v-else type="primary" link @click="viewReply(scope.row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 回复弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isViewMode ? '查看反馈' : '回复反馈'" width="500px">
      <div class="feedback-detail">
        <div class="detail-item">
          <span class="label">反馈类型：</span>
          <span class="value">{{ currentRecord.title || currentRecord.type }}</span>
        </div>
        <div class="detail-item">
          <span class="label">反馈内容：</span>
          <span class="value" style="white-space: pre-wrap;">{{ currentRecord.description }}</span>
        </div>
        
        <div class="reply-section" v-if="!isViewMode">
          <el-divider border-style="dashed" />
          <el-input 
            v-model="replyContent" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入回复内容，回复后学生端将立即收到" 
          />
        </div>
        <div class="reply-section view-mode" v-else>
          <el-divider border-style="dashed" />
          <div class="detail-item">
            <span class="label">我的回复：</span>
            <span class="value" style="white-space: pre-wrap;">{{ currentRecord.reply }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">{{ isViewMode ? '关闭' : '取消' }}</el-button>
          <el-button v-if="!isViewMode" type="primary" @click="submitReply" :loading="submitting">发送回复</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { MessageCircle, RefreshCw } from '@lucide/vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'

const feedbacks = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isViewMode = ref(false)
const currentRecord = ref({})
const replyContent = ref('')
const activeTab = ref('PENDING')
const submitting = ref(false)

const filteredFeedbacks = computed(() => {
  return feedbacks.value.filter(f => f.status === activeTab.value)
})

const fetchFeedbacks = async () => {
  loading.value = true
  try {
    const res = await request({ url: '/businessRecord/list', method: 'get', params: { type: 'feedback' } })
    feedbacks.value = res || []
  } catch (e) { console.error(e);
    ElMessage.error('获取列表失败')
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  // tab changes handled by computed property
}

const openReplyDialog = (row) => {
  currentRecord.value = { ...row }
  replyContent.value = ''
  isViewMode.value = false
  dialogVisible.value = true
}

const viewReply = (row) => {
  currentRecord.value = { ...row }
  isViewMode.value = true
  dialogVisible.value = true
}

const submitReply = async () => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  submitting.value = true
  try {
    await request({
      url: '/businessRecord/save',
      method: 'post',
      data: {
        ...currentRecord.value,
        reply: replyContent.value,
        status: 'REPLIED'
      }
    })
    ElMessage.success('回复成功')
    dialogVisible.value = false
    fetchFeedbacks()
  } catch (e) { console.error(e);
    ElMessage.error('回复失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchFeedbacks()
})
</script>

<style scoped>
.feedback-manager-container { max-width: 1200px; margin: 0 auto; }
.hero-card { margin-bottom: 24px; border-radius: 12px; background: linear-gradient(135deg, rgba(var(--el-color-primary-rgb), 0.1), rgba(var(--el-color-primary-rgb), 0.02)); border: none; }
.hero-content { display: flex; justify-content: space-between; align-items: center; }
.hero-text { display: flex; align-items: center; gap: 16px; }
.hero-text h2 { margin: 0 0 4px; font-size: 22px; color: var(--text); }
.hero-text p { margin: 0; color: var(--sub); font-size: 14px; }
.list-card { border-radius: 12px; border: 1px solid var(--border); }
.feedback-detail { padding: 0 10px; }
.detail-item { display: flex; margin-bottom: 12px; line-height: 1.6; }
.detail-item .label { width: 80px; color: var(--sub); flex-shrink: 0; }
.detail-item .value { color: var(--text); flex: 1; }
.reply-section { margin-top: 16px; }
.reply-section.view-mode { background: rgba(var(--el-color-success-rgb), 0.08); padding: 12px; border-radius: 8px; margin-top: 24px; }
</style>
