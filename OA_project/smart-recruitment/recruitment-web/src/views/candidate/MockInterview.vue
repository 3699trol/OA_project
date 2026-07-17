<template>
  <div class="page">
    <div class="page-header"><h2>🤖 AI模拟面试</h2><p>选择目标职位，AI模拟真实面试场景</p></div>
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">面试配置</h3>
          <el-form :model="config" label-width="100px">
            <el-form-item label="目标职位">
              <el-select v-model="config.jobId" placeholder="选择职位" style="width:100%;">
                <!-- TODO: 从 getJobList() 加载职位选项 -->
                <el-option label="TODO: 加载职位列表" value="" disabled />
              </el-select>
            </el-form-item>
            <el-form-item label="面试类型">
              <el-radio-group v-model="config.type"><el-radio-button value="technical">技术面试</el-radio-button><el-radio-button value="hr">HR面试</el-radio-button><el-radio-button value="comprehensive">综合面试</el-radio-button></el-radio-group>
            </el-form-item>
            <el-form-item label="难度">
              <el-radio-group v-model="config.difficulty"><el-radio-button value="easy">初级</el-radio-button><el-radio-button value="medium">中级</el-radio-button><el-radio-button value="hard">高级</el-radio-button></el-radio-group>
            </el-form-item>
            <el-form-item label="题目数量"><el-slider v-model="config.count" :min="3" :max="15" show-stops style="width:200px;" /></el-form-item>
            <el-form-item><el-button type="primary" size="large" icon="VideoPlay" @click="startInterview">开始面试</el-button></el-form-item>
          </el-form>
        </el-card>
        <el-card v-if="started" shadow="never" class="section-card">
          <h3 class="card-title">💬 面试对话</h3>
          <!-- TODO: 对接AI模拟面试接口 sendMockAnswer() -->
          <el-empty description="TODO: 对接AI面试接口" :image-size="60" />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">📊 面试技巧</h3>
          <el-collapse>
            <el-collapse-item title="STAR法则" name="1"><p>Situation / Task / Action / Result</p></el-collapse-item>
            <el-collapse-item title="技术面试要点" name="2"><ul><li>基础扎实程度</li><li>问题分析能力</li><li>系统设计思路</li></ul></el-collapse-item>
          </el-collapse>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
// import { startMockInterview, sendMockAnswer } from '@/api/ai'

const started = ref(false)
const config = reactive({ jobId: null, type: 'technical', difficulty: 'medium', count: 5 })

function startInterview() {
  if (!config.jobId) { ElMessage.warning('请选择目标职位'); return }
  started.value = true
  // TODO: const res = await startMockInterview(config)
  ElMessage.info('TODO: 对接AI模拟面试接口')
}
</script>

<style scoped>
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.page-header p { color: #999; margin: 6px 0 0; }
.section-card { border-radius: 12px; margin-bottom: 16px; }
.card-title { margin: 0 0 14px; font-size: 16px; color: #3E2723; padding-bottom: 10px; border-bottom: 1px solid #f0f0f0; }
</style>
