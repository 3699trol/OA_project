<template>
  <div class="page">
    <div class="page-header">
      <h2>🤖 AI人岗匹配分析</h2>
      <el-button icon="ArrowLeft" @click="$router.back()" link>返回</el-button>
    </div>
    <el-row :gutter="20">
      <!-- 左侧：候选人 -->
      <el-col :span="6">
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">👤 候选人</h3>
          <div class="candidate-brief">
            <el-avatar :size="56" style="background:#ED8936;">张</el-avatar>
            <div>
              <h4>张三</h4>
              <p>Java高级开发工程师</p>
              <el-tag type="success" size="small" effect="plain">92% 匹配</el-tag>
            </div>
          </div>
          <el-divider />
          <div class="skill-list">
            <div v-for="s in candidateSkills" :key="s.name" class="skill-row">
              <span class="skill-name">{{ s.name }}</span>
              <el-progress :percentage="s.level" :stroke-width="6" :show-text="false" :color="s.level >= 80 ? '#67C23A' : '#E6A23C'" style="width:100px;" />
            </div>
          </div>
        </el-card>
      </el-col>
      <!-- 中间 -->
      <el-col :span="12">
        <el-card shadow="never" class="section-card">
          <h3 class="card-title" style="text-align:center;">📊 匹配分析报告</h3>
          <div class="match-overall">
            <el-progress type="dashboard" :percentage="92" :stroke-width="14" :width="150" :color="[{color:'#67C23A',percentage:80},{color:'#ED8936',percentage:100}]">
              <template #default><span class="overall-score">92%</span></template>
            </el-progress>
            <div class="overall-text">
              <h4>高度匹配</h4>
              <p>该候选人与职位需求高度吻合，建议优先安排面试。</p>
            </div>
          </div>
          <el-divider />
          <div class="match-detail">
            <div v-for="d in dimensionResults" :key="d.label" class="dim-row">
              <div class="dim-header">
                <span class="dim-label">{{ d.icon }} {{ d.label }}</span>
                <span class="dim-score" :style="{ color: d.score >= 85 ? '#67C23A' : '#E6A23C' }">{{ d.score }}%</span>
              </div>
              <p class="dim-comment">{{ d.comment }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <!-- 右侧：职位 -->
      <el-col :span="6">
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">💼 目标职位</h3>
          <div class="job-brief">
            <h4>Java高级开发工程师</h4>
            <p>某科技有限公司</p>
            <p style="color:#E76F51;font-weight:700;">15K-25K</p>
          </div>
          <el-divider />
          <div class="skill-list">
            <div v-for="s in jobRequirements" :key="s.name" class="skill-row">
              <span class="skill-name">{{ s.name }}</span>
              <el-tag :type="s.required ? 'danger' : 'info'" size="small">{{ s.required ? '必须' : '加分' }}</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
const candidateSkills = [
  { name: 'Java', level: 95 }, { name: 'Spring Boot', level: 90 }, { name: '微服务', level: 85 },
  { name: 'MySQL', level: 88 }, { name: 'Redis', level: 82 }, { name: 'Docker', level: 75 },
  { name: 'K8s', level: 60 }, { name: 'Linux', level: 80 }
]

const jobRequirements = [
  { name: 'Java', required: true }, { name: 'Spring Boot', required: true }, { name: '微服务', required: true },
  { name: 'MySQL', required: true }, { name: 'Redis', required: true }, { name: 'Docker', required: false },
  { name: 'K8s', required: false }, { name: 'Elasticsearch', required: false }
]

const dimensionResults = [
  { icon: '🛠️', label: '技术栈匹配', score: 95, comment: 'Java、Spring Boot、微服务等核心技术栈100%覆盖，K8s略有不足但不影响核心要求。' },
  { icon: '📊', label: '经验年限匹配', score: 88, comment: '候选人5年经验，职位要求3-5年，经验在目标范围内且略有富余。' },
  { icon: '🎓', label: '教育背景匹配', score: 100, comment: '本科学历完全符合，学校背景良好。' },
  { icon: '💡', label: '项目经验匹配', score: 85, comment: '有支付系统高并发项目经验，与公司后台系统架构需求相关。' },
  { icon: '📍', label: '地点意愿匹配', score: 100, comment: '候选人现居北京，期望工作地点一致。' },
  { icon: '💰', label: '薪资期望匹配', score: 82, comment: '候选人期望在职位薪资范围内，略有议价空间。' }
]
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.section-card { border-radius: 12px; }
.card-title { margin: 0 0 14px; font-size: 16px; color: #3E2723; padding-bottom: 10px; border-bottom: 1px solid #f0f0f0; }
.candidate-brief { display: flex; align-items: center; gap: 12px; }
.candidate-brief h4 { margin: 0 0 2px; font-size: 15px; }
.candidate-brief p { margin: 0 0 6px; font-size: 13px; color: #999; }
.skill-list { display: flex; flex-direction: column; gap: 10px; }
.skill-row { display: flex; justify-content: space-between; align-items: center; }
.skill-name { font-size: 13px; color: #666; width: 80px; }
.match-overall { display: flex; align-items: center; gap: 24px; justify-content: center; }
.overall-score { font-size: 32px; font-weight: 700; color: #ED8936; }
.overall-text h4 { margin: 0 0 6px; color: #3E2723; font-size: 17px; }
.overall-text p { margin: 0; font-size: 13px; color: #666; line-height: 1.6; }
.match-detail { display: flex; flex-direction: column; gap: 14px; }
.dim-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.dim-label { font-size: 14px; color: #3E2723; font-weight: 500; }
.dim-score { font-size: 20px; font-weight: 700; }
.dim-comment { font-size: 13px; color: #777; line-height: 1.6; margin: 0; }
.job-brief h4 { margin: 0 0 4px; font-size: 15px; color: #3E2723; }
.job-brief p { margin: 0 0 4px; font-size: 13px; color: #999; }
</style>
