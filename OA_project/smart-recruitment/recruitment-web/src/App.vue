<template>
  <router-view />
</template>

<script setup>
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { connectWebSocket } from '@/utils/websocket'

onMounted(() => {
  const userStore = useUserStore()
  if (userStore.token && userStore.userInfo && userStore.userInfo.username) {
    connectWebSocket(userStore.userInfo.username)
  }
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}

/* ===== Element Plus 暖橙色主题覆盖 ===== */
:root {
  --el-color-primary: #ED8936;
  --el-color-primary-light-1: #f0a35e;
  --el-color-primary-light-2: #f3b87a;
  --el-color-primary-light-3: #f5c896;
  --el-color-primary-light-4: #f8d8b2;
  --el-color-primary-light-5: #fae4cc;
  --el-color-primary-light-6: #fceed9;
  --el-color-primary-light-7: #fdf4e8;
  --el-color-primary-light-8: #fef9f3;
  --el-color-primary-light-9: #fffcf8;
  --el-color-primary-dark-1: #d97b2e;
  --el-color-primary-dark-2: #c46d26;
  --el-color-primary-dark-3: #a85c1e;
  --el-color-primary-dark-4: #8c4b16;

  --el-color-success: #67C23A;
  --el-color-warning: #E6A23C;
  --el-color-danger: #F56C6C;
  --el-color-info: #909399;
}
</style>
