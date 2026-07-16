# 智能招聘系统 - 接口文档

## 基础信息
- 核心服务端口: 8080
- AI服务端口: 8081
- 统一前缀: /api

## 认证接口
| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 注册 | POST | /api/auth/register | 用户注册 |
| 登录 | POST | /api/auth/login | 用户登录 |
| 退出 | POST | /api/auth/logout | 退出登录 |
| 当前用户 | GET | /api/auth/current-user | 获取当前用户信息 |
| 刷新Token | POST | /api/auth/refresh-token | 刷新JWT |
| 修改密码 | POST | /api/auth/change-password | 修改密码 |

## 系统管理接口
| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 用户列表 | GET | /api/system/user/list | 查询用户列表 |
| 用户详情 | GET | /api/system/user/{id} | 根据ID查询用户 |
| 角色列表 | GET | /api/system/role/list | 查询角色列表 |
| 权限列表 | GET | /api/system/permission/list | 查询权限列表 |

## 职位接口
| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 职位列表 | GET | /api/job/list | 查询职位列表 |
| 职位详情 | GET | /api/job/{id} | 查询职位详情 |
| 创建职位 | POST | /api/job | HR创建职位 |
| 更新职位 | PUT | /api/job/{id} | 更新职位信息 |
| 发布职位 | POST | /api/job/{id}/publish | 发布职位 |
| 下架职位 | POST | /api/job/{id}/unpublish | 下架职位 |

## 简历接口
| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 简历详情 | GET | /api/resume/{id} | 查询简历 |
| 保存简历 | POST | /api/resume | 新建简历 |
| 更新简历 | PUT | /api/resume/{id} | 更新简历 |
| AI解析 | POST | /api/resume/{id}/parse | AI解析简历 |
| AI评估 | POST | /api/resume/{id}/evaluate | AI简历评估 |

## 投递接口
| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 投递 | POST | /api/application | 投递职位 |
| 投递记录 | GET | /api/application/list | 查询投递记录 |
| 修改状态 | PUT | /api/application/{id}/status | 修改投递状态 |

## 面试接口
| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 创建面试 | POST | /api/interview | 创建面试任务 |
| 面试列表 | GET | /api/interview/list | 查询面试列表 |
| 面试详情 | GET | /api/interview/{id} | 查询面试详情 |
| 生成面试题 | POST | /api/interview/question/generate | AI生成面试题 |
| 保存评价 | POST | /api/interview/evaluation | 填写面试评价 |

## AI服务接口
| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 简历解析 | POST | /api/ai/resume/parse | AI简历结构化解析 |
| 人岗匹配 | POST | /api/ai/match | AI人岗匹配分析 |
| 面试题生成 | POST | /api/ai/question/generate | AI生成面试题 |
| 模拟面试 | POST | /api/ai/mock-interview/generate | 生成模拟面试题 |
| 模拟追问 | POST | /api/ai/mock-interview/follow-up | AI追问 |

## 搜索接口
| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 职位搜索 | GET | /api/search/job | ES职位搜索 |
| 简历搜索 | GET | /api/search/resume | ES简历搜索 |
| 重建索引 | POST | /api/search/rebuild-index | 重建ES索引 |

## 仪表板接口
| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 统计数据 | GET | /api/dashboard/stats | HR工作台统计 |

## 文件接口
| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 上传文件 | POST | /api/file/upload | 文件上传 |
| 下载文件 | GET | /api/file/download/{id} | 文件下载 |
