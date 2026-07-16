<template>
  <div>
    <h2>面试官工作台</h2>
    <el-row :gutter="20" style="margin-top: 30px;">
      <el-col :span="8"><el-card><el-statistic title="待面试" :value="0" /></el-card></el-col>
      <el-col :span="8"><el-card><el-statistic title="已完成" :value="0" /></el-card></el-col>
      <el-col :span="8"><el-card><el-statistic title="本月面试" :value="0" /></el-card></el-col>
    </el-row>
  </div>
</template>
