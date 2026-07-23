<template>
  <div class="page">
    <el-button icon="ArrowLeft" @click="$router.back()" link style="margin-bottom:12px;">返回任务列表</el-button>
    <el-row :gutter="20">
      <el-col :span="16">
        <!-- 面试基本信息 -->
        <el-card shadow="never" class="section-card">
          <div class="candidate-header">
            <div>
              <h2>{{ detail.candidateName }} — {{ detail.jobName }}</h2>
              <div class="meta">
                <el-tag :type="detail.status === 0 ? 'warning' : 'success'">{{ detail.statusLabel }}</el-tag>
                <span>{{ formatDateTime(detail.interviewTime) }}</span>
                <span>{{ detail.interviewType }}</span>
                <span v-if="detail.address">{{ detail.address }}</span>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 面试题 -->
        <el-card v-if="detail.questions && detail.questions.length" shadow="never" class="section-card">
          <h3 class="card-title">📝 面试题（共 {{ detail.questions.length }} 道）</h3>
          <div v-for="(q, i) in detail.questions" :key="q.id" class="q-item">
            <div class="q-header" @click="q._expanded = !q._expanded">
              <span class="q-num">Q{{ i + 1 }}</span>
              <span class="q-title">{{ q.title }}</span>
              <el-tag v-if="q.difficulty" size="small">{{ q.difficulty }}</el-tag>
              <el-tag v-if="q.questionType" size="small" type="info" effect="plain">{{ q.questionType }}</el-tag>
              <el-tag v-if="q.candidateAnswer" type="success" size="small">✓ 候选人已作答</el-tag>
              <el-icon class="expand-icon" :class="{ rotated: q._expanded }"><ArrowDown /></el-icon>
            </div>
            <div v-if="q._expanded" class="q-body">
              <div class="q-section" v-if="q.candidateAnswer">
                <strong style="color: #67C23A;">💬 候选人回答：</strong>
                <p class="answer-text">{{ q.candidateAnswer }}</p>
              </div>
              <div class="q-section">
                <strong>📖 参考答案：</strong>
                <p>{{ q.referenceAnswer || '暂无参考答案' }}</p>
              </div>
              <div class="q-section">
                <strong>📊 评分标准：</strong>
                <p>{{ q.scoringCriteria || '暂无评分标准' }}</p>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 未出题提示 -->
        <el-card v-else shadow="never" class="section-card">
          <el-empty description="尚未生成面试题，请点击右侧「AI生成面试题」开始" :image-size="80">
            <el-button type="primary" @click="$router.push(`/interviewer/questions/generate?interviewId=${route.params.id}`)">立即生成</el-button>
          </el-empty>
        </el-card>

        <!-- 面试评价 -->
        <el-card v-if="detail.evaluation" shadow="never" class="section-card">
          <h3 class="card-title">⭐ 面试评价</h3>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="技术评分">{{ detail.evaluation.technicalScore }}分</el-descriptions-item>
            <el-descriptions-item label="沟通评分">{{ detail.evaluation.communicationScore }}分</el-descriptions-item>
            <el-descriptions-item label="逻辑评分">{{ detail.evaluation.logicScore }}分</el-descriptions-item>
            <el-descriptions-item label="综合评分">{{ detail.evaluation.overallScore }}分</el-descriptions-item>
            <el-descriptions-item label="面试结果" :span="2">
              <el-tag :type="detail.evaluation.result === 2 ? 'success' : detail.evaluation.result === 1 ? 'warning' : 'danger'">
                {{ detail.evaluation.resultLabel }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
          <div v-if="detail.evaluation.evaluation" style="margin-top:16px;">
            <h4>评价内容</h4>
            <p style="color:#666;line-height:1.8;white-space:pre-wrap;">{{ detail.evaluation.evaluation }}</p>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">面试信息</h3>
          <el-descriptions direction="vertical" :column="1" size="small">
            <el-descriptions-item label="面试官">{{ detail.interviewerName }}</el-descriptions-item>
            <el-descriptions-item label="面试时间">{{ formatDateTime(detail.interviewTime) }}</el-descriptions-item>
            <el-descriptions-item label="面试类型">{{ detail.interviewType }}</el-descriptions-item>
            <el-descriptions-item label="地点/链接">{{ detail.address || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">操作</h3>
          <template v-if="detail.status === 0">
            <el-button type="primary" style="width:100%;margin-bottom:10px;" @click="$router.push(`/interviewer/questions/generate?interviewId=${route.params.id}`)">
              🤖 AI 生成面试题
            </el-button>
            <el-button type="success" style="width:100%;" @click="$router.push(`/interviewer/evaluation/${route.params.id}`)">
              ✍️ 填写面试评价
            </el-button>
          </template>
          <el-tag v-else type="info" style="width:100%;text-align:center;">面试已结束</el-tag>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { getInterviewDetail } from '@/api/interview'
import { formatDateTime } from '@/utils/date'

const route = useRoute()
const detail = reactive({
  id: route.params.id,
  candidateName: '',
  jobName: '',
  interviewTime: '',
  interviewType: '',
  address: '',
  interviewerName: '',
  status: 0,
  statusLabel: '',
  evaluation: null,
  questions: []
})

onMounted(async () => {
  try {
    const res = await getInterviewDetail(route.params.id)
    if (res && res.data) {
      const questions = (res.data.questions || []).map(q => ({ ...q, _expanded: false }))
      Object.assign(detail, res.data, { questions })
    }
  } catch (e) {
    ElMessage.error('加载面试详情失败')
  }
})
</script>

<style scoped>
.section-card { border-radius: 12px; margin-bottom: 16px; }
.card-title { margin: 0 0 14px; font-size: 16px; color: #3E2723; padding-bottom: 10px; border-bottom: 1px solid #f0f0f0; }
.candidate-header h2 { margin: 0 0 8px; font-size: 20px; color: #3E2723; }
.meta { display: flex; gap: 16px; align-items: center; font-size: 13px; color: #999; flex-wrap: wrap; }

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
.answer-text { color: #333 !important; }
</style>
