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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { generateQuestions, saveFormalQuestion, getInterviewDetail } from '@/api/interview'
import { getJobList } from '@/api/job'

const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const questions = ref([])
const jobOptions = ref([])
const interviewJobId = ref(null)

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
      ElMessage.success(`成功生成 ${data.questions.length} 道面试题`)
    } else {
      ElMessage.warning('AI 暂未返回题目，请重试')
    }
  } catch (e) {
    ElMessage.error(e.message || 'AI 生成失败')
  } finally {
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
</style>
