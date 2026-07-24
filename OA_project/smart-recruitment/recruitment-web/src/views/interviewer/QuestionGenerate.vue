<template>
  <div class="page">
    <div class="page-header">
      <h2>🤖 AI 生成面试题</h2>
      <el-button icon="ArrowLeft" @click="goBack" link>返回面试详情</el-button>
    </div>

    <!-- 配置区 -->
    <el-card shadow="never" class="section-card" style="max-width: 860px;">
      <el-form :model="config" label-width="90px" status-icon>
        <el-form-item label="目标职位">
          <el-select v-model="config.jobId" placeholder="请选择职位" style="width: 100%;" filterable clearable @change="onJobChange">
            <el-option
              v-for="job in jobOptions"
              :key="job.id"
              :label="`${job.jobName} (${job.department || ''})`"
              :value="job.id"
            />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="题型">
              <el-select v-model="config.questionType" style="width: 100%;" placeholder="选择题型">
                <el-option label="全部" value="" />
                <el-option label="基础理论" value="基础理论" />
                <el-option label="项目经验" value="项目经验" />
                <el-option label="系统设计" value="系统设计" />
                <el-option label="算法编程" value="算法编程" />
                <el-option label="行为面试" value="行为面试" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="难度">
              <el-select v-model="config.difficulty" style="width: 100%;" placeholder="选择难度">
                <el-option label="全部" value="" />
                <el-option label="简单" value="简单" />
                <el-option label="中等" value="中等" />
                <el-option label="困难" value="困难" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="数量">
              <el-input-number v-model="config.count" :min="1" :max="20" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item>
          <el-button type="primary" size="large" icon="MagicStick" :loading="loading" @click="generate">AI 智能生成</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- AI 生成中加载遮罩 -->
    <el-card v-if="loading" shadow="never" class="section-card loading-card">
      <div class="loading-wrap">
        <div class="loading-avatar">
          <el-icon class="is-loading"><Loading /></el-icon>
        </div>
        <div class="loading-info">
          <h3 class="loading-title">🤖 AI 正在为您智能生成面试题...</h3>
          <p class="loading-tip">{{ currentTip }}</p>
          <div class="loading-meta">
            <el-tag type="warning" effect="plain" size="large">
              <el-icon style="margin-right:4px;"><Timer /></el-icon>
              已耗时 {{ formattedElapsed }}
            </el-tag>
            <span class="loading-hint">请耐心等待，大模型正在深度分析岗位要求</span>
          </div>
          <el-progress
            :percentage="progressPercent"
            :stroke-width="8"
            :show-text="false"
            status="warning"
            style="margin-top:10px;"
          />
        </div>
      </div>
    </el-card>

    <!-- 结果展示 -->
    <el-card v-if="questions.length" shadow="never" class="section-card">
      <div class="result-header">
        <h3 class="card-title">生成的题目（共 {{ questions.length }} 道）</h3>
        <div>
          <el-button v-if="interviewId" type="success" size="large" icon="Check" :loading="saving" @click="saveQuestions">
            保存到面试并返回
          </el-button>
          <el-tag v-else type="warning" style="margin-left:8px;" effect="plain">未关联面试，无法保存</el-tag>
        </div>
      </div>

      <div v-for="(q, i) in questions" :key="i" class="q-item">
        <div class="q-header" @click="q._expanded = !q._expanded">
          <span class="q-num">Q{{ i + 1 }}</span>
          <span class="q-title">{{ q.title }}</span>
          <el-tag v-if="q.type" size="small" type="info" effect="plain">{{ q.type }}</el-tag>
          <el-tag :type="difficultyTag(q.difficulty)" size="small">{{ q.difficulty || '未指定' }}</el-tag>
          <el-icon class="expand-icon" :class="{ rotated: q._expanded }"><ArrowDown /></el-icon>
        </div>
        <div v-if="q._expanded" class="q-body">
          <div class="q-section">
            <strong>📝 参考答案：</strong>
            <p>{{ q.referenceAnswer || '暂无参考答案' }}</p>
          </div>
          <div class="q-section">
            <strong>📊 评分标准：</strong>
            <p>{{ q.scoringCriteria || '暂无评分标准' }}</p>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown, Loading, Timer } from '@element-plus/icons-vue'
import { generateQuestions, saveFormalQuestion, getInterviewDetail } from '@/api/interview'
import { getJobList } from '@/api/job'

const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const questions = ref([])
const jobOptions = ref([])
const interviewJobId = ref(null)

// === 生成中加载状态：耗时计时 + 提示轮播 + 进度条 ===
const elapsedMs = ref(0)
const loadingTimer = ref(null)
const tipTimer = ref(null)
const tipIndex = ref(0)
const GENERATE_TIPS = [
  '正在解析目标岗位的核心技能要求...',
  '正在结合行业知识库匹配最佳题型...',
  '正在调整题目难度梯度，确保由浅入深...',
  '正在为每道题撰写参考答案与评分标准...',
  '正在对生成结果进行质量校验与去重...',
  '即将完成，请稍候...'
]
const currentTip = ref(GENERATE_TIPS[0])
// 预估总耗时 30s，进度条按比例推进（最高 95% 封顶，完成时跳到 100%）
const ESTIMATE_MS = 30000
const progressPercent = computed(() => {
  if (!loading.value) return 0
  const p = Math.min(95, Math.round((elapsedMs.value / ESTIMATE_MS) * 100))
  return p
})
const formattedElapsed = computed(() => {
  const totalSec = Math.floor(elapsedMs.value / 1000)
  const m = Math.floor(totalSec / 60)
  const s = totalSec % 60
  return m > 0 ? `${m}分${s.toString().padStart(2, '0')}秒` : `${s}秒`
})

function startLoadingTimer() {
  elapsedMs.value = 0
  tipIndex.value = 0
  currentTip.value = GENERATE_TIPS[0]
  const startTime = Date.now()
  loadingTimer.value = setInterval(() => {
    elapsedMs.value = Date.now() - startTime
  }, 100)
  tipTimer.value = setInterval(() => {
    tipIndex.value = (tipIndex.value + 1) % GENERATE_TIPS.length
    currentTip.value = GENERATE_TIPS[tipIndex.value]
  }, 3000)
}

function stopLoadingTimer() {
  if (loadingTimer.value) {
    clearInterval(loadingTimer.value)
    loadingTimer.value = null
  }
  if (tipTimer.value) {
    clearInterval(tipTimer.value)
    tipTimer.value = null
  }
}

const config = reactive({
  jobId: null,
  questionType: '',
  difficulty: '',
  count: 5
})

// 从路由 query 获取 interviewId
const interviewId = new URLSearchParams(window.location.search).get('interviewId') || null

function difficultyTag(d) {
  return { '简单': 'success', '中等': 'warning', '困难': 'danger' }[d] || 'info'
}

function goBack() {
  if (interviewId) {
    router.push(`/interviewer/tasks/${interviewId}`)
  } else {
    router.back()
  }
}

async function fetchJobs() {
  try {
    const res = await getJobList({ size: 200, status: 1 })
    jobOptions.value = (res.data && res.data.records) ? res.data.records : []
  } catch (e) {
    // 静默失败
  }
}

async function loadInterviewInfo() {
  if (!interviewId) return
  try {
    const res = await getInterviewDetail(interviewId)
    if (res && res.data && res.data.jobId) {
      interviewJobId.value = res.data.jobId
      // 自动选中面试关联的职位
      config.jobId = res.data.jobId
    }
  } catch (e) {
    // 不报错，用户可以手动选
  }
}

function onJobChange() {
  questions.value = []
}

async function generate() {
  if (!config.jobId) {
    ElMessage.warning('请先选择目标职位')
    return
  }
  loading.value = true
  questions.value = []
  startLoadingTimer()
  try {
    const res = await generateQuestions({
      jobId: config.jobId,
      questionType: config.questionType || null,
      difficulty: config.difficulty || null,
      count: config.count
    })
    const data = res.data
    if (data && data.questions) {
      questions.value = data.questions.map(q => ({ ...q, _expanded: false }))
      ElMessage.success(`成功生成 ${data.questions.length} 道面试题（耗时 ${formattedElapsed.value}）`)
    } else {
      ElMessage.warning('AI 暂未返回题目，请重试')
    }
  } catch (e) {
    ElMessage.error(e.message || 'AI 生成失败')
  } finally {
    stopLoadingTimer()
    loading.value = false
  }
}

async function saveQuestions() {
  if (!interviewId) return
  saving.value = true
  try {
    await saveFormalQuestion({
      interviewId: Number(interviewId),
      questions: questions.value.map(q => ({
        title: q.title,
        type: q.type,
        difficulty: q.difficulty,
        referenceAnswer: q.referenceAnswer,
        scoringCriteria: q.scoringCriteria
      }))
    })
    ElMessage.success('面试题已保存，返回面试详情')
    // 保存成功后跳回面试详情页
    router.push(`/interviewer/tasks/${interviewId}`)
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  fetchJobs()
  loadInterviewInfo()
})

onBeforeUnmount(() => {
  stopLoadingTimer()
})
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.section-card { border-radius: 12px; margin-top: 16px; }

.result-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px; }
.card-title { margin: 0; font-size: 16px; color: #3E2723; }

.q-item { padding: 12px 14px; margin-bottom: 10px; background: #fafafa; border-radius: 8px; border-left: 4px solid #409EFF; }
.q-header { display: flex; align-items: center; gap: 10px; cursor: pointer; user-select: none; }
.q-header:hover { background: #f0f4ff; border-radius: 6px; padding: 4px 6px; margin: -4px -6px; }
.q-title { flex: 1; font-weight: 500; }
.q-num { min-width: 28px; height: 28px; border-radius: 50%; background: #409EFF; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 700; flex-shrink: 0; }
.expand-icon { transition: transform 0.2s; color: #909399; }
.expand-icon.rotated { transform: rotate(180deg); }

.q-body { margin-top: 12px; padding-top: 12px; border-top: 1px dashed #e0e0e0; }
.q-section { margin-bottom: 10px; }
.q-section p { margin: 4px 0 0; color: #555; white-space: pre-wrap; line-height: 1.6; }
.q-section strong { color: #333; }

/* AI 生成中加载卡片 */
.loading-card { border: 1px solid #fde8e4; }
.loading-wrap { display: flex; gap: 20px; align-items: flex-start; padding: 8px 4px; }
.loading-avatar {
  width: 64px; height: 64px; border-radius: 50%;
  background: linear-gradient(135deg, #e76f51 0%, #f4a261 100%);
  color: #fff; font-size: 30px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 6px 18px rgba(231, 111, 81, 0.25);
}
.loading-info { flex: 1; min-width: 0; }
.loading-title { margin: 0 0 8px; font-size: 17px; color: #3E2723; }
.loading-tip {
  margin: 0 0 12px; font-size: 14px; color: #e76f51; font-weight: 500;
  min-height: 22px; transition: opacity 0.3s;
}
.loading-meta { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.loading-hint { font-size: 12px; color: #a0aec0; }
</style>
