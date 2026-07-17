<template>
  <div class="settings-page">
    <el-card shadow="never" class="page-card">
      <div class="page-heading">
        <div class="heading-icon"><UserCog :size="22" /></div>
        <div>
          <h2>账户设置</h2>
          <p>维护用于宿舍服务联系的个人资料。</p>
        </div>
      </div>
    </el-card>

    <div class="settings-grid">
      <el-card shadow="never" class="profile-card">
        <template #header><strong>个人资料</strong></template>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <div class="form-grid">
            <el-form-item label="学号">
              <el-input v-model="form.username" disabled />
            </el-form-item>
            <el-form-item label="姓名" prop="name">
              <el-input v-model.trim="form.name" maxlength="20" />
            </el-form-item>
            <el-form-item label="班级" prop="className">
              <el-input v-model.trim="form.className" maxlength="40" />
            </el-form-item>
            <el-form-item label="手机号码" prop="phone">
              <el-input v-model.trim="form.phone" maxlength="20" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email" class="wide-field">
              <el-input v-model.trim="form.email" maxlength="80" />
            </el-form-item>
          </div>
          <div class="form-footer">
            <el-button @click="resetForm">恢复原值</el-button>
            <el-button type="primary" :loading="saving" @click="saveProfile">保存资料</el-button>
          </div>
        </el-form>
      </el-card>

      <el-card shadow="never" class="security-card">
        <template #header><strong>账户安全</strong></template>
        <div class="security-item">
          <ShieldCheck :size="20" />
          <div><strong>登录身份</strong><span>学生</span></div>
        </div>
        <div class="security-item">
          <KeyRound :size="20" />
          <div><strong>密码管理</strong><span>如需重置密码，请联系宿舍管理员。</span></div>
        </div>
        <el-alert title="姓名、学号和角色等身份信息受系统保护，学生不能自行修改。" type="info" :closable="false" show-icon />
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { KeyRound, ShieldCheck, UserCog } from '@lucide/vue'
import { saveUser } from '../../api/user'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const formRef = ref(null)
const saving = ref(false)

const form = reactive({ username: '', name: '', className: '', phone: '', email: '' })

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ pattern: /^$|^1\d{10}$/, message: '请输入正确的手机号码', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }]
}

const resetForm = () => {
  const user = userStore.userInfo || {}
  Object.assign(form, {
    username: user.username || '',
    name: user.name || '',
    className: user.className || '',
    phone: user.phone || '',
    email: user.email || ''
  })
  formRef.value?.clearValidate()
}

const saveProfile = async () => {
  try {
    await formRef.value?.validate()
  } catch (error) { console.error(error);
    return
  }
  saving.value = true
  try {
    await saveUser({ id: userStore.userInfo?.id, ...form })
    userStore.setUserInfo({ ...userStore.userInfo, ...form })
    ElMessage.success('个人资料已更新')
  } catch (error) { console.error(error);
    // The request interceptor displays the server error.
  } finally {
    saving.value = false
  }
}

resetForm()
</script>

<style scoped>
.settings-page { max-width: 1200px; margin: 0 auto; }
.page-card { margin-bottom: 24px; }
.page-heading { display: flex; align-items: center; gap: 14px; }
.page-heading h2 { margin: 0 0 4px; color: var(--text); font-size: 20px; }
.page-heading p { margin: 0; color: var(--sub); font-size: 14px; }
.heading-icon { display: grid; width: 44px; height: 44px; place-items: center; border-radius: 8px; background: var(--primary-2); color: var(--primary); }
.settings-grid { display: grid; grid-template-columns: minmax(0, 2fr) minmax(280px, 1fr); gap: 24px; }
.form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 0 20px; }
.wide-field { grid-column: 1 / -1; }
.form-footer { display: flex; justify-content: flex-end; gap: 12px; padding-top: 8px; border-top: 1px solid var(--line); }
.security-item { display: flex; gap: 12px; align-items: flex-start; padding: 4px 0 20px; }
.security-item svg { flex: none; color: var(--primary); }
.security-item div { display: grid; gap: 5px; }
.security-item strong { color: var(--text); font-size: 14px; }
.security-item span { color: var(--sub); font-size: 13px; line-height: 1.6; }
@media (max-width: 800px) {
  .settings-grid, .form-grid { grid-template-columns: 1fr; }
  .wide-field { grid-column: auto; }
}
</style>
