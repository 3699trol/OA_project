<template>
  <div class="page">
    <el-button icon="ArrowLeft" @click="$router.back()" link style="margin-bottom:12px;">返回候选人列表</el-button>
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="never" class="section-card">
          <div style="text-align:center;">
            <el-avatar :size="80" style="background:#ED8936;font-size:32px;">{{ candidate.name[0] }}</el-avatar>
            <h3 style="margin:12px 0 4px;">{{ candidate.name }}</h3>
            <el-tag :type="statusColor" effect="plain">{{ candidate.status }}</el-tag>
          </div>
          <el-divider />
          <div class="info-list">
            <div class="info-item"><span>📧</span><span>{{ candidate.email }}</span></div>
            <div class="info-item"><span>📱</span><span>{{ candidate.phone }}</span></div>
            <div class="info-item"><span>🎓</span><span>{{ candidate.education }} · {{ candidate.school }}</span></div>
            <div class="info-item"><span>📍</span><span>{{ candidate.city }}</span></div>
            <div class="info-item"><span>💼</span><span>{{ candidate.experience }}</span></div>
          </div>
          <el-divider />
          <div style="text-align:center;">
            <el-button type="primary" icon="Check" @click="handlePass">初筛通过</el-button>
            <el-button type="danger" icon="Close" plain @click="handleReject">不合适</el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">📄 简历详情</h3>
          <h4 style="margin: 0 0 12px;">投递职位：{{ candidate.jobTitle }}</h4>
          <div class="resume-section">
            <h5>教育背景</h5>
            <p>北京大学 · 计算机科学与技术 · 本科 · 2016-2020</p>
          </div>
          <div class="resume-section">
            <h5>工作经历</h5>
            <div class="exp-block">
              <div class="exp-header"><b>某互联网科技有限公司</b> · Java开发工程师 · 2020.07 - 2023.06</div>
              <ul><li>负责核心业务系统后端开发，参与多个项目从0到1搭建</li><li>使用Spring Boot + Spring Cloud构建微服务架构</li><li>优化系统性能，QPS提升30%</li></ul>
            </div>
            <div class="exp-block">
              <div class="exp-header"><b>某金融科技公司</b> · 高级Java开发工程师 · 2023.07至今</div>
              <ul><li>主导支付系统核心模块设计，日处理交易百万级</li><li>引入Docker + K8s容器化方案，搭建CI/CD流水线</li></ul>
            </div>
          </div>
          <div class="resume-section">
            <h5>专业技能</h5>
            <div class="skill-tags">
              <el-tag v-for="s in skills" :key="s" size="default" effect="plain">{{ s }}</el-tag>
            </div>
          </div>
          <div class="resume-section">
            <h5>自我评价</h5>
            <p style="color:#666;line-height:1.7;">5年Java后端开发经验，熟悉微服务架构，具备良好的系统设计能力。热爱技术，持续学习，追求高质量代码。希望能在大数据/AI相关领域继续深耕。</p>
          </div>
        </el-card>
        <!-- AI分析 -->
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">🤖 AI匹配分析</h3>
          <div class="match-header">
            <el-progress type="dashboard" :percentage="92" :stroke-width="12" :width="120" :color="[{color:'#ED8936',percentage:100}]">
              <template #default><span style="font-size:24px;font-weight:700;">92%</span></template>
            </el-progress>
            <div class="match-summary">
              <h4>综合匹配度：高度匹配</h4>
              <p>该候选人的技术栈与职位要求高度吻合，有5年相关经验，建议优先安排面试。</p>
            </div>
          </div>
          <div class="match-items">
            <div v-for="m in matchItems" :key="m.label" class="match-row">
              <span class="match-label">{{ m.label }}</span>
              <el-progress :percentage="m.score" :stroke-width="8" :show-text="false" :color="m.score >= 80 ? '#67C23A' : '#E6A23C'" style="flex:1; margin:0 12px;" />
              <span class="match-score">{{ m.score }}%</span>
              <span class="match-note">{{ m.note }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'

const candidate = {
  name: '张三', status: '初筛中', email: 'zhangsan@example.com', phone: '138****6789',
  education: '本科', school: '北京大学', city: '北京', experience: '5年', jobTitle: 'Java高级开发工程师'
}

const skills = ['Java', 'Spring Boot', 'Spring Cloud', 'MySQL', 'Redis', 'Elasticsearch', 'Docker', 'Kubernetes', 'Nacos', 'Git', 'Linux', 'MyBatis-Plus']

const matchItems = [
  { label: '技能匹配', score: 95, note: 'Java/Spring/Redis 等技术栈完全匹配' },
  { label: '经验匹配', score: 88, note: '5年经验略超要求，级别匹配' },
  { label: '学历匹配', score: 100, note: '本科学历完全符合要求' },
  { label: '地点匹配', score: 100, note: '现居北京，无需异地调动' },
  { label: '稳定性', score: 75, note: '近3年无频繁跳槽记录' }
]

const statusColor = 'warning'

function handlePass() { ElMessage.success('已标记为初筛通过') }
function handleReject() { ElMessage.info('已标记为不合适') }
</script>

<style scoped>
.section-card { border-radius: 12px; margin-bottom: 16px; }
.card-title { margin: 0 0 14px; font-size: 16px; color: #3E2723; padding-bottom: 10px; border-bottom: 1px solid #f0f0f0; }
.info-list { display: flex; flex-direction: column; gap: 10px; }
.info-item { display: flex; gap: 10px; font-size: 14px; color: #666; }
.resume-section { margin-bottom: 18px; }
.resume-section h5 { font-size: 14px; color: #ED8936; margin: 0 0 8px; }
.resume-section p { margin: 0; }
.exp-block { margin-bottom: 12px; padding: 10px; background: #fafafa; border-radius: 8px; }
.exp-header { font-size: 13px; color: #3E2723; margin-bottom: 6px; }
.exp-block ul { margin: 0; padding-left: 20px; font-size: 13px; color: #666; line-height: 1.6; }
.skill-tags { display: flex; flex-wrap: wrap; gap: 8px; }
.match-header { display: flex; align-items: center; gap: 20px; margin-bottom: 16px; }
.match-summary h4 { margin: 0 0 6px; color: #3E2723; }
.match-summary p { margin: 0; font-size: 13px; color: #666; line-height: 1.6; }
.match-items { display: flex; flex-direction: column; gap: 10px; }
.match-row { display: flex; align-items: center; font-size: 13px; }
.match-label { width: 80px; color: #666; }
.match-score { font-weight: 600; color: #ED8936; width: 36px; }
.match-note { font-size: 12px; color: #999; width: 220px; text-align: left; }
</style>
