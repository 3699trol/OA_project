<template>
  <div class="page">
    <div class="page-header"><h2>📊 面试官工作台</h2><span>欢迎，{{ userStore.userInfo.username || '面试官' }}</span></div>
    <el-row :gutter="20" style="margin-top:16px;">
      <el-col :span="6" v-for="card in stats" :key="card.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-inner"><div><div class="stat-label">{{ card.label }}</div><div class="stat-value">{{ card.value }}</div></div>
            <div class="stat-icon" :style="{ background: card.bg }"><el-icon size="28" :color="card.color"><component :is="card.icon" /></el-icon></div></div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20" style="margin-top:20px;">
      <el-col :span="14">
        <el-card shadow="never" class="section-card"><h3>📅 今日面试任务</h3><!-- TODO: fetch from API --><el-empty :image-size="60" /></el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never" class="section-card"><h3>📝 最近评价</h3><!-- TODO: fetch from API --><el-empty :image-size="60" /></el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getInterviewerStats } from '@/api/dashboard'

const userStore = useUserStore()
const stats = ref([
  { label: '待面试', value: 0, icon: 'Calendar', color: '#E6A23C', bg: '#fef9f0' },
  { label: '已完成', value: 0, icon: 'Checked', color: '#67C23A', bg: '#e8f8e8' },
  { label: '本月面试', value: 0, icon: 'TrendCharts', color: '#409EFF', bg: '#ecf5ff' },
  { label: '通过率', value: '-', icon: 'DataAnalysis', color: '#ED8936', bg: '#fdf4e8' }
])
onMounted(async () => {
  try {
    const res = await getInterviewerStats()
    if (res.code === 200 && res.data) {
      const d = res.data
      stats.value[0].value = d.interviewing || 0
      stats.value[1].value = d.activeJobs || 0
      stats.value[2].value = d.totalApplications || 0
      stats.value[3].value = d.onboardingThisMonth != null ? d.onboardingThisMonth + '%' : '-'
    }
  } catch (e) {
    console.error('获取面试官看板数据失败', e)
  }
})
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
</style>
