<template>
  <div class="page">
    <el-button icon="ArrowLeft" @click="$router.back()" link style="margin-bottom:12px;">返回面试管理</el-button>
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">面试信息</h3>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="候选人">{{ detail.candidateName }}</el-descriptions-item>
            <el-descriptions-item label="面试职位">{{ detail.jobName }}</el-descriptions-item>
            <el-descriptions-item label="面试官">{{ detail.interviewerName }}</el-descriptions-item>
            <el-descriptions-item label="面试类型">{{ detail.interviewType }}</el-descriptions-item>
            <el-descriptions-item label="面试时间">{{ formatDateTime(detail.interviewTime) }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="detail.status === 0 ? 'warning' : detail.status === 1 ? 'success' : 'danger'">{{ detail.statusLabel }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="地点/链接" :span="2">{{ detail.address || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
        <!-- 面试题与候选人答案 -->
        <el-card v-if="detail.questions && detail.questions.length" shadow="never" class="section-card">
          <h3 class="card-title">📝 面试题与候选人作答</h3>
          <div v-for="(q, i) in detail.questions" :key="q.id" class="q-item">
            <div class="q-header">
              <span class="q-num">Q{{ i + 1 }}</span>
              <span class="q-title">{{ q.title }}</span>
              <el-tag v-if="q.difficulty" size="small">{{ q.difficulty }}</el-tag>
            </div>
            <div v-if="q.candidateAnswer" class="candidate-answer">
              <strong>候选人回答：</strong>
              <p>{{ q.candidateAnswer }}</p>
            </div>
            <div v-else class="no-answer"><el-tag type="info" size="small">候选人尚未作答</el-tag></div>
          </div>
        </el-card>

        <el-card shadow="never" class="section-card">
          <h3 class="card-title">面试评价</h3>
          <div v-if="detail.evaluation">
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
          </div>
          <el-empty v-else description="暂无评价" :image-size="60" />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">操作</h3>
          <div class="action-buttons">
            <el-button v-if="detail.status === 0" type="danger" @click="handleCancel">取消面试</el-button>
            <el-button v-if="detail.status === 0" type="primary" @click="$router.push(`/hr/candidates/detail/${detail.applicationId}`)">查看候选人简历</el-button>
            <el-button v-if="detail.status === 1 && detail.evaluation" type="success" @click="handleHire(1)">录用</el-button>
            <el-button v-if="detail.status === 1 && detail.evaluation" type="danger" @click="handleHire(0)">淘汰</el-button>
            <el-button v-if="detail.status === 1 && !detail.evaluation" disabled>等待面试官提交评价</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getInterviewDetail, cancelInterview, processInterviewResult } from '@/api/interview'
import { formatDateTime } from '@/utils/date'

const route = useRoute()
const router = useRouter()
const detail = reactive({
  id: route.params.id,
  candidateName: '',
  jobName: '',
  interviewTime: '',
  interviewType: '',
  address: '',
  interviewerName: '',
  applicationId: null,
  status: 0,
  statusLabel: '',
  evaluation: null
})

async function loadDetail() {
  try {
    const res = await getInterviewDetail(route.params.id)
    if (res && res.data) {
      Object.assign(detail, res.data)
    }
  } catch (e) {
    ElMessage.error('加载面试详情失败')
  }
}

async function handleCancel() {
  try {
    await ElMessageBox.confirm('确定要取消该面试吗？', '确认操作', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await cancelInterview(route.params.id)
    ElMessage.success('面试已取消')
    detail.status = 2
    detail.statusLabel = '已取消'
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

async function handleHire(decision) {
  const actionText = decision === 1 ? '录用' : '淘汰'
  try {
    await ElMessageBox.confirm(`确定要${actionText}该候选人吗？`, '确认操作', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await processInterviewResult(route.params.id, decision)
    ElMessage.success(`已${actionText}该候选人`)
    await loadDetail()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

onMounted(loadDetail)
</script>

<style scoped>
.section-card { border-radius: 12px; margin-bottom: 16px; }
.card-title { margin: 0 0 14px; font-size: 16px; color: #3E2723; padding-bottom: 10px; border-bottom: 1px solid #f0f0f0; }

.q-item { padding: 12px 14px; margin-bottom: 10px; background: #fafafa; border-radius: 8px; border-left: 4px solid #409EFF; }
.q-header { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.q-title { flex: 1; font-weight: 500; }
.q-num { min-width: 28px; height: 28px; border-radius: 50%; background: #409EFF; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 700; flex-shrink: 0; }
.candidate-answer { margin-top: 8px; padding: 10px 12px; background: #f0f9eb; border-radius: 6px; border-left: 3px solid #67C23A; }
.candidate-answer p { margin: 4px 0 0; color: #333; white-space: pre-wrap; line-height: 1.6; }
.no-answer { margin-top: 8px; }
.action-buttons { display: flex; flex-direction: column; gap: 10px; }
.action-buttons .el-button { width: 100%; margin: 0; }
</style>
