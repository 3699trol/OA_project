<template>
  <div class="page">
    <el-button icon="ArrowLeft" @click="$router.back()" link style="margin-bottom:12px;">返回</el-button>
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card shadow="never" class="section-card">
          <div class="candidate-header">
            <div>
              <h2>{{ detail.candidateName }} — {{ detail.jobName }}</h2>
              <div class="meta">
                <el-tag :type="detail.status === 0 ? 'warning' : 'success'">{{ detail.statusLabel }}</el-tag>
                <span>{{ detail.interviewTime }}</span>
                <span>{{ detail.interviewType }}</span>
                <span v-if="detail.address">{{ detail.address }}</span>
              </div>
            </div>
          </div>
        </el-card>
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">面试评价</h3>
          <div v-if="detail.evaluation">
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="技术评分">{{ detail.evaluation.technicalScore }}分</el-descriptions-item>
              <el-descriptions-item label="沟通评分">{{ detail.evaluation.communicationScore }}分</el-descriptions-item>
              <el-descriptions-item label="逻辑评分">{{ detail.evaluation.logicScore }}分</el-descriptions-item>
              <el-descriptions-item label="综合评分">{{ detail.evaluation.overallScore }}分</el-descriptions-item>
              <el-descriptions-item label="面试结果" :span="2">
                <el-tag :type="detail.evaluation.result === 2 ? 'success' : detail.evaluation.result === 1 ? 'warning' : 'danger'">
                  {{ detail.evaluation.resultLabel }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
            <div v-if="detail.evaluation.evaluation" style="margin-top:16px;">
              <h4>评价内容</h4>
              <p style="color:#666;line-height:1.8;white-space:pre-wrap;">{{ detail.evaluation.evaluation }}</p>
            </div>
          </div>
          <el-empty v-else description="暂无评价" :image-size="60" />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">面试信息</h3>
          <el-descriptions direction="vertical" :column="1" size="small">
            <el-descriptions-item label="面试官">{{ detail.interviewerName }}</el-descriptions-item>
            <el-descriptions-item label="面试时间">{{ detail.interviewTime }}</el-descriptions-item>
            <el-descriptions-item label="面试类型">{{ detail.interviewType }}</el-descriptions-item>
            <el-descriptions-item label="地点/链接">{{ detail.address || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">操作</h3>
          <el-button v-if="detail.status === 0" type="primary" style="width:100%;margin-bottom:10px;" @click="$router.push(`/interviewer/evaluation/${route.params.id}`)">填写评价</el-button>
          <el-button style="width:100%;" @click="$router.push('/interviewer/questions/generate')">AI生成面试题</el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getInterviewDetail } from '@/api/interview'

const route = useRoute()
const detail = reactive({
  id: route.params.id,
  candidateName: '',
  jobName: '',
  interviewTime: '',
  interviewType: '',
  address: '',
  interviewerName: '',
  status: 0,
  statusLabel: '',
  evaluation: null
})

onMounted(async () => {
  try {
    const res = await getInterviewDetail(route.params.id)
    if (res && res.data) {
      Object.assign(detail, res.data)
    }
  } catch (e) {
    ElMessage.error('加载面试详情失败')
  }
})
</script>

<style scoped>
.section-card { border-radius: 12px; margin-bottom: 16px; }
.card-title { margin: 0 0 14px; font-size: 16px; color: #3E2723; padding-bottom: 10px; border-bottom: 1px solid #f0f0f0; }
.candidate-header h2 { margin: 0 0 8px; font-size: 20px; color: #3E2723; }
.meta { display: flex; gap: 16px; align-items: center; font-size: 13px; color: #999; flex-wrap: wrap; }
</style>
