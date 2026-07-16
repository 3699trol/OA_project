<template>
  <div>
    <h2>AI生成面试题</h2>
    <el-card style="margin-top: 20px;">
      <el-form :model="form" style="max-width: 600px;">
        <el-form-item label="题目类型">
          <el-select v-model="form.type" placeholder="请选择">
            <el-option label="技术题" value="technical" />
            <el-option label="项目追问" value="project" />
            <el-option label="综合题" value="general" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目数量">
          <el-input-number v-model="form.count" :min="1" :max="10" />
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="form.difficulty" placeholder="请选择">
            <el-option label="简单" value="easy" />
            <el-option label="中等" value="medium" />
            <el-option label="困难" value="hard" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary">AI生成</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
const form = ref({ type: 'technical', count: 5, difficulty: 'medium' })
</script>
