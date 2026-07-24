<template>
  <div class="login-page">
    <div class="login-bg">
      <!-- 左侧品牌展示区 -->
      <div class="login-left">
        <div class="brand">
          <h1>Smart Recruit</h1>
          <p>企业级智慧招聘与人才管理系统</p>
        </div>
        <div class="feature-list">
          <div class="feature-item" v-for="f in features" :key="f.title">
            <div class="feature-icon">
              <el-icon size="24"><component :is="f.icon" /></el-icon>
            </div>
            <div class="feature-content">
              <h4>{{ f.title }}</h4>
              <span>{{ f.desc }}</span>
            </div>
          </div>
        </div>
        <div class="brand-footer">
          <span>© 2026 Smart Recruit. All rights reserved.</span>
        </div>
      </div>

      <!-- 右侧登录表单区 -->
      <div class="login-right">
        <el-card class="login-card" shadow="always">
          <div class="card-header">
            <h2>欢迎登录</h2>
            <p>输入账号和密码，系统将自动进入您的工作台</p>
          </div>

          <el-form :model="form" :rules="rules" ref="formRef" class="login-form">
            <el-form-item prop="username">
              <el-input 
                v-model="form.username" 
                placeholder="用户名/邮箱" 
                prefix-icon="User" 
                size="large" 
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input 
                v-model="form.password" 
                type="password" 
                placeholder="登录密码" 
                prefix-icon="Lock" 
                size="large" 
                show-password 
                @keyup.enter="handleLogin" 
              />
            </el-form-item>

            <el-form-item>
              <div class="form-extra">
                <el-checkbox v-model="rememberMe" class="remember-checkbox" @change="handleRememberChange">记住登录密码</el-checkbox>
                <el-link type="primary" underline="never" class="forget-link" @click="openForgotDialog">忘记密码？</el-link>
              </div>
            </el-form-item>

            <el-form-item>
              <el-button 
                type="primary" 
                size="large" 
                class="login-btn" 
                :loading="loading" 
                @click="handleLogin"
              >
                登 录
              </el-button>
            </el-form-item>
          </el-form>

          <!-- 快捷登录体验 -->
          <div class="quick-login">
            <span>✨ 快速体验账号登录（点击直接填入）</span>
            <div class="quick-btns">
              <el-tag 
                v-for="r in quickRoles" 
                :key="r.role" 
                :type="r.type" 
                size="default" 
                class="quick-tag" 
                @click="quickLogin(r)"
              >
                {{ r.label }}
              </el-tag>
            </div>
          </div>

          <div class="register-link">
            还没有账号？<router-link to="/register">立即注册</router-link>
          </div>
        </el-card>

        <el-dialog
          v-model="forgotDialogVisible"
          title="重置登录密码"
          width="460px"
          class="forgot-dialog"
          :close-on-click-modal="false"
          @closed="resetForgotDialog"
        >
          <div class="forgot-panel">
            <el-steps :active="forgotStep" finish-status="success" simple class="forgot-steps">
              <el-step title="验证邮箱" />
              <el-step title="设置新密码" />
            </el-steps>

            <el-alert
              title="验证码将发送到账号绑定邮箱，10 分钟内有效。"
              type="info"
              :closable="false"
              show-icon
              class="forgot-alert"
            />

            <el-form
              ref="forgotFormRef"
              :model="forgotForm"
              :rules="forgotRules"
              label-position="top"
              class="forgot-form"
            >
              <el-form-item label="绑定邮箱" prop="email">
                <el-input
                  v-model.trim="forgotForm.email"
                  placeholder="请输入注册或绑定的邮箱"
                  prefix-icon="Message"
                  size="large"
                  :disabled="forgotCodeSent"
                />
              </el-form-item>

              <template v-if="forgotCodeSent">
                <el-form-item label="邮箱验证码" prop="code">
                  <el-input
                    v-model.trim="forgotForm.code"
                    placeholder="请输入 6 位验证码"
                    prefix-icon="Key"
                    size="large"
                    maxlength="6"
                  />
                </el-form-item>

                <el-form-item label="新密码" prop="newPassword">
                  <el-input
                    v-model="forgotForm.newPassword"
                    type="password"
                    placeholder="请输入新密码（不少于 6 位）"
                    prefix-icon="Lock"
                    size="large"
                    show-password
                  />
                </el-form-item>

                <el-form-item label="确认新密码" prop="confirmPassword">
                  <el-input
                    v-model="forgotForm.confirmPassword"
                    type="password"
                    placeholder="请再次输入新密码"
                    prefix-icon="Lock"
                    size="large"
                    show-password
                    @keyup.enter="submitResetPassword"
                  />
                </el-form-item>
              </template>
            </el-form>
          </div>

          <template #footer>
            <div class="forgot-footer">
              <el-button @click="forgotDialogVisible = false">取消</el-button>
              <el-button
                v-if="!forgotCodeSent"
                type="primary"
                :loading="codeSending"
                :disabled="countdown > 0"
                @click="sendResetCode"
              >
                {{ countdown > 0 ? `${countdown}s 后重试` : '发送验证码' }}
              </el-button>
              <el-button
                v-else
                type="primary"
                :loading="resetPasswordLoading"
                @click="submitResetPassword"
              >
                确认重置
              </el-button>
            </div>
          </template>
        </el-dialog>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, sendForgotPasswordCode, resetForgotPassword } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const rememberMe = ref(false)
const forgotDialogVisible = ref(false)
const forgotFormRef = ref()
const codeSending = ref(false)
const resetPasswordLoading = ref(false)
const forgotCodeSent = ref(false)
const countdown = ref(0)
const REMEMBER_LOGIN_KEY = 'remember_login_credentials'
let countdownTimer = null

const form = reactive({ username: '', password: '' })
const forgotForm = reactive({
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const forgotStep = computed(() => forgotCodeSent.value ? 1 : 0)

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入新密码'))
    return
  }
  if (value !== forgotForm.newPassword) {
    callback(new Error('两次输入的新密码不一致'))
    return
  }
  callback()
}

const rules = {
  username: [{ required: true, message: '请输入用户名或邮箱', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码不少于6位', trigger: 'blur' }
  ]
}
const forgotRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码为 6 位数字', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 64, message: '新密码长度为 6 到 64 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const features = [
  { icon: 'Document', title: 'AI 简历解析提取', desc: '秒级解析各种格式简历，提取工作与技能核心词' },
  { icon: 'Connection', title: '双向人岗智能匹配', desc: '多维度匹配度算法模型，精准分析人才与岗位的契合度' },
  { icon: 'DataAnalysis', title: '全链路招聘大屏', desc: '可视化漏斗看板，助力企业招聘决策全方位数智化' }
]

const quickRoles = [
  { role: 'CANDIDATE', label: '求职者体验', username: 'zhangsan', type: 'success' },
  { role: 'HR', label: 'HR 体验', username: 'lijingli', type: 'warning' },
  { role: 'INTERVIEWER', label: '面试官体验', username: 'wanggong', type: 'primary' }
]

const roleRedirects = {
  CANDIDATE: '/candidate',
  HR: '/hr',
  INTERVIEWER: '/interviewer',
  ADMIN: '/admin/users'
}

onMounted(() => {
  const remembered = readRememberedLogin()
  if (!remembered) return

  rememberMe.value = true
  form.username = remembered.username || ''
  form.password = remembered.password || ''
})

onBeforeUnmount(() => {
  stopCountdown()
})

function encodeRememberedLogin(data) {
  return window.btoa(encodeURIComponent(JSON.stringify(data)))
}

function decodeRememberedLogin(value) {
  try {
    return JSON.parse(decodeURIComponent(window.atob(value)))
  } catch (e) {
    localStorage.removeItem(REMEMBER_LOGIN_KEY)
    return null
  }
}

function readRememberedLogin() {
  const saved = localStorage.getItem(REMEMBER_LOGIN_KEY)
  return saved ? decodeRememberedLogin(saved) : null
}

function syncRememberedLogin() {
  if (!rememberMe.value) {
    localStorage.removeItem(REMEMBER_LOGIN_KEY)
    return
  }

  localStorage.setItem(REMEMBER_LOGIN_KEY, encodeRememberedLogin({
    username: form.username,
    password: form.password
  }))
}

function handleRememberChange(value) {
  if (!value) {
    localStorage.removeItem(REMEMBER_LOGIN_KEY)
  }
}

function quickLogin(r) {
  form.username = r.username
  form.password = '123456'
  
  // 填充后自动触发登录
  handleLogin()
}

function openForgotDialog() {
  forgotDialogVisible.value = true
  if (form.username && form.username.includes('@')) {
    forgotForm.email = form.username
  }
}

function resetForgotDialog() {
  stopCountdown()
  forgotCodeSent.value = false
  countdown.value = 0
  forgotForm.email = ''
  forgotForm.code = ''
  forgotForm.newPassword = ''
  forgotForm.confirmPassword = ''
  forgotFormRef.value?.clearValidate()
}

function startCountdown(seconds = 60) {
  stopCountdown()
  countdown.value = seconds
  countdownTimer = window.setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0) {
      stopCountdown()
    }
  }, 1000)
}

function stopCountdown() {
  if (countdownTimer) {
    window.clearInterval(countdownTimer)
    countdownTimer = null
  }
  if (countdown.value < 0) {
    countdown.value = 0
  }
}

async function validateForgotEmail() {
  return forgotFormRef.value
    ?.validateField('email')
    .then(() => true)
    .catch(() => false)
}

async function sendResetCode() {
  if (countdown.value > 0) return
  const valid = await validateForgotEmail()
  if (!valid) return

  codeSending.value = true
  try {
    await sendForgotPasswordCode({ email: forgotForm.email })
    forgotCodeSent.value = true
    forgotForm.code = ''
    startCountdown()
    ElMessage.success('如果邮箱已绑定账号，验证码将发送到该邮箱')
  } catch (e) {
    ElMessage.error(e.message || '验证码发送失败，请稍后再试')
  } finally {
    codeSending.value = false
  }
}

async function submitResetPassword() {
  const valid = await forgotFormRef.value.validate().catch(() => false)
  if (!valid) return

  resetPasswordLoading.value = true
  try {
    await resetForgotPassword({
      email: forgotForm.email,
      code: forgotForm.code,
      newPassword: forgotForm.newPassword
    })
    forgotDialogVisible.value = false
    form.username = forgotForm.email
    form.password = ''
    if (rememberMe.value) {
      localStorage.removeItem(REMEMBER_LOGIN_KEY)
      rememberMe.value = false
    }
    ElMessage.success('密码已重置，请使用新密码登录')
  } catch (e) {
    ElMessage.error(e.message || '密码重置失败，请检查验证码')
  } finally {
    resetPasswordLoading.value = false
  }
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await login({ 
      username: form.username, 
      password: form.password
    })

    // 防御性安全提取
    const dataObj = res?.data || res || {}
    const userInfoObj = dataObj.userInfo || res?.userInfo || {}
    const tokenVal = dataObj.token || res?.token || ''
    const refreshTokenVal = dataObj.refreshToken || res?.refreshToken || ''

    // 使用后端返回的真实角色
    const actualRole = userInfoObj.role
    if (!actualRole) {
      ElMessage.error('登录失败：未获取到用户角色信息')
      return
    }
    
    if (!tokenVal) {
      ElMessage.error('登录失败：未获取到 Token，请检查后端服务是否正常')
      return
    }
    syncRememberedLogin()
    // 保存至 Pinia 和 LocalStorage
    userStore.setToken(tokenVal)
    if (refreshTokenVal) {
      userStore.setRefreshToken(refreshTokenVal)
    } else {
      localStorage.removeItem('refreshToken')
    }
    userStore.setUserInfo(userInfoObj)
    
    ElMessage.success({
      message: `欢迎回来，${userInfoObj.nickName || userInfoObj.username || '精英人才'}！`,
      duration: 2000
    })
    
    // 延时跳转，使用实际角色进行路由
    setTimeout(() => {
      router.push(roleRedirects[actualRole] || '/candidate')
    }, 400)
  } catch (e) {
    console.error('[Login] 异常', e)
    ElMessage.error(e.message || '登录失败，请检查账号密码')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

.login-bg {
  display: flex;
  height: 100%;
  width: 100%;
}

/* 左侧品牌区 */
.login-left {
  flex: 1.1;
  background: linear-gradient(135deg, #1f1b24 0%, #3a221d 40%, #e76f51 100%);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  color: #fff;
  padding: 80px 60px;
  position: relative;
}

.brand h1 {
  font-size: 40px;
  font-weight: 800;
  margin-bottom: 12px;
  letter-spacing: 1.5px;
  background: linear-gradient(to right, #ffffff, #f4a261);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.brand p {
  font-size: 16px;
  color: #d1d5db;
  font-weight: 300;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 36px;
  margin: 60px 0;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 20px;
}

.feature-icon {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #f4a261;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.feature-content h4 {
  font-size: 16px;
  font-weight: 600;
  color: #ffffff;
  margin: 0 0 6px 0;
}

.feature-content span {
  font-size: 13px;
  color: #a0aec0;
  line-height: 1.5;
  display: block;
}

.brand-footer {
  font-size: 12px;
  color: #8a90a0;
}

/* 右侧表单区 */
.login-right {
  width: 540px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fc;
  padding: 40px;
  flex-shrink: 0;
  border-left: 1px solid #e2e8f0;
}

.login-card {
  width: 100%;
  max-width: 440px;
  border-radius: 16px;
  border: 1px solid #f1f3f9;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.04) !important;
  padding: 10px;
}

.card-header {
  text-align: center;
  margin-bottom: 28px;
}

.card-header h2 {
  font-size: 24px;
  font-weight: 700;
  color: #2d3748;
  margin: 0 0 8px 0;
}

.card-header p {
  color: #718096;
  font-size: 14px;
}

.login-form {
  margin-top: 8px;
}

.login-form :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #e2e8f0 inset;
  border-radius: 10px;
  padding: 4px 15px;
  transition: all 0.2s;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #e76f51 inset, 0 0 0 3px rgba(231, 111, 81, 0.1) !important;
}

.form-extra {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.remember-checkbox :deep(.el-checkbox__label) {
  color: #718096;
  font-size: 13px;
}

.remember-checkbox :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #e76f51;
  border-color: #e76f51;
}

.forget-link {
  font-size: 13px;
  color: #e76f51;
}

.forget-link:hover {
  color: #f4a261;
}

.forgot-panel {
  padding-top: 2px;
}

.forgot-steps {
  margin-bottom: 18px;
  border-radius: 8px;
}

.forgot-alert {
  margin-bottom: 18px;
  border-radius: 8px;
}

.forgot-form {
  margin-top: 4px;
}

.forgot-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: #4a5568;
  padding-bottom: 6px;
}

.forgot-form :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #e2e8f0 inset;
}

.forgot-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #e76f51 inset, 0 0 0 3px rgba(231, 111, 81, 0.1) !important;
}

.forgot-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.forgot-footer .el-button--primary {
  background: #e76f51;
  border-color: #e76f51;
}

.forgot-footer .el-button--primary:hover {
  background: #f4a261;
  border-color: #f4a261;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 10px;
  background: #e76f51;
  border-color: #e76f51;
  letter-spacing: 2px;
  box-shadow: 0 4px 12px rgba(231, 111, 81, 0.25);
  transition: all 0.2s;
}

.login-btn:hover {
  background: #f4a261;
  border-color: #f4a261;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(231, 111, 81, 0.35);
}

/* 快捷登录体验 */
.quick-login {
  text-align: center;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px dashed #e2e8f0;
}

.quick-login span {
  font-size: 12px;
  color: #718096;
  display: block;
  margin-bottom: 12px;
  font-weight: 500;
}

.quick-btns {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.quick-tag {
  cursor: pointer;
  border-radius: 6px;
  padding: 6px 12px;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.quick-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.05);
}

.register-link {
  text-align: center;
  margin-top: 24px;
  font-size: 13px;
  color: #718096;
}

.register-link a {
  color: #e76f51;
  font-weight: 600;
  text-decoration: none;
  margin-left: 4px;
  transition: color 0.2s;
}

.register-link a:hover {
  color: #f4a261;
}

/* 适配移动端 */
@media (max-width: 1024px) {
  .login-left {
    display: none;
  }
  .login-right {
    width: 100%;
    border-left: none;
  }
}
</style>
