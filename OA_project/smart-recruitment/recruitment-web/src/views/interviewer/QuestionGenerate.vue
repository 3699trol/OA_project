<template>
  <div class="page">
    <div class="page-header"><h2>🤖 AI生成面试题</h2><el-button icon="ArrowLeft" @click="$router.back()" link>返回</el-button></div>
    <el-card shadow="never" class="section-card" style="max-width:800px;">
      <el-form :model="config" label-width="100px">
        <el-form-item label="目标职位"><el-select v-model="config.jobId" style="width:100%;"><!-- TODO: load jobs --></el-select></el-form-item>
        <el-row :gutter="20">
          <el-col :span="8"><el-form-item label="类型"><el-select v-model="config.type" style="width:100%;"><el-option label="基础" value="基础" /><el-option label="项目" value="项目" /><el-option label="设计" value="设计" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="难度"><el-select v-model="config.difficulty" style="width:100%;"><el-option label="简单" value="简单" /><el-option label="中等" value="中等" /><el-option label="困难" value="困难" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="数量"><el-input-number v-model="config.count" :min="1" :max="20" /></el-form-item></el-col>
        </el-row>
        <el-form-item><el-button type="primary" size="large" icon="MagicStick" @click="generate" :loading="loading">AI生成</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card v-if="questions.length" shadow="never" class="section-card">
      <h3 class="card-title">生成的题目 ({{ questions.length }})</h3>
      <div v-for="(q, i) in questions" :key="i" class="q-item">
        <div class="q-header"><span class="q-num">Q{{ i + 1 }}</span><span>{{ q.title }}</span><el-tag size="small">{{ q.difficulty }}</el-tag></div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
// import { generateQuestions } from '@/api/interview'

const loading = ref(false); const questions = ref([])
const config = reactive({ jobId: null, type: '基础', difficulty: '中等', count: 5 })

async function generate() {
  loading.value = true
  try { /* TODO: const res = await generateQuestions(config); questions.value = res.data */ ElMessage.info('TODO: 对接AI出题接口') }
  catch (e) { ElMessage.error(e.message) }
  finally { loading.value = false }
}
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.section-card { border-radius: 12px; margin-top: 16px; }
.card-title { margin: 0 0 14px; font-size: 16px; color: #3E2723; padding-bottom: 10px; border-bottom: 1px solid #f0f0f0; }
.q-item { padding: 12px; margin-bottom: 8px; background: #fafafa; border-radius: 8px; border-left: 4px solid #409EFF; }
.q-header { display: flex; align-items: center; gap: 8px; }
.q-num { min-width: 26px; height: 26px; border-radius: 50%; background: #409EFF; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 700; }
</style>
