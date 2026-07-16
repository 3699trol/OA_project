<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center;">
      <h2>职位管理</h2>
      <el-button type="primary" @click="$router.push('/hr/jobs/edit')">新增职位</el-button>
    </div>
    <el-table :data="[]" style="margin-top: 20px;">
      <el-table-column prop="title" label="职位名称" />
      <el-table-column prop="status" label="状态" />
      <el-table-column prop="headcount" label="招聘人数" />
      <el-table-column label="操作">
        <template #default>
          <el-button size="small">编辑</el-button>
          <el-button size="small" type="success">发布</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
