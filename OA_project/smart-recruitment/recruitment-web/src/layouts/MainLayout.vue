<template>
  <el-container class="app-container">
    <!-- 侧边栏 -->
    <el-aside width="240px" class="aside-bar">
      <div class="logo-wrapper">
        <div class="logo-icon">SR</div>
        <div class="logo-text">
          <span class="brand-title">Smart Recruit</span>
          <span class="brand-subtitle">智慧招聘管理系统</span>
        </div>
      </div>
      
      <div class="menu-scroll-wrapper">
        <el-menu
          :default-active="route.path"
          router
          class="custom-menu"
        >
          <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
            <el-icon class="menu-icon"><component :is="item.icon" /></el-icon>
            <template #title>
              <span class="menu-title">{{ item.title }}</span>
            </template>
          </el-menu-item>
        </el-menu>
      </div>
    </el-aside>

    <!-- 主内容区 -->
    <el-container class="main-container">
      <!-- 头部 -->
      <el-header class="header-bar">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>{{ pageTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <!-- 收件箱消息通知钟 -->
          <div class="inbox-bell-trigger" @click="inboxVisible = true">
            <el-badge :value="inboxStore.unreadCount" :max="99" :hidden="inboxStore.unreadCount === 0" class="bell-badge">
              <el-icon class="bell-icon"><Bell /></el-icon>
            </el-badge>
          </div>
          
          <el-dropdown trigger="click" class="user-dropdown">
            <div class="user-info">
              <el-avatar :size="32" class="user-avatar">
                {{ (userStore.userInfo.nickName || userStore.userInfo.username || 'U').substring(0, 1) }}
              </el-avatar>
              <span class="user-name">
                {{ userStore.userInfo.nickName || userStore.userInfo.username || '用户' }}
                <span class="user-role-badge">{{ roleText }}</span>
              </span>
              <el-icon class="dropdown-arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="custom-dropdown-menu">
                <div class="dropdown-user-header">
                  <p class="h-name">{{ userStore.userInfo.nickName || userStore.userInfo.username }}</p>
                  <p class="h-role">{{ roleText }}</p>
                </div>
                <el-dropdown-item @click="openProfileDialog">
                  <el-icon><User /></el-icon>个人资料
                </el-dropdown-item>
                <el-dropdown-item @click="changePasswordVisible = true">
                  <el-icon><Lock /></el-icon>修改密码
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout" class="logout-item">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主视图 -->
      <el-main class="view-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>

    <!-- 收件箱抽屉 -->
    <el-drawer
      v-model="inboxVisible"
      size="440px"
      destroy-on-close
      class="inbox-custom-drawer"
      :show-close="false"
    >
      <template #header>
        <div class="inbox-header">
          <h3>📬 收件箱通知中心</h3>
          <el-button v-if="inboxStore.messages.length > 0" type="primary" size="small" link @click="inboxStore.markAllAsRead">全部标记已读</el-button>
        </div>
      </template>

      <!-- 消息列表 -->
      <div v-if="inboxStore.messages.length === 0" class="empty-inbox">
        <el-empty description="消息收件箱目前空空如也哦" :image-size="80" />
      </div>
      
      <div v-else class="inbox-msg-list">
        <div 
          v-for="msg in inboxStore.messages" 
          :key="msg.id" 
          class="inbox-msg-item"
          :class="{ unread: !msg.read }"
          @click="readMessage(msg)"
        >
          <div class="msg-dot" v-if="!msg.read"></div>
          <div class="msg-item-header">
            <span class="msg-title">{{ msg.title }}</span>
            <span class="msg-time">{{ msg.time }}</span>
          </div>
          <p class="msg-content">{{ msg.content }}</p>
          <div class="msg-actions">
            <el-button type="primary" size="small" link v-if="msg.link" @click.stop="handleNavigate(msg)">查看详情</el-button>
            <el-button type="danger" size="small" link @click.stop="inboxStore.deleteMessage(msg.id)">删除消息</el-button>
          </div>
        </div>
      </div>

      <!-- 快捷 Mock 消息发生器 -->
      <template #footer v-if="isMock">
        <div class="mock-generator-panel">
          <p class="panel-lbl">🛠️ WebSocket 简历 & 面试结果推送仿真</p>
          <div class="generator-btns">
            <el-button size="small" type="success" @click="triggerMockPush('RESUME_PASS')">初筛通过</el-button>
            <el-button size="small" type="warning" @click="triggerMockPush('INTERVIEW_SCHEDULE')">面试安排</el-button>
            <el-button size="small" type="primary" @click="triggerMockPush('OFFER')">录用 Offer</el-button>
            <el-button size="small" type="danger" @click="triggerMockPush('REJECT')">感谢信</el-button>
          </div>
        </div>
      </template>
    </el-drawer>

    <!-- 个人资料对话框 -->
    <el-dialog
      v-model="profileVisible"
      title="个人资料"
      width="440px"
      destroy-on-close
    >
      <el-form :model="profileForm" :rules="profileRules" ref="profileFormRef" label-width="90px" v-loading="profileLoading">
        <el-form-item label="用户名">
          <el-input :model-value="userStore.userInfo.username" disabled />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="profileForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="profileVisible = false">取消</el-button>
        <el-button type="primary" :loading="profileSubmitting" @click="handleUpdateProfile">保存</el-button>
      </template>
    </el-dialog>

    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="changePasswordVisible"
      title="修改密码"
      width="420px"
      destroy-on-close
    >
      <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-width="90px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码（不少于6位）" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="changePasswordVisible = false">取消</el-button>
        <el-button type="primary" :loading="pwdSubmitting" @click="handleChangePassword">确认修改</el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useInboxStore } from '@/stores/inbox'
import { simulateWebSocketPush } from '@/utils/websocket'
import { ElMessage } from 'element-plus'
import { changePassword, getCurrentUser, updateProfile } from '@/api/auth'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const inboxStore = useInboxStore()

const isMock = ref(localStorage.getItem('use_mock') !== 'false')
const inboxVisible = ref(false)

// 个人资料
const profileVisible = ref(false)
const profileLoading = ref(false)
const profileSubmitting = ref(false)
const profileFormRef = ref()
const profileForm = ref({ realName: '', phone: '', email: '' })
const profileRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

async function openProfileDialog() {
  profileVisible.value = true
  profileLoading.value = true
  try {
    const res = await getCurrentUser()
    const data = res?.data || res || {}
    profileForm.value = {
      realName: data.realName || '',
      phone: data.phone || '',
      email: data.email || ''
    }
  } catch (e) {
    ElMessage.error(e.message || '获取个人资料失败')
  } finally {
    profileLoading.value = false
  }
}

async function handleUpdateProfile() {
  const valid = await profileFormRef.value.validate().catch(() => false)
  if (!valid) return

  profileSubmitting.value = true
  try {
    await updateProfile({
      realName: profileForm.value.realName,
      phone: profileForm.value.phone,
      email: profileForm.value.email
    })
    // 同步更新本地用户信息（昵称等）
    userStore.setUserInfo({
      ...userStore.userInfo,
      nickName: profileForm.value.realName
    })
    ElMessage.success('个人资料更新成功')
    profileVisible.value = false
  } catch (e) {
    ElMessage.error(e.message || '个人资料更新失败')
  } finally {
    profileSubmitting.value = false
  }
}

// 修改密码
const changePasswordVisible = ref(false)
const pwdSubmitting = ref(false)
const pwdFormRef = ref()
const pwdForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码不少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

async function handleChangePassword() {
  const valid = await pwdFormRef.value.validate().catch(() => false)
  if (!valid) return

  pwdSubmitting.value = true
  try {
    await changePassword({
      oldPassword: pwdForm.value.oldPassword,
      newPassword: pwdForm.value.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    changePasswordVisible.value = false
    setTimeout(() => {
      userStore.logout()
      router.push('/login')
    }, 800)
  } catch (e) {
    ElMessage.error(e.message || '密码修改失败')
  } finally {
    pwdSubmitting.value = false
  }
}

function readMessage(msg) {
  inboxStore.markAsRead(msg.id)
}

function handleNavigate(msg) {
  inboxStore.markAsRead(msg.id)
  inboxVisible.value = false
  if (msg.link) {
    router.push(msg.link)
  }
}

function triggerMockPush(type) {
  simulateWebSocketPush(type)
}

const pageTitle = computed(() => {
  return route.meta?.title || '智能招聘系统'
})

const roleText = computed(() => {
  const mapping = {
    ADMIN: '管理员',
    HR: 'HR经理',
    INTERVIEWER: '面试官',
    CANDIDATE: '求职者'
  }
  return mapping[userStore.userInfo.role] || '用户'
})

const menuItems = computed(() => {
  const role = userStore.userInfo.role
  const menus = {
    CANDIDATE: [
      { path: '/candidate', title: '智能求职首页', icon: 'HomeFilled' },
      { path: '/candidate/jobs', title: '全职岗位大厅', icon: 'Briefcase' },
      { path: '/candidate/resume', title: '我的智能简历', icon: 'Document' },
      { path: '/candidate/applications', title: '投递进度追踪', icon: 'List' },
      { path: '/candidate/mock-interview', title: 'AI 仿真模拟面试', icon: 'ChatDotRound' },
    ],
    HR: [
      { path: '/hr', title: '招聘监控大屏', icon: 'DataLine' },
      { path: '/hr/jobs', title: '发布职位管理', icon: 'Briefcase' },
      { path: '/hr/candidates', title: '候选人才库', icon: 'User' },
      { path: '/hr/interviews', title: '日常面试安排', icon: 'Calendar' },
    ],
    INTERVIEWER: [
      { path: '/interviewer', title: '面试官工作台', icon: 'DataLine' },
      { path: '/interviewer/tasks', title: '待面试任务', icon: 'Calendar' },
      { path: '/interviewer/questions/generate', title: 'AI 面试出题器', icon: 'EditPen' },
      { path: '/interviewer/history', title: '我评审的历程', icon: 'Clock' },
    ],
    ADMIN: [
      { path: '/admin/users', title: '系统用户主表', icon: 'User' },
      { path: '/admin/roles', title: '系统角色权限', icon: 'UserFilled' },
      // { path: '/admin/permissions', title: '权限节点定义', icon: 'Lock' }, // 暂时屏蔽
      { path: '/admin/categories', title: '行业岗位分类', icon: 'Collection' },
      { path: '/admin/logs', title: '后台审计日志', icon: 'DocumentChecked' },
      // { path: '/admin/config', title: '智能AI系统参数', icon: 'Setting' }, // 暂时屏蔽
    ]
  }
  return menus[role] || []
})

function toggleMockMode() {
  const current = localStorage.getItem('use_mock') !== 'false'
  localStorage.setItem('use_mock', !current ? 'true' : 'false')
  isMock.value = !current
  ElMessage.success({
    message: isMock.value ? '已成功切换为 Mock 模拟数据，页面体验更丝滑' : '已关闭 Mock，正在尝试连接本地后端开发服务',
    duration: 3000
  })
  // 稍后刷新，使请求重载
  setTimeout(() => {
    window.location.reload()
  }, 800)
}

function handleLogout() {
  userStore.logout()
  router.push('/login')
  ElMessage.success('登出成功')
}
</script>

<style scoped>
/* 全局布局 */
.app-container {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background-color: #f8f9fc;
}

/* 侧边栏 */
.aside-bar {
  background-color: #1a1d21;
  display: flex;
  flex-direction: column;
  box-shadow: 4px 0 24px rgba(0, 0, 0, 0.15);
  z-index: 10;
  transition: all 0.3s;
}

.logo-wrapper {
  height: 72px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  border-bottom: 1px solid #2b3036;
}

.logo-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #e76f51 0%, #f4a261 100%);
  border-radius: 8px;
  color: #fff;
  font-weight: bold;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  box-shadow: 0 4px 10px rgba(231, 111, 81, 0.3);
}

.logo-text {
  display: flex;
  flex-direction: column;
}

.brand-title {
  color: #ffffff;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.brand-subtitle {
  color: #7a8590;
  font-size: 11px;
  margin-top: 2px;
}

.menu-scroll-wrapper {
  flex: 1;
  overflow-y: auto;
  padding: 16px 12px;
}

/* 隐藏侧边栏滚动条 */
.menu-scroll-wrapper::-webkit-scrollbar {
  width: 0;
}

/* Element Menu 自定义样式 */
.custom-menu {
  border-right: none;
  background-color: transparent;
}

:deep(.el-menu) {
  border-right: none;
  background-color: transparent;
}

.custom-menu :deep(.el-menu-item) {
  height: 48px;
  line-height: 48px;
  margin-bottom: 8px;
  border-radius: 8px;
  color: #a3aed0;
  padding: 0 16px !important;
  transition: all 0.2s ease-in-out;
}

.custom-menu :deep(.el-menu-item:hover) {
  color: #ffffff;
  background-color: #2b3036;
}

.custom-menu :deep(.el-menu-item.is-active) {
  color: #ffffff;
  background: linear-gradient(90deg, rgba(231, 111, 81, 0.15) 0%, rgba(244, 162, 97, 0.05) 100%);
  border-left: 3px solid #e76f51;
  font-weight: 600;
  padding-left: 13px !important; /* 加上 border 后微调内边距保持对齐 */
}

.menu-icon {
  margin-right: 12px;
  font-size: 18px;
  color: inherit;
}

.menu-title {
  font-size: 14px;
}

/* 侧边栏底部 */
.aside-footer {
  padding: 16px 20px;
  border-top: 1px solid #2b3036;
  background-color: #15181a;
}

.mock-badge {
  font-size: 11px;
  color: #7a8590;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  background-color: #1e2225;
  border-radius: 6px;
}

.mock-badge .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: #909399;
}

.mock-badge.is-mock {
  color: #f4a261;
}

.mock-badge.is-mock .dot {
  background-color: #f4a261;
  box-shadow: 0 0 6px #f4a261;
}

/* 主内容容器 */
.main-container {
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* 头部样式 */
.header-bar {
  height: 72px !important;
  background-color: #ffffff;
  border-bottom: 1px solid #f1f3f9;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  z-index: 9;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.mock-toggle-tag {
  cursor: pointer;
  border-radius: 12px;
  padding: 0 12px;
  font-weight: 600;
  transition: transform 0.1s;
}

.mock-toggle-tag:hover {
  transform: scale(1.04);
}

.user-dropdown {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px;
  border-radius: 20px;
  transition: background-color 0.2s;
}

.user-info:hover {
  background-color: #f8f9fc;
}

.user-avatar {
  background: linear-gradient(135deg, #e76f51 0%, #f4a261 100%);
  color: #ffffff;
  font-weight: 700;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #2d3748;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.user-role-badge {
  font-size: 10px;
  font-weight: 500;
  color: #718096;
  background-color: #edf2f7;
  padding: 1px 6px;
  border-radius: 4px;
  margin-top: 1px;
}

.dropdown-arrow {
  font-size: 12px;
  color: #a0aec0;
}

/* 下拉菜单自定义 */
.custom-dropdown-menu {
  padding: 0;
  border-radius: 12px;
  border: 1px solid #f1f3f9;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08) !important;
}

.dropdown-user-header {
  padding: 16px 20px;
  background-color: #f8f9fc;
  border-top-left-radius: 12px;
  border-top-right-radius: 12px;
  text-align: center;
}

.dropdown-user-header .h-name {
  font-size: 15px;
  font-weight: 700;
  color: #2d3748;
  margin: 0 0 4px 0;
}

.dropdown-user-header .h-role {
  font-size: 11px;
  color: #718096;
  margin: 0;
}

.logout-item {
  color: #e53e3e !important;
  padding: 12px 20px !important;
}

.logout-item:hover {
  background-color: #fff5f5 !important;
}

/* 视图主区域 */
.view-main {
  flex: 1;
  overflow-y: auto;
  padding: 32px;
}

/* 页面切换动画 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.25s ease-out;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateY(-12px);
}

/* ==================== 收件箱及消息钟 ==================== */
.inbox-bell-trigger {
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background-color: #f8f9fc;
  transition: background-color 0.2s, transform 0.1s;
}

.inbox-bell-trigger:hover {
  background-color: #edf2f7;
  transform: scale(1.05);
}

.bell-icon {
  font-size: 18px;
  color: #718096;
}

.bell-badge :deep(.el-badge__content.is-fixed) {
  top: 4px;
  right: 4px;
}

.inbox-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding-bottom: 4px;
}

.inbox-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #2d3748;
}

.empty-inbox {
  padding: 60px 0;
  text-align: center;
}

.inbox-msg-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 4px 0;
}

.inbox-msg-item {
  position: relative;
  background-color: #ffffff;
  border: 1px solid #edf2f7;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.inbox-msg-item:hover {
  border-color: #e76f51;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.04);
}

.inbox-msg-item.unread {
  background-color: #fffbf9;
  border-color: #fceed9;
}

.msg-dot {
  position: absolute;
  top: 21px;
  left: 6px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: #e76f51;
  box-shadow: 0 0 6px #e76f51;
}

.msg-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.msg-title {
  font-size: 13.5px;
  font-weight: 700;
  color: #2d3748;
}

.msg-time {
  font-size: 10px;
  color: #a0aec0;
}

.msg-content {
  font-size: 12px;
  color: #4a5568;
  line-height: 1.6;
  margin: 0 0 12px 0;
}

.msg-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.msg-actions .el-button {
  font-size: 12px;
  padding: 0;
}

.mock-generator-panel {
  border-top: 1px solid #edf2f7;
  padding-top: 14px;
}

.panel-lbl {
  font-size: 11px;
  font-weight: 700;
  color: #718096;
  margin-bottom: 10px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.generator-btns {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.generator-btns .el-button {
  margin: 0 !important;
  border-radius: 6px;
}
</style>
