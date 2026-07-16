<template>
  <div>
    <h2>操作日志</h2>
    <el-table :data="[]" style="margin-top: 20px;">
      <el-table-column prop="operator" label="操作人" />
      <el-table-column prop="module" label="模块" />
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="createTime" label="时间" />
    </el-table>
  </div>
</template>
