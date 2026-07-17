<template>
  <div class="page">
    <el-button type="default" icon="ArrowLeft" @click="$router.back()" style="margin-bottom:16px;" link>返回职位列表</el-button>
    <el-card class="detail-card" shadow="never">
      <div class="detail-header">
        <div>
          <div class="title-row">
            <h2>Java高级开发工程师</h2>
            <el-tag type="danger" size="small" effect="dark">急聘</el-tag>
          </div>
          <div class="company-name">某科技有限公司</div>
          <div class="meta-row">
            <span><el-icon><Location /></el-icon> 北京·朝阳区</span>
            <span><el-icon><School /></el-icon> 本科及以上</span>
            <span><el-icon><Clock /></el-icon> 3-5年经验</span>
            <span>招聘 3 人</span>
          </div>
        </div>
        <div class="salary-box">
          <div class="salary">15K — 25K</div>
          <div class="salary-sub">13薪 · 五险一金 · 双休</div>
        </div>
      </div>
    </el-card>
    <el-row :gutter="20" style="margin-top:16px;">
      <el-col :span="16">
        <!-- 职位详情 -->
        <el-card shadow="never" class="section-card">
          <h3 class="card-section-title">📋 职位描述</h3>
          <div class="desc-text">
            <p><b>岗位职责：</b></p>
            <ul>
              <li>负责公司核心业务系统的后端开发与架构设计</li>
              <li>参与微服务架构的设计和优化，保证系统高可用和高性能</li>
              <li>编写高质量的技术文档和代码注释</li>
              <li>参与代码评审，指导初级开发工程师</li>
              <li>与产品经理、前端工程师紧密协作，保证项目按时交付</li>
            </ul>
            <p><b>任职要求：</b></p>
            <ul>
              <li>3年以上Java开发经验，熟悉Java 8/11/17特性</li>
              <li>精通Spring Boot、Spring Cloud微服务框架</li>
              <li>熟练使用MySQL、Redis、Elasticsearch等中间件</li>
              <li>有Docker、Kubernetes实际使用经验优先</li>
              <li>具备良好的沟通能力和团队协作精神</li>
            </ul>
          </div>
          <h3 class="card-section-title">🏷️ 技术栈</h3>
          <div class="skill-tags">
            <el-tag v-for="s in techStack" :key="s" size="default" effect="plain">{{ s }}</el-tag>
          </div>
        </el-card>
        <!-- 公司信息 -->
        <el-card shadow="never" class="section-card">
          <h3 class="card-section-title">🏢 公司信息</h3>
          <div class="company-info">
            <div class="company-intro">
              <h4>某科技有限公司</h4>
              <p>成立于2018年，专注于企业级SaaS软件研发。核心团队来自BAT，已获得B轮融资。目前200+员工，业务覆盖全国。</p>
            </div>
            <div class="company-stats">
              <div class="stat-item"><b>规模：</b>200-500人</div>
              <div class="stat-item"><b>行业：</b>企业服务/SaaS</div>
              <div class="stat-item"><b>融资：</b>B轮</div>
              <div class="stat-item"><b>地址：</b>北京市朝阳区望京SOHO</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <!-- 匹配度卡片 -->
        <el-card shadow="never" class="match-card">
          <h4>🎯 AI匹配度分析</h4>
          <div class="match-circle">
            <el-progress type="dashboard" :percentage="92" :color="customColors" :stroke-width="14" :width="140">
              <template #default><span class="match-num">92%</span></template>
            </el-progress>
            <p>您的简历与此职位高度匹配</p>
          </div>
          <el-divider />
          <div class="match-details">
            <div v-for="d in matchDetails" :key="d.label" class="match-item">
              <span>{{ d.label }}</span>
              <el-progress :percentage="d.score" :stroke-width="6" :show-text="false" :color="barColor(d.score)" style="flex:1; margin: 0 10px;" />
              <span class="match-score">{{ d.score }}%</span>
            </div>
          </div>
        </el-card>
        <!-- 投递按钮 -->
        <el-card shadow="never" class="action-card">
          <el-button type="primary" size="large" style="width:100%;" @click="handleApply" :loading="applying">
            <el-icon><Position /></el-icon> 立即投递
          </el-button>
          <el-button size="large" style="width:100%;margin-top:10px;" plain>
            <el-icon><Star /></el-icon> 收藏职位
          </el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const applying = ref(false)
const customColors = [{ color: '#67C23A', percentage: 70 }, { color: '#E6A23C', percentage: 90 }, { color: '#ED8936', percentage: 100 }]

const techStack = ['Java', 'Spring Boot', 'Spring Cloud', 'MySQL', 'Redis', 'Elasticsearch', 'Docker', 'Kubernetes', 'MyBatis-Plus', 'JWT', 'Nacos', 'RabbitMQ']

const matchDetails = [
  { label: '技能匹配', score: 95 },
  { label: '经验匹配', score: 88 },
  { label: '学历匹配', score: 100 },
  { label: '综合匹配', score: 92 }
]

function barColor(s) { return s >= 85 ? '#67C23A' : s >= 70 ? '#E6A23C' : '#909399' }

function handleApply() {
  applying.value = true
  setTimeout(() => { applying.value = false; ElMessage.success('投递成功！HR将在1-3个工作日内处理') }, 1200)
}
</script>

<style scoped>
.detail-card { border-radius: 12px; }
.detail-header { display: flex; justify-content: space-between; align-items: flex-start; }
.title-row { display: flex; align-items: center; gap: 10px; margin-bottom: 6px; }
.title-row h2 { margin: 0; font-size: 22px; color: #3E2723; }
.company-name { font-size: 15px; color: #666; margin-bottom: 10px; }
.meta-row { display: flex; gap: 20px; font-size: 13px; color: #999; flex-wrap: wrap; }
.meta-row span { display: flex; align-items: center; gap: 3px; }
.salary-box { text-align: right; flex-shrink: 0; }
.salary { font-size: 24px; font-weight: 700; color: #E76F51; }
.salary-sub { font-size: 12px; color: #999; margin-top: 4px; }
.section-card { border-radius: 12px; margin-bottom: 16px; }
.card-section-title { font-size: 16px; color: #3E2723; margin: 0 0 14px; padding-bottom: 10px; border-bottom: 1px solid #f0f0f0; }
.desc-text { font-size: 14px; line-height: 1.8; color: #555; }
.desc-text ul { padding-left: 20px; }
.desc-text li { margin-bottom: 6px; }
.skill-tags { display: flex; flex-wrap: wrap; gap: 8px; }
.company-intro h4 { margin: 0 0 8px; color: #3E2723; }
.company-intro p { font-size: 13px; color: #777; line-height: 1.7; margin-bottom: 16px; }
.company-stats { display: flex; flex-wrap: wrap; gap: 12px; }
.stat-item { font-size: 13px; color: #666; background: #f5f5f5; padding: 6px 14px; border-radius: 8px; }
.match-card, .action-card { border-radius: 12px; margin-bottom: 16px; }
.match-card h4 { margin: 0 0 10px; color: #3E2723; text-align: center; }
.match-circle { text-align: center; }
.match-num { font-size: 28px; font-weight: 700; color: #ED8936; }
.match-circle p { margin: 8px 0 0; font-size: 13px; color: #999; }
.match-details { margin-top: 12px; }
.match-item { display: flex; align-items: center; margin-bottom: 10px; font-size: 13px; color: #666; }
.match-score { font-weight: 600; color: #ED8936; min-width: 32px; }
</style>
