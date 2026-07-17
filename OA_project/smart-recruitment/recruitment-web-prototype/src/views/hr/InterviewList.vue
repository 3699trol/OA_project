<template>
  <div class="page">
    <div class="page-header">
      <h2>📅 面试管理</h2>
      <el-button type="primary" icon="Plus" @click="$router.push('/hr/interviews/create')">创建面试</el-button>
    </div>
    <el-card shadow="never" class="section-card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="全部 (18)" name="all" />
        <el-tab-pane label="待面试 (6)" name="pending" />
        <el-tab-pane label="已完成 (10)" name="completed" />
        <el-tab-pane label="已取消 (2)" name="cancelled" />
      </el-tabs>
      <el-table :data="interviews" style="width:100%;" stripe>
        <el-table-column prop="candidateName" label="候选人" width="80" />
        <el-table-column prop="jobTitle" label="面试职位" min-width="160" />
        <el-table-column prop="interviewerName" label="面试官" width="100" />
        <el-table-column prop="type" label="面试类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === '技术面试' ? 'primary' : 'success'" size="small" effect="plain">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="time" label="面试时间" width="160" sortable />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '待面试' ? 'warning' : row.status === '已完成' ? 'success' : 'info'" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === '已完成'" size="small" type="primary" link>查看评价</el-button>
            <el-button v-if="row.status === '待面试'" size="small" type="warning" link>调整</el-button>
            <el-button size="small" type="danger" link>取消</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const activeTab = ref('all')
const interviews = [
  { id: 1, candidateName: '张三', jobTitle: 'Java高级开发工程师', interviewerName: '王工', type: '技术面试', time: '2024-07-18 14:00', status: '待面试' },
  { id: 2, candidateName: '李四', jobTitle: '前端开发工程师（Vue）', interviewerName: '刘工', type: '技术面试', time: '2024-07-18 15:30', status: '待面试' },
  { id: 3, candidateName: '王五', jobTitle: '数据分析师', interviewerName: '赵工', type: '技术面试', time: '2024-07-17 10:00', status: '已完成' },
  { id: 4, candidateName: '赵六', jobTitle: 'DevOps工程师', interviewerName: '王工', type: '技术面试', time: '2024-07-16 14:00', status: '已完成' },
  { id: 5, candidateName: '陈七', jobTitle: '产品经理', interviewerName: '李经理', type: '综合面试', time: '2024-07-19 09:30', status: '待面试' },
  { id: 6, candidateName: '孙八', jobTitle: 'UI设计师', interviewerName: '刘工', type: '综合面试', time: '2024-07-15 14:00', status: '已取消' },
  { id: 7, candidateName: '周九', jobTitle: '测试开发工程师', interviewerName: '王工', type: '技术面试', time: '2024-07-20 10:00', status: '待面试' },
  { id: 8, candidateName: '吴十', jobTitle: '全栈工程师', interviewerName: '赵工', type: '技术面试', time: '2024-07-14 10:00', status: '已完成' }
]
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.section-card { border-radius: 12px; margin-top: 16px; }
</style>
