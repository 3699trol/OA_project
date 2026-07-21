# 智能招聘系统 - 接口文档

## 基础信息
- 核心服务端口: 8080
- AI服务端口: 8081
- 统一前缀: /api
- 统一响应结构: `Result<T>` = `{ code, message, data }`，`code === 200` 表示成功，其余（401 未登录、403 无权限、500 服务错误等）均为失败并携带 `message`
- 统一分页约定：所有 `/list` 接口查询参数固定为 `page`（从1开始，默认1）与 `size`（默认10），可附加业务筛选参数（如 `keyword`、`status`）；返回统一为 `Result<PageResult<T>>`，`PageResult` 结构为 `{ records, total, pageNum, pageSize }`
- 统一路径风格：资源集合用 `/list` 查询，单资源用 `/{id}`；创建用 `POST`（无 id），更新用 `PUT /{id}`，删除用 `DELETE /{id}`，状态切换用 `PUT /{id}/status`
- 接口文档状态：✅️为已开发测试通过，❓️为开发过但存在问题，⬜为未开发。

## 认证接口
| 接口 | 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|:--:|
| 注册 | POST | /api/auth/register | 用户注册 | ✅ |
| 登录 | POST | /api/auth/login | 用户名或邮箱登录 | ✅ |
| 退出 | POST | /api/auth/logout | 退出登录 | ✅ |
| 当前用户 | GET | /api/auth/current-user | 获取当前用户信息 | ✅ |
| 刷新Token | POST | /api/auth/refresh-token | 刷新JWT | ⬜ |
| 修改密码 | POST | /api/auth/change-password | 当前用户修改自己的密码 | ✅ |
| 修改个人资料 | PUT | /api/auth/profile | 当前用户修改自己的真实姓名/手机号/邮箱等资料 | ✅ |

## 系统管理接口
| 接口 | 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|:--:|
| 用户列表 | GET | /api/system/user/list | 管理员分页查询用户，支持字段搜索和状态筛选（包含已删除） | ✅ |
| 用户详情 | GET | /api/system/user/{id} | 根据ID查询用户 | ✅ |
| 更新用户 | PUT | /api/system/user/{id} | 管理员更新用户信息（姓名、角色、手机、邮箱、状态等） | ✅ |
| 重置密码 | PUT | /api/system/user/{id}/reset-password | 重置用户密码为123456 | ✅ |
| 删除用户 | DELETE | /api/system/user/{id} | 删除用户（逻辑删除） | ✅ |
| 恢复用户 | PUT | /api/system/user/{id}/restore | 恢复已删除的用户 | ✅ |
| 角色列表 | GET | /api/system/role/list | 查询角色列表 | ✅ |
| 角色详情 | GET | /api/system/role/{id} | 根据ID查询角色 | ✅ |
| 更新角色 | PUT | /api/system/role/{id} | 更新角色信息（名称、编码、描述、状态） | ✅ |
| 权限列表 | GET | /api/system/permission/list | 查询权限列表 | ⬜ |
| 操作日志 | GET | /api/system/log/list | 查询操作日志（支持关键字、时间范围） | ⬜ |

### 用户列表查询参数

`GET /api/system/user/list` 仅允许管理员调用。未指定筛选条件时返回全部用户（包括逻辑删除用户），正常用户优先、已删除用户靠后，同组内按创建时间倒序排列。

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|:---:|------|------|
| page | Long | 否 | 1 | 页码，从 1 开始 |
| size | Long | 否 | 10 | 每页数量，范围 1–100 |
| keyword | String | 否 | - | 搜索关键词，自动去除首尾空格，使用包含匹配 |
| searchField | String | 否 | all | 搜索字段：`all`、`username`、`realName`、`phone`、`email` |
| userType | Integer | 否 | - | 用户类型：1 管理员、2 HR、3 面试官、4 求职者 |
| status | Integer | 否 | - | 账号状态：1 正常、0 禁用 |
| deleted | Integer | 否 | - | 删除状态：0 未删除、1 已删除 |

示例：查询未删除且正常的 HR，并按姓名搜索“李”：

```http
GET /api/system/user/list?page=1&size=10&searchField=realName&keyword=李&userType=2&status=1&deleted=0
```

## 职位接口
| 接口 | 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|:--:|
| 职位列表 | GET | /api/job/list | 查询职位列表（支持关键字、状态） | ✅️ |
| 职位详情 | GET | /api/job/{id} | 查询职位详情 | ✅️ |
| 创建职位 | POST | /api/job | HR创建职位 | ✅ |
| 更新职位 | PUT | /api/job/{id} | 更新职位信息 | ✅️ |
| 发布职位 | POST | /api/job/{id}/publish | 发布职位 | ✅️ |
| 下架职位 | POST | /api/job/{id}/unpublish | 下架职位 | ✅️ |
| 分类列表 | GET | /api/job/category/list | 查询职位分类列表 | ⬜ |
| 新增分类 | POST | /api/job/category | 新增职位分类 | ⬜ |
| 更新分类 | PUT | /api/job/category/{id} | 更新职位分类 | ⬜ |
| 删除分类 | DELETE | /api/job/category/{id} | 删除职位分类 | ⬜ |
| 切换分类状态 | PUT | /api/job/category/{id}/status | 启用/停用职位分类 | ⬜ |

## 简历接口
| 接口 | 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|:--:|
| 简历详情 | GET | /api/resume/{id} | 查询简历 | ⬜ |
| 保存简历 | POST | /api/resume | 新建简历 | ✅️ |
| 更新简历 | PUT | /api/resume/{id} | 更新简历 | ✅️ |
| AI解析 | POST | /api/resume/{id}/parse | AI解析简历 | ⬜ |
| AI评估 | POST | /api/resume/{id}/evaluate | AI简历评估 | ⬜ |

## 投递接口
| 接口 | 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|:--:|
| 投递 | POST | /api/application | 投递职位 | ✅️ |
| 投递记录 | GET | /api/application/list | 查询投递记录（支持职位、状态筛选） | ✅️ |
| 修改状态 | PUT | /api/application/{id}/status | 修改投递状态 | ❓️ |

## 面试接口
| 接口 | 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|:--:|
| 创建面试 | POST | /api/interview | 创建面试任务 | ✅️ |
| 面试列表 | GET | /api/interview/list | 查询面试列表（支持状态筛选） | ✅️ |
| 面试详情 | GET | /api/interview/{id} | 查询面试详情 | ✅️ |
| 生成面试题 | POST | /api/interview/question/generate | AI生成面试题 | ⬜ |
| 保存正式面试题 | POST | /api/interview/question | 保存面试官确认的面试题 | ⬜ |
| 查询面试题 | GET | /api/interview/question/{interviewId} | 根据面试ID查询面试题 | ⬜ |
| 保存评价 | POST | /api/interview/evaluation | 填写面试评价 | ✅️ |
| 查询评价 | GET | /api/interview/evaluation/{interviewId} | 根据面试ID查询评价 | ✅️ |

## AI服务接口
| 接口 | 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|:--:|
| 简历解析与诊断 | POST | /api/ai/resume/parse | AI简历结构化解析、质量评分、问题诊断与优化建议（需配置 API Key） | ✅ |
| 简历评估 | POST | /api/ai/resume/evaluate | AI简历质量评估 | ⬜ |
| 简历优化建议 | POST | /api/ai/resume/optimize | AI简历优化建议 | ⬜ |
| 人岗匹配 | POST | /api/ai/match | AI人岗匹配分析（需配置 API Key） | ❓️ |
| 面试题生成 | POST | /api/ai/question/generate | AI生成面试题（需配置 API Key） | ❓️ |
| 模拟面试生成 | POST | /api/ai/mock-interview/generate | 生成模拟面试题 | ⬜ |
| 模拟面试答题记录 | POST | /api/ai/mock-interview/record | 记录用户回答 | ⬜ |
| 模拟追问 | POST | /api/ai/mock-interview/follow-up | AI追问 | ⬜ |
| 模拟面试报告 | GET | /api/ai/mock-interview/report/{sessionId} | 生成模拟面试报告 | ⬜ |

### AI 服务团队配置

团队共享开发配置位于：

`recruitment-backend/recruitment-ai-service/application-secrets.properties`

相同简历内容的完整解析结果会在 AI 服务进程内缓存 15 分钟，最多保留 200 条；
内容发生变化或服务重启后会重新调用模型。

该文件随代码提交，包含团队开发环境使用的 API Key、Base URL 和模型参数；成员无需额外创建配置文件。
操作系统中的同名 `OPENAI_*` 环境变量可以覆盖共享默认值。代理配置作为直连失败时的回退路由。
分别启动
`recruitment-ai-service`（8081）和 `recruitment-service`（8080）后即可调用。

## 搜索接口
| 接口 | 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|:--:|
| 职位搜索 | GET | /api/search/job | ES职位搜索（支持分页） | ⬜ |
| 简历搜索 | GET | /api/search/resume | ES简历搜索（支持分页） | ⬜ |
| 重建索引 | POST | /api/search/rebuild-index | 重建ES索引 | ⬜ |

## 仪表板接口
| 接口 | 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|:--:|
| HR统计数据 | GET | /api/dashboard/stats | HR工作台统计 | ✅ |
| 面试官统计数据 | GET | /api/dashboard/interviewer-stats | 面试官工作台统计 | ✅ |

## 文件接口
| 接口 | 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|:--:|
| 上传文件 | POST | /api/file/upload | 文件上传 | ❓️ |
| 下载文件 | GET | /api/file/download/{id} | 文件下载 | ❓️ |
