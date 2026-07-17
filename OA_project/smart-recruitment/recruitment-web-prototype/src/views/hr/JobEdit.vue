<template>
  <div class="page">
    <div class="page-header">
      <h2>{{ isEdit ? '编辑职位' : '发布新职位' }}</h2>
      <el-button icon="ArrowLeft" @click="$router.push('/hr/jobs')" link>返回职位列表</el-button>
    </div>
    <el-card shadow="never" class="section-card" style="max-width:800px;">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" class="job-form">
        <el-divider content-position="left">基本信息</el-divider>
        <el-form-item label="职位名称" prop="title">
          <el-input v-model="form.title" placeholder="如：Java高级开发工程师" />
        </el-form-item>
        <el-form-item label="职位类别" prop="category">
          <el-select v-model="form.category" placeholder="请选择" style="width:100%;">
            <el-option label="技术研发" value="技术研发" /><el-option label="产品设计" value="产品设计" />
            <el-option label="数据科学" value="数据科学" /><el-option label="运营市场" value="运营市场" />
            <el-option label="职能支持" value="职能支持" />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="工作地点" prop="location">
              <el-input v-model="form.location" placeholder="如：北京·朝阳区" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="招聘人数" prop="headcount">
              <el-input-number v-model="form.headcount" :min="1" :max="99" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="最低薪资(K)">
              <el-input-number v-model="form.salaryMin" :min="0" :step="1" /> K
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最高薪资(K)">
              <el-input-number v-model="form.salaryMax" :min="0" :step="1" /> K
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="left">任职要求</el-divider>
        <el-form-item label="学历要求" prop="education">
          <el-radio-group v-model="form.education">
            <el-radio value="大专">大专</el-radio><el-radio value="本科">本科</el-radio><el-radio value="硕士">硕士</el-radio><el-radio value="不限">不限</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="经验要求" prop="experience">
          <el-radio-group v-model="form.experience">
            <el-radio value="应届生">应届生</el-radio><el-radio value="1-3年">1-3年</el-radio><el-radio value="3-5年">3-5年</el-radio><el-radio value="5年以上">5年以上</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="职位描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="6" placeholder="描述该职位的日常工作内容和职责..." />
        </el-form-item>
        <el-form-item label="任职要求" prop="requirements">
          <el-input v-model="form.requirements" type="textarea" :rows="6" placeholder="描述应聘者需要具备的技能、经验和素质..." />
        </el-form-item>
        <el-divider content-position="left">技术标签</el-divider>
        <el-form-item label="技能标签">
          <div class="tag-input">
            <el-tag v-for="t in form.skills" :key="t" closable @close="form.skills = form.skills.filter(s => s !== t)" class="skill-tag">{{ t }}</el-tag>
            <el-input v-if="tagVisible" ref="tagInputRef" v-model="tagValue" size="small" style="width:120px;" @keyup.enter="addTag" @blur="addTag" />
            <el-button v-else size="small" @click="showTagInput">+ 添加标签</el-button>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" icon="Check" @click="handleSave">保存职位</el-button>
          <el-button size="large" icon="Promise" @click="handlePublish">保存并发布</el-button>
          <el-button size="large" @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'

const route = useRoute()
const isEdit = computed(() => !!route.params.id)
const formRef = ref()
const tagVisible = ref(false)
const tagValue = ref('')
const tagInputRef = ref()

const form = reactive({
  title: isEdit.value ? 'Java高级开发工程师' : '', category: isEdit.value ? '技术研发' : '',
  location: isEdit.value ? '北京·朝阳区' : '', headcount: 3,
  salaryMin: 15, salaryMax: 25, education: '本科', experience: '3-5年',
  description: isEdit.value ? '负责公司核心业务系统的后端开发与架构设计\n参与微服务架构的设计和优化\n编写高质量的技术文档和代码注释\n参与代码评审，指导初级开发工程师' : '',
  requirements: isEdit.value ? '3年以上Java开发经验\n精通Spring Boot、Spring Cloud\n熟练使用MySQL、Redis、Elasticsearch\n具备良好的沟通和团队协作能力' : '',
  skills: isEdit.value ? ['Java', 'Spring Boot', 'Spring Cloud', 'MySQL', 'Redis', 'Docker'] : []
})

const rules = {
  title: [{ required: true, message: '请输入职位名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择类别', trigger: 'change' }],
  location: [{ required: true, message: '请输入工作地点', trigger: 'blur' }],
  headcount: [{ required: true, message: '请输入招聘人数', trigger: 'blur' }],
  description: [{ required: true, message: '请输入职位描述', trigger: 'blur' }],
  requirements: [{ required: true, message: '请输入任职要求', trigger: 'blur' }]
}

function addTag() {
  const v = tagValue.value.trim()
  if (v && !form.skills.includes(v)) form.skills.push(v)
  tagVisible.value = false; tagValue.value = ''
}
function showTagInput() { tagVisible.value = true; nextTick(() => tagInputRef.value?.input?.focus()) }

function handleSave() { ElMessage.success('职位保存成功（草稿）') }
function handlePublish() { ElMessage.success('职位已发布！') }
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.section-card { border-radius: 12px; margin-top: 16px; }
.job-form { padding: 10px 0; }
.tag-input { display: flex; flex-wrap: wrap; gap: 8px; align-items: center; }
</style>
