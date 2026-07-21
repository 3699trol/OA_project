/**
 * Mock 接口数据定义与拦截器模块
 * 用于在前端独立运行时拦截 Axios 请求，模拟后端 API 返回
 */

// 内存中的临时状态，让数据具有一定的动态可变性
const state = {
  // 职位列表
  jobs: [
    { id: 1, title: '高级Java开发工程师', category: '开发', department: '技术部', city: '北京', salary: '25K-35K', experience: '3-5年', education: '本科', status: 'PUBLISHED', description: '负责核心业务系统的设计与开发...', requirements: '1. 扎实的Java基础\n2. 熟悉Spring Cloud微服务架构\n3. 5年以上开发经验', createTime: '2026-07-10 10:00:00' },
    { id: 2, title: '前端架构师', category: '开发', department: '技术部', city: '上海', salary: '30K-45K', experience: '5-10年', education: '本科', status: 'PUBLISHED', description: '负责前端基础架构建设与性能优化...', requirements: '1. 精通Vue3及主流构建工具\n2. 具有大型前端项目架构经验\n3. 熟悉Node.js', createTime: '2026-07-12 14:30:00' },
    { id: 3, title: 'AI算法专家', category: '算法', department: 'AI研究院', city: '深圳', salary: '40K-60K', experience: '3-5年', education: '硕士', status: 'PUBLISHED', description: '负责大语言模型微调与招聘场景落地...', requirements: '1. 熟悉Transformer、GPT等大模型架构\n2. 精通Python及PyTorch\n3. 硕士及以上学历', createTime: '2026-07-15 09:00:00' },
    { id: 4, title: '资深产品经理', category: '产品', department: '产品部', city: '北京', salary: '20K-30K', experience: '3-5年', education: '本科', status: 'DRAFT', description: '负责智慧OA招聘模块的产品规划...', requirements: '1. 3年以上B端产品经验\n2. 优秀的逻辑思维与沟通能力', createTime: '2026-07-16 11:15:00' }
  ],
  // 职位分类
  categories: [
    { id: 1, name: '开发', code: 'DEV', count: 12, status: 1 },
    { id: 2, name: '算法', code: 'ALGO', count: 5, status: 1 },
    { id: 3, name: '产品', code: 'PROD', count: 8, status: 1 },
    { id: 4, name: '设计', code: 'DESIGN', count: 3, status: 1 },
    { id: 5, name: '运营', code: 'OPER', count: 6, status: 1 }
  ],
  // 投递申请记录
  applications: [
    { id: 101, jobId: 1, jobTitle: '高级Java开发工程师', candidateName: '张三', phone: '13800138000', email: 'zhangsan@example.com', resumeScore: 88, status: 'INTERVIEW', applyTime: '2026-07-16 14:20:11' },
    { id: 102, jobId: 2, jobTitle: '前端架构师', candidateName: '李四', phone: '13900139000', email: 'lisi@example.com', resumeScore: 92, status: 'PENDING', applyTime: '2026-07-17 10:05:32' },
    { id: 103, jobId: 3, jobTitle: 'AI算法专家', candidateName: '王五', phone: '13700137000', email: 'wangwu@example.com', resumeScore: 75, status: 'REJECTED', applyTime: '2026-07-15 16:40:00' },
    { id: 104, jobId: 1, jobTitle: '高级Java开发工程师', candidateName: '赵六', phone: '13600136000', email: 'zhaoliu@example.com', resumeScore: 82, status: 'OFFER', applyTime: '2026-07-14 09:30:22' }
  ],
  // 面试列表
  interviews: [
    { id: 201, applicationId: 101, candidateName: '张三', jobTitle: '高级Java开发工程师', interviewerName: '王工', time: '2026-07-19 10:00', type: 'ONLINE', status: 'SCHEDULED', videoUrl: 'https://meeting.example.com/join/123456', evaluation: '' },
    { id: 202, applicationId: 104, candidateName: '赵六', jobTitle: '高级Java开发工程师', interviewerName: '李经理', time: '2026-07-18 15:30', type: 'OFFLINE', status: 'COMPLETED', videoUrl: '', evaluation: '候选人基础扎实，微服务经验丰富，契合岗位要求，建议发出Offer。' }
  ],
  // 模拟面试历史
  mockInterviews: [
    { id: 301, jobTitle: '高级Java开发工程师', duration: '12分钟', score: 85, time: '2026-07-17 19:30:15', report: { overall: '非常优秀，Java并发与JVM调优理解深刻，系统设计能力良好，但在微服务容错机制上还可进一步加强。', scores: { base: 90, framework: 85, design: 80, communication: 85 }, details: '1. 基础知识：对HashMap底层、AQS并发原理回答精准。\n2. 框架应用：Spring Cloud组件熟悉度高。\n3. 系统设计：秒杀系统设计合理，但对分布式事务处理较宽泛。' } }
  ],
  // 个人简历信息
  resume: {
    name: '张三',
    gender: '男',
    age: 28,
    phone: '13800138000',
    email: 'zhangsan@example.com',
    education: '北京邮电大学 · 软件工程 · 本科 (2016-2020)',
    skills: 'Java, Spring Cloud, Redis, MySQL, Docker, Kubernetes',
    experience: '2020.07 - 至今 | 阿里系某电商公司 | 高级开发工程师\n1. 负责核心购物车与结算模块重构，高并发环境下接口响应时间降低30%。\n2. 引入Redis多级缓存，提升QPS达5000+。',
    evaluation: '具有扎实的软件工程基础，对高并发与分布式架构有深入研究，具备较强的自我驱动力。'
  },
  // 日志
  logs: [
    { id: 1, operator: 'admin', ip: '127.0.0.1', action: '登录系统', time: '2026-07-18 14:15:22' },
    { id: 2, operator: 'admin', ip: '127.0.0.1', action: '修改系统参数 [sys.config.ai.enabled]', time: '2026-07-18 14:16:05' }
  ],
  // 权限与角色
  roles: [
    { id: 1, name: '超级管理员', code: 'ROLE_ADMIN', description: '系统最高权限拥有者', createTime: '2026-01-01' },
    { id: 2, name: 'HR经理', code: 'ROLE_HR', description: '招聘业务主流程管理者', createTime: '2026-01-02' },
    { id: 3, name: '专业面试官', code: 'ROLE_INTERVIEWER', description: '技术/业务面试评审专家', createTime: '2026-01-03' },
    { id: 4, name: '普通求职者', code: 'ROLE_CANDIDATE', description: '投递简历与模拟面试用户', createTime: '2026-01-04' }
  ]
}

// 路由注册表
const routes = []

function addRoute(method, pathPattern, handler) {
  // 将类似 /job/:id 的路径转为正则
  const regexString = '^' + pathPattern
    .replace(/\/:\w+/g, '/([^/]+)')
    .replace(/\//g, '\\/') + '$'
  routes.push({
    method: method.toUpperCase(),
    pattern: new RegExp(regexString),
    paramNames: (pathPattern.match(/:\w+/g) || []).map(p => p.slice(1)),
    handler
  })
}

// ==================== 1. 认证模块 ====================
addRoute('POST', '/api/auth/login', (req) => {
  const { username, password } = req.body
  const roleMapping = {
    zhangsan: 'CANDIDATE',
    lijingli: 'HR',
    wanggong: 'INTERVIEWER',
    admin: 'ADMIN'
  }
  const nameMapping = {
    zhangsan: '张三 (求职者)',
    lijingli: '李经理 (招聘专家)',
    wanggong: '王工 (架构师)',
    admin: '超级管理员'
  }
  const role = roleMapping[username] || 'CANDIDATE'
  const nickName = nameMapping[username] || username

  return {
    code: 200,
    message: '登录成功',
    data: {
      token: `demo-token-${username}-${Date.now()}`,
      userInfo: { username, role, nickName, avatar: '' }
    }
  }
})

addRoute('POST', '/api/auth/register', (req) => {
  return { code: 200, message: '注册成功，请使用新账号登录', data: null }
})

addRoute('POST', '/api/auth/logout', () => {
  return { code: 200, message: '退出成功', data: null }
})

addRoute('POST', '/api/auth/change-password', (req) => {
  const { oldPassword, newPassword } = req.body || {}
  if (!oldPassword || !newPassword) {
    return { code: 400, message: '原密码和新密码不能为空', data: null }
  }
  if (newPassword.length < 6) {
    return { code: 400, message: '新密码长度不能少于6位', data: null }
  }
  console.log('[Mock] 修改密码成功')
  return { code: 200, message: '密码修改成功', data: null }
})

addRoute('GET', '/api/auth/current-user', (req) => {
  // 解析 token 来获取用户名
  const authHeader = req.headers.Authorization || ''
  const token = authHeader.replace('Bearer ', '')
  let username = 'zhangsan'
  if (token.includes('lijingli')) username = 'lijingli'
  else if (token.includes('wanggong')) username = 'wanggong'
  else if (token.includes('admin')) username = 'admin'

  const roleMapping = { zhangsan: 'CANDIDATE', lijingli: 'HR', wanggong: 'INTERVIEWER', admin: 'ADMIN' }
  const nameMapping = { zhangsan: '张三', lijingli: '李经理', wanggong: '王工', admin: '系统管理员' }

  return {
    code: 200,
    message: '获取成功',
    data: {
      username,
      role: roleMapping[username] || 'CANDIDATE',
      realName: nameMapping[username] || username,
      nickName: nameMapping[username] || username,
      email: `${username}@example.com`,
      phone: '13800000000',
      avatarUrl: ''
    }
  }
})

addRoute('PUT', '/api/auth/profile', (req) => {
  const { realName, email, phone, avatarUrl } = req.body || {}
  console.log('[Mock] 更新个人资料', req.body)
  return {
    code: 200,
    message: '个人资料更新成功',
    data: { realName, email, phone, avatarUrl }
  }
})

// ==================== 2. 看板数据 ====================
addRoute('GET', '/api/dashboard/stats', () => {
  return {
    code: 200,
    message: '获取成功',
    data: {
      activeJobs: state.jobs.filter(j => j.status === 'PUBLISHED').length,
      totalApplications: state.applications.length,
      interviewing: state.interviews.filter(i => i.status === 'SCHEDULED').length,
      onboardingThisMonth: state.applications.filter(a => a.status === 'OFFER').length,
      recentApplications: state.applications.slice(0, 5),
      monthlyTrend: [
        { month: '2月', count: 12 },
        { month: '3月', count: 28 },
        { month: '4月', count: 45 },
        { month: '5月', count: 30 },
        { month: '6月', count: 52 },
        { month: '7月', count: state.applications.length + 10 }
      ],
      progressData: [
        { status: '待筛选', count: state.applications.filter(a => a.status === 'PENDING').length },
        { status: '面试中', count: state.applications.filter(a => a.status === 'INTERVIEW').length },
        { status: '录用', count: state.applications.filter(a => a.status === 'OFFER').length },
        { status: '已淘汰', count: state.applications.filter(a => a.status === 'REJECTED').length }
      ]
    }
  }
})

// ==================== 3. 职位模块 ====================
addRoute('GET', '/api/job/list', (req) => {
  const { title, keyword, category, status, sortBy, sortOrder, location } = req.params || {}
  let list = [...state.jobs]
  
  // Filter by title / keyword
  const searchTitle = title || keyword
  if (searchTitle) {
    list = list.filter(j => j.title.toLowerCase().includes(searchTitle.toLowerCase()) || (j.requirements && j.requirements.toLowerCase().includes(searchTitle.toLowerCase())))
  }
  
  // Filter by location
  if (location) {
    list = list.filter(j => j.city === location || j.location === location)
  }
  
  // Filter by category
  if (category) {
    list = list.filter(j => j.category === category)
  }
  
  // Filter by status (default to PUBLISHED for candidates)
  if (status) {
    list = list.filter(j => j.status === status)
  } else {
    list = list.filter(j => j.status === 'PUBLISHED')
  }
  
  // Apply sorting
  if (sortBy) {
    const isDesc = sortOrder === 'desc'
    list.sort((a, b) => {
      let valA = a[sortBy]
      let valB = b[sortBy]
      
      if (sortBy === 'salary') {
        // Parse salary string e.g. "25K-35K" or "40K-60K"
        const getSalaryVal = (str) => {
          if (!str) return 0
          const match = str.match(/(\d+)/)
          return match ? parseInt(match[1], 10) : 0
        }
        valA = getSalaryVal(valA)
        valB = getSalaryVal(valB)
      } else if (sortBy === 'createTime') {
        valA = valA ? new Date(valA).getTime() : 0
        valB = valB ? new Date(valB).getTime() : 0
      }
      
      if (valA < valB) return isDesc ? 1 : -1
      if (valA > valB) return isDesc ? -1 : 1
      return 0
    })
  }
  
  return { code: 200, message: '获取成功', data: { list, total: list.length } }
})

addRoute('GET', '/api/job/category/list', (req) => {
  return { code: 200, message: '获取成功', data: state.categories }
})

addRoute('GET', '/api/job/:id', (req) => {
  const job = state.jobs.find(j => j.id == req.pathParams[0])
  if (job) {
    return { code: 200, message: '获取成功', data: job }
  }
  return { code: 404, message: '职位未找到', data: null }
})

addRoute('POST', '/api/job', (req) => {
  const newJob = {
    id: Date.now(),
    createTime: new Date().toLocaleString(),
    status: 'DRAFT',
    ...req.body
  }
  state.jobs.unshift(newJob)
  return { code: 200, message: '创建成功', data: newJob }
})

addRoute('PUT', '/api/job/:id', (req) => {
  const idx = state.jobs.findIndex(j => j.id == req.pathParams[0])
  if (idx !== -1) {
    state.jobs[idx] = { ...state.jobs[idx], ...req.body }
    return { code: 200, message: '修改成功', data: state.jobs[idx] }
  }
  return { code: 404, message: '职位未找到', data: null }
})

addRoute('POST', '/api/job/:id/publish', (req) => {
  const job = state.jobs.find(j => j.id == req.pathParams[0])
  if (job) {
    job.status = 'PUBLISHED'
    return { code: 200, message: '发布成功', data: job }
  }
  return { code: 404, message: '职位未找到', data: null }
})

addRoute('POST', '/api/job/:id/unpublish', (req) => {
  const job = state.jobs.find(j => j.id == req.pathParams[0])
  if (job) {
    job.status = 'UNPUBLISHED'
    return { code: 200, message: '下线成功', data: job }
  }
  return { code: 404, message: '职位未找到', data: null }
})

// ==================== 4. 投递模块 ====================
addRoute('GET', '/api/application/list', (req) => {
  const { status, candidateName } = req.params || {}
  let list = [...state.applications]
  if (status) {
    list = list.filter(a => a.status === status)
  }
  if (candidateName) {
    list = list.filter(a => a.candidateName.toLowerCase().includes(candidateName.toLowerCase()))
  }
  return { code: 200, message: '获取成功', data: { list, total: list.length } }
})

addRoute('POST', '/api/application', (req) => {
  const { jobId } = req.body
  const job = state.jobs.find(j => j.id == jobId)
  if (!job) return { code: 404, message: '职位不存在', data: null }

  const exists = state.applications.some(a => a.jobId == jobId && a.candidateName === '张三')
  if (exists) return { code: 400, message: '您已投递过该职位，请勿重复投递', data: null }

  const newApp = {
    id: Date.now(),
    jobId,
    jobTitle: job.title,
    candidateName: '张三',
    phone: '13800138000',
    email: 'zhangsan@example.com',
    resumeScore: Math.floor(Math.random() * 20) + 75,
    status: 'PENDING',
    applyTime: new Date().toISOString().replace('T', ' ').substring(0, 19)
  }
  state.applications.unshift(newApp)
  return { code: 200, message: '投递成功！已触发AI智能简历解析与评估。', data: newApp }
})

addRoute('PUT', '/api/application/:id/status', (req) => {
  const app = state.applications.find(a => a.id == req.pathParams[0])
  if (app) {
    app.status = req.body.status
    return { code: 200, message: '状态修改成功', data: app }
  }
  return { code: 404, message: '投递记录不存在', data: null }
})

// ==================== 5. 面试模块 ====================
addRoute('GET', '/api/interview/list', () => {
  return { code: 200, message: '获取成功', data: { list: state.interviews, total: state.interviews.length } }
})

addRoute('POST', '/api/interview', (req) => {
  const { applicationId, time, interviewerName, type } = req.body
  const app = state.applications.find(a => a.id == applicationId)
  if (!app) return { code: 404, message: '投递记录不存在', data: null }

  app.status = 'INTERVIEW'
  const newInt = {
    id: Date.now(),
    applicationId,
    candidateName: app.candidateName,
    jobTitle: app.jobTitle,
    interviewerName,
    time,
    type,
    status: 'SCHEDULED',
    videoUrl: type === 'ONLINE' ? 'https://meeting.example.com/join/' + Math.floor(Math.random() * 900000 + 100000) : '',
    evaluation: ''
  }
  state.interviews.unshift(newInt)
  return { code: 200, message: '安排面试成功', data: newInt }
})

addRoute('PUT', '/api/interview/:id/evaluation', (req) => {
  const intv = state.interviews.find(i => i.id == req.pathParams[0])
  if (intv) {
    intv.evaluation = req.body.evaluation
    intv.status = 'COMPLETED'
    // 联动更新投递记录状态
    const app = state.applications.find(a => a.id == intv.applicationId)
    if (app) app.status = req.body.passed ? 'OFFER' : 'REJECTED'
    return { code: 200, message: '提交面试评价成功', data: intv }
  }
  return { code: 404, message: '面试记录不存在', data: null }
})

// ==================== 6. 简历模块 ====================
addRoute('GET', '/api/resume/my', () => {
  return { code: 200, message: '获取成功', data: state.resume }
})

addRoute('PUT', '/api/resume/my', (req) => {
  state.resume = { ...state.resume, ...req.body }
  return { code: 200, message: '更新简历成功', data: state.resume }
})

addRoute('POST', '/api/resume/evaluate', () => {
  return {
    code: 200,
    message: '评估成功',
    data: {
      score: 88,
      analysis: '简历完整度高。亮点：具备一线大厂高并发电商重构经验，Redis、JVM原理掌握到位。不足：缺少Kubernetes生产集群调优细节，建议在技能描述中细化对微服务链路追踪和网关灰度发布的理解。'
    }
  }
})

addRoute('POST', '/api/ai/resume/evaluate', (req) => {
  return {
    code: 200,
    message: '评估成功',
    data: {
      score: 87,
      dimensions: [
        { name: '技术栈契合度', score: 92 },
        { name: '项目经验深度', score: 85 },
        { name: '教育与专业背景', score: 88 },
        { name: '简历完整度', score: 95 },
        { name: '核心竞争力描述', score: 80 }
      ],
      highlights: [
        '具备大流量高并发业务系统（QPS 5000+）实战经历与重构经验。',
        '熟练掌握 Spring Cloud、Redis、Message Queue 等中后台微服务组件。',
        '学习经历清晰，软件工程专业背景良好，基本技术栈扎实。'
      ],
      weaknesses: [
        '项目经历中缺少量化的绩效数据（例如：吞吐量提升%、服务器成本降低%等）。',
        '缺少对容器平台（K8s/Docker）以及日志、监控、灰度发布等运维体系的微调经验描述。',
        '自我评价或职业竞争力总结稍显平淡，没有亮出个人标志性技术标签。'
      ],
      advice: '建议采用标准的 STAR 法则重写项目经验，重点体现“你在高并发下解决什么难题、怎么做的、最终提升了多少性能”；并在技能树中显式增加 APM 链路监控、服务治理或分布式锁调优细节，以彰显资深属性。'
    }
  }
})

addRoute('POST', '/api/ai/resume/optimize', (req) => {
  return {
    code: 200,
    message: '优化成功',
    data: {
      summary: '5年Java核心系统研发经验，具备高并发高可靠系统重构经验。擅长多层高频缓存架构设计，曾在QPS 5000+秒杀核心场景下将响应时间降低30%。深入理解并发编程及JVM内存性能调优，熟练掌握 Spring Cloud、RabbitMQ 微服务架构体系，具备 K8s 与微服务灰度发布的部署实践经验。',
      skills: ['Java', 'Spring Cloud', 'Redis (两级缓存设计)', 'MySQL (分库分表调优)', 'RabbitMQ (分布式事务)', 'Docker/K8s', 'Sentinel 限流熔断'],
      experience: '1. 主导核心购物车与结算微服务化重构，克服极端瞬时高流量，QPS达5000+，核心接口P99延迟缩短30%。\n2. 引入Redis+Caffeine两级分布式缓存，实现布隆过滤器防止穿透，使得核心业务缓存命中率提升至98.5%。\n3. 基于RabbitMQ事务消息实现高抗压下的分布式事务及强一致性投递，确保高额结算全流程“零死锁、零掉单”。'
    }
  }
})

addRoute('POST', '/api/resume/parse', (req) => {
  return {
    code: 200,
    message: 'AI 简历解析成功',
    data: {
      name: '李小明',
      gender: '男',
      phone: '13599998888',
      email: 'xiaoming@example.com',
      education: '华中科技大学 · 计算机科学与技术 · 本科',
      skills: 'Python, Golang, Docker, MySQL, Redis, Kafka',
      experience: '字节跳动 | 后端开发 | 2年经验\n参与高QPS服务重构，优化数据库索引提高接口响应速度。',
      evaluation: '极富潜力的青年后端工程师，重构经验较好。'
    }
  }
})

// ==================== 7. AI 模块 ====================
addRoute('POST', '/api/ai/match-analysis', (req) => {
  const { jobId, resumeData } = req.body
  const score = Math.floor(Math.random() * 20) + 75
  return {
    code: 200,
    message: '匹配分析成功',
    data: {
      score,
      dimensions: [
        { name: '技术栈契合度', score: score + 3, desc: '候选人熟练掌握Spring Cloud、Redis，与岗位极度契合。' },
        { name: '工作年限', score: score - 5, desc: '岗位要求5年以上经验，候选人实际4年经验，基本满足。' },
        { name: '项目经验相关性', score: score + 2, desc: '其电商高并发重构经历与我们目前重构OA的核心诉求非常符合。' },
        { name: '学历与背景', score: score + 5, desc: '北邮软件工程本科，符合名校计算机专业优先的原则。' }
      ],
      conclusion: `综合匹配度为 ${score}%。建议邀约技术一试。该候选人具备极强的高并发调优基因，对解决我们系统高投递时的高负载有直接帮助。`
    }
  }
})

addRoute('POST', '/api/ai/mock-interview/start', (req) => {
  return {
    code: 200,
    message: '模拟面试初始化成功',
    data: {
      sessionId: `session-${Date.now()}`,
      welcomeMessage: '你好！我是AI面试官小智。很高兴能进行这次关于高级Java开发岗位的技术交流。首先，请简单介绍一下你自己，并说说你做过的最具有技术挑战性的项目是什么？'
    }
  }
})

addRoute('POST', '/api/ai/mock-interview/chat', (req) => {
  const { message } = req.body
  const responses = [
    '非常清晰的介绍。既然提到了高并发秒杀，在极端流量瞬时涌入时，你是如何防止Redis缓存雪崩和击穿的？有用到什么限流降级方案吗？',
    '回答得非常到位。使用布隆过滤器防击穿和Sentinel限流是经典且有效的。那么，在电商下单分布式事务方面，你选用了什么方案（如Seata/MQ最终一致性）？它们分别有什么优缺点？',
    '了解。利用RocketMQ的事务消息实现柔性事务确实比2PC性能高很多。好，下面问一个底层基础，你对JVM垃圾回收器如G1和ZGC有什么理解？在实际项目中做过哪些GC调优？',
    '很好。今天的专业技能交流就先到这里，你的知识面和深度都非常棒！你可以点击“结束并生成报告”按钮，我会为你生成一份全维度的AI技术面试评估报告。'
  ]
  const count = parseInt(req.body.chatCount || '0')
  const reply = responses[count % responses.length]

  return {
    code: 200,
    message: '回复成功',
    data: {
      reply,
      suggestedQuestions: count % 2 === 0 ? ['说说分布式锁的实现细节', '如何排查线上OOM问题'] : ['怎么设计一个高吞吐消息队列', '谈谈你对DDD架构的理解']
    }
  }
})

addRoute('POST', '/api/ai/mock-interview/submit', (req) => {
  const reportId = Date.now()
  const newReport = {
    id: reportId,
    jobTitle: req.body.jobTitle || '高级Java开发工程师',
    duration: '15分钟',
    score: 87,
    time: new Date().toLocaleString(),
    report: {
      overall: '候选人在本次模拟技术面试中表现优异，技术广度和深度皆属上乘。尤其对分布式架构、并发编程和RocketMQ事务消息原理理解极为清晰。但在大流量削峰的细节设计上有些许瑕疵，总体推荐定级为高级工程师。',
      scores: { base: 92, framework: 88, design: 85, communication: 83 },
      details: '1. 并发编程：对ReentrantLock与AQS源码机制理解深入，回答全面。\n2. 分布式事务：准确说出MQ事务消息原理与回查机制。\n3. JVM调优：对G1内存划分及Young GC触发时机有深刻认知。\n4. 改进建议：可进一步研究高并发写入时的数据库分库分表与多级缓存一致性保障。'
    }
  }
  state.mockInterviews.unshift(newReport)
  return { code: 200, message: '已完成面试，报告已生成！', data: newReport }
})

addRoute('GET', '/api/ai/mock-interview/report/:id', (req) => {
  const rId = req.pathParams[0]
  const report = state.mockInterviews.find(m => m.id == rId)
  if (report) {
    return { code: 200, message: '获取成功', data: report }
  }
  return { code: 404, message: '报告不存在', data: null }
})

// ==================== 8. 用户、角色、日志、系统配置等管理模块 ====================
// 用户管理
addRoute('GET', '/api/system/user/list', (req) => {
  const {
    keyword = '',
    searchField = 'all',
    userType,
    status,
    deleted,
    page = 1,
    size = 10
  } = req.params || {}
  let users = [
    { id: 1, username: 'admin', realName: '系统管理员', userType: 1, status: 1, deleted: 0, email: 'admin@example.com', phone: '13800000001', createTime: '2026-01-01' },
    { id: 2, username: 'lijingli', realName: '李经理', userType: 2, status: 1, deleted: 0, email: 'lijingli@example.com', phone: '13800000002', createTime: '2026-01-02' },
    { id: 3, username: 'wanggong', realName: '王工', userType: 3, status: 1, deleted: 0, email: 'wanggong@example.com', phone: '13800000003', createTime: '2026-01-03' },
    { id: 4, username: 'zhangsan', realName: '张三', userType: 4, status: 1, deleted: 0, email: 'zhangsan@example.com', phone: '13800000004', createTime: '2026-01-04' },
    { id: 5, username: 'olduser', realName: '历史用户', userType: 4, status: 0, deleted: 1, email: 'olduser@example.com', phone: '13800000005', createTime: '2025-12-01' }
  ]

  if (keyword) {
    const fields = ['username', 'realName', 'phone', 'email']
    const selectedFields = fields.includes(searchField) ? [searchField] : fields
    const normalizedKeyword = String(keyword).trim().toLowerCase()
    users = users.filter(user => selectedFields.some(field =>
      String(user[field] || '').toLowerCase().includes(normalizedKeyword)
    ))
  }

  if (userType !== undefined && userType !== null && userType !== '') {
    users = users.filter(user => user.userType === Number(userType))
  }
  if (status !== undefined && status !== null && status !== '') {
    users = users.filter(user => user.status === Number(status))
  }
  if (deleted !== undefined && deleted !== null && deleted !== '') {
    users = users.filter(user => user.deleted === Number(deleted))
  }

  users.sort((a, b) => a.deleted - b.deleted || b.createTime.localeCompare(a.createTime))
  const pageNum = Math.max(1, Number(page) || 1)
  const pageSize = Math.max(1, Number(size) || 10)
  const total = users.length
  const records = users.slice((pageNum - 1) * pageSize, pageNum * pageSize)

  return {
    code: 200,
    message: '获取成功',
    data: { records, total, pageNum, pageSize }
  }
})

addRoute('GET', '/api/system/user/:id', (req) => {
  const users = [
    { id: 1, username: 'admin', realName: '系统管理员', userType: 1, status: 1, email: 'admin@example.com', phone: '13800000001', createTime: '2026-01-01' },
    { id: 2, username: 'lijingli', realName: '李经理', userType: 2, status: 1, email: 'lijingli@example.com', phone: '13800000002', createTime: '2026-01-02' },
    { id: 3, username: 'wanggong', realName: '王工', userType: 3, status: 1, email: 'wanggong@example.com', phone: '13800000003', createTime: '2026-01-03' },
    { id: 4, username: 'zhangsan', realName: '张三', userType: 4, status: 1, email: 'zhangsan@example.com', phone: '13800000004', createTime: '2026-01-04' }
  ]
  const user = users.find(u => u.id == req.pathParams[0])
  if (user) {
    return { code: 200, message: '获取成功', data: user }
  }
  return { code: 404, message: '用户不存在', data: null }
})

addRoute('PUT', '/api/system/user/:id/reset-password', (req) => {
  const userId = req.pathParams[0]
  console.log(`[Mock] 重置用户 ${userId} 的密码为 123456`)
  return { code: 200, message: '密码重置成功', data: null }
})

addRoute('DELETE', '/api/system/user/:id', (req) => {
  const userId = req.pathParams[0]
  console.log(`[Mock] 删除用户 ${userId}`)
  return { code: 200, message: '用户删除成功', data: null }
})

addRoute('PUT', '/api/system/user/:id/restore', (req) => {
  const userId = req.pathParams[0]
  console.log(`[Mock] 恢复用户 ${userId}`)
  return { code: 200, message: '用户恢复成功', data: null }
})

addRoute('GET', '/api/system/role/list', () => {
  return { code: 200, message: '获取成功', data: state.roles }
})

addRoute('GET', '/api/system/log/list', () => {
  return { code: 200, message: '获取成功', data: { list: state.logs, total: state.logs.length } }
})

addRoute('GET', '/api/admin/config/info', () => {
  return {
    code: 200,
    message: '获取成功',
    data: {
      'sys.config.ai.enabled': 'true',
      'sys.config.ai.model': 'gpt-4o-mini',
      'sys.config.resume.parse.auto': 'true',
      'sys.config.mock.interview.limit': '5'
    }
  }
})

addRoute('POST', '/api/admin/config/update', (req) => {
  state.logs.unshift({
    id: Date.now(),
    operator: 'admin',
    ip: '127.0.0.1',
    action: `更新系统配置: ${JSON.stringify(req.body)}`,
    time: new Date().toLocaleString()
  })
  return { code: 200, message: '更新配置成功', data: null }
})

/**
 * 拦截核心处理器
 */
export function handleMockRequest(config) {
  // 清洗 url，提取 path 和 query 参数
  const fullUrl = config.url || ''
  let pathWithQuery = fullUrl
  
  // 容错处理：若 axios 发出请求时在请求拦截器中还没有拼接 config.baseURL，我们手动进行补齐以便匹配以 /api/ 开头的 mock 路由
  if (!fullUrl.startsWith('http') && !fullUrl.startsWith('/api') && config.baseURL) {
    const base = config.baseURL.endsWith('/') ? config.baseURL.slice(0, -1) : config.baseURL
    const relative = fullUrl.startsWith('/') ? fullUrl : '/' + fullUrl
    pathWithQuery = base + relative
  } else if (fullUrl.startsWith('http')) {
    pathWithQuery = new URL(fullUrl).pathname
  }

  const [path, queryString] = pathWithQuery.split('?')
  
  const method = (config.method || 'GET').toUpperCase()

  // 解析 query params
  const params = {}
  if (queryString) {
    queryString.split('&').forEach(pair => {
      const [k, v] = pair.split('=')
      params[decodeURIComponent(k)] = decodeURIComponent(v || '')
    })
  }
  if (config.params) {
    Object.assign(params, config.params)
  }

  // 匹配 mock 路由
  for (const route of routes) {
    if (route.method === method) {
      const match = path.match(route.pattern)
      if (match) {
        const pathParams = match.slice(1) // 匹配到的正则捕获组即为路径参数
        const mockRequest = {
          url: fullUrl,
          method,
          path,
          params,
          body: typeof config.data === 'string' ? JSON.parse(config.data) : config.data,
          headers: config.headers || {},
          pathParams
        }

        try {
          const response = route.handler(mockRequest)
          return {
            status: 200,
            delay: 350, // 稍微延时，营造真实的呼吸交互感
            data: response
          }
        } catch (e) {
          console.error('[Mock Handler Error]:', e)
          return {
            status: 500,
            message: e.message || 'Mock Handler Error',
            data: null
          }
        }
      }
    }
  }

  return null // 未匹配到 mock 路由，走真实网络
}
