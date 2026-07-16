<template>
  <div>
    <h2>欢迎使用智能招聘系统</h2>
    <el-row :gutter="20" style="margin-top: 30px;">
      <el-col :span="8">
        <el-card shadow="hover">
          <h3>浏览职位</h3>
          <p>查看最新发布的招聘信息</p>
          <el-button type="primary" @click="$router.push('/candidate/jobs')">去查看</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <h3>完善简历</h3>
          <p>填写个人信息，AI帮你优化</p>
          <el-button type="primary" @click="$router.push('/candidate/resume')">去编辑</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <h3>模拟面试</h3>
          <p>AI模拟面试，提前准备</p>
          <el-button type="primary" @click="$router.push('/candidate/mock-interview')">开始练习</el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
