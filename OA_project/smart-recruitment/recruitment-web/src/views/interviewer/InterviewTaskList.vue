<template>
  <div class="page">
    <div class="page-header">
      <h2>📋 面试任务</h2>
      <el-radio-group v-model="filter" size="small"><el-radio-button value="all">全部</el-radio-button><el-radio-button value="pending">待面试</el-radio-button><el-radio-button value="completed">已完成</el-radio-button></el-radio-group>
    </div>
    <el-card shadow="never" class="section-card">
      <el-table :data="tasks" v-loading="loading" stripe>
        <el-table-column prop="candidateName" label="候选人" width="80" />
        <el-table-column prop="jobTitle" label="面试职位" min-width="150" />
        <el-table-column prop="type" label="类型" width="100" />
        <el-table-column prop="time" label="时间" width="150" sortable />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }"><el-tag :type="row.status === '待面试' ? 'warning' : 'success'" size="small">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="$router.push(`/interviewer/tasks/${row.id}`)">详情</el-button>
            <el-button size="small" type="success" link @click="$router.push(`/interviewer/evaluation/${row.id}`)">评价</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
// import { getInterviewTasks } from '@/api/interview'

const filter = ref('all'); const loading = ref(false); const tasks = ref([])
onMounted(async () => { /* TODO: const res = await getInterviewTasks({ status: filter.value }) */ })
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.section-card { border-radius: 12px; margin-top: 16px; }
</style>
