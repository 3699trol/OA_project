<template>
  <div class="page">
    <div class="page-header"><h2>📋 面试报告</h2><el-button icon="ArrowLeft" @click="$router.push('/candidate/mock-interview')" link>返回</el-button></div>
    <!-- TODO: 对接AI面试报告接口 getMockInterviewReport(interviewId) -->
    <el-empty description="TODO: 对接AI面试报告接口 — 展示得分、逐题分析、能力雷达图、改进建议" :image-size="100" />
  </div>
</template>

<script setup>
// import { getMockInterviewReport } from '@/api/ai'
// onMounted: const res = await getMockInterviewReport(route.params.id)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
</style>
