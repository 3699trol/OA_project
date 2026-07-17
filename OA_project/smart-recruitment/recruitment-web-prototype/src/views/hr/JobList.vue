<template>
  <div class="page">
    <div class="page-header">
      <h2>💼 职位管理</h2>
      <el-button type="primary" icon="Plus" @click="$router.push('/hr/jobs/edit')">发布新职位</el-button>
    </div>
    <el-card shadow="never" class="section-card" style="margin-top:16px;">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="全部 (12)" name="all" />
        <el-tab-pane label="已发布 (8)" name="published" />
        <el-tab-pane label="草稿 (2)" name="draft" />
        <el-tab-pane label="已下架 (2)" name="closed" />
      </el-tabs>
      <el-table :data="filteredJobs" style="width:100%;" stripe>
        <el-table-column prop="title" label="职位名称" min-width="170">
          <template #default="{ row }">
            <el-button type="primary" link @click="$router.push(`/hr/jobs/edit/${row.id}`)">{{ row.title }}</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="类别" width="100" />
        <el-table-column prop="location" label="工作地点" width="120" />
        <el-table-column prop="salary" label="薪资范围" width="120" />
        <el-table-column label="招聘进度" width="160">
          <template #default="{ row }">
            <span class="progress-text">{{ row.filled }}/{{ row.headcount }}</span>
            <el-progress :percentage="Math.round(row.filled / row.headcount * 100)" :stroke-width="6" :show-text="false" :color="row.filled === row.headcount ? '#67C23A' : '#ED8936'" style="width:100px;display:inline-block;margin-left:8px;" />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '已发布' ? 'success' : row.status === '草稿' ? 'warning' : 'info'" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applications" label="投递数" width="80" align="center" />
        <el-table-column prop="updateTime" label="更新时间" width="130" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="$router.push(`/hr/jobs/edit/${row.id}`)">编辑</el-button>
            <el-button v-if="row.status === '草稿'" size="small" type="success">发布</el-button>
            <el-button v-if="row.status === '已发布'" size="small" type="warning">下架</el-button>
            <el-button size="small" type="danger" plain>删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" :page-size="10" :total="12" layout="prev, pager, next" background style="margin-top:16px;justify-content:center;" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const activeTab = ref('all')
const page = ref(1)

const jobs = [
  { id: 1, title: 'Java高级开发工程师', category: '技术研发', location: '北京·朝阳区', salary: '15K-25K', headcount: 3, filled: 2, status: '已发布', applications: 47, updateTime: '2024-07-17' },
  { id: 2, title: '前端开发工程师（Vue）', category: '技术研发', location: '上海·浦东新区', salary: '12K-20K', headcount: 2, filled: 0, status: '已发布', applications: 32, updateTime: '2024-07-16' },
  { id: 3, title: '数据分析师', category: '数据科学', location: '深圳·南山区', salary: '10K-18K', headcount: 1, filled: 1, status: '已发布', applications: 28, updateTime: '2024-07-15' },
  { id: 4, title: 'DevOps工程师', category: '技术研发', location: '北京·海淀区', salary: '18K-28K', headcount: 2, filled: 1, status: '已发布', applications: 18, updateTime: '2024-07-14' },
  { id: 5, title: '产品经理（B端SaaS）', category: '产品设计', location: '杭州·余杭区', salary: '14K-22K', headcount: 1, filled: 0, status: '已发布', applications: 25, updateTime: '2024-07-13' },
  { id: 6, title: 'UI/UX设计师', category: '产品设计', location: '广州·天河区', salary: '11K-19K', headcount: 1, filled: 0, status: '草稿', applications: 0, updateTime: '2024-07-12' },
  { id: 7, title: '测试开发工程师', category: '技术研发', location: '北京·西城区', salary: '16K-24K', headcount: 2, filled: 0, status: '已发布', applications: 22, updateTime: '2024-07-11' },
  { id: 8, title: '全栈工程师', category: '技术研发', location: '成都·高新区', salary: '13K-22K', headcount: 1, filled: 0, status: '已下架', applications: 35, updateTime: '2024-07-10' },
  { id: 9, title: '运维工程师', category: '技术研发', location: '北京·朝阳区', salary: '12K-18K', headcount: 1, filled: 0, status: '草稿', applications: 0, updateTime: '2024-07-09' },
  { id: 10, title: '市场营销经理', category: '运营市场', location: '上海·徐汇区', salary: '12K-20K', headcount: 1, filled: 0, status: '已发布', applications: 16, updateTime: '2024-07-08' }
]

const filteredJobs = computed(() => {
  const map = { published: '已发布', draft: '草稿', closed: '已下架' }
  return activeTab.value === 'all' ? jobs : jobs.filter(j => j.status === map[activeTab.value])
})
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.section-card { border-radius: 12px; }
.progress-text { font-size: 12px; color: #999; }
</style>
