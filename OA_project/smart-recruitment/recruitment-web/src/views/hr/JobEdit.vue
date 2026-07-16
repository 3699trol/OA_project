<template>
  <div>
    <h2>编辑职位</h2>
    <el-form :model="form" style="margin-top: 20px; max-width: 600px;">
      <el-form-item label="职位名称"><el-input v-model="form.title" /></el-form-item>
      <el-form-item label="职位描述"><el-input v-model="form.description" type="textarea" :rows="4" /></el-form-item>
      <el-form-item label="招聘人数"><el-input-number v-model="form.headcount" :min="1" /></el-form-item>
      <el-form-item>
        <el-button type="primary">保存</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
const form = ref({ title: '', description: '', headcount: 1 })
</script>
