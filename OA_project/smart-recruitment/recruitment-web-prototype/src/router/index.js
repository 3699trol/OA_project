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
      { path: '', name: 'CandidateHome', component: () => import('@/views/candidate/Home.vue'), meta: { title: '首页' } },
      { path: 'jobs', name: 'JobList', component: () => import('@/views/candidate/JobList.vue'), meta: { title: '职位列表' } },
      { path: 'jobs/:id', name: 'JobDetail', component: () => import('@/views/candidate/JobDetail.vue'), meta: { title: '职位详情' } },
      { path: 'resume', name: 'ResumeEdit', component: () => import('@/views/candidate/ResumeEdit.vue'), meta: { title: '我的简历' } },
      { path: 'resume/evaluation', name: 'ResumeEvaluation', component: () => import('@/views/candidate/ResumeEvaluation.vue'), meta: { title: '简历评估' } },
      { path: 'applications', name: 'MyApplications', component: () => import('@/views/candidate/MyApplications.vue'), meta: { title: '投递记录' } },
      { path: 'mock-interview', name: 'MockInterview', component: () => import('@/views/candidate/MockInterview.vue'), meta: { title: '模拟面试' } },
      { path: 'mock-interview/report', name: 'MockInterviewReport', component: () => import('@/views/candidate/MockInterviewReport.vue'), meta: { title: '面试报告' } },
    ]
  },
  // HR
  {
    path: '/hr',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { role: 'HR' },
    children: [
      { path: '', name: 'HrDashboard', component: () => import('@/views/hr/Dashboard.vue'), meta: { title: '工作台' } },
      { path: 'jobs', name: 'HrJobList', component: () => import('@/views/hr/JobList.vue'), meta: { title: '职位管理' } },
      { path: 'jobs/edit/:id?', name: 'HrJobEdit', component: () => import('@/views/hr/JobEdit.vue'), meta: { title: '编辑职位' } },
      { path: 'candidates', name: 'CandidateList', component: () => import('@/views/hr/CandidateList.vue'), meta: { title: '候选人' } },
      { path: 'candidates/detail', name: 'CandidateDetail', component: () => import('@/views/hr/CandidateDetail.vue'), meta: { title: '候选人详情' } },
      { path: 'interviews', name: 'InterviewList', component: () => import('@/views/hr/InterviewList.vue'), meta: { title: '面试管理' } },
      { path: 'interviews/create', name: 'InterviewCreate', component: () => import('@/views/hr/InterviewCreate.vue'), meta: { title: '创建面试' } },
      { path: 'match', name: 'MatchAnalysis', component: () => import('@/views/hr/MatchAnalysis.vue'), meta: { title: 'AI匹配分析' } },
    ]
  },
  // 面试官
  {
    path: '/interviewer',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { role: 'INTERVIEWER' },
    children: [
      { path: '', name: 'InterviewerDashboard', component: () => import('@/views/interviewer/Dashboard.vue'), meta: { title: '工作台' } },
      { path: 'tasks', name: 'InterviewTaskList', component: () => import('@/views/interviewer/InterviewTaskList.vue'), meta: { title: '面试任务' } },
      { path: 'tasks/:id', name: 'InterviewDetail', component: () => import('@/views/interviewer/InterviewDetail.vue'), meta: { title: '面试详情' } },
      { path: 'questions/generate', name: 'QuestionGenerate', component: () => import('@/views/interviewer/QuestionGenerate.vue'), meta: { title: 'AI出题' } },
      { path: 'questions/edit', name: 'QuestionEdit', component: () => import('@/views/interviewer/QuestionEdit.vue'), meta: { title: '编辑面试题' } },
      { path: 'evaluation/:id', name: 'InterviewEvaluation', component: () => import('@/views/interviewer/InterviewEvaluation.vue'), meta: { title: '面试评价' } },
      { path: 'history', name: 'InterviewHistory', component: () => import('@/views/interviewer/InterviewHistory.vue'), meta: { title: '面试历史' } },
    ]
  },
  // 管理员
  {
    path: '/admin',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { role: 'ADMIN' },
    children: [
      { path: '', name: 'AdminDashboard', redirect: '/admin/users' },
      { path: 'users', name: 'UserList', component: () => import('@/views/admin/UserList.vue'), meta: { title: '用户管理' } },
      { path: 'roles', name: 'RoleList', component: () => import('@/views/admin/RoleList.vue'), meta: { title: '角色管理' } },
      { path: 'permissions', name: 'PermissionList', component: () => import('@/views/admin/PermissionList.vue'), meta: { title: '权限管理' } },
      { path: 'categories', name: 'JobCategoryList', component: () => import('@/views/admin/JobCategoryList.vue'), meta: { title: '职位分类' } },
      { path: 'logs', name: 'OperationLog', component: () => import('@/views/admin/OperationLog.vue'), meta: { title: '操作日志' } },
      { path: 'config', name: 'SystemConfig', component: () => import('@/views/admin/SystemConfig.vue'), meta: { title: '系统配置' } },
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
