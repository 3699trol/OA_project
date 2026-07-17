<template>
  <div class="page">
    <div class="page-header"><h2>✍️ 面试评价</h2></div>
    <el-card shadow="never" class="section-card" style="max-width:800px;">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="总分" prop="totalScore"><el-rate v-model="form.totalScore" :max="10" show-score show-text /></el-form-item>
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
        <el-form-item label="面试总结" prop="summary"><el-input v-model="form.summary" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="建议" prop="recommendation">
          <el-radio-group v-model="form.recommendation"><el-radio-button value="PASS">通过</el-radio-button><el-radio-button value="PENDING">待定</el-radio-button><el-radio-button value="REJECT">不通过</el-radio-button></el-radio-group>
        </el-form-item>
        <el-form-item><el-button type="primary" size="large" @click="handleSubmit">提交评价</el-button></el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
// import { submitEvaluation } from '@/api/interview'
// import { useRoute } from 'vue-router'

const formRef = ref()
const form = reactive({ totalScore: 0, technical: 0, analysis: 0, design: 0, communication: 0, learning: 0, teamwork: 0, summary: '', recommendation: 'PENDING' })
const rules = { totalScore: [{ required: true, message: '请评分', trigger: 'change' }], summary: [{ required: true, message: '请填写总结', trigger: 'blur' }] }

async function handleSubmit() {
  await formRef.value.validate().catch(() => {})
  // TODO: await submitEvaluation({ interviewId: route.params.id, ...form })
  ElMessage.info('TODO: 对接面试评价提交接口')
}
</script>

<style scoped>
.page-header h2 { margin: 0 0 16px; font-size: 22px; color: #3E2723; }
.section-card { border-radius: 12px; }
</style>
