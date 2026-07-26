<template>
  <div class="login-page">
    <section class="campus-panel" aria-labelledby="system-title">
      <div class="campus-scrim"></div>
      <div class="campus-content">
        <div class="brand-lockup">
          <span class="brand-mark" aria-hidden="true">
            <el-icon :size="24"><Building2 /></el-icon>
          </span>
          <span>校园后勤服务</span>
        </div>

        <div class="campus-copy">
          <p class="eyebrow">住宿服务平台</p>
          <h1 id="system-title">学生宿舍<br />管理系统</h1>
          <p>统一处理住宿信息、调宿申请和报修服务。</p>
        </div>

        <ul class="service-list" aria-label="系统服务">
          <li><CheckCircle2 :size="17" />住宿信息实时更新</li>
          <li><CheckCircle2 :size="17" />多角色协同处理</li>
          <li><CheckCircle2 :size="17" />报修进度全程可查</li>
        </ul>
      </div>
    </section>

    <main class="login-surface">
      <div class="mobile-brand" aria-hidden="true">
        <Building2 :size="21" />
        <span>学生宿舍管理系统</span>
      </div>

      <div class="form-content">
        <header class="form-header">
          <p class="eyebrow">账号验证</p>
          <h2>用户登录</h2>
          <p>选择身份后，使用您的账号进入系统。</p>
        </header>

        <div class="role-group" role="group" aria-label="登录身份">
          <button
            v-for="role in roles"
            :key="role.value"
            class="role-button"
            :class="{ active: form.role === role.value }"
            type="button"
            :aria-pressed="form.role === role.value"
            @click="form.role = role.value"
          >
            <component :is="role.icon" :size="17" />
            <span>{{ role.label }}</span>
          </button>
        </div>

        <form class="login-form" novalidate @submit.prevent="handleLogin">
          <label class="field-label" for="login-username">学号 / 工号</label>
          <div class="field-control" :class="{ invalid: errors.username }">
            <User :size="19" aria-hidden="true" />
            <input
              id="login-username"
              ref="usernameInput"
              v-model.trim="form.username"
              :aria-describedby="errors.username ? 'username-error' : undefined"
              :aria-invalid="Boolean(errors.username)"
              autocomplete="username"
              placeholder="请输入学号或工号"
              type="text"
              @blur="validateField('username')"
            />
          </div>
          <p v-if="errors.username" id="username-error" class="field-error" role="alert">{{ errors.username }}</p>

          <label class="field-label" for="login-password">密码</label>
          <div class="field-control" :class="{ invalid: errors.password }">
            <LockKeyhole :size="19" aria-hidden="true" />
            <input
              id="login-password"
              ref="passwordInput"
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              :aria-describedby="errors.password ? 'password-error' : undefined"
              :aria-invalid="Boolean(errors.password)"
              autocomplete="current-password"
              placeholder="请输入密码"
              @blur="validateField('password')"
            />
            <button
              class="password-toggle"
              type="button"
              :title="showPassword ? '隐藏密码' : '显示密码'"
              :aria-label="showPassword ? '隐藏密码' : '显示密码'"
              @click="showPassword = !showPassword"
            >
              <EyeOff v-if="showPassword" :size="19" />
              <Eye v-else :size="19" />
            </button>
          </div>
          <p v-if="errors.password" id="password-error" class="field-error" role="alert">{{ errors.password }}</p>

          <div class="form-actions">
            <el-checkbox v-model="form.remember">记住账号</el-checkbox>
            <button class="help-link" type="button" @click="showResetTip">忘记密码？</button>
          </div>

          <button class="login-button" type="submit" :disabled="loading">
            <LoaderCircle v-if="loading" class="is-loading" :size="20" />
            <span v-else>登录系统</span>
          </button>
        </form>

        <p class="security-note"><ShieldCheck :size="16" />请妥善保管账号信息，勿向他人透露密码。</p>
      </div>
    </main>
  </div>
</template>

<script setup>
import { nextTick, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { loginApi } from '../api/auth'
import {
  Building2,
  CheckCircle2,
  Eye,
  EyeOff,
  LoaderCircle,
  LockKeyhole,
  Settings,
  ShieldCheck,
  User,
  UserRound
} from '@lucide/vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const showPassword = ref(false)
const usernameInput = ref(null)
const passwordInput = ref(null)
const errors = reactive({ username: '', password: '' })

const roles = [
  { value: 'student', label: '学生', icon: UserRound },
  { value: 'dormmanager', label: '宿管', icon: ShieldCheck },
  { value: 'admin', label: '管理员', icon: Settings }
]

const form = reactive({
  username: '',
  password: '',
  role: 'student',
  remember: false
})

const demoAccounts = {
  student: '20240001',
  dormmanager: 'manager1',
  admin: 'admin'
}

// 切换角色时自动填入演示账号
watch(() => form.role, (role) => {
  form.username = demoAccounts[role]
  errors.username = ''
}, { immediate: true })

const showResetTip = () => {
  ElMessage.info('请联系宿舍管理员重置密码。')
}

const validateField = (field) => {
  errors[field] = form[field] ? '' : field === 'username' ? '请输入学号或工号。' : '请输入密码。'
}

const validateForm = () => {
  validateField('username')
  validateField('password')
  return !errors.username && !errors.password
}

const handleLogin = async () => {
  if (!validateForm()) {
    await nextTick()
    if (errors.username) usernameInput.value?.focus()
    else passwordInput.value?.focus()
    return
  }

  loading.value = true

  try {
    const res = await loginApi(form.username, form.password, form.role)
    userStore.setToken(res.token)
    userStore.setRole(res.user.role)
    userStore.setUserInfo(res.user)

    const roleLabel = roles.find((role) => role.value === form.role)?.label || '用户'
    ElMessage.success({ message: `欢迎进入${roleLabel}端系统`, duration: 2000 })

    if (form.role === 'student') router.push('/student/desk')
    else if (form.role === 'dormmanager') router.push('/dormmanager/workbench')
    else router.push('/admin/overview')
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  --ink: #0f172a;
  --muted: #64748b;
  --line: #dbe5f2;
  --primary: #2563eb;
  --primary-hover: #1d4ed8;
  --primary-soft: #eff6ff;
  --surface: #ffffff;
  --surface-subtle: #f8faff;
  --danger: #dc2626;
  position: relative;
  isolation: isolate;
  display: grid;
  grid-template-columns: minmax(0, 1.25fr) minmax(390px, 0.75fr);
  min-height: 100dvh;
  overflow: hidden;
  background: url('../assets/login-campus.jpg') center / cover no-repeat;
  color: var(--ink);
  font-family: Inter, "Microsoft YaHei", "PingFang SC", sans-serif;
}

.login-page::before {
  position: absolute;
  z-index: -1;
  inset: 0;
  background: rgba(9, 32, 62, 0.5);
  content: '';
}

.campus-panel {
  position: relative;
  min-height: 100dvh;
  color: #fff;
}

.campus-scrim {
  display: none;
}

.campus-content {
  position: relative;
  z-index: 1;
  display: flex;
  box-sizing: border-box;
  flex-direction: column;
  justify-content: space-between;
  min-height: 100dvh;
  padding: clamp(30px, 5vw, 74px);
}

.brand-lockup,
.mobile-brand,
.security-note,
.service-list li,
.role-button,
.field-control,
.form-actions,
.login-button {
  display: flex;
  align-items: center;
}

.brand-lockup {
  gap: 10px;
  font-size: 14px;
  font-weight: 650;
}

.brand-mark {
  display: grid;
  width: 42px;
  height: 42px;
  place-items: center;
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 6px;
}

.campus-copy {
  max-width: 470px;
  margin: auto 0;
}

.eyebrow {
  margin: 0 0 12px;
  color: inherit;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.campus-copy h1 {
  margin: 0;
  color: #fff;
  font-family: "Microsoft YaHei", "PingFang SC", sans-serif;
  font-size: clamp(36px, 3.9vw, 56px);
  font-weight: 700;
  line-height: 1.18;
}

.campus-copy > p:last-child {
  margin: 20px 0 0;
  color: rgba(255, 255, 255, 0.9);
  font-size: 17px;
  line-height: 1.7;
}

.service-list {
  display: grid;
  gap: 12px;
  margin: 0;
  padding: 0;
  list-style: none;
  color: rgba(255, 255, 255, 0.94);
  font-size: 14px;
}

.service-list li {
  gap: 9px;
}

.service-list svg {
  color: #93c5fd;
}

.login-surface {
  display: grid;
  min-height: 100dvh;
  place-items: center;
  background: transparent;
}

.form-content {
  position: relative;
  box-sizing: border-box;
  width: min(100% - 64px, 440px);
  border: 1px solid rgba(255, 255, 255, 0.48);
  border-radius: 8px;
  padding: 42px 36px 34px;
  background: rgba(232, 240, 250, 0.54);
  box-shadow: 0 24px 64px rgba(4, 24, 49, 0.28), inset 0 1px 0 rgba(255, 255, 255, 0.42);
  backdrop-filter: blur(30px) saturate(150%) contrast(104%);
  -webkit-backdrop-filter: blur(30px) saturate(150%) contrast(104%);
}

.mobile-brand {
  display: none;
}

.form-header {
  margin-bottom: 28px;
}

.form-header .eyebrow {
  margin-bottom: 9px;
  color: var(--primary);
}

.form-header h2 {
  margin: 0;
  color: var(--ink);
  font-family: "Microsoft YaHei", "PingFang SC", sans-serif;
  font-size: 30px;
  font-weight: 700;
  line-height: 1.25;
}

.form-header p:last-child {
  margin: 11px 0 0;
  color: var(--muted);
  font-size: 14px;
  line-height: 1.7;
}

.role-group {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 6px;
  margin-bottom: 24px;
}

.role-button {
  position: relative;
  justify-content: center;
  gap: 6px;
  min-height: 44px;
  border: 0;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.2);
  box-shadow: inset 0 0 0 1px rgba(100, 116, 139, 0.14);
  color: #52657a;
  cursor: pointer;
  font: inherit;
  font-size: 13px;
  font-weight: 650;
  transition: background-color 180ms ease, color 180ms ease, box-shadow 180ms ease;
}

.role-button:hover {
  background: rgba(255, 255, 255, 0.36);
  box-shadow: inset 0 0 0 1px rgba(37, 99, 235, 0.2);
  color: var(--primary);
}

.role-button.active {
  border-radius: 6px;
  background: var(--primary);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.2);
  color: #fff;
  font-weight: 700;
}

.role-button.active svg {
  stroke-width: 2.4;
}

.role-button:focus-visible {
  z-index: 1;
  border-radius: 4px;
  outline: 2px solid rgba(37, 99, 235, 0.55);
  outline-offset: -4px;
}

.login-form {
  display: grid;
  gap: 9px;
}

.field-label {
  margin-top: 7px;
  color: #374151;
  font-size: 14px;
  font-weight: 650;
}

.field-control {
  position: relative;
  min-height: 48px;
  gap: 11px;
  box-sizing: border-box;
  border: 1px solid rgba(148, 163, 184, 0.32);
  border-radius: 6px;
  padding: 0 14px;
  background: rgba(255, 255, 255, 0.52);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.38);
  color: #819097;
  transition: border-color 180ms ease, box-shadow 180ms ease;
}

.field-control:focus-within {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15);
}

.field-control.invalid {
  border-color: var(--danger);
}

.field-control.invalid:focus-within {
  border-color: var(--danger);
  box-shadow: 0 0 0 3px rgba(220, 38, 38, 0.12);
}

.field-control input {
  min-width: 0;
  width: 100%;
  border: 0;
  outline: 0;
  background: transparent;
  color: var(--ink);
  font: inherit;
  font-size: 15px;
}

.field-control input::placeholder {
  color: #94a3b8;
}

.field-error {
  margin: -2px 0 0;
  color: var(--danger);
  font-size: 12px;
  line-height: 1.5;
}

.password-toggle {
  display: grid;
  flex: 0 0 auto;
  width: 36px;
  height: 36px;
  place-items: center;
  border: 0;
  border-radius: 4px;
  background: transparent;
  color: #6b7280;
  cursor: pointer;
  transition: background-color 180ms ease, color 180ms ease;
}

.password-toggle:hover,
.password-toggle:focus-visible {
  background: var(--primary-soft);
  color: var(--primary);
  outline: none;
}

.form-actions {
  justify-content: space-between;
  min-height: 28px;
  margin: 6px 0 14px;
}

.form-actions :deep(.el-checkbox__label) {
  color: #59666d;
  font-size: 14px;
}

.form-actions :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: var(--primary);
  border-color: var(--primary);
}

.help-link {
  border: 0;
  background: transparent;
  color: var(--primary);
  cursor: pointer;
  font: inherit;
  font-size: 14px;
  font-weight: 650;
}

.help-link:hover,
.help-link:focus-visible {
  color: var(--primary-hover);
  outline: none;
  text-decoration: underline;
}

.login-button {
  justify-content: center;
  min-height: 50px;
  border: 1px solid var(--primary);
  border-radius: 6px;
  background: var(--primary);
  color: #fff;
  cursor: pointer;
  font: inherit;
  font-size: 16px;
  font-weight: 700;
  transition: background-color 180ms ease, border-color 180ms ease, transform 180ms ease;
}

.login-button:hover:not(:disabled) {
  border-color: var(--primary-hover);
  background: var(--primary-hover);
  transform: translateY(-1px);
}

.login-button:focus-visible {
  outline: 3px solid rgba(37, 99, 235, 0.25);
  outline-offset: 3px;
}

.login-button:disabled {
  cursor: wait;
  opacity: 0.7;
}

.is-loading {
  animation: spin 0.8s linear infinite;
}

.security-note {
  gap: 8px;
  margin: 26px 0 0;
  border-top: 1px solid rgba(255, 255, 255, 0.46);
  padding-top: 19px;
  color: #778187;
  font-size: 12px;
  line-height: 1.5;
}

.security-note svg {
  flex: 0 0 auto;
  color: var(--primary);
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@media (prefers-reduced-motion: reduce) {
  .role-button,
  .field-control,
  .login-button,
  .password-toggle,
  .is-loading {
    animation: none;
    transition: none;
  }
}

@media (max-width: 860px) {
  .login-page {
    display: block;
  }

  .campus-panel {
    display: none;
  }

  .login-surface {
    position: relative;
    display: grid;
    min-height: 100dvh;
    box-sizing: border-box;
    padding: 82px 24px 32px;
  }

  .mobile-brand {
    display: flex;
    position: absolute;
    top: 28px;
    left: 24px;
    gap: 8px;
    width: auto;
    margin: 0;
    color: #fff;
    font-size: 14px;
    font-weight: 700;
  }

  .mobile-brand svg {
    color: #bfdbfe;
  }

  .form-content {
    width: min(100%, 400px);
  }
}

@media (max-width: 430px) {
  .role-button {
    gap: 4px;
    font-size: 13px;
  }

  .role-button svg {
    display: none;
  }

  .form-content {
    padding: 38px 24px 28px;
  }

}
</style>
