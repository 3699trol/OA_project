<template>
  <div class="page">
    <div class="page-header">
      <h2>📊 简历评估报告</h2>
      <el-button icon="ArrowLeft" @click="$router.push('/candidate/resume')" link>返回简历编辑</el-button>
    </div>
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="never" class="score-card">
          <div class="score-circle">
            <el-progress type="dashboard" :percentage="overallScore" :color="scoreColors" :stroke-width="14" :width="160">
              <template #default><span class="big-score">{{ overallScore }}</span></template>
            </el-progress>
          </div>
          <h3 style="text-align:center;">综合评分</h3>
          <el-divider />
          <div v-for="dim in dimensions" :key="dim.label" class="dim-item">
            <div class="dim-label"><span>{{ dim.icon }}</span> <span>{{ dim.label }}</span> <span class="dim-val">{{ dim.score }}/100</span></div>
            <el-progress :percentage="dim.score" :stroke-width="6" :show-text="false" :color="dim.color" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">📝 详细评估</h3>
          <el-timeline>
            <el-timeline-item v-for="(item, i) in evaluations" :key="i" :timestamp="item.category" placement="top" :color="item.color">
              <el-card shadow="never" class="eval-card">
                <h4>{{ item.title }}</h4>
                <p>{{ item.content }}</p>
                <el-tag :type="item.level === '优秀' ? 'success' : item.level === '良好' ? 'primary' : 'warning'">{{ item.level }}</el-tag>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </el-card>
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">💡 优化建议</h3>
          <el-alert v-for="(tip, i) in suggestions" :key="i" :title="tip.title" :description="tip.desc" :type="tip.type" show-icon :closable="false" style="margin-bottom:10px;" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
const overallScore = 78
const scoreColors = [{ color: '#F56C6C', percentage: 50 }, { color: '#E6A23C', percentage: 75 }, { color: '#67C23A', percentage: 100 }]

const dimensions = [
  { icon: '📋', label: '内容完整度', score: 85, color: '#67C23A' },
  { icon: '💼', label: '经历匹配度', score: 72, color: '#E6A23C' },
  { icon: '🛠️', label: '技能竞争力', score: 80, color: '#409EFF' },
  { icon: '📖', label: '教育背景', score: 75, color: '#409EFF' },
  { icon: '✍️', label: '表达清晰度', score: 78, color: '#E6A23C' }
]

const evaluations = [
  { category: '基本信息', title: '个人信息完整', content: '姓名、联系方式、教育背景等基本信息齐全，但缺少个人作品链接和GitHub主页。', level: '良好', color: '#409EFF' },
  { category: '工作经历', title: '经历描述需量化', content: '工作经历描述了职责，但缺少量化成果。建议使用"提升系统性能30%"、"月活用户增长10万+"等具体数据。', level: '一般', color: '#E6A23C' },
  { category: '技能标签', title: '技术栈符合市场需求', content: 'Java/Spring Cloud/Redis等技能标签覆盖了当前热门的后端技术栈，建议补充Docker/K8s等云原生技能。', level: '优秀', color: '#67C23A' },
  { category: '自我评价', title: '自我评价偏通用', content: '当前自我评价过于通用，建议结合具体项目和成果来突出个人特色。', level: '一般', color: '#E6A23C' }
]

const suggestions = [
  { title: '量化工作成果', desc: '在每个工作经历中至少添加1-2个量化指标，如优化性能百分比、负责的系统日活用户量、团队规模等。', type: 'warning' },
  { title: '突出核心技能', desc: '将最擅长的3-5项技能放在显眼位置，并标注熟练程度（精通/熟练/了解）。', type: 'info' },
  { title: '添加项目亮点', desc: '建议添加一个项目经验板块，描述1-2个最具代表性的项目，突出技术挑战和解决方案。', type: '' }
]
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.score-card { border-radius: 12px; margin-bottom: 16px; }
.score-circle { text-align: center; }
.big-score { font-size: 36px; font-weight: 700; color: #ED8936; }
.dim-item { margin-bottom: 14px; }
.dim-label { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; font-size: 13px; }
.dim-val { color: #999; }
.section-card { border-radius: 12px; margin-bottom: 16px; }
.card-title { margin: 0 0 16px; font-size: 16px; color: #3E2723; padding-bottom: 10px; border-bottom: 1px solid #f0f0f0; }
.eval-card { border-radius: 10px; background: #fafafa; border: none; }
.eval-card h4 { margin: 0 0 8px; color: #3E2723; font-size: 15px; }
.eval-card p { margin: 0 0 10px; color: #666; font-size: 13px; line-height: 1.6; }
</style>
