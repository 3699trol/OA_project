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
      <el-table :data="users" v-loading="loading" stripe :row-class-name="tableRowClassName">
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
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <template v-if="row.deleted === 0">
              <el-button size="small" type="primary" link>编辑</el-button>
              <el-button size="small" type="warning" link @click="handleResetPassword(row)">重置密码</el-button>
              <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
            </template>
            <template v-else>
              <el-tag type="info" size="small" style="margin-right: 8px">已删除</el-tag>
              <el-button size="small" type="success" link @click="handleRestore(row)">恢复</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" :page-size="10" :total="total" layout="prev, pager, next, total" background style="margin-top:16px;justify-content:center;" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, resetPassword, deleteUser, restoreUser } from '@/api/user'

const search = ref(''); const loading = ref(false); const page = ref(1); const total = ref(0); const users = ref([])

const userTypeMap = { 1: '管理员', 2: 'HR', 3: '面试官', 4: '求职者' }
function roleType(r) { return { '管理员': 'danger', 'HR': 'warning', '面试官': 'primary', '求职者': 'success' }[r] || 'info' }
function mapRole(userType) { return userTypeMap[userType] || '未知' }

async function fetchUsers() {
  loading.value = true
  try {
    const res = await getUserList({ page: page.value, size: 10, keyword: search.value })
    users.value = (res.data.records || []).map(u => ({ ...u, role: mapRole(u.userType) }))
    total.value = res.data.total || 0
  } catch (e) { /* fallback */ }
  finally { loading.value = false }
}

async function handleResetPassword(user) {
  try {
    await ElMessageBox.confirm(
      `确定要将用户 "${user.username}" 的密码重置为 123456 吗？`,
      '重置密码确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await resetPassword(user.id)
    ElMessage.success('密码重置成功，新密码为：123456')
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '重置密码失败')
    }
  }
}

async function handleDelete(user) {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${user.username}" 吗？删除后可以恢复。`,
      '删除用户确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await deleteUser(user.id)
    ElMessage.success('用户删除成功')
    fetchUsers()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '删除用户失败')
    }
  }
}

async function handleRestore(user) {
  try {
    await ElMessageBox.confirm(
      `确定要恢复用户 "${user.username}" 吗？`,
      '恢复用户确认',
      {
        confirmButtonText: '确定恢复',
        cancelButtonText: '取消',
        type: 'success'
      }
    )
    await restoreUser(user.id)
    ElMessage.success('用户恢复成功')
    fetchUsers()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '恢复用户失败')
    }
  }
}

function tableRowClassName({ row }) {
  return row.deleted === 1 ? 'deleted-row' : ''
}

onMounted(fetchUsers)
watch([page, search], fetchUsers)
watch(search, () => { page.value = 1 })
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.header-right { display: flex; align-items: center; }
.section-card { border-radius: 12px; margin-top: 16px; }

/* 已删除用户的灰色行样式 */
:deep(.deleted-row) {
  background-color: #f5f5f5 !important;
  color: #999;
  opacity: 0.7;
}

:deep(.deleted-row:hover) {
  background-color: #eeeeee !important;
}
</style>
