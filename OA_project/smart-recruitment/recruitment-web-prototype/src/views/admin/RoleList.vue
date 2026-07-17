<template>
  <div class="page">
    <div class="page-header">
      <h2>🔐 角色管理</h2>
      <el-button type="primary" icon="Plus">新增角色</el-button>
    </div>
    <el-card shadow="never" class="section-card">
      <el-table :data="roles" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="roleName" label="角色名称" width="120">
          <template #default="{ row }">
            <el-tag :type="roleColor(row.roleCode)" effect="dark" round>{{ row.roleName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="roleCode" label="角色编码" width="140" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column prop="userCount" label="用户数" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" active-color="#67C23A" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default>
            <el-button size="small" type="primary" link>编辑</el-button>
            <el-button size="small" type="warning" link>分配权限</el-button>
            <el-button size="small" type="info" link>查看用户</el-button>
            <el-button size="small" type="danger" link>删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
const roles = [
  { id: 1, roleCode: 'ADMIN', roleName: '管理员', description: '系统管理员，拥有所有权限 — 用户管理、角色管理、权限管理、系统配置查看', userCount: 1, status: 1 },
  { id: 2, roleCode: 'HR', roleName: 'HR', description: '人力资源 — 职位管理、候选人筛选、面试安排、招聘数据统计查看', userCount: 3, status: 1 },
  { id: 3, roleCode: 'INTERVIEWER', roleName: '面试官', description: '面试执行者 — 查看面试任务、AI生成面试题、填写面试评价', userCount: 5, status: 1 },
  { id: 4, roleCode: 'CANDIDATE', roleName: '求职者', description: '职位浏览、简历管理、职位投递、模拟面试', userCount: 18, status: 1 }
]

function roleColor(code) { return { 'ADMIN': 'danger', 'HR': 'warning', 'INTERVIEWER': 'primary', 'CANDIDATE': 'success' }[code] || 'info' }
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.section-card { border-radius: 12px; margin-top: 16px; }
</style>
