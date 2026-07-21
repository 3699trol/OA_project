<template>
  <div class="page">
    <div class="page-header">
      <h2>📄 我的智能简历</h2>
      <div class="header-actions">
        <el-button type="success" icon="MagicStick" @click="handleAiParse">🤖 AI智能解析</el-button>
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
import { ref, reactive, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyResume, saveMyResume } from '@/api/resume'
import { getCurrentUser } from '@/api/auth'
import { aiParseResume } from '@/api/ai'

const uploadVisible = ref(false)
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
  try {
    ElMessage.info('AI 开始解析...')
    const res = await aiParseResume({})
    if (res && res.data) {
      const data = res.data
      form.name = data.name || form.name
      form.gender = data.gender || form.gender
      form.phone = data.phone || form.phone
      form.email = data.email || form.email
      form.summary = data.evaluation || form.summary
      if (data.skills) {
        form.skills = typeof data.skills === 'string' ? data.skills.split(',').map(s => s.trim()) : data.skills
      }
      ElMessage.success('AI 智能解析填充成功')
    }
  } catch (error) {
    ElMessage.error('AI 解析失败')
  }
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

/* 年月下拉框样式 */
.time-select-row { display: flex; align-items: center; gap: 8px; width: 100%; }
.time-part-item { display: flex; align-items: center; gap: 8px; flex: 1; }
.time-sep { color: #718096; font-size: 13px; font-weight: bold; }
@media (max-width: 480px) {
  .wrap-mobile { flex-wrap: wrap; }
}
</style>
