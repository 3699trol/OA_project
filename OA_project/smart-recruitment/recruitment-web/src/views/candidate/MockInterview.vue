<template>
  <div class="page">
    <div class="page-header">
      <h2>🤖 AI 模拟仿真面试</h2>
      <p>依托大语言模型深度融合您的个人简历与目标岗位的描述要求，100%还原真实大厂面试现场</p>
    </div>
    
    <el-row :gutter="24">
      <!-- 配置及职位详情卡 -->
      <el-col :span="8">
        <el-card shadow="never" class="config-card mb-4" :disabled="started">
          <template #header>
            <span class="panel-title"><el-icon><Setting /></el-icon> 面试场设配置</span>
          </template>
          
          <el-form :model="config" label-position="top">
            <el-form-item label="目标职位" required>
              <el-select v-model="config.jobId" placeholder="选择目标投递职位" style="width:100%;" :disabled="started">
                <el-option 
                  v-for="job in jobOptions" 
                  :key="job.id" 
                  :label="job.jobName" 
                  :value="job.id" 
                />
              </el-select>
            </el-form-item>
            
            <el-form-item label="面试类型">
              <el-radio-group v-model="config.type" :disabled="started" size="small">
                <el-radio-button value="technical">专业技术面</el-radio-button>
                <el-radio-button value="hr">HR综合面</el-radio-button>
                <el-radio-button value="comprehensive">架构/总监面</el-radio-button>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item label="难度等级">
              <el-radio-group v-model="config.difficulty" :disabled="started" size="small">
                <el-radio-button value="easy">初级</el-radio-button>
                <el-radio-button value="medium">中级</el-radio-button>
                <el-radio-button value="hard">高级</el-radio-button>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="拟出题数量">
              <div class="slider-wrapper">
                <el-slider v-model="config.count" :min="3" :max="10" show-stops :disabled="started" />
              </div>
            </el-form-item>
            
            <el-form-item v-if="!started" class="mt-4">
              <el-button type="primary" size="large" class="start-btn" icon="VideoPlay" @click="startInterview">
                进入 AI 面试间
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 职位要求预览 -->
        <el-card v-if="selectedJob" shadow="never" class="job-detail-preview-card">
          <template #header>
            <div class="card-title-row" style="margin: 0; padding: 0; border: none; display: flex; justify-content: space-between; align-items: center; width: 100%;">
              <span class="panel-title"><el-icon><Briefcase /></el-icon> 岗位JD与技能契合</span>
              <el-button 
                type="primary" 
                link 
                icon="Edit" 
                :disabled="started" 
                @click="openJdEditor"
              >
                自定义编辑
              </el-button>
            </div>
          </template>
          <div class="job-meta-row">
            <span class="job-title-tag">{{ customTitle || selectedJob.jobName }}</span>
            <el-tag type="danger" size="small">{{ customSalary || (selectedJob.salaryMin != null && selectedJob.salaryMax != null ? (selectedJob.salaryMin/1000) + 'K-' + (selectedJob.salaryMax/1000) + 'K' : '-') }}</el-tag>
          </div>
          <div class="meta-small-text">
            <span>地点: {{ customCity || selectedJob.city || selectedJob.location || '北京' }}</span> | <span>部门: {{ customDept || selectedJob.department || '研发部' }}</span>
          </div>
          
          <el-divider style="margin: 12px 0;" />
          
          <div class="jd-section">
            <label>岗位描述：</label>
            <p class="jd-text" style="white-space: pre-line;">{{ customJd || '暂无岗位描述' }}</p>
          </div>
          <div class="jd-section mt-2">
            <label>任职资格：</label>
            <p class="jd-text" style="white-space: pre-line;">{{ customRequirements || '暂无任职资格要求' }}</p>
          </div>
        </el-card>
      </el-col>

      <!-- 面试大厅对话区域 -->
      <el-col :span="16">
        <el-card v-if="!started" shadow="never" class="lobby-welcome-card">
          <div class="welcome-inner">
            <div class="ai-avatar-glow">🤖</div>
            <h2>欢迎来到 AI 仿真模拟面试间</h2>
            <p class="desc">
              本系统将基于您的<b>简历画像</b>、目标岗位的<b>职位描述(JD)</b>及<b>任职条件</b>，智能生成具有极强针对性的、由浅入深的定制面试题。
            </p>
            <div class="features-flow">
              <div class="flow-item">
                <div class="icon-wrap"><el-icon color="#e76f51"><Document /></el-icon></div>
                <h4>对齐简历画像</h4>
                <span>提取简历核心技能和项目盲区</span>
              </div>
              <div class="flow-item">
                <div class="icon-wrap"><el-icon color="#e76f51"><Briefcase /></el-icon></div>
                <h4>对齐目标职位要求</h4>
                <span>针对JD难点进行核心发问</span>
              </div>
              <div class="flow-item">
                <div class="icon-wrap"><el-icon color="#e76f51"><ChatDotRound /></el-icon></div>
                <h4>STAR 面试评估</h4>
                <span>评估您的技术解决思路和表达</span>
              </div>
            </div>
            <div class="lobby-action">
              <el-button type="primary" size="large" class="start-btn-large" @click="startInterview">
                立刻开始 AI 面试
              </el-button>
            </div>
          </div>
        </el-card>

        <!-- 对话框 -->
        <el-card v-else shadow="never" class="interview-chat-card">
          <template #header>
            <div class="chat-header-flex">
              <div class="interviewer-meta">
                <span class="avatar-sm">🤖</span>
                <div class="meta-txt">
                  <span class="name">AI 资深面试官 · 小智</span>
                  <span class="status">在线提问中 ({{ chatCount }}/{{ config.count }} 轮)</span>
                </div>
              </div>
              <el-button type="danger" icon="SwitchButton" size="small" @click="confirmQuit">
                退出面试间
              </el-button>
            </div>
          </template>

          <!-- 聊天内容区 -->
          <div class="chat-messages-container" ref="chatScrollContainer">
            <div v-for="(msg, index) in messages" :key="index" class="chat-bubble-row" :class="msg.role">
              <div class="bubble-avatar">{{ msg.role === 'ai' ? '🤖' : '👨‍💻' }}</div>
              <div class="bubble-content-wrap">
                <div class="bubble-speaker">{{ msg.role === 'ai' ? '面试官' : '我' }}</div>
                <div class="bubble-text">{{ msg.content }}</div>
              </div>
            </div>
            
            <div v-if="loadingChat" class="chat-bubble-row ai">
              <div class="bubble-avatar">🤖</div>
              <div class="bubble-content-wrap">
                <div class="bubble-speaker">面试官</div>
                <div class="bubble-text loading-text">
                  <el-icon class="is-loading"><Loading /></el-icon> 正在针对您的回答进行追问思考...
                </div>
              </div>
            </div>
          </div>

          <!-- 快捷建议及回答建议 -->
          <div v-if="suggestedQuestions.length > 0 && !loadingChat" class="suggested-replies-row">
            <span class="title">💡 您可以说：</span>
            <div class="reply-chips">
              <el-tag 
                v-for="(chip, idx) in suggestedQuestions" 
                :key="idx" 
                class="reply-chip" 
                @click="useSuggestedReply(chip)"
                effect="plain"
                type="warning"
              >
                {{ chip }}
              </el-tag>
            </div>
          </div>

          <!-- 输入区 -->
          <div class="chat-input-row">
            <div class="voice-sim-panel">
              <el-button type="info" icon="Mic" circle @click="simulateVoiceDictation" :disabled="loadingChat" title="模拟语音转文字" />
            </div>
            <el-input
              v-model="userInput"
              type="textarea"
              :rows="3"
              placeholder="请输入您的作答内容（可结合 STAR 法则，即：情境、任务、行动、结果进行细致阐述）..."
              :disabled="loadingChat"
              @keyup.enter.ctrl="sendMessage"
            />
            <div class="input-actions">
              <span class="tip">Ctrl + Enter 快捷发送</span>
              <div class="btns">
                <el-button 
                  v-if="chatCount >= 3" 
                  type="success" 
                  icon="DocumentChecked" 
                  @click="finishInterview"
                  :loading="loadingChat"
                >
                  结束面试并生成 AI 评估报告
                </el-button>
                <el-button 
                  type="primary" 
                  icon="Promotion" 
                  @click="sendMessage"
                  :disabled="!userInput.trim() || loadingChat"
                >
                  提交作答
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 自定义职位及JD编辑弹窗 -->
    <el-dialog v-model="jdDialogVisible" title="✍️ 自定义修改目标岗位与技能门槛" width="620px" append-to-body>
      <el-form label-position="top">
        <el-row :gutter="16">
          <el-col :span="14">
            <el-form-item label="职位名称" required>
              <el-input v-model="customTitle" placeholder="如：高级Java开发工程师" />
            </el-form-item>
          </el-col>
          <el-col :span="10">
            <el-form-item label="薪资范围">
              <el-input v-model="customSalary" placeholder="如：25K-35K" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="工作地点">
              <el-input v-model="customCity" placeholder="如：北京" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="归属部门">
              <el-input v-model="customDept" placeholder="如：技术部" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="岗位描述 (Job Description)" required>
          <el-input 
            v-model="customJd" 
            type="textarea" 
            :rows="4" 
            placeholder="请详细描述此职位的主要职责与核心产出要求..." 
          />
        </el-form-item>
        <el-form-item label="任职资格 / 技能要求" required>
          <el-input 
            v-model="customRequirements" 
            type="textarea" 
            :rows="6" 
            placeholder="请输入此职位所要求的硬性技术栈及专业素养要求，每行一条..." 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="jdDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCustomJd">确认保存并应用</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getJobList } from '@/api/job'
import { getMyResume } from '@/api/resume'
import { startMockInterview, chatMockInterview, submitMockInterview } from '@/api/ai'

const router = useRouter()
const started = ref(false)
const loadingChat = ref(false)
const chatCount = ref(0)
const userInput = ref('')
const sessionId = ref('')

const config = reactive({
  jobId: null,
  type: 'technical',
  difficulty: 'medium',
  count: 5
})

const jobOptions = ref([])
const resumeData = ref(null)
const messages = ref([])
const suggestedQuestions = ref([])
const chatScrollContainer = ref(null)

const selectedJob = computed(() => {
  if (!config.jobId) return null
  return jobOptions.value.find(j => j.id === config.jobId) || null
})

// === 自定义 JD / 技能契合度字段 ===
const customTitle = ref('')
const customSalary = ref('')
const customCity = ref('')
const customDept = ref('')
const customJd = ref('')
const customRequirements = ref('')

const jdDialogVisible = ref(false)

function openJdEditor() {
  jdDialogVisible.value = true
}

function saveCustomJd() {
  if (!customTitle.value.trim()) {
    ElMessage.warning('请输入目标职位名称')
    return
  }
  jdDialogVisible.value = false
  ElMessage.success('职位信息及JD自定义保存成功！')
}

// 监听选中的目标职位，自动同步预设 JD 描述与任职技能条件
watch(() => config.jobId, (newJobId) => {
  const job = jobOptions.value.find(j => j.id === newJobId)
  if (job) {
    customTitle.value = job.jobName || ''
    customSalary.value = (job.salaryMin != null && job.salaryMax != null ? (job.salaryMin/1000) + 'K-' + (job.salaryMax/1000) + 'K' : '') || ''
    customCity.value = job.city || job.location || ''
    customDept.value = job.department || '研发部'
    customJd.value = job.description || ''
    customRequirements.value = job.requirements || ''
  } else {
    customTitle.value = ''
    customSalary.value = ''
    customCity.value = ''
    customDept.value = '研发部'
    customJd.value = ''
    customRequirements.value = ''
  }
}, { immediate: true })

// 深度监听职位列表加载动作以赋初始值
watch(jobOptions, (newOptions) => {
  if (config.jobId) {
    const job = newOptions.find(j => j.id === config.jobId)
    if (job) {
      customTitle.value = job.jobName || ''
      customSalary.value = (job.salaryMin != null && job.salaryMax != null ? (job.salaryMin/1000) + 'K-' + (job.salaryMax/1000) + 'K' : '') || ''
      customCity.value = job.city || job.location || ''
      customDept.value = job.department || '研发部'
      customJd.value = job.description || ''
      customRequirements.value = job.requirements || ''
    }
  }
}, { deep: true })

onMounted(async () => {
  await Promise.all([loadJobs(), loadResume()])
})

async function loadJobs() {
  try {
    const res = await getJobList({ status: 1 })
    if (res && res.data) {
      jobOptions.value = res.data.records || (Array.isArray(res.data) ? res.data : [])
      if (jobOptions.value.length > 0) {
        config.jobId = jobOptions.value[0].id // 默认选择第一个
      }
    }
  } catch (error) {
    console.error('加载职位列表失败:', error)
  }
}

async function loadResume() {
  try {
    const res = await getMyResume()
    if (res && res.data) {
      resumeData.value = res.data
    }
  } catch (error) {
    console.error('加载个人简历失败:', error)
  }
}

async function startInterview() {
  if (!config.jobId) {
    ElMessage.warning('请先选择目标职位')
    return
  }
  
  started.value = true
  loadingChat.value = true
  chatCount.value = 0
  messages.value = []
  suggestedQuestions.value = []

  try {
    const res = await startMockInterview({
      jobId: config.jobId,
      jobTitle: customTitle.value || selectedJob.value?.jobName || '', // 提交用户自定义编辑后的职位名称
      jobRequirements: customRequirements.value || '', // 提交用户自定义编辑后的技能要求
      jobDescription: customJd.value || '',           // 提交用户自定义编辑后的岗位描述
      resumeData: resumeData.value,
      type: config.type,
      difficulty: config.difficulty,
      count: config.count
    })

    if (res && res.data) {
      sessionId.value = res.data.sessionId
      messages.value.push({
        role: 'ai',
        content: res.data.welcomeMessage || '您好！我是本次的 AI 仿真面试官。非常高兴今天能和您探讨这个职位的相关技术。首先，请您做一个自我介绍，以便我更全面地了解您的背景。'
      })
    }
  } catch (error) {
    started.value = false
    ElMessage.error('无法启动模拟面试，请检查网络或系统参数配置')
  } finally {
    loadingChat.value = false
    scrollToBottom()
  }
}

async function sendMessage() {
  const content = userInput.value.trim()
  if (!content || loadingChat.value) return

  messages.value.push({
    role: 'user',
    content
  })
  userInput.value = ''
  loadingChat.value = true
  scrollToBottom()

  try {
    const res = await chatMockInterview({
      sessionId: sessionId.value,
      message: content,
      chatCount: chatCount.value,
      jobTitle: selectedJob.value?.jobName || ''
    })

    if (res && res.data) {
      chatCount.value++
      messages.value.push({
        role: 'ai',
        content: res.data.reply
      })
      suggestedQuestions.value = res.data.suggestedQuestions || []
    }
  } catch (error) {
    ElMessage.error('作答提交失败，请重试')
  } finally {
    loadingChat.value = false
    scrollToBottom()
  }
}

function useSuggestedReply(text) {
  userInput.value = text
}

function simulateVoiceDictation() {
  const presets = [
    '在这个分布式系统的购物车模块，我们采用了本地多级缓存，Redis 缓存结合 JVM 的 Caffeine，并在发生写操作时，采用 MQ 进行消息广播使其他服务节点的 JVM 缓存失效，最大程度提高并发度。',
    '我们采用 RocketMQ 事务消息来处理结算微服务与账户、库存微服务间的分布式事务。主要利用了其半消息发送和反向事务回查机制，保障最终一致性。',
    '对于 JVM 垃圾回收，我在生产环境中遇到过多次 Full GC 引发的 Stop The World，当时排查发现是因为内存泄漏。利用 jmap 和 jvisualvm 工具定位，发现有个 ThreadLocal 线程池使用完后未调用 remove，导致大量小对象堆积，优化后恢复了正常。'
  ]
  const randomPreset = presets[Math.floor(Math.random() * presets.length)]
  
  ElMessage.info('🎤 正在识别您的模拟语音输入...')
  let i = 0
  const interval = setInterval(() => {
    if (i < randomPreset.length) {
      userInput.value += randomPreset[i]
      i++
    } else {
      clearInterval(interval)
      ElMessage.success('语音转文字模拟完成')
    }
  }, 20)
}

async function finishInterview() {
  if (chatCount.value < 2) {
    ElMessage.warning('面试对话轮次太少，建议至少回答两到三个问题后再生成报告')
    return
  }

  loadingChat.value = true
  try {
    const res = await submitMockInterview({
      sessionId: sessionId.value,
      jobTitle: selectedJob.value?.jobName || ''
    })
    if (res && res.data) {
      ElMessage.success('🎉 面试已顺利结束！AI 资深面试官正在撰写全维度能力分析报告...')
      setTimeout(() => {
        router.push({
          path: '/candidate/mock-interview/report',
          query: { id: res.data.id }
        })
      }, 1500)
    }
  } catch (error) {
    ElMessage.error('无法生成面试报告')
  } finally {
    loadingChat.value = false
  }
}

function confirmQuit() {
  ElMessageBox.confirm(
    '当前面试正在进行中，退出将无法保存本次对话记录并生成能力报告。确定要退出吗？',
    '安全退出提示',
    {
      confirmButtonText: '确定退出',
      cancelButtonText: '继续面试',
      type: 'warning',
    }
  ).then(() => {
    started.value = false
    chatCount.value = 0
    messages.value = []
  }).catch(() => {})
}

function scrollToBottom() {
  nextTick(() => {
    if (chatScrollContainer.value) {
      chatScrollContainer.value.scrollTop = chatScrollContainer.value.scrollHeight
    }
  })
}
</script>

<style scoped>
.page {
  padding: 4px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  color: #1a1d21;
}

.page-header p {
  color: #7a8590;
  margin: 6px 0 0;
  font-size: 13px;
}

.mb-4 {
  margin-bottom: 16px;
}

.mt-4 {
  margin-top: 16px;
}

.panel-title {
  font-size: 15px;
  font-weight: 700;
  color: #2d3748;
  display: flex;
  align-items: center;
  gap: 8px;
}

.config-card {
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.slider-wrapper {
  padding: 0 10px;
  width: 100%;
}

.start-btn {
  width: 100%;
  border-radius: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #e76f51 0%, #f4a261 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(231, 111, 81, 0.25);
}

.start-btn:hover {
  background: linear-gradient(135deg, #f4a261 0%, #e76f51 104%);
}

/* 职位JD卡 */
.job-detail-preview-card {
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  max-height: calc(100vh - 460px);
  overflow-y: auto;
}

.job-meta-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.job-title-tag {
  font-size: 14px;
  font-weight: 700;
  color: #2d3748;
}

.meta-small-text {
  font-size: 11px;
  color: #a0aec0;
  margin-top: 4px;
}

.jd-section label {
  display: block;
  font-size: 12px;
  font-weight: 700;
  color: #e76f51;
  margin-bottom: 4px;
}

.jd-text {
  font-size: 12px;
  color: #4a5568;
  line-height: 1.5;
  margin: 0;
  max-height: 120px;
  overflow-y: auto;
}

/* 模拟面试大厅欢迎面板 */
.lobby-welcome-card {
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  height: calc(100vh - 160px);
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
}

.welcome-inner {
  max-width: 580px;
  text-align: center;
  margin: 0 auto;
  padding: 20px;
}

.ai-avatar-glow {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e76f51 0%, #f4a261 100%);
  color: white;
  font-size: 32px;
  line-height: 72px;
  display: inline-block;
  margin-bottom: 24px;
  box-shadow: 0 8px 24px rgba(231, 111, 81, 0.35);
}

.welcome-inner h2 {
  font-size: 22px;
  color: #1a1d21;
  margin: 0 0 12px 0;
}

.welcome-inner .desc {
  font-size: 14px;
  color: #4a5568;
  line-height: 1.6;
  margin-bottom: 40px;
}

.welcome-inner .desc b {
  color: #e76f51;
}

.features-flow {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 48px;
}

.flow-item {
  flex: 1;
  padding: 16px 12px;
  background-color: #f8fafc;
  border-radius: 10px;
  border: 1px solid #edf2f7;
}

.flow-item .icon-wrap {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: white;
  line-height: 44px;
  display: inline-block;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  margin-bottom: 12px;
  font-size: 18px;
}

.flow-item h4 {
  margin: 0 0 6px 0;
  font-size: 13px;
  color: #2d3748;
}

.flow-item span {
  font-size: 11px;
  color: #718096;
  line-height: 1.4;
  display: block;
}

.start-btn-large {
  padding: 12px 48px;
  border-radius: 24px;
  font-size: 15px;
  font-weight: 700;
  background: linear-gradient(135deg, #e76f51 0%, #f4a261 100%);
  border: none;
  box-shadow: 0 6px 18px rgba(231, 111, 81, 0.3);
}

/* 聊天间卡片 */
.interview-chat-card {
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  height: calc(100vh - 160px);
  display: flex;
  flex-direction: column;
}

.interview-chat-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 0;
  overflow: hidden;
}

.chat-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.interviewer-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar-sm {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background-color: #f7fafc;
  border: 1px solid #edf2f7;
  font-size: 20px;
  text-align: center;
  line-height: 34px;
}

.meta-txt {
  display: flex;
  flex-direction: column;
}

.meta-txt .name {
  font-size: 14px;
  font-weight: 700;
  color: #2d3748;
}

.meta-txt .status {
  font-size: 11px;
  color: #4a5568;
}

/* 消息流体滚屏容器 */
.chat-messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background-color: #f8fafc;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.chat-bubble-row {
  display: flex;
  gap: 12px;
  max-width: 85%;
}

.chat-bubble-row.ai {
  align-self: flex-start;
}

.chat-bubble-row.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.bubble-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background-color: white;
  border: 1px solid #e2e8f0;
  font-size: 18px;
  text-align: center;
  line-height: 34px;
  flex-shrink: 0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.02);
}

.bubble-content-wrap {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.chat-bubble-row.user .bubble-speaker {
  text-align: right;
}

.bubble-speaker {
  font-size: 11px;
  color: #a0aec0;
  font-weight: 600;
}

.bubble-text {
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 13.5px;
  line-height: 1.6;
  white-space: pre-wrap;
}

.chat-bubble-row.ai .bubble-text {
  background-color: white;
  color: #2d3748;
  border: 1px solid #edf2f7;
  border-top-left-radius: 2px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.01);
}

.chat-bubble-row.user .bubble-text {
  background: linear-gradient(135deg, #e76f51 0%, #f4a261 100%);
  color: white;
  border-top-right-radius: 2px;
  box-shadow: 0 4px 12px rgba(231, 111, 81, 0.15);
}

.loading-text {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #e76f51 !important;
  font-weight: 600;
}

/* 快捷建议行 */
.suggested-replies-row {
  background-color: #fffaf0;
  border-top: 1px solid #fef3e7;
  border-bottom: 1px solid #fef3e7;
  padding: 10px 24px;
  display: flex;
  align-items: center;
  gap: 8px;
  overflow-x: auto;
}

.suggested-replies-row::-webkit-scrollbar {
  height: 4px;
}

.suggested-replies-row .title {
  font-size: 11px;
  color: #f4a261;
  font-weight: 700;
  flex-shrink: 0;
}

.reply-chips {
  display: flex;
  gap: 8px;
}

.reply-chip {
  cursor: pointer;
  transition: transform 0.1s, background-color 0.1s;
  border-radius: 14px;
}

.reply-chip:hover {
  transform: scale(1.02);
  background-color: #fde8e4;
}

/* 作答输入区 */
.chat-input-row {
  padding: 16px 24px;
  background-color: white;
  border-top: 1px solid #f1f3f9;
  position: relative;
  display: flex;
  gap: 14px;
  align-items: flex-start;
}

.voice-sim-panel {
  flex-shrink: 0;
  padding-top: 4px;
}

.chat-input-row :deep(.el-textarea__inner) {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 10px 14px;
  font-size: 13px;
  resize: none;
}

.chat-input-row :deep(.el-textarea__inner:focus) {
  border-color: #f4a261;
  box-shadow: 0 0 0 1px rgba(244, 162, 97, 0.2);
}

.input-actions {
  position: absolute;
  bottom: 24px;
  right: 36px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: calc(100% - 100px);
  pointer-events: none;
}

.input-actions .tip {
  font-size: 11px;
  color: #cbd5e0;
}

.input-actions .btns {
  pointer-events: auto;
  display: flex;
  gap: 8px;
}

.input-actions .btns .el-button {
  height: 32px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 600;
}
</style>
