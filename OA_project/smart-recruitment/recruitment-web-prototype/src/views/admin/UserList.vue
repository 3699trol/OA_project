<template>
  <div class="page">
    <div class="page-header">
      <h2>👥 用户管理</h2>
      <div class="header-right">
        <el-input v-model="search" placeholder="搜索用户名/姓名" prefix-icon="Search" clearable style="width:220px;margin-right:10px;" />
        <el-select v-model="statusFilter" placeholder="状态" clearable style="width:120px;margin-right:10px;">
          <el-option label="启用" :value="1" /><el-option label="禁用" :value="0" />
        </el-select>
        <el-button type="primary" icon="Plus">新增用户</el-button>
      </div>
    </div>
    <el-card shadow="never" class="section-card">
      <el-table :data="users" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户名" width="110">
          <template #default="{ row }">
            <el-avatar :size="28" style="vertical-align:middle;margin-right:6px;">{{ row.realName[0] }}</el-avatar>
            {{ row.username }}
          </template>
        </el-table-column>
        <el-table-column prop="realName" label="真实姓名" width="90" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="roleType(row.role)" size="small">{{ row.role }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="160" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" active-color="#67C23A" inactive-color="#F56C6C" size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="130" sortable />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link>编辑</el-button>
            <el-button size="small" type="warning" link>重置密码</el-button>
            <el-button size="small" type="danger" link>删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" :page-size="10" :total="25" layout="prev, pager, next, total" background style="margin-top:16px;justify-content:center;" />
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const search = ref('')
const statusFilter = ref(null)
const page = ref(1)

const users = [
  { id: 1, username: 'admin', realName: '系统管理员', role: '管理员', email: 'admin@company.com', phone: '138****0000', status: 1, createTime: '2024-07-01' },
  { id: 2, username: 'lijingli', realName: '李经理', role: 'HR', email: 'lijingli@company.com', phone: '138****0001', status: 1, createTime: '2024-07-02' },
  { id: 3, username: 'wanggong', realName: '王工', role: '面试官', email: 'wanggong@company.com', phone: '138****0002', status: 1, createTime: '2024-07-02' },
  { id: 4, username: 'zhangsan', realName: '张三', role: '求职者', email: 'zhangsan@example.com', phone: '138****6789', status: 1, createTime: '2024-07-03' },
  { id: 5, username: 'lisi', realName: '李四', role: '求职者', email: 'lisi@example.com', phone: '139****8901', status: 1, createTime: '2024-07-04' },
  { id: 6, username: 'liugong', realName: '刘工', role: '面试官', email: 'liugong@company.com', phone: '138****0004', status: 0, createTime: '2024-07-05' },
  { id: 7, username: 'wangwu', realName: '王五', role: '求职者', email: 'wangwu@example.com', phone: '137****7890', status: 1, createTime: '2024-07-06' },
  { id: 8, username: 'zhaoliu', realName: '赵六', role: '求职者', email: 'zhaoliu@example.com', phone: '136****5678', status: 1, createTime: '2024-07-07' }
]

function roleType(r) { return { '管理员': 'danger', 'HR': 'warning', '面试官': 'primary', '求职者': 'success' }[r] || 'info' }
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.header-right { display: flex; align-items: center; }
.section-card { border-radius: 12px; margin-top: 16px; }
</style>
