<template>
  <div>
    <h2>编辑简历</h2>
    <el-form :model="form" style="margin-top: 20px; max-width: 600px;">
      <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
      <el-form-item label="电话"><el-input v-model="form.phone" /></el-form-item>
      <el-form-item label="学历"><el-input v-model="form.education" /></el-form-item>
      <el-form-item label="技能"><el-input v-model="form.skills" type="textarea" :rows="3" /></el-form-item>
      <el-form-item>
        <el-button type="primary">保存</el-button>
        <el-button type="success">AI解析简历</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
const form = ref({ name: '', email: '', phone: '', education: '', skills: '' })
</script>
