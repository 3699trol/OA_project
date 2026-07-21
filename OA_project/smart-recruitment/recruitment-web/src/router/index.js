import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue')
  },
  // 求职者
  {
    path: '/candidate',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { role: 'CANDIDATE' },
    children: [
      { path: '', name: 'CandidateHome', component: () => import('@/views/candidate/Home.vue'), meta: { title: '智能求职首页' } },
      { path: 'jobs', name: 'JobList', component: () => import('@/views/candidate/JobList.vue'), meta: { title: '全职岗位大厅' } },
      { path: 'jobs/:id', name: 'JobDetail', component: () => import('@/views/candidate/JobDetail.vue'), meta: { title: '岗位详情介绍' } },
      { path: 'resume', name: 'ResumeEdit', component: () => import('@/views/candidate/ResumeEdit.vue'), meta: { title: '我的智能简历' } },
      { path: 'resume/evaluation', name: 'ResumeEvaluation', component: () => import('@/views/candidate/ResumeEvaluation.vue'), meta: { title: 'AI 简历诊断与评估' } },
      { path: 'applications', name: 'MyApplications', component: () => import('@/views/candidate/MyApplications.vue'), meta: { title: '投递进度追踪' } },
      { path: 'mock-interview', name: 'MockInterview', component: () => import('@/views/candidate/MockInterview.vue'), meta: { title: 'AI 仿真模拟面试' } },
      { path: 'mock-interview/report', name: 'MockInterviewReport', component: () => import('@/views/candidate/MockInterviewReport.vue'), meta: { title: '模拟面试诊断评估报告' } },
    ]
  },
  // HR
  {
    path: '/hr',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { role: 'HR' },
    children: [
      { path: '', name: 'HrDashboard', component: () => import('@/views/hr/Dashboard.vue'), meta: { title: '招聘监控大屏' } },
      { path: 'jobs', name: 'HrJobList', component: () => import('@/views/hr/JobList.vue'), meta: { title: '发布职位管理' } },
      { path: 'jobs/edit/:id?', name: 'HrJobEdit', component: () => import('@/views/hr/JobEdit.vue'), meta: { title: '发布职位录入' } },
      { path: 'candidates', name: 'CandidateList', component: () => import('@/views/hr/CandidateList.vue'), meta: { title: '候选人才库' } },
      { path: 'candidates/detail/:id', name: 'CandidateDetail', component: () => import('@/views/hr/CandidateDetail.vue'), meta: { title: '候选人才详情' } },
      { path: 'interviews', name: 'InterviewList', component: () => import('@/views/hr/InterviewList.vue'), meta: { title: '日常面试安排' } },
      { path: 'interviews/create', name: 'InterviewCreate', component: () => import('@/views/hr/InterviewCreate.vue'), meta: { title: '发起面试邀约' } },
      { path: 'interviews/detail/:id', name: 'HrInterviewDetail', component: () => import('@/views/hr/InterviewDetail.vue'), meta: { title: '面试详情' } },
      { path: 'match', name: 'MatchAnalysis', component: () => import('@/views/hr/MatchAnalysis.vue'), meta: { title: 'AI 双向人岗匹配分析' } },
    ]
  },
  // 面试官
  {
    path: '/interviewer',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { role: 'INTERVIEWER' },
    children: [
      { path: '', name: 'InterviewerDashboard', component: () => import('@/views/interviewer/Dashboard.vue'), meta: { title: '面试官工作台' } },
      { path: 'tasks', name: 'InterviewTaskList', component: () => import('@/views/interviewer/InterviewTaskList.vue'), meta: { title: '待面试任务' } },
      { path: 'tasks/:id', name: 'InterviewDetail', component: () => import('@/views/interviewer/InterviewDetail.vue'), meta: { title: '面试任务详情' } },
      { path: 'questions/generate', name: 'QuestionGenerate', component: () => import('@/views/interviewer/QuestionGenerate.vue'), meta: { title: 'AI 面试出题器' } },
      { path: 'questions/edit', name: 'QuestionEdit', component: () => import('@/views/interviewer/QuestionEdit.vue'), meta: { title: '标准面试题编辑' } },
      { path: 'evaluation/:id', name: 'InterviewEvaluation', component: () => import('@/views/interviewer/InterviewEvaluation.vue'), meta: { title: '面试专业综合评价' } },
      { path: 'history', name: 'InterviewHistory', component: () => import('@/views/interviewer/InterviewHistory.vue'), meta: { title: '我评审的历程' } },
    ]
  },
  // 管理员
  {
    path: '/admin',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { role: 'ADMIN' },
    children: [
      { path: '', name: 'AdminDashboard', redirect: '/admin/users' },
      { path: 'users', name: 'UserList', component: () => import('@/views/admin/UserList.vue'), meta: { title: '系统用户主表' } },
      { path: 'roles', name: 'RoleList', component: () => import('@/views/admin/RoleList.vue'), meta: { title: '系统角色权限' } },
      { path: 'permissions', name: 'PermissionList', component: () => import('@/views/admin/PermissionList.vue'), meta: { title: '权限节点定义' } },
      { path: 'categories', name: 'JobCategoryList', component: () => import('@/views/admin/JobCategoryList.vue'), meta: { title: '行业岗位分类' } },
      { path: 'logs', name: 'OperationLog', component: () => import('@/views/admin/OperationLog.vue'), meta: { title: '后台审计日志' } },
      { path: 'config', name: 'SystemConfig', component: () => import('@/views/admin/SystemConfig.vue'), meta: { title: '智能AI系统参数' } },
    ]
  },
  // 错误页面
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/error/NotFound.vue')
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.role && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
