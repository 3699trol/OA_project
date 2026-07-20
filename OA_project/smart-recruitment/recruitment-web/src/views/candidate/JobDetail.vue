<template>
  <div class="page">
    <el-button icon="ArrowLeft" @click="$router.back()" link style="margin-bottom:12px;">返回职位列表</el-button>
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card shadow="never" class="section-card">
          <div class="detail-header">
            <div>
              <div class="title-row"><h2>{{ job.jobName }}</h2><el-tag v-if="job.isUrgent" type="danger" size="small">急聘</el-tag></div>
              <div class="company-name">{{ job.department }}</div>
              <div class="meta-row"><span><el-icon><Location /></el-icon> {{ job.city }}</span><span><el-icon><School /></el-icon> {{ job.education }}</span><span><el-icon><Clock /></el-icon> {{ job.experience }}</span></div>
            </div>
            <div class="salary-box"><div class="salary">{{ job.salaryMin != null && job.salaryMax != null ? (job.salaryMin) + 'K-' + (job.salaryMax) + 'K' : '-' }}</div></div>
          </div>
        </el-card>
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">📋 职位描述</h3>
          <div class="desc-text">{{ job.description || '加载中...' }}</div>
          <h3 class="card-title">🏷️ 技能标签</h3>
          <div class="skill-tags"><el-tag v-for="s in job.skills" :key="s" size="default" effect="plain">{{ s }}</el-tag></div>
        </el-card>
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">🏢 公司信息</h3>
          <p>{{ job.companyIntro || '加载中...' }}</p>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="section-card">
          <h4>🎯 AI匹配分析</h4>
          <!-- TODO: 对接AI匹配接口 getMatchScore(jobId, resumeId) -->
          <el-empty description="TODO: 对接匹配接口" :image-size="60" />
        </el-card>
        <el-card shadow="never" class="section-card">
          <el-button type="primary" size="large" style="width:100%;" @click="handleApply" :loading="applying">立即投递</el-button>
          <el-button size="large" style="width:100%;margin-top:10px;" plain>收藏职位</el-button>
        </el-card>
      </el-col>
    </el-row>
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
  salaryMin: null, salaryMax: null, description: '', requirements: '', isUrgent: false, skills: [], companyIntro: ''
})

function formatSalary(min, max) {
  if (!min && !max) return '薪资面议'
  const k = v => (v ).toFixed(0) + 'K'
  return k(min) + '-' + k(max)
}

onMounted(async () => {
  try {
    const res = await getJobDetail(route.params.id)
    const j = res.data
    Object.assign(job, {
      jobName: j.jobName || '',
      department: j.department || '',
      city: j.city || '',
      education: j.education || '',
      experience: j.experience || '',
      salaryMin: j.salaryMin,
      salaryMax: j.salaryMax,
      description: j.description || '',
      requirements: j.requirements || '',
      skills: j.requirements ? j.requirements.split('\n').filter(Boolean) : []
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
.section-card { border-radius: 12px; margin-bottom: 16px; }
.card-title { font-size: 16px; color: #3E2723; margin: 0 0 14px; padding-bottom: 10px; border-bottom: 1px solid #f0f0f0; }
.detail-header { display: flex; justify-content: space-between; align-items: flex-start; }
.title-row { display: flex; align-items: center; gap: 10px; margin-bottom: 6px; }
.title-row h2 { margin: 0; font-size: 22px; color: #3E2723; }
.company-name { font-size: 15px; color: #666; margin-bottom: 10px; }
.meta-row { display: flex; gap: 20px; font-size: 13px; color: #999; }
.meta-row span { display: flex; align-items: center; gap: 3px; }
.salary-box { text-align: right; }
.salary { font-size: 24px; font-weight: 700; color: #E76F51; }
.desc-text { font-size: 14px; line-height: 1.8; color: #555; white-space: pre-wrap; }
.skill-tags { display: flex; flex-wrap: wrap; gap: 8px; }
</style>
