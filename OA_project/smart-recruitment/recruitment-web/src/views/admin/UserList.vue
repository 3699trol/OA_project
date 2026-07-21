<template>
  <div class="page">
    <div class="page-header">
      <h2>👥 用户管理</h2>
      <el-button type="primary" icon="Plus">新增用户</el-button>
    </div>

    <div class="filter-bar">
      <div class="keyword-search">
        <el-select v-model="filters.searchField" class="field-select" aria-label="搜索字段">
          <el-option label="全部字段" value="all" />
          <el-option label="用户名" value="username" />
          <el-option label="姓名" value="realName" />
          <el-option label="手机号" value="phone" />
          <el-option label="邮箱" value="email" />
        </el-select>
        <el-input
          v-model="filters.keyword"
          :placeholder="searchPlaceholder"
          clearable
          class="keyword-input"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
      </div>

      <el-select v-model="filters.userType" placeholder="全部角色" clearable class="filter-select">
        <el-option label="管理员" :value="1" />
        <el-option label="HR" :value="2" />
        <el-option label="面试官" :value="3" />
        <el-option label="求职者" :value="4" />
      </el-select>

      <el-select v-model="filters.status" placeholder="全部账号状态" clearable class="filter-select">
        <el-option label="正常" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>

      <el-select v-model="filters.deleted" placeholder="全部删除状态" clearable class="filter-select">
        <el-option label="未删除" :value="0" />
        <el-option label="已删除" :value="1" />
      </el-select>

      <div class="filter-actions">
        <el-button type="primary" icon="Search" :loading="loading" @click="handleSearch">查询</el-button>
        <el-button icon="RefreshRight" @click="handleReset">重置</el-button>
      </div>
    </div>

    <el-card shadow="never" class="section-card">
      <el-table
        :data="users"
        v-loading="loading"
        stripe
        empty-text="暂无符合条件的用户"
        :row-class-name="tableRowClassName"
      >
        <el-table-column prop="username" label="用户名" width="110" />
        <el-table-column prop="realName" label="姓名" width="90" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }"><el-tag :type="roleType(row.role)" size="small">{{ row.role }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="160" />
        <el-table-column prop="status" label="账号状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" sortable />
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
      <el-pagination
        v-model:current-page="page"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next, total"
        background
        class="pagination"
      />
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, resetPassword, deleteUser, restoreUser } from '@/api/user'

const pageSize = 10
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const users = ref([])
const filters = reactive({
  searchField: 'all',
  keyword: '',
  userType: null,
  status: null,
  deleted: null
})

const userTypeMap = { 1: '管理员', 2: 'HR', 3: '面试官', 4: '求职者' }
const searchPlaceholderMap = {
  all: '搜索用户名、姓名、手机号或邮箱',
  username: '请输入用户名',
  realName: '请输入姓名',
  phone: '请输入手机号',
  email: '请输入邮箱'
}

const searchPlaceholder = computed(() => searchPlaceholderMap[filters.searchField])

function roleType(r) { return { '管理员': 'danger', 'HR': 'warning', '面试官': 'primary', '求职者': 'success' }[r] || 'info' }
function mapRole(userType) { return userTypeMap[userType] || '未知' }

async function fetchUsers() {
  loading.value = true
  try {
    const res = await getUserList({
      page: page.value,
      size: pageSize,
      searchField: filters.searchField,
      keyword: filters.keyword.trim(),
      userType: filters.userType,
      status: filters.status,
      deleted: filters.deleted
    })
    users.value = (res.data.records || []).map(u => ({ ...u, role: mapRole(u.userType) }))
    total.value = res.data.total || 0
  } catch (e) {
    users.value = []
    total.value = 0
    ElMessage.error(e.message || '用户列表加载失败')
  }
  finally { loading.value = false }
}

function handleSearch() {
  if (page.value !== 1) {
    page.value = 1
    return
  }
  fetchUsers()
}

function handleReset() {
  filters.searchField = 'all'
  filters.keyword = ''
  filters.userType = null
  filters.status = null
  filters.deleted = null
  handleSearch()
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
watch(page, fetchUsers)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.filter-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 16px;
  padding: 14px 0;
  border-top: 1px solid #ebeef5;
}
.keyword-search { display: flex; min-width: 360px; flex: 1 1 420px; }
.field-select { width: 120px; flex: 0 0 120px; }
.keyword-input { min-width: 220px; }
.filter-select { width: 145px; }
.filter-actions { display: flex; align-items: center; }
.section-card { border-radius: 8px; }
.pagination { margin-top: 16px; justify-content: center; }

.keyword-search :deep(.field-select .el-select__wrapper) {
  border-radius: 4px 0 0 4px;
}

.keyword-search :deep(.keyword-input .el-input__wrapper) {
  border-radius: 0 4px 4px 0;
  margin-left: -1px;
}

/* 已删除用户的灰色行样式 */
:deep(.deleted-row) {
  background-color: #f5f5f5 !important;
  color: #999;
  opacity: 0.7;
}

:deep(.deleted-row:hover) {
  background-color: #eeeeee !important;
}

@media (max-width: 768px) {
  .page-header { align-items: flex-start; }
  .filter-bar { align-items: stretch; }
  .keyword-search { min-width: 100%; flex-basis: 100%; }
  .filter-select { flex: 1 1 calc(50% - 5px); width: auto; }
  .filter-actions { width: 100%; }
  .filter-actions .el-button { flex: 1; }
}
</style>
