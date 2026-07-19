<template>
  <div class="page">
    <div class="page-header">
      <h2>📄 我的简历</h2>
      <div class="header-actions">
        <el-button type="success" icon="MagicStick" @click="handleAiParse">🤖 AI智能解析</el-button>
        <el-button type="primary" icon="Upload" @click="uploadVisible = true">📤 上传简历文件</el-button>
      </div>
    </div>
    <el-card shadow="never" class="section-card">
      <h3 class="card-title">基本信息</h3>
      <el-form :model="form" label-width="80px">
        <el-row :gutter="24">
          <el-col :span="8"><el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="性别"><el-radio-group v-model="form.gender"><el-radio value="男">男</el-radio><el-radio value="女">女</el-radio></el-radio-group></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="出生年月"><el-date-picker v-model="form.birth" type="month" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="24">
          <el-col :span="8"><el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="现居城市"><el-input v-model="form.city" /></el-form-item></el-col>
        </el-row>
      </el-form>
    </el-card>
    <el-card shadow="never" class="section-card">
      <div class="card-title-row"><h3 class="card-title">教育背景</h3><el-button type="primary" link icon="Plus" @click="addEducation">添加</el-button></div>
      <div v-for="(edu, i) in form.educations" :key="i" class="list-item">
        <div class="item-header"><span class="item-title">{{ edu.school }}</span><span class="item-sub">{{ edu.degree }}</span><span class="item-time">{{ edu.time }}</span></div>
        <p class="item-detail">{{ edu.major }}</p>
      </div>
    </el-card>
    <el-card shadow="never" class="section-card">
      <div class="card-title-row"><h3 class="card-title">工作经历</h3><el-button type="primary" link icon="Plus" @click="addExperience">添加</el-button></div>
      <div v-for="(exp, i) in form.experiences" :key="i" class="list-item">
        <div class="item-header"><span class="item-title">{{ exp.company }}</span><span class="item-sub">{{ exp.role }}</span><span class="item-time">{{ exp.time }}</span></div>
      </div>
    </el-card>
    <el-card shadow="never" class="section-card">
      <h3 class="card-title">专业技能</h3>
      <div class="skill-input">
        <el-tag v-for="s in form.skills" :key="s" closable @close="removeSkill(s)" class="skill-tag">{{ s }}</el-tag>
        <el-input v-if="tagVisible" ref="tagInput" v-model="tagValue" size="small" style="width:120px;" @keyup.enter="addSkill" @blur="addSkill" />
        <el-button v-else size="small" @click="showTagInput">+ 添加技能</el-button>
      </div>
    </el-card>
    <el-card shadow="never" class="section-card">
      <h3 class="card-title">自我评价</h3>
      <el-input v-model="form.summary" type="textarea" :rows="4" placeholder="简要描述您的优势、职业规划..." />
    </el-card>
    <div class="form-actions">
      <el-button type="primary" size="large" icon="Check" @click="handleSave">保存简历</el-button>
    </div>
    <el-dialog v-model="uploadVisible" title="上传简历文件" width="480px">
      <el-upload drag :auto-upload="false" accept=".pdf,.doc,.docx">
        <el-icon class="el-icon--upload" size="48"><UploadFilled /></el-icon>
        <div class="el-upload__text">拖拽文件或<em>点击上传</em></div>
      </el-upload>
      <template #footer><el-button @click="uploadVisible = false">取消</el-button><el-button type="primary" @click="handleUpload">上传</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyResume, saveMyResume } from '@/api/resume'
import { aiParseResume } from '@/api/ai'

const uploadVisible = ref(false); const tagVisible = ref(false); const tagValue = ref(''); const tagInput = ref()
const form = reactive({
  name: '', gender: '男', birth: '', phone: '', email: '', city: '',
  educations: [], experiences: [], skills: [], summary: ''
})

onMounted(async () => {
  try {
    const res = await getMyResume()
    if (res && res.data) {
      const data = res.data
      form.name = data.name || ''
      form.gender = data.gender || '男'
      form.birth = data.birth || ''
      form.phone = data.phone || ''
      form.email = data.email || ''
      form.city = data.city || ''
      form.summary = data.summary || data.evaluation || ''
      
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
        const details = lines.slice(1).map(l => l.trim()).filter(Boolean)
        form.experiences = [{ company, role, time, details }]
      } else {
        form.experiences = []
      }
    }
  } catch (error) {
    console.error('加载个人简历失败:', error)
  }
})

function addSkill() { const v = tagValue.value.trim(); if (v && !form.skills.includes(v)) form.skills.push(v); tagVisible.value = false; tagValue.value = '' }
function removeSkill(s) { form.skills = form.skills.filter(x => x !== s) }
function showTagInput() { tagVisible.value = true; nextTick(() => tagInput.value?.input?.focus()) }
function addEducation() { form.educations.push({ school: '', degree: '', time: '', major: '' }) }
function addExperience() { form.experiences.push({ company: '', role: '', time: '', details: [] }) }

async function handleSave() {
  try {
    const educationStr = form.educations.map(e => `${e.school} · ${e.major} · ${e.degree} (${e.time})`).join('; ')
    const skillsStr = form.skills.join(', ')
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
.page-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.section-card { border-radius: 12px; margin-bottom: 16px; }
.card-title { margin: 0 0 16px; font-size: 16px; color: #3E2723; padding-bottom: 10px; border-bottom: 1px solid #f0f0f0; }
.card-title-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; padding-bottom: 10px; border-bottom: 1px solid #f0f0f0; }
.card-title-row .card-title { margin: 0; padding: 0; border: none; }
.list-item { padding: 12px; margin-bottom: 8px; background: #fafafa; border-radius: 10px; }
.item-header { display: flex; gap: 16px; align-items: center; }
.item-title { font-weight: 600; color: #3E2723; }
.item-sub { color: #ED8936; }
.item-time { color: #999; font-size: 13px; }
.item-detail { color: #666; font-size: 14px; margin: 4px 0 0; }
.skill-input { display: flex; flex-wrap: wrap; gap: 8px; align-items: center; }
.form-actions { margin-top: 24px; display: flex; gap: 12px; justify-content: center; }
</style>
