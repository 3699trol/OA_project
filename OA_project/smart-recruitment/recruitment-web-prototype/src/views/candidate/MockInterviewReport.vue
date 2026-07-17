<template>
  <div class="page">
    <div class="page-header">
      <h2>📋 模拟面试报告</h2>
      <el-button icon="ArrowLeft" @click="$router.push('/candidate/mock-interview')" link>返回模拟面试</el-button>
    </div>
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="never" class="summary-card">
          <div style="text-align:center;">
            <el-progress type="dashboard" :percentage="totalScore" :color="scoreColor" :stroke-width="14" :width="160">
              <template #default><span class="total-score">{{ totalScore }}</span></template>
            </el-progress>
            <h3>综合得分</h3>
            <el-tag :type="totalScore >= 80 ? 'success' : totalScore >= 60 ? 'warning' : 'danger'" size="large" effect="dark">{{ levelText }}</el-tag>
          </div>
          <el-divider />
          <div class="summary-stats">
            <div class="stat"><b>面试日期：</b>2024-07-17</div>
            <div class="stat"><b>面试类型：</b>技术面试</div>
            <div class="stat"><b>目标职位：</b>Java高级开发工程师</div>
            <div class="stat"><b>总题数：</b>5题</div>
            <div class="stat"><b>答题时长：</b>12分30秒</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">📊 逐题分析</h3>
          <div v-for="(q, i) in questionResults" :key="i" class="q-item">
            <div class="q-header">
              <span class="q-num">Q{{ i + 1 }}</span>
              <span class="q-title">{{ q.question }}</span>
              <el-tag :type="q.score >= 80 ? 'success' : q.score >= 60 ? 'warning' : 'danger'" size="small">{{ q.score }}分</el-tag>
            </div>
            <p class="q-answer"><b>您的回答摘要：</b>{{ q.answer }}</p>
            <div class="q-feedback">
              <el-icon><InfoFilled /></el-icon> <b>AI点评：</b>{{ q.feedback }}
            </div>
          </div>
        </el-card>
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">🎯 能力雷达图</h3>
          <div class="radar-placeholder" style="text-align:center;padding:30px;">
            <div v-for="d in radarData" :key="d.label" class="radar-bar">
              <span class="radar-label">{{ d.label }}</span>
              <el-progress :percentage="d.value" :stroke-width="14" :color="d.color" :text-inside="true" style="flex:1;margin:0 12px;" />
              <span class="radar-value">{{ d.value }}%</span>
            </div>
          </div>
        </el-card>
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">💡 改进建议</h3>
          <el-alert v-for="(s, i) in suggestions" :key="i" :title="s.title" :description="s.desc" :type="s.type" show-icon :closable="false" style="margin-bottom:10px;" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const totalScore = 76
const scoreColor = [{ color: '#F56C6C', percentage: 50 }, { color: '#E6A23C', percentage: 75 }, { color: '#67C23A', percentage: 100 }]

const levelText = computed(() => totalScore >= 80 ? '表现优秀' : totalScore >= 60 ? '表现良好' : '需要加强')
const radarData = [
  { label: '技术基础', value: 85, color: '#67C23A' },
  { label: '问题分析', value: 72, color: '#E6A23C' },
  { label: '表达沟通', value: 78, color: '#409EFF' },
  { label: '系统设计', value: 65, color: '#E6A23C' },
  { label: '实战经验', value: 80, color: '#67C23A' }
]

const questionResults = [
  { question: '请简单介绍一下你自己', answer: '介绍了教育背景和两段工作经历，但对核心技能描述不够突出。', score: 78, feedback: '建议用30秒电梯演讲模式——用一句话概括你的核心价值。' },
  { question: 'Spring Boot自动配置原理', answer: '基本说清了@SpringBootApplication和自动配置机制。', score: 82, feedback: '回答较完整，可以补充Conditional注解的实际应用场景。' },
  { question: '微服务拆分遇到的挑战', answer: '提到了数据一致性问题，但没有给出具体的解决方案。', score: 70, feedback: '建议结合CAP定理说明分布式事务的处理策略。' },
  { question: '高并发系统排查思路', answer: '从监控-日志-数据库-缓存层层排查的思路正确。', score: 80, feedback: '排查思路清晰，建议补充具体工具（Arthas/Grafana/Prometheus）。' },
  { question: '秒杀系统设计', answer: '提到了库存扣减、消息队列削峰，但对限流和防刷考虑不全。', score: 68, feedback: '需要补充：令牌桶限流、验证码防刷、前端限流+后端校验多层防护。' }
]

const suggestions = [
  { title: '加强系统设计训练', desc: '建议多练习秒杀系统、IM系统、推荐系统等常见的设计题，形成结构化答题框架。', type: 'warning' },
  { title: '补充量化表达', desc: '在描述项目经历时，用具体数据代替模糊描述，如QPS、RT、用户量等。', type: 'info' },
  { title: '预习面试公司技术栈', desc: '面试前了解目标公司的技术栈和业务特点，针对性准备相关知识点。', type: '' }
]
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.summary-card { border-radius: 12px; }
.total-score { font-size: 36px; font-weight: 700; color: #ED8936; }
.summary-stats { font-size: 14px; color: #666; }
.summary-stats .stat { margin-bottom: 8px; }
.section-card { border-radius: 12px; margin-bottom: 16px; }
.card-title { margin: 0 0 16px; font-size: 16px; color: #3E2723; padding-bottom: 10px; border-bottom: 1px solid #f0f0f0; }
.q-item { padding: 14px; margin-bottom: 10px; background: #fafafa; border-radius: 10px; }
.q-header { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
.q-num { width: 28px; height: 28px; border-radius: 50%; background: #ED8936; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 700; flex-shrink: 0; }
.q-title { flex: 1; font-weight: 600; color: #3E2723; font-size: 14px; }
.q-answer { font-size: 13px; color: #666; margin: 0 0 8px; line-height: 1.6; }
.q-feedback { font-size: 13px; color: #409EFF; background: #ecf5ff; padding: 8px 12px; border-radius: 8px; display: flex; align-items: flex-start; gap: 6px; }
.radar-bar { display: flex; align-items: center; margin-bottom: 12px; }
.radar-label { width: 80px; font-size: 13px; color: #666; text-align: right; }
.radar-value { width: 36px; font-size: 13px; color: #999; text-align: left; }
</style>
