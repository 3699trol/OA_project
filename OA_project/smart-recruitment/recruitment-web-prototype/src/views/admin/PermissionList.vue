<template>
  <div class="page">
    <div class="page-header">
      <h2>🔒 权限管理</h2>
      <el-button type="primary" icon="Plus">新增权限</el-button>
    </div>
    <el-card shadow="never" class="section-card">
      <el-table :data="permissions" stripe row-key="id" default-expand-all>
        <el-table-column prop="permissionName" label="权限名称" min-width="200">
          <template #default="{ row }">
            <span :style="{ paddingLeft: (row.level - 1) * 24 + 'px' }">
              <el-icon v-if="row.children" style="margin-right:6px;"><Folder /></el-icon>
              <el-icon v-else style="margin-right:6px;color:#999;"><Key /></el-icon>
              {{ row.permissionName }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="permissionCode" label="权限编码" width="200" />
        <el-table-column prop="type" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.type === '菜单' ? 'primary' : 'success'" size="small" effect="plain">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" active-color="#67C23A" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default><el-button size="small" type="primary" link>编辑</el-button><el-button size="small" type="danger" link>删除</el-button></template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
const permissions = [
  { id: 1, permissionCode: 'system:user', permissionName: '用户管理', type: '菜单', description: '用户列表查看、新增、编辑、删除', level: 1, status: 1 },
  { id: 11, permissionCode: 'system:user:list', permissionName: '用户列表', type: '按钮', description: '查看用户列表', level: 2, status: 1 },
  { id: 12, permissionCode: 'system:user:add', permissionName: '新增用户', type: '按钮', description: '新增系统用户', level: 2, status: 1 },
  { id: 13, permissionCode: 'system:user:edit', permissionName: '编辑用户', type: '按钮', description: '编辑用户信息', level: 2, status: 1 },
  { id: 14, permissionCode: 'system:user:delete', permissionName: '删除用户', type: '按钮', description: '删除系统用户', level: 2, status: 1 },
  { id: 2, permissionCode: 'system:role', permissionName: '角色管理', type: '菜单', description: '角色列表、权限分配', level: 1, status: 1 },
  { id: 21, permissionCode: 'system:role:list', permissionName: '角色列表', type: '按钮', description: '查看角色列表', level: 2, status: 1 },
  { id: 22, permissionCode: 'system:role:assign', permissionName: '分配权限', type: '按钮', description: '为角色分配权限', level: 2, status: 1 },
  { id: 3, permissionCode: 'job', permissionName: '职位管理', type: '菜单', description: '职位发布、编辑、上下架', level: 1, status: 1 },
  { id: 31, permissionCode: 'job:add', permissionName: '发布职位', type: '按钮', description: '发布新职位', level: 2, status: 1 },
  { id: 32, permissionCode: 'job:edit', permissionName: '编辑职位', type: '按钮', description: '修改职位信息', level: 2, status: 1 },
  { id: 4, permissionCode: 'resume', permissionName: '简历管理', type: '菜单', description: '简历上传、编辑、查看', level: 1, status: 1 },
  { id: 5, permissionCode: 'interview', permissionName: '面试管理', type: '菜单', description: '面试安排、评价', level: 1, status: 1 },
  { id: 6, permissionCode: 'dashboard', permissionName: '数据统计', type: '菜单', description: '招聘数据看板', level: 1, status: 1 }
]
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.section-card { border-radius: 12px; margin-top: 16px; }
</style>
