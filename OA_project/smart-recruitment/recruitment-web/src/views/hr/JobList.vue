<template>
  <div class="job-list-page">
    <div class="page-header">
      <div class="header-left">
        <h2>💼 职位发布与管理</h2>
        <p class="subtitle">在这里发布和编辑您的招聘岗位，一键发布/下架并跟踪招聘进展</p>
      </div>
      <el-button type="primary" class="header-btn" icon="Plus" @click="$router.push('/hr/jobs/edit')">
        发布新职位
      </el-button>
    </div>

    <!-- 过滤器面板 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="职位名称">
          <el-input v-model="filterForm.title" placeholder="输入关键字搜索..." clearable class="search-input" />
        </el-form-item>
        <el-form-item label="职位类别">
          <el-select v-model="filterForm.category" placeholder="全部类别" clearable class="category-select">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="filter-btn" icon="Search" @click="fetchJobs">
            查询
          </el-button>
          <el-button icon="Refresh" @click="resetFilter" class="reset-btn">
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 列表展示面板 -->
    <el-card shadow="never" class="table-card">
      <el-tabs v-model="activeTab" class="custom-tabs" @tab-change="handleTabChange">
        <el-tab-pane label="📋 全部职位" name="ALL" />
        <el-tab-pane label="🟢 已发布" name="PUBLISHED" />
        <el-tab-pane label="🟡 草稿箱" name="DRAFT" />
        <el-tab-pane label="🔴 已下架" name="UNPUBLISHED" />
      </el-tabs>

      <el-table 
        :data="jobs" 
        v-loading="loading" 
        class="custom-table"
        :header-cell-style="{ background: '#f8f9fc', color: '#4a5568', fontWeight: '600', height: '48px' }"
      >
        <el-table-column prop="title" label="职位名称" min-width="180">
          <template #default="{ row }">
            <div class="job-title-cell">
              <span class="j-title">{{ row.jobName }}</span>
              <span class="j-dept">{{ row.department }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="职位类别" width="120">
          <template #default="{ row }">
            <el-tag type="info" effect="plain" class="category-tag">{{ row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="city" label="工作城市" width="120" />
        <el-table-column prop="salary" label="薪资范围" width="130">
          <template #default="{ row }">
            <span class="salary-text">{{ row.salaryMin != null && row.salaryMax != null ? (row.salaryMin) + 'K-' + (row.salaryMax) + 'K' : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="experience" label="经验要求" width="120" />
        <el-table-column prop="education" label="最低学历" width="110" />
        <el-table-column prop="status" label="当前状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" effect="light" class="status-tag">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="更新时间" width="170" />
        <el-table-column label="快捷操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button 
              size="small" 
              type="primary" 
              link
              icon="Edit"
              @click="$router.push(`/hr/jobs/edit/${row.id}`)"
            >
              编辑
            </el-button>
            <el-button 
              v-if="row.status === 0 || row.status === 2" 
              size="small" 
              type="success" 
              link
              icon="Upload"
              @click="handlePublish(row)"
            >
              发布
            </el-button>
            <el-button 
              v-if="row.status === 1" 
              size="small" 
              type="warning" 
              link
              icon="Download"
              @click="handleUnpublish(row)"
            >
              下线
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination 
          v-model:current-page="page" 
          :page-size="10" 
          :total="total" 
          layout="prev, pager, next, total" 
          background 
          @current-change="fetchJobs"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getJobList, publishJob, unpublishJob, getActiveCategoryNames } from '@/api/job'

const activeTab = ref('ALL')
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const jobs = ref([])
const categories = ref([])

const filterForm = reactive({
  title: '',
  category: ''
})

function statusType(s) {
  const mapping = {
    1: 'success',
    0: 'warning',
    2: 'danger'
  }
  return mapping[s] || 'info'
}

function statusLabel(s) {
  const mapping = {
    1: '已发布',
    0: '草稿',
    2: '已下架'
  }
  return mapping[s] || s
}

function statusToNum(s) {
  const mapping = {
    PUBLISHED: 1,
    DRAFT: 0,
    UNPUBLISHED: 2
  }
  return mapping[s] ?? s
}

async function fetchJobs() {
  loading.value = true
  try {
    const params = {
      keyword: filterForm.title || undefined,
      status: activeTab.value === 'ALL' ? undefined : statusToNum(activeTab.value),
      category: filterForm.category || undefined,
      page: page.value,
      size: 10
    }
    const res = await getJobList(params)
    if (res.code === 200) {
      jobs.value = res.data.records
      total.value = res.data.total
    }
  } catch (e) {
    console.error('获取职位列表失败', e)
  } finally {
    loading.value = false
  }
}

function handleTabChange() {
  page.value = 1
  fetchJobs()
}

function resetFilter() {
  filterForm.title = ''
  filterForm.category = ''
  page.value = 1
  fetchJobs()
}

async function handlePublish(row) {
  try {
    await ElMessageBox.confirm(`确定要发布该职位 [${row.jobName}] 吗？发布后将展示给求职者大厅。`, '发布提示', {
      confirmButtonText: '立即发布',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'msg-confirm-btn'
    })
    
    const res = await publishJob(row.id)
    if (res.code === 200) {
      ElMessage.success('发布职位成功！')
      fetchJobs()
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '操作失败')
    }
  }
}

async function handleUnpublish(row) {
  try {
    await ElMessageBox.confirm(`确定要下架职位 [${row.jobName}] 吗？下架后求职者将无法浏览此职位。`, '下架提示', {
      confirmButtonText: '下线职位',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'msg-danger-btn'
    })
    
    const res = await unpublishJob(row.id)
    if (res.code === 200) {
      ElMessage.success('职位已成功下架！')
      fetchJobs()
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '操作失败')
    }
  }
}

onMounted(() => {
  fetchCategories()
  fetchJobs()
})

async function fetchCategories() {
  try {
    const res = await getActiveCategoryNames()
    categories.value = res.data || []
  } catch (e) { /* ignore */ }
}
</script>

<style scoped>
.job-list-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 头部样式 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left h2 {
  font-size: 24px;
  font-weight: 800;
  color: #1a202c;
  margin: 0 0 6px 0;
}

.header-left .subtitle {
  font-size: 13px;
  color: #718096;
  margin: 0;
}

.header-btn {
  background-color: #e76f51;
  border-color: #e76f51;
  border-radius: 8px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(231, 111, 81, 0.2);
}

.header-btn:hover {
  background-color: #f4a261;
  border-color: #f4a261;
}

/* 过滤器卡片 */
.filter-card {
  border-radius: 12px;
  border: 1px solid #f1f3f9;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: 24px;
}

.filter-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: #4a5568;
}

.search-input {
  width: 200px;
}

.category-select {
  width: 140px;
}

.filter-btn {
  background-color: #1a1d21;
  border-color: #1a1d21;
  border-radius: 6px;
  font-weight: 600;
}

.filter-btn:hover {
  background-color: #2b3036;
  border-color: #2b3036;
}

.reset-btn {
  border-radius: 6px;
  color: #4a5568;
}

/* 表格展示卡片 */
.table-card {
  border-radius: 12px;
  border: 1px solid #f1f3f9;
}

.custom-tabs :deep(.el-tabs__item) {
  font-size: 14px;
  font-weight: 500;
  color: #718096;
  padding: 0 20px;
  height: 48px;
  line-height: 48px;
}

.custom-tabs :deep(.el-tabs__item.is-active) {
  color: #e76f51;
  font-weight: 700;
}

.custom-tabs :deep(.el-tabs__active-bar) {
  background-color: #e76f51;
  height: 3px;
  border-radius: 2px;
}

.custom-tabs :deep(.el-tabs__nav-wrap::after) {
  background-color: #f1f3f9;
}

.custom-table :deep(.el-table__row) {
  height: 64px;
}

.job-title-cell {
  display: flex;
  flex-direction: column;
}

.j-title {
  font-weight: 700;
  color: #2d3748;
  font-size: 15px;
}

.j-dept {
  font-size: 11px;
  color: #a0aec0;
  margin-top: 2px;
}

.category-tag {
  border-radius: 6px;
  font-weight: 500;
}

.salary-text {
  font-weight: 700;
  color: #e76f51;
}

.status-tag {
  border-radius: 6px;
  font-weight: 600;
  padding: 4px 8px;
}

.pagination-wrapper {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

/* 分页覆盖 */
:deep(.el-pagination.is-background .el-pager li:not(.is-active)) {
  background-color: #f8f9fc;
  color: #4a5568;
}

:deep(.el-pagination.is-background .el-pager li.is-active) {
  background-color: #e76f51;
  color: #ffffff;
  font-weight: 700;
}
</style>
