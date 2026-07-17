<template>
  <div class="register-page">
    <div class="register-wrapper">
      <div class="register-brand">
        <h1>Smart Recruitment</h1>
        <p>加入智能招聘平台，发现理想工作</p>
      </div>
      <el-card class="register-card" shadow="always">
        <h2>创建账号</h2>
        <el-steps :active="step" align-center class="steps">
          <el-step title="账号信息" />
          <el-step title="选择角色" />
          <el-step title="完成注册" />
        </el-steps>
        <el-form v-if="step === 0" :model="form" :rules="rules" ref="formRef" label-width="80px">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" placeholder="4-20位字母数字" prefix-icon="User" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" type="password" placeholder="不少于6位" prefix-icon="Lock" show-password />
          </el-form-item>
          <el-form-item label="确认密码" prop="rePassword">
            <el-input v-model="form.rePassword" type="password" placeholder="再次输入密码" prefix-icon="Lock" show-password />
          </el-form-item>
          <el-form-item label="真实姓名" prop="realName">
            <el-input v-model="form.realName" placeholder="请输入姓名" prefix-icon="EditPen" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="请输入邮箱" prefix-icon="Message" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" style="width:100%;" @click="nextStep">下一步 —— 选择角色</el-button>
          </el-form-item>
        </el-form>
        <div v-if="step === 1" class="role-select">
          <h3>请选择您的角色</h3>
          <div class="role-cards">
            <div v-for="r in roles" :key="r.value" :class="['role-card', { active: selectedRole === r.value }]" @click="selectedRole = r.value">
              <div class="role-icon"><el-icon size="36"><component :is="r.icon" /></el-icon></div>
              <h4>{{ r.label }}</h4>
              <p>{{ r.desc }}</p>
            </div>
          </div>
          <el-button type="primary" size="large" style="width:100%;margin-top:24px;" :disabled="!selectedRole" @click="handleRegister">完成注册</el-button>
          <el-button size="large" style="width:100%;margin-top:10px;" @click="step = 0">返回上一步</el-button>
        </div>
        <div v-if="step === 2" class="register-success">
          <div class="success-icon"><el-icon size="64" color="#67C23A"><CircleCheckFilled /></el-icon></div>
          <h3>注册成功！</h3>
          <p>欢迎 {{ form.realName }} 加入智能招聘系统</p>
          <el-tag :type="roleTagType" size="large" style="margin-top:12px;">{{ roleLabel }}</el-tag>
          <br/>
          <el-button type="primary" size="large" style="margin-top:24px;" @click="$router.push('/login')">前往登录</el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'

const step = ref(0)
const selectedRole = ref('')
const formRef = ref()

const form = reactive({ username: '', password: '', rePassword: '', realName: '', email: '' })
const validateRePass = (rule, value, callback) => {
  if (value !== form.password) callback(new Error('两次输入密码不一致'))
  else callback()
}
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }, { min: 4, max: 20, message: '4-20个字符', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '不少于6位', trigger: 'blur' }],
  rePassword: [{ required: true, message: '请确认密码', trigger: 'blur' }, { validator: validateRePass, trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }, { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

const roles = [
  { value: 'CANDIDATE', label: '求职者', desc: '浏览职位、投递简历、AI模拟面试', icon: 'User' },
  { value: 'HR', label: 'HR', desc: '发布管理职位、筛选候选人、安排面试', icon: 'UserFilled' },
  { value: 'INTERVIEWER', label: '面试官', desc: '查看面试任务、AI生成面试题、评价面试', icon: 'Avatar' }
]

const roleLabel = computed(() => roles.find(r => r.value === selectedRole.value)?.label || '')
const roleTagType = computed(() => ({ CANDIDATE: 'success', HR: 'warning', INTERVIEWER: 'primary' }[selectedRole.value] || 'info'))

async function nextStep() {
  await formRef.value.validate().catch(() => {})
  step.value = 1
}

function handleRegister() {
  ElMessage.success('注册成功！')
  step.value = 2
}
</script>

<style scoped>
.register-page { min-height: 100vh; background: linear-gradient(135deg, #E76F51 0%, #F4A261 100%); display: flex; align-items: center; justify-content: center; padding: 40px; }
.register-wrapper { width: 560px; }
.register-brand { text-align: center; color: #fff; margin-bottom: 30px; }
.register-brand h1 { font-size: 36px; font-weight: 700; margin: 0 0 6px; }
.register-brand p { font-size: 16px; opacity: 0.85; }
.register-card { border-radius: 16px; }
.register-card h2 { text-align: center; font-size: 24px; color: #3E2723; margin: 0 0 24px; }
.steps { margin-bottom: 30px; }
.role-select h3 { text-align: center; margin-bottom: 20px; color: #3E2723; }
.role-cards { display: flex; gap: 16px; }
.role-card {
  flex: 1; padding: 28px 16px; border: 2px solid #eee; border-radius: 12px; text-align: center; cursor: pointer;
  transition: all 0.2s;
}
.role-card:hover { border-color: #F4A261; background: #fef9f3; }
.role-card.active { border-color: #ED8936; background: #fdf4e8; }
.role-icon { margin-bottom: 12px; color: #F4A261; }
.role-card.active .role-icon { color: #ED8936; }
.role-card h4 { font-size: 16px; margin: 0 0 6px; color: #3E2723; }
.role-card p { font-size: 12px; color: #999; margin: 0; }
.register-success { text-align: center; padding: 20px 0; }
.success-icon { margin-bottom: 16px; }
.register-success h3 { font-size: 22px; color: #3E2723; margin: 0 0 8px; }
.register-success p { color: #666; margin: 0; }
</style>
