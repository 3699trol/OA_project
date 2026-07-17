<template>
  <div class="page">
    <div class="page-header"><h2>📜 面试历史记录</h2></div>
    <el-card shadow="never" class="section-card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="全部 (32)" name="all" />
        <el-tab-pane label="本月 (12)" name="thisMonth" />
        <el-tab-pane label="上月 (8)" name="lastMonth" />
      </el-tabs>
      <el-table :data="history" stripe>
        <el-table-column prop="candidate" label="候选人" width="80" />
        <el-table-column prop="jobTitle" label="面试职位" min-width="150" />
        <el-table-column prop="type" label="类型" width="90">
          <template #default="{ row }"><el-tag size="small" effect="plain">{{ row.type }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="date" label="日期" width="110" sortable />
        <el-table-column prop="score" label="综合评分" width="100" align="center">
          <template #default="{ row }">
            <el-rate v-model="row.score" :max="5" size="small" disabled show-score text-color="#ED8936" />
          </template>
        </el-table-column>
        <el-table-column prop="recommendation" label="建议" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.recommendation === 'PASS' ? 'success' : row.recommendation === 'PENDING' ? 'warning' : 'danger'" size="small">{{ row.recommendation === 'PASS' ? '通过' : row.recommendation === 'PENDING' ? '待定' : '未通过' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="$router.push(`/interviewer/tasks/${row.id}`)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const activeTab = ref('all')
const history = [
  { id: 3, candidate: '王五', jobTitle: '数据分析师', type: '技术面试', date: '2024-07-17', score: 4, recommendation: 'PASS' },
  { id: 4, candidate: '赵六', jobTitle: 'DevOps工程师', type: '技术面试', date: '2024-07-16', score: 4, recommendation: 'PASS' },
  { id: 6, candidate: '孙八', jobTitle: 'UI设计师', type: '综合面试', date: '2024-07-15', score: 2, recommendation: 'REJECT' },
  { id: 8, candidate: '吴十', jobTitle: '全栈工程师', type: '技术面试', date: '2024-07-14', score: 3, recommendation: 'PENDING' },
  { id: 9, candidate: '郑十一', jobTitle: 'Java开发工程师', type: '技术面试', date: '2024-07-10', score: 4, recommendation: 'PASS' },
  { id: 10, candidate: '钱十二', jobTitle: '前端开发工程师', type: '技术面试', date: '2024-07-08', score: 3, recommendation: 'PENDING' }
]
</script>

<style scoped>
.page-header h2 { margin: 0 0 16px; font-size: 22px; color: #3E2723; }
.section-card { border-radius: 12px; }
</style>
