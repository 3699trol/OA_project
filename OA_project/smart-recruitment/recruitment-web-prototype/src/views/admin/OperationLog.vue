<template>
  <div class="page">
    <div class="page-header"><h2>📋 操作日志</h2></div>
    <el-card shadow="never" class="section-card">
      <div class="toolbar">
        <el-input v-model="search" placeholder="搜索模块/操作描述" prefix-icon="Search" clearable style="width:260px;" />
        <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束" style="margin-left:12px;" />
        <el-button type="primary" icon="Search" style="margin-left:12px;">查询</el-button>
      </div>
      <el-table :data="logs" stripe style="margin-top:16px;">
        <el-table-column prop="operatorName" label="操作人" width="110" />
        <el-table-column prop="module" label="模块" width="100">
          <template #default="{ row }"><el-tag size="small" effect="plain">{{ row.module }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="description" label="操作描述" min-width="240" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column prop="costTime" label="耗时" width="80" align="center">
          <template #default="{ row }">{{ row.costTime }}ms</template>
        </el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="160" sortable />
      </el-table>
      <el-pagination v-model:current-page="page" :page-size="10" :total="156" layout="prev, pager, next, total" background style="margin-top:16px;justify-content:center;" />
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
const search = ref(''); const dateRange = ref([]); const page = ref(1)
const logs = [
  { operatorName: 'admin', module: '用户管理', description: '新增用户 zhangsan', ip: '192.168.1.100', costTime: 45, createTime: '2024-07-17 14:30' },
  { operatorName: 'lijingli', module: '职位管理', description: '发布职位：Java高级开发工程师', ip: '192.168.1.101', costTime: 32, createTime: '2024-07-17 14:20' },
  { operatorName: 'wanggong', module: '面试管理', description: '提交面试评价：张三', ip: '192.168.1.102', costTime: 128, createTime: '2024-07-17 10:15' },
  { operatorName: 'zhangsan', module: '简历管理', description: '上传简历文件 resume_v3.pdf', ip: '172.16.0.50', costTime: 520, createTime: '2024-07-17 09:45' },
  { operatorName: 'admin', module: '系统配置', description: '修改系统配置：关闭用户注册', ip: '192.168.1.100', costTime: 18, createTime: '2024-07-16 18:00' },
  { operatorName: 'lijingli', module: '候选人', description: '修改投递状态：张三 — 初筛通过', ip: '192.168.1.101', costTime: 28, createTime: '2024-07-16 16:20' },
  { operatorName: 'zhangsan', module: '投递管理', description: '投递职位：Java高级开发工程师', ip: '172.16.0.50', costTime: 65, createTime: '2024-07-15 14:30' }
]
</script>

<style scoped>
.page-header h2 { margin: 0 0 16px; font-size: 22px; color: #3E2723; }
.section-card { border-radius: 12px; }
.toolbar { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; }
</style>
