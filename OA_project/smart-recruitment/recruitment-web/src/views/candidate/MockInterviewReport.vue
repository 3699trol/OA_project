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
            <div class="level-badge" :class="getLevelClass(reportData.score)">
              推荐定级：{{ getLevelText(reportData.score) }}
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
                  <span class="name">💻 技术基础与理论</span>
                  <span class="score">{{ getScoreVal(reportData.report?.scores?.base, 80) }}分</span>
                </div>
                <el-progress :percentage="getScoreVal(reportData.report?.scores?.base, 80)" color="#67C23A" :show-text="false" stroke-width="8" />
              </div>
              <div class="dim-item">
                <div class="dim-meta">
                  <span class="name">⚡ 框架与工程实战</span>
                  <span class="score">{{ getScoreVal(reportData.report?.scores?.framework, 85) }}分</span>
                </div>
                <el-progress :percentage="getScoreVal(reportData.report?.scores?.framework, 85)" color="#e76f51" :show-text="false" stroke-width="8" />
              </div>
              <div class="dim-item">
                <div class="dim-meta">
                  <span class="name">🏗️ 架构与高并发设计</span>
                  <span class="score">{{ getScoreVal(reportData.report?.scores?.design, 82) }}分</span>
                </div>
                <el-progress :percentage="getScoreVal(reportData.report?.scores?.design, 82)" color="#f4a261" :show-text="false" stroke-width="8" />
              </div>
              <div class="dim-item">
                <div class="dim-meta">
                  <span class="name">🗣️ 语言表达与逻辑STAR</span>
                  <span class="score">{{ getScoreVal(reportData.report?.scores?.communication, 80) }}分</span>
                </div>
                <el-progress :percentage="getScoreVal(reportData.report?.scores?.communication, 80)" color="#409EFF" :show-text="false" stroke-width="8" />
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
                <ul class="bullet-list-adv">
                  <li><b>高流量削峰方案：</b>在回答 Redis 缓存穿透防范时，可进一步补充布隆过滤器的预热及内存占用估算细节，使方案更加闭环。</li>
                  <li><b>分布式一致性：</b>对 Seata AT 模式和 MQ 最终一致性模式下的事务边界异常回滚处理（如 RocketMQ 事务回查异常和死信队列）细节阐述较浅。</li>
                  <li><b>GC 调优实战：</b>在 JVM GC 排查中，可以补充对于 OutOfMemoryError 时产生 Heap Dump 文件的分析命令（如 jhat/jvisualvm）及具体的线上诊断路径，增强资深说服力。</li>
                </ul>
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

function getLevelText(score) {
  if (score >= 90) return '资深研发专家 (P7 / Lead)'
  if (score >= 82) return '高级工程师 (P6 / Senior)'
  return '中级开发工程师 (P5 / Normal)'
}

function getLevelClass(score) {
  if (score >= 90) return 'level-expert'
  if (score >= 82) return 'level-senior'
  return 'level-normal'
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

.level-badge {
  display: inline-block;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 700;
}

.level-badge.level-expert {
  background-color: #e8f5e9;
  color: #2e7d32;
  border: 1px solid #c8e6c9;
}

.level-badge.level-senior {
  background-color: #fff3e0;
  color: #e65100;
  border: 1px solid #ffe0b2;
}

.level-badge.level-normal {
  background-color: #e3f2fd;
  color: #1565c0;
  border: 1px solid #bbdefb;
}

/* 维度卡 */
.dimension-progress-card {
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.panel-title {
  font-size: 15px;
  font-weight: 700;
  color: #2d3748;
  display: flex;
  align-items: center;
  gap: 8px;
}

.dim-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.dim-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.dim-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12.5px;
}

.dim-meta .name {
  color: #4a5568;
  font-weight: 600;
}

.dim-meta .score {
  color: #2d3748;
  font-weight: 700;
}

/* 右边建议及详情 */
.overall-advice-card {
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.summary-paragraph {
  font-size: 13.5px;
  color: #4a5568;
  line-height: 1.7;
  margin: 0;
  text-indent: 2em;
}

.details-critique-card {
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.critique-section {
  padding: 4px;
}

.section-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 700;
  margin-bottom: 12px;
}

.section-label.pos {
  color: #67C23A;
}

.section-label.neg {
  color: #e76f51;
}

.formatted-details-text {
  font-size: 13px;
  color: #4a5568;
  line-height: 1.6;
}

.bullet-list-adv {
  padding-left: 18px;
  margin: 0;
  font-size: 13px;
  color: #4a5568;
  line-height: 1.7;
}

.bullet-list-adv li {
  margin-bottom: 10px;
}

.bullet-list-adv b {
  color: #1a1d21;
}
</style>
