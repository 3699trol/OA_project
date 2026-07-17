<template>
  <div class="page">
    <div class="page-header">
      <h2>📋 我的投递</h2>
      <el-tabs v-model="activeTab" class="status-tabs">
        <el-tab-pane label="全部 (7)" name="all" />
        <el-tab-pane label="初筛中 (3)" name="screening" />
        <el-tab-pane label="面试中 (2)" name="interviewing" />
        <el-tab-pane label="已录用 (1)" name="hired" />
        <el-tab-pane label="不合适 (1)" name="rejected" />
      </el-tabs>
    </div>
    <el-table :data="filteredApplications" style="width:100%;" :default-sort="{ prop: 'createTime', order: 'descending' }" stripe>
      <el-table-column prop="jobTitle" label="投递职位" min-width="160">
        <template #default="{ row }">
          <el-button type="primary" link @click="$router.push(`/candidate/jobs/${row.jobId}`)">{{ row.jobTitle }}</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="company" label="公司" min-width="140" />
      <el-table-column prop="location" label="地点" width="100" />
      <el-table-column prop="salary" label="薪资范围" width="110" />
      <el-table-column prop="status" label="当前状态" width="130" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" effect="plain" round>{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="投递时间" width="130" sortable />
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button size="small" @click="showDetail(row)">查看进度</el-button>
          <el-button v-if="row.status === '初筛中'" size="small" type="danger" plain @click="cancelApply(row)">撤销</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 进度详情弹窗 -->
    <el-dialog v-model="detailVisible" title="投递进度详情" width="560px">
      <el-steps :active="detailStep" align-center finish-status="success">
        <el-step title="简历投递" :description="detailData.createTime" />
        <el-step title="HR初筛" :description="detailData.screeningTime || '等待中'" />
        <el-step title="面试" :description="detailData.interviewTime || '待安排'" />
        <el-step title="结果" :description="detailData.result || '待定'" />
      </el-steps>
      <el-timeline style="margin-top:24px;">
        <el-timeline-item v-for="(log, i) in detailData.logs" :key="i" :timestamp="log.time" :type="log.type" :color="log.color">
          {{ log.content }}
        </el-timeline-item>
      </el-timeline>
      <template #footer><el-button type="primary" @click="detailVisible = false">关闭</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const activeTab = ref('all')
const detailVisible = ref(false)
const detailStep = ref(1)

const detailData = reactive({
  createTime: '2024-07-15',
  screeningTime: '2024-07-16',
  interviewTime: '',
  result: '',
  logs: [
    { content: '投递成功，简历已进入HR筛选池', time: '2024-07-15 14:30', type: 'primary', color: '#ED8936' },
    { content: 'HR已查看您的简历', time: '2024-07-16 09:15', type: 'primary', color: '#ED8936' },
    { content: '简历通过初筛，进入面试候选池', time: '2024-07-16 16:20', type: 'success', color: '#67C23A' }
  ]
})

const applications = [
  { id: 1, jobId: 1, jobTitle: 'Java高级开发工程师', company: '某科技有限公司', location: '北京', salary: '15K-25K', status: '面试中', createTime: '2024-07-15' },
  { id: 2, jobId: 2, jobTitle: '前端开发工程师（Vue）', company: '某网络技术有限公司', location: '上海', salary: '12K-20K', status: '初筛中', createTime: '2024-07-14' },
  { id: 3, jobId: 3, jobTitle: '数据分析师', company: '某数据科技集团', location: '深圳', salary: '10K-18K', status: '已录用', createTime: '2024-07-10' },
  { id: 4, jobId: 5, jobTitle: 'DevOps工程师', company: '某云计算公司', location: '北京', salary: '18K-28K', status: '初筛中', createTime: '2024-07-13' },
  { id: 5, jobId: 7, jobTitle: '全栈工程师（Node.js）', company: '某创业公司', location: '成都', salary: '13K-22K', status: '不合适', createTime: '2024-07-08' },
  { id: 6, jobId: 8, jobTitle: '测试开发工程师', company: '某金融科技公司', location: '北京', salary: '16K-24K', status: '面试中', createTime: '2024-07-12' },
  { id: 7, jobId: 4, jobTitle: '产品经理（B端SaaS）', company: '某软件有限公司', location: '杭州', salary: '14K-22K', status: '初筛中', createTime: '2024-07-16' }
]

const filteredApplications = computed(() => {
  const map = { screening: '初筛中', interviewing: '面试中', hired: '已录用', rejected: '不合适' }
  return activeTab.value === 'all' ? applications : applications.filter(a => a.status === map[activeTab.value])
})

function statusType(s) { return { '初筛中': 'warning', '面试中': 'primary', '已录用': 'success', '不合适': 'info' }[s] || '' }
function showDetail(row) { detailVisible.value = true; detailStep.value = ({ '初筛中': 1, '面试中': 2, '已录用': 3 })[row.status] || 1 }
function cancelApply(row) {
  ElMessageBox.confirm('确认撤销该职位的投递？', '提示', { type: 'warning' }).then(() => {
    ElMessage.success('已撤销投递')
  })
}
</script>

<style scoped>
.page-header h2 { margin: 0 0 4px; font-size: 22px; color: #3E2723; }
.status-tabs { margin-top: 8px; }
</style>
