<template>
  <div class="page">
    <div class="page-header">
      <h2>📊 HR工作台</h2>
      <span class="date">数据更新时间：{{ updateTime }}</span>
    </div>
    <!-- 统计卡片 -->
    <el-row :gutter="20" style="margin-top:16px;">
      <el-col :span="6" v-for="card in statsCards" :key="card.label">
        <el-card shadow="hover" class="stat-card" :style="{ borderTop: `3px solid ${card.color}` }">
          <div class="stat-inner">
            <div>
              <div class="stat-label">{{ card.label }}</div>
              <div class="stat-value">{{ card.value }}</div>
              <div class="stat-trend" :class="card.trend > 0 ? 'up' : 'down'">{{ card.trend > 0 ? '↑' : '↓' }} {{ Math.abs(card.trend) }}%</div>
            </div>
            <div class="stat-icon" :style="{ background: card.bg }">
              <el-icon size="28" :color="card.color"><component :is="card.icon" /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <!-- 图表区域 -->
    <el-row :gutter="20" style="margin-top:20px;">
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <h3>📈 月度招聘趋势</h3>
          <div class="chart-placeholder">
            <div class="bar-chart">
              <div v-for="bar in barData" :key="bar.month" class="bar-col">
                <div class="bar-stack">
                  <div class="bar hired" :style="{ height: bar.hired * 3 + 'px' }" :title="'录用:' + bar.hired"></div>
                  <div class="bar applied" :style="{ height: bar.applied * 3 + 'px' }" :title="'投递:' + bar.applied"></div>
                </div>
                <span class="bar-label">{{ bar.month }}</span>
              </div>
            </div>
            <div class="chart-legend">
              <span><span class="dot applied-dot"></span> 投递</span>
              <span><span class="dot hired-dot"></span> 录用</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <h3>🍩 职位招聘进度</h3>
          <div class="progress-list">
            <div v-for="job in jobProgress" :key="job.title" class="progress-item">
              <div class="progress-header">
                <span class="progress-title">{{ job.title }}</span>
                <span class="progress-num">{{ job.filled }}/{{ job.headcount }}</span>
              </div>
              <el-progress :percentage="Math.round(job.filled / job.headcount * 100)" :stroke-width="10" :color="job.filled === job.headcount ? '#67C23A' : '#ED8936'" />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <!-- 最近投递 -->
    <el-card shadow="never" class="section-card" style="margin-top:20px;">
      <h3>📥 最新投递记录</h3>
      <el-table :data="recentApplications" style="width:100%;">
        <el-table-column prop="candidateName" label="候选人" width="100" />
        <el-table-column prop="jobTitle" label="投递职位" min-width="150" />
        <el-table-column prop="matchScore" label="匹配度" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.matchScore >= 80 ? 'success' : row.matchScore >= 60 ? 'warning' : 'info'" size="small">{{ row.matchScore }}%</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="投递时间" width="140" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="$router.push('/hr/candidates/detail')">查看</el-button>
            <el-button size="small" type="success" link>初筛通过</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const updateTime = ref('2024-07-17 14:30')

const statsCards = [
  { label: '在招职位', value: 12, trend: 20, icon: 'Briefcase', color: '#409EFF', bg: '#ecf5ff' },
  { label: '累计投递', value: 347, trend: 35, icon: 'Document', color: '#ED8936', bg: '#fdf4e8' },
  { label: '面试中', value: 28, trend: -5, icon: 'Calendar', color: '#E6A23C', bg: '#fef9f0' },
  { label: '本月入职', value: 5, trend: 66, icon: 'UserFilled', color: '#67C23A', bg: '#e8f8e8' }
]

const barData = [
  { month: '1月', applied: 45, hired: 3 }, { month: '2月', applied: 32, hired: 2 },
  { month: '3月', applied: 68, hired: 5 }, { month: '4月', applied: 55, hired: 4 },
  { month: '5月', applied: 82, hired: 7 }, { month: '6月', applied: 65, hired: 5 }
]

const jobProgress = [
  { title: 'Java高级开发工程师', headcount: 3, filled: 2 },
  { title: '前端开发工程师（Vue）', headcount: 2, filled: 0 },
  { title: '数据分析师', headcount: 1, filled: 1 },
  { title: 'DevOps工程师', headcount: 2, filled: 1 },
  { title: '产品经理', headcount: 1, filled: 0 }
]

const recentApplications = [
  { candidateName: '张三', jobTitle: 'Java高级开发工程师', matchScore: 92, status: '初筛中', applyTime: '2024-07-17 14:20' },
  { candidateName: '李四', jobTitle: '前端开发工程师（Vue）', matchScore: 85, status: '初筛中', applyTime: '2024-07-17 11:15' },
  { candidateName: '王五', jobTitle: '数据分析师', matchScore: 78, status: '已查看', applyTime: '2024-07-17 09:30' },
  { candidateName: '赵六', jobTitle: 'DevOps工程师', matchScore: 91, status: '面试中', applyTime: '2024-07-16 16:45' },
  { candidateName: '陈七', jobTitle: '产品经理', matchScore: 65, status: '不合适', applyTime: '2024-07-16 10:00' }
]

function statusType(s) { return { '初筛中': 'warning', '已查看': 'info', '面试中': 'primary', '不合适': '', '待沟通': 'success' }[s] || '' }
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.date { font-size: 13px; color: #bbb; }
.stat-card { border-radius: 12px; cursor: default; }
.stat-inner { display: flex; justify-content: space-between; align-items: flex-start; }
.stat-label { font-size: 13px; color: #999; margin-bottom: 6px; }
.stat-value { font-size: 28px; font-weight: 700; color: #3E2723; }
.stat-trend { font-size: 12px; margin-top: 4px; }
.stat-trend.up { color: #67C23A; }
.stat-trend.down { color: #F56C6C; }
.stat-icon { width: 52px; height: 52px; border-radius: 12px; display: flex; align-items: center; justify-content: center; }
.chart-card { border-radius: 12px; }
.chart-card h3 { margin: 0 0 16px; font-size: 16px; color: #3E2723; }
.chart-placeholder { padding: 10px 0; }
.bar-chart { display: flex; justify-content: space-around; align-items: flex-end; height: 200px; }
.bar-col { display: flex; flex-direction: column; align-items: center; gap: 6px; }
.bar-stack { display: flex; flex-direction: column-reverse; align-items: center; width: 36px; gap: 2px; }
.bar.applied { width: 100%; background: #ED8936; border-radius: 4px 4px 0 0; opacity: 0.8; }
.bar.hired { width: 70%; background: #67C23A; border-radius: 4px 4px 0 0; }
.bar-label { font-size: 11px; color: #999; }
.chart-legend { display: flex; justify-content: center; gap: 24px; margin-top: 12px; font-size: 12px; color: #999; }
.dot { display: inline-block; width: 10px; height: 10px; border-radius: 2px; margin-right: 4px; }
.applied-dot { background: #ED8936; }
.hired-dot { background: #67C23A; }
.progress-list { display: flex; flex-direction: column; gap: 16px; }
.progress-header { display: flex; justify-content: space-between; margin-bottom: 6px; font-size: 13px; }
.progress-title { color: #3E2723; font-weight: 500; }
.progress-num { color: #999; }
.section-card { border-radius: 12px; }
.section-card h3 { margin: 0 0 14px; font-size: 16px; color: #3E2723; }
</style>
