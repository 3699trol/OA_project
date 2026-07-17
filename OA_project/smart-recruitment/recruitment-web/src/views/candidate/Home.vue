<template>
  <div class="page">
    <div class="page-header"><h2>🏠 欢迎回来，{{ userStore.userInfo.username || '用户' }}</h2></div>
    <el-row :gutter="20" style="margin-top:20px;">
      <el-col :span="8" v-for="card in quickCards" :key="card.title">
        <el-card shadow="hover" :body-style="{ padding: '24px' }" class="quick-card" @click="$router.push(card.path)">
          <div class="card-inner">
            <div><h3>{{ card.title }}</h3><p>{{ card.desc }}</p></div>
            <div class="card-icon" :style="{ background: card.bg }"><el-icon size="28"><component :is="card.icon" /></el-icon></div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <h3 class="section-title">🔥 推荐职位</h3>
    <!-- TODO: 从后端获取推荐职位列表 getRecommendJobs() -->
    <el-empty description="TODO: 对接后端推荐职位接口" :image-size="80" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
const userStore = useUserStore()

const quickCards = [
  { title: '浏览职位', desc: '查看最新招聘信息', icon: 'Briefcase', path: '/candidate/jobs', bg: '#e8f4fd' },
  { title: '我的简历', desc: '完善个人信息', icon: 'Document', path: '/candidate/resume', bg: '#fdf4e8' },
  { title: '模拟面试', desc: 'AI模拟面试练习', icon: 'ChatDotRound', path: '/candidate/mock-interview', bg: '#e8f8e8' }
]

onMounted(() => {
  // TODO: 加载推荐职位 fetchRecommendJobs()
})
</script>

<style scoped>
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.quick-card { cursor: pointer; border-radius: 12px; transition: all 0.2s; }
.quick-card:hover { transform: translateY(-2px); box-shadow: 0 4px 16px rgba(0,0,0,0.08); }
.card-inner { display: flex; justify-content: space-between; align-items: center; }
.card-inner h3 { margin: 0 0 6px; font-size: 17px; color: #3E2723; }
.card-inner p { margin: 0; font-size: 13px; color: #999; }
.card-icon { width: 52px; height: 52px; border-radius: 12px; display: flex; align-items: center; justify-content: center; color: #ED8936; }
.section-title { margin: 28px 0 16px; font-size: 17px; color: #3E2723; }
</style>
