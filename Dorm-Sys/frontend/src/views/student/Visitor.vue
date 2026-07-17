<template>
  <div class="visitor-container">
    <!-- Hero Header -->
    <el-card shadow="never" class="hero-card">
      <div class="hero-content">
        <div class="hero-text">
          <el-icon :size="28" color="var(--el-color-primary)"><component :is="Users2" /></el-icon>
          <div>
            <h2>访客登记</h2>
            <p>提前申报 · 安全通行</p>
          </div>
        </div>
      </div>
    </el-card>

    <el-row :gutter="24">
      <!-- Left: Form -->
      <el-col :span="10">
        <el-card shadow="never" class="form-card">
          <template #header>
            <div class="card-header"><span>登记来访信息</span></div>
          </template>
          <el-form :model="form" label-width="80px">
            <el-form-item label="访客姓名" required>
              <el-input v-model="form.visitorName" placeholder="请输入访客姓名" />
            </el-form-item>
            <el-form-item label="联系电话">
              <el-input v-model="form.phone" placeholder="请输入手机号码" />
            </el-form-item>
            <el-form-item label="与您关系">
              <el-select v-model="form.relation" placeholder="请选择" style="width: 100%">
                <el-option label="家人" value="家人" />
                <el-option label="朋友" value="朋友" />
                <el-option label="同学" value="同学" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
            <el-form-item label="来访时间" required>
              <el-date-picker v-model="form.visitTime" type="datetime" placeholder="请选择来访时间" style="width: 100%" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSubmit" :loading="submitting">提交登记</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- Right: My Records -->
      <el-col :span="14">
        <el-card shadow="never" class="list-card">
          <template #header>
            <div class="card-header">
              <span>我的来访记录</span>
              <el-button size="small" @click="fetchMyVisitors">刷新</el-button>
            </div>
          </template>
          <div v-loading="loading">
            <el-empty v-if="myVisitors.length === 0" description="暂无来访记录" />
            <div v-for="v in myVisitors" :key="v.id" class="visitor-item">
              <el-avatar :size="40" style="background: var(--el-color-primary);">{{ v.visitorName?.[0] }}</el-avatar>
              <div class="visitor-info">
                <div class="visitor-main">
                  <span class="visitor-name">{{ v.visitorName }}</span>
                  <el-tag size="small" :type="v.status === 'APPROVED' ? 'success' : v.status === 'LEFT' ? 'info' : 'warning'" effect="plain" round>
                    {{ v.status === 'APPROVED' ? '已批准' : v.status === 'LEFT' ? '已离开' : '待审批' }}
                  </el-tag>
                </div>
                <div class="visitor-meta">
                  <span>{{ v.relation }} · {{ v.phone || '无电话' }}</span>
                </div>
                <div class="visitor-time">来访: {{ v.visitTime ? v.visitTime.replace('T', ' ').substring(0, 16) : '' }}</div>
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
import { Users2 } from '@lucide/vue'
import { useUserStore } from '../../store/user'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'

const userStore = useUserStore()
const form = ref({ visitorName: '', phone: '', relation: '朋友', visitTime: '' })
const myVisitors = ref([])
const loading = ref(false)
const submitting = ref(false)

const fetchMyVisitors = async () => {
  loading.value = true
  try {
    const res = await request({ url: '/visitorRecord/list', method: 'get', params: { studentId: userStore.userInfo?.id } })
    myVisitors.value = res || []
  } catch (e) { console.error(e);
    ElMessage.error('获取来访记录失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => fetchMyVisitors())

const handleSubmit = async () => {
  if (!form.value.visitorName || !form.value.visitTime) {
    ElMessage.warning('请填写访客姓名和来访时间')
    return
  }
  submitting.value = true
  try {
    await request({
      url: '/visitorRecord/save',
      method: 'post',
      data: {
        studentId: userStore.userInfo?.id,
        visitorName: form.value.visitorName,
        phone: form.value.phone,
        relation: form.value.relation,
        visitTime: form.value.visitTime,
        status: 'PENDING'
      }
    })
    ElMessage.success('来访登记提交成功！')
    form.value = { visitorName: '', phone: '', relation: '朋友', visitTime: '' }
    fetchMyVisitors()
  } catch (e) { console.error(e);
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.visitor-container { max-width: 1200px; margin: 0 auto; }
.hero-card { margin-bottom: 24px; border-radius: 12px; }
.hero-content { display: flex; justify-content: space-between; align-items: center; }
.hero-text { display: flex; align-items: center; gap: 16px; }
.hero-text h2 { margin: 0 0 4px 0; font-size: 20px; color: var(--text); }
.hero-text p { margin: 0; font-size: 14px; color: var(--sub); }
.form-card, .list-card { border-radius: 12px; min-height: 400px; }
.card-header { display: flex; justify-content: space-between; align-items: center; font-weight: bold; }
.visitor-item {
  display: flex; align-items: flex-start; gap: 14px;
  padding: 14px; background: var(--bg); border-radius: 10px; margin-bottom: 12px;
}
.visitor-info { flex: 1; }
.visitor-main { display: flex; align-items: center; gap: 10px; margin-bottom: 4px; }
.visitor-name { font-weight: 600; color: var(--text); }
.visitor-meta { font-size: 13px; color: var(--sub); margin-bottom: 4px; }
.visitor-time { font-size: 12px; color: var(--sub); }
</style>
