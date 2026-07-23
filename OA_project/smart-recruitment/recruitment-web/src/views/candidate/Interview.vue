<template>
  <div class="page">
    <el-button icon="ArrowLeft" @click="$router.back()" link style="margin-bottom: 12px;">返回投递列表</el-button>

    <el-row :gutter="20">
      <el-col :span="16">
        <!-- 面试信息 -->
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">📋 面试信息</h3>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="面试职位">{{ interview.jobName }}</el-descriptions-item>
            <el-descriptions-item label="面试官">{{ interview.interviewerName }}</el-descriptions-item>
            <el-descriptions-item label="面试类型">{{ interview.interviewType }}</el-descriptions-item>
            <el-descriptions-item label="面试时间">{{ interview.interviewTime }}</el-descriptions-item>
            <el-descriptions-item label="地点/链接" :span="2">{{ interview.address || '-' }}</el-descriptions-item>
            <el-descriptions-item label="状态" :span="2">
              <el-tag :type="interview.status === 0 ? 'warning' : 'success'">{{ interview.statusLabel }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 面试题 -->
        <el-card v-if="questions.length" shadow="never" class="section-card">
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <h3 class="card-title" style="border-bottom: none; margin-bottom: 0;">✏️ 面试题（共 {{ questions.length }} 道）</h3>
            <el-button type="primary" icon="Check" :loading="submitting" @click="submitAllAnswers" :disabled="interview.status !== 0">
              {{ interview.status !== 0 ? '面试已结束' : '提交全部答案' }}
            </el-button>
          </div>

          <div v-for="(q, i) in questions" :key="q.id" class="q-item">
            <div class="q-header">
              <span class="q-num">Q{{ i + 1 }}</span>
              <span class="q-title">{{ q.title }}</span>
              <el-tag v-if="q.difficulty" size="small">{{ q.difficulty }}</el-tag>
              <el-tag v-if="q.questionType" size="small" type="info" effect="plain">{{ q.questionType }}</el-tag>
            </div>
            <div class="q-answer">
              <el-input
                v-model="q._answer"
                type="textarea"
                :rows="4"
                placeholder="请输入你的回答..."
                :disabled="!!q.candidateAnswer || interview.status !== 0"
              />
              <div v-if="q.candidateAnswer" class="answer-saved">
                <el-icon><CircleCheckFilled /></el-icon> 已作答 · {{ q.answerTime }}
              </div>
            </div>
          </div>
        </el-card>

        <el-card v-else shadow="never" class="section-card">
          <el-empty description="面试官尚未出题，请耐心等待" :image-size="80" />
        </el-card>
      </el-col>

      <!-- 右侧：已提交的答案摘要 -->
      <el-col :span="8">
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">📊 答题进度</h3>
          <div style="text-align: center; padding: 16px 0;">
            <el-progress
              type="circle"
              :percentage="questions.length ? Math.round(answeredCount / questions.length * 100) : 0"
              :status="answeredCount === questions.length && questions.length > 0 ? 'success' : ''"
            />
            <p style="margin-top: 12px; color: #666;">
              已答 {{ answeredCount }} / {{ questions.length }} 题
            </p>
          </div>
          <el-divider />
          <div v-if="allAnswered && interview.status === 0" style="padding: 8px;">
            <el-alert title="全部作答完成" type="success" :closable="false" show-icon>
              <template #default>请检查答案后点击"提交全部答案"完成提交</template>
            </el-alert>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CircleCheckFilled } from '@element-plus/icons-vue'
import { getInterviewDetail, getCandidateQuestions, submitAnswer } from '@/api/interview'

const route = useRoute()
const interviewId = Number(route.params.id)

const submitting = ref(false)
const interview = reactive({
  jobName: '',
  interviewerName: '',
  interviewType: '',
  interviewTime: '',
  address: '',
  status: 0,
  statusLabel: ''
})
const questions = ref([])

const answeredCount = computed(() => questions.value.filter(q => q._answer || q.candidateAnswer).length)
const allAnswered = computed(() => questions.value.length > 0 && questions.value.every(q => q._answer || q.candidateAnswer))

async function loadInterview() {
  try {
    const res = await getInterviewDetail(interviewId)
    if (res && res.data) {
      Object.assign(interview, res.data)
    }
  } catch (e) {
    ElMessage.error('加载面试信息失败')
  }
}

async function loadQuestions() {
  try {
    const res = await getCandidateQuestions(interviewId)
    if (res && res.data) {
      questions.value = res.data.map(q => ({
        ...q,
        _answer: q.candidateAnswer || ''
      }))
    }
  } catch (e) {
    // 题目不存在时不报错
  }
}

async function submitAllAnswers() {
  const unanswered = questions.value.filter(q => !q._answer && !q.candidateAnswer)
  if (unanswered.length > 0) {
    ElMessage.warning(`还有 ${unanswered.length} 道题未作答`)
    return
  }

  submitting.value = true
  try {
    for (const q of questions.value) {
      if (!q.candidateAnswer && q._answer) {
        await submitAnswer({ questionId: q.id, answer: q._answer })
      }
    }
    ElMessage.success('全部答案已提交')
    await loadQuestions() // 刷新状态
  } catch (e) {
    ElMessage.error(e.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadInterview()
  loadQuestions()
})
</script>

<style scoped>
.section-card { border-radius: 12px; margin-bottom: 16px; }
.card-title { margin: 0 0 14px; font-size: 16px; color: #3E2723; padding-bottom: 10px; border-bottom: 1px solid #f0f0f0; }

.q-item { padding: 16px; margin-bottom: 12px; background: #fafafa; border-radius: 8px; border-left: 4px solid #409EFF; }
.q-header { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.q-title { flex: 1; font-weight: 500; }
.q-num { min-width: 28px; height: 28px; border-radius: 50%; background: #409EFF; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 700; flex-shrink: 0; }
.q-answer { margin-top: 8px; }

.answer-saved { margin-top: 6px; font-size: 12px; color: #67C23A; display: flex; align-items: center; gap: 4px; }
</style>
