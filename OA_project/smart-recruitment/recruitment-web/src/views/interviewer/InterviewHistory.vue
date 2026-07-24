<template>
  <div class="page">
    <div class="page-header"><h2>面试历史</h2></div>
    <el-card shadow="never" class="section-card">
      <el-table :data="history" v-loading="loading" stripe>
        <el-table-column prop="candidateName" label="候选人" width="100" />
        <el-table-column prop="jobName" label="职位" min-width="150" />
        <el-table-column prop="interviewTime" label="日期" width="160" sortable>
          <template #default="{ row }">{{ formatDateTime(row.interviewTime) }}</template>
        </el-table-column>
        <el-table-column label="评分" width="120" align="center">
          <template #default="{ row }">
            <span v-if="row.evaluation">{{ row.evaluation.overallScore || '-' }}分</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="$router.push(`/interviewer/tasks/${row.id}`)">查看</el-button>
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

const loading = ref(false)
const page = ref(1)
const total = ref(0)
const history = ref([])

async function fetchHistory() {
  loading.value = true
  try {
    const res = await getInterviewerTasks({ page: page.value, size: 10, status: 1 })
    if (res && res.data) {
      history.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (e) {
    ElMessage.error('加载面试历史失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchHistory)
watch(page, fetchHistory)
</script>

<style scoped>
.page-header h2 { margin: 0 0 16px; font-size: 22px; color: #3E2723; }
.section-card { border-radius: 12px; }
</style>
