<template>
  <div class="page">
    <div class="page-header">
      <h2>✍️ 面试评价</h2>
      <span>候选人：张三 · Java高级开发工程师</span>
    </div>
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card shadow="never" class="section-card">
          <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
            <el-divider content-position="left">综合评分</el-divider>
            <el-form-item label="总分" prop="totalScore">
              <el-rate v-model="form.totalScore" :max="10" show-score :texts="['差', '较差', '一般', '中等', '良好', '好', '很好', '优秀', '极好', '完美']" show-text />
            </el-form-item>
            <el-divider content-position="left">分项评分</el-divider>
            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item label="技术能力"><el-rate v-model="form.technical" :max="5" /></el-form-item>
                <el-form-item label="问题分析"><el-rate v-model="form.analysis" :max="5" /></el-form-item>
                <el-form-item label="系统设计"><el-rate v-model="form.design" :max="5" /></el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="沟通表达"><el-rate v-model="form.communication" :max="5" /></el-form-item>
                <el-form-item label="学习能力"><el-rate v-model="form.learning" :max="5" /></el-form-item>
                <el-form-item label="团队匹配"><el-rate v-model="form.teamwork" :max="5" /></el-form-item>
              </el-col>
            </el-row>
            <el-divider content-position="left">逐题评价</el-divider>
            <div v-for="(q, i) in form.questionReviews" :key="i" class="q-review">
              <div class="q-title">Q{{ i + 1 }}. {{ q.title }}</div>
              <div class="q-answer"><b>候选人回答：</b>{{ q.answer }}</div>
              <el-form-item label="评分">
                <el-rate v-model="q.score" :max="5" size="small" />
              </el-form-item>
              <el-form-item label="点评">
                <el-input v-model="q.comment" type="textarea" :rows="2" placeholder="对本题回答的点评..." />
              </el-form-item>
            </div>
            <el-divider content-position="left">综合评价</el-divider>
            <el-form-item label="面试总结" prop="summary">
              <el-input v-model="form.summary" type="textarea" :rows="4" placeholder="整体评价候选人的优势、不足和亮点..." />
            </el-form-item>
            <el-form-item label="面试建议" prop="recommendation">
              <el-radio-group v-model="form.recommendation">
                <el-radio-button value="PASS">✅ 建议通过</el-radio-button>
                <el-radio-button value="PENDING">⏸️ 待定</el-radio-button>
                <el-radio-button value="REJECT">❌ 建议不通过</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="large" icon="Check" @click="handleSubmit">提交评价</el-button>
              <el-button size="large" icon="Document" @click="handleDraft">保存草稿</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">📊 面试概览</h3>
          <div class="overview-item"><b>候选人：</b>张三</div>
          <div class="overview-item"><b>面试职位：</b>Java高级开发工程师</div>
          <div class="overview-item"><b>面试时间：</b>2024-07-18 14:00</div>
          <div class="overview-item"><b>面试类型：</b>技术面试</div>
          <el-divider />
          <p style="font-size:13px;color:#999;">评价提交后将同步给HR，用于最终录用决策。</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

const formRef = ref()

const form = reactive({
  totalScore: 4, technical: 4, analysis: 3, design: 3, communication: 4, learning: 4, teamwork: 4,
  summary: '候选人技术基础扎实，对Java生态和微服务架构有深入理解。在支付系统设计方面经验丰富，能清晰描述高并发场景下的解决方案。系统设计部分略有欠缺，对K8s的实际使用经验不够深入。总体表现良好，建议录用。',
  recommendation: 'PASS',
  questionReviews: [
    { title: '介绍支付系统核心模块', answer: '详细描述了分布式事务方案（Saga模式），并展示了幂等设计和对账机制。', score: 4, comment: '回答完整，结合实际项目经验。' },
    { title: '排查大量超时问题', answer: '从监控、日志、链路追踪、数据库、GC逐层排查。', score: 4, comment: '排查思路系统化，考虑全面。' },
    { title: '微服务vs单体架构', answer: '优势说了4点，挑战说了3点。', score: 3, comment: '基本正确，缺少数仓和服务治理方面的讨论。' },
    { title: 'Redis使用场景', answer: '缓存、分布式锁、Session。说了缓存穿透解决方案。', score: 4, comment: '基础扎实，有实战经验。' },
    { title: '系统设计改进', answer: '提出了几点合理改进方向。', score: 3, comment: '方向正确但不够具体，缺少量化依据。' }
  ]
})

const rules = {
  totalScore: [{ required: true, message: '请评分', trigger: 'change' }],
  summary: [{ required: true, message: '请填写面试总结', trigger: 'blur' }],
  recommendation: [{ required: true, message: '请选择面试建议', trigger: 'change' }]
}

function handleSubmit() { ElMessage.success('面试评价已提交！') }
function handleDraft() { ElMessage.info('评价草稿已保存') }
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.page-header span { font-size: 14px; color: #999; }
.section-card { border-radius: 12px; margin-bottom: 16px; }
.card-title { margin: 0 0 12px; font-size: 16px; color: #3E2723; }
.overview-item { font-size: 13px; color: #666; margin-bottom: 8px; }
.q-review { background: #fafafa; border-radius: 10px; padding: 14px; margin-bottom: 12px; }
.q-title { font-weight: 600; color: #3E2723; margin-bottom: 8px; }
.q-answer { font-size: 13px; color: #666; margin-bottom: 10px; line-height: 1.6; }
</style>
