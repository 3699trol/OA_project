<template>
  <div class="page">
    <div class="page-header">
      <h2>💼 职位列表</h2>
      <p class="subtitle">共找到 <b>{{ totalJobs }}</b> 个职位</p>
    </div>
    <!-- 搜索筛选 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="filters" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="filters.keyword" placeholder="职位名称/技能" clearable prefix-icon="Search" style="width:240px;" />
        </el-form-item>
        <el-form-item label="工作地点">
          <el-select v-model="filters.location" placeholder="不限" clearable style="width:150px;">
            <el-option label="北京" value="北京" /><el-option label="上海" value="上海" />
            <el-option label="深圳" value="深圳" /><el-option label="杭州" value="杭州" />
            <el-option label="广州" value="广州" /><el-option label="成都" value="成都" />
          </el-select>
        </el-form-item>
        <el-form-item label="职位类别">
          <el-select v-model="filters.category" placeholder="不限" clearable style="width:150px;">
            <el-option label="技术研发" value="技术研发" /><el-option label="产品设计" value="产品设计" />
            <el-option label="数据科学" value="数据科学" /><el-option label="运营市场" value="运营市场" />
          </el-select>
        </el-form-item>
        <el-form-item label="薪资范围">
          <el-select v-model="filters.salary" placeholder="不限" clearable style="width:150px;">
            <el-option label="10K以下" value="0-10" /><el-option label="10K-20K" value="10-20" />
            <el-option label="20K-30K" value="20-30" /><el-option label="30K以上" value="30+" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="doSearch">搜索</el-button>
          <el-button icon="RefreshRight" @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <!-- 排序 -->
    <div class="sort-bar">
      <span>排序：</span>
      <el-radio-group v-model="sortBy" size="small">
        <el-radio-button value="default">综合排序</el-radio-button>
        <el-radio-button value="time">最新发布</el-radio-button>
        <el-radio-button value="salary">薪资最高</el-radio-button>
        <el-radio-button value="match">匹配度优先</el-radio-button>
      </el-radio-group>
    </div>
    <!-- 职位列表 -->
    <div class="job-list">
      <el-card v-for="job in jobList" :key="job.id" shadow="hover" class="job-item" @click="$router.push(`/candidate/jobs/${job.id}`)">
        <div class="job-main">
          <div class="job-info">
            <div class="job-title-row">
              <h4>{{ job.title }}</h4>
              <el-tag v-if="job.isUrgent" type="danger" size="small" effect="dark">急聘</el-tag>
              <el-tag v-if="job.isNew" type="warning" size="small" effect="dark">新</el-tag>
            </div>
            <div class="job-company">{{ job.company }}</div>
            <div class="job-conditions">
              <span><el-icon><Location /></el-icon> {{ job.location }}</span>
              <span><el-icon><School /></el-icon> {{ job.education }}</span>
              <span><el-icon><Clock /></el-icon> {{ job.experience }}</span>
            </div>
            <div class="job-skill-tags">
              <el-tag v-for="t in job.skills" :key="t" size="small" effect="plain" round>{{ t }}</el-tag>
            </div>
          </div>
          <div class="job-right">
            <div class="job-salary">{{ job.salary }}</div>
            <div class="job-match" v-if="job.matchScore">
              <el-progress :percentage="job.matchScore" :stroke-width="8" :color="matchColor(job.matchScore)" />
              <span>匹配度 {{ job.matchScore }}%</span>
            </div>
            <div class="job-time">{{ job.postTime }}</div>
          </div>
        </div>
      </el-card>
    </div>
    <!-- 分页 -->
    <el-pagination
      v-model:current-page="page" :page-size="10" :total="totalJobs"
      layout="prev, pager, next, total" background
      style="margin-top:24px; justify-content:center;" />
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

const page = ref(1)
const totalJobs = ref(47)
const sortBy = ref('default')
const filters = reactive({ keyword: '', location: '', category: '', salary: '' })

function matchColor(score) { return score >= 85 ? '#67C23A' : score >= 70 ? '#E6A23C' : '#909399' }

const jobList = [
  { id: 1, title: 'Java高级开发工程师', company: '某科技有限公司', location: '北京·朝阳区', education: '本科', experience: '3-5年', salary: '15K-25K', matchScore: 92, isUrgent: true, isNew: false, postTime: '2小时前', skills: ['Java', 'Spring Boot', '微服务', 'MySQL', 'Redis', 'Docker'] },
  { id: 2, title: '前端开发工程师（Vue）', company: '某网络技术有限公司', location: '上海·浦东新区', education: '本科', experience: '2-4年', salary: '12K-20K', matchScore: 88, isUrgent: false, isNew: true, postTime: '5小时前', skills: ['Vue3', 'TypeScript', 'Element Plus', 'Vite', 'Pinia'] },
  { id: 3, title: '数据分析师', company: '某数据科技集团', location: '深圳·南山区', education: '本科', experience: '1-3年', salary: '10K-18K', matchScore: 85, isUrgent: false, isNew: false, postTime: '1天前', skills: ['Python', 'SQL', 'Tableau', 'Excel', '机器学习'] },
  { id: 4, title: '产品经理（B端SaaS）', company: '某软件有限公司', location: '杭州·余杭区', education: '本科', experience: '3-5年', salary: '14K-22K', matchScore: 79, isUrgent: false, isNew: false, postTime: '1天前', skills: ['产品规划', 'PRD', 'Axure', '数据分析', '敏捷开发'] },
  { id: 5, title: 'DevOps工程师', company: '某云计算公司', location: '北京·海淀区', education: '本科', experience: '3-5年', salary: '18K-28K', matchScore: 76, isUrgent: true, isNew: false, postTime: '2天前', skills: ['Docker', 'Kubernetes', 'Jenkins', 'Terraform', 'Linux'] },
  { id: 6, title: 'UI/UX设计师', company: '某互联网集团', location: '广州·天河区', education: '大专', experience: '1-3年', salary: '11K-19K', matchScore: 73, isUrgent: false, isNew: true, postTime: '2天前', skills: ['Figma', 'Sketch', '设计系统', '用户研究', '交互设计'] },
  { id: 7, title: '全栈工程师（Node.js）', company: '某创业公司', location: '成都·高新区', education: '本科', experience: '2-4年', salary: '13K-22K', matchScore: 70, isUrgent: false, isNew: false, postTime: '3天前', skills: ['Node.js', 'React', 'MongoDB', 'AWS', 'GraphQL'] },
  { id: 8, title: '测试开发工程师', company: '某金融科技公司', location: '北京·西城区', education: '本科', experience: '3-5年', salary: '16K-24K', matchScore: 65, isUrgent: false, isNew: false, postTime: '3天前', skills: ['自动化测试', 'Selenium', 'Python', '性能测试', 'CI/CD'] }
]

function doSearch() { /* mock */ }
function resetFilters() { filters.keyword = ''; filters.location = ''; filters.category = ''; filters.salary = '' }
</script>

<style scoped>
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.subtitle { color: #999; margin: 6px 0 0; font-size: 14px; }
.subtitle b { color: #ED8936; }
.search-card { margin-top: 16px; border-radius: 12px; background: #fafafa; border: none; }
.search-form { margin-bottom: 0; }
.sort-bar { display: flex; align-items: center; gap: 12px; margin: 16px 0; font-size: 14px; color: #666; }
.job-list { display: flex; flex-direction: column; gap: 12px; }
.job-item { cursor: pointer; border-radius: 12px; transition: all 0.2s; }
.job-item:hover { transform: translateY(-1px); box-shadow: 0 4px 20px rgba(0,0,0,0.08); }
.job-main { display: flex; justify-content: space-between; }
.job-title-row { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.job-title-row h4 { margin: 0; font-size: 17px; color: #3E2723; }
.job-company { color: #666; font-size: 13px; margin-bottom: 8px; }
.job-conditions { display: flex; gap: 18px; font-size: 13px; color: #999; margin-bottom: 8px; }
.job-conditions span { display: flex; align-items: center; gap: 3px; }
.job-skill-tags { display: flex; flex-wrap: wrap; gap: 6px; }
.job-right { text-align: right; flex-shrink: 0; min-width: 140px; }
.job-salary { font-size: 18px; font-weight: 700; color: #E76F51; margin-bottom: 8px; }
.job-match span { font-size: 12px; color: #999; display: block; margin-top: 2px; }
.job-time { font-size: 12px; color: #bbb; margin-top: 6px; }
</style>
