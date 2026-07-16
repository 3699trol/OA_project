<template>
  <div>
    <h2>面试任务列表</h2>
    <el-table :data="[]" style="margin-top: 20px;">
      <el-table-column prop="candidate" label="候选人" />
      <el-table-column prop="position" label="职位" />
      <el-table-column prop="status" label="状态" />
      <el-table-column prop="interviewTime" label="面试时间" />
      <el-table-column label="操作">
        <template #default>
          <el-button size="small">查看详情</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
