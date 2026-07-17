<template>
  <div class="page">
    <el-button icon="ArrowLeft" @click="$router.back()" link style="margin-bottom:12px;">返回</el-button>
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card shadow="never" class="section-card">
          <div class="candidate-header">
            <div><h2>{{ interview.candidateName }} — {{ interview.jobTitle }}</h2>
              <div class="meta"><el-tag type="warning">待面试</el-tag><span>{{ interview.time }}</span></div></div>
          </div>
        </el-card>
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">📄 简历摘要</h3>
          <!-- TODO: load from getCandidateDetail(interview.candidateId) -->
          <el-empty description="TODO: 对接候选人信息接口" :image-size="60" />
        </el-card>
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">🤖 AI推荐面试题</h3>
          <!-- TODO: load from getInterviewQuestions(interview.id) -->
          <el-empty description="TODO: 对接AI面试题接口" :image-size="60" />
          <el-button type="primary" @click="$router.push('/interviewer/questions/generate')">去生成面试题</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">📝 面试笔记</h3>
          <el-input v-model="notes" type="textarea" :rows="6" placeholder="记录关键信息..." />
          <el-button type="primary" style="width:100%;margin-top:12px;">保存笔记</el-button>
        </el-card>
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">操作</h3>
          <el-button style="width:100%;margin-bottom:10px;" @click="$router.push(`/interviewer/evaluation/${route.params.id}`)">填写评价</el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
// import { getInterviewDetail } from '@/api/interview'

const route = useRoute()
const notes = ref('')
const interview = reactive({ id: route.params.id, candidateName: '', jobTitle: '', time: '', candidateId: null })
onMounted(async () => { /* TODO: const res = await getInterviewDetail(route.params.id) */ })
</script>

<style scoped>
.section-card { border-radius: 12px; margin-bottom: 16px; }
.card-title { margin: 0 0 14px; font-size: 16px; color: #3E2723; padding-bottom: 10px; border-bottom: 1px solid #f0f0f0; }
.candidate-header h2 { margin: 0 0 8px; font-size: 20px; color: #3E2723; }
.meta { display: flex; gap: 16px; align-items: center; font-size: 13px; color: #999; }
</style>
