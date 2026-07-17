<template>
  <div class="page">
    <div class="page-header"><h2>📊 HR工作台</h2></div>
    <el-row :gutter="20" style="margin-top:16px;">
      <el-col :span="6" v-for="card in statsCards" :key="card.label">
        <el-card shadow="hover" class="stat-card" :style="{ borderTop: `3px solid ${card.color}` }">
          <div class="stat-inner">
            <div><div class="stat-label">{{ card.label }}</div><div class="stat-value">{{ card.value }}</div></div>
            <div class="stat-icon" :style="{ background: card.bg }"><el-icon size="28" :color="card.color"><component :is="card.icon" /></el-icon></div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20" style="margin-top:20px;">
      <el-col :span="12">
        <el-card shadow="never" class="chart-card"><h3>📈 月度招聘趋势</h3><!-- TODO: ECharts图表 --><el-empty :image-size="60" /></el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="chart-card"><h3>🍩 招聘进度</h3><!-- TODO: ECharts图表 --><el-empty :image-size="60" /></el-card>
      </el-col>
    </el-row>
    <el-card shadow="never" class="section-card" style="margin-top:20px;">
      <h3>📥 最新投递</h3>
      <el-table :data="recentApplications" v-loading="loading">
        <el-table-column prop="candidateName" label="候选人" />
        <el-table-column prop="jobTitle" label="投递职位" />
        <el-table-column prop="status" label="状态" />
        <el-table-column prop="applyTime" label="投递时间" />
        <el-table-column label="操作">
          <template #default><el-button size="small" type="primary" link>查看</el-button></template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
// import { getDashboardStats } from '@/api/dashboard'

const loading = ref(false); const recentApplications = ref([])

const statsCards = [
  { label: '在招职位', value: 0, icon: 'Briefcase', color: '#409EFF', bg: '#ecf5ff' },
  { label: '累计投递', value: 0, icon: 'Document', color: '#ED8936', bg: '#fdf4e8' },
  { label: '面试中', value: 0, icon: 'Calendar', color: '#E6A23C', bg: '#fef9f0' },
  { label: '本月入职', value: 0, icon: 'UserFilled', color: '#67C23A', bg: '#e8f8e8' }
]

onMounted(async () => {
  // TODO: const res = await getDashboardStats(); update statsCards and charts
})
</script>

<style scoped>
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.stat-card { border-radius: 12px; }
.stat-inner { display: flex; justify-content: space-between; align-items: flex-start; }
.stat-label { font-size: 13px; color: #999; margin-bottom: 6px; }
.stat-value { font-size: 28px; font-weight: 700; color: #3E2723; }
.stat-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; }
.chart-card, .section-card { border-radius: 12px; }
.chart-card h3, .section-card h3 { margin: 0 0 14px; font-size: 16px; color: #3E2723; }
</style>
