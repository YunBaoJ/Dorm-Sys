<template>
  <div class="profile-manager-container">
    <el-card shadow="never" class="hero-card">
      <div class="hero-content">
        <div class="hero-text">
          <el-icon :size="28" color="var(--el-color-primary)"><component :is="User" /></el-icon>
          <div>
            <h2>个人中心</h2>
            <p>管理您的宿管账号信息与偏好设置</p>
          </div>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="form-card">
      <el-form :model="form" label-width="100px" style="max-width: 600px;">
        <el-form-item label="姓名">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="电子邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="账号角色">
          <el-input :value="form.role === 'dormmanager' ? '宿舍管理员' : form.role" disabled />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSave" :loading="saving">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { User } from '@lucide/vue'
import { useUserStore } from '../../store/user'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'

const userStore = useUserStore()
const form = ref({ id: null, name: '', phone: '', email: '', role: '' })
const saving = ref(false)

const loadUserInfo = () => {
  if (userStore.userInfo) {
    form.value.id = userStore.userInfo.id
    form.value.name = userStore.userInfo.name || ''
    form.value.phone = userStore.userInfo.phone || ''
    form.value.email = userStore.userInfo.email || ''
    form.value.role = userStore.userInfo.role || ''
  }
}

const handleSave = async () => {
  saving.value = true
  try {
    const res = await request({
      url: '/user/save',
      method: 'post',
      data: form.value
    })
    ElMessage.success('保存成功！下次重新登录时生效。')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile-manager-container { max-width: 800px; margin: 0 auto; }
.hero-card { margin-bottom: 24px; border-radius: 12px; background: linear-gradient(135deg, rgba(var(--el-color-primary-rgb), 0.1), rgba(var(--el-color-primary-rgb), 0.02)); border: none; }
.hero-content { display: flex; justify-content: space-between; align-items: center; }
.hero-text { display: flex; align-items: center; gap: 16px; }
.hero-text h2 { margin: 0 0 4px; font-size: 22px; color: var(--text); }
.hero-text p { margin: 0; color: var(--sub); font-size: 14px; }
.form-card { border-radius: 12px; border: 1px solid var(--border); padding: 24px; }
</style>
