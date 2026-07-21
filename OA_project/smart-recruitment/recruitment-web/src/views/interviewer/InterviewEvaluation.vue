<template>
  <div class="page">
    <el-button icon="ArrowLeft" @click="$router.back()" link style="margin-bottom:12px;">返回面试详情</el-button>
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">面试信息</h3>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="候选人">{{ interview.candidateName }}</el-descriptions-item>
            <el-descriptions-item label="面试职位">{{ interview.jobName }}</el-descriptions-item>
            <el-descriptions-item label="面试类型">{{ interview.interviewType }}</el-descriptions-item>
            <el-descriptions-item label="面试时间">{{ interview.interviewTime }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card shadow="never" class="section-card">
          <h3 class="card-title">面试评价</h3>
          <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
            <el-form-item label="技术评分" prop="technicalScore">
              <el-input-number v-model="form.technicalScore" :min="0" :max="100" :step="5" />
              <span class="score-tip">（0-100分）</span>
            </el-form-item>
            <el-form-item label="沟通评分" prop="communicationScore">
              <el-input-number v-model="form.communicationScore" :min="0" :max="100" :step="5" />
              <span class="score-tip">（0-100分）</span>
            </el-form-item>
            <el-form-item label="逻辑评分" prop="logicScore">
              <el-input-number v-model="form.logicScore" :min="0" :max="100" :step="5" />
              <span class="score-tip">（0-100分）</span>
            </el-form-item>
            <el-form-item label="综合评分" prop="overallScore">
              <el-input-number v-model="form.overallScore" :min="0" :max="100" :step="5" />
              <span class="score-tip">（0-100分）</span>
            </el-form-item>
            <el-form-item label="面试结果" prop="result">
              <el-radio-group v-model="form.result">
                <el-radio :value="2">通过</el-radio>
                <el-radio :value="1">待定</el-radio>
                <el-radio :value="0">淘汰</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="评价内容" prop="evaluation">
              <el-input v-model="form.evaluation" type="textarea" :rows="6" placeholder="请输入面试评价内容..." />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSubmit" :loading="submitting">提交评价</el-button>
              <el-button @click="$router.back()">取消</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">已有评价</h3>
          <div v-if="existingEvaluation">
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="技术评分">{{ existingEvaluation.technicalScore }}分</el-descriptions-item>
              <el-descriptions-item label="沟通评分">{{ existingEvaluation.communicationScore }}分</el-descriptions-item>
              <el-descriptions-item label="逻辑评分">{{ existingEvaluation.logicScore }}分</el-descriptions-item>
              <el-descriptions-item label="综合评分">{{ existingEvaluation.overallScore }}分</el-descriptions-item>
              <el-descriptions-item label="面试结果">
                <el-tag :type="existingEvaluation.result === 2 ? 'success' : existingEvaluation.result === 1 ? 'warning' : 'danger'">
                  {{ existingEvaluation.resultLabel }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
            <div v-if="existingEvaluation.evaluation" style="margin-top:16px;">
              <h4>评价内容</h4>
              <p style="color:#666;line-height:1.8;white-space:pre-wrap;">{{ existingEvaluation.evaluation }}</p>
            </div>
          </div>
          <el-empty v-else description="暂无评价" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getInterviewDetail, saveEvaluation, getEvaluationByInterview } from '@/api/interview'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)

const interview = reactive({
  candidateName: '',
  jobName: '',
  interviewType: '',
  interviewTime: ''
})

const form = reactive({
  technicalScore: 80,
  communicationScore: 80,
  logicScore: 80,
  overallScore: 80,
  result: 1,
  evaluation: ''
})

const rules = {
  technicalScore: [{ required: true, message: '请输入技术评分', trigger: 'blur' }],
  communicationScore: [{ required: true, message: '请输入沟通评分', trigger: 'blur' }],
  logicScore: [{ required: true, message: '请输入逻辑评分', trigger: 'blur' }],
  overallScore: [{ required: true, message: '请输入综合评分', trigger: 'blur' }],
  result: [{ required: true, message: '请选择面试结果', trigger: 'change' }]
}

const existingEvaluation = ref(null)

async function loadInterview() {
  try {
    const res = await getInterviewDetail(route.params.id)
    if (res && res.data) {
      Object.assign(interview, res.data)
    }
  } catch (e) {
    ElMessage.error('加载面试信息失败')
  }
}

async function loadEvaluation() {
  try {
    const res = await getEvaluationByInterview(route.params.id)
    if (res && res.data) {
      existingEvaluation.value = res.data
      // 填充已有评价到表单
      if (res.data.technicalScore !== null && res.data.technicalScore !== undefined) form.technicalScore = res.data.technicalScore
      if (res.data.communicationScore !== null && res.data.communicationScore !== undefined) form.communicationScore = res.data.communicationScore
      if (res.data.logicScore !== null && res.data.logicScore !== undefined) form.logicScore = res.data.logicScore
      if (res.data.overallScore !== null && res.data.overallScore !== undefined) form.overallScore = res.data.overallScore
      if (res.data.result !== null && res.data.result !== undefined) form.result = res.data.result
      if (res.data.evaluation) form.evaluation = res.data.evaluation
    }
  } catch (e) {
    // 评价不存在时不报错
  }
}

async function handleSubmit() {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }

  submitting.value = true
  try {
    await saveEvaluation({
      interviewId: route.params.id,
      technicalScore: form.technicalScore,
      communicationScore: form.communicationScore,
      logicScore: form.logicScore,
      overallScore: form.overallScore,
      result: form.result,
      evaluation: form.evaluation
    })
    ElMessage.success('评价提交成功')
    router.back()
  } catch (e) {
    ElMessage.error('提交评价失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadInterview()
  loadEvaluation()
})
</script>

<style scoped>
.section-card { border-radius: 12px; margin-bottom: 16px; }
.card-title { margin: 0 0 14px; font-size: 16px; color: #3E2723; padding-bottom: 10px; border-bottom: 1px solid #f0f0f0; }
.score-tip { margin-left: 8px; font-size: 12px; color: #999; }
</style>
