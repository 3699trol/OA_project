<template>
  <div class="page">
    <div class="page-header">
      <h2>📊 面试官工作台</h2>
      <span>欢迎回来，{{ userStore.userInfo.username || '面试官' }}</span>
    </div>
    <!-- 统计 -->
    <el-row :gutter="20" style="margin-top:16px;">
      <el-col :span="6" v-for="card in stats" :key="card.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-inner">
            <div><div class="stat-label">{{ card.label }}</div><div class="stat-value">{{ card.value }}</div></div>
            <div class="stat-icon" :style="{ background: card.bg }"><el-icon size="28" :color="card.color"><component :is="card.icon" /></el-icon></div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <!-- 今日任务 + 最近评价 -->
    <el-row :gutter="20" style="margin-top:20px;">
      <el-col :span="14">
        <el-card shadow="never" class="section-card">
          <h3>📅 今日面试任务</h3>
          <div v-if="todayTasks.length" class="task-list">
            <div v-for="t in todayTasks" :key="t.id" class="task-item">
              <div class="task-time">{{ t.time }}</div>
              <div class="task-info">
                <h4>{{ t.candidate }} — {{ t.jobTitle }}</h4>
                <p>{{ t.type }} · {{ t.mode }} · {{ t.duration }}分钟</p>
              </div>
              <div class="task-actions">
                <el-button type="primary" size="small" @click="$router.push(`/interviewer/tasks/${t.id}`)">进入面试</el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="今天没有面试任务" :image-size="80" />
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never" class="section-card">
          <h3>📝 最近评价</h3>
          <div v-for="r in recentReviews" :key="r.id" class="review-item">
            <div class="review-top">
              <span class="review-name">{{ r.candidate }}</span>
              <el-tag :type="r.recommendation === 'PASS' ? 'success' : 'danger'" size="small">{{ r.recommendation === 'PASS' ? '通过' : '未通过' }}</el-tag>
            </div>
            <p class="review-job">{{ r.jobTitle }}</p>
            <p class="review-time">{{ r.time }}</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user'
const userStore = useUserStore()

const stats = [
  { label: '待面试', value: 6, icon: 'Calendar', color: '#E6A23C', bg: '#fef9f0' },
  { label: '已完成', value: 32, icon: 'Checked', color: '#67C23A', bg: '#e8f8e8' },
  { label: '本月面试', value: 12, icon: 'TrendCharts', color: '#409EFF', bg: '#ecf5ff' },
  { label: '通过率', value: '68%', icon: 'DataAnalysis', color: '#ED8936', bg: '#fdf4e8' }
]

const todayTasks = [
  { id: 1, candidate: '张三', jobTitle: 'Java高级开发工程师', time: '14:00 - 15:00', type: '技术面试', mode: '线上', duration: 60 },
  { id: 2, candidate: '李四', jobTitle: '前端开发工程师（Vue）', time: '15:30 - 16:30', type: '技术面试', mode: '线上', duration: 60 }
]

const recentReviews = [
  { id: 1, candidate: '王五', jobTitle: '数据分析师', recommendation: 'PASS', time: '2024-07-17' },
  { id: 2, candidate: '赵六', jobTitle: 'DevOps工程师', recommendation: 'PASS', time: '2024-07-16' },
  { id: 3, candidate: '孙八', jobTitle: 'UI设计师', recommendation: 'REJECT', time: '2024-07-15' }
]
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.page-header span { color: #999; font-size: 14px; }
.stat-card { border-radius: 12px; }
.stat-inner { display: flex; justify-content: space-between; align-items: flex-start; }
.stat-label { font-size: 13px; color: #999; margin-bottom: 6px; }
.stat-value { font-size: 28px; font-weight: 700; color: #3E2723; }
.stat-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; }
.section-card { border-radius: 12px; }
.section-card h3 { margin: 0 0 14px; font-size: 16px; color: #3E2723; }
.task-list { display: flex; flex-direction: column; gap: 12px; }
.task-item { display: flex; align-items: center; gap: 16px; padding: 14px; background: #fafafa; border-radius: 10px; }
.task-time { font-weight: 600; color: #ED8936; font-size: 14px; min-width: 100px; }
.task-info { flex: 1; }
.task-info h4 { margin: 0 0 4px; font-size: 15px; color: #3E2723; }
.task-info p { margin: 0; font-size: 12px; color: #999; }
.review-item { padding: 12px; margin-bottom: 8px; background: #fafafa; border-radius: 8px; }
.review-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; }
.review-name { font-weight: 600; font-size: 14px; color: #3E2723; }
.review-job { font-size: 13px; color: #666; margin: 0 0 2px; }
.review-time { font-size: 12px; color: #bbb; margin: 0; }
</style>
