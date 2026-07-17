<template>
  <div class="page">
    <div class="page-header">
      <h2>🏠 欢迎回来，{{ userStore.userInfo.username || '求职者' }}</h2>
      <p class="subtitle">智能招聘系统为您推荐最匹配的职位</p>
    </div>
    <!-- 快捷入口 -->
    <el-row :gutter="20" style="margin-top:20px;">
      <el-col :span="8" v-for="card in quickCards" :key="card.title">
        <el-card shadow="hover" :body-style="{ padding: '24px' }" class="quick-card" @click="$router.push(card.path)">
          <div class="card-inner">
            <div>
              <h3>{{ card.title }}</h3>
              <p>{{ card.desc }}</p>
            </div>
            <div class="card-icon" :style="{ background: card.bg }">
              <el-icon size="28"><component :is="card.icon" /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <!-- 推荐职位 -->
    <h3 class="section-title">🔥 为您推荐</h3>
    <el-row :gutter="20">
      <el-col :span="8" v-for="job in recommendJobs" :key="job.id">
        <el-card shadow="hover" class="job-card" @click="$router.push(`/candidate/jobs/${job.id}`)">
          <div class="job-top">
            <h4>{{ job.title }}</h4>
            <span class="salary">{{ job.salary }}</span>
          </div>
          <div class="job-tags">
            <el-tag size="small" v-for="t in job.tags" :key="t" :type="t.type">{{ t.label }}</el-tag>
          </div>
          <div class="job-meta">
            <span><el-icon><Location /></el-icon> {{ job.location }}</span>
            <span><el-icon><School /></el-icon> {{ job.education }}</span>
            <span>{{ job.match }} 匹配</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <!-- 最近浏览 -->
    <h3 class="section-title">🕐 最近浏览</h3>
    <el-table :data="recentViews" style="width:100%;">
      <el-table-column prop="title" label="职位名称" />
      <el-table-column prop="company" label="公司" />
      <el-table-column prop="location" label="地点" width="120" />
      <el-table-column prop="time" label="浏览时间" width="160" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button type="primary" link @click="$router.push(`/candidate/jobs/${row.id}`)">查看详情</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user'
const userStore = useUserStore()

const quickCards = [
  { title: '浏览职位', desc: '查看最新招聘信息', icon: 'Briefcase', path: '/candidate/jobs', bg: '#e8f4fd' },
  { title: '我的简历', desc: '完善个人信息，AI帮你优化', icon: 'Document', path: '/candidate/resume', bg: '#fdf4e8' },
  { title: '模拟面试', desc: 'AI模拟面试，提前准备', icon: 'ChatDotRound', path: '/candidate/mock-interview', bg: '#e8f8e8' }
]

const recommendJobs = [
  { id: 1, title: 'Java高级开发工程师', salary: '15K-25K', location: '北京·朝阳区', education: '本科', match: '92%', tags: [{ label: '急聘', type: 'danger' }, { label: 'Spring Boot', type: '' }, { label: '微服务', type: 'success' }] },
  { id: 2, title: '前端开发工程师（Vue）', salary: '12K-20K', location: '上海·浦东新区', education: '本科', match: '88%', tags: [{ label: 'Vue3', type: '' }, { label: 'TypeScript', type: 'success' }] },
  { id: 3, title: '数据分析师', salary: '10K-18K', location: '深圳·南山区', education: '本科', match: '85%', tags: [{ label: 'Python', type: '' }, { label: 'SQL', type: 'success' }, { label: '新发布', type: 'warning' }] },
  { id: 4, title: '产品经理', salary: '14K-22K', location: '杭州·余杭区', education: '本科', match: '79%', tags: [{ label: 'B端', type: '' }, { label: 'SaaS', type: 'success' }] },
  { id: 5, title: 'DevOps工程师', salary: '18K-28K', location: '北京·海淀区', education: '本科', match: '76%', tags: [{ label: 'K8s', type: '' }, { label: 'CI/CD', type: 'success' }] },
  { id: 6, title: 'UI/UX设计师', salary: '11K-19K', location: '广州·天河区', education: '大专', match: '73%', tags: [{ label: 'Figma', type: '' }, { label: '设计系统', type: '' }] }
]

const recentViews = [
  { id: 1, title: 'Java高级开发工程师', company: '某科技有限公司', location: '北京', time: '2024-07-17 14:30' },
  { id: 3, title: '数据分析师', company: '某数据集团', location: '深圳', time: '2024-07-17 10:15' },
  { id: 7, title: '全栈工程师', company: '某互联网公司', location: '成都', time: '2024-07-16 16:45' }
]
</script>

<style scoped>
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.subtitle { color: #999; margin: 6px 0 0; font-size: 14px; }
.quick-card { cursor: pointer; border-radius: 12px; transition: all 0.2s; }
.quick-card:hover { transform: translateY(-2px); box-shadow: 0 4px 16px rgba(0,0,0,0.08); }
.card-inner { display: flex; justify-content: space-between; align-items: center; }
.card-inner h3 { margin: 0 0 6px; font-size: 17px; color: #3E2723; }
.card-inner p { margin: 0; font-size: 13px; color: #999; }
.card-icon { width: 52px; height: 52px; border-radius: 12px; display: flex; align-items: center; justify-content: center; color: #ED8936; }
.section-title { margin: 28px 0 16px; font-size: 17px; color: #3E2723; }
.job-card { cursor: pointer; border-radius: 12px; margin-bottom: 16px; transition: all 0.2s; }
.job-card:hover { transform: translateY(-2px); box-shadow: 0 4px 16px rgba(0,0,0,0.08); }
.job-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.job-top h4 { margin: 0; font-size: 16px; color: #3E2723; }
.salary { color: #E76F51; font-weight: 700; font-size: 15px; }
.job-tags { display: flex; gap: 6px; margin-bottom: 10px; flex-wrap: wrap; }
.job-meta { display: flex; gap: 18px; font-size: 13px; color: #999; }
.job-meta span { display: flex; align-items: center; gap: 3px; }
</style>
