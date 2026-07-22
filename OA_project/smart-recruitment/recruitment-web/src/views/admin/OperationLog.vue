<template>
  <div class="page">
    <div class="page-header"><h2>📋 操作日志</h2></div>
    <el-card shadow="never" class="section-card">
      <div class="toolbar">
        <el-input v-model="search" placeholder="搜索" prefix-icon="Search" clearable style="width:240px;" />
        <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD" range-separator="至" start-placeholder="开始" end-placeholder="结束" style="margin-left:12px;" />
        <el-button type="primary" icon="Search" style="margin-left:12px;" @click="handleSearch">查询</el-button>
      </div>
      <el-table :data="logs" v-loading="loading" stripe style="margin-top:16px;">
        <el-table-column prop="operatorName" label="操作人" width="110" />
        <el-table-column prop="module" label="模块" width="100" />
        <el-table-column prop="description" label="描述" min-width="240" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP" width="140" />
        <el-table-column prop="costTime" label="耗时(ms)" width="90" align="center" />
        <el-table-column prop="createTime" label="时间" width="150" sortable>
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" :page-size="10" :total="total" layout="prev, pager, next, total" background style="margin-top:16px;justify-content:center;" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import dayjs from 'dayjs'
import { getOperationLogs } from '@/api/user'

const search = ref(''); const dateRange = ref([]); const loading = ref(false); const page = ref(1); const total = ref(0); const logs = ref([])

async function fetchLogs() {
  loading.value = true
  try {
    const params = { page: page.value, size: 10, keyword: search.value }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }
    const res = await getOperationLogs(params)
    logs.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) { /* fallback */ }
  finally { loading.value = false }
}

function handleSearch() {
  if (page.value === 1) fetchLogs()
  else page.value = 1
}

function formatDateTime(value) {
  if (!value) return '-'
  const date = dayjs(value)
  return date.isValid() ? date.format('YYYY-MM-DD HH:mm') : value
}

onMounted(fetchLogs)
watch([page, dateRange], fetchLogs)
</script>

<style scoped>
.page-header h2 { margin: 0 0 16px; font-size: 22px; color: #3E2723; }
.section-card { border-radius: 12px; }
.toolbar { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; }
</style>
