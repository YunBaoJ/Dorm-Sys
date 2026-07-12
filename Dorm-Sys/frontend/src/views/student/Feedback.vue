<template>
  <div class="feedback-container">
    <el-card shadow="never" class="hero-card">
      <div class="hero-content">
        <div class="hero-text">
          <el-icon :size="28" color="var(--el-color-primary)"><component :is="MessageCircle" /></el-icon>
          <div>
            <h2>意见反馈</h2>
            <p>向宿管提交您的意见、建议或投诉</p>
          </div>
        </div>
      </div>
    </el-card>

    <el-row :gutter="24">
      <el-col :span="10">
        <el-card shadow="never" class="form-card">
          <template #header>
            <div class="card-header"><span>填写反馈</span></div>
          </template>
          <el-form :model="form" label-width="80px">
            <el-form-item label="反馈类型" required>
              <el-select v-model="form.type" placeholder="请选择" style="width: 100%">
                <el-option label="意见建议" value="意见建议" />
                <el-option label="投诉举报" value="投诉举报" />
                <el-option label="生活求助" value="生活求助" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
            <el-form-item label="反馈内容" required>
              <el-input v-model="form.content" type="textarea" :rows="5" placeholder="请详细描述您的反馈内容" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSubmit" :loading="submitting">提交反馈</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="14">
        <el-card shadow="never" class="list-card">
          <template #header>
            <div class="card-header">
              <span>历史反馈</span>
              <el-button size="small" @click="fetchMyFeedbacks">刷新</el-button>
            </div>
          </template>
          <div v-loading="loading">
            <el-empty v-if="myFeedbacks.length === 0" description="暂无历史反馈" />
            <div v-for="f in myFeedbacks" :key="f.id" class="feedback-item">
              <div class="feedback-head">
                <div class="feedback-type">{{ f.type }}</div>
                <el-tag size="small" :type="f.status === 'REPLIED' ? 'success' : 'info'" effect="plain" round>
                  {{ f.status === 'REPLIED' ? '已回复' : '未读' }}
                </el-tag>
              </div>
              <div class="feedback-content">{{ f.content }}</div>
              <div class="feedback-time">提交于 {{ f.createTime ? f.createTime.replace('T', ' ').substring(0, 16) : '' }}</div>
              <div v-if="f.status === 'REPLIED'" class="feedback-reply">
                <strong>宿管回复：</strong>
                <p>{{ f.reply }}</p>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { MessageCircle } from '@lucide/vue'
import { useUserStore } from '../../store/user'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'

const userStore = useUserStore()
const form = ref({ type: '意见建议', content: '' })
const myFeedbacks = ref([])
const loading = ref(false)
const submitting = ref(false)

const fetchMyFeedbacks = async () => {
  loading.value = true
  try {
    const res = await request({ url: '/feedback/list', method: 'get', params: { studentId: userStore.userInfo?.id } })
    myFeedbacks.value = res || []
  } catch (e) {
    ElMessage.error('获取反馈记录失败')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!form.value.content) {
    ElMessage.warning('请填写反馈内容')
    return
  }
  submitting.value = true
  try {
    await request({
      url: '/feedback/add',
      method: 'post',
      data: {
        studentId: userStore.userInfo?.id,
        type: form.value.type,
        content: form.value.content
      }
    })
    ElMessage.success('反馈提交成功')
    form.value.content = ''
    fetchMyFeedbacks()
  } catch (e) {
    ElMessage.error('反馈提交失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  if (userStore.userInfo?.id) fetchMyFeedbacks()
})
</script>

<style scoped>
.feedback-container { max-width: 1200px; margin: 0 auto; }
.hero-card { margin-bottom: 24px; border-radius: 12px; background: linear-gradient(135deg, rgba(var(--el-color-primary-rgb), 0.1), rgba(var(--el-color-primary-rgb), 0.02)); border: none; }
.hero-content { display: flex; justify-content: space-between; align-items: center; }
.hero-text { display: flex; align-items: center; gap: 16px; }
.hero-text h2 { margin: 0 0 4px; font-size: 22px; color: var(--text); }
.hero-text p { margin: 0; color: var(--sub); font-size: 14px; }
.form-card, .list-card { border-radius: 12px; height: 100%; border-color: var(--border); }
.card-header { display: flex; justify-content: space-between; align-items: center; font-weight: 600; }
.feedback-item { padding: 18px; border: 1px solid var(--border); border-radius: 10px; margin-bottom: 16px; background: var(--bg); transition: all 0.25s ease; }
.feedback-item:hover { border-color: var(--el-color-primary); box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
.feedback-head { display: flex; justify-content: space-between; margin-bottom: 12px; align-items: center; }
.feedback-type { font-weight: 600; font-size: 16px; color: var(--text); }
.feedback-content { font-size: 14px; color: var(--text); line-height: 1.6; margin-bottom: 12px; }
.feedback-time { font-size: 12px; color: var(--muted); }
.feedback-reply { margin-top: 16px; padding: 12px 16px; background: rgba(var(--el-color-success-rgb), 0.08); border-radius: 8px; font-size: 14px; line-height: 1.6; }
.feedback-reply strong { color: var(--el-color-success); display: block; margin-bottom: 6px; }
.feedback-reply p { margin: 0; color: var(--text); }
</style>
