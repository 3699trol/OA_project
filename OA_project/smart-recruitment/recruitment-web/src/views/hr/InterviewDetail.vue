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
            <el-descriptions-item label="面试状态" :span="1">
              <el-tag :type="detail.status === 0 ? 'warning' : detail.status === 1 ? 'success' : 'danger'">{{ detail.statusLabel }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="投递状态" :span="1">
              <el-tag :type="appStatusTagType">{{ appStatusLabel }}</el-tag>
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
        <!-- 面试官评价摘要 -->
        <el-card v-if="detail.evaluation" shadow="never" class="section-card">
          <h3 class="card-title">👨‍💼 面试官评价</h3>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="技术评分">{{ detail.evaluation.technicalScore }}分</el-descriptions-item>
            <el-descriptions-item label="沟通评分">{{ detail.evaluation.communicationScore }}分</el-descriptions-item>
            <el-descriptions-item label="逻辑评分">{{ detail.evaluation.logicScore }}分</el-descriptions-item>
            <el-descriptions-item label="综合评分">{{ detail.evaluation.overallScore }}分</el-descriptions-item>
            <el-descriptions-item label="面试官结论">
              <el-tag :type="detail.evaluation.result === 2 ? 'success' : detail.evaluation.result === 1 ? 'warning' : 'danger'" size="large">
                {{ detail.evaluation.resultLabel }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- HR 操作区 -->
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">⚡ HR 操作</h3>

          <!-- 状态 0：待面试 → 可取消 -->
          <div v-if="detail.status === 0" class="action-buttons">
            <el-alert title="等待面试官完成评价" type="warning" :closable="false" show-icon style="margin-bottom:12px;" />
            <el-button type="danger" @click="handleCancel">取消面试</el-button>
            <el-button type="primary" @click="$router.push(`/hr/candidates/detail/${detail.applicationId}`)">查看候选人简历</el-button>
          </div>

          <!-- 状态 1：已完成 + 有评价 + HR 尚未决定 → 做最终决定 -->
          <div v-else-if="detail.status === 1 && detail.evaluation && detail.applicationStatus === 1" class="action-buttons">
            <el-alert
              :title="'面试官建议：' + detail.evaluation.resultLabel + ' — 请确认最终结果'"
              :type="detail.evaluation.result === 2 ? 'success' : detail.evaluation.result === 1 ? 'warning' : 'error'"
              :closable="false"
              show-icon
              style="margin-bottom:12px;"
            />
            <el-button
              type="success"
              @click="handleHire(1)"
              :plain="detail.evaluation.result !== 2"
              size="large"
            >
              ✅ 录用
              <span v-if="detail.evaluation.result === 2" style="font-size:11px;opacity:.8;">（面试官推荐）</span>
            </el-button>
            <el-button
              type="danger"
              @click="handleHire(0)"
              :plain="detail.evaluation.result !== 0"
              size="large"
            >
              ❌ 淘汰
              <span v-if="detail.evaluation.result === 0" style="font-size:11px;opacity:.8;">（面试官推荐）</span>
            </el-button>
          </div>

          <!-- 状态 1：已完成 + HR 已处理 → 展示结果 -->
          <div v-else-if="detail.status === 1 && detail.applicationStatus >= 2">
            <el-alert
              :title="'最终结果：' + appStatusLabel"
              :type="detail.applicationStatus === 2 ? 'success' : 'error'"
              :closable="false"
              show-icon
            />
          </div>

          <!-- 状态 1：已完成但没评价（异常状态） -->
          <div v-else-if="detail.status === 1 && !detail.evaluation" class="action-buttons">
            <el-alert title="面试已完成，但评价数据异常，请刷新页面重试" type="error" :closable="false" show-icon />
          </div>

          <!-- 状态 2：已取消 -->
          <div v-else-if="detail.status === 2">
            <el-alert title="该面试已取消" type="info" :closable="false" show-icon />
          </div>

        </el-card>
      </el-col>
    </el-row>

    <!-- 邮件编辑对话框 -->
    <el-dialog
      v-model="emailDialogVisible"
      :title="pendingDecision === 1 ? '发送录用通知邮件' : '发送面试结果通知邮件'"
      width="650px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-alert
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom:16px;"
      >
        收件人：<strong>{{ detail.candidateName }}</strong> &lt;<el-tag size="small" type="info">{{ detail.candidateEmail || '未填写邮箱' }}</el-tag>&gt;
      </el-alert>
      <el-form label-position="top">
        <el-form-item label="邮件主题">
          <el-input v-model="emailSubject" placeholder="请输入邮件主题" />
        </el-form-item>
        <el-form-item label="邮件正文">
          <el-input v-model="emailBody" type="textarea" :rows="12" placeholder="请输入邮件正文" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="emailDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="emailSending" @click="confirmSend">
          <el-icon style="margin-right:4px;"><Promotion /></el-icon> 发送邮件并{{ pendingDecision === 1 ? '录用' : '淘汰' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Promotion } from '@element-plus/icons-vue'
import { getInterviewDetail, cancelInterview, processInterviewResult } from '@/api/interview'
import { formatDateTime } from '@/utils/date'

const route = useRoute()
const router = useRouter()

// 邮件编辑相关状态
const emailDialogVisible = ref(false)
const emailSending = ref(false)
const pendingDecision = ref(null)  // 1-录用 0-淘汰
const emailSubject = ref('')
const emailBody = ref('')

// 投递状态映射
const APP_STATUS_MAP = { 0: '待筛选', 1: '面试中', 2: '已录用', 3: '不合适', 4: '已撤回' }
const appStatusLabel = computed(() => APP_STATUS_MAP[detail.applicationStatus] || '未知')
const appStatusTagType = computed(() => {
  const s = detail.applicationStatus
  if (s === 2) return 'success'
  if (s === 1) return 'warning'
  if (s === 3 || s === 4) return 'danger'
  return 'info'
})

const detail = reactive({
  id: route.params.id,
  candidateName: '',
  candidateEmail: '',
  jobName: '',
  interviewTime: '',
  interviewType: '',
  address: '',
  interviewerName: '',
  applicationId: null,
  applicationStatus: null,
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

/** 点击录用/淘汰：先弹确认框，再弹邮件编辑框 */
async function handleHire(decision) {
  const actionText = decision === 1 ? '录用' : '淘汰'
  try {
    await ElMessageBox.confirm(`确定要${actionText}该候选人吗？`, '确认操作', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch (e) {
    return  // 用户取消
  }

  // 预填邮件内容
  pendingDecision.value = decision
  if (decision === 1) {
    emailSubject.value = `【录用通知】恭喜您通过 ${detail.jobName} 岗位面试`
    emailBody.value = [
      `尊敬的 ${detail.candidateName}：`,
      '',
      `恭喜您！经过综合评估，您已成功通过「${detail.jobName}」岗位的面试考核，我们诚挚地欢迎您加入团队。`,
      '',
      `录用岗位：${detail.jobName}`,
      `面试时间：${formatDateTime(detail.interviewTime)}`,
      '',
      '后续入职流程及相关材料，HR 将另行与您联系。如有任何疑问，请回复本邮件或联系 HR。',
      '',
      '祝您工作愉快！'
    ].join('\n')
  } else {
    emailSubject.value = `【面试结果】${detail.jobName} 岗位面试反馈`
    emailBody.value = [
      `尊敬的 ${detail.candidateName}：`,
      '',
      `感谢您对「${detail.jobName}」岗位的关注与参与面试。`,
      '',
      '经过面试官综合评估，很遗憾本次未能与您继续推进。我们会将您的简历保留在人才库中，如有更合适的岗位机会将优先与您联系。',
      '',
      '您可以继续浏览其他岗位进行投递。再次感谢您的宝贵时间，祝您求职顺利！'
    ].join('\n')
  }
  emailDialogVisible.value = true
}

/** 邮件编辑框点击发送：调用 API 并关闭对话框 */
async function confirmSend() {
  emailSending.value = true
  try {
    await processInterviewResult(
      route.params.id,
      pendingDecision.value,
      emailSubject.value,
      emailBody.value
    )
    const actionText = pendingDecision.value === 1 ? '录用' : '淘汰'
    ElMessage.success(`已${actionText}该候选人，邮件已发送`)
    emailDialogVisible.value = false
    await loadDetail()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    emailSending.value = false
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
