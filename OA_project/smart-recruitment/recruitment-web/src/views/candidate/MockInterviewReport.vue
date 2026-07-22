<template>
  <div class="page">
    <div class="page-header">
      <div class="title-section">
        <h2>📋 AI 模拟面试诊断评估报告</h2>
        <p class="subtitle">深度还原您的技术深度，为您诊断回答盲区并指明薪资晋升方向</p>
      </div>
      <el-button icon="ArrowLeft" @click="$router.push('/candidate/mock-interview')" plain>返回面试大厅</el-button>
    </div>

    <div v-if="loading" class="loading-wrapper">
      <el-icon class="is-loading" size="48" color="#e76f51"><Loading /></el-icon>
      <p>正在拉取 AI 资深面试官评语，请稍后...</p>
    </div>

    <div v-else-if="!reportData" class="empty-wrapper">
      <el-empty description="未找到相关的模拟面试评估报告。您可以在面试大厅完成一局AI面试后生成报告！">
        <el-button type="primary" @click="$router.push('/candidate/mock-interview')">前往模拟面试</el-button>
      </el-empty>
    </div>

    <div v-else class="report-container">
      <el-row :gutter="24">
        <!-- 左边：核心得分及能力维度 -->
        <el-col :span="9">
          <!-- 得分卡 -->
          <el-card shadow="never" class="score-summary-card mb-4 text-center">
            <div class="overall-badge">
              <span class="score-num">{{ reportData.score }}</span>
              <span class="score-label">综合得分</span>
            </div>
            <h3 class="job-title-lbl">{{ reportData.jobTitle }}</h3>
            <div class="meta-row">
              <span>⏱️ 时长：{{ reportData.duration || '15分钟' }}</span> | 
              <span>📅 日期：{{ formatDate(reportData.time) }}</span>
            </div>
          </el-card>

          <!-- 维度评分 -->
          <el-card shadow="never" class="dimension-progress-card">
            <template #header>
              <span class="panel-title"><el-icon><DataAnalysis /></el-icon> 核心技术与表达评估</span>
            </template>
            <div class="dim-list">
              <div class="dim-item">
                <div class="dim-meta">
                  <span class="name">💻 技术</span>
                  <span class="score">{{ getScoreVal(reportData.report?.scores?.tech, 75) }}分</span>
                </div>
                <el-progress :percentage="getScoreVal(reportData.report?.scores?.tech, 75)" color="#67C23A" :show-text="false" stroke-width="8" />
              </div>
              <div class="dim-item">
                <div class="dim-meta">
                  <span class="name">🧠 逻辑</span>
                  <span class="score">{{ getScoreVal(reportData.report?.scores?.logic, 75) }}分</span>
                </div>
                <el-progress :percentage="getScoreVal(reportData.report?.scores?.logic, 75)" color="#f4a261" :show-text="false" stroke-width="8" />
              </div>
              <div class="dim-item">
                <div class="dim-meta">
                  <span class="name">🗣️ 表达</span>
                  <span class="score">{{ getScoreVal(reportData.report?.scores?.communication, 75) }}分</span>
                </div>
                <el-progress :percentage="getScoreVal(reportData.report?.scores?.communication, 75)" color="#409EFF" :show-text="false" stroke-width="8" />
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 右边：详细诊断 -->
        <el-col :span="15">
          <!-- 核心总评 -->
          <el-card shadow="never" class="overall-advice-card mb-4">
            <template #header>
              <span class="panel-title"><el-icon><ChatLineSquare /></el-icon> 面试官核心总评 (Overall)</span>
            </template>
            <div class="advice-content">
              <p class="summary-paragraph">{{ reportData.report?.overall }}</p>
            </div>
          </el-card>

          <!-- 逐题深度透视 / 问答建议 -->
          <el-card shadow="never" class="details-critique-card">
            <template #header>
              <span class="panel-title"><el-icon><Compass /></el-icon> 面试细节诊断与回答润色</span>
            </template>
            <div class="critique-content-body">
              <div class="critique-section">
                <label class="section-label pos"><el-icon><CircleCheck /></el-icon> 答题亮点与闪光点：</label>
                <div class="formatted-details-text">
                  <p v-if="reportData.report?.details" style="white-space: pre-line;">{{ reportData.report.details }}</p>
                  <p v-else>候选人回答流畅度良好，对 Spring Boot、JVM、并发理论等基础知识掌握扎实；能够结合过往购物车项目的缓存选型与微服务治理进行针对性思考，在表达中能主次分明地叙述要点。</p>
                </div>
              </div>
              
              <el-divider style="margin: 20px 0;" />

              <div class="critique-section">
                <label class="section-label neg"><el-icon><Warning /></el-icon> 薄弱环节与进阶建议：</label>
                <ul v-if="reportData.report?.weaknesses && reportData.report.weaknesses.length > 0" class="bullet-list-adv">
                  <li v-for="(w, i) in reportData.report.weaknesses" :key="i">{{ w }}</li>
                </ul>
                <p v-else class="no-data-text">暂无针对性建议，请参考面试对话记录进行自我评估。</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMockInterviewReport } from '@/api/ai'

const route = useRoute()
const loading = ref(false)
const reportData = ref(null)

onMounted(async () => {
  const id = route.query.id || route.params.id
  if (!id) {
    ElMessage.warning('未传入报告标识 ID')
    return
  }
  await loadReport(id)
})

async function loadReport(id) {
  loading.value = true
  try {
    const res = await getMockInterviewReport(id)
    if (res && res.data) {
      reportData.value = res.data
    }
  } catch (error) {
    console.error('获取面试报告失败:', error)
    ElMessage.error('加载面试报告数据失败')
  } finally {
    loading.value = false
  }
}

function getScoreVal(val, def) {
  if (val === undefined || val === null) return def
  return typeof val === 'number' ? val : parseInt(val, 10)
}

function formatDate(str) {
  if (!str) return new Date().toLocaleString()
  return str
}
</script>

<style scoped>
.page {
  padding: 4px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.title-section h2 {
  margin: 0;
  font-size: 24px;
  color: #1a1d21;
}

.title-section .subtitle {
  margin: 6px 0 0;
  font-size: 13px;
  color: #7a8590;
}

.loading-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 400px;
  color: #7a8590;
}

.loading-wrapper p {
  margin-top: 14px;
  font-size: 14px;
}

.empty-wrapper {
  background: white;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  padding: 40px;
  text-align: center;
}

.report-container {
  margin-top: 12px;
}

.mb-4 {
  margin-bottom: 16px;
}

.text-center {
  text-align: center;
}

/* 得分卡 */
.score-summary-card {
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  background: linear-gradient(135deg, #fffcf9 0%, #fff 100%);
  padding: 24px 10px;
}

.overall-badge {
  display: inline-block;
  text-align: center;
  margin-bottom: 16px;
}

.score-num {
  font-size: 64px;
  font-weight: 900;
  color: #e76f51;
  display: block;
  line-height: 1;
  text-shadow: 0 4px 12px rgba(231, 111, 81, 0.15);
}

.score-label {
  font-size: 11px;
  color: #718096;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-top: 4px;
  display: block;
  font-weight: 600;
}

.job-title-lbl {
  font-size: 18px;
  font-weight: 700;
  color: #2d3748;
  margin: 0 0 10px 0;
}

.meta-row {
  font-size: 12px;
  color: #a0aec0;
  margin-bottom: 20px;
}

.no-data-text {
  font-size: 13px;
  color: #a0aec0;
  margin: 0;
}
</style>
