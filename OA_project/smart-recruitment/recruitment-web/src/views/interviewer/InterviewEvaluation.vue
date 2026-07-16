<template>
  <div>
    <h2>面试评价</h2>
    <el-card style="margin-top: 20px;">
      <el-form :model="form" style="max-width: 600px;">
        <el-form-item label="技术评分"><el-rate v-model="form.technicalScore" :max="5" /></el-form-item>
        <el-form-item label="沟通评分"><el-rate v-model="form.communicationScore" :max="5" /></el-form-item>
        <el-form-item label="面试总结"><el-input v-model="form.summary" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="建议">
          <el-radio-group v-model="form.recommendation">
            <el-radio value="PASS">通过</el-radio>
            <el-radio value="REJECT">不通过</el-radio>
            <el-radio value="PENDING">待定</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary">提交评价</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
const form = ref({ technicalScore: 0, communicationScore: 0, summary: '', recommendation: 'PENDING' })
</script>
