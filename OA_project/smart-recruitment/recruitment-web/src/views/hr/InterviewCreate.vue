<template>
  <div class="page">
    <div class="page-header"><h2>创建面试</h2><el-button icon="ArrowLeft" @click="$router.back()" link>返回</el-button></div>
    <el-card shadow="never" class="section-card" style="max-width:700px;">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="候选人" prop="candidateId">
          <el-select v-model="form.candidateId" placeholder="选择候选人" style="width:100%;" filterable>
            <!-- TODO: load candidates from API -->
          </el-select>
        </el-form-item>
        <el-form-item label="面试官" prop="interviewerId">
          <el-select v-model="form.interviewerId" placeholder="选择面试官" style="width:100%;">
            <!-- TODO: load interviewers from API -->
          </el-select>
        </el-form-item>
        <el-form-item label="面试类型"><el-radio-group v-model="form.type"><el-radio-button value="技术面试">技术</el-radio-button><el-radio-button value="HR面试">HR</el-radio-button><el-radio-button value="综合面试">综合</el-radio-button></el-radio-group></el-form-item>
        <el-form-item label="面试方式"><el-radio-group v-model="form.mode"><el-radio-button value="线上">线上</el-radio-button><el-radio-button value="线下">线下</el-radio-button></el-radio-group></el-form-item>
        <el-form-item label="面试时间" prop="interviewTime"><el-date-picker v-model="form.interviewTime" type="datetime" value-format="YYYY-MM-DD HH:mm" style="width:100%;" /></el-form-item>
        <el-form-item v-if="form.mode === '线下'" label="地点"><el-input v-model="form.location" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="3" /></el-form-item>
        <el-form-item><el-button type="primary" size="large" @click="handleCreate">创建</el-button></el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
// import { createInterview } from '@/api/interview'

const formRef = ref()
const form = reactive({ candidateId: null, interviewerId: null, type: '技术面试', mode: '线上', interviewTime: '', location: '', remark: '' })
const rules = { candidateId: [{ required: true, message: '请选择', trigger: 'change' }], interviewerId: [{ required: true, message: '请选择', trigger: 'change' }], interviewTime: [{ required: true, message: '请选择', trigger: 'change' }] }

async function handleCreate() {
  await formRef.value.validate().catch(() => {})
  // TODO: await createInterview(form)
  ElMessage.info('TODO: 对接面试创建接口')
}
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.section-card { border-radius: 12px; margin-top: 16px; }
</style>
