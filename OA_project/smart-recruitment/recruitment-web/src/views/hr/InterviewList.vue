<template>
  <div class="page">
    <div class="page-header">
      <h2>📅 面试管理</h2>
      <el-button type="primary" icon="Plus" @click="$router.push('/hr/interviews/create')">创建面试</el-button>
    </div>
    <el-card shadow="never" class="section-card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="全部" name="all" /><el-tab-pane label="待面试" name="pending" />
        <el-tab-pane label="已完成" name="completed" />
      </el-tabs>
      <el-table :data="interviews" v-loading="loading" stripe>
        <el-table-column prop="candidateName" label="候选人" width="100" />
        <el-table-column prop="jobName" label="面试职位" min-width="150" />
        <el-table-column prop="interviewerName" label="面试官" width="100" />
        <el-table-column prop="interviewType" label="类型" width="100" />
        <el-table-column prop="interviewTime" label="时间" width="160" sortable>
          <template #default="{ row }">{{ formatDateTime(row.interviewTime) }}</template>
        </el-table-column>
        <el-table-column prop="statusLabel" label="状态" width="90" align="center">
          <template #default="{ row }"><el-tag :type="row.statusType" size="small">{{ row.statusLabel }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="$router.push(`/hr/interviews/detail/${row.id}`)">查看</el-button>
            <el-button v-if="row.status === 0" size="small" type="danger" link @click="handleCancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" :page-size="10" :total="total" layout="prev, pager, next, total" background style="margin-top:16px;justify-content:center;" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getInterviewList, cancelInterview } from '@/api/interview'
import { formatDateTime } from '@/utils/date'

const router = useRouter()
const activeTab = ref('all'); const loading = ref(false); const page = ref(1); const total = ref(0)
const interviews = ref([])

const tabStatusMap = { all: undefined, pending: 0, completed: 1 }

function statusLabel(s) {
  const map = { 0: '待面试', 1: '已完成', 2: '已取消' }
  return map[s] ?? '未知'
}

function statusType(s) {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[s] || 'info'
}

async function fetchInterviews() {
  loading.value = true
  try {
    const params = { page: page.value, size: 10 }
    if (tabStatusMap[activeTab.value] !== undefined) {
      params.status = tabStatusMap[activeTab.value]
    }
    const res = await getInterviewList(params)
    if (res && res.data) {
      interviews.value = (res.data.records || []).map(item => ({
        ...item,
        statusLabel: statusLabel(item.status),
        statusType: statusType(item.status)
      }))
      total.value = res.data.total || 0
    }
  } catch (e) {
    ElMessage.error('加载面试列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchInterviews)
watch([page, activeTab], fetchInterviews)

async function handleCancel(row) {
  try {
    await ElMessageBox.confirm('确定要取消该面试吗？', '确认操作', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await cancelInterview(row.id)
    ElMessage.success('面试已取消')
    await fetchInterviews()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.section-card { border-radius: 12px; margin-top: 16px; }
</style>
