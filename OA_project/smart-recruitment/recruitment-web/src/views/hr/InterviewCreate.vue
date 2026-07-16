<template>
  <div>
    <h2>创建面试</h2>
    <el-form :model="form" style="margin-top: 20px; max-width: 600px;">
      <el-form-item label="选择候选人"><el-select v-model="form.candidateId" placeholder="请选择" /></el-form-item>
      <el-form-item label="选择面试官"><el-select v-model="form.interviewerId" placeholder="请选择" /></el-form-item>
      <el-form-item label="面试时间"><el-date-picker v-model="form.interviewTime" type="datetime" /></el-form-item>
      <el-form-item>
        <el-button type="primary">创建</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
const form = ref({ candidateId: '', interviewerId: '', interviewTime: '' })
</script>
