<template>
  <div class="page">
    <div class="page-header">
      <h2>📄 我的简历</h2>
      <div class="header-actions">
        <el-button type="success" icon="MagicStick" @click="handleAiParse">🤖 AI智能解析</el-button>
        <el-button type="warning" icon="TrendCharts" @click="$router.push('/candidate/resume/evaluation')">📊 简历评估</el-button>
        <el-button type="primary" icon="Upload" @click="uploadVisible = true">📤 上传简历文件</el-button>
      </div>
    </div>
    <!-- 基本信息 -->
    <el-card shadow="never" class="section-card">
      <h3 class="card-title">基本信息</h3>
      <el-form :model="form" label-width="100px" class="resume-form">
        <el-row :gutter="24">
          <el-col :span="8">
            <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="性别">
              <el-radio-group v-model="form.gender"><el-radio value="男">男</el-radio><el-radio value="女">女</el-radio></el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="出生年月"><el-date-picker v-model="form.birth" type="month" placeholder="选择月份" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="24">
          <el-col :span="8">
            <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="现居城市"><el-input v-model="form.city" placeholder="如：北京" /></el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>
    <!-- 教育背景 -->
    <el-card shadow="never" class="section-card">
      <div class="card-title-row">
        <h3 class="card-title">教育背景</h3>
        <el-button type="primary" link icon="Plus">添加</el-button>
      </div>
      <div v-for="(edu, i) in form.educations" :key="i" class="edu-item">
        <div class="edu-header">
          <span class="edu-school">{{ edu.school }}</span>
          <span class="edu-degree">{{ edu.degree }}</span>
          <span class="edu-time">{{ edu.time }}</span>
        </div>
        <p class="edu-major">{{ edu.major }}</p>
      </div>
    </el-card>
    <!-- 工作经历 -->
    <el-card shadow="never" class="section-card">
      <div class="card-title-row">
        <h3 class="card-title">工作经历</h3>
        <el-button type="primary" link icon="Plus">添加</el-button>
      </div>
      <div v-for="(exp, i) in form.experiences" :key="i" class="exp-item">
        <div class="exp-header">
          <span class="exp-company">{{ exp.company }}</span>
          <span class="exp-role">{{ exp.role }}</span>
          <span class="exp-time">{{ exp.time }}</span>
        </div>
        <ul class="exp-desc">
          <li v-for="d in exp.details" :key="d">{{ d }}</li>
        </ul>
      </div>
    </el-card>
    <!-- 技能标签 -->
    <el-card shadow="never" class="section-card">
      <h3 class="card-title">专业技能</h3>
      <div class="skill-input">
        <el-tag v-for="s in form.skills" :key="s" closable :disable-transitions="false" @close="removeSkill(s)" class="skill-tag" size="default">{{ s }}</el-tag>
        <el-input v-if="skillInputVisible" ref="skillInputRef" v-model="skillInputValue" size="small" style="width:120px;" @keyup.enter="addSkill" @blur="addSkill" />
        <el-button v-else size="small" @click="showSkillInput">+ 添加技能</el-button>
      </div>
    </el-card>
    <!-- 自我评价 -->
    <el-card shadow="never" class="section-card">
      <h3 class="card-title">自我评价</h3>
      <el-input v-model="form.summary" type="textarea" :rows="4" placeholder="简要描述您的优势、职业规划和期望..." />
    </el-card>
    <!-- 操作按钮 -->
    <div class="form-actions">
      <el-button type="primary" size="large" icon="Check" @click="handleSave">保存简历</el-button>
      <el-button size="large" icon="RefreshLeft">重置</el-button>
    </div>
    <!-- 上传弹窗 -->
    <el-dialog v-model="uploadVisible" title="上传简历文件" width="480px">
      <el-upload drag multiple :auto-upload="false" :limit="3" accept=".pdf,.doc,.docx">
        <el-icon class="el-icon--upload" size="48"><UploadFilled /></el-icon>
        <div class="el-upload__text">将简历文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">支持 PDF、Word 格式，单个文件不超过 10MB</div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="uploadVisible = false">取消</el-button>
        <el-button type="primary" @click="uploadVisible = false">确认上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick } from 'vue'
import { ElMessage } from 'element-plus'

const uploadVisible = ref(false)
const skillInputVisible = ref(false)
const skillInputValue = ref('')
const skillInputRef = ref()

const form = reactive({
  name: '张三', gender: '男', birth: '1998-06', phone: '138****6789', email: 'zhangsan@example.com', city: '北京',
  educations: [
    { school: '北京大学', degree: '本科', time: '2016.09 - 2020.06', major: '计算机科学与技术' },
  ],
  experiences: [
    { company: '某互联网科技有限公司', role: 'Java开发工程师', time: '2020.07 - 2023.06', details: ['负责公司核心业务系统的后端开发，参与多个项目从0到1的搭建', '使用Spring Boot + Spring Cloud构建微服务架构，实现服务的注册发现、配置管理、负载均衡', '对接MySQL、Redis、Elasticsearch等中间件，优化系统性能，QPS提升30%'] },
    { company: '某金融科技公司', role: '高级Java开发工程师', time: '2023.07 - 至今', details: ['主导支付系统核心模块的设计与开发，日处理交易量百万级', '引入Docker + Kubernetes容器化部署方案，CI/CD流水线搭建', '指导2名初级开发工程师，组织代码评审和技术分享'] }
  ],
  skills: ['Java', 'Spring Boot', 'Spring Cloud', 'MySQL', 'Redis', 'Elasticsearch', 'Docker', 'Git', 'Linux'],
  summary: '5年Java后端开发经验，熟悉微服务架构，具备良好的系统设计能力和团队协作精神。热爱技术，持续学习，追求高质量代码。希望能在大数据/AI相关领域继续深耕。'
})

function addSkill() {
  const v = skillInputValue.value.trim()
  if (v && !form.skills.includes(v)) form.skills.push(v)
  skillInputVisible.value = false
  skillInputValue.value = ''
}
function removeSkill(s) { form.skills = form.skills.filter(x => x !== s) }
function showSkillInput() { skillInputVisible.value = true; nextTick(() => skillInputRef.value?.input?.focus()) }

function handleSave() { ElMessage.success('简历保存成功') }
function handleAiParse() { ElMessage.info('正在调用AI解析...请稍候') }
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.section-card { border-radius: 12px; margin-bottom: 16px; }
.card-title { margin: 0 0 16px; font-size: 16px; color: #3E2723; padding-bottom: 10px; border-bottom: 1px solid #f0f0f0; }
.card-title-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; padding-bottom: 10px; border-bottom: 1px solid #f0f0f0; }
.card-title-row .card-title { margin: 0; padding: 0; border: none; }
.edu-item, .exp-item { margin-bottom: 14px; padding: 14px; background: #fafafa; border-radius: 10px; }
.edu-header, .exp-header { display: flex; gap: 16px; margin-bottom: 6px; flex-wrap: wrap; }
.edu-school, .exp-company { font-weight: 600; color: #3E2723; }
.edu-degree, .exp-role { color: #ED8936; }
.edu-time, .exp-time { color: #999; font-size: 13px; }
.edu-major { color: #666; font-size: 14px; }
.exp-desc { padding-left: 20px; margin: 8px 0 0; color: #666; font-size: 13px; line-height: 1.7; }
.exp-desc li { margin-bottom: 4px; }
.skill-input { display: flex; flex-wrap: wrap; gap: 8px; align-items: center; }
.skill-tag { cursor: default; }
.form-actions { margin-top: 24px; display: flex; gap: 12px; justify-content: center; }
</style>
