<template>
  <div style="display:flex;justify-content:center;align-items:center;height:100vh;">
    <div style="text-align:center;">
      <h1 style="font-size:80px;color:#ED8936;margin:0;">404</h1>
      <h2 style="color:#3E2723;">页面未找到</h2>
      <p style="color:#999;">您访问的页面不存在或已被移除</p>
      <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
    </div>
  </div>
</template>
