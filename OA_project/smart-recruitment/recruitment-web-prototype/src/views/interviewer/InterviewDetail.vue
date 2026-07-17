<template>
  <div class="page">
    <el-button icon="ArrowLeft" @click="$router.back()" link style="margin-bottom:12px;">返回任务列表</el-button>
    <el-row :gutter="20">
      <el-col :span="16">
        <!-- 候选人信息 -->
        <el-card shadow="never" class="section-card">
          <div class="candidate-header">
            <div>
              <h2>张三 — Java高级开发工程师</h2>
              <div class="meta-row">
                <el-tag type="warning">待面试</el-tag>
                <span>🕐 2024-07-18 14:00-15:00</span>
                <span>🌐 线上视频面试</span>
              </div>
            </div>
            <div class="actions">
              <el-button type="primary" icon="VideoPlay">开始面试</el-button>
              <el-button type="success" icon="EditPen" @click="$router.push(`/interviewer/questions/generate`)">AI生成面试题</el-button>
            </div>
          </div>
        </el-card>
        <!-- 简历摘要 -->
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">📄 简历摘要</h3>
          <div class="resume-grid">
            <div class="resume-item"><b>学历：</b>本科 · 北京大学 · 计算机科学与技术</div>
            <div class="resume-item"><b>经验：</b>5年 Java开发</div>
            <div class="resume-item"><b>当前公司：</b>某金融科技公司</div>
            <div class="resume-item"><b>核心技能：</b>Java, Spring Cloud, Redis, Docker, K8s</div>
          </div>
          <el-collapse style="margin-top:12px;">
            <el-collapse-item title="查看完整简历" name="1">
              <div class="resume-section">
                <h5>工作经历</h5>
                <p><b>某金融科技公司</b> · 高级Java开发工程师 · 2023.07 - 至今</p>
                <ul><li>主导支付系统核心模块设计与开发，日处理交易量百万级</li><li>引入Docker + K8s容器化方案，CI/CD流水线搭建</li></ul>
                <p><b>某互联网科技有限公司</b> · Java开发工程师 · 2020.07 - 2023.06</p>
                <ul><li>负责核心业务系统后端开发</li><li>Spring Boot + Spring Cloud微服务架构实践</li></ul>
              </div>
            </el-collapse-item>
          </el-collapse>
        </el-card>
        <!-- AI面试题 -->
        <el-card shadow="never" class="section-card">
          <div class="card-title-row">
            <h3 class="card-title">🤖 AI推荐面试题</h3>
            <el-tag type="primary" effect="plain">AI根据职位和候选人简历自动生成</el-tag>
          </div>
          <div v-for="(q, i) in questions" :key="i" class="question-item">
            <div class="q-header">
              <span class="q-num">Q{{ i + 1 }}</span>
              <span class="q-title">{{ q.title }}</span>
              <el-tag :type="q.difficulty === '困难' ? 'danger' : q.difficulty === '中等' ? 'warning' : 'success'" size="small">{{ q.difficulty }}</el-tag>
            </div>
            <p class="q-ref" v-if="q.reference">📖 参考答案要点：{{ q.reference }}</p>
            <p class="q-criteria" v-if="q.criteria">📏 评分标准：{{ q.criteria }}</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">📝 面试笔记</h3>
          <el-input v-model="notes" type="textarea" :rows="8" placeholder="记录面试过程中的关键信息..." />
          <el-button type="primary" style="width:100%;margin-top:12px;">保存笔记</el-button>
        </el-card>
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">⏱️ 快速操作</h3>
          <el-button style="width:100%;margin-bottom:10px;" @click="$router.push(`/interviewer/evaluation/1`)">✍️ 填写面试评价</el-button>
          <el-button style="width:100%;margin-bottom:10px;" type="success">✅ 标记通过</el-button>
          <el-button style="width:100%;" type="danger" plain>❌ 标记不通过</el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const notes = ref('')

const questions = [
  { title: '请介绍你在支付系统中负责的核心模块，以及如何保证数据一致性？', difficulty: '中等', reference: '分布式事务方案（TCC/Saga）、幂等设计、对账机制', criteria: '说出至少2种方案 + 实际场景应用' },
  { title: '假设支付系统出现大量超时，你如何排查根因？', difficulty: '困难', reference: '监控→日志→链路追踪→数据库慢查询→GC日志→网络排查', criteria: '排查步骤系统化、考虑上下游依赖' },
  { title: 'Spring Cloud微服务相比单体架构的优势和挑战是什么？', difficulty: '中等', reference: '独立部署、技术异构、服务治理复杂性、分布式事务', criteria: '至少列出3优势+3挑战' },
  { title: 'Redis在你的项目中有哪些使用场景？遇到过什么问题？', difficulty: '简单', reference: '缓存、分布式锁、Session、消息队列；缓存穿透/雪崩/击穿', criteria: '3个以上使用场景+1个问题案例' },
  { title: '如果让你重新设计这个支付系统，你会做哪些改进？', difficulty: '中等', reference: '架构设计能力检验', criteria: '逻辑清晰、有前瞻性' }
]
</script>

<style scoped>
.section-card { border-radius: 12px; margin-bottom: 16px; }
.card-title { margin: 0 0 14px; font-size: 16px; color: #3E2723; padding-bottom: 10px; border-bottom: 1px solid #f0f0f0; }
.card-title-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; padding-bottom: 10px; border-bottom: 1px solid #f0f0f0; }
.card-title-row .card-title { margin: 0; padding: 0; border: none; }
.candidate-header { display: flex; justify-content: space-between; align-items: flex-start; }
.candidate-header h2 { margin: 0 0 10px; font-size: 20px; color: #3E2723; }
.meta-row { display: flex; gap: 16px; align-items: center; font-size: 13px; color: #999; flex-wrap: wrap; }
.resume-grid { display: flex; flex-wrap: wrap; gap: 12px; }
.resume-item { font-size: 13px; color: #555; background: #fafafa; padding: 8px 14px; border-radius: 8px; }
.resume-section p { font-size: 13px; color: #3E2723; margin: 8px 0 4px; }
.resume-section ul { padding-left: 20px; font-size: 13px; color: #666; line-height: 1.6; margin: 4px 0; }
.question-item { padding: 14px; margin-bottom: 8px; background: #fafafa; border-radius: 10px; border-left: 4px solid #ED8936; }
.q-header { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.q-num { min-width: 28px; height: 28px; border-radius: 50%; background: #ED8936; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 700; }
.q-title { flex: 1; font-size: 14px; color: #3E2723; font-weight: 500; }
.q-ref, .q-criteria { font-size: 12px; color: #888; margin: 4px 0 0; }
</style>
