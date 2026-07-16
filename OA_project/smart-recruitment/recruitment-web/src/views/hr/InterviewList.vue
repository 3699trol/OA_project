<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center;">
      <h2>面试管理</h2>
      <el-button type="primary" @click="$router.push('/hr/interviews/create')">创建面试</el-button>
    </div>
    <el-table :data="[]" style="margin-top: 20px;">
      <el-table-column prop="candidate" label="候选人" />
      <el-table-column prop="interviewer" label="面试官" />
      <el-table-column prop="status" label="状态" />
      <el-table-column prop="interviewTime" label="面试时间" />
    </el-table>
  </div>
</template>
