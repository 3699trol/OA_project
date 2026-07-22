<template>
  <div class="page">
    <div class="page-header">
      <h2>📄 我的智能简历</h2>
      <div class="header-actions">
        <el-button type="success" icon="MagicStick" :loading="aiParsing" @click="handleAiParse">AI智能解析</el-button>
        <el-button type="primary" icon="Upload" @click="uploadVisible = true">📤 上传简历文件</el-button>
        <el-button type="danger" icon="Delete" plain @click="handleDeleteResume">删除简历</el-button>
      </div>
    </div>
    
    <!-- 基本信息 -->
    <el-card shadow="never" class="section-card">
      <h3 class="card-title">基本信息</h3>
      <el-form :model="form" label-width="80px">
        <el-row :gutter="24">
          <el-col :span="8"><el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="性别"><el-radio-group v-model="form.gender"><el-radio value="男">男</el-radio><el-radio value="女">女</el-radio></el-radio-group></el-form-item></el-col>
          <el-col :span="8">
            <el-form-item label="出生日期">
              <el-date-picker 
                v-model="form.birth" 
                type="date" 
                value-format="YYYY-MM-DD" 
                placeholder="选择出生日期" 
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="24">
          <el-col :span="8"><el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="现居城市"><el-input v-model="form.city" /></el-form-item></el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 简历文件 -->
    <el-card shadow="never" class="section-card">
      <div class="card-title-row">
        <h3 class="card-title">简历文件</h3>
      </div>
      <div v-if="form.fileName" class="file-item">
        <el-icon class="file-icon"><Document /></el-icon>
        <span class="file-name" :title="form.fileName">{{ form.fileName }}</span>
        <div class="file-actions">
          <el-button type="primary" link icon="Download" size="small" @click="handleDownloadFile">下载</el-button>
          <el-button type="danger" link icon="Delete" size="small" @click="handleRemoveFile">移除</el-button>
        </div>
      </div>
      <div v-else class="empty-subtext">暂未上传简历文件，点击右上角「上传简历文件」可上传 PDF/DOC/DOCX</div>
    </el-card>

    <!-- 教育背景 -->
    <el-card shadow="never" class="section-card">
      <div class="card-title-row">
        <h3 class="card-title">教育背景</h3>
        <el-button type="primary" link icon="Plus" @click="openAddEdu">添加教育</el-button>
      </div>
      <div v-for="(edu, i) in form.educations" :key="i" class="list-item flex-item-row">
        <div class="list-item-body">
          <div class="item-header">
            <span class="item-title">{{ edu.school }}</span>
            <span class="item-sub">{{ edu.degree }}</span>
            <span class="item-time">{{ edu.time }}</span>
          </div>
          <p class="item-detail" v-if="edu.major">{{ edu.major }}</p>
        </div>
        <div class="list-item-actions">
          <el-button type="primary" link icon="Edit" size="small" @click="openEditEdu(i)">编辑</el-button>
          <el-button type="danger" link icon="Delete" size="small" @click="removeEdu(i)">删除</el-button>
        </div>
      </div>
      <div v-if="form.educations.length === 0" class="empty-subtext">暂无教育背景，请点击右上角添加</div>
    </el-card>

    <!-- 工作经历 -->
    <el-card shadow="never" class="section-card">
      <div class="card-title-row">
        <h3 class="card-title">工作经历</h3>
        <el-button type="primary" link icon="Plus" @click="openAddExp">添加工作</el-button>
      </div>
      <div v-for="(exp, i) in form.experiences" :key="i" class="list-item flex-item-row-top">
        <div class="list-item-body">
          <div class="item-header">
            <span class="item-title">{{ exp.company }}</span>
            <span class="item-sub">{{ exp.role }}</span>
            <span class="item-time">{{ exp.time }}</span>
          </div>
          <div class="item-details" v-if="exp.details && exp.details.length > 0">
            <ul class="exp-bullet-list">
              <li v-for="(det, di) in exp.details" :key="di">{{ det }}</li>
            </ul>
          </div>
        </div>
        <div class="list-item-actions">
          <el-button type="primary" link icon="Edit" size="small" @click="openEditExp(i)">编辑</el-button>
          <el-button type="danger" link icon="Delete" size="small" @click="removeExp(i)">删除</el-button>
        </div>
      </div>
      <div v-if="form.experiences.length === 0" class="empty-subtext">暂无工作经历，请点击右上角添加</div>
    </el-card>

    <!-- 专业技能 -->
    <el-card shadow="never" class="section-card">
      <h3 class="card-title">专业技能</h3>
      <div class="skill-input">
        <el-tag v-for="s in form.skills" :key="s" closable @close="removeSkill(s)" class="skill-tag">{{ s }}</el-tag>
        <el-input v-if="tagVisible" ref="tagInput" v-model="tagValue" size="small" style="width:120px;" @keyup.enter="addSkill" @blur="addSkill" />
        <el-button v-else size="small" @click="showTagInput">+ 添加技能</el-button>
      </div>
    </el-card>

    <!-- 自我评价 -->
    <el-card shadow="never" class="section-card">
      <h3 class="card-title">自我评价</h3>
      <el-input v-model="form.summary" type="textarea" :rows="4" placeholder="简要描述您的优势、职业规划..." />
    </el-card>

    <div class="form-actions">
      <el-button type="primary" size="large" icon="Check" @click="handleSave">保存简历</el-button>
    </div>

    <!-- AI 解析进度 -->
    <el-dialog
      :model-value="aiParsing"
      title="AI 正在解析简历"
      width="min(440px, 92vw)"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
      align-center
    >
      <div class="ai-waiting">
        <el-progress :percentage="50" :indeterminate="true" :duration="2" :show-text="false" />
        <p class="ai-waiting-status">{{ aiWaitingStatus }}</p>
        <div class="ai-waiting-metrics">
          <span>已用时 {{ formatAiDuration(aiElapsedMs) }}</span>
        </div>
      </div>
    </el-dialog>

    <!-- AI 解析结果预览 -->
    <el-dialog
      v-model="aiResultVisible"
      width="min(880px, 94vw)"
      top="5vh"
      class="ai-result-dialog"
      destroy-on-close
    >
      <template #header>
        <div class="ai-result-header">
          <div class="ai-result-title">
            <el-icon><DocumentChecked /></el-icon>
            <strong>AI 简历解析结果</strong>
          </div>
          <span v-if="lastAiDurationMs" class="ai-result-duration">
            <el-icon><Clock /></el-icon>
            {{ formatAiDuration(lastAiDurationMs) }}
          </span>
        </div>
      </template>

      <div v-if="aiResult" class="ai-result">
        <!-- 字段选择区 -->
        <div class="ai-field-selection">
          <span class="ai-field-selection-label">选择要应用的字段：</span>
          <el-checkbox v-model="aiFieldSelection.basic">基本信息</el-checkbox>
          <el-checkbox v-model="aiFieldSelection.education">教育背景</el-checkbox>
          <el-checkbox v-model="aiFieldSelection.experience">工作经历</el-checkbox>
          <el-checkbox v-model="aiFieldSelection.skills">专业技能</el-checkbox>
          <el-checkbox v-model="aiFieldSelection.summary">个人摘要</el-checkbox>
        </div>

        <section class="ai-result-section ai-profile-section">
          <div class="ai-section-heading">
            <el-icon><User /></el-icon>
            <h4>识别概览</h4>
          </div>
          <dl class="ai-profile-grid">
            <div><dt>姓名</dt><dd>{{ displayAiValue(aiResult.name) }}</dd></div>
            <div><dt>工作年限</dt><dd>{{ aiResult.workYears == null ? '未识别' : `${aiResult.workYears} 年` }}</dd></div>
            <div><dt>手机号</dt><dd>{{ displayAiValue(aiResult.phone) }}</dd></div>
            <div><dt>邮箱</dt><dd>{{ displayAiValue(aiResult.email) }}</dd></div>
            <div><dt>最高学历</dt><dd>{{ displayAiValue(aiResult.education) }}</dd></div>
            <div><dt>学校与专业</dt><dd>{{ [aiResult.school, aiResult.major].filter(Boolean).join(' · ') || '未识别' }}</dd></div>
          </dl>
        </section>

        <section class="ai-result-section ai-diagnosis">
          <div class="ai-section-heading">
            <el-icon><DataAnalysis /></el-icon>
            <h4>简历诊断</h4>
          </div>
          <div class="ai-score-overview" :class="{ 'without-score': aiResult.overallScore == null }">
            <div v-if="aiResult.overallScore != null" class="ai-score-block">
              <strong class="ai-score" :style="{ color: scoreColor }">{{ aiScore }}</strong>
              <span>综合评分 / 100</span>
              <el-progress :percentage="aiScore" :stroke-width="8" :color="scoreColor" :show-text="false" />
            </div>
            <p v-if="aiResult.evaluation" class="ai-evaluation">{{ aiResult.evaluation }}</p>
            <span v-else class="ai-empty">暂无综合评价</span>
          </div>

          <div class="ai-diagnosis-grid">
            <div class="ai-diagnosis-column is-strength">
              <h5><el-icon><CircleCheck /></el-icon>简历优势</h5>
              <ul v-if="aiStrengths.length">
                <li v-for="item in aiStrengths" :key="item">{{ item }}</li>
              </ul>
              <span v-else class="ai-empty">暂无</span>
            </div>
            <div class="ai-diagnosis-column is-issue">
              <h5><el-icon><Warning /></el-icon>存在问题</h5>
              <ul v-if="aiIssues.length">
                <li v-for="item in aiIssues" :key="item">{{ item }}</li>
              </ul>
              <span v-else class="ai-empty">暂无</span>
            </div>
            <div class="ai-diagnosis-column is-suggestion">
              <h5><el-icon><Aim /></el-icon>修改建议</h5>
              <ul v-if="aiSuggestions.length">
                <li v-for="item in aiSuggestions" :key="item">{{ item }}</li>
              </ul>
              <span v-else class="ai-empty">暂无</span>
            </div>
          </div>
        </section>

        <section class="ai-result-section">
          <div class="ai-section-heading">
            <el-icon><CollectionTag /></el-icon>
            <h4>专业技能</h4>
          </div>
          <div v-if="normalizedAiSkills.length" class="ai-skill-list">
            <el-tag v-for="skill in normalizedAiSkills" :key="skill" effect="plain">{{ skill }}</el-tag>
          </div>
          <span v-else class="ai-empty">未识别</span>
        </section>

        <section class="ai-result-section">
          <div class="ai-section-heading">
            <el-icon><EditPen /></el-icon>
            <h4>个人摘要</h4>
          </div>
          <div class="ai-summary-grid" :class="{ 'is-single': !aiResult.optimizedSummary }">
            <div class="ai-summary-panel">
              <h5>原始摘要</h5>
              <p v-if="aiResult.summary" class="ai-summary">{{ aiResult.summary }}</p>
              <span v-else class="ai-empty">未识别</span>
            </div>
            <div v-if="aiResult.optimizedSummary" class="ai-summary-panel is-optimized">
              <h5>AI 优化摘要</h5>
              <p class="ai-summary">{{ aiResult.optimizedSummary }}</p>
            </div>
          </div>
        </section>

        <section class="ai-result-section">
          <div class="ai-section-heading">
            <el-icon><School /></el-icon>
            <h4>教育经历</h4>
          </div>
          <div v-if="displayedAiEducations.length" class="ai-timeline-list">
            <div v-for="(education, index) in displayedAiEducations" :key="index" class="ai-timeline-item">
              <div class="ai-timeline-heading">
                <strong>{{ education.school || '未识别学校' }}</strong>
                <span>{{ [education.major, education.degree].filter(Boolean).join(' · ') || '专业学历未识别' }}</span>
                <time>{{ education.time || '时间未识别' }}</time>
              </div>
            </div>
          </div>
          <span v-else class="ai-empty">未识别</span>
        </section>

        <section class="ai-result-section">
          <div class="ai-section-heading">
            <el-icon><Briefcase /></el-icon>
            <h4>工作经历</h4>
          </div>
          <div v-if="normalizedAiExperiences.length" class="ai-timeline-list">
            <div v-for="(experience, index) in normalizedAiExperiences" :key="index" class="ai-timeline-item">
              <div class="ai-timeline-heading">
                <strong>{{ experience.company || '未识别公司' }}</strong>
                <span>{{ experience.role || '未识别职位' }}</span>
                <time>{{ experience.time || '时间未识别' }}</time>
              </div>
              <ul v-if="experience.details.length" class="ai-timeline-details">
                <li v-for="(detail, detailIndex) in experience.details" :key="detailIndex">{{ detail }}</li>
              </ul>
            </div>
          </div>
          <span v-else class="ai-empty">未识别</span>
        </section>
      </div>
      <template #footer>
        <el-button @click="aiResultVisible = false">暂不应用</el-button>
        <el-button type="primary" icon="Check" @click="applyAiResult">应用勾选字段</el-button>
      </template>
    </el-dialog>

    <!-- 上传简历文件弹窗 -->
    <el-dialog v-model="uploadVisible" title="上传简历文件" width="480px">
      <el-upload
        ref="resumeUploadRef"
        drag
        :auto-upload="false"
        :limit="1"
        accept=".pdf,.doc,.docx"
        :on-change="handleResumeFileChange"
        :on-remove="handleResumeFileRemove"
        :on-exceed="handleResumeFileExceed"
      >
        <el-icon class="el-icon--upload" size="48"><UploadFilled /></el-icon>
        <div class="el-upload__text">拖拽文件或<em>点击上传</em></div>
        <template #tip><div class="el-upload__tip">支持 PDF、DOC、DOCX，大小不超过 10MB</div></template>
      </el-upload>
      <template #footer>
        <el-button @click="uploadVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpload">上传</el-button>
      </template>
    </el-dialog>

    <!-- 添加/编辑教育经历弹窗 -->
    <el-dialog v-model="eduDialogVisible" :title="editingEduIndex > -1 ? '编辑教育经历' : '添加教育经历'" width="520px">
      <el-form :model="eduForm" label-position="top">
        <el-form-item label="学校名称" required>
          <el-input v-model="eduForm.school" placeholder="请输入学校名称，如：华中科技大学" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="专业名称">
              <el-input v-model="eduForm.major" placeholder="如：计算机科学与技术" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学历学位">
              <el-select v-model="eduForm.degree" placeholder="选择学位" style="width: 100%;">
                <el-option label="专科" value="专科" />
                <el-option label="本科" value="本科" />
                <el-option label="硕士" value="硕士" />
                <el-option label="博士" value="博士" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="在校起止时间" required>
          <div class="time-select-row">
            <el-select v-model="eduForm.startYear" placeholder="开始年份" style="flex: 1;">
              <el-option v-for="y in yearOptions" :key="y" :label="y + ' 年'" :value="String(y)" />
            </el-select>
            <el-select v-model="eduForm.startMonth" placeholder="月份" style="width: 90px;">
              <el-option v-for="m in monthOptions" :key="m" :label="m + ' 月'" :value="m" />
            </el-select>
            <span class="time-sep">至</span>
            <el-select v-model="eduForm.endYear" placeholder="结束年份" style="flex: 1;">
              <el-option v-for="y in yearOptions" :key="y" :label="y + ' 年'" :value="String(y)" />
            </el-select>
            <el-select v-model="eduForm.endMonth" placeholder="月份" style="width: 90px;">
              <el-option v-for="m in monthOptions" :key="m" :label="m + ' 月'" :value="m" />
            </el-select>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="eduDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmEdu">确认保存</el-button>
      </template>
    </el-dialog>

    <!-- 添加/编辑工作经历弹窗 -->
    <el-dialog v-model="expDialogVisible" :title="editingExpIndex > -1 ? '编辑工作经历' : '添加工作经历'" width="560px">
      <el-form :model="expForm" label-position="top">
        <el-form-item label="公司名称" required>
          <el-input v-model="expForm.company" placeholder="请输入公司名称，如：腾讯科技" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="职位角色">
              <el-input v-model="expForm.role" placeholder="如：高级Java开发工程师" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="在职起止时间" required>
          <div class="time-select-row wrap-mobile">
            <div class="time-part-item">
              <el-select v-model="expForm.startYear" placeholder="开始年份" style="flex: 1;">
                <el-option v-for="y in yearOptions" :key="y" :label="y + ' 年'" :value="String(y)" />
              </el-select>
              <el-select v-model="expForm.startMonth" placeholder="月份" style="width: 90px;">
                <el-option v-for="m in monthOptions" :key="m" :label="m + ' 月'" :value="m" />
              </el-select>
            </div>
            
            <span class="time-sep">至</span>
            
            <div class="time-part-item">
              <el-select v-model="expForm.endYear" placeholder="结束年份" :disabled="expForm.isCurrent" style="flex: 1;">
                <el-option v-for="y in yearOptions" :key="y" :label="y + ' 年'" :value="String(y)" />
              </el-select>
              <el-select v-model="expForm.endMonth" placeholder="月份" :disabled="expForm.isCurrent" style="width: 90px;">
                <el-option v-for="m in monthOptions" :key="m" :label="m + ' 月'" :value="m" />
              </el-select>
            </div>
            
            <el-checkbox v-model="expForm.isCurrent" label="至今" style="margin-left: 8px;" />
          </div>
        </el-form-item>
        <el-form-item label="工作详情与项目描述 (可输入多行详细文本描述)">
          <el-input
            v-model="expForm.description"
            type="textarea"
            :rows="6"
            placeholder="请输入您在职期间的主要工作、职责产出或项目业绩。每行可作为一条单独的项目细节展示..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="expDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmExp">确认保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, nextTick, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyResume, saveMyResume, deleteMyResume } from '@/api/resume'
import { getCurrentUser } from '@/api/auth'
import { aiParseResume, aiParseResumeFile } from '@/api/ai'
import { uploadFile, getFileUrl, downloadFile, getFileInfo, deleteFile } from '@/api/file'

const uploadVisible = ref(false)
const resumeUploadRef = ref()
const selectedResumeFile = ref(null)
const aiResultVisible = ref(false)
const aiParsing = ref(false)
const aiResult = ref(null)
const aiElapsedMs = ref(0)
const lastAiDurationMs = ref(0)
let aiStartedAt = 0
let aiTimer = null
let lastAiRequestContent = ''
const tagVisible = ref(false)
const tagValue = ref('')
const tagInput = ref()

// AI 解析结果字段勾选：用户可选择只应用部分字段
const aiFieldSelection = reactive({
  basic: true,       // 姓名、手机、邮箱
  education: true,   // 教育背景
  experience: true,  // 工作经历
  skills: true,      // 专业技能
  summary: true      // 个人摘要
})

const eduDialogVisible = ref(false)
const expDialogVisible = ref(false)
const editingEduIndex = ref(-1)
const editingExpIndex = ref(-1)

// 下拉框可选项选项数据
const currentYear = new Date().getFullYear()
const yearOptions = Array.from({ length: 36 }, (_, i) => currentYear - 25 + i) // 2001 - 2036 年
const monthOptions = Array.from({ length: 12 }, (_, i) => String(i + 1).padStart(2, '0')) // 01 - 12 月

const eduForm = reactive({ school: '', major: '', degree: '本科', startYear: '', startMonth: '', endYear: '', endMonth: '' })
const expForm = reactive({ company: '', role: '', startYear: '', startMonth: '', endYear: '', endMonth: '', isCurrent: false, description: '' })

const form = reactive({
  name: '', gender: '男', birth: '', phone: '', email: '', city: '',
  educations: [], experiences: [], skills: [], summary: '',
  fileId: null, fileName: '', fileUrl: ''
})

onMounted(async () => {
  const [resumeResult, userResult] = await Promise.allSettled([
    getMyResume(),
    getCurrentUser()
  ])

  if (resumeResult.status === 'rejected') {
    console.error('加载个人简历失败:', resumeResult.reason)
    return
  }

  const resumeData = resumeResult.value?.data || {}
  const userData = userResult.status === 'fulfilled' ? userResult.value?.data || {} : {}
  const hasResumeField = field => Object.prototype.hasOwnProperty.call(resumeData, field)
  const data = {
    ...resumeData,
    name: hasResumeField('name') ? resumeData.name : userData.realName ?? '',
    phone: hasResumeField('phone') ? resumeData.phone : userData.phone ?? '',
    email: hasResumeField('email') ? resumeData.email : userData.email ?? ''
  }

  if (userResult.status === 'rejected') {
    console.error('加载个人资料失败:', userResult.reason)
  }

  form.name = data.name ?? ''
  form.gender = data.gender ?? '男'
  form.birth = data.birth ?? ''
  form.phone = data.phone ?? ''
  form.email = data.email ?? ''
  form.city = data.city ?? ''
  form.summary = data.summary ?? data.evaluation ?? ''
  form.fileId = data.fileId ?? null
  form.fileName = data.fileName ?? ''
  form.fileUrl = data.fileUrl ?? ''

  // 如果 fileUrl 存在但 fileId/fileName 缺失，从 fileUrl 反查文件信息
  if (form.fileUrl && (!form.fileId || !form.fileName)) {
    const match = form.fileUrl.match(/\/download\/(\d+)/)
    if (match) {
      form.fileId = Number(match[1])
      try {
        const info = await getFileInfo(form.fileId)
        if (info?.data) {
          form.fileName = info.data.originalName || ''
        }
      } catch (e) {
        console.error('获取文件信息失败:', e)
      }
    }
  }

  if (Array.isArray(data.skills)) {
    form.skills = [...data.skills]
  } else if (typeof data.skills === 'string') {
    form.skills = data.skills.split(',').map(s => s.trim()).filter(Boolean)
  } else {
    form.skills = []
  }

  if (Array.isArray(data.educations) && data.educations.length > 0) {
    form.educations = [...data.educations]
  } else if (data.education) {
    const parts = data.education.split('·').map(s => s.trim())
    const school = parts[0] || ''
    const major = parts[1] || ''
    const degreeParts = parts[2] ? parts[2].split(' ') : []
    const degree = degreeParts[0] || ''
    const time = degreeParts[1] ? degreeParts[1].replace(/[()]/g, '') : ''
    form.educations = [{ school, major, degree, time }]
  } else {
    form.educations = []
  }

  if (Array.isArray(data.experiences) && data.experiences.length > 0) {
    form.experiences = [...data.experiences]
  } else if (data.experience) {
    const lines = data.experience.split('\n')
    const firstLineParts = lines[0].split('|').map(s => s.trim())
    const time = firstLineParts[0] || ''
    const company = firstLineParts[1] || ''
    const role = firstLineParts[2] || ''
    const details = lines.slice(1).map(l => l.trim().replace(/^\d+\.\s*/, '')).filter(Boolean)
    form.experiences = [{ company, role, time, details }]
  } else {
    form.experiences = []
  }
})

const normalizedAiSkills = computed(() => normalizeSkills(aiResult.value?.skills))
const normalizedAiEducations = computed(() => normalizeAiEducations(aiResult.value?.educationExperiences))
const displayedAiEducations = computed(() => {
  if (normalizedAiEducations.value.length) return normalizedAiEducations.value
  const data = aiResult.value
  if (!data || !(data.school || data.major || data.education)) return []
  return [{ school: data.school || '', major: data.major || '', degree: data.education || '', time: '' }]
})
const normalizedAiExperiences = computed(() => normalizeAiExperiences(aiResult.value?.workExperiences))
const aiStrengths = computed(() => normalizeTextList(aiResult.value?.strengths))
const aiIssues = computed(() => normalizeTextList(aiResult.value?.issues))
const aiSuggestions = computed(() => normalizeTextList(aiResult.value?.suggestions))
const aiScore = computed(() => Math.min(100, Math.max(0, Number(aiResult.value?.overallScore) || 0)))
const scoreColor = computed(() => {
  if (aiScore.value >= 80) return '#38a169'
  if (aiScore.value >= 60) return '#d69e2e'
  return '#e53e3e'
})

// 时间字符串解析工具：把 "2022.09 - 2026.06" 转为起止年月
function parseTimeRange(timeStr) {
  const result = { startYear: '', startMonth: '', endYear: '', endMonth: '', isCurrent: false }
  if (!timeStr) return result
  
  const parts = timeStr.split('-').map(s => s.trim())
  if (parts[0]) {
    const startParts = parts[0].split('.')
    result.startYear = startParts[0] || ''
    result.startMonth = startParts[1] || ''
  }
  if (parts[1]) {
    if (parts[1] === '至今') {
      result.isCurrent = true
    } else {
      const endParts = parts[1].split('.')
      result.endYear = endParts[0] || ''
      result.endMonth = endParts[1] || ''
    }
  }
  return result
}

// 时间范围格式化：把起止年月转为 "2022.09 - 2026.06"
function formatTimeRange(startY, startM, endY, endM, isCurrent) {
  let start = ''
  if (startY && startM) {
    start = `${startY}.${startM}`
  } else if (startY) {
    start = startY
  }
  
  let end = ''
  if (isCurrent) {
    end = '至今'
  } else if (endY && endM) {
    end = `${endY}.${endM}`
  } else if (endY) {
    end = endY
  }
  
  if (start && end) {
    return `${start} - ${end}`
  }
  return start || end || ''
}

function addSkill() { 
  const v = tagValue.value.trim()
  if (v && !form.skills.includes(v)) form.skills.push(v)
  tagVisible.value = false
  tagValue.value = '' 
}

function removeSkill(s) { 
  form.skills = form.skills.filter(x => x !== s) 
}

function showTagInput() { 
  tagVisible.value = true
  nextTick(() => tagInput.value?.input?.focus()) 
}

/* 教育经历弹窗操作 */
function openAddEdu() {
  editingEduIndex.value = -1
  eduForm.school = ''
  eduForm.major = ''
  eduForm.degree = '本科'
  eduForm.startYear = ''
  eduForm.startMonth = ''
  eduForm.endYear = ''
  eduForm.endMonth = ''
  eduDialogVisible.value = true
}

function openEditEdu(index) {
  editingEduIndex.value = index
  const item = form.educations[index]
  eduForm.school = item.school || ''
  eduForm.major = item.major || ''
  eduForm.degree = item.degree || '本科'
  
  const parsed = parseTimeRange(item.time)
  eduForm.startYear = parsed.startYear
  eduForm.startMonth = parsed.startMonth
  eduForm.endYear = parsed.endYear
  eduForm.endMonth = parsed.endMonth
  
  eduDialogVisible.value = true
}

function confirmEdu() {
  if (!eduForm.school.trim()) {
    ElMessage.warning('请输入学校名称')
    return
  }
  if (!eduForm.startYear || !eduForm.startMonth || !eduForm.endYear || !eduForm.endMonth) {
    ElMessage.warning('请完整选择在校起止年月')
    return
  }
  
  const timeStr = formatTimeRange(eduForm.startYear, eduForm.startMonth, eduForm.endYear, eduForm.endMonth, false)
  const item = {
    school: eduForm.school,
    major: eduForm.major,
    degree: eduForm.degree,
    time: timeStr
  }
  if (editingEduIndex.value > -1) {
    form.educations[editingEduIndex.value] = item
  } else {
    form.educations.push(item)
  }
  eduDialogVisible.value = false
}

function removeEdu(index) {
  form.educations.splice(index, 1)
  ElMessage.info('已移除对应教育经历')
}

/* 工作经历弹窗操作 */
function openAddExp() {
  editingExpIndex.value = -1
  expForm.company = ''
  expForm.role = ''
  expForm.startYear = ''
  expForm.startMonth = ''
  expForm.endYear = ''
  expForm.endMonth = ''
  expForm.isCurrent = false
  expForm.description = ''
  expDialogVisible.value = true
}

function openEditExp(index) {
  editingExpIndex.value = index
  const item = form.experiences[index]
  expForm.company = item.company || ''
  expForm.role = item.role || ''
  
  const parsed = parseTimeRange(item.time)
  expForm.startYear = parsed.startYear
  expForm.startMonth = parsed.startMonth
  expForm.endYear = parsed.endYear
  expForm.endMonth = parsed.endMonth
  expForm.isCurrent = parsed.isCurrent
  
  expForm.description = Array.isArray(item.details) ? item.details.join('\n') : ''
  expDialogVisible.value = true
}

function confirmExp() {
  if (!expForm.company.trim()) {
    ElMessage.warning('请输入公司名称')
    return
  }
  if (!expForm.startYear || !expForm.startMonth) {
    ElMessage.warning('请选择入职年月')
    return
  }
  if (!expForm.isCurrent && (!expForm.endYear || !expForm.endMonth)) {
    ElMessage.warning('请选择离职时间，或勾选“至今”')
    return
  }
  
  const timeStr = formatTimeRange(expForm.startYear, expForm.startMonth, expForm.endYear, expForm.endMonth, expForm.isCurrent)
  const details = expForm.description.split('\n')
    .map(s => s.trim())
    .filter(Boolean)
  
  const item = {
    company: expForm.company,
    role: expForm.role,
    time: timeStr,
    details: details
  }
  if (editingExpIndex.value > -1) {
    form.experiences[editingExpIndex.value] = item
  } else {
    form.experiences.push(item)
  }
  expDialogVisible.value = false
}

function removeExp(index) {
  form.experiences.splice(index, 1)
  ElMessage.info('已移除对应工作经历')
}

async function handleSave() {
  try {
    const educationStr = form.educations.map(e => `${e.school} · ${e.major} · ${e.degree} (${e.time})`).join('; ')
    const experienceStr = form.experiences.map(exp => {
      let head = `${exp.time} | ${exp.company} | ${exp.role}`
      if (Array.isArray(exp.details) && exp.details.length > 0) {
        return head + '\n' + exp.details.map((d, i) => `${i + 1}. ${d}`).join('\n')
      }
      return head
    }).join('\n\n')

    const submitData = {
      ...form,
      education: educationStr,
      experience: experienceStr,
      evaluation: form.summary,
      fileId: form.fileId,
      fileName: form.fileName,
      fileUrl: form.fileUrl
    }
    
    await saveMyResume(submitData)
    ElMessage.success('简历保存成功')
  } catch (e) { 
    ElMessage.error(e.message || '保存失败') 
  }
}

async function handleDeleteResume() {
  try {
    await ElMessageBox.confirm(
      '删除后将同时清除简历所有信息、教育/工作经历及上传的简历文件，且不可恢复。确认删除？',
      '删除简历',
      { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }

  try {
    await deleteMyResume()
    // 清空表单
    form.name = ''
    form.gender = '男'
    form.birth = ''
    form.phone = ''
    form.email = ''
    form.city = ''
    form.summary = ''
    form.educations = []
    form.experiences = []
    form.skills = []
    form.fileId = null
    form.fileName = ''
    form.fileUrl = ''
    ElMessage.success('简历已删除')
  } catch (e) {
    ElMessage.error(e.message || '删除失败')
  }
}

const aiWaitingStatus = computed(() => {
  return aiElapsedMs.value < 30000
    ? '正在等待 AI 服务返回解析结果'
    : '本次模型响应较慢，仍在处理中'
})

function formatAiDuration(duration) {
  const seconds = Math.max(0, duration) / 1000
  return seconds < 10 ? `${seconds.toFixed(1)} 秒` : `${Math.round(seconds)} 秒`
}

function startAiTimer() {
  stopAiTimer()
  aiStartedAt = Date.now()
  aiElapsedMs.value = 0
  aiTimer = window.setInterval(() => {
    aiElapsedMs.value = Date.now() - aiStartedAt
  }, 250)
}

function stopAiTimer() {
  if (aiTimer) {
    window.clearInterval(aiTimer)
    aiTimer = null
  }
  aiElapsedMs.value = aiStartedAt ? Date.now() - aiStartedAt : 0
  return aiElapsedMs.value
}

async function handleAiParse() {
  if (aiParsing.value) return

  const resumeContent = JSON.stringify(form)
  let forceRefresh = false
  if (aiResult.value && lastAiRequestContent === resumeContent) {
    try {
      await ElMessageBox.confirm(
        '简历内容未发生变化。你可以查看上次结果，或重新调用 AI 生成新结果。',
        '再次解析',
        {
          type: 'info',
          confirmButtonText: '重新解析',
          cancelButtonText: '查看上次结果',
          distinguishCancelAndClose: true
        }
      )
      forceRefresh = true
    } catch (action) {
      if (action === 'cancel') aiResultVisible.value = true
      return
    }
  }

  aiParsing.value = true
  startAiTimer()
  try {
    const res = await aiParseResume({
      resumeContent,
      fileType: 'json',
      forceRefresh
    })
    if (res && res.data) {
      const duration = stopAiTimer()
      lastAiRequestContent = resumeContent
      lastAiDurationMs.value = duration
      aiResult.value = res.data
      aiResultVisible.value = true
      ElMessage.success(`AI 解析完成，用时 ${formatAiDuration(duration)}`)
    }
  } catch (error) {
    console.error('AI 解析失败:', error)
  } finally {
    stopAiTimer()
    aiParsing.value = false
  }
}

onBeforeUnmount(stopAiTimer)

function displayAiValue(value) {
  return value == null || String(value).trim() === '' ? '未识别' : value
}

function normalizeSkills(skills) {
  if (Array.isArray(skills)) {
    return skills.map(skill => String(skill).trim()).filter(Boolean)
  }
  if (typeof skills === 'string') {
    return skills.split(/[,，]/).map(skill => skill.trim()).filter(Boolean)
  }
  return []
}

function normalizeTextList(value) {
  if (Array.isArray(value)) {
    return value.map(item => String(item).trim()).filter(Boolean)
  }
  if (typeof value === 'string') {
    return value.split(/\r?\n/).map(item => item.trim().replace(/^[-•]\s*/, '')).filter(Boolean)
  }
  return []
}

function normalizeAiDate(date) {
  if (!date) return ''
  const value = String(date).trim()
  if (/^(至今|现在|present|current)$/i.test(value)) return '至今'
  return value.replace(/[年月]/g, '.').replace(/[/-](\d{1,2})(?:[/-]\d{1,2})?$/, '.$1').replace(/\.$/, '')
}

function normalizeAiEducations(educations) {
  if (!Array.isArray(educations)) return []
  return educations.map(education => {
    const start = normalizeAiDate(education.startDate)
    const end = normalizeAiDate(education.endDate)
    return {
      school: education.school || '',
      major: education.major || '',
      degree: education.degree || '',
      time: start && end ? `${start} - ${end}` : start || end
    }
  }).filter(education => education.school || education.major || education.degree || education.time)
}

function normalizeAiExperiences(experiences) {
  if (!Array.isArray(experiences)) return []
  return experiences.map(experience => {
    const start = normalizeAiDate(experience.startDate)
    const end = normalizeAiDate(experience.endDate)
    const details = String(experience.description || '')
      .split(/\r?\n/)
      .map(detail => detail.trim().replace(/^[-•]\s*/, ''))
      .filter(Boolean)

    return {
      company: experience.company || '',
      role: experience.position || '',
      time: start && end ? `${start} - ${end}` : start || end,
      details
    }
  }).filter(experience => experience.company || experience.role || experience.time || experience.details.length)
}

function applyAiResult() {
  const data = aiResult.value
  if (!data) return

  if (aiFieldSelection.basic) {
    form.name = data.name || form.name
    form.phone = data.phone || form.phone
    form.email = data.email || form.email
  }

  if (aiFieldSelection.summary) {
    form.summary = data.optimizedSummary || data.summary || form.summary
  }

  if (aiFieldSelection.skills && normalizedAiSkills.value.length) {
    form.skills = [...normalizedAiSkills.value]
  }

  if (aiFieldSelection.experience && normalizedAiExperiences.value.length) {
    form.experiences = normalizedAiExperiences.value.map(experience => ({ ...experience, details: [...experience.details] }))
  }

  if (aiFieldSelection.education) {
    if (normalizedAiEducations.value.length) {
      form.educations = normalizedAiEducations.value.map(education => ({ ...education }))
    } else if (data.school || data.major || data.education) {
      form.educations = [{
        school: data.school || '',
        major: data.major || '',
        degree: data.education || '',
        time: ''
      }]
    }
  }

  aiResultVisible.value = false
  ElMessage.success('已应用勾选的解析字段，请检查后保存简历')
}

function handleResumeFileChange(uploadFile) {
  selectedResumeFile.value = uploadFile.raw || null
}

function handleResumeFileRemove() {
  selectedResumeFile.value = null
}

function handleResumeFileExceed() {
  ElMessage.warning('一次只能上传一个简历文件')
}

async function handleUpload() {
  if (!selectedResumeFile.value) {
    ElMessage.warning('请先选择简历文件')
    return
  }
  const file = selectedResumeFile.value
  const extension = file.name.split('.').pop()?.toLowerCase()
  if (!['pdf', 'doc', 'docx'].includes(extension)) {
    ElMessage.error('仅支持 PDF、DOC、DOCX 文件')
    return
  }
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过 10MB')
    return
  }

  try {
    // 如果已有旧文件，先删除旧文件
    if (form.fileId) {
      try {
        await deleteFile(form.fileId)
      } catch (e) {
        console.warn('删除旧文件失败:', e)
      }
    }

    const uploadRes = await uploadFile(file, 'resume')
    if (uploadRes?.data) {
      form.fileId = uploadRes.data.id
      form.fileName = uploadRes.data.originalName || file.name
      form.fileUrl = getFileUrl(uploadRes.data.id)
      uploadVisible.value = false
      resumeUploadRef.value?.clearFiles()
      selectedResumeFile.value = null

      // 自动保存到后端，确保持久化 fileUrl
      await persistFileUrl()

      ElMessage.success('简历文件已保存到服务器，正在解析文件内容...')

      // 自动触发 AI 解析上传的文件
      aiParsing.value = true
      startAiTimer()
      try {
        const res = await aiParseResumeFile(file)
        if (res?.data) {
          const duration = stopAiTimer()
          lastAiDurationMs.value = duration
          aiResult.value = res.data
          aiResultVisible.value = true
          ElMessage.success(`文件解析完成，用时 ${formatAiDuration(duration)}，请确认解析结果`)
        }
      } catch (parseError) {
        console.error('文件 AI 解析失败:', parseError)
        ElMessage.warning('文件已保存，但 AI 解析失败，可稍后点击「AI智能解析」重试')
      } finally {
        stopAiTimer()
        aiParsing.value = false
      }
    }
  } catch (error) {
    console.error('简历文件上传失败:', error)
  }
}

async function persistFileUrl() {
  try {
    const submitData = {
      ...form,
      evaluation: form.summary,
      fileId: form.fileId,
      fileName: form.fileName,
      fileUrl: form.fileUrl
    }
    await saveMyResume(submitData)
  } catch (e) {
    console.error('自动保存简历文件信息失败:', e)
  }
}

async function handleDownloadFile() {
  if (!form.fileId) return
  try {
    const res = await downloadFile(form.fileId)
    const blob = new Blob([res], { type: 'application/octet-stream' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = form.fileName || 'resume.pdf'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch (error) {
    console.error('文件下载失败:', error)
    ElMessage.error('文件下载失败')
  }
}

async function handleRemoveFile() {
  // 删除磁盘文件和数据库记录
  if (form.fileId) {
    try {
      await deleteFile(form.fileId)
    } catch (e) {
      console.warn('删除文件失败:', e)
    }
  }
  form.fileId = null
  form.fileName = ''
  form.fileUrl = ''
  await persistFileUrl()
  ElMessage.info('简历文件已删除')
}
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; margin-bottom: 24px; }
.page-header h2 { margin: 0; font-size: 24px; color: #1a1d21; }
.section-card { border-radius: 12px; margin-bottom: 16px; border: 1px solid #e2e8f0; }
.card-title { margin: 0 0 16px; font-size: 16px; color: #2d3748; padding-bottom: 10px; border-bottom: 1px solid #f0f0f0; font-weight: 700; }
.card-title-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; padding-bottom: 10px; border-bottom: 1px solid #f0f0f0; }
.card-title-row .card-title { margin: 0; padding: 0; border: none; }

.list-item { padding: 16px; margin-bottom: 12px; background: #f8fafc; border-radius: 10px; border: 1px solid #edf2f7; }
.flex-item-row { display: flex; justify-content: space-between; align-items: center; }
.flex-item-row-top { display: flex; justify-content: space-between; align-items: flex-start; }
.list-item-body { flex: 1; }
.list-item-actions { display: flex; gap: 8px; flex-shrink: 0; margin-left: 20px; }

.item-header { display: flex; gap: 16px; align-items: center; margin-bottom: 6px; }
.item-title { font-weight: 700; color: #2d3748; font-size: 14px; }
.item-sub { color: #ED8936; font-size: 13px; font-weight: 600; }
.item-time { color: #a0aec0; font-size: 12px; margin-left: auto; }
.item-detail { color: #4a5568; font-size: 13px; margin: 4px 0 0; }

.item-details { margin-top: 8px; }
.exp-bullet-list { font-size: 13px; color: #4a5568; margin: 0; padding-left: 18px; line-height: 1.6; }
.exp-bullet-list li { margin-bottom: 4px; }

.empty-subtext { font-size: 13px; color: #cbd5e0; text-align: center; padding: 16px 0; font-style: italic; }

.file-item { display: flex; align-items: center; gap: 12px; padding: 14px 16px; background: #f8fafc; border-radius: 10px; border: 1px solid #edf2f7; }
.file-icon { font-size: 24px; color: #3182ce; flex-shrink: 0; }
.file-name { flex: 1; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: #2d3748; font-size: 14px; font-weight: 500; }
.file-actions { display: flex; gap: 8px; flex-shrink: 0; }

.skill-input { display: flex; flex-wrap: wrap; gap: 8px; align-items: center; }
.skill-tag { margin-right: 4px; }
.form-actions { margin-top: 32px; display: flex; gap: 12px; justify-content: center; }

.ai-waiting { padding: 8px 0 4px; }
.ai-waiting-status { margin: 18px 0 10px; color: #303133; font-size: 15px; text-align: center; }
.ai-waiting-metrics { min-height: 20px; display: flex; justify-content: center; flex-wrap: wrap; gap: 8px 20px; color: #606266; font-size: 13px; }

:global(.ai-result-dialog) { max-height: 90vh; display: flex; flex-direction: column; margin-bottom: 0; }
:global(.ai-result-dialog .el-dialog__header) { padding: 18px 24px; border-bottom: 1px solid #ebeef5; }
:global(.ai-result-dialog .el-dialog__body) { flex: 1; min-height: 0; overflow-y: auto; padding: 0 24px; }
:global(.ai-result-dialog .el-dialog__footer) { padding: 14px 24px; border-top: 1px solid #ebeef5; }

.ai-result-header { display: flex; align-items: center; justify-content: space-between; gap: 16px; padding-right: 32px; }
.ai-result-title { display: flex; align-items: center; gap: 10px; min-width: 0; color: #1f2937; }
.ai-result-title .el-icon { flex: 0 0 auto; color: #3182ce; font-size: 21px; }
.ai-result-title strong { font-size: 18px; line-height: 1.4; }
.ai-result-duration { display: inline-flex; align-items: center; gap: 5px; flex: 0 0 auto; color: #606266; font-size: 13px; }

.ai-result-section { margin: 0; padding: 22px 0; border-bottom: 1px solid #edf2f7; }
.ai-result-section:last-child { border-bottom: none; }
.ai-field-selection { display: flex; flex-wrap: wrap; align-items: center; gap: 12px; padding: 12px 16px; margin-bottom: 8px; background: #f0f9ff; border-radius: 8px; border: 1px solid #bae6fd; }
.ai-field-selection-label { font-weight: 600; color: #2d3748; font-size: 14px; }
.ai-profile-section { padding: 18px 0 22px; }
.ai-section-heading { display: flex; align-items: center; gap: 8px; margin-bottom: 14px; color: #2d3748; }
.ai-section-heading .el-icon { flex: 0 0 auto; color: #718096; font-size: 17px; }
.ai-section-heading h4 { margin: 0; font-size: 15px; line-height: 1.4; }

.ai-profile-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 0; margin: 0; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 6px; overflow: hidden; }
.ai-profile-grid > div { min-width: 0; padding: 13px 16px; border-right: 1px solid #e2e8f0; border-bottom: 1px solid #e2e8f0; }
.ai-profile-grid > div:nth-child(3n) { border-right: none; }
.ai-profile-grid > div:nth-last-child(-n + 3) { border-bottom: none; }
.ai-profile-grid dt { margin-bottom: 5px; color: #909399; font-size: 12px; }
.ai-profile-grid dd { margin: 0; color: #303133; font-size: 14px; font-weight: 500; overflow-wrap: anywhere; }

.ai-score-overview { display: grid; grid-template-columns: 190px minmax(0, 1fr); gap: 22px; align-items: center; padding: 16px 18px; background: #f8fafc; border-radius: 6px; }
.ai-score-overview.without-score { grid-template-columns: 1fr; }
.ai-score-overview.without-score .ai-evaluation { padding-left: 0; border-left: none; }
.ai-score-block { min-width: 0; }
.ai-score-block > span { margin-left: 8px; color: #718096; font-size: 12px; }
.ai-score-block .el-progress { margin-top: 8px; }
.ai-score { font-size: 30px; line-height: 1; white-space: nowrap; }
.ai-evaluation { margin: 0; padding-left: 20px; border-left: 1px solid #dbe3ec; color: #4a5568; line-height: 1.7; }
.ai-diagnosis-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 12px; margin-top: 14px; }
.ai-diagnosis-column { min-width: 0; padding: 14px 14px 15px; background: #f8fafc; border-top: 3px solid #cbd5e0; border-radius: 6px; }
.ai-diagnosis-column.is-strength { border-color: #38a169; }
.ai-diagnosis-column.is-issue { border-color: #e53e3e; }
.ai-diagnosis-column.is-suggestion { border-color: #3182ce; }
.ai-diagnosis-column h5 { display: flex; align-items: center; gap: 6px; margin: 0 0 9px; color: #303133; font-size: 14px; }
.ai-diagnosis-column h5 .el-icon { flex: 0 0 auto; }
.ai-diagnosis-column.is-strength h5 .el-icon { color: #38a169; }
.ai-diagnosis-column.is-issue h5 .el-icon { color: #e53e3e; }
.ai-diagnosis-column.is-suggestion h5 .el-icon { color: #3182ce; }
.ai-diagnosis-column ul { margin: 0; padding-left: 18px; color: #4a5568; line-height: 1.6; }
.ai-diagnosis-column li + li { margin-top: 6px; }

.ai-skill-list { display: flex; flex-wrap: wrap; gap: 8px; }
.ai-summary-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px; }
.ai-summary-grid.is-single { grid-template-columns: 1fr; }
.ai-summary-panel { min-width: 0; padding: 14px 16px; background: #f8fafc; border-left: 3px solid #a0aec0; border-radius: 0 6px 6px 0; }
.ai-summary-panel.is-optimized { background: #f0f9ff; border-color: #3182ce; }
.ai-summary-panel h5 { margin: 0 0 8px; color: #303133; font-size: 13px; }
.ai-summary { margin: 0; color: #4a5568; line-height: 1.7; white-space: pre-wrap; overflow-wrap: anywhere; }
.ai-empty { color: #909399; font-size: 14px; }

.ai-timeline-list { position: relative; padding-left: 18px; }
.ai-timeline-list::before { content: ''; position: absolute; top: 7px; bottom: 8px; left: 4px; width: 1px; background: #cbd5e0; }
.ai-timeline-item { position: relative; padding: 0 0 18px 12px; }
.ai-timeline-item:last-child { padding-bottom: 0; }
.ai-timeline-item::before { content: ''; position: absolute; top: 6px; left: -18px; width: 7px; height: 7px; background: #fff; border: 2px solid #3182ce; border-radius: 50%; }
.ai-timeline-heading { display: grid; grid-template-columns: minmax(150px, 1fr) minmax(150px, 1fr) auto; gap: 12px; align-items: baseline; }
.ai-timeline-heading strong { color: #303133; overflow-wrap: anywhere; }
.ai-timeline-heading span { color: #606266; overflow-wrap: anywhere; }
.ai-timeline-heading time { color: #909399; font-size: 13px; white-space: nowrap; }
.ai-timeline-details { margin: 9px 0 0; padding-left: 18px; color: #4a5568; line-height: 1.6; }
.ai-timeline-details li + li { margin-top: 5px; }

/* 年月下拉框样式 */
.time-select-row { display: flex; align-items: center; gap: 8px; width: 100%; }
.time-part-item { display: flex; align-items: center; gap: 8px; flex: 1; }
.time-sep { color: #718096; font-size: 13px; font-weight: bold; }
@media (max-width: 768px) {
  :global(.ai-result-dialog .el-dialog__header) { padding: 16px 18px; }
  :global(.ai-result-dialog .el-dialog__body) { padding: 0 18px; }
  :global(.ai-result-dialog .el-dialog__footer) { padding: 12px 18px; }
  .ai-profile-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .ai-profile-grid > div:nth-child(3n) { border-right: 1px solid #e2e8f0; }
  .ai-profile-grid > div:nth-child(2n) { border-right: none; }
  .ai-profile-grid > div:nth-last-child(-n + 3) { border-bottom: 1px solid #e2e8f0; }
  .ai-profile-grid > div:nth-last-child(-n + 2) { border-bottom: none; }
  .ai-diagnosis-grid { grid-template-columns: 1fr; }
  .ai-summary-grid { grid-template-columns: 1fr; }
}
@media (max-width: 480px) {
  .wrap-mobile { flex-wrap: wrap; }
  .ai-result-header { align-items: flex-start; flex-direction: column; gap: 5px; }
  .ai-profile-grid { grid-template-columns: 1fr; }
  .ai-profile-grid > div { border-right: none; border-bottom: 1px solid #e2e8f0; }
  .ai-profile-grid > div:nth-child(3n),
  .ai-profile-grid > div:nth-child(2n),
  .ai-profile-grid > div:nth-last-child(-n + 2) { border-right: none; border-bottom: 1px solid #e2e8f0; }
  .ai-profile-grid > div:last-child { border-bottom: none; }
  .ai-score-overview { grid-template-columns: 1fr; gap: 14px; }
  .ai-evaluation { padding: 12px 0 0; border-top: 1px solid #dbe3ec; border-left: none; }
  .ai-timeline-heading { grid-template-columns: 1fr; gap: 4px; }
  .ai-timeline-heading time { white-space: normal; }
}
</style>
