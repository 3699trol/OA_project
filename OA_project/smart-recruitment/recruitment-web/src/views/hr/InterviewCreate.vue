<template>
  <div class="page">
    <div class="page-header">
      <h2>创建面试</h2>
      <el-button icon="ArrowLeft" @click="$router.back()" link>返回</el-button>
    </div>
    <el-card shadow="never" class="section-card" style="max-width:700px;">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="候选人" prop="candidateId">
          <el-select v-model="form.candidateId" placeholder="选择候选人" style="width:100%;" filterable @change="onCandidateChange">
            <el-option v-for="c in candidates" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="投递职位" prop="applicationId">
          <el-select v-model="form.applicationId" placeholder="选择投递职位" style="width:100%;" :disabled="!form.candidateId">
            <el-option v-for="a in applications" :key="a.id" :label="a.jobName" :value="a.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="面试官" prop="interviewerId">
          <el-select v-model="form.interviewerId" placeholder="选择面试官" style="width:100%;" filterable>
            <el-option v-for="i in interviewers" :key="i.id" :label="`${i.name}（${i.userType === 2 ? 'HR' : '面试官'}）`" :value="i.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="面试类型">
          <el-radio-group v-model="form.type">
            <el-radio-button value="技术">技术</el-radio-button>
            <el-radio-button value="HR">HR</el-radio-button>
            <el-radio-button value="综合">综合</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="面试方式">
          <el-radio-group v-model="form.mode">
            <el-radio-button value="线上">线上</el-radio-button>
            <el-radio-button value="线下">线下</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="面试时间" prop="interviewTime">
          <el-date-picker v-model="form.interviewTime" type="datetime" value-format="YYYY-MM-DD HH:mm" style="width:100%;" placeholder="选择面试时间" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.address" type="textarea" :rows="3" placeholder="线下填写地点，线上填写会议链接" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" @click="handleCreate" :loading="submitting">创建</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createInterview, getInterviewOptions, getCandidateApplications } from '@/api/interview'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const submitting = ref(false)
const candidates = ref([])
const interviewers = ref([])
const applications = ref([])

const form = reactive({
  candidateId: null,
  applicationId: null,
  interviewerId: null,
  type: '技术',
  mode: '线上',
  interviewTime: '',
  address: ''
})

const rules = {
  candidateId: [{ required: true, message: '请选择候选人', trigger: 'change' }],
  applicationId: [{ required: true, message: '请选择投递职位', trigger: 'change' }],
  interviewerId: [{ required: true, message: '请选择面试官', trigger: 'change' }],
  interviewTime: [{ required: true, message: '请选择面试时间', trigger: 'change' }]
}

async function loadOptions() {
  try {
    const res = await getInterviewOptions()
    if (res && res.data) {
      candidates.value = res.data.candidates || []
      interviewers.value = res.data.interviewers || []
    }
  } catch (e) {
    ElMessage.error('加载选项失败')
  }
}

async function onCandidateChange(userId) {
  form.applicationId = null
  applications.value = []
  if (!userId) return
  try {
    const res = await getCandidateApplications(userId)
    if (res && res.data) {
      // 只保留"面试中(1)"和"已录用(2)"的投递记录，其他状态不允许安排面试
      applications.value = res.data.filter(a => a.status === 1 || a.status === 2)
      if (applications.value.length === 0) {
        ElMessage.warning('该候选人暂无可安排面试的投递记录（需初筛通过后才可安排面试）')
      }
      // 如果只有一个可安排的投递记录，自动选中
      if (applications.value.length === 1) {
        form.applicationId = applications.value[0].id
      }
    }
  } catch (e) {
    ElMessage.error('加载候选人投递记录失败')
  }
}

async function handleCreate() {
  await formRef.value.validate().catch(() => {})
  if (!form.candidateId || !form.applicationId || !form.interviewerId || !form.interviewTime) {
    ElMessage.warning('请填写必填项')
    return
  }
  submitting.value = true
  try {
    await createInterview(form)
    ElMessage.success('面试创建成功')
    setTimeout(() => {
      router.back()
    }, 500)
  } catch (e) {
    // 错误已由拦截器处理
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await loadOptions()
  // 如果路由带了 candidateId 参数（从候选人列表跳转过来），自动选中
  if (route.query.candidateId) {
    const cid = Number(route.query.candidateId)
    form.candidateId = cid
    await onCandidateChange(cid)
  }
})
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.section-card { border-radius: 12px; margin-top: 16px; }
</style>
