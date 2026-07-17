<template>
  <div class="page">
    <div class="page-header">
      <h2>🤖 AI生成面试题</h2>
      <el-button icon="ArrowLeft" @click="$router.back()" link>返回</el-button>
    </div>
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card shadow="never" class="section-card">
          <el-form :model="config" label-width="100px">
            <el-form-item label="目标职位">
              <el-select v-model="config.jobId" style="width:100%;">
                <el-option v-for="j in jobs" :key="j.id" :label="`${j.title} — ${j.candidate}`" :value="j.id" />
              </el-select>
            </el-form-item>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="题目类型">
                  <el-select v-model="config.type" style="width:100%;">
                    <el-option label="技术基础" value="基础" /><el-option label="项目追问" value="项目" /><el-option label="系统设计" value="设计" /><el-option label="算法" value="算法" /><el-option label="综合" value="综合" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="难度">
                  <el-select v-model="config.difficulty" style="width:100%;">
                    <el-option label="简单" value="简单" /><el-option label="中等" value="中等" /><el-option label="困难" value="困难" /><el-option label="混合" value="混合" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="数量">
                  <el-input-number v-model="config.count" :min="1" :max="20" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item>
              <el-button type="primary" size="large" icon="MagicStick" @click="generate" :loading="loading">
                {{ loading ? 'AI生成中...' : '🤖 AI生成面试题' }}
              </el-button>
              <el-button v-if="questions.length" type="success" size="large" icon="Download">导出Word</el-button>
            </el-form-item>
          </el-form>
        </el-card>
        <!-- 生成的题目 -->
        <el-card v-if="questions.length" shadow="never" class="section-card">
          <h3 class="card-title">📝 生成的面试题（共 {{ questions.length }} 题）</h3>
          <div v-for="(q, i) in questions" :key="i" class="q-item">
            <div class="q-header">
              <span class="q-num">Q{{ i + 1 }}</span>
              <span class="q-title">{{ q.title }}</span>
              <el-tag :type="q.difficulty === '困难' ? 'danger' : q.difficulty === '中等' ? 'warning' : 'success'" size="small">{{ q.difficulty }}</el-tag>
              <el-tag type="info" size="small" effect="plain">{{ q.type }}</el-tag>
            </div>
            <div v-if="q.referenceAnswer" class="q-ref"><b>参考答案：</b>{{ q.referenceAnswer }}</div>
            <div v-if="q.criteria" class="q-criteria"><b>评分维度：</b>{{ q.criteria }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">💡 出题策略</h3>
          <el-steps direction="vertical" :active="1">
            <el-step title="分析职位JD" description="提取核心技术栈和要求" />
            <el-step title="分析候选人简历" description="定位候选人的经验亮点和盲区" />
            <el-step title="智能生成题目" description="基于Gap分析生成针对性题目" />
          </el-steps>
        </el-card>
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">📜 历史生成记录</h3>
          <div v-for="h in history" :key="h.id" class="his-item">
            <div class="his-top">
              <span class="his-candidate">{{ h.candidate }}</span>
              <span class="his-count">{{ h.count }}题</span>
            </div>
            <div class="his-time">{{ h.time }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)

const config = reactive({ jobId: 1, type: '基础', difficulty: '混合', count: 6 })
const jobs = [
  { id: 1, title: 'Java高级开发工程师', candidate: '张三' },
  { id: 2, title: '前端开发工程师（Vue）', candidate: '李四' },
  { id: 3, title: '测试开发工程师', candidate: '周九' }
]

const questions = ref([])
const history = [
  { id: 1, candidate: '王五(数据分析师)', count: 5, time: '2024-07-17' },
  { id: 2, candidate: '赵六(DevOps)', count: 6, time: '2024-07-16' }
]

function generate() {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    questions.value = [
      { title: '请解释Java内存模型（JMM）中volatile关键字的作用和原理', type: '基础', difficulty: '中等', referenceAnswer: '保证可见性+禁止指令重排序，结合happens-before原则和内存屏障说明', criteria: '可见性原理、内存屏障、使用场景' },
      { title: '你在支付系统中如何保证分布式事务的一致性？', type: '项目', difficulty: '困难', referenceAnswer: 'TCC/Saga模式选型依据、幂等设计、补偿机制、对账兜底方案', criteria: '方案对比、实际落地、异常处理' },
      { title: '如果QPS从1000飙升到10000，从哪些层面排查和优化？', type: '设计', difficulty: '中等', referenceAnswer: '监控告警→日志分析→慢SQL→缓存策略→限流降级→水平扩容', criteria: '排查系统性、方案合理性' },
      { title: 'Redis分布式锁的实现原理和RedLock算法', type: '基础', difficulty: '中等', referenceAnswer: 'SET NX EX、锁续期、RedLock的5节点投票机制、适用场景和局限性', criteria: '原理清晰、知道局限性' },
      { title: 'Spring Cloud Gateway和Zuul的区别，以及你如何选择？', type: '基础', difficulty: '简单', referenceAnswer: 'Gateway基于WebFlux异步非阻塞、Zuul1同步阻塞、Zuul2异步，性能选择', criteria: '能说出核心区别' },
      { title: '设计一个支持千万级用户的IM系统核心架构', type: '设计', difficulty: '困难', referenceAnswer: 'WebSocket长连接、消息队列异步解耦、分布式ID生成、多端同步、消息可靠性', criteria: '分层设计、关键组件、高可用考虑' }
    ]
    ElMessage.success('面试题生成成功！')
  }, 2000)
}
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.section-card { border-radius: 12px; margin-bottom: 16px; }
.card-title { margin: 0 0 14px; font-size: 16px; color: #3E2723; padding-bottom: 10px; border-bottom: 1px solid #f0f0f0; }
.q-item { padding: 14px; margin-bottom: 8px; background: #fafafa; border-radius: 10px; border-left: 4px solid #409EFF; }
.q-header { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; flex-wrap: wrap; }
.q-num { min-width: 28px; height: 28px; border-radius: 50%; background: #409EFF; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 700; }
.q-title { flex: 1; font-size: 14px; color: #3E2723; font-weight: 500; }
.q-ref, .q-criteria { font-size: 12px; color: #888; margin-top: 4px; line-height: 1.6; }
.his-item { padding: 10px; margin-bottom: 6px; background: #fafafa; border-radius: 8px; }
.his-top { display: flex; justify-content: space-between; margin-bottom: 4px; }
.his-candidate { font-size: 13px; font-weight: 500; color: #3E2723; }
.his-count { font-size: 12px; color: #ED8936; }
.his-time { font-size: 12px; color: #bbb; }
</style>
