<template>
  <div class="page">
    <div class="page-header">
      <h2>👥 候选人管理</h2>
      <el-input v-model="searchKeyword" placeholder="搜索姓名/技能" prefix-icon="Search" clearable style="width:240px;" />
    </div>
    <el-card shadow="never" class="section-card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="全部" name="all" /><el-tab-pane label="初筛中" name="screening" />
        <el-tab-pane label="面试中" name="interviewing" />
        <el-tab-pane label="已录用" name="hired" />
        <el-tab-pane label="不合适" name="rejected" />
      </el-tabs>
      <el-table :data="candidates" v-loading="loading" stripe>
        <el-table-column prop="name" label="姓名" width="80" />
        <el-table-column prop="jobTitle" label="投递职位" min-width="160" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }"><el-tag :type="statusType(row.status)" size="small">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="$router.push(`/hr/candidates/detail/${row.id}`)">简历详情</el-button>
            <el-button v-if="row.status === '面试中' && !row.interviewId" size="small" type="success" link @click="$router.push(`/hr/interviews/create?candidateId=${row.userId}&applicationId=${row.id}`)">安排面试</el-button>
            <el-button v-if="row.status === '面试中' && row.interviewId" size="small" type="warning" link @click="$router.push(`/hr/interviews/detail/${row.interviewId}`)">面试详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" :page-size="10" :total="total" layout="prev, pager, next, total" background style="margin-top:16px;justify-content:center;" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { getApplicationList } from '@/api/application'

const activeTab = ref('all'); const loading = ref(false); const searchKeyword = ref(''); const page = ref(1); const total = ref(0)
const candidates = ref([])

function statusType(s) { return { '初筛中': 'warning', '面试中': 'primary', '已录用': 'success', '不合适': 'info' }[s] || '' }

const tabStatusMap = { all: undefined, screening: 0, interviewing: 1, hired: 2, rejected: 3 }
function mapStatus(code) {
  if (code === 0) return '初筛中'
  if (code === 1) return '面试中'
  if (code === 2) return '已录用'
  if (code === 3) return '不合适'
  return '初筛中'
}

async function fetchCandidates() {
  loading.value = true
  try {
    const params = { page: page.value, size: 10, role: 'hr', status: tabStatusMap[activeTab.value] }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    const res = await getApplicationList(params)
    candidates.value = (res.data.records || []).map(a => ({
      id: a.id,
      userId: a.userId,
      name: a.candidateName || a.userName || '候选人',
      jobTitle: a.jobName || a.jobTitle || '',
      status: mapStatus(a.status),
      interviewId: a.interviewId || null,
      interviewStatus: a.interviewStatus != null ? a.interviewStatus : null
    }))
    total.value = res.data.total || 0
  } catch (e) { /* fallback */ }
  finally { loading.value = false }
}

onMounted(fetchCandidates)
watch([page, activeTab, searchKeyword], fetchCandidates)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.section-card { border-radius: 12px; margin-top: 16px; }
</style>
