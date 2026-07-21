<template>
  <div class="page">
    <div class="page-header">
      <h2>📄 我的智能简历</h2>
      <div class="header-actions">
        <el-button type="success" icon="MagicStick" :loading="aiParsing" @click="handleAiParse">AI智能解析</el-button>
        <el-button type="primary" icon="Upload" @click="uploadVisible = true">📤 上传简历文件</el-button>
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

    <!-- AI 解析结果预览 -->
    <el-dialog v-model="aiResultVisible" title="AI 简历解析结果" width="min(720px, 92vw)" destroy-on-close>
      <div v-if="aiResult" class="ai-result">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="姓名">{{ displayAiValue(aiResult.name) }}</el-descriptions-item>
          <el-descriptions-item label="工作年限">
            {{ aiResult.workYears == null ? '未识别' : `${aiResult.workYears} 年` }}
          </el-descriptions-item>
          <el-descriptions-item label="手机号">{{ displayAiValue(aiResult.phone) }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ displayAiValue(aiResult.email) }}</el-descriptions-item>
          <el-descriptions-item label="学校">{{ displayAiValue(aiResult.school) }}</el-descriptions-item>
          <el-descriptions-item label="专业">{{ displayAiValue(aiResult.major) }}</el-descriptions-item>
          <el-descriptions-item label="教育背景" :span="2">{{ displayAiValue(aiResult.education) }}</el-descriptions-item>
        </el-descriptions>

        <section class="ai-result-section ai-diagnosis">
          <div class="ai-section-heading">
            <h4>AI 简历诊断</h4>
            <strong v-if="aiResult.overallScore != null" class="ai-score">{{ aiScore }} 分</strong>
          </div>
          <el-progress
            v-if="aiResult.overallScore != null"
            :percentage="aiScore"
            :stroke-width="10"
            :color="scoreColor"
          />
          <p v-if="aiResult.evaluation" class="ai-summary">{{ aiResult.evaluation }}</p>
          <span v-else class="ai-empty">暂无综合评价</span>

          <div class="ai-diagnosis-grid">
            <div class="ai-diagnosis-column is-strength">
              <h5>简历优势</h5>
              <ul v-if="aiStrengths.length">
                <li v-for="item in aiStrengths" :key="item">{{ item }}</li>
              </ul>
              <span v-else class="ai-empty">暂无</span>
            </div>
            <div class="ai-diagnosis-column is-issue">
              <h5>存在问题</h5>
              <ul v-if="aiIssues.length">
                <li v-for="item in aiIssues" :key="item">{{ item }}</li>
              </ul>
              <span v-else class="ai-empty">暂无</span>
            </div>
            <div class="ai-diagnosis-column is-suggestion">
              <h5>修改建议</h5>
              <ul v-if="aiSuggestions.length">
                <li v-for="item in aiSuggestions" :key="item">{{ item }}</li>
              </ul>
              <span v-else class="ai-empty">暂无</span>
            </div>
          </div>
        </section>

        <section class="ai-result-section">
          <h4>专业技能</h4>
          <div v-if="normalizedAiSkills.length" class="ai-skill-list">
            <el-tag v-for="skill in normalizedAiSkills" :key="skill" effect="plain">{{ skill }}</el-tag>
          </div>
          <span v-else class="ai-empty">未识别</span>
        </section>

        <section class="ai-result-section">
          <h4>个人摘要</h4>
          <p v-if="aiResult.summary" class="ai-summary">{{ aiResult.summary }}</p>
          <span v-else class="ai-empty">未识别</span>
        </section>

        <section v-if="aiResult.optimizedSummary" class="ai-result-section ai-optimized-summary">
          <h4>AI 优化后的个人摘要</h4>
          <p class="ai-summary">{{ aiResult.optimizedSummary }}</p>
        </section>

        <section class="ai-result-section">
          <h4>工作经历</h4>
          <div v-if="normalizedAiExperiences.length" class="ai-experience-list">
            <div v-for="(experience, index) in normalizedAiExperiences" :key="index" class="ai-experience-item">
              <div class="ai-experience-heading">
                <strong>{{ experience.company || '未识别公司' }}</strong>
                <span>{{ experience.role || '未识别职位' }}</span>
                <time>{{ experience.time || '时间未识别' }}</time>
              </div>
              <p v-for="(detail, detailIndex) in experience.details" :key="detailIndex">{{ detail }}</p>
            </div>
          </div>
          <span v-else class="ai-empty">未识别</span>
        </section>
      </div>
      <template #footer>
        <el-button @click="aiResultVisible = false">关闭</el-button>
        <el-button type="primary" icon="Check" @click="applyAiResult">应用提取及优化结果</el-button>
      </template>
    </el-dialog>

    <!-- 上传简历文件弹窗 -->
    <el-dialog v-model="uploadVisible" title="上传简历文件" width="480px">
      <el-upload drag :auto-upload="false" accept=".pdf,.doc,.docx">
        <el-icon class="el-icon--upload" size="48"><UploadFilled /></el-icon>
        <div class="el-upload__text">拖拽文件或<em>点击上传</em></div>
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
import { ref, reactive, computed, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyResume, saveMyResume } from '@/api/resume'
import { getCurrentUser } from '@/api/auth'
import { aiParseResume } from '@/api/ai'

const uploadVisible = ref(false)
const aiResultVisible = ref(false)
const aiParsing = ref(false)
const aiResult = ref(null)
const tagVisible = ref(false)
const tagValue = ref('')
const tagInput = ref()

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
  educations: [], experiences: [], skills: [], summary: ''
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
      evaluation: form.summary
    }
    
    await saveMyResume(submitData)
    ElMessage.success('简历保存成功')
  } catch (e) { 
    ElMessage.error(e.message || '保存失败') 
  }
}

async function handleAiParse() {
  if (aiParsing.value) return
  aiParsing.value = true
  try {
    const res = await aiParseResume({
      resumeContent: JSON.stringify(form),
      fileType: 'json'
    })
    if (res && res.data) {
      aiResult.value = res.data
      aiResultVisible.value = true
      ElMessage.success('AI 解析完成，请确认解析结果')
    }
  } catch (error) {
    console.error('AI 解析失败:', error)
  } finally {
    aiParsing.value = false
  }
}

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
  return value.replace(/-(\d{1,2})(?:-\d{1,2})?$/, '.$1')
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

  form.name = data.name || form.name
  form.phone = data.phone || form.phone
  form.email = data.email || form.email
  form.summary = data.optimizedSummary || data.summary || form.summary

  if (normalizedAiSkills.value.length) {
    form.skills = [...normalizedAiSkills.value]
  }
  if (normalizedAiExperiences.value.length) {
    form.experiences = normalizedAiExperiences.value.map(experience => ({ ...experience, details: [...experience.details] }))
  }
  if (data.school || data.major || data.education) {
    form.educations = [{
      school: data.school || '',
      major: data.major || '',
      degree: data.education || '',
      time: ''
    }]
  }

  aiResultVisible.value = false
  ElMessage.success('解析结果已应用，请检查后保存简历')
}

async function handleUpload() {
  ElMessage.success('上传简历文件成功，已触发自动AI解析')
  uploadVisible.value = false
  await handleAiParse()
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

.skill-input { display: flex; flex-wrap: wrap; gap: 8px; align-items: center; }
.skill-tag { margin-right: 4px; }
.form-actions { margin-top: 32px; display: flex; gap: 12px; justify-content: center; }

.ai-result-section { margin-top: 20px; }
.ai-result-section h4 { margin: 0 0 10px; color: #2d3748; font-size: 15px; }
.ai-section-heading { display: flex; align-items: center; justify-content: space-between; gap: 16px; }
.ai-section-heading h4 { margin-bottom: 0; }
.ai-score { color: #303133; font-size: 18px; white-space: nowrap; }
.ai-diagnosis > .ai-summary { margin-top: 12px; }
.ai-diagnosis-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; margin-top: 18px; }
.ai-diagnosis-column { padding-left: 12px; border-left: 3px solid #cbd5e0; min-width: 0; }
.ai-diagnosis-column.is-strength { border-color: #38a169; }
.ai-diagnosis-column.is-issue { border-color: #e53e3e; }
.ai-diagnosis-column.is-suggestion { border-color: #3182ce; }
.ai-diagnosis-column h5 { margin: 0 0 8px; color: #303133; font-size: 14px; }
.ai-diagnosis-column ul { margin: 0; padding-left: 18px; color: #4a5568; line-height: 1.6; }
.ai-diagnosis-column li + li { margin-top: 6px; }
.ai-optimized-summary { padding: 14px; background: #f0f9ff; border-left: 3px solid #3182ce; }
.ai-skill-list { display: flex; flex-wrap: wrap; gap: 8px; }
.ai-summary { margin: 0; color: #4a5568; line-height: 1.7; white-space: pre-wrap; }
.ai-empty { color: #909399; font-size: 14px; }
.ai-experience-list { border-top: 1px solid #ebeef5; }
.ai-experience-item { padding: 14px 0; border-bottom: 1px solid #ebeef5; }
.ai-experience-heading { display: grid; grid-template-columns: minmax(120px, 1fr) minmax(120px, 1fr) auto; gap: 12px; align-items: baseline; }
.ai-experience-heading span { color: #606266; }
.ai-experience-heading time { color: #909399; font-size: 13px; }
.ai-experience-item p { margin: 8px 0 0; color: #4a5568; line-height: 1.6; }
.ai-result :deep(.el-descriptions__content) { overflow-wrap: anywhere; }

/* 年月下拉框样式 */
.time-select-row { display: flex; align-items: center; gap: 8px; width: 100%; }
.time-part-item { display: flex; align-items: center; gap: 8px; flex: 1; }
.time-sep { color: #718096; font-size: 13px; font-weight: bold; }
@media (max-width: 768px) {
  .ai-diagnosis-grid { grid-template-columns: 1fr; }
}
@media (max-width: 480px) {
  .wrap-mobile { flex-wrap: wrap; }
  .ai-experience-heading { grid-template-columns: 1fr; gap: 4px; }
}
</style>
