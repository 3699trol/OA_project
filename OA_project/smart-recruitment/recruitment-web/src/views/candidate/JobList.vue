<template>
  <div class="page">
    <div class="page-header"><h2>💼 职位列表</h2></div>
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="filters">
        <el-form-item label="关键词"><el-input v-model="filters.keyword" placeholder="职位/技能" prefix-icon="Search" clearable style="width:200px;" /></el-form-item>
        <el-form-item label="地点"><el-select v-model="filters.location" placeholder="不限" clearable style="width:140px;"><el-option label="北京" value="北京" /><el-option label="上海" value="上海" /><el-option label="深圳" value="深圳" /><el-option label="杭州" value="杭州" /></el-select></el-form-item>
        <el-form-item label="类别"><el-select v-model="filters.category" placeholder="不限" clearable style="width:140px;"><el-option label="技术研发" value="技术研发" /><el-option label="产品设计" value="产品设计" /></el-select></el-form-item>
        <el-form-item><el-button type="primary" icon="Search" @click="fetchJobs">搜索</el-button><el-button @click="resetFilters">重置</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-table :data="jobList" style="width:100%;margin-top:16px;" v-loading="loading" stripe>
      <el-table-column prop="title" label="职位名称" min-width="160" />
      <el-table-column prop="company" label="公司" min-width="140" />
      <el-table-column prop="location" label="地点" width="120" />
      <el-table-column prop="salary" label="薪资" width="110" />
      <el-table-column prop="matchScore" label="匹配度" width="90" align="center" />
      <el-table-column label="操作" width="120"><template #default="{ row }"><el-button type="primary" link @click="$router.push(`/candidate/jobs/${row.id}`)">查看详情</el-button></template></el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :page-size="10" :total="total" layout="prev, pager, next, total" background style="margin-top:20px;justify-content:center;" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getJobList } from '@/api/job'

const loading = ref(false); const page = ref(1); const total = ref(0)
const jobList = ref([])
const filters = reactive({ keyword: '', location: '', category: '' })

async function fetchJobs() {
  loading.value = true
  try {
    // TODO: const res = await getJobList({ page: page.value, size: 10, ...filters })
    // jobList.value = res.data.records; total.value = res.data.total
  } finally { loading.value = false }
}
function resetFilters() { filters.keyword = ''; filters.location = ''; filters.category = ''; fetchJobs() }

onMounted(() => fetchJobs())
</script>

<style scoped>
.page-header h2 { margin: 0 0 4px; font-size: 22px; color: #3E2723; }
.search-card { border-radius: 12px; background: #fafafa; border: none; }
</style>
