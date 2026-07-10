<template>
  <div class="login-wrapper">
    <!-- Animated background elements -->
    <div class="bg-orb orb-1"></div>
    <div class="bg-orb orb-2"></div>
    <div class="bg-orb orb-3"></div>

    <div class="glass-card">
      <div class="card-inner">
        <!-- Left Side: Brand Showcase -->
        <div class="brand-panel">
          <div class="brand-overlay"></div>
          <div class="brand-content">
            <div class="logo-box">
              <el-icon :size="32" color="#fff"><component :is="Building2" /></el-icon>
            </div>
            <h1 class="brand-title">Dormitory<br/>Management</h1>
            <p class="brand-subtitle">学生宿舍智能管理平台</p>
            
            <div class="brand-features">
              <div class="feature-item">
                <el-icon><component :is="CheckCircle2" /></el-icon>
                <span>数字化协同流程</span>
              </div>
              <div class="feature-item">
                <el-icon><component :is="CheckCircle2" /></el-icon>
                <span>实时状态追踪</span>
              </div>
              <div class="feature-item">
                <el-icon><component :is="CheckCircle2" /></el-icon>
                <span>全角色数据互通</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Right Side: Login Form -->
        <div class="form-panel">
          <div class="form-header">
            <h2>欢迎登录</h2>
            <p>请选择您的身份并输入凭证进入系统</p>
          </div>

          <!-- Modern Role Selector -->
          <div class="role-segmented-control">
            <div 
              class="segment" 
              :class="{ active: form.role === 'student' }"
              @click="form.role = 'student'"
            >
              <el-icon><component :is="UserRound" /></el-icon>学生
            </div>
            <div 
              class="segment" 
              :class="{ active: form.role === 'dormmanager' }"
              @click="form.role = 'dormmanager'"
            >
              <el-icon><component :is="ShieldCheck" /></el-icon>宿管
            </div>
            <div 
              class="segment" 
              :class="{ active: form.role === 'admin' }"
              @click="form.role = 'admin'"
            >
              <el-icon><component :is="Settings" /></el-icon>管理员
            </div>
            <div class="active-indicator" :style="indicatorStyle"></div>
          </div>

          <el-form :model="form" class="login-form" @submit.prevent="handleLogin">
            <div class="input-group">
              <div class="input-field">
                <el-icon class="input-icon"><component :is="User" /></el-icon>
                <input 
                  v-model="form.username" 
                  type="text" 
                  placeholder="学号 / 工号" 
                />
              </div>
              
              <div class="input-field">
                <el-icon class="input-icon"><component :is="Lock" /></el-icon>
                <input 
                  v-model="form.password" 
                  type="password" 
                  placeholder="密码" 
                />
              </div>
            </div>

            <div class="form-options">
              <el-checkbox v-model="form.remember">记住账号</el-checkbox>
              <a href="#" class="forgot-link">忘记密码?</a>
            </div>

            <button type="button" class="login-btn" @click="handleLogin" :disabled="loading">
              <span v-if="!loading">登录系统</span>
              <el-icon v-else class="is-loading"><component :is="Loader2" /></el-icon>
            </button>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { loginApi } from '../api/auth'
import { Building2, UserRound, ShieldCheck, Settings, User, Lock, CheckCircle2, Loader2 } from '@lucide/vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)

const form = reactive({
  username: 'student',
  password: '123456',
  role: 'student',
  remember: true
})

const indicatorStyle = computed(() => {
  const map = {
    'student': '0%',
    'dormmanager': '100%',
    'admin': '200%'
  }
  return {
    transform: `translateX(${map[form.role]})`
  }
})

const handleLogin = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入完整账号信息')
    return
  }

  loading.value = true
  
  try {
    // 调用我们在 api/auth.js 里封装的登录接口
    const res = await loginApi(form.username, form.password, form.role)
    
    // 登录成功，将 Token 和身份存入 Pinia (它会自动存入 localStorage)
    userStore.setToken(res.token)
    userStore.setRole(res.user.role)
    userStore.setUserInfo(res.user)

    ElMessage.success({
      message: `欢迎进入${form.role === 'student' ? '学生' : form.role === 'dormmanager' ? '宿管' : '管理'}端系统`,
      duration: 2000
    })
    
    if (form.role === 'student') router.push('/student/desk')
    else if (form.role === 'dormmanager') router.push('/dormmanager/workbench')
    else router.push('/admin/overview')
  } catch (error) {
    // 失败时的提示已在拦截器或者 api 里处理，此处可以做一些补充
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrapper {
  min-height: 100vh;
  width: 100vw;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f8fafc;
  position: relative;
  overflow: hidden;
}

/* Ambient animated background orbs */
.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  z-index: 0;
  animation: float 20s infinite ease-in-out;
}

.orb-1 {
  width: 600px;
  height: 600px;
  background: rgba(59, 130, 246, 0.15); /* blue */
  top: -100px;
  left: -100px;
  animation-delay: 0s;
}

.orb-2 {
  width: 500px;
  height: 500px;
  background: rgba(139, 92, 246, 0.15); /* purple */
  bottom: -100px;
  right: -50px;
  animation-delay: -5s;
}

.orb-3 {
  width: 400px;
  height: 400px;
  background: rgba(14, 165, 233, 0.15); /* sky */
  top: 40%;
  left: 40%;
  animation-delay: -10s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(50px, -50px) scale(1.1); }
  66% { transform: translate(-30px, 40px) scale(0.9); }
}

/* Central Glassmorphism Card */
.glass-card {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 1000px;
  height: 600px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 24px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.1), 0 0 0 1px rgba(255, 255, 255, 0.2) inset;
  padding: 12px;
}

.card-inner {
  display: flex;
  width: 100%;
  height: 100%;
  border-radius: 16px;
  overflow: hidden;
  background: white;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

/* Brand Panel (Left) */
.brand-panel {
  flex: 1;
  position: relative;
  background: linear-gradient(145deg, #1e3a8a 0%, #3b82f6 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 60px;
  color: white;
  overflow: hidden;
}

.brand-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyMCIgaGVpZ2h0PSIyMCI+PHBhdGggZD0iTTAgMGgyMHYyMEgwem0xMCAxMGgxMHYxMEgxMHoiIGZpbGw9InJnYmEoMjU1LDI1NSwyNTUsMC4wMykiIGZpbGwtcnVsZT0iZXZlbm9kZCIvPjwvc3ZnPg==') repeat;
  opacity: 0.5;
}

.brand-content {
  position: relative;
  z-index: 2;
}

.logo-box {
  width: 64px;
  height: 64px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32px;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.brand-title {
  font-size: 42px;
  font-weight: 800;
  line-height: 1.1;
  margin: 0 0 16px 0;
  letter-spacing: -1px;
}

.brand-subtitle {
  font-size: 18px;
  color: #bfdbfe;
  margin: 0 0 48px 0;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
  color: #e0e7ff;
}

.feature-item .el-icon {
  color: #60a5fa;
}

/* Form Panel (Right) */
.form-panel {
  flex: 1;
  padding: 60px 80px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: #ffffff;
}

.form-header {
  margin-bottom: 40px;
}

.form-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 8px 0;
}

.form-header p {
  color: #64748b;
  font-size: 15px;
  margin: 0;
}

/* iOS-style Segmented Control */
.role-segmented-control {
  position: relative;
  display: flex;
  background: #f1f5f9;
  border-radius: 12px;
  padding: 6px;
  margin-bottom: 32px;
}

.segment {
  flex: 1;
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 40px;
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
  cursor: pointer;
  transition: color 0.3s;
}

.segment.active {
  color: #0f172a;
}

.active-indicator {
  position: absolute;
  top: 6px;
  left: 6px;
  height: 40px;
  width: calc(33.33% - 4px);
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05), 0 1px 2px rgba(0,0,0,0.1);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 1;
}

/* Modern Inputs */
.input-group {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 24px;
}

.input-field {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 16px;
  color: #94a3b8;
  font-size: 18px;
  pointer-events: none;
}

.input-field input {
  width: 100%;
  height: 52px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 0 16px 0 44px;
  font-size: 15px;
  color: #0f172a;
  outline: none;
  transition: all 0.2s ease;
}

.input-field input::placeholder {
  color: #94a3b8;
}

.input-field input:focus {
  background: white;
  border-color: #3b82f6;
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1);
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.forgot-link {
  color: #3b82f6;
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
}

.forgot-link:hover {
  text-decoration: underline;
}

.login-btn {
  width: 100%;
  height: 52px;
  border: none;
  border-radius: 12px;
  background: #3b82f6;
  color: white;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-btn:hover:not(:disabled) {
  background: #2563eb;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.2);
}

.login-btn:active:not(:disabled) {
  transform: translateY(0);
}

.login-btn:disabled {
  background: #93c5fd;
  cursor: not-allowed;
}

.is-loading {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  100% { transform: rotate(360deg); }
}

@media (max-width: 900px) {
  .brand-panel {
    display: none;
  }
  .form-panel {
    padding: 40px;
  }
  .glass-card {
    height: auto;
    margin: 24px;
  }
}
</style>
