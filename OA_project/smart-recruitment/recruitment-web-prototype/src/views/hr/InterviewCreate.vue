<template>
  <div class="page">
    <div class="page-header">
      <h2>{{ isEdit ? '调整面试' : '创建面试' }}</h2>
      <el-button icon="ArrowLeft" @click="$router.back()" link>返回面试列表</el-button>
    </div>
    <el-card shadow="never" class="section-card" style="max-width:700px;">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="候选人" prop="candidateId">
          <el-select v-model="form.candidateId" placeholder="选择候选人" style="width:100%;" filterable>
            <el-option v-for="c in candidates" :key="c.id" :label="`${c.name} — ${c.jobTitle}`" :value="c.id">
              <span>{{ c.name }}</span>
              <el-tag size="small" :type="c.match >= 80 ? 'success' : 'warning'" style="margin-left:10px;">{{ c.match }}%</el-tag>
              <span style="float:right;color:#999;">{{ c.jobTitle }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="面试官" prop="interviewerId">
          <el-select v-model="form.interviewerId" placeholder="选择面试官" style="width:100%;">
            <el-option v-for="i in interviewers" :key="i.id" :label="`${i.name} — ${i.expertise}`" :value="i.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="面试类型">
          <el-radio-group v-model="form.type">
            <el-radio-button value="技术面试">技术面试</el-radio-button>
            <el-radio-button value="HR面试">HR面试</el-radio-button>
            <el-radio-button value="综合面试">综合面试</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="面试方式">
          <el-radio-group v-model="form.mode">
            <el-radio-button value="线上">🌐 线上视频</el-radio-button>
            <el-radio-button value="线下">🏢 线下面谈</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="面试时间" prop="interviewTime">
          <el-date-picker v-model="form.interviewTime" type="datetime" placeholder="选择日期时间" value-format="YYYY-MM-DD HH:mm" style="width:100%;" />
        </el-form-item>
        <el-form-item v-if="form.mode === '线下'" label="面试地点">
          <el-input v-model="form.location" placeholder="如：公司3楼会议室A" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="面试注意事项、面试重点方向等..." />
        </el-form-item>
        <el-form-item label="面试题">
          <el-switch v-model="form.autoGenerate" active-text="AI自动生成面试题" inactive-text="手动出题" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" icon="Check" @click="handleCreate">确认创建</el-button>
          <el-button size="large" @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

const formRef = ref()
const isEdit = ref(false)

const form = reactive({
  candidateId: 1, interviewerId: 1, type: '技术面试', mode: '线上',
  interviewTime: '2024-07-18 14:00', location: '公司3楼会议室A',
  remark: '', autoGenerate: true
})

const candidates = [
  { id: 1, name: '张三', jobTitle: 'Java高级开发工程师', match: 92 },
  { id: 2, name: '李四', jobTitle: '前端开发工程师（Vue）', match: 88 },
  { id: 3, name: '陈七', jobTitle: '产品经理', match: 79 },
  { id: 4, name: '周九', jobTitle: '测试开发工程师', match: 83 }
]

const interviewers = [
  { id: 1, name: '王工', expertise: 'Java/微服务/云原生' },
  { id: 2, name: '刘工', expertise: '前端/全栈/Node.js' },
  { id: 3, name: '赵工', expertise: '数据科学/AI算法' }
]

const rules = {
  candidateId: [{ required: true, message: '请选择候选人', trigger: 'change' }],
  interviewerId: [{ required: true, message: '请选择面试官', trigger: 'change' }],
  interviewTime: [{ required: true, message: '请选择面试时间', trigger: 'change' }]
}

function handleCreate() { ElMessage.success('面试已创建！系统将自动通知候选人和面试官') }
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.section-card { border-radius: 12px; margin-top: 16px; }
</style>
