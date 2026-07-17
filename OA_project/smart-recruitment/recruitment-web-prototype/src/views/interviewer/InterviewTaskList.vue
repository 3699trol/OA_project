<template>
  <div class="page">
    <div class="page-header">
      <h2>📋 面试任务</h2>
      <div class="header-right">
        <el-radio-group v-model="filter" size="small">
          <el-radio-button value="all">全部</el-radio-button>
          <el-radio-button value="pending">待面试</el-radio-button>
          <el-radio-button value="completed">已完成</el-radio-button>
        </el-radio-group>
      </div>
    </div>
    <el-card shadow="never" class="section-card">
      <el-table :data="filteredTasks" style="width:100%;" stripe>
        <el-table-column prop="candidateName" label="候选人" width="80" />
        <el-table-column prop="jobTitle" label="面试职位" min-width="160" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }"><el-tag size="small" effect="plain">{{ row.type }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="time" label="面试时间" width="160" sortable />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '待面试' ? 'warning' : 'success'" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="$router.push(`/interviewer/tasks/${row.id}`)">
              {{ row.status === '待面试' ? '进入面试' : '查看详情' }}
            </el-button>
            <el-button v-if="row.status === '待面试'" size="small" type="success" link @click="$router.push(`/interviewer/questions/generate`)">
              AI出题
            </el-button>
            <el-button v-if="row.status === '待面试'" size="small" type="warning" link @click="$router.push(`/interviewer/evaluation/${row.id}`)">
              评价
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const filter = ref('all')
const tasks = [
  { id: 1, candidateName: '张三', jobTitle: 'Java高级开发工程师', type: '技术面试', time: '2024-07-18 14:00', status: '待面试' },
  { id: 2, candidateName: '李四', jobTitle: '前端开发工程师（Vue）', type: '技术面试', time: '2024-07-18 15:30', status: '待面试' },
  { id: 3, candidateName: '王五', jobTitle: '数据分析师', type: '技术面试', time: '2024-07-17 10:00', status: '已完成' },
  { id: 4, candidateName: '赵六', jobTitle: 'DevOps工程师', type: '技术面试', time: '2024-07-16 14:00', status: '已完成' },
  { id: 5, candidateName: '周九', jobTitle: '测试开发工程师', type: '技术面试', time: '2024-07-20 10:00', status: '待面试' },
  { id: 6, candidateName: '陈七', jobTitle: '产品经理', type: '综合面试', time: '2024-07-19 09:30', status: '待面试' }
]
const filteredTasks = computed(() => filter.value === 'all' ? tasks : tasks.filter(t => t.status === (filter.value === 'pending' ? '待面试' : '已完成')))
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.section-card { border-radius: 12px; margin-top: 16px; }
</style>
