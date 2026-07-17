<template>
  <div class="page">
    <el-button icon="ArrowLeft" @click="$router.back()" link style="margin-bottom:12px;">返回候选人列表</el-button>
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="never" class="section-card">
          <div style="text-align:center;">
            <el-avatar :size="80">{{ candidate.name?.[0] }}</el-avatar>
            <h3>{{ candidate.name }}</h3>
          </div>
          <el-divider />
          <div class="info-list">
            <div class="info-item"><span>{{ candidate.email }}</span></div>
            <div class="info-item"><span>{{ candidate.phone }}</span></div>
            <div class="info-item"><span>{{ candidate.education }} · {{ candidate.school }}</span></div>
          </div>
          <el-divider />
          <div style="text-align:center;">
            <el-button type="primary">初筛通过</el-button>
            <el-button type="danger" plain>不合适</el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">📄 简历详情</h3>
          <!-- TODO: 从 getCandidateDetail(id).data.resume 加载 -->
          <el-empty description="TODO: 对接候选人详情接口" :image-size="80" />
        </el-card>
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">🤖 AI匹配分析</h3>
          <!-- TODO: 对接AI匹配接口 -->
          <el-empty description="TODO: 对接AI匹配分析接口" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
// import { getCandidateDetail } from '@/api/application'

const candidate = reactive({ name: '', email: '', phone: '', education: '', school: '' })
onMounted(async () => {
  // TODO: const res = await getCandidateDetail(route.params.id); Object.assign(candidate, res.data)
})
</script>

<style scoped>
.section-card { border-radius: 12px; margin-bottom: 16px; }
.card-title { margin: 0 0 14px; font-size: 16px; color: #3E2723; padding-bottom: 10px; border-bottom: 1px solid #f0f0f0; }
.info-list { display: flex; flex-direction: column; gap: 10px; }
.info-item { font-size: 14px; color: #666; }
</style>
