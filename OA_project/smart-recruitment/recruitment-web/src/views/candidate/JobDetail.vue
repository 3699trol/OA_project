<template>
  <div class="job-detail-page">
    <el-button icon="ArrowLeft" @click="$router.back()" link class="back-link">返回职位列表</el-button>

    <!-- 职位头部卡片：核心信息 + 操作按钮一体 -->
    <el-card shadow="never" class="hero-card">
      <div class="hero-top">
        <div class="hero-info">
          <div class="title-row">
            <h1>{{ job.jobName }}</h1>
            <el-tag v-if="job.isUrgent" type="danger" size="small" effect="dark">急聘</el-tag>
          </div>
          <div class="company-name">{{ job.department }}</div>
          <div class="meta-row">
            <span><el-icon><Location /></el-icon>{{ job.city || '不限' }}</span>
            <el-divider direction="vertical" />
            <span><el-icon><School /></el-icon>{{ job.education || '不限' }}</span>
            <el-divider direction="vertical" />
            <span><el-icon><Clock /></el-icon>{{ job.experience || '不限' }}</span>
            <el-divider direction="vertical" />
            <span><el-icon><User /></el-icon>招聘 {{ job.headcount || 1 }} 人</span>
          </div>
        </div>
        <div class="salary-box">
          <div class="salary">{{ job.salaryMin != null && job.salaryMax != null ? job.salaryMin + 'K-' + job.salaryMax + 'K' : '薪资面议' }}</div>
        </div>
      </div>
      <div class="hero-actions">
        <el-button type="primary" size="large" @click="handleApply" :loading="applying">立即投递</el-button>
        <el-button size="large" plain>收藏职位</el-button>
      </div>
    </el-card>

    <!-- 职位描述：为空时不渲染，避免空卡片 -->
    <el-card v-if="job.description" shadow="never" class="section-card">
      <h3 class="card-title">📋 职位描述</h3>
      <div class="desc-text">{{ job.description }}</div>
    </el-card>

    <!-- 任职要求 -->
    <el-card v-if="job.requirements" shadow="never" class="section-card">
      <h3 class="card-title">📝 任职要求</h3>
      <div class="desc-text">{{ job.requirements }}</div>
    </el-card>

    <!-- 技能标签 -->
    <el-card v-if="job.skills && job.skills.length" shadow="never" class="section-card">
      <h3 class="card-title">🏷️ 技能标签</h3>
      <div class="skill-tags">
        <el-tag v-for="s in job.skills" :key="s" size="default" effect="plain">{{ s }}</el-tag>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getJobDetail } from '@/api/job'
import { apply } from '@/api/application'

const route = useRoute()
const applying = ref(false)

const job = reactive({
  id: route.params.id, jobName: '', department: '', city: '', education: '', experience: '',
  salaryMin: null, salaryMax: null, headcount: null, description: '', requirements: '',
  isUrgent: false, skills: []
})

onMounted(async () => {
  try {
    const res = await getJobDetail(route.params.id)
    const j = res.data
    const skillList = j.skills
      ? String(j.skills).split(/[,，\n]/).map(s => s.trim()).filter(Boolean)
      : []
    Object.assign(job, {
      jobName: j.jobName || '',
      department: j.department || '',
      city: j.city || '',
      education: j.education || '',
      experience: j.experience || '',
      salaryMin: j.salaryMin,
      salaryMax: j.salaryMax,
      headcount: j.headcount,
      description: j.description || '',
      requirements: j.requirements || '',
      skills: skillList
    })
  } catch (e) { /* fallback */ }
})

async function handleApply() {
  applying.value = true
  try {
    await apply({ jobId: job.id })
    ElMessage.success('投递成功')
  } catch (e) { ElMessage.error(e.message || '投递失败') }
  finally { applying.value = false }
}
</script>

<style scoped>
.job-detail-page { max-width: 960px; margin: 0 auto; }
.back-link { margin-bottom: 16px; }

.hero-card { border-radius: 14px; margin-bottom: 16px; }
.hero-top { display: flex; justify-content: space-between; align-items: flex-start; gap: 16px; }
.hero-info { flex: 1; min-width: 0; }
.title-row { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.title-row h1 { margin: 0; font-size: 24px; color: #2d3748; }
.company-name { font-size: 15px; color: #666; margin-bottom: 12px; }
.meta-row { display: flex; align-items: center; gap: 4px; font-size: 13px; color: #999; flex-wrap: wrap; }
.meta-row span { display: flex; align-items: center; gap: 4px; }
.salary-box { text-align: right; flex-shrink: 0; }
.salary { font-size: 26px; font-weight: 800; color: #E76F51; white-space: nowrap; }

.hero-actions { margin-top: 20px; padding-top: 18px; border-top: 1px dashed #f0f0f0; display: flex; gap: 12px; }
.hero-actions .el-button { min-width: 120px; }

.section-card { border-radius: 14px; margin-bottom: 16px; }
.card-title { font-size: 16px; color: #2d3748; margin: 0 0 14px; padding-bottom: 10px; border-bottom: 1px solid #f0f0f0; }
.desc-text { font-size: 14px; line-height: 1.9; color: #555; white-space: pre-wrap; }
.skill-tags { display: flex; flex-wrap: wrap; gap: 10px; }
</style>
