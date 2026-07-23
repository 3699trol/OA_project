<template>
  <div class="dashboard-page">
    <div class="dashboard-header">
      <div class="header-title">
        <h2>📊 招聘监控看板</h2>
        <p class="subtitle">实时监控企业招聘漏斗、投递趋势与团队效率数据</p>
      </div>
      <el-button type="primary" class="header-btn" icon="Plus" @click="router.push('/hr/jobs')">
        发布新职位
      </el-button>
    </div>

    <!-- 顶部四色状态卡片 -->
    <el-row :gutter="24" class="stats-row">
      <el-col :span="6" v-for="card in statsCards" :key="card.label">
        <el-card shadow="hover" class="stat-card" :style="{ borderTop: `4px solid ${card.color}` }">
          <div class="stat-inner">
            <div class="stat-info">
              <span class="stat-label">{{ card.label }}</span>
              <h3 class="stat-value">{{ card.value }}</h3>
            </div>
            <div class="stat-icon" :style="{ background: card.bg }">
              <el-icon size="24" :color="card.color"><component :is="card.icon" /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表可视化区域 -->
    <el-row :gutter="24" class="charts-row">
      <!-- 投递趋势 -->
      <el-col :span="14">
        <el-card shadow="never" class="chart-card">
          <div class="card-title-bar">
            <h3>📈 月度招聘投递趋势</h3>
            <span class="card-tag">最近6个月</span>
          </div>
          
          <!-- 高质感纯 SVG 极简面积趋势图 -->
          <div class="svg-chart-container">
            <svg viewBox="0 0 600 200" class="trend-svg">
              <defs>
                <linearGradient id="gradient" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stop-color="#e76f51" stop-opacity="0.25"/>
                  <stop offset="100%" stop-color="#e76f51" stop-opacity="0"/>
                </linearGradient>
              </defs>
              <!-- 网格背景线 -->
              <line x1="0" y1="40" x2="600" y2="40" stroke="#f1f3f9" stroke-dasharray="4"/>
              <line x1="0" y1="90" x2="600" y2="90" stroke="#f1f3f9" stroke-dasharray="4"/>
              <line x1="0" y1="140" x2="600" y2="140" stroke="#f1f3f9" stroke-dasharray="4"/>
              
              <!-- 填充渐变面积 -->
              <path d="M 50,140 Q 150,90 250,50 T 450,80 T 550,30 L 550,170 L 50,140 Z" fill="url(#gradient)"/>
              
              <!-- 折线图 -->
              <path d="M 50,140 Q 150,90 250,50 T 450,80 T 550,30" fill="none" stroke="#e76f51" stroke-width="3" stroke-linecap="round"/>
              
              <!-- 节点和小圆点 -->
              <circle cx="50" cy="140" r="4" fill="#ffffff" stroke="#e76f51" stroke-width="2"/>
              <circle cx="150" cy="100" r="4" fill="#ffffff" stroke="#e76f51" stroke-width="2"/>
              <circle cx="250" cy="65" r="4" fill="#ffffff" stroke="#e76f51" stroke-width="2"/>
              <circle cx="350" cy="90" r="4" fill="#ffffff" stroke="#e76f51" stroke-width="2"/>
              <circle cx="450" cy="78" r="4" fill="#ffffff" stroke="#e76f51" stroke-width="2"/>
              <circle cx="550" cy="30" r="4" fill="#ffffff" stroke="#e76f51" stroke-width="2"/>
              
              <!-- 底部标签 -->
              <text x="50" y="190" class="chart-text">2月</text>
              <text x="150" y="190" class="chart-text">3月</text>
              <text x="250" y="190" class="chart-text">4月</text>
              <text x="350" y="190" class="chart-text">5月</text>
              <text x="450" y="190" class="chart-text">6月</text>
              <text x="550" y="190" class="chart-text">7月</text>
            </svg>
          </div>
        </el-card>
      </el-col>

      <!-- 招聘漏斗/进度 -->
      <el-col :span="10">
        <el-card shadow="never" class="chart-card">
          <div class="card-title-bar">
            <h3>🍩 投递阶段漏斗占比</h3>
            <span class="card-tag">全维度</span>
          </div>
          
          <div class="donut-chart-wrapper">
            <div class="donut-visual">
              <!-- 纯 SVG 极美环形图 -->
              <svg width="140" height="140" viewBox="0 0 36 36" class="donut-svg">
                <!-- 待筛选 (蓝色 - 20%) -->
                <circle cx="18" cy="18" r="15.915" fill="none" stroke="#2b6cb0" stroke-width="4.2" stroke-dasharray="20 80" stroke-dashoffset="25"/>
                <!-- 面试中 (橙色 - 50%) -->
                <circle cx="18" cy="18" r="15.915" fill="none" stroke="#dd6b20" stroke-width="4.2" stroke-dasharray="50 50" stroke-dashoffset="5"/>
                <!-- Offer录用 (绿色 - 20%) -->
                <circle cx="18" cy="18" r="15.915" fill="none" stroke="#38a169" stroke-width="4.2" stroke-dasharray="20 80" stroke-dashoffset="55"/>
                <!-- 淘汰 (灰色 - 10%) -->
                <circle cx="18" cy="18" r="15.915" fill="none" stroke="#a0aec0" stroke-width="4.2" stroke-dasharray="10 90" stroke-dashoffset="75"/>
              </svg>
              <div class="donut-center-text">
                <span class="d-val">100%</span>
                <span class="d-lbl">覆盖率</span>
              </div>
            </div>
            
            <div class="donut-legend">
              <div class="legend-item" v-for="item in progressData" :key="item.status">
                <span class="legend-dot" :style="{ backgroundColor: item.color }"></span>
                <span class="legend-label">{{ item.status }}</span>
                <span class="legend-value">{{ item.count }} 份</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最新投递表格区 -->
    <el-card shadow="never" class="table-card">
      <div class="card-title-bar" style="margin-bottom: 20px;">
        <h3>📥 最新投递简历处理</h3>
        <el-button type="primary" link icon="ArrowRight" @click="router.push('/hr/candidates')">
          查看全部人才库
        </el-button>
      </div>

      <el-table 
        :data="recentApplications" 
        v-loading="loading" 
        class="custom-table"
        :header-cell-style="{ background: '#f8f9fc', color: '#4a5568', fontWeight: '600', height: '48px' }"
      >
        <el-table-column prop="candidateName" label="候选人姓名" width="140">
          <template #default="scope">
            <div class="candidate-cell">
              <el-avatar :size="28" class="table-avatar">
                {{ scope.row.candidateName.substring(0, 1) }}
              </el-avatar>
              <span class="c-name">{{ scope.row.candidateName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="jobTitle" label="投递意向岗位" />
        <el-table-column prop="resumeScore" label="AI 简历契合度" width="160">
          <template #default="scope">
            <div class="score-cell">
              <el-progress 
                :percentage="scope.row.resumeScore" 
                :color="getScoreColor(scope.row.resumeScore)"
                :stroke-width="6"
                class="table-progress"
              />
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="当前所处阶段" width="140">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" effect="light" class="status-tag">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="投递时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.applyTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button 
              size="small" 
              type="primary" 
              link 
              @click="router.push('/hr/candidates')"
            >
              简历甄选
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getDashboardStats } from '@/api/dashboard'
import { formatDateTime } from '@/utils/date'

const router = useRouter()
const loading = ref(false)
const recentApplications = ref([])

const statsCards = ref([
  { label: '智能在招岗位', value: 0, icon: 'Briefcase', color: '#e76f51', bg: 'rgba(231, 111, 81, 0.08)' },
  { label: '系统累计投递', value: 0, icon: 'Document', color: '#dd6b20', bg: 'rgba(221, 107, 32, 0.08)' },
  { label: '日常面试中', value: 0, icon: 'Calendar', color: '#3182ce', bg: 'rgba(49, 130, 206, 0.08)' },
  { label: '本月入职新员工', value: 0, icon: 'UserFilled', color: '#38a169', bg: 'rgba(56, 161, 105, 0.08)' }
])

const progressData = ref([
  { status: '待筛选', count: 0, color: '#2b6cb0' },
  { status: '面试中', count: 0, color: '#dd6b20' },
  { status: '已录用', count: 0, color: '#38a169' },
  { status: '已淘汰', count: 0, color: '#a0aec0' }
])

onMounted(async () => {
  loading.value = true
  try {
    const res = await getDashboardStats()
    if (res.code === 200) {
      const stats = res.data
      statsCards.value[0].value = stats.activeJobs
      statsCards.value[1].value = stats.totalApplications
      statsCards.value[2].value = stats.interviewing
      statsCards.value[3].value = stats.onboardingThisMonth
      
      recentApplications.value = stats.recentApplications
      
      // 更新饼图数据
      const progressMapping = {
        'PENDING': '待筛选',
        'INTERVIEW': '面试中',
        'OFFER': '已录用',
        'REJECTED': '已淘汰'
      }
      
      progressData.value.forEach(item => {
        const found = stats.progressData.find(pd => pd.status === item.status)
        if (found) {
          item.count = found.count
        }
      })
    }
  } catch (e) {
    console.error('获取看板数据失败', e)
  } finally {
    loading.value = false
  }
})

function getScoreColor(score) {
  if (score >= 85) return '#38a169'
  if (score >= 70) return '#dd6b20'
  return '#e53e3e'
}

function getStatusType(status) {
  const mapping = {
    PENDING: 'info',
    INTERVIEW: 'warning',
    OFFER: 'success',
    REJECTED: 'danger'
  }
  return mapping[status] || 'info'
}

function getStatusLabel(status) {
  const mapping = {
    PENDING: '待筛选',
    INTERVIEW: '面试中',
    OFFER: '已录用',
    REJECTED: '已淘汰'
  }
  return mapping[status] || '未知'
}
</script>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 头部样式 */
.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title h2 {
  font-size: 24px;
  font-weight: 800;
  color: #1a202c;
  margin: 0 0 6px 0;
}

.header-title .subtitle {
  font-size: 13px;
  color: #718096;
  margin: 0;
}

.header-btn {
  background-color: #e76f51;
  border-color: #e76f51;
  border-radius: 8px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(231, 111, 81, 0.2);
}

.header-btn:hover {
  background-color: #f4a261;
  border-color: #f4a261;
}

/* 状态卡片 */
.stats-row {
  margin-bottom: 4px;
}

.stat-card {
  border-radius: 12px;
  border: 1px solid #f1f3f9;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.015);
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.04);
}

.stat-inner {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-label {
  font-size: 13px;
  color: #718096;
  font-weight: 500;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #2d3748;
  margin: 0;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

/* 图表区域 */
.charts-row {
  margin-bottom: 4px;
}

.chart-card {
  border-radius: 12px;
  border: 1px solid #f1f3f9;
  background: #ffffff;
}

.card-title-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-card h3, .table-card h3 {
  font-size: 16px;
  font-weight: 700;
  color: #2d3748;
  margin: 0;
}

.card-tag {
  font-size: 11px;
  color: #718096;
  background-color: #edf2f7;
  padding: 3px 8px;
  border-radius: 12px;
  font-weight: 500;
}

/* 纯 SVG 图表样式 */
.svg-chart-container {
  height: 200px;
  margin-top: 16px;
  display: flex;
  align-items: flex-end;
}

.trend-svg {
  width: 100%;
  height: 100%;
}

.chart-text {
  font-size: 11px;
  fill: #a0aec0;
  text-anchor: middle;
  font-weight: 500;
}

/* 环形进度图样式 */
.donut-chart-wrapper {
  height: 200px;
  margin-top: 16px;
  display: flex;
  align-items: center;
  justify-content: space-around;
  gap: 16px;
}

.donut-visual {
  position: relative;
  width: 140px;
  height: 140px;
}

.donut-svg {
  transform: rotate(-90deg);
}

.donut-center-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.donut-center-text .d-val {
  font-size: 20px;
  font-weight: 700;
  color: #2d3748;
}

.donut-center-text .d-lbl {
  font-size: 11px;
  color: #a0aec0;
  margin-top: 2px;
}

.donut-legend {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.legend-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.legend-label {
  font-size: 13px;
  color: #4a5568;
  font-weight: 500;
  width: 60px;
}

.legend-value {
  font-size: 13px;
  font-weight: 700;
  color: #2d3748;
}

/* 最新投递表格 */
.table-card {
  border-radius: 12px;
  border: 1px solid #f1f3f9;
}

.custom-table :deep(.el-table__row) {
  height: 60px;
}

.candidate-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.table-avatar {
  background: linear-gradient(135deg, #e76f51 0%, #f4a261 100%);
  color: #ffffff;
  font-weight: bold;
  font-size: 12px;
}

.c-name {
  font-weight: 600;
  color: #2d3748;
  font-size: 14px;
}

.score-cell {
  padding-right: 20px;
}

.status-tag {
  border-radius: 6px;
  font-weight: 600;
  padding: 4px 8px;
}
</style>
