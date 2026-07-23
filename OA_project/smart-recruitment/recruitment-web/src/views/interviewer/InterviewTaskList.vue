<template>
  <div class="page">
    <div class="page-header">
      <h2>面试任务</h2>
      <el-radio-group v-model="activeTab" size="small">
        <el-radio-button value="all">全部</el-radio-button>
        <el-radio-button value="pending">待面试</el-radio-button>
        <el-radio-button value="completed">已完成</el-radio-button>
      </el-radio-group>
    </div>
    <el-card shadow="never" class="section-card">
      <el-table :data="tasks" v-loading="loading" stripe>
        <el-table-column prop="candidateName" label="候选人" width="100" />
        <el-table-column prop="jobName" label="面试职位" min-width="150" />
        <el-table-column prop="interviewType" label="类型" width="100" />
        <el-table-column prop="interviewTime" label="时间" width="160" sortable>
          <template #default="{ row }">{{ formatDateTime(row.interviewTime) }}</template>
        </el-table-column>
        <el-table-column prop="statusLabel" label="状态" width="90" align="center">
          <template #default="{ row }"><el-tag :type="statusType(row.status)" size="small">{{ row.statusLabel }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="$router.push(`/interviewer/tasks/${row.id}`)">详情</el-button>
            <el-button v-if="row.status === 0" size="small" type="success" link @click="$router.push(`/interviewer/evaluation/${row.id}`)">评价</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" :page-size="10" :total="total" layout="prev, pager, next, total" background style="margin-top:16px;justify-content:center;" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getInterviewerTasks } from '@/api/interview'
import { formatDateTime } from '@/utils/date'

const activeTab = ref('all')
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const tasks = ref([])

const tabStatusMap = { all: undefined, pending: 0, completed: 1 }

function statusType(s) {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[s] || 'info'
}

async function fetchTasks() {
  loading.value = true
  try {
    const params = { page: page.value, size: 10 }
    if (tabStatusMap[activeTab.value] !== undefined) {
      params.status = tabStatusMap[activeTab.value]
    }
    const res = await getInterviewerTasks(params)
    if (res && res.data) {
      tasks.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (e) {
    ElMessage.error('加载面试任务失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchTasks)
watch([page, activeTab], fetchTasks)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.section-card { border-radius: 12px; margin-top: 16px; }
</style>
