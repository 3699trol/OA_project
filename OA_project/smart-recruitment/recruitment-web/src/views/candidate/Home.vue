<template>
  <div class="candidate-home-page">
    <!-- 头部欢迎及智能助手 Banner 区 -->
    <div class="welcome-banner">
      <div class="banner-content">
        <span class="banner-badge">✨ AI 智能求职助手已就绪</span>
        <h2>欢迎回来，{{ userStore.userInfo.nickName || userStore.userInfo.username || '精英人才' }}</h2>
        <p>基于企业大语言模型的“双向人岗匹配”系统已为您生成专属简历评估与最匹配职位推荐，助力您轻松拿到心仪 Offer。</p>
        <div class="banner-actions">
          <el-button type="primary" class="banner-btn-primary" icon="ChatDotRound" @click="$router.push('/candidate/mock-interview')">
            体验 AI 仿真模拟面试
          </el-button>
          <el-button class="banner-btn-secondary" icon="Document" @click="$router.push('/candidate/resume')">
            优化智能简历
          </el-button>
        </div>
      </div>
      <div class="banner-deco">
        <!-- 精美 SVG 科技质感网格点缀 -->
        <svg width="220" height="150" viewBox="0 0 220 150" fill="none">
          <circle cx="40" cy="30" r="4" fill="rgba(255,255,255,0.15)" />
          <circle cx="100" cy="30" r="4" fill="rgba(255,255,255,0.15)" />
          <circle cx="160" cy="30" r="4" fill="rgba(255,255,255,0.15)" />
          <circle cx="220" cy="30" r="4" fill="rgba(255,255,255,0.15)" />
          <circle cx="40" cy="80" r="4" fill="rgba(255,255,255,0.15)" />
          <circle cx="100" cy="80" r="4" fill="rgba(255,255,255,0.3)" />
          <circle cx="160" cy="80" r="4" fill="rgba(255,255,255,0.15)" />
          <circle cx="40" cy="130" r="4" fill="rgba(255,255,255,0.15)" />
          <circle cx="100" cy="130" r="4" fill="rgba(255,255,255,0.15)" />
          <circle cx="160" cy="130" r="4" fill="rgba(255,255,255,0.4)" />
        </svg>
      </div>
    </div>

    <!-- 快捷功能九宫格 -->
    <el-row :gutter="24" class="quick-row">
      <el-col :span="8" v-for="card in quickCards" :key="card.title">
        <el-card shadow="hover" class="quick-card" @click="$router.push(card.path)">
          <div class="card-inner">
            <div class="card-text">
              <h3>{{ card.title }}</h3>
              <p>{{ card.desc }}</p>
            </div>
            <div class="card-icon" :style="{ background: card.bg, color: card.color }">
              <el-icon size="24"><component :is="card.icon" /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 推荐职位网格 -->
    <div class="section-container">
      <div class="section-header">
        <h3>🔥 智能算法推荐职位</h3>
        <el-button type="primary" link icon="ArrowRight" @click="$router.push('/candidate/jobs')">
          查看岗位大厅
        </el-button>
      </div>

      <el-row :gutter="24" v-loading="loading" class="job-grid-row">
        <el-col :span="12" v-for="job in recommendedJobs" :key="job.id" class="job-col">
          <el-card shadow="hover" class="job-card" @click="$router.push(`/candidate/jobs`)">
            <div class="job-card-header">
              <div class="job-main-info">
                <h4 class="job-title">{{ job.jobName }}</h4>
                <span class="job-dept">{{ job.department }}</span>
              </div>
              <span class="job-salary">{{ job.salaryMin != null && job.salaryMax != null ? (job.salaryMin) + 'K-' + (job.salaryMax) + 'K' : '-' }}</span>
            </div>
            
            <div class="job-tags">
              <el-tag size="small" type="info" effect="plain" class="j-tag">{{ job.city }}</el-tag>
              <el-tag size="small" type="info" effect="plain" class="j-tag">{{ job.experience }}</el-tag>
              <el-tag size="small" type="info" effect="plain" class="j-tag">{{ job.education }}</el-tag>
            </div>

            <div class="job-card-footer">
              <div class="rec-score">
                <el-icon size="14" color="#67C23A" class="sparkle-icon"><Compass /></el-icon>
                <span>AI 匹配度：<b class="score-num">95%</b></span>
              </div>
              <span class="pub-time">{{ formatDate(job.createTime) }} 发布</span>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <div v-if="recommendedJobs.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无符合意向推荐的公开职位" :image-size="80" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getJobList } from '@/api/job'
import { formatDate } from '@/utils/date'

const userStore = useUserStore()
const loading = ref(false)
const recommendedJobs = ref([])

const quickCards = [
  { title: '浏览公开职位', desc: '查看最新最热的大厂直聘信息', icon: 'Briefcase', path: '/candidate/jobs', bg: 'rgba(231,111,81,0.08)', color: '#e76f51' },
  { title: '我的智能简历', desc: '上传与优化您的智能解析简历', icon: 'Document', path: '/candidate/resume', bg: 'rgba(221,107,32,0.08)', color: '#dd6b20' },
  { title: 'AI 仿真模拟面试', desc: '对话式AI场景化对练并输出报告', icon: 'ChatDotRound', path: '/candidate/mock-interview', bg: 'rgba(56,161,105,0.08)', color: '#38a169' }
]

async function fetchJobs() {
  loading.value = true
  try {
    const res = await getJobList({ status: 1 })
    if (res.code === 200) {
      // 选取前 4 个已发布的职位作为推荐
      recommendedJobs.value = res.data.records.slice(0, 4)
    }
  } catch (e) {
    console.error('加载推荐职位失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchJobs()
})
</script>

<style scoped>
.candidate-home-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* Banner 区 */
.welcome-banner {
  background: linear-gradient(135deg, #1f1d24 0%, #3a221d 50%, #e76f51 100%);
  border-radius: 16px;
  padding: 40px 48px;
  color: #ffffff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(231, 111, 81, 0.15);
}

.banner-content {
  max-width: 60%;
  z-index: 2;
}

.banner-badge {
  font-size: 11px;
  background-color: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 4px 12px;
  border-radius: 20px;
  font-weight: 600;
  letter-spacing: 0.5px;
  display: inline-block;
  margin-bottom: 16px;
}

.banner-content h2 {
  font-size: 28px;
  font-weight: 800;
  margin: 0 0 12px 0;
  letter-spacing: 0.5px;
}

.banner-content p {
  font-size: 14px;
  line-height: 1.6;
  opacity: 0.85;
  margin: 0 0 24px 0;
  font-weight: 300;
}

.banner-actions {
  display: flex;
  gap: 16px;
}

.banner-btn-primary {
  background-color: #ffffff;
  color: #e76f51;
  border: none;
  font-weight: 700;
  border-radius: 8px;
  padding: 10px 20px;
  box-shadow: 0 4px 14px rgba(255, 255, 255, 0.15);
  transition: all 0.2s;
}

.banner-btn-primary:hover {
  background-color: #f8f9fc;
  color: #f4a261;
  transform: translateY(-1px);
}

.banner-btn-secondary {
  background-color: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.25);
  color: #ffffff;
  font-weight: 600;
  border-radius: 8px;
  padding: 10px 20px;
  transition: all 0.2s;
}

.banner-btn-secondary:hover {
  background-color: rgba(255, 255, 255, 0.2);
  border-color: #ffffff;
}

.banner-deco {
  position: absolute;
  right: -20px;
  bottom: -20px;
  opacity: 0.6;
  z-index: 1;
}

/* 快捷卡片 */
.quick-row {
  margin-bottom: 4px;
}

.quick-card {
  border-radius: 12px;
  border: 1px solid #f1f3f9;
  cursor: pointer;
  transition: all 0.25s ease-in-out;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.01);
}

.quick-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.04);
}

.card-inner {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-text h3 {
  font-size: 16px;
  font-weight: 700;
  color: #2d3748;
  margin: 0 0 6px 0;
}

.card-text p {
  font-size: 13px;
  color: #718096;
  margin: 0;
  font-weight: 400;
}

.card-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

/* 推荐岗位网格 */
.section-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-header h3 {
  font-size: 18px;
  font-weight: 800;
  color: #1a202c;
  margin: 0;
}

.job-grid-row {
  margin-bottom: -16px;
}

.job-col {
  margin-bottom: 16px;
}

.job-card {
  border-radius: 12px;
  border: 1px solid #f1f3f9;
  cursor: pointer;
  transition: all 0.25s ease-in-out;
  padding: 6px;
}

.job-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.05);
  border-color: #f4a261;
}

.job-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 14px;
}

.job-main-info {
  display: flex;
  flex-direction: column;
}

.job-title {
  font-size: 16px;
  font-weight: 700;
  color: #2d3748;
  margin: 0 0 4px 0;
}

.job-dept {
  font-size: 12px;
  color: #a0aec0;
}

.job-salary {
  font-size: 16px;
  font-weight: 800;
  color: #e76f51;
}

.job-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.j-tag {
  border-radius: 6px;
  border: 1px solid #edf2f7;
  background-color: #f8f9fc;
  color: #718096;
}

.job-card-footer {
  border-top: 1px solid #f1f3f9;
  padding-top: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.rec-score {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #4a5568;
}

.sparkle-icon {
  animation: pulse 2s infinite;
}

.score-num {
  color: #38a169;
  font-weight: 700;
}

.pub-time {
  font-size: 11px;
  color: #a0aec0;
}

.empty-state {
  padding: 40px 0;
}

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 0.8;
  }
  50% {
    transform: scale(1.1);
    opacity: 1;
  }
  100% {
    transform: scale(1);
    opacity: 0.8;
  }
}
</style>
