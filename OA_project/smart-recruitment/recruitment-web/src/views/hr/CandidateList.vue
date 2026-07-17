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
      </el-tabs>
      <el-table :data="candidates" v-loading="loading" stripe>
        <el-table-column prop="name" label="姓名" width="80" />
        <el-table-column prop="jobTitle" label="投递职位" min-width="160" />
        <el-table-column prop="matchScore" label="AI匹配度" width="100" align="center">
          <template #default="{ row }"><el-tag :type="row.matchScore >= 80 ? 'success' : 'warning'">{{ row.matchScore }}%</el-tag></template>
        </el-table-column>
        <el-table-column prop="education" label="学历" width="80" />
        <el-table-column prop="experience" label="经验" width="90" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }"><el-tag :type="statusType(row.status)" size="small">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="$router.push('/hr/candidates/detail')">简历详情</el-button>
            <el-button size="small" type="success" link>安排面试</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" :page-size="10" :total="total" layout="prev, pager, next, total" background style="margin-top:16px;justify-content:center;" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
// import { getCandidateList } from '@/api/application'

const activeTab = ref('all'); const loading = ref(false); const searchKeyword = ref(''); const page = ref(1); const total = ref(0)
const candidates = ref([])

function statusType(s) { return { '初筛中': 'warning', '面试中': 'primary', '已录用': 'success', '不合适': 'info' }[s] || '' }

onMounted(async () => {
  // TODO: const res = await getCandidateList({ page: page.value, size: 10, keyword: searchKeyword.value, status: activeTab.value })
})
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.section-card { border-radius: 12px; margin-top: 16px; }
</style>
