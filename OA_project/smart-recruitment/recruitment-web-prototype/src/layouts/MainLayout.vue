<template>
  <el-container style="height: 100vh">
    <el-aside width="220px" style="background-color: #3E2723">
      <div style="height: 60px; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 18px; font-weight: bold;">
        智能招聘系统
      </div>
      <el-menu
        :default-active="route.path"
        router
        background-color="#3E2723"
        text-color="#D4A373"
        active-text-color="#F4A261"
      >
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #eee;">
        <span style="font-size: 16px; font-weight: bold;">{{ pageTitle }}</span>
        <el-dropdown>
          <span style="cursor: pointer;">{{ userStore.userInfo.username || '用户' }} <el-icon><ArrowDown /></el-icon></span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const pageTitle = computed(() => {
  return route.meta?.title || '智能招聘系统'
})

const menuItems = computed(() => {
  const role = userStore.userInfo.role
  const menus = {
    CANDIDATE: [
      { path: '/candidate', title: '首页', icon: 'HomeFilled' },
      { path: '/candidate/jobs', title: '职位列表', icon: 'Briefcase' },
      { path: '/candidate/resume', title: '我的简历', icon: 'Document' },
      { path: '/candidate/applications', title: '投递记录', icon: 'List' },
      { path: '/candidate/mock-interview', title: '模拟面试', icon: 'ChatDotRound' },
    ],
    HR: [
      { path: '/hr', title: '工作台', icon: 'DataLine' },
      { path: '/hr/jobs', title: '职位管理', icon: 'Briefcase' },
      { path: '/hr/candidates', title: '候选人', icon: 'User' },
      { path: '/hr/interviews', title: '面试管理', icon: 'Calendar' },
    ],
    INTERVIEWER: [
      { path: '/interviewer', title: '工作台', icon: 'DataLine' },
      { path: '/interviewer/tasks', title: '面试任务', icon: 'Calendar' },
      { path: '/interviewer/questions/generate', title: 'AI生成面试题', icon: 'EditPen' },
      { path: '/interviewer/history', title: '面试历史', icon: 'Clock' },
    ],
    ADMIN: [
      { path: '/admin/users', title: '用户管理', icon: 'User' },
      { path: '/admin/roles', title: '角色管理', icon: 'UserFilled' },
      { path: '/admin/permissions', title: '权限管理', icon: 'Lock' },
      { path: '/admin/categories', title: '职位分类', icon: 'Collection' },
      { path: '/admin/logs', title: '操作日志', icon: 'DocumentChecked' },
      { path: '/admin/config', title: '系统配置', icon: 'Setting' },
    ]
  }
  return menus[role] || []
})

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>
