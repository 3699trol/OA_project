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
            <p>请选择角色并输入您的凭证</p>
          </div>

          <!-- 角色快速切换 -->
          <el-radio-group v-model="loginRole" class="role-switch" size="default">
            <el-radio-button value="CANDIDATE">求职者</el-radio-button>
            <el-radio-button value="HR">HR经理</el-radio-button>
            <el-radio-button value="INTERVIEWER">面试官</el-radio-button>
            <el-radio-button value="ADMIN">管理员</el-radio-button>
          </el-radio-group>

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
                <el-checkbox v-model="rememberMe" class="remember-checkbox">记住登录密码</el-checkbox>
                <el-link type="primary" underline="never" class="forget-link">忘记密码？</el-link>
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
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const rememberMe = ref(false)
const loginRole = ref('CANDIDATE')

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码不少于6位', trigger: 'blur' }
  ]
}

const features = [
  { icon: 'Document', title: 'AI 简历解析提取', desc: '秒级解析各种格式简历，提取工作与技能核心词' },
  { icon: 'Connection', title: '双向人岗智能匹配', desc: '多维度匹配度算法模型，精准分析人才与岗位的契合度' },
  { icon: 'DataAnalysis', title: '全链路招聘大屏', desc: '可视化漏斗看板，助力企业招聘决策全方位数智化' }
]

const quickRoles = [
  { role: 'CANDIDATE', label: '🧑‍💼 张三 (求职者)', type: 'success' },
  { role: 'HR', label: '👩‍💼 李经理 (HR)', type: 'warning' },
  { role: 'INTERVIEWER', label: '👨‍🏫 王工 (面试官)', type: 'primary' },
  { role: 'ADMIN', label: '🛡️ Admin (管理员)', type: 'danger' }
]

const roleRedirects = {
  CANDIDATE: '/candidate',
  HR: '/hr',
  INTERVIEWER: '/interviewer',
  ADMIN: '/admin/users'
}

function quickLogin(r) {
  loginRole.value = r.role
  form.username = {
    CANDIDATE: 'zhangsan',
    HR: 'lijingli',
    INTERVIEWER: 'wanggong',
    ADMIN: 'admin'
  }[r.role]
  form.password = '123456'
  
  // 填充后自动触发登录
  handleLogin()
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    console.log('[Login] 发送请求', { username: form.username, password: form.password, expectedRole: loginRole.value })
    const res = await login({ 
      username: form.username, 
      password: form.password,
      expectedRole: loginRole.value
    })
    console.log('[Login] 收到响应', JSON.parse(JSON.stringify(res)))

    // 防御性安全提取
    const dataObj = res?.data || res || {}
    console.log('[Login] dataObj', dataObj)
    const userInfoObj = dataObj.userInfo || res?.userInfo || {}
    const tokenVal = dataObj.token || res?.token || ''
    console.log('[Login] userInfo', userInfoObj, 'token长度', tokenVal.length)

    // 使用后端返回的真实角色
    const actualRole = userInfoObj.role
    if (!actualRole) {
      ElMessage.error('登录失败：未获取到用户角色信息')
      return
    }
    
    // 验证角色是否匹配（双重验证）
    if (actualRole !== loginRole.value) {
      ElMessage.error('角色类型不匹配，请选择正确的角色登录')
      return
    }

    if (!tokenVal) {
      ElMessage.error('登录失败：未获取到 Token，请检查后端服务是否正常')
      return
    }
    
    // 保存至 Pinia 和 LocalStorage
    userStore.setToken(tokenVal)
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
    console.log('[Login] 异常详情: name=', e.name, 'message=', e.message, 'response=', e.response?.data)
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

.role-switch {
  width: 100%;
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}

/* 覆盖 ElementPlus RadioButton 为更高级的设计 */
.role-switch :deep(.el-radio-button__inner) {
  border-radius: 6px;
  border: 1px solid #e2e8f0;
  margin: 0 4px;
  font-size: 13px;
  font-weight: 500;
  background: #ffffff;
  color: #4a5568;
  box-shadow: none !important;
  transition: all 0.2s;
}

.role-switch :deep(.el-radio-button:first-child .el-radio-button__inner),
.role-switch :deep(.el-radio-button:last-child .el-radio-button__inner) {
  border-radius: 6px;
}

.role-switch :deep(.el-radio-button__orig-radio:checked + .el-radio-button__inner) {
  background: #e76f51;
  border-color: #e76f51;
  color: #ffffff;
  font-weight: 600;
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
