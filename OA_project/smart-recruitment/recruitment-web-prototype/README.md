# 智能招聘系统 - 前端产品原型

> **纯静态 Demo，不依赖后端接口。所有数据均为硬编码 Mock 数据。**

## 快速启动

```bash
cd recruitment-web-prototype
npm install
npm run dev
```

## 演示流程

1. 打开 `http://localhost:5173`
2. 点击登录页底部的**快速体验**标签一键登录任意角色
3. 按角色查看对应功能页面

## 页面清单（共 30 页）

| 角色 | 页面 | 路径 |
|------|------|------|
| 认证 | 登录 | `/login` |
| 认证 | 注册 | `/register` |
| 求职者 | 首页 | `/candidate` |
| 求职者 | 职位列表 | `/candidate/jobs` |
| 求职者 | 职位详情 | `/candidate/jobs/:id` |
| 求职者 | 我的简历 | `/candidate/resume` |
| 求职者 | 简历评估 | `/candidate/resume/evaluation` |
| 求职者 | 投递记录 | `/candidate/applications` |
| 求职者 | 模拟面试 | `/candidate/mock-interview` |
| 求职者 | 面试报告 | `/candidate/mock-interview/report` |
| HR | 工作台 | `/hr` |
| HR | 职位管理 | `/hr/jobs` |
| HR | 编辑职位 | `/hr/jobs/edit/:id?` |
| HR | 候选人列表 | `/hr/candidates` |
| HR | 候选人详情 | `/hr/candidates/detail` |
| HR | 面试管理 | `/hr/interviews` |
| HR | 创建面试 | `/hr/interviews/create` |
| HR | AI匹配分析 | `/hr/match` |
| 面试官 | 工作台 | `/interviewer` |
| 面试官 | 面试任务 | `/interviewer/tasks` |
| 面试官 | 面试详情 | `/interviewer/tasks/:id` |
| 面试官 | 面试评价 | `/interviewer/evaluation/:id` |
| 面试官 | AI生成面试题 | `/interviewer/questions/generate` |
| 面试官 | 编辑面试题 | `/interviewer/questions/edit` |
| 面试官 | 面试历史 | `/interviewer/history` |
| 管理员 | 用户管理 | `/admin/users` |
| 管理员 | 角色管理 | `/admin/roles` |
| 管理员 | 权限管理 | `/admin/permissions` |
| 管理员 | 职位分类 | `/admin/categories` |
| 管理员 | 操作日志 | `/admin/logs` |
| 管理员 | 系统配置 | `/admin/config` |

## 注意

- 此项目仅供**产品原型演示**，不连接后端
- 实际开发请使用 `recruitment-web/` 目录
- 原型中的页面结构和组件设计可作为开发参考
