<template>
  <div class="page">
    <div class="page-header"><h2>📊 简历评估</h2><el-button icon="ArrowLeft" @click="$router.push('/candidate/resume')" link>返回简历</el-button></div>
    <!-- TODO: 对接AI简历评估接口 getResumeEvaluation(resumeId) -->
    <el-empty description="TODO: 对接AI简历评估接口 — 展示评分雷达图、维度分析、优化建议" :image-size="100" />
  </div>
</template>

<script setup>
// import { getResumeEvaluation } from '@/api/resume'
// onMounted: const res = await getResumeEvaluation()
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
</style>
