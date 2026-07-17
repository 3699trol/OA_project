<template>
  <div class="page">
    <div class="page-header">
      <h2>💼 职位管理</h2>
      <el-button type="primary" icon="Plus" @click="$router.push('/hr/jobs/edit')">发布新职位</el-button>
    </div>
    <el-card shadow="never" class="section-card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="全部" name="all" /><el-tab-pane label="已发布" name="published" />
        <el-tab-pane label="草稿" name="draft" /><el-tab-pane label="已下架" name="closed" />
      </el-tabs>
      <el-table :data="jobs" v-loading="loading" stripe>
        <el-table-column prop="title" label="职位名称" min-width="160" />
        <el-table-column prop="category" label="类别" width="100" />
        <el-table-column prop="location" label="地点" width="120" />
        <el-table-column prop="salary" label="薪资" width="120" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }"><el-tag :type="statusType(row.status)" size="small">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="applications" label="投递数" width="80" align="center" />
        <el-table-column prop="updateTime" label="更新时间" width="120" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="$router.push(`/hr/jobs/edit/${row.id}`)">编辑</el-button>
            <el-button v-if="row.status === '草稿'" size="small" type="success">发布</el-button>
            <el-button v-if="row.status === '已发布'" size="small" type="warning">下架</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" :page-size="10" :total="total" layout="prev, pager, next, total" background style="margin-top:16px;justify-content:center;" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getJobList } from '@/api/job'

const activeTab = ref('all'); const loading = ref(false); const page = ref(1); const total = ref(0)
const jobs = ref([])

function statusType(s) { return { '已发布': 'success', '草稿': 'warning', '已下架': 'info' }[s] || '' }

onMounted(async () => {
  loading.value = true
  // TODO: const res = await getJobList({ page: page.value, size: 10, status: activeTab.value })
  // jobs.value = res.data.records; total.value = res.data.total
  loading.value = false
})
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.section-card { border-radius: 12px; margin-top: 16px; }
</style>
