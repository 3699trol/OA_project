<template>
  <div>
    <h2>权限管理</h2>
    <el-table :data="[]" style="margin-top: 20px;">
      <el-table-column prop="permissionName" label="权限名称" />
      <el-table-column prop="permissionCode" label="权限编码" />
      <el-table-column prop="description" label="描述" />
    </el-table>
  </div>
</template>
