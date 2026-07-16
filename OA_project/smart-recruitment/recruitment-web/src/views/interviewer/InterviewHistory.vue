<template>
  <div>
    <h2>面试历史</h2>
    <el-table :data="[]" style="margin-top: 20px;">
      <el-table-column prop="candidate" label="候选人" />
      <el-table-column prop="position" label="职位" />
      <el-table-column prop="result" label="结果" />
      <el-table-column prop="date" label="日期" />
    </el-table>
  </div>
</template>
