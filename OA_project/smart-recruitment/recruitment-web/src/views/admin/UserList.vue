<template>
  <div class="page">
    <div class="page-header">
      <h2>👥 用户管理</h2>
      <div class="header-right">
        <el-input v-model="search" placeholder="搜索用户名" prefix-icon="Search" clearable style="width:200px;margin-right:10px;" />
        <el-button type="primary" icon="Plus">新增用户</el-button>
      </div>
    </div>
    <el-card shadow="never" class="section-card">
      <el-table :data="users" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户名" width="110" />
        <el-table-column prop="realName" label="姓名" width="90" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }"><el-tag :type="roleType(row.role)" size="small">{{ row.role }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="160" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }"><el-switch :model-value="row.status === 1" active-color="#67C23A" size="small" /></template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="120" sortable />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default><el-button size="small" type="primary" link>编辑</el-button><el-button size="small" type="warning" link>重置密码</el-button><el-button size="small" type="danger" link>删除</el-button></template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" :page-size="10" :total="total" layout="prev, pager, next, total" background style="margin-top:16px;justify-content:center;" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
// import { getUserList } from '@/api/user'

const search = ref(''); const loading = ref(false); const page = ref(1); const total = ref(0); const users = ref([])
function roleType(r) { return { '管理员': 'danger', 'HR': 'warning', '面试官': 'primary', '求职者': 'success' }[r] || 'info' }
onMounted(async () => { /* TODO: const res = await getUserList({ page: page.value, keyword: search.value }) */ })
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.header-right { display: flex; align-items: center; }
.section-card { border-radius: 12px; margin-top: 16px; }
</style>
