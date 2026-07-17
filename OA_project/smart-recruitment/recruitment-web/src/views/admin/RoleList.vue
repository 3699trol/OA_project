<template>
  <div class="page">
    <div class="page-header"><h2>🔐 角色管理</h2><el-button type="primary" icon="Plus">新增角色</el-button></div>
    <el-card shadow="never" class="section-card">
      <el-table :data="roles" v-loading="loading" stripe>
        <el-table-column prop="roleCode" label="编码" width="140" />
        <el-table-column prop="roleName" label="名称" width="120">
          <template #default="{ row }"><el-tag :type="roleColor(row.roleCode)" effect="dark" round>{{ row.roleName }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column prop="userCount" label="用户数" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }"><el-switch :model-value="row.status === 1" active-color="#67C23A" size="small" /></template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default><el-button size="small" type="primary" link>编辑</el-button><el-button size="small" type="warning" link>分配权限</el-button><el-button size="small" type="danger" link>删除</el-button></template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
// import { getRoleList } from '@/api/user'

const loading = ref(false); const roles = ref([])
function roleColor(c) { return { 'ADMIN': 'danger', 'HR': 'warning', 'INTERVIEWER': 'primary', 'CANDIDATE': 'success' }[c] || 'info' }
onMounted(async () => { /* TODO: const res = await getRoleList() */ })
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.section-card { border-radius: 12px; margin-top: 16px; }
</style>
