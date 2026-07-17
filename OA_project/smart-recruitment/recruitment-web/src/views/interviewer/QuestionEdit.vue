<template>
  <div class="page">
    <div class="page-header"><h2>✏️ 编辑面试题</h2><el-button icon="ArrowLeft" @click="$router.back()" link>返回</el-button></div>
    <el-card shadow="never" class="section-card" style="max-width:800px;">
      <div v-for="(q, i) in questions" :key="i" class="q-edit-item">
        <div class="q-edit-header">
          <span class="q-edit-num">Q{{ i + 1 }}</span>
          <el-input v-model="q.title" style="flex:1;" />
          <el-select v-model="q.difficulty" size="small" style="width:100px;"><el-option label="简单" value="简单" /><el-option label="中等" value="中等" /><el-option label="困难" value="困难" /></el-select>
          <el-button size="small" type="danger" plain circle icon="Delete" @click="removeQ(i)" />
        </div>
        <el-form-item label="参考答案"><el-input v-model="q.referenceAnswer" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="评分标准"><el-input v-model="q.criteria" /></el-form-item>
      </div>
      <el-button icon="Plus" style="width:100%;" @click="addQ">添加题目</el-button>
      <div class="form-actions" style="margin-top:20px;">
        <el-button type="primary" size="large" @click="handleSave">保存</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
// import { saveQuestions, getInterviewQuestions } from '@/api/interview'

const questions = reactive([{ title: '', difficulty: '中等', referenceAnswer: '', criteria: '' }])

function addQ() { questions.push({ title: '新题目', difficulty: '中等', referenceAnswer: '', criteria: '' }) }
function removeQ(i) { if (questions.length > 1) questions.splice(i, 1); else ElMessage.warning('至少保留一道') }
async function handleSave() { /* TODO: await saveQuestions({ interviewId:..., questions }) */ ElMessage.info('TODO: 对接题目保存接口') }
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.section-card { border-radius: 12px; margin-top: 16px; }
.q-edit-item { padding: 16px; margin-bottom: 12px; background: #fafafa; border-radius: 10px; border-left: 4px solid #409EFF; }
.q-edit-header { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
.q-edit-num { min-width: 28px; height: 28px; border-radius: 50%; background: #409EFF; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 700; }
</style>
