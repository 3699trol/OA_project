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

        <!-- 候选人简历详情 -->
        <el-card v-if="resumeData" shadow="never" class="section-card">
          <h3 class="card-title">🗂️ 候选人简历详情</h3>
          <div v-if="resumeData.basicInfo" class="resume-section">
            <h4>基本信息</h4>
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item v-for="(val, key) in resumeData.basicInfo" :key="key" :label="key">{{ val }}</el-descriptions-item>
            </el-descriptions>
          </div>
          <div v-if="resumeData.educations?.length" class="resume-section">
            <h4>教育经历</h4>
            <div v-for="(edu, idx) in resumeData.educations" :key="idx" class="resume-block">
              <p><strong>{{ edu.school }}</strong> - {{ edu.major }} ({{ edu.degree }}) <span v-if="edu.time">{{ edu.time }}</span></p>
            </div>
          </div>
          <div v-if="resumeData.experiences?.length" class="resume-section">
            <h4>工作经历</h4>
            <div v-for="(exp, idx) in resumeData.experiences" :key="idx" class="resume-block">
              <p><strong>{{ exp.company }}</strong> - {{ exp.role }} <span v-if="exp.time">({{ exp.time }})</span></p>
              <ul v-if="exp.details?.length" class="exp-details">
                <li v-for="(det, di) in exp.details" :key="di">{{ det }}</li>
              </ul>
              <p v-else-if="exp.description" class="exp-desc">{{ exp.description }}</p>
            </div>
          </div>
          <div v-if="resumeData.skills?.length" class="resume-section">
            <h4>技能标签</h4>
            <div class="skill-tags">
              <el-tag v-for="s in resumeData.skills" :key="s" size="default" effect="plain">{{ s }}</el-tag>
            </div>
          </div>
          <div v-if="detail.selfEvaluation" class="resume-section">
            <h4>自我评价</h4>
            <p class="eval-text">{{ detail.selfEvaluation }}</p>
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

        <!-- 候选人简历 -->
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">📄 候选人简历</h3>
          <div v-if="resumeFile.fileName" class="file-item">
            <el-icon class="file-icon"><Document /></el-icon>
            <span class="file-name" :title="resumeFile.fileName">{{ resumeFile.fileName }}</span>
            <div class="file-actions">
              <el-button type="primary" link icon="View" size="small" @click="handlePreviewFile">预览</el-button>
              <el-button type="primary" link icon="Download" size="small" @click="handleDownloadFile">下载</el-button>
            </div>
          </div>
          <div v-else class="empty-subtext">该候选人暂未上传简历文件</div>
          <div v-if="detail.candidatePhone || detail.candidateEmail" class="contact-list">
            <div v-if="detail.candidatePhone" class="contact-item"><el-icon><Phone /></el-icon> {{ detail.candidatePhone }}</div>
            <div v-if="detail.candidateEmail" class="contact-item"><el-icon><Message /></el-icon> {{ detail.candidateEmail }}</div>
          </div>
        </el-card>

        <el-card shadow="never" class="section-card">
          <h3 class="card-title">操作</h3>
          <div class="action-buttons">
            <template v-if="detail.status === 0">
              <el-button type="primary" @click="$router.push(`/interviewer/questions/generate?interviewId=${route.params.id}`)">
                🤖 AI 生成面试题
              </el-button>
              <el-button type="success" @click="$router.push(`/interviewer/evaluation/${route.params.id}`)">
                ✍️ 填写面试评价
              </el-button>
            </template>
            <el-tag v-else type="info" class="finished-tag">面试已结束</el-tag>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown, Document, Phone, Message } from '@element-plus/icons-vue'
import { getInterviewDetail } from '@/api/interview'
import { downloadFile } from '@/api/file'
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
  questions: [],
  candidatePhone: '',
  candidateEmail: '',
  selfEvaluation: ''
})
const resumeData = ref(null)
const resumeFile = reactive({ fileId: null, fileName: '', fileUrl: '' })

async function loadDetail() {
  try {
    const res = await getInterviewDetail(route.params.id)
    if (res && res.data) {
      const questions = (res.data.questions || []).map(q => ({ ...q, _expanded: false }))
      Object.assign(detail, res.data, { questions })
      resumeData.value = res.data.resume || null
      // 解析简历文件信息：后端返回的 resumeFileUrl 形如 /api/file/download/{id}
      const fileUrl = res.data.resumeFileUrl || ''
      const match = fileUrl.match(/\/download\/(\d+)/)
      if (match) {
        resumeFile.fileId = Number(match[1])
        resumeFile.fileUrl = fileUrl
        const ext = fileUrl.split('.').pop()?.split('?')[0]
        resumeFile.fileName = `${detail.candidateName || '候选人'}的简历${ext && ext !== fileUrl ? '.' + ext : ''}`
      }
    }
  } catch (e) {
    ElMessage.error('加载面试详情失败')
  }
}

async function handleDownloadFile() {
  if (!resumeFile.fileId) {
    ElMessage.warning('暂无可下载的简历文件')
    return
  }
  try {
    const res = await downloadFile(resumeFile.fileId)
    const blob = new Blob([res], { type: 'application/octet-stream' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = resumeFile.fileName || 'resume.pdf'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch (e) {
    ElMessage.error('简历下载失败')
  }
}

async function handlePreviewFile() {
  if (!resumeFile.fileId) {
    ElMessage.warning('暂无可预览的简历文件')
    return
  }
  try {
    const res = await downloadFile(resumeFile.fileId)
    const blob = new Blob([res], { type: 'application/pdf' })
    const url = window.URL.createObjectURL(blob)
    window.open(url, '_blank')
    setTimeout(() => window.URL.revokeObjectURL(url), 60000)
  } catch (e) {
    ElMessage.error('简历预览失败，请尝试下载')
  }
}

onMounted(loadDetail)
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

.file-item { display: flex; align-items: center; gap: 10px; padding: 10px 12px; background: #f8fafc; border-radius: 8px; border: 1px solid #edf2f7; }
.file-icon { font-size: 22px; color: #3182ce; flex-shrink: 0; }
.file-name { flex: 1; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: #2d3748; font-size: 13px; }
.file-actions { display: flex; gap: 6px; flex-shrink: 0; }
.empty-subtext { font-size: 13px; color: #a0aec0; text-align: center; padding: 12px 0; }
.contact-list { margin-top: 12px; display: flex; flex-direction: column; gap: 6px; }
.contact-item { font-size: 13px; color: #666; display: flex; align-items: center; gap: 6px; }

.resume-section { margin-bottom: 18px; }
.resume-section h4 { margin: 0 0 10px; font-size: 15px; color: #3E2723; }
.resume-block { padding: 8px 0; border-bottom: 1px dashed #f0f0f0; }
.resume-block p { margin: 4px 0; color: #555; }
.skill-tags { display: flex; flex-wrap: wrap; gap: 8px; }
.eval-text { color: #666; line-height: 1.8; white-space: pre-wrap; }
.exp-details { margin: 6px 0 0; padding-left: 20px; color: #555; line-height: 1.7; }
.exp-details li { margin-bottom: 2px; }
.exp-desc { color: #555; line-height: 1.7; }

.action-buttons { display: flex; flex-direction: column; gap: 10px; }
.action-buttons .el-button { width: 100%; margin: 0; }
.finished-tag { width: 100%; text-align: center; }
</style>
