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
      { path: '', name: 'CandidateHome', component: () => import('@/views/candidate/Home.vue') },
      { path: 'jobs', name: 'JobList', component: () => import('@/views/candidate/JobList.vue') },
      { path: 'jobs/:id', name: 'JobDetail', component: () => import('@/views/candidate/JobDetail.vue') },
      { path: 'resume', name: 'ResumeEdit', component: () => import('@/views/candidate/ResumeEdit.vue') },
      { path: 'applications', name: 'MyApplications', component: () => import('@/views/candidate/MyApplications.vue') },
      { path: 'mock-interview', name: 'MockInterview', component: () => import('@/views/candidate/MockInterview.vue') },
    ]
  },
  // HR
  {
    path: '/hr',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { role: 'HR' },
    children: [
      { path: '', name: 'HrDashboard', component: () => import('@/views/hr/Dashboard.vue') },
      { path: 'jobs', name: 'HrJobList', component: () => import('@/views/hr/JobList.vue') },
      { path: 'jobs/edit/:id?', name: 'HrJobEdit', component: () => import('@/views/hr/JobEdit.vue') },
      { path: 'candidates', name: 'CandidateList', component: () => import('@/views/hr/CandidateList.vue') },
      { path: 'interviews', name: 'InterviewList', component: () => import('@/views/hr/InterviewList.vue') },
      { path: 'interviews/create', name: 'InterviewCreate', component: () => import('@/views/hr/InterviewCreate.vue') },
    ]
  },
  // 面试官
  {
    path: '/interviewer',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { role: 'INTERVIEWER' },
    children: [
      { path: '', name: 'InterviewerDashboard', component: () => import('@/views/interviewer/Dashboard.vue') },
      { path: 'tasks', name: 'InterviewTaskList', component: () => import('@/views/interviewer/InterviewTaskList.vue') },
      { path: 'tasks/:id', name: 'InterviewDetail', component: () => import('@/views/interviewer/InterviewDetail.vue') },
      { path: 'questions/generate', name: 'QuestionGenerate', component: () => import('@/views/interviewer/QuestionGenerate.vue') },
      { path: 'evaluation/:id', name: 'InterviewEvaluation', component: () => import('@/views/interviewer/InterviewEvaluation.vue') },
    ]
  },
  // 管理员
  {
    path: '/admin',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { role: 'ADMIN' },
    children: [
      { path: '', name: 'AdminDashboard', redirect: '/admin/users' },
      { path: 'users', name: 'UserList', component: () => import('@/views/admin/UserList.vue') },
      { path: 'roles', name: 'RoleList', component: () => import('@/views/admin/RoleList.vue') },
      { path: 'permissions', name: 'PermissionList', component: () => import('@/views/admin/PermissionList.vue') },
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
