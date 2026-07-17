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
        <el-table-column prop="candidateName" label="候选人" width="80" />
        <el-table-column prop="jobTitle" label="面试职位" min-width="150" />
        <el-table-column prop="interviewerName" label="面试官" width="100" />
        <el-table-column prop="type" label="类型" width="100" />
        <el-table-column prop="time" label="时间" width="140" sortable />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }"><el-tag :type="row.status === '待面试' ? 'warning' : 'success'" size="small">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default><el-button size="small" type="primary" link>查看</el-button><el-button size="small" type="danger" link>取消</el-button></template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" :page-size="10" :total="total" layout="prev, pager, next, total" background style="margin-top:16px;justify-content:center;" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
// import { getInterviewList } from '@/api/interview'

const activeTab = ref('all'); const loading = ref(false); const page = ref(1); const total = ref(0)
const interviews = ref([])

onMounted(async () => {
  // TODO: const res = await getInterviewList({ page: page.value, size: 10, status: activeTab.value })
})
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.section-card { border-radius: 12px; margin-top: 16px; }
</style>
