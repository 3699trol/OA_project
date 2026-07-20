<template>
  <div class="page">
    <div class="page-header"><h2>🔒 权限管理</h2><el-button type="primary" icon="Plus">新增权限</el-button></div>
    <el-card shadow="never" class="section-card">
      <el-table :data="permissions" v-loading="loading" stripe>
        <el-table-column prop="permissionName" label="名称" min-width="200">
          <template #default="{ row }"><span :style="{ paddingLeft: (row.level - 1) * 24 + 'px' }">{{ row.permissionName }}</span></template>
        </el-table-column>
        <el-table-column prop="permissionCode" label="编码" width="200" />
        <el-table-column prop="type" label="类型" width="90" align="center">
          <template #default="{ row }"><el-tag size="small" effect="plain">{{ row.type }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="150"><template #default><el-button size="small" type="primary" link>编辑</el-button><el-button size="small" type="danger" link>删除</el-button></template></el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getPermissionList } from '@/api/user'

const loading = ref(false); const permissions = ref([])
onMounted(async () => {
  loading.value = true
  try {
    const res = await getPermissionList()
    permissions.value = res.data || []
  } catch (e) { /* fallback */ }
  finally { loading.value = false }
})
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.section-card { border-radius: 12px; margin-top: 16px; }
</style>
