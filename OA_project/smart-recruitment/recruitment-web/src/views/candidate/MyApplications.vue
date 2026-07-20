<template>
  <div class="page">
    <div class="page-header">
      <h2>📋 我的投递</h2>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="全部" name="all" /><el-tab-pane label="初筛中" name="screening" />
        <el-tab-pane label="面试中" name="interviewing" /><el-tab-pane label="已录用" name="hired" />
      </el-tabs>
    </div>
    <el-table :data="applications" style="width:100%;" v-loading="loading" stripe>
      <el-table-column prop="jobTitle" label="投递职位" min-width="160" />
      <el-table-column prop="company" label="公司" min-width="140" />
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }"><el-tag :type="statusType(row.status)" effect="plain" round>{{ row.status }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="createTime" label="投递时间" width="130" sortable />
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button size="small" @click="showDetail(row)">查看进度</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="detailVisible" title="投递进度" width="500px">
      <el-steps :active="detailStep" align-center finish-status="success">
        <el-step title="投递" /><el-step title="初筛" /><el-step title="面试" /><el-step title="结果" />
      </el-steps>
      <!-- TODO: 对接投递状态日志接口 getApplicationLogs(applicationId) -->
      <template #footer><el-button type="primary" @click="detailVisible = false">关闭</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { getApplications } from '@/api/application'

const activeTab = ref('all'); const loading = ref(false); const detailVisible = ref(false); const detailStep = ref(0)
const applications = ref([])

function statusType(s) { return { '初筛中': 'warning', '面试中': 'primary', '已录用': 'success', '不合适': 'info' }[s] || '' }
function showDetail(row) { detailStep.value = { '初筛中': 1, '面试中': 2, '已录用': 3 }[row.status] || 1; detailVisible.value = true }

const statusMap = { all: undefined, screening: 0, interviewing: 1, hired: 2 }

async function fetchApplications() {
  loading.value = true
  try {
    const res = await getApplications({ page: 1, size: 50, status: statusMap[activeTab.value] })
    applications.value = (res.data.records || []).map(a => {
      let statusLabel = '初筛中'
      if (a.status === 1) statusLabel = '面试中'
      else if (a.status === 2) statusLabel = '已录用'
      else if (a.status === 3) statusLabel = '不合适'
      return { ...a, status: statusLabel, jobTitle: a.jobName || a.jobTitle || '', company: a.company || '' }
    })
  } catch (e) { /* fallback */ }
  finally { loading.value = false }
}

onMounted(fetchApplications)
watch(activeTab, fetchApplications)
</script>

<style scoped>
.page-header h2 { margin: 0 0 4px; font-size: 22px; color: #3E2723; }
</style>
