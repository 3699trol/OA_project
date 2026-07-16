<template>
  <div>
    <h2>职位列表</h2>
    <el-table :data="[]" style="margin-top: 20px;">
      <el-table-column prop="title" label="职位名称" />
      <el-table-column prop="category" label="分类" />
      <el-table-column prop="location" label="工作地点" />
      <el-table-column label="操作">
        <template #default>
          <el-button type="primary" size="small">查看详情</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
