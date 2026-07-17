<template>
  <div class="page">
    <div class="page-header"><h2>📂 职位分类管理</h2><el-button type="primary" icon="Plus">新增分类</el-button></div>
    <el-card shadow="never" class="section-card">
      <el-table :data="categories" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="名称" width="150" />
        <el-table-column prop="code" label="编码" width="150" />
        <el-table-column prop="jobCount" label="关联职位数" width="100" align="center" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }"><el-switch :model-value="row.status === 1" active-color="#67C23A" size="small" /></template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default><el-button size="small" type="primary" link>编辑</el-button><el-button size="small" type="danger" link>删除</el-button></template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
// import { getJobCategories } from '@/api/job'

const loading = ref(false); const categories = ref([])
onMounted(async () => { /* TODO: const res = await getJobCategories() */ })
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.section-card { border-radius: 12px; margin-top: 16px; }
</style>
