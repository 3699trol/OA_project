<template>
  <div class="page">
    <div class="page-header">
      <h2>👥 候选人才库</h2>
    </div>
    <el-card shadow="never" class="section-card">
      <!-- ES 搜索栏 -->
      <div class="talent-search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索教育经历、工作经历、技能标签..."
          prefix-icon="Search"
          clearable
          size="large"
          class="talent-input"
          @keyup.enter="doSearch"
          @clear="doSearch"
        />
        <el-button type="primary" size="large" icon="Search" @click="doSearch" :loading="loading">搜索</el-button>
      </div>

      <!-- 搜索结果 -->
      <div v-if="!searched" class="empty-state">
        <el-empty description="请输入关键词搜索人才库" :image-size="80" />
      </div>
      <div v-else-if="results.length === 0" class="empty-state">
        <el-empty description="未找到匹配的候选人" :image-size="80" />
      </div>
      <el-table v-else :data="results" v-loading="loading" stripe>
        <el-table-column prop="candidateName" label="姓名" width="100" />
        <el-table-column label="教育经历" min-width="200">
          <template #default="{ row }">
            <span v-if="row.education" v-html="row.education"></span>
            <span v-else class="no-data">-</span>
          </template>
        </el-table-column>
        <el-table-column label="工作经历" min-width="200">
          <template #default="{ row }">
            <span v-if="row.experience" v-html="row.experience"></span>
            <span v-else class="no-data">-</span>
          </template>
        </el-table-column>
        <el-table-column label="技能标签" min-width="150">
          <template #default="{ row }">
            <span v-if="row.skills" v-html="row.skills"></span>
            <span v-else class="no-data">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="$router.push(`/hr/candidates/detail/${row.applicationId}`)">简历详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-if="searched && total > 0"
        v-model:current-page="page"
        :page-size="10"
        :total="total"
        layout="prev, pager, next, total"
        background
        style="margin-top:16px;justify-content:center;"
        @current-change="doSearch"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { searchResume } from '@/api/search'

const keyword = ref('')
const loading = ref(false)
const searched = ref(false)
const results = ref([])
const total = ref(0)
const page = ref(1)

async function doSearch() {
  if (!keyword.value.trim()) {
    searched.value = false
    results.value = []
    total.value = 0
    return
  }
  loading.value = true
  searched.value = true
  try {
    const res = await searchResume({ keyword: keyword.value, page: page.value, size: 10 })
    const records = res.data.records || []
    results.value = records.map(r => ({
      applicationId: r.applicationId || null,
      candidateName: r.candidateName || '候选人',
      education: highlightText(r.highlights?.education || r.education || ''),
      experience: highlightText(r.highlights?.experience || r.experience || ''),
      skills: highlightText(r.highlights?.skills || r.skills || '')
    }))
    total.value = res.data.total || 0
  } catch (e) {
    results.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function highlightText(text) {
  if (!text) return ''
  // ES highlights 返回的是数组，取第一个元素
  if (Array.isArray(text)) {
    text = text[0] || ''
  }
  if (typeof text !== 'string') return ''
  return text.replace(/<em>/gi, '<span class="highlight">').replace(/<\/em>/gi, '</span>')
}
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.section-card { border-radius: 12px; margin-top: 16px; }
.talent-search-bar { display: flex; gap: 12px; margin-bottom: 16px; }
.talent-input { flex: 1; }
.empty-state { padding: 40px 0; text-align: center; }
.no-data { color: #a0aec0; }
:deep(.highlight) {
  background-color: #fff3e0;
  color: #e65100;
  font-weight: 600;
  padding: 1px 4px;
  border-radius: 3px;
}
</style>
