<template>
  <div class="page">
    <div class="page-header"><h2>🤖 AI人岗匹配分析</h2><el-button icon="ArrowLeft" @click="$router.back()" link>返回</el-button></div>
    <!-- TODO: 对接AI匹配接口 getMatchAnalysis(candidateId, jobId) -->
    <el-empty description="TODO: 对接AI匹配分析接口 — 展示匹配度、维度分析、技能Gap" :image-size="100" />
  </div>
</template>

<script setup>
// import { getMatchAnalysis } from '@/api/ai'
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
</style>
