<template>
  <div class="page">
    <div class="page-header"><h2>💼 职位列表</h2></div>
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="filters">
        <el-form-item label="关键词"><el-input v-model="filters.keyword" placeholder="职位" prefix-icon="Search" clearable style="width:200px;" /></el-form-item>
        <el-form-item label="地点"><el-input v-model="filters.location" placeholder="不限" clearable style="width:140px;" /></el-form-item>
        <el-form-item label="类别"><el-select v-model="filters.category" placeholder="不限" clearable style="width:140px;" @change="fetchJobs"><el-option v-for="c in categories" :key="c" :label="c" :value="c" /></el-select></el-form-item>
        <el-form-item label="排序"><el-select v-model="filters.sortBy" placeholder="默认" clearable style="width:120px;" @change="fetchJobs"><el-option label="发布时间" value="createTime" /><el-option label="薪资水平" value="salary" /></el-select></el-form-item>
        <el-form-item label="方式" v-if="filters.sortBy"><el-select v-model="filters.sortOrder" style="width:100px;" @change="fetchJobs"><el-option label="降序" value="desc" /><el-option label="升序" value="asc" /></el-select></el-form-item>
        <el-form-item><el-button type="primary" icon="Search" @click="fetchJobs">搜索</el-button><el-button @click="resetFilters">重置</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-table :data="jobList" style="width:100%;margin-top:16px;" v-loading="loading" stripe>
      <el-table-column prop="jobName" label="职位名称" min-width="160" />
      <el-table-column prop="publisherName" label="发布者" min-width="120" />
      <el-table-column prop="city" label="地点" width="120" />
      <el-table-column label="薪资" width="130">
        <template #default="{ row }">
          {{ row.salaryMin != null && row.salaryMax != null ? (row.salaryMin) + 'K-' + (row.salaryMax) + 'K' : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="category" label="类别" width="120" />
      <el-table-column label="技能标签" min-width="200">
        <template #default="{ row }">
          <div v-if="parseSkills(row.skills).length" class="skill-cell">
            <el-tag v-for="s in parseSkills(row.skills)" :key="s" size="small" effect="plain" type="info">{{ s }}</el-tag>
          </div>
          <span v-else class="empty-skills">-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120"><template #default="{ row }"><el-button type="primary" link @click="$router.push(`/candidate/jobs/${row.id}`)">查看详情</el-button></template></el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :page-size="10" :total="total" layout="prev, pager, next, total" background style="margin-top:20px;justify-content:center;" @current-change="fetchJobs" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getJobList, getActiveCategoryNames } from '@/api/job'

const loading = ref(false); const page = ref(1); const total = ref(0)
const jobList = ref([])
const categories = ref([])
const filters = reactive({ keyword: '', location: '', category: '', sortBy: '', sortOrder: 'desc' })

async function fetchCategories() {
  try {
    const res = await getActiveCategoryNames()
    categories.value = res.data || []
  } catch (e) { /* ignore */ }
}

async function fetchJobs() {
  loading.value = true
  try {
    const res = await getJobList({
      page: page.value,
      size: 10,
      keyword: filters.keyword || undefined,
      category: filters.category || undefined,
      city: filters.location || undefined,
      sortBy: filters.sortBy || undefined,
      sortOrder: filters.sortBy ? filters.sortOrder : undefined
    })
    if (res && res.data) {
      jobList.value = res.data.records || []
      total.value = res.data.total || jobList.value.length
    }
  } catch (error) {
    console.error('获取职位列表失败:', error)
  } finally { loading.value = false }
}
function resetFilters() {
  filters.keyword = '';
  filters.location = '';
  filters.category = '';
  filters.sortBy = '';
  filters.sortOrder = 'desc';
  fetchJobs()
}

function parseSkills(skills) {
  if (!skills) return []
  return String(skills).split(/[,，\n]/).map(s => s.trim()).filter(Boolean)
}

onMounted(() => { fetchCategories(); fetchJobs() })
</script>

<style scoped>
.page-header h2 { margin: 0 0 4px; font-size: 22px; color: #3E2723; }
.search-card { border-radius: 12px; background: #fafafa; border: none; }
.skill-cell { display: flex; flex-wrap: wrap; gap: 4px; }
.empty-skills { color: #c0c4cc; }
</style>
