<template>
  <div class="call-container">
    <el-card shadow="never" class="hero-card">
      <div class="hero-content">
        <div class="hero-text">
          <el-icon :size="28" color="var(--el-color-primary)"><component :is="Video" /></el-icon>
          <div>
            <h2>智能通话</h2>
            <p>预约宿舍事务通话并查看处理进度</p>
          </div>
        </div>
      </div>
    </el-card>

    <el-row :gutter="24">
      <el-col :span="10">
        <el-card shadow="never" class="form-card">
          <template #header>
            <div class="card-header"><span>新建通话预约</span></div>
          </template>
          <el-form :model="form" label-width="80px">
            <el-form-item label="通话事由" required>
              <el-input v-model="form.topic" placeholder="例如：联系宿管确认维修时间" />
            </el-form-item>
            <el-form-item label="联系人" required>
              <el-input v-model="form.targetPerson" placeholder="例如：宿管阿姨" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSubmit" :loading="submitting">提交预约</el-button>
            </el-form-item>
          </el-form>
          <div style="margin-top: 20px;">
            <el-alert title="预约提交后由宿管处理，学生端不能自行改变处理状态。紧急事项请直接联系宿管或值班电话。" type="info" :closable="false" show-icon />
          </div>
        </el-card>
      </el-col>

      <el-col :span="14">
        <el-card shadow="never" class="list-card">
          <template #header>
            <div class="card-header">
              <span>我的通话预约</span>
              <el-button size="small" @click="fetchMyCalls">刷新</el-button>
            </div>
          </template>
          <div v-loading="loading">
            <el-empty v-if="myCalls.length === 0" description="暂无通话预约" />
            <div v-for="c in myCalls" :key="c.id" class="repair-item">
              <div class="repair-badge" style="background: var(--el-color-primary); color: white;">
                <el-icon><Phone /></el-icon>
              </div>
              <div class="repair-info">
                <div class="repair-main">
                  <span class="repair-type">{{ c.topic }}</span>
                  <el-tag size="small" :type="statusTypeMap[c.status]" effect="plain" round>{{ statusMap[c.status] }}</el-tag>
                </div>
                <div class="repair-desc">联系: {{ c.targetPerson }}</div>
                <div class="repair-time">{{ c.createTime ? c.createTime.replace('T', ' ').substring(0, 16) : '' }}</div>
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
import { Video, Phone } from '@lucide/vue'
import { useUserStore } from '../../store/user'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'

const userStore = useUserStore()
const form = ref({ topic: '', targetPerson: '' })
const myCalls = ref([])
const loading = ref(false)
const submitting = ref(false)

const statusMap = { 'PENDING': '待接通', 'ACCEPTED': '已接通', 'FINISHED': '已结束' }
const statusTypeMap = { 'PENDING': 'warning', 'ACCEPTED': 'primary', 'FINISHED': 'info' }

const fetchMyCalls = async () => {
  loading.value = true
  try {
    const res = await request({ url: '/callRecord/list', method: 'get', params: { studentId: userStore.userInfo?.id } })
    myCalls.value = res || []
  } catch (e) { console.error(e);
    ElMessage.error('获取通话记录失败')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!form.value.topic || !form.value.targetPerson) {
    ElMessage.warning('请填写完整信息')
    return
  }
  submitting.value = true
  try {
    await request({
      url: '/callRecord/save',
      method: 'post',
      data: {
        studentId: userStore.userInfo?.id,
        topic: form.value.topic,
        targetPerson: form.value.targetPerson
      }
    })
    ElMessage.success('预约提交成功')
    form.value.topic = ''
    form.value.targetPerson = ''
    fetchMyCalls()
  } catch (e) { console.error(e);
    ElMessage.error('预约提交失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  if (userStore.userInfo?.id) fetchMyCalls()
})
</script>

<style scoped>
.call-container { max-width: 1200px; margin: 0 auto; }
.hero-card { margin-bottom: 24px; border-radius: 12px; background: linear-gradient(135deg, rgba(var(--el-color-primary-rgb), 0.1), rgba(var(--el-color-primary-rgb), 0.02)); border: none; }
.hero-content { display: flex; justify-content: space-between; align-items: center; }
.hero-text { display: flex; align-items: center; gap: 16px; }
.hero-text h2 { margin: 0 0 4px; font-size: 22px; color: var(--text); }
.hero-text p { margin: 0; color: var(--sub); font-size: 14px; }
.form-card, .list-card { border-radius: 12px; height: 100%; border-color: var(--border); }
.card-header { display: flex; justify-content: space-between; align-items: center; font-weight: 600; }
.repair-item { display: flex; gap: 16px; padding: 16px; border: 1px solid var(--border); border-radius: 10px; margin-bottom: 12px; background: var(--bg); transition: all 0.25s ease; }
.repair-item:hover { border-color: var(--el-color-primary); box-shadow: 0 4px 12px rgba(0,0,0,0.05); transform: translateY(-2px); }
.repair-badge { width: 42px; height: 42px; border-radius: 10px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.repair-info { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 6px; }
.repair-main { display: flex; justify-content: space-between; align-items: center; }
.repair-type { font-weight: 600; font-size: 15px; color: var(--text); }
.repair-desc { font-size: 14px; color: var(--sub); display: -webkit-box; -webkit-line-clamp: 2; line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.repair-time { font-size: 12px; color: var(--muted); margin-top: 4px; }
</style>
