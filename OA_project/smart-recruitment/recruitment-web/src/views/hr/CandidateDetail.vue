<template>
  <div class="page">
    <el-button icon="ArrowLeft" @click="$router.back()" link style="margin-bottom:12px;">返回候选人列表</el-button>
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="never" class="section-card">
          <div style="text-align:center;">
            <el-avatar :size="80" :src="candidate.avatarUrl">{{ candidate.candidateName?.[0] }}</el-avatar>
            <h3>{{ candidate.candidateName }}</h3>
            <el-tag :type="statusType(candidate.status)" effect="plain" round style="margin-top:8px;">{{ statusLabel(candidate.status) }}</el-tag>
          </div>
          <el-divider />
          <div class="info-list">
            <div class="info-item" v-if="candidate.phone"><el-icon><Phone /></el-icon> {{ candidate.phone }}</div>
            <div class="info-item" v-if="candidate.email"><el-icon><Message /></el-icon> {{ candidate.email }}</div>
            <div class="info-item" v-if="candidate.jobName"><el-icon><Briefcase /></el-icon> 投递职位：{{ candidate.jobName }}</div>
            <div class="info-item" v-if="candidate.department"><el-icon><OfficeBuilding /></el-icon> 部门：{{ candidate.department }}</div>
            <div class="info-item" v-if="candidate.applyTime"><el-icon><Clock /></el-icon> 投递时间：{{ candidate.applyTime }}</div>
          </div>
          <el-divider />
          <div style="text-align:center; display:flex; gap:12px; justify-content:center;">
            <el-button type="primary" @click="handleStatusChange(1)" :loading="statusLoading">初筛通过</el-button>
            <el-button type="danger" plain @click="handleStatusChange(3)" :loading="statusLoading">不合适</el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">📄 简历详情</h3>
          <div v-if="resumeData" class="resume-content">
            <div v-if="resumeData.basicInfo" class="resume-section">
              <h4>基本信息</h4>
              <el-descriptions :column="2" border size="small">
                <el-descriptions-item v-for="(val, key) in resumeData.basicInfo" :key="key" :label="key">{{ val }}</el-descriptions-item>
              </el-descriptions>
            </div>
            <div v-if="resumeData.educations?.length" class="resume-section">
              <h4>教育经历</h4>
              <div v-for="(edu, idx) in resumeData.educations" :key="idx" class="resume-block">
                <p><strong>{{ edu.school }}</strong> - {{ edu.major }} ({{ edu.degree }}) {{ edu.startDate }} ~ {{ edu.endDate }}</p>
              </div>
            </div>
            <div v-if="resumeData.experiences?.length" class="resume-section">
              <h4>工作经历</h4>
              <div v-for="(exp, idx) in resumeData.experiences" :key="idx" class="resume-block">
                <p><strong>{{ exp.company }}</strong> - {{ exp.position }} ({{ exp.startDate }} ~ {{ exp.endDate }})</p>
                <p v-if="exp.description">{{ exp.description }}</p>
              </div>
            </div>
            <div v-if="resumeData.skills?.length" class="resume-section">
              <h4>技能标签</h4>
              <div class="skill-tags">
                <el-tag v-for="s in resumeData.skills" :key="s" size="default" effect="plain">{{ s }}</el-tag>
              </div>
            </div>
            <div v-if="candidate.selfEvaluation" class="resume-section">
              <h4>自我评价</h4>
              <p class="eval-text">{{ candidate.selfEvaluation }}</p>
            </div>
            <div v-if="!resumeData.basicInfo && !resumeData.educations?.length && !resumeData.experiences?.length && !resumeData.skills?.length" class="resume-empty">
              <el-empty description="简历内容为空" :image-size="60" />
            </div>
          </div>
          <el-empty v-else description="暂无简历数据" :image-size="80" />
        </el-card>
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">🤖 AI匹配分析</h3>
          <div v-if="candidate.aiMatchScore != null">
            <el-progress :percentage="Number(candidate.aiMatchScore)" :color="'#E76F51'" :stroke-width="12" />
            <p v-if="candidate.aiMatchReason" style="margin-top:12px; color:#666; font-size:14px;">{{ candidate.aiMatchReason }}</p>
          </div>
          <el-empty v-else description="暂无AI匹配数据" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Phone, Message, Briefcase, Clock, OfficeBuilding } from '@element-plus/icons-vue'
import { getCandidateDetail, updateApplicationStatus } from '@/api/application'

const route = useRoute()
const router = useRouter()
const statusLoading = ref(false)

const candidate = reactive({
  id: '', candidateName: '', username: '', email: '', phone: '', avatarUrl: '',
  jobId: '', jobName: '', department: '', category: '',
  resumeId: '', status: 0, applyTime: '', aiMatchScore: null, aiMatchReason: '', selfEvaluation: ''
})
const resumeData = ref(null)

function statusLabel(s) {
  const map = { 0: '待筛选', 1: '面试中', 2: '已录用', 3: '不合适', 4: '已撤回' }
  return map[s] ?? '未知'
}
function statusType(s) {
  const map = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'danger', 4: 'info' }
  return map[s] ?? ''
}

async function loadDetail() {
  try {
    const res = await getCandidateDetail(route.params.id)
    if (res && res.data) {
      Object.assign(candidate, res.data)
      resumeData.value = res.data.resume || null
    }
  } catch (e) {
    ElMessage.error('加载候选人详情失败')
  }
}

async function handleStatusChange(newStatus) {
  const label = newStatus === 1 ? '初筛通过' : '标记为不合适'
  try {
    await ElMessageBox.confirm(`确定要${label}该候选人吗？`, '确认操作', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: newStatus === 1 ? 'success' : 'warning'
    })
    statusLoading.value = true
    await updateApplicationStatus(route.params.id, { status: newStatus })
    ElMessage.success(`${label}成功`)
    candidate.status = newStatus
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '操作失败')
  } finally {
    statusLoading.value = false
  }
}

onMounted(loadDetail)
</script>

<style scoped>
.section-card { border-radius: 12px; margin-bottom: 16px; }
.card-title { margin: 0 0 14px; font-size: 16px; color: #3E2723; padding-bottom: 10px; border-bottom: 1px solid #f0f0f0; }
.info-list { display: flex; flex-direction: column; gap: 10px; }
.info-item { font-size: 14px; color: #666; display: flex; align-items: center; gap: 6px; }
.resume-content { font-size: 14px; }
.resume-section { margin-bottom: 20px; }
.resume-section h4 { margin: 0 0 10px; font-size: 15px; color: #3E2723; }
.resume-block { padding: 8px 0; border-bottom: 1px dashed #f0f0f0; }
.resume-block p { margin: 4px 0; color: #555; }
.skill-tags { display: flex; flex-wrap: wrap; gap: 8px; }
.eval-text { color: #666; line-height: 1.8; white-space: pre-wrap; }
</style>
