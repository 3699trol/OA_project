<template>
  <div>
    <h2>候选人列表</h2>
    <el-table :data="[]" style="margin-top: 20px;">
      <el-table-column prop="name" label="姓名" />
      <el-table-column prop="position" label="投递职位" />
      <el-table-column prop="status" label="状态" />
      <el-table-column label="操作">
        <template #default>
          <el-button size="small">查看详情</el-button>
          <el-button size="small" type="success">AI匹配分析</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
