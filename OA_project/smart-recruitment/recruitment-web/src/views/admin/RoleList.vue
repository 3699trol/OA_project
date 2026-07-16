<template>
  <div>
    <h2>角色管理</h2>
    <el-table :data="[]" style="margin-top: 20px;">
      <el-table-column prop="roleName" label="角色名称" />
      <el-table-column prop="roleCode" label="角色编码" />
      <el-table-column prop="description" label="描述" />
    </el-table>
  </div>
</template>
