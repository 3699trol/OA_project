<template>
  <div>
    <h2>我的投递</h2>
    <el-table :data="[]" style="margin-top: 20px;">
      <el-table-column prop="jobTitle" label="职位名称" />
      <el-table-column prop="status" label="状态" />
      <el-table-column prop="createTime" label="投递时间" />
    </el-table>
  </div>
</template>
