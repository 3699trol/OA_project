<template>
  <div class="page">
    <div class="page-header"><h2>📜 面试历史</h2></div>
    <el-card shadow="never" class="section-card">
      <el-table :data="history" v-loading="loading" stripe>
        <el-table-column prop="candidate" label="候选人" />
        <el-table-column prop="jobTitle" label="职位" />
        <el-table-column prop="date" label="日期" width="120" sortable />
        <el-table-column prop="score" label="评分" width="100" align="center">
          <template #default="{ row }"><el-rate :model-value="row.score" :max="5" disabled show-score /></template>
        </el-table-column>
        <el-table-column prop="recommendation" label="建议" width="90" align="center" />
        <el-table-column label="操作" width="120"><template #default><el-button type="primary" link size="small">查看</el-button></template></el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" :page-size="10" :total="total" layout="prev, pager, next" background style="margin-top:16px;justify-content:center;" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
// import { getInterviewHistory } from '@/api/interview'

const loading = ref(false); const page = ref(1); const total = ref(0); const history = ref([])
onMounted(async () => { /* TODO: const res = await getInterviewHistory({ page: page.value }) */ })
</script>

<style scoped>
.page-header h2 { margin: 0 0 16px; font-size: 22px; color: #3E2723; }
.section-card { border-radius: 12px; }
</style>
