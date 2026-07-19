<template>
  <div class="page">
    <div class="page-header">
      <div class="title-section">
        <h2>📊 AI 简历诊断与评估</h2>
        <p class="subtitle">深度挖掘简历亮点，诊断薄弱环节，一键生成 AI 核心优化建议</p>
      </div>
      <el-button icon="ArrowLeft" @click="$router.push('/candidate/resume')" plain>返回我的简历</el-button>
    </div>

    <el-row :gutter="24" class="main-content">
      <!-- 左侧：当前简历内容预览与快速编辑 -->
      <el-col :span="10">
        <el-card shadow="never" class="resume-card-preview">
          <template #header>
            <div class="card-header-flex">
              <span class="panel-title"><el-icon><Document /></el-icon> 待评估简历内容</span>
              <el-button type="primary" size="small" @click="isEditing = !isFormValid" link>
                {{ isEditing ? '保存预览' : '修改内容' }}
              </el-button>
            </div>
          </template>

          <div v-if="loadingResume" class="skeleton-wrapper">
            <el-skeleton :rows="5" animated />
          </div>

          <div v-else-if="!isEditing" class="resume-preview-body">
            <div class="preview-item">
              <label>基本信息</label>
              <div class="info-inline">
                <strong>{{ resumeForm.name || '张三' }}</strong> | {{ resumeForm.gender }} | {{ resumeForm.phone || '未填写' }}
              </div>
            </div>
            <div class="preview-item">
              <label>教育经历</label>
              <div v-for="(edu, i) in resumeForm.educations" :key="i" class="edu-item">
                <strong>{{ edu.school }}</strong> · {{ edu.major }} · {{ edu.degree }} <span class="time">({{ edu.time }})</span>
              </div>
              <div v-if="!resumeForm.educations || resumeForm.educations.length === 0" class="empty-subtext">暂无教育经历</div>
            </div>
            <div class="preview-item">
              <label>专业技能</label>
              <div class="tag-container">
                <el-tag v-for="tag in resumeForm.skills" :key="tag" class="skill-tag" size="small" type="info">{{ tag }}</el-tag>
              </div>
              <div v-if="!resumeForm.skills || resumeForm.skills.length === 0" class="empty-subtext">暂无专业技能标签</div>
            </div>
            <div class="preview-item">
              <label>项目经历 / 工作细节</label>
              <div v-for="(exp, i) in resumeForm.experiences" :key="i" class="exp-item">
                <div class="exp-title"><strong>{{ exp.company }}</strong> · {{ exp.role }} <span class="time">({{ exp.time }})</span></div>
                <ul class="exp-details-list">
                  <li v-for="(detail, di) in exp.details" :key="di">{{ detail }}</li>
                </ul>
              </div>
              <div v-if="!resumeForm.experiences || resumeForm.experiences.length === 0" class="empty-subtext">暂无工作经历</div>
            </div>
            <div class="preview-item">
              <label>自我评价</label>
              <p class="summary-text">{{ resumeForm.summary || '未填写自我评价优势' }}</p>
            </div>

            <div class="action-row">
              <el-button type="success" size="large" class="diagnose-btn" :loading="diagnosing" icon="Cpu" @click="runDiagnosis">
                {{ diagnosing ? '智能诊断中...' : '开始 AI 智能诊断' }}
              </el-button>
            </div>
          </div>

          <!-- 编辑表单 -->
          <el-form v-else :model="resumeForm" label-position="top" class="edit-form">
            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="姓名" required>
                  <el-input v-model="resumeForm.name" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="性别">
                  <el-select v-model="resumeForm.gender" style="width: 100%;">
                    <el-option label="男" value="男" />
                    <el-option label="女" value="女" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="专业技能">
              <el-select
                v-model="resumeForm.skills"
                multiple
                filterable
                allow-create
                default-first-option
                placeholder="键入后回车添加技能标签"
                style="width: 100%;"
              />
            </el-form-item>
            <el-form-item label="自我评价 / 个人优势">
              <el-input v-model="resumeForm.summary" type="textarea" :rows="4" />
            </el-form-item>
            <div class="form-save-row">
              <el-button type="primary" @click="saveDraft">保存预览</el-button>
              <el-button @click="isEditing = false">取消</el-button>
            </div>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右侧：评估诊断结果 -->
      <el-col :span="14">
        <div v-if="!hasReport && !diagnosing" class="empty-report-wrapper">
          <el-empty description="点击左侧【开始 AI 智能诊断】按钮，即可多维扫描您的简历并生成全方位优化建议">
            <el-button type="primary" icon="Cpu" @click="runDiagnosis">立即体验 AI 诊断</el-button>
          </el-empty>
        </div>

        <div v-else-if="diagnosing" class="diagnosing-loading-card">
          <div class="loader-spinner">
            <el-icon class="is-loading" size="48" color="#e76f51"><Loading /></el-icon>
          </div>
          <h3>AI 正在多维度评估您的简历...</h3>
          <div class="scanning-steps">
            <div v-for="(step, index) in scanSteps" :key="index" class="scan-step" :class="{ active: currentScanStep >= index }">
              <el-icon class="step-icon">
                <CircleCheck v-if="currentScanStep > index" color="#67C23A" />
                <Loading v-else-if="currentScanStep === index" class="is-loading" color="#e76f51" />
                <CircleClose v-else color="#C0C4CC" />
              </el-icon>
              <span>{{ step }}</span>
            </div>
          </div>
        </div>

        <!-- 诊断结果展示 -->
        <div v-else class="report-content-wrapper">
          <!-- 1. 核心得分卡 -->
          <el-card shadow="never" class="score-card mb-4">
            <div class="score-card-body">
              <div class="score-gauge">
                <div class="gauge-value" :class="scoreColorClass">{{ reportData.score }}</div>
                <div class="gauge-label">综合评分</div>
              </div>
              <div class="score-summary">
                <h4>🎉 简历诊断完成！</h4>
                <p v-if="reportData.score >= 85">
                  您的简历表现非常优秀，核心项目经历完整，专业技能与市场契合度极高。只需在部分量化绩效和细节处稍加打磨即可成为“Offer斩杀器”！
                </p>
                <p v-else>
                  您的简历整体框架完整，但仍有较大提升空间。主要薄弱点在于项目产出缺乏具体量化数据。建议采纳下方 AI 优化，一键润色。
                </p>
                <div class="score-actions">
                  <el-button type="success" icon="MagicStick" @click="handleOptimize" :loading="optimizing">
                    {{ optimizing ? 'AI 智能润色中...' : '🤖 AI 一键智能润色' }}
                  </el-button>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 2. 维度雷达分析 -->
          <el-card shadow="never" class="dimension-card mb-4">
            <template #header>
              <span class="panel-title"><el-icon><DataAnalysis /></el-icon> 简历完整度及核心评估维度</span>
            </template>
            <div class="dimension-progress-list">
              <div v-for="dim in reportData.dimensions" :key="dim.name" class="dim-progress-item">
                <div class="dim-info">
                  <span class="dim-name">{{ dim.name }}</span>
                  <span class="dim-score">{{ dim.score }}分</span>
                </div>
                <el-progress :percentage="dim.score" :color="getDimensionColor(dim.score)" :show-text="false" stroke-width="8" />
              </div>
            </div>
          </el-card>

          <!-- 3. 亮点与不足 -->
          <el-row :gutter="16" class="mb-4">
            <el-col :span="12">
              <el-card shadow="never" class="analysis-box-card highlight-box">
                <template #header>
                  <span class="panel-title highlight-title"><el-icon><CircleCheck /></el-icon> 核心亮点 (Highlights)</span>
                </template>
                <ul class="bullet-list positive-list">
                  <li v-for="(hl, i) in reportData.highlights" :key="i">{{ hl }}</li>
                </ul>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card shadow="never" class="analysis-box-card weakness-box">
                <template #header>
                  <span class="panel-title weakness-title"><el-icon><Warning /></el-icon> 诊断不足 (Weaknesses)</span>
                </template>
                <ul class="bullet-list negative-list">
                  <li v-for="(wl, i) in reportData.weaknesses" :key="i">{{ wl }}</li>
                </ul>
              </el-card>
            </el-col>
          </el-row>

          <!-- 4. 优化建议 -->
          <el-card shadow="never" class="advice-card mb-4">
            <template #header>
              <span class="panel-title"><el-icon><ChatLineSquare /></el-icon> AI 专家落地改进方案</span>
            </template>
            <div class="advice-text-body">
              <p>{{ reportData.advice }}</p>
            </div>
          </el-card>

          <!-- 5. 优化预览弹窗 -->
          <el-dialog v-model="optimizeDialogVisible" title="🤖 AI 简历黄金点金润色预览" width="850px" class="optimize-dialog">
            <div class="optimize-intro">
              <el-alert title="AI 已经结合 STAR 原则，对您的专业技能、项目成果以及自我总结进行了深度重构和专业词汇修饰，使之更符合大厂及HR的招聘偏好。" type="success" :closable="false" show-icon class="mb-3" />
            </div>
            
            <div class="compare-panels">
              <!-- 左边：原有的 -->
              <div class="compare-panel-box original">
                <div class="p-header">原有简历内容</div>
                <div class="p-body">
                  <div class="compare-block">
                    <label>专业技能</label>
                    <div class="val-tags">
                      <el-tag v-for="tag in resumeForm.skills" :key="tag" class="mr-1" size="small">{{ tag }}</el-tag>
                    </div>
                  </div>
                  <div class="compare-block">
                    <label>自我评价</label>
                    <p class="summary-desc">{{ resumeForm.summary || '未填写自我评价' }}</p>
                  </div>
                  <div class="compare-block">
                    <label>工作亮点细节</label>
                    <p class="summary-desc" style="white-space: pre-line;">{{ originalExperienceText }}</p>
                  </div>
                </div>
              </div>

              <!-- 右边：优化的 -->
              <div class="compare-panel-box optimized">
                <div class="p-header"><el-icon><MagicStick /></el-icon> AI 黄金优选润色</div>
                <div class="p-body">
                  <div class="compare-block">
                    <label>专业技能 (技术亮点提炼)</label>
                    <div class="val-tags">
                      <el-tag v-for="tag in optimizedData.skills" :key="tag" type="success" class="mr-1" size="small">{{ tag }}</el-tag>
                    </div>
                  </div>
                  <div class="compare-block">
                    <label>自我评价 (高亮竞争力优势)</label>
                    <p class="summary-desc glow-border">{{ optimizedData.summary }}</p>
                  </div>
                  <div class="compare-block">
                    <label>工作亮点细节 (基于 STAR 法则量化成果)</label>
                    <p class="summary-desc glow-border" style="white-space: pre-line;">{{ optimizedData.experience }}</p>
                  </div>
                </div>
              </div>
            </div>

            <template #footer>
              <div class="dialog-footer">
                <el-button @click="optimizeDialogVisible = false">继续打磨</el-button>
                <el-button type="success" icon="Check" @click="applyAiOptimization">一键采纳并更新我的简历</el-button>
              </div>
            </template>
          </el-dialog>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyResume, saveMyResume } from '@/api/resume'
import { aiEvaluateResume, aiOptimizeResume } from '@/api/ai'

const loadingResume = ref(false)
const isEditing = ref(false)
const diagnosing = ref(false)
const hasReport = ref(false)
const optimizing = ref(false)
const optimizeDialogVisible = ref(false)

const currentScanStep = ref(0)
const scanSteps = [
  '正在读取简历完整性信息...',
  '正在对齐当前中高级招聘岗位大纲要求...',
  '正在通过大模型进行关键词与技能图谱覆盖检测...',
  '正在结合 STAR 描述法则进行成果量化核验...',
  '正在整合专家智库并输出简历改进建议方案...'
]

const resumeForm = reactive({
  name: '', gender: '男', birth: '', phone: '', email: '', city: '',
  educations: [], experiences: [], skills: [], summary: ''
})

const reportData = ref({
  score: 0,
  dimensions: [],
  highlights: [],
  weaknesses: [],
  advice: ''
})

const optimizedData = ref({
  skills: [],
  summary: '',
  experience: ''
})

const originalExperienceText = computed(() => {
  return resumeForm.experiences.map(e => {
    let base = `${e.company} | ${e.role}`
    if (e.details && e.details.length > 0) {
      return base + '\n' + e.details.map(d => `· ${d}`).join('\n')
    }
    return base
  }).join('\n\n')
})

const isFormValid = computed(() => {
  return resumeForm.name.trim() !== ''
})

const scoreColorClass = computed(() => {
  const s = reportData.value.score
  if (s >= 90) return 'text-success'
  if (s >= 80) return 'text-warning'
  return 'text-danger'
})

onMounted(async () => {
  await loadResume()
})

async function loadResume() {
  loadingResume.value = true
  try {
    const res = await getMyResume()
    if (res && res.data) {
      const data = res.data
      resumeForm.name = data.name || ''
      resumeForm.gender = data.gender || '男'
      resumeForm.birth = data.birth || ''
      resumeForm.phone = data.phone || ''
      resumeForm.email = data.email || ''
      resumeForm.city = data.city || ''
      resumeForm.summary = data.summary || data.evaluation || ''
      
      if (Array.isArray(data.skills)) {
        resumeForm.skills = [...data.skills]
      } else if (typeof data.skills === 'string') {
        resumeForm.skills = data.skills.split(',').map(s => s.trim()).filter(Boolean)
      } else {
        resumeForm.skills = []
      }

      if (Array.isArray(data.educations) && data.educations.length > 0) {
        resumeForm.educations = [...data.educations]
      } else if (data.education) {
        const parts = data.education.split('·').map(s => s.trim())
        const school = parts[0] || ''
        const major = parts[1] || ''
        const degreeParts = parts[2] ? parts[2].split(' ') : []
        const degree = degreeParts[0] || ''
        const time = degreeParts[1] ? degreeParts[1].replace(/[()]/g, '') : ''
        resumeForm.educations = [{ school, major, degree, time }]
      } else {
        resumeForm.educations = []
      }

      if (Array.isArray(data.experiences) && data.experiences.length > 0) {
        resumeForm.experiences = [...data.experiences]
      } else if (data.experience) {
        const lines = data.experience.split('\n')
        const firstLineParts = lines[0].split('|').map(s => s.trim())
        const time = firstLineParts[0] || ''
        const company = firstLineParts[1] || ''
        const role = firstLineParts[2] || ''
        const details = lines.slice(1).map(l => l.trim().replace(/^\d+\.\s*/, '')).filter(Boolean)
        resumeForm.experiences = [{ company, role, time, details }]
      } else {
        resumeForm.experiences = []
      }
    }
  } catch (error) {
    console.error('加载个人简历失败:', error)
  } finally {
    loadingResume.value = false
  }
}

function saveDraft() {
  if (!isFormValid.value) {
    ElMessage.warning('请输入姓名信息')
    return
  }
  isEditing.value = false
  ElMessage.success('简历预览修改保存成功，可进行 AI 诊断')
}

async function runDiagnosis() {
  diagnosing.value = true
  hasReport.value = false
  currentScanStep.value = 0
  
  // 模拟精美的诊断动画
  const interval = setInterval(() => {
    if (currentScanStep.value < scanSteps.length - 1) {
      currentScanStep.value++
    } else {
      clearInterval(interval)
    }
  }, 1000)

  try {
    const res = await aiEvaluateResume({
      skills: resumeForm.skills,
      summary: resumeForm.summary,
      experience: originalExperienceText.value
    })
    
    // 保证动画演示至少3秒
    setTimeout(() => {
      clearInterval(interval)
      if (res && res.data) {
        reportData.value = res.data
        hasReport.value = true
      }
      diagnosing.value = false
    }, 3200)
  } catch (error) {
    clearInterval(interval)
    diagnosing.value = false
    ElMessage.error('简历诊断分析失败，请重试')
  }
}

async function handleOptimize() {
  optimizing.value = true
  try {
    const res = await aiOptimizeResume({
      skills: resumeForm.skills,
      summary: resumeForm.summary,
      experience: originalExperienceText.value
    })
    if (res && res.data) {
      optimizedData.value = {
        skills: res.data.skills || [],
        summary: res.data.summary || '',
        experience: res.data.experience || ''
      }
      optimizeDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('优化方案生成失败')
  } finally {
    optimizing.value = false
  }
}

async function applyAiOptimization() {
  try {
    resumeForm.skills = [...optimizedData.value.skills]
    resumeForm.summary = optimizedData.value.summary
    
    // 解析优化后的 experience 字符并写回到数组
    if (optimizedData.value.experience) {
      const bulletPoints = optimizedData.value.experience.split('\n')
        .map(line => line.trim().replace(/^\d+\.\s*/, ''))
        .filter(Boolean)
      
      if (resumeForm.experiences.length > 0) {
        resumeForm.experiences[0].details = bulletPoints
      } else {
        resumeForm.experiences.push({
          company: '推荐重构企业',
          role: '研发核心专家',
          time: '至今',
          details: bulletPoints
        })
      }
    }

    // 联动保存到实际的 Mock 数据
    const educationStr = resumeForm.educations.map(e => `${e.school} · ${e.major} · ${e.degree} (${e.time})`).join('; ')
    const skillsStr = resumeForm.skills.join(', ')
    const experienceStr = resumeForm.experiences.map(exp => {
      let head = `${exp.time} | ${exp.company} | ${exp.role}`
      if (Array.isArray(exp.details) && exp.details.length > 0) {
        return head + '\n' + exp.details.map((d, i) => `${i + 1}. ${d}`).join('\n')
      }
      return head
    }).join('\n\n')

    const submitData = {
      ...resumeForm,
      education: educationStr,
      experience: experienceStr,
      evaluation: resumeForm.summary
    }

    await saveMyResume(submitData)
    optimizeDialogVisible.value = false
    ElMessage.success('🎉 简历一键采纳优化并存入系统！可在诊断左侧及【我的简历】页预览新内容')
  } catch (error) {
    ElMessage.error('采纳更新失败')
  }
}

function getDimensionColor(score) {
  if (score >= 90) return '#67C23A'
  if (score >= 80) return '#e76f51'
  return '#F56C6C'
}
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.title-section h2 {
  margin: 0;
  font-size: 24px;
  color: #1a1d21;
}

.title-section .subtitle {
  margin: 6px 0 0;
  font-size: 13px;
  color: #7a8590;
}

.main-content {
  margin-top: 12px;
}

.resume-card-preview {
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  height: calc(100vh - 160px);
  overflow-y: auto;
}

.card-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-title {
  font-size: 15px;
  font-weight: 700;
  color: #2d3748;
  display: flex;
  align-items: center;
  gap: 8px;
}

.skeleton-wrapper {
  padding: 20px 10px;
}

.resume-preview-body {
  padding: 8px 4px;
}

.preview-item {
  margin-bottom: 20px;
}

.preview-item label {
  display: block;
  font-size: 12px;
  color: #e76f51;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 6px;
  border-left: 3px solid #e76f51;
  padding-left: 8px;
}

.info-inline {
  font-size: 14px;
  color: #4a5568;
}

.edu-item, .exp-title {
  font-size: 14px;
  color: #2d3748;
  margin-bottom: 4px;
}

.time {
  font-size: 12px;
  color: #a0aec0;
  margin-left: 6px;
}

.empty-subtext {
  font-size: 13px;
  color: #cbd5e0;
  font-style: italic;
}

.tag-container {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.skill-tag {
  background-color: #f7fafc;
  color: #4a5568;
  border-color: #edf2f7;
}

.exp-details-list {
  padding-left: 18px;
  margin: 4px 0 0 0;
  font-size: 13px;
  color: #4a5568;
}

.exp-details-list li {
  margin-bottom: 4px;
}

.summary-text {
  font-size: 13px;
  color: #4a5568;
  line-height: 1.6;
  white-space: pre-wrap;
  margin: 0;
}

.action-row {
  margin-top: 32px;
  text-align: center;
}

.diagnose-btn {
  width: 90%;
  border-radius: 20px;
  font-weight: 700;
  box-shadow: 0 4px 12px rgba(231, 111, 81, 0.25);
  background: linear-gradient(135deg, #e76f51 0%, #f4a261 100%);
  border: none;
}

.diagnose-btn:hover {
  background: linear-gradient(135deg, #f4a261 0%, #e76f51 100%);
}

.edit-form {
  padding: 10px 4px;
}

.form-save-row {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 24px;
}

/* 右边结果 */
.empty-report-wrapper {
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  height: calc(100vh - 160px);
  display: flex;
  align-items: center;
  justify-content: center;
}

.diagnosing-loading-card {
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  height: calc(100vh - 160px);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.loader-spinner {
  margin-bottom: 24px;
}

.diagnosing-loading-card h3 {
  font-size: 18px;
  color: #2d3748;
  margin: 0 0 32px 0;
}

.scanning-steps {
  width: 80%;
  max-width: 450px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.scan-step {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: #cbd5e0;
  transition: color 0.3s;
}

.scan-step.active {
  color: #4a5568;
}

.scan-step.active span {
  font-weight: 600;
}

.step-icon {
  font-size: 18px;
}

/* 评估诊断报告面板 */
.report-content-wrapper {
  height: calc(100vh - 160px);
  overflow-y: auto;
  padding-right: 4px;
}

.mb-4 {
  margin-bottom: 16px;
}

.score-card {
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  background: linear-gradient(135deg, #fffcf9 0%, #fff 100%);
}

.score-card-body {
  display: flex;
  align-items: center;
  gap: 32px;
}

.score-gauge {
  text-align: center;
  padding: 10px;
}

.gauge-value {
  font-size: 56px;
  font-weight: 900;
  line-height: 1;
}

.text-success { color: #67C23A; text-shadow: 0 4px 10px rgba(103, 194, 58, 0.15); }
.text-warning { color: #e76f51; text-shadow: 0 4px 10px rgba(231, 111, 81, 0.15); }
.text-danger { color: #F56C6C; text-shadow: 0 4px 10px rgba(245, 108, 108, 0.15); }

.gauge-label {
  font-size: 11px;
  color: #718096;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-top: 6px;
  font-weight: 600;
}

.score-summary {
  flex: 1;
}

.score-summary h4 {
  margin: 0 0 8px;
  font-size: 16px;
  color: #2d3748;
}

.score-summary p {
  font-size: 13px;
  color: #4a5568;
  line-height: 1.6;
  margin: 0 0 16px 0;
}

.dimension-card {
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.dimension-progress-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.dim-progress-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.dim-info {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
}

.dim-name {
  color: #4a5568;
  font-weight: 600;
}

.dim-score {
  color: #e76f51;
  font-weight: 700;
}

.analysis-box-card {
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  height: 240px;
  overflow-y: auto;
}

.highlight-box {
  background: #fcfdf9;
}

.weakness-box {
  background: #fffbfa;
}

.highlight-title {
  color: #67C23A !important;
}

.weakness-title {
  color: #e76f51 !important;
}

.bullet-list {
  padding-left: 18px;
  margin: 0;
  font-size: 13px;
  line-height: 1.6;
}

.bullet-list li {
  margin-bottom: 8px;
}

.positive-list li {
  color: #2d3748;
}

.negative-list li {
  color: #2d3748;
}

.advice-card {
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  background-color: #fafbfc;
}

.advice-text-body p {
  margin: 0;
  font-size: 13px;
  color: #4a5568;
  line-height: 1.6;
}

/* 优化比较面板的样式 */
.optimize-dialog :deep(.el-dialog__body) {
  padding-top: 10px;
}

.compare-panels {
  display: flex;
  gap: 20px;
  margin-top: 16px;
  height: 380px;
}

.compare-panel-box {
  flex: 1;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.compare-panel-box.optimized {
  border-color: #67C23A;
  box-shadow: 0 4px 18px rgba(103, 194, 58, 0.08);
}

.compare-panel-box .p-header {
  height: 40px;
  line-height: 40px;
  background-color: #edf2f7;
  text-align: center;
  font-weight: 700;
  color: #4a5568;
  font-size: 13px;
}

.compare-panel-box.optimized .p-header {
  background-color: #e1f5fe;
  color: #2e7d32;
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.compare-panel-box .p-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.compare-block {
  margin-bottom: 16px;
}

.compare-block label {
  display: block;
  font-size: 11px;
  font-weight: 700;
  color: #718096;
  margin-bottom: 6px;
}

.summary-desc {
  font-size: 13px;
  color: #4a5568;
  line-height: 1.6;
  margin: 0;
  background: #f8fafc;
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid #edf2f7;
}

.glow-border {
  border-color: #a5d6a7;
  background: #f1f8e9;
  color: #1b5e20;
}

.mr-1 {
  margin-right: 4px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
