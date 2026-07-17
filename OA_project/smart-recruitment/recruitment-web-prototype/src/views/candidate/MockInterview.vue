<template>
  <div class="page">
    <div class="page-header">
      <h2>🤖 AI模拟面试</h2>
      <p>选择目标职位，AI模拟真实面试场景，助您提前准备</p>
    </div>
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">面试配置</h3>
          <el-form :model="config" label-width="100px">
            <el-form-item label="目标职位">
              <el-select v-model="config.jobId" placeholder="选择职位" style="width:100%;">
                <el-option v-for="j in targetJobs" :key="j.id" :label="`${j.title} - ${j.company}`" :value="j.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="面试类型">
              <el-radio-group v-model="config.type">
                <el-radio-button value="technical">技术面试</el-radio-button>
                <el-radio-button value="hr">HR面试</el-radio-button>
                <el-radio-button value="comprehensive">综合面试</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="难度">
              <el-radio-group v-model="config.difficulty">
                <el-radio-button value="easy">初级</el-radio-button>
                <el-radio-button value="medium">中级</el-radio-button>
                <el-radio-button value="hard">高级</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="题目数量">
              <el-slider v-model="config.count" :min="3" :max="15" show-stops style="width:240px;" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="large" icon="VideoPlay" @click="startInterview" :disabled="interviewing">
                {{ interviewing ? '面试进行中...' : '开始模拟面试' }}
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
        <!-- 面试对话区 -->
        <el-card v-if="interviewing" shadow="never" class="section-card interview-box">
          <h3 class="card-title">
            💬 面试对话
            <el-tag type="primary" size="small" style="margin-left:10px;">第 {{ currentQ }}/{{ config.count }} 题</el-tag>
            <el-tag v-if="aiThinking" type="warning" size="small" style="margin-left:6px;">AI思考中...</el-tag>
          </h3>
          <div class="chat-area" ref="chatRef">
            <div v-for="(msg, i) in messages" :key="i" :class="['chat-msg', msg.role]">
              <div class="msg-avatar">
                <el-icon size="20"><component :is="msg.role === 'ai' ? 'Cpu' : 'User'" /></el-icon>
              </div>
              <div class="msg-bubble">
                <div class="msg-role">{{ msg.role === 'ai' ? 'AI面试官' : '我' }}</div>
                <div class="msg-text">{{ msg.content }}</div>
                <div v-if="msg.followUp" class="msg-follow">🔍 追问：{{ msg.followUp }}</div>
              </div>
            </div>
          </div>
          <div class="answer-bar" v-if="!finished">
            <el-input v-model="currentAnswer" placeholder="输入您的回答..." @keyup.enter="submitAnswer" :disabled="aiThinking">
              <template #append>
                <el-button type="primary" icon="Position" @click="submitAnswer" :loading="aiThinking">发送</el-button>
              </template>
            </el-input>
          </div>
          <div v-if="finished" class="interview-result">
            <el-result icon="success" title="模拟面试完成!" sub-title="AI正在分析您的表现...">
              <template #extra>
                <el-button type="primary" @click="showReport">查看面试报告</el-button>
                <el-button @click="resetInterview">再来一次</el-button>
              </template>
            </el-result>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">📊 面试技巧提示</h3>
          <el-collapse>
            <el-collapse-item title="STAR法则回答结构化问题" name="1">
              <p>Situation - 情境 / Task - 任务 / Action - 行动 / Result - 结果</p>
            </el-collapse-item>
            <el-collapse-item title="技术面试常见考察点" name="2">
              <ul><li>基础知识扎实程度</li><li>问题分析和解决思路</li><li>代码质量和最佳实践</li><li>系统设计能力</li></ul>
            </el-collapse-item>
            <el-collapse-item title="面试官想听到什么" name="3">
              <p>逻辑清晰、结构化表达，展示思考过程而非死记硬背的答案</p>
            </el-collapse-item>
          </el-collapse>
        </el-card>
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">📝 历史面试记录</h3>
          <div v-for="h in history" :key="h.id" class="history-item">
            <div class="his-top">
              <span class="his-job">{{ h.jobTitle }}</span>
              <el-tag :type="h.score >= 80 ? 'success' : h.score >= 60 ? 'warning' : 'danger'" size="small">{{ h.score }}分</el-tag>
            </div>
            <div class="his-time">{{ h.time }}</div>
            <el-button type="primary" link size="small" @click="$router.push('/candidate/mock-interview/report')">查看报告</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const interviewing = ref(false)
const finished = ref(false)
const aiThinking = ref(false)
const currentQ = ref(1)
const currentAnswer = ref('')
const chatRef = ref()

const config = reactive({ jobId: 1, type: 'technical', difficulty: 'medium', count: 5 })
const targetJobs = [{ id: 1, title: 'Java高级开发工程师', company: '某科技有限公司' }, { id: 2, title: '前端开发工程师（Vue）', company: '某网络技术有限公司' }, { id: 3, title: '数据分析师', company: '某数据科技集团' }]

const messages = ref([])
const history = [
  { id: 1, jobTitle: 'Java开发工程师', score: 82, time: '2024-07-14' },
  { id: 2, jobTitle: '全栈工程师', score: 68, time: '2024-07-10' }
]

const mockQuestions = [
  { q: '请简单介绍一下你自己，特别是与Java开发相关的经历。', followUp: '' },
  { q: '请解释一下Spring Boot的自动配置原理，以及你如何在项目中使用它。', followUp: '能结合你项目中实际遇到的问题说一下吗？' },
  { q: '你提到了微服务架构，在服务拆分时你遇到过哪些挑战？如何解决的？', followUp: '' },
  { q: '假设现在系统QPS突然从1000飙升到10000，你会如何排查和解决？', followUp: '除了数据库层面，还有哪些可能的瓶颈？' },
  { q: '请设计一个高并发的秒杀系统，核心要考虑哪些点？', followUp: '' }
]

function startInterview() {
  interviewing.value = true
  finished.value = false
  currentQ.value = 1
  messages.value = [{ role: 'ai', content: `欢迎参加${config.type === 'technical' ? '技术' : config.type === 'hr' ? 'HR' : '综合'}面试！本次面试共${config.count}题，难度：${config.difficulty === 'easy' ? '初级' : config.difficulty === 'medium' ? '中级' : '高级'}。准备好了吗？`, followUp: '' }]
  setTimeout(() => { messages.value.push({ role: 'ai', content: mockQuestions[0].q, followUp: mockQuestions[0].followUp }) }, 1500)
}

function submitAnswer() {
  if (!currentAnswer.value.trim() || aiThinking.value) return
  messages.value.push({ role: 'user', content: currentAnswer.value.trim() })
  currentAnswer.value = ''
  if (currentQ.value >= config.count) {
    aiThinking.value = true
    setTimeout(() => {
      messages.value.push({ role: 'ai', content: '好的，感谢您的回答！本次模拟面试到此结束。您可以查看系统生成的面试报告。' })
      aiThinking.value = false; finished.value = true; ElMessage.success('面试完成！')
    }, 1500)
    return
  }
  aiThinking.value = true
  setTimeout(() => {
    currentQ.value++
    messages.value.push({ role: 'ai', content: mockQuestions[currentQ.value - 1].q, followUp: mockQuestions[currentQ.value - 1].followUp })
    aiThinking.value = false
  }, 1200)
}

function resetInterview() { interviewing.value = false; finished.value = false; messages.value = []; currentQ.value = 1 }
function showReport() { ElMessage.info('正在生成面试报告...'); setTimeout(() => window.location.hash = '/candidate/mock-interview/report', 1000) }
</script>

<style scoped>
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.page-header p { color: #999; margin: 6px 0 0; }
.section-card { border-radius: 12px; margin-bottom: 16px; }
.card-title { margin: 0 0 16px; font-size: 16px; color: #3E2723; padding-bottom: 10px; border-bottom: 1px solid #f0f0f0; }
.interview-box .card-title { display: flex; align-items: center; }
.chat-area { max-height: 400px; overflow-y: auto; margin-bottom: 16px; padding-right: 8px; }
.chat-msg { display: flex; gap: 10px; margin-bottom: 14px; }
.chat-msg.user { flex-direction: row-reverse; }
.msg-avatar { width: 36px; height: 36px; border-radius: 50%; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.chat-msg.ai .msg-avatar { background: #fdf4e8; color: #ED8936; }
.chat-msg.user .msg-avatar { background: #e8f4fd; color: #409EFF; }
.msg-bubble { max-width: 80%; padding: 12px 16px; border-radius: 12px; }
.chat-msg.ai .msg-bubble { background: #fafafa; border: 1px solid #eee; }
.chat-msg.user .msg-bubble { background: #ecf5ff; border: 1px solid #d9ecff; }
.msg-role { font-size: 12px; font-weight: 600; margin-bottom: 4px; color: #999; }
.msg-text { font-size: 14px; line-height: 1.6; color: #333; white-space: pre-wrap; }
.msg-follow { margin-top: 8px; padding: 8px 12px; background: #fef0e8; border-radius: 8px; font-size: 13px; color: #E76F51; }
.answer-bar { margin-top: 8px; }
.interview-result { padding: 20px 0; }
.history-item { padding: 12px; margin-bottom: 8px; background: #fafafa; border-radius: 8px; }
.his-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; }
.his-job { font-weight: 600; font-size: 14px; color: #3E2723; }
.his-time { font-size: 12px; color: #999; margin-bottom: 6px; }
</style>
