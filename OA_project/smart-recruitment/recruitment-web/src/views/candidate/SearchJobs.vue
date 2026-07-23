<template>
  <div class="page">
    <div class="page-header">
      <h2>🔍 职位全文搜索</h2>
      <p class="header-desc">基于 Elasticsearch 多字段智能匹配，支持职位名称、描述、技能要求等全文检索</p>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索职位名称、技能要求、工作描述..."
          prefix-icon="Search"
          clearable
          size="large"
          class="search-input"
          @keyup.enter="doSearch"
          @clear="doSearch"
        />
        <el-button type="primary" size="large" icon="Search" @click="doSearch" :loading="loading">搜索</el-button>
      </div>
    </el-card>

    <!-- 搜索结果 -->
    <div v-loading="loading" class="result-area">
      <div v-if="!hasSearched" class="empty-hint">
        <el-empty description="输入关键词开始搜索职位" :image-size="120" />
      </div>

      <div v-else-if="jobList.length === 0" class="empty-hint">
        <el-empty description="未找到匹配的职位，试试其他关键词吧" :image-size="120" />
      </div>

      <div v-else>
        <div class="result-meta">
          <span>找到约 <strong>{{ total }}</strong> 个相关职位</span>
        </div>

        <el-card
          v-for="job in jobList"
          :key="job.id"
          class="job-card"
          shadow="hover"
          @click="$router.push(`/candidate/jobs/${job.id}`)"
        >
          <div class="job-card-body">
            <div class="job-main">
              <h3 class="job-name" v-html="highlightText(job.jobName, 'jobName')"></h3>
              <div class="job-tags">
                <el-tag size="small" type="info" effect="plain">{{ job.department || '未指定部门' }}</el-tag>
                <el-tag size="small" v-if="job.city">{{ job.city }}</el-tag>
                <el-tag size="small" v-if="job.category" type="warning" effect="plain">{{ job.category }}</el-tag>
                <el-tag size="small" v-if="job.education" type="success" effect="plain">{{ job.education }}</el-tag>
                <el-tag size="small" v-if="job.experience">{{ job.experience }}</el-tag>
              </div>
              <div class="job-salary" v-if="job.salaryMin != null && job.salaryMax != null">
                {{ job.salaryMin }}K - {{ job.salaryMax }}K
              </div>
            </div>
            <div class="job-desc">
              <p v-if="hasHighlight('description')" v-html="highlightText(job.description, 'description')" class="desc-text"></p>
              <p v-else-if="job.description" class="desc-text">{{ job.description }}</p>
              <p v-if="hasHighlight('requirements')" v-html="highlightText(job.requirements, 'requirements')" class="req-text"></p>
              <p v-else-if="job.requirements" class="req-text">{{ job.requirements }}</p>
            </div>
          </div>
          <div class="job-card-footer">
            <span class="publish-time">发布于 {{ formatTime(job.publishTime || job.createTime) }}</span>
            <el-button type="primary" link>查看详情 &rarr;</el-button>
          </div>
        </el-card>

        <el-pagination
          v-model:current-page="page"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next, total"
          background
          class="pagination"
          @current-change="doSearch"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { searchJob } from '@/api/search'
import { ElMessage } from 'element-plus'

const keyword = ref('')
const jobList = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = 10
const total = ref(0)
const hasSearched = ref(false)

// 存储原始高亮数据
const highlightMap = ref({})

async function doSearch() {
  if (!keyword.value.trim()) {
    jobList.value = []
    total.value = 0
    hasSearched.value = false
    highlightMap.value = {}
    return
  }

  loading.value = true
  hasSearched.value = true
  try {
    const res = await searchJob({
      keyword: keyword.value.trim(),
      page: page.value,
      size: pageSize
    })
    if (res && res.data) {
      jobList.value = res.data.records || []
      total.value = res.data.total || 0
      // 收集每条结果的高亮数据
      highlightMap.value = {}
      jobList.value.forEach((job, idx) => {
        if (job.highlights) {
          highlightMap.value[idx] = job.highlights
        }
      })
    }
  } catch (error) {
    ElMessage.error(error.message || '搜索失败，请检查 Elasticsearch 服务是否启动')
    jobList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

/** 判断某个字段是否有高亮片段 */
function hasHighlight(field) {
  // 检查任意一条结果是否有该字段的高亮
  return Object.values(highlightMap.value).some(hl => hl[field] && hl[field].length > 0)
}

/** 对指定字段应用高亮渲染 */
function highlightText(originalText, field) {
  if (!originalText) return ''
  // 查找第一条有该字段高亮的结果
  const firstHighlight = Object.values(highlightMap.value).find(hl => hl[field] && hl[field].length > 0)
  if (firstHighlight && firstHighlight[field] && firstHighlight[field].length > 0) {
    // ES 默认用 <em> 标签包裹高亮词
    return firstHighlight[field][0]
  }
  return originalText
}

function formatTime(timeStr) {
  if (!timeStr) return '-'
  return timeStr.replace('T', ' ').substring(0, 19)
}
</script>

<style scoped>
.page-header {
  margin-bottom: 20px;
}
.page-header h2 {
  margin: 0 0 6px;
  font-size: 22px;
  color: #3E2723;
}
.header-desc {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

.search-card {
  border-radius: 12px;
  background: #fafafa;
  border: none;
  margin-bottom: 20px;
}
.search-bar {
  display: flex;
  gap: 12px;
  align-items: center;
}
.search-input {
  flex: 1;
}

.result-area {
  min-height: 200px;
}
.empty-hint {
  padding: 40px 0;
}
.result-meta {
  margin-bottom: 16px;
  font-size: 13px;
  color: #909399;
}

.job-card {
  margin-bottom: 14px;
  border-radius: 10px;
  cursor: pointer;
  transition: transform 0.15s, box-shadow 0.15s;
}
.job-card:hover {
  transform: translateY(-2px);
}
.job-card-body {
  display: flex;
  gap: 24px;
}
.job-main {
  flex: 0 0 280px;
}
.job-name {
  margin: 0 0 10px;
  font-size: 17px;
  color: #2d3748;
  line-height: 1.4;
}
.job-name :deep(em) {
  color: #e76f51;
  font-style: normal;
  font-weight: 700;
  background: #fff3e0;
  padding: 1px 4px;
  border-radius: 3px;
}
.job-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 10px;
}
.job-salary {
  font-size: 18px;
  font-weight: 700;
  color: #e76f51;
}
.job-desc {
  flex: 1;
  min-width: 0;
}
.desc-text, .req-text {
  margin: 0 0 8px;
  font-size: 13px;
  color: #606266;
  line-height: 1.7;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.desc-text :deep(em), .req-text :deep(em) {
  color: #e76f51;
  font-style: normal;
  font-weight: 600;
  background: #fff3e0;
  padding: 1px 3px;
  border-radius: 3px;
}
.req-text {
  color: #909399;
}

.job-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f5f7fa;
}
.publish-time {
  font-size: 12px;
  color: #c0c4cc;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>
