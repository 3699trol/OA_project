<template>
  <div class="page">
    <div class="page-header">
      <h2>👥 候选人管理</h2>
      <div class="header-right">
        <el-input v-model="searchKeyword" placeholder="搜索姓名/技能" prefix-icon="Search" clearable style="width:240px;margin-right:12px;" />
        <el-select v-model="filterStatus" placeholder="筛选状态" clearable style="width:140px;">
          <el-option label="初筛中" value="初筛中" /><el-option label="面试中" value="面试中" /><el-option label="已录用" value="已录用" />
        </el-select>
      </div>
    </div>
    <el-card shadow="never" class="section-card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="全部候选人 (47)" name="all" />
        <el-tab-pane label="高匹配 (15)" name="highMatch" />
        <el-tab-pane label="待初筛 (22)" name="screening" />
        <el-tab-pane label="面试中 (8)" name="interviewing" />
      </el-tabs>
      <el-table :data="candidates" style="width:100%;" stripe>
        <el-table-column prop="name" label="姓名" width="80" />
        <el-table-column prop="jobTitle" label="投递职位" min-width="160" />
        <el-table-column label="AI匹配度" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.match >= 80 ? 'success' : row.match >= 60 ? 'warning' : 'info'">{{ row.match }}%</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="education" label="学历" width="80" />
        <el-table-column prop="experience" label="经验" width="90" />
        <el-table-column label="核心技能" min-width="180">
          <template #default="{ row }">
            <el-tag v-for="s in row.skills.split(',').slice(0,4)" :key="s" size="small" effect="plain" round style="margin-right:4px;">{{ s }}</el-tag>
            <el-tag v-if="row.skills.split(',').length > 4" size="small" type="info">+{{ row.skills.split(',').length - 4 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small" effect="plain">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="投递时间" width="110" sortable />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="$router.push('/hr/candidates/detail')">简历详情</el-button>
            <el-button size="small" type="success" link @click="$router.push('/hr/match')">AI分析</el-button>
            <el-button v-if="row.status === '初筛中'" size="small" type="warning" link @click="$router.push('/hr/interviews/create')">安排面试</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" :page-size="10" :total="47" layout="prev, pager, next, total" background style="margin-top:16px;justify-content:center;" />
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const activeTab = ref('all')
const searchKeyword = ref('')
const filterStatus = ref('')
const page = ref(1)

const candidates = [
  { id: 1, name: '张三', jobTitle: 'Java高级开发工程师', match: 92, education: '本科', experience: '5年', skills: 'Java,Spring Boot,Spring Cloud,MySQL,Redis,Docker,Linux', status: '初筛中', applyTime: '2024-07-17' },
  { id: 2, name: '李四', jobTitle: '前端开发工程师（Vue）', match: 88, education: '本科', experience: '3年', skills: 'Vue3,TypeScript,Element Plus,Vite,Pinia', status: '初筛中', applyTime: '2024-07-16' },
  { id: 3, name: '王五', jobTitle: '数据分析师', match: 85, education: '硕士', experience: '2年', skills: 'Python,SQL,Tableau,机器学习,Spark', status: '面试中', applyTime: '2024-07-15' },
  { id: 4, name: '赵六', jobTitle: 'DevOps工程师', match: 91, education: '本科', experience: '4年', skills: 'Docker,Kubernetes,Jenkins,Terraform,AWS,Linux', status: '已录用', applyTime: '2024-07-14' },
  { id: 5, name: '陈七', jobTitle: '产品经理', match: 79, education: '本科', experience: '4年', skills: '产品规划,PRD,Axure,数据分析,敏捷', status: '初筛中', applyTime: '2024-07-13' },
  { id: 6, name: '孙八', jobTitle: 'UI设计师', match: 72, education: '大专', experience: '2年', skills: 'Figma,Sketch,PS,AI,设计系统', status: '面试中', applyTime: '2024-07-12' },
  { id: 7, name: '周九', jobTitle: '测试开发工程师', match: 83, education: '本科', experience: '3年', skills: 'Selenium,JMeter,Python,CI/CD,自动化', status: '初筛中', applyTime: '2024-07-11' },
  { id: 8, name: '吴十', jobTitle: '全栈工程师', match: 75, education: '本科', experience: '4年', skills: 'Node.js,React,MongoDB,AWS,GraphQL', status: '不合适', applyTime: '2024-07-10' }
]

function statusType(s) { return { '初筛中': 'warning', '面试中': 'primary', '已录用': 'success', '不合适': 'info' }[s] || '' }
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.header-right { display: flex; gap: 8px; }
.section-card { border-radius: 12px; margin-top: 16px; }
</style>
