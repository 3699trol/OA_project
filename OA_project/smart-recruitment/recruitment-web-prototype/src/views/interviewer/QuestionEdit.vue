<template>
  <div class="page">
    <div class="page-header">
      <h2>✏️ 编辑面试题</h2>
      <el-button icon="ArrowLeft" @click="$router.back()" link>返回</el-button>
    </div>
    <el-card shadow="never" class="section-card" style="max-width:800px;">
      <div class="preview-box">
        <h4 style="margin:0 0 12px;">面试：张三 — Java高级开发工程师</h4>
        <el-tag type="primary" size="small" effect="plain">AI已生成 6 道面试题</el-tag>
        <span style="font-size:13px;color:#999;margin-left:12px;">点击题目可编辑内容</span>
      </div>
      <div v-for="(q, i) in questions" :key="i" class="q-edit-item">
        <div class="q-edit-header">
          <span class="q-edit-num">Q{{ i + 1 }}</span>
          <el-input v-model="q.title" class="q-edit-title" />
          <el-select v-model="q.difficulty" size="small" style="width:100px;"><el-option label="简单" value="简单" /><el-option label="中等" value="中等" /><el-option label="困难" value="困难" /></el-select>
          <el-button size="small" type="danger" plain circle icon="Delete" @click="removeQ(i)" />
        </div>
        <el-form-item label="参考答案"><el-input v-model="q.referenceAnswer" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="评分标准"><el-input v-model="q.criteria" /></el-form-item>
      </div>
      <el-button icon="Plus" style="width:100%;" @click="addQ">添加新题目</el-button>
      <div class="form-actions">
        <el-button type="primary" size="large" icon="Check">保存面试题</el-button>
        <el-button size="large" icon="MagicStick" type="success">🤖 AI重新生成</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'

const questions = reactive([
  { title: '请解释Java内存模型中volatile关键字的作用', difficulty: '中等', referenceAnswer: '保证可见性、禁止指令重排序、与synchronized的区别', criteria: '可见性原理+内存屏障+使用场景' },
  { title: '支付系统中如何保证分布式事务一致性？', difficulty: '困难', referenceAnswer: 'TCC/Saga模式、幂等设计、对账机制', criteria: '方案对比+实际落地' },
  { title: 'QPS从1000升至10000时如何排查优化？', difficulty: '中等', referenceAnswer: '监控→日志→慢SQL→缓存→限流→扩容', criteria: '系统性排查' },
  { title: 'Redis分布式锁的实现与RedLock', difficulty: '中等', referenceAnswer: 'SET NX EX、锁续期、RedLock原理和局限', criteria: '原理+局限性' },
  { title: 'Spring Cloud Gateway vs Zuul选型', difficulty: '简单', referenceAnswer: 'Gateway异步非阻塞、Zuul1同步阻塞', criteria: '核心区别' },
  { title: '千万级IM系统核心架构设计', difficulty: '困难', referenceAnswer: 'WebSocket、消息队列、分布式ID、多端同步', criteria: '分层设计+高可用' }
])

function addQ() { questions.push({ title: '新题目', difficulty: '中等', referenceAnswer: '', criteria: '' }); ElMessage.success('已添加新题目') }
function removeQ(i) { if (questions.length > 1) { questions.splice(i, 1); ElMessage.success('已删除') } else { ElMessage.warning('至少保留一道题目') } }
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.section-card { border-radius: 12px; margin-top: 16px; }
.preview-box { padding: 12px; background: #ecf5ff; border-radius: 8px; margin-bottom: 20px; display: flex; align-items: center; flex-wrap: wrap; gap: 8px; }
.q-edit-item { padding: 16px; margin-bottom: 12px; background: #fafafa; border-radius: 10px; border-left: 4px solid #409EFF; }
.q-edit-header { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
.q-edit-num { min-width: 28px; height: 28px; border-radius: 50%; background: #409EFF; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 700; }
.q-edit-title { flex: 1; }
.form-actions { margin-top: 20px; display: flex; gap: 12px; justify-content: center; }
</style>
