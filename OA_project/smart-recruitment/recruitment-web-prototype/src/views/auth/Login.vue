<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="login-left">
        <div class="brand">
          <h1>Smart Recruitment</h1>
          <p>企业智慧招聘OA系统</p>
        </div>
        <div class="feature-list">
          <div class="feature-item" v-for="f in features" :key="f.title">
            <div class="feature-icon"><el-icon size="24"><component :is="f.icon" /></el-icon></div>
            <div>
              <h4>{{ f.title }}</h4>
              <span>{{ f.desc }}</span>
            </div>
          </div>
        </div>
      </div>
      <div class="login-right">
        <el-card class="login-card" shadow="always">
          <div class="card-header">
            <h2>欢迎登录</h2>
            <p>请选择角色并输入账号密码</p>
          </div>
          <el-radio-group v-model="loginRole" class="role-switch" size="large">
            <el-radio-button value="CANDIDATE">求职者</el-radio-button>
            <el-radio-button value="HR">HR</el-radio-button>
            <el-radio-button value="INTERVIEWER">面试官</el-radio-button>
            <el-radio-button value="ADMIN">管理员</el-radio-button>
          </el-radio-group>
          <el-form :model="form" :rules="rules" ref="formRef" class="login-form">
            <el-form-item prop="username">
              <el-input v-model="form.username" placeholder="请输入用户名" prefix-icon="User" size="large" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="Lock" size="large" show-password @keyup.enter="handleLogin" />
            </el-form-item>
            <el-form-item>
              <div class="form-extra">
                <el-checkbox v-model="rememberMe">记住密码</el-checkbox>
                <el-link type="primary" :underline="false">忘记密码？</el-link>
              </div>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="handleLogin">登 录</el-button>
            </el-form-item>
          </el-form>
          <div class="quick-login">
            <span>快速体验（点击直接登录）</span>
            <div class="quick-btns">
              <el-tag v-for="r in quickRoles" :key="r.role" :type="r.type" size="large" class="quick-tag" @click="quickLogin(r)">
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

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const rememberMe = ref(false)
const loginRole = ref('CANDIDATE')

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码不少于6位', trigger: 'blur' }]
}

const features = [
  { icon: 'Document', title: 'AI简历解析', desc: '智能提取教育、技能、工作经历' },
  { icon: 'Connection', title: '人岗智能匹配', desc: '精准匹配度评分与推荐排序' },
  { icon: 'DataAnalysis', title: '招聘数据看板', desc: '多维度可视化招聘数据分析' }
]

const quickRoles = [
  { role: 'CANDIDATE', label: '🧑‍💼 求职者-张三', type: 'success' },
  { role: 'HR', label: '👩‍💼 HR-李经理', type: 'warning' },
  { role: 'INTERVIEWER', label: '👨‍🏫 面试官-王工', type: 'primary' },
  { role: 'ADMIN', label: '🛡️ 管理员-admin', type: 'danger' }
]

const roleRedirects = { CANDIDATE: '/candidate', HR: '/hr', INTERVIEWER: '/interviewer', ADMIN: '/admin/users' }

function quickLogin(r) {
  loginRole.value = r.role
  form.username = { CANDIDATE: 'zhangsan', HR: 'lijingli', INTERVIEWER: 'wanggong', ADMIN: 'admin' }[r.role]
  form.password = '123456'
}

async function handleLogin() {
  await formRef.value.validate().catch(() => {})
  loading.value = true
  setTimeout(() => {
    loading.value = false
    localStorage.setItem('token', 'demo-token-2024')
    localStorage.setItem('userInfo', JSON.stringify({ username: form.username, role: loginRole.value }))
    ElMessage.success('登录成功，正在跳转...')
    router.push(roleRedirects[loginRole.value] || '/candidate')
  }, 800)
}
</script>

<style scoped>
.login-page { height: 100vh; overflow: hidden; }
.login-bg { display: flex; height: 100%; }
.login-left {
  flex: 1; background: linear-gradient(135deg, #E76F51 0%, #F4A261 50%, #3E2723 100%);
  display: flex; flex-direction: column; justify-content: center; align-items: center; color: #fff; padding: 60px;
}
.brand h1 { font-size: 42px; font-weight: 700; margin-bottom: 8px; letter-spacing: 2px; }
.brand p { font-size: 18px; opacity: 0.85; }
.feature-list { margin-top: 48px; display: flex; flex-direction: column; gap: 24px; }
.feature-item { display: flex; align-items: center; gap: 16px; }
.feature-icon { width: 48px; height: 48px; background: rgba(255,255,255,0.2); border-radius: 12px; display: flex; align-items: center; justify-content: center; }
.feature-item h4 { font-size: 16px; margin-bottom: 2px; }
.feature-item span { font-size: 13px; opacity: 0.75; }
.login-right { width: 520px; display: flex; align-items: center; justify-content: center; background: #f5f0eb; padding: 40px; }
.login-card { width: 100%; border-radius: 16px; }
.card-header { text-align: center; margin-bottom: 24px; }
.card-header h2 { font-size: 26px; color: #3E2723; margin-bottom: 6px; }
.card-header p { color: #999; font-size: 14px; }
.role-switch { width: 100%; display: flex; justify-content: center; margin-bottom: 28px; }
.login-form { margin-top: 8px; }
.form-extra { width: 100%; display: flex; justify-content: space-between; align-items: center; }
.login-btn { width: 100%; height: 44px; font-size: 16px; letter-spacing: 4px; }
.quick-login { text-align: center; margin-top: 24px; padding-top: 20px; border-top: 1px dashed #ddd; }
.quick-login span { font-size: 12px; color: #999; display: block; margin-bottom: 10px; }
.quick-btns { display: flex; flex-wrap: wrap; gap: 8px; justify-content: center; }
.quick-tag { cursor: pointer; transition: transform 0.15s; }
.quick-tag:hover { transform: scale(1.06); }
.register-link { text-align: center; margin-top: 20px; font-size: 14px; color: #999; }
</style>
