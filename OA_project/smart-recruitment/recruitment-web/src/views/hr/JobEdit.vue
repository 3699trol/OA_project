<template>
  <div class="page">
    <div class="page-header">
      <h2>{{ isEdit ? '编辑职位' : '发布新职位' }}</h2>
      <el-button icon="ArrowLeft" @click="$router.push('/hr/jobs')" link>返回</el-button>
    </div>
    <el-card shadow="never" class="section-card" style="max-width:800px;">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="职位名称" prop="title"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="职位类别" prop="category">
          <el-select v-model="form.category" style="width:100%;"><el-option label="技术研发" value="技术研发" /><el-option label="产品设计" value="产品设计" /><el-option label="数据科学" value="数据科学" /></el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="工作地点" prop="location"><el-input v-model="form.location" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="招聘人数" prop="headcount"><el-input-number v-model="form.headcount" :min="1" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="最低薪资(K)"><el-input-number v-model="form.salaryMin" :min="0" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="最高薪资(K)"><el-input-number v-model="form.salaryMax" :min="0" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="学历要求"><el-radio-group v-model="form.education"><el-radio value="大专">大专</el-radio><el-radio value="本科">本科</el-radio><el-radio value="硕士">硕士</el-radio></el-radio-group></el-form-item>
        <el-form-item label="经验要求"><el-radio-group v-model="form.experience"><el-radio value="应届生">应届生</el-radio><el-radio value="1-3年">1-3年</el-radio><el-radio value="3-5年">3-5年</el-radio></el-radio-group></el-form-item>
        <el-form-item label="职位描述" prop="description"><el-input v-model="form.description" type="textarea" :rows="5" /></el-form-item>
        <el-form-item label="任职要求" prop="requirements"><el-input v-model="form.requirements" type="textarea" :rows="5" /></el-form-item>
        <el-form-item label="技能标签">
          <div class="tag-input">
            <el-tag v-for="t in form.skills" :key="t" closable @close="form.skills = form.skills.filter(s => s !== t)">{{ t }}</el-tag>
            <el-input v-if="tagVisible" ref="tagInput" v-model="tagValue" size="small" style="width:120px;" @keyup.enter="addTag" @blur="addTag" />
            <el-button v-else size="small" @click="showTagInput">+ 添加</el-button>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" @click="handleSave">保存草稿</el-button>
          <el-button size="large" type="success" @click="handlePublish">保存并发布</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, nextTick, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createJob, updateJob, getJobDetail } from '@/api/job'

const route = useRoute()
const isEdit = computed(() => !!route.params.id)
const formRef = ref(); const tagVisible = ref(false); const tagValue = ref(''); const tagInput = ref()

const form = reactive({ title: '', category: '', location: '', headcount: 1, salaryMin: 0, salaryMax: 0, education: '本科', experience: '3-5年', description: '', requirements: '', skills: [] })

const rules = {
  title: [{ required: true, message: '请输入职位名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择类别', trigger: 'change' }]
}

function addTag() { const v = tagValue.value.trim(); if (v && !form.skills.includes(v)) form.skills.push(v); tagVisible.value = false; tagValue.value = '' }
function showTagInput() { tagVisible.value = true; nextTick(() => tagInput.value?.input?.focus()) }

onMounted(async () => {
  if (isEdit.value) { /* TODO: const res = await getJobDetail(route.params.id); Object.assign(form, res.data) */ }
})

async function handleSave() { /* TODO: await createJob(form) */ ElMessage.info('TODO: 对接职位保存接口') }
async function handlePublish() { /* TODO: await createJob({ ...form, status: '已发布' }) */ ElMessage.info('TODO: 对接职位发布接口') }
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.section-card { border-radius: 12px; margin-top: 16px; }
.tag-input { display: flex; flex-wrap: wrap; gap: 8px; align-items: center; }
</style>
