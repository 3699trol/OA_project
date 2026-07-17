<template>
  <div class="page">
    <div class="page-header">
      <h2>📂 职位分类管理</h2>
      <el-button type="primary" icon="Plus">新增分类</el-button>
    </div>
    <el-card shadow="never" class="section-card">
      <el-table :data="categories" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="分类名称" width="150" />
        <el-table-column prop="code" label="分类编码" width="150" />
        <el-table-column prop="jobCount" label="关联职位数" width="100" align="center" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" active-color="#67C23A" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default>
            <el-button size="small" type="primary" link>编辑</el-button>
            <el-button size="small" type="danger" link>删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
const categories = [
  { id: 1, name: '技术研发', code: 'TECH', jobCount: 6, description: '软件开发、测试、运维等技术岗位', status: 1 },
  { id: 2, name: '产品设计', code: 'PRODUCT', jobCount: 3, description: '产品经理、UI/UX设计等产品相关岗位', status: 1 },
  { id: 3, name: '数据科学', code: 'DATA', jobCount: 2, description: '数据分析、算法工程、机器学习等数据岗位', status: 1 },
  { id: 4, name: '运营市场', code: 'MARKETING', jobCount: 1, description: '市场推广、品牌运营等岗位', status: 1 },
  { id: 5, name: '职能支持', code: 'ADMIN', jobCount: 0, description: '人事、财务、行政等职能类岗位', status: 0 }
]
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.section-card { border-radius: 12px; margin-top: 16px; }
</style>
