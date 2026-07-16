<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center;">
      <h2>用户管理</h2>
      <el-button type="primary">新增用户</el-button>
    </div>
    <el-table :data="[]" style="margin-top: 20px;">
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="realName" label="真实姓名" />
      <el-table-column prop="role" label="角色" />
      <el-table-column prop="status" label="状态" />
      <el-table-column label="操作">
        <template #default>
          <el-button size="small">编辑</el-button>
          <el-button size="small" type="danger">禁用</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
