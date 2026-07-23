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
        <el-card shadow="never" class="section-card">
          <div class="section-header">
            <h3>📅 今日面试任务</h3>
            <el-button type="primary" link @click="$router.push('/interviewer/tasks')">查看全部任务</el-button>
          </div>
          <div v-loading="todayLoading">
            <el-timeline v-if="todayInterviews.length" class="today-timeline">
              <el-timeline-item
                v-for="item in todayInterviews"
                :key="item.id"
                :timestamp="formatTime(item.interviewTime)"
                placement="top"
                :type="statusTimelineType(item.status)"
              >
                <div class="today-item">
                  <div class="today-item-main">
                    <span class="today-candidate">{{ item.candidateName || '未知候选人' }}</span>
                    <span class="today-job">{{ item.jobName || '-' }}</span>
                  </div>
                  <div class="today-item-meta">
                    <el-tag size="small" :type="statusType(item.status)">{{ item.statusLabel }}</el-tag>
                    <el-tag v-if="item.interviewType" size="small" type="info" effect="plain">{{ item.interviewType }}</el-tag>
                    <span v-if="item.address" class="today-address">{{ item.address }}</span>
                  </div>
                  <div class="today-item-actions">
                    <el-button size="small" type="primary" link @click="$router.push(`/interviewer/tasks/${item.id}`)">详情</el-button>
                    <el-button v-if="item.status === 0" size="small" type="success" link @click="$router.push(`/interviewer/evaluation/${item.id}`)">去评价</el-button>
                  </div>
                </div>
              </el-timeline-item>
            </el-timeline>
            <el-empty v-else description="今日暂无面试安排" :image-size="80" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never" class="section-card">
          <div class="section-header">
            <h3>📝 最近评价</h3>
            <el-button type="primary" link @click="$router.push('/interviewer/history')">评价历程</el-button>
          </div>
          <div v-loading="evalLoading">
            <div v-if="recentEvaluations.length" class="eval-list">
              <div v-for="item in recentEvaluations" :key="item.id" class="eval-item" @click="$router.push(`/interviewer/tasks/${item.interviewId}`)">
                <div class="eval-item-header">
                  <span class="eval-candidate">{{ item.candidateName || '未知候选人' }}</span>
                  <el-tag size="small" :type="resultType(item.result)">{{ item.resultLabel }}</el-tag>
                </div>
                <div class="eval-item-meta">
                  <span class="eval-job">{{ item.jobName || '-' }}</span>
                  <span class="eval-score">综合分：<b>{{ item.overallScore != null ? item.overallScore : '-' }}</b></span>
                </div>
                <div v-if="item.evaluation" class="eval-text">{{ item.evaluation }}</div>
                <div class="eval-time">{{ formatTime(item.feedbackTime) }}</div>
              </div>
            </div>
            <el-empty v-else description="暂无最近评价" :image-size="80" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getInterviewerStats } from '@/api/dashboard'
import { getTodayInterviews, getRecentEvaluations } from '@/api/interview'
import { formatDateTime } from '@/utils/date'

const userStore = useUserStore()
const stats = ref([
  { label: '待面试', value: 0, icon: 'Calendar', color: '#E6A23C', bg: '#fef9f0' },
  { label: '已完成', value: 0, icon: 'Checked', color: '#67C23A', bg: '#e8f8e8' },
  { label: '本月面试', value: 0, icon: 'TrendCharts', color: '#409EFF', bg: '#ecf5ff' },
  { label: '通过率', value: '-', icon: 'DataAnalysis', color: '#ED8936', bg: '#fdf4e8' }
])
const todayLoading = ref(false)
const evalLoading = ref(false)
const todayInterviews = ref([])
const recentEvaluations = ref([])

onMounted(async () => {
  fetchStats()
  fetchTodayInterviews()
  fetchRecentEvaluations()
})

async function fetchStats() {
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
}

async function fetchTodayInterviews() {
  todayLoading.value = true
  try {
    const res = await getTodayInterviews()
    if (res.code === 200) {
      todayInterviews.value = res.data || []
    }
  } catch (e) {
    console.error('获取今日面试任务失败', e)
  } finally {
    todayLoading.value = false
  }
}

async function fetchRecentEvaluations() {
  evalLoading.value = true
  try {
    const res = await getRecentEvaluations(5)
    if (res.code === 200) {
      recentEvaluations.value = res.data || []
    }
  } catch (e) {
    console.error('获取最近评价失败', e)
  } finally {
    evalLoading.value = false
  }
}

function formatTime(value) {
  if (!value) return '-'
  // 今日面试时间只展示 时:分
  return formatDateTime(value, 'HH:mm')
}

function statusType(s) {
  const map = { 0: 'warning', 1: 'success', 2: 'info' }
  return map[s] || 'info'
}

function statusTimelineType(s) {
  const map = { 0: 'warning', 1: 'success', 2: 'info' }
  return map[s] || 'primary'
}

function resultType(r) {
  const map = { 0: 'danger', 1: 'info', 2: 'success' }
  return map[r] || 'info'
}
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
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.section-header h3 { margin: 0; }

.today-timeline { padding-left: 8px; }
.today-item { display: flex; flex-direction: column; gap: 6px; padding: 6px 0; }
.today-item-main { display: flex; align-items: center; gap: 10px; }
.today-candidate { font-size: 15px; font-weight: 700; color: #2d3748; }
.today-job { font-size: 13px; color: #718096; }
.today-item-meta { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.today-address { font-size: 12px; color: #a0aec0; }
.today-item-actions { display: flex; gap: 8px; }

.eval-list { display: flex; flex-direction: column; gap: 12px; }
.eval-item { padding: 12px; border-radius: 10px; background: #fafafa; cursor: pointer; transition: background 0.2s; }
.eval-item:hover { background: #f4f6fa; }
.eval-item-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.eval-candidate { font-size: 14px; font-weight: 700; color: #2d3748; }
.eval-item-meta { display: flex; justify-content: space-between; align-items: center; font-size: 12px; color: #718096; margin-bottom: 6px; }
.eval-job { color: #718096; }
.eval-score b { color: #E76F51; }
.eval-text { font-size: 13px; color: #4a5568; line-height: 1.6; margin: 6px 0; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.eval-time { font-size: 11px; color: #a0aec0; }
</style>
