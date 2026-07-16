<template>
  <div>
    <h2>HR工作台</h2>
    <el-row :gutter="20" style="margin-top: 30px;">
      <el-col :span="6"><el-card><el-statistic title="职位数量" :value="0" /></el-card></el-col>
      <el-col :span="6"><el-card><el-statistic title="投递人数" :value="0" /></el-card></el-col>
      <el-col :span="6"><el-card><el-statistic title="待筛选" :value="0" /></el-card></el-col>
      <el-col :span="6"><el-card><el-statistic title="面试中" :value="0" /></el-card></el-col>
    </el-row>
  </div>
</template>
