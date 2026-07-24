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
| 刷新Token | POST | /api/auth/refresh-token | 使用刷新令牌轮换访问令牌和刷新令牌 | ✅ |
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
| 权限列表 | GET | /api/system/permission/list | 查询权限列表 | ✅️ |
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
| 分类列表 | GET | /api/job/category/list | 查询职位分类列表 | ✅️ |
| 启用分类名称 | GET | /api/job/category/active-names | 查询已启用的分类名称，供职位表单下拉选择 | ✅️ |
| 新增分类 | POST | /api/job/category | 新增职位分类 | ✅️ |
| 更新分类 | PUT | /api/job/category/{id} | 更新职位分类 | ✅️ |
| 删除分类 | DELETE | /api/job/category/{id} | 删除职位分类 | ✅️ |
| 切换分类状态 | PUT | /api/job/category/{id}/status | 启用/停用职位分类 | ✅️ |

### 职位与分类 Redis 缓存

职位和分类属于读多写少的数据。相关查询优先读取 Redis，缓存未命中时查询 MySQL 并回填；Redis 不可用时会降级为数据库查询，不阻断业务请求。

| 接口 | Redis Key | TTL | 一致性策略 |
|------|-----------|-----|------------|
| `GET /api/job/list` | `job:list:{page}:{size}:{keyword}:{status}:{category}` | 5分钟 | 创建、更新、发布或下架职位后删除 `job:list:*` |
| `GET /api/job/{id}` | `job:detail:{id}` | 10分钟 | 更新、发布或下架后覆盖该职位详情缓存 |
| `GET /api/job/category/list`、`GET /api/job/category/active-names` | `job:category:list` | 10分钟 | 新增、更新、删除或切换分类状态后删除缓存 |

该策略减少热门职位和分类查询对 MySQL 的重复访问，同时通过写操作主动失效避免长期读取旧数据。

## 简历接口
| 接口 | 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|:-:|
| 按ID查询简历 | GET | /api/resume/{id} | 预留的简历详情接口，当前返回空数据，尚未实现权限校验 | ⬜ |
| 保存简历 | POST | /api/resume | 按当前登录用户新建或更新简历，兼容旧前端调用 | ✅️ |
| 按ID更新简历 | PUT | /api/resume/{id} | 兼容接口；当前忽略路径中的ID，实际更新当前用户简历 | ❓️ |
| 我的简历 | GET | /api/resume/my | 查询当前登录用户的完整简历；无简历时返回空对象 | ✅️ |
| 保存我的简历 | PUT | /api/resume/my | 推荐使用的保存入口，新建或覆盖当前用户简历 | ✅️ |
| 删除我的简历 | DELETE | /api/resume/my | 删除教育/工作经历、关联文件和ES文档，并逻辑删除简历主记录 | ❓️ |
| 按ID AI解析 | POST | /api/resume/{id}/parse | 预留接口，当前为TODO占位并返回空数据 | ⬜ |
| 按ID AI评估 | POST | /api/resume/{id}/evaluate | 预留接口，当前为TODO占位并返回空数据 | ⬜ |

### 推荐调用流程

求职者简历页面应以当前登录用户为边界，优先使用以下流程：

1. 调用 `GET /api/resume/my` 加载简历。
2. 可选调用 `POST /api/ai/resume/parse-file` 解析 PDF、DOC 或 DOCX 文件，并由用户确认解析结果。
3. 调用 `PUT /api/resume/my` 保存用户确认后的结构化数据和文件地址。
4. 再次调用 `GET /api/resume/my` 获取数据库持久化结果。

`GET /api/resume/{id}`、`POST /api/resume/{id}/parse` 和 `POST /api/resume/{id}/evaluate` 尚未实现，不应作为当前业务主路径。`PUT /api/resume/{id}` 暂未根据路径ID更新指定简历，管理端或HR端不能依赖该接口操作他人简历。

### 简历保存请求

`POST /api/resume` 与 `PUT /api/resume/my` 接收相同的 JSON 对象。当前前端使用的主要字段如下：

| 字段 | 类型 | 必填 | 说明 |
|------|------|:---:|------|
| name | String | 否 | 姓名 |
| gender | String | 否 | 性别 |
| birth | String | 否 | 出生日期 |
| phone | String | 否 | 手机号 |
| email | String | 否 | 邮箱 |
| city | String | 否 | 所在城市 |
| educations | Array | 否 | 教育经历列表，元素结构见下表 |
| experiences | Array | 否 | 工作经历列表，元素结构见下表 |
| skills | Array<String> | 否 | 技能标签 |
| summary | String | 否 | 个人摘要；未传 `evaluation` 时也作为自我评价保存 |
| evaluation | String | 否 | 自我评价，优先级高于 `summary` |
| fileId | Long | 否 | 已上传文件的记录ID，随完整JSON保存 |
| fileName | String | 否 | 原始文件名，随完整JSON保存 |
| fileUrl | String | 否 | 文件下载地址，例如 `/api/file/download/{id}` |

教育经历元素：

| 字段 | 类型 | 说明 |
|------|------|------|
| school | String | 学校名称 |
| major | String | 专业 |
| degree | String | 学历 |
| time | String | 时间范围，例如 `2022.09 - 2026.06` 或 `2022.09 - 至今` |

工作经历元素：

| 字段 | 类型 | 说明 |
|------|------|------|
| company | String | 公司名称 |
| role | String | 职位名称 |
| time | String | 时间范围，格式同教育经历 |
| details | Array<String> | 多行工作内容，保存时以换行符写入描述字段 |


### 持久化与索引同步

- 完整请求对象序列化到 `resume.parsed_content`，用于恢复姓名、技能、摘要等扩展字段。
- `educations` 和 `experiences` 采用先删除旧记录、再全量写入子表的覆盖式保存策略。
- 保存操作使用数据库事务；数据库保存完成后调用 `syncResume` 更新 Elasticsearch 的 `resume_index`。
- ES同步异常只记录警告，不回滚MySQL事务；出现搜索数据不一致时可调用 `POST /api/search/rebuild-index` 重建索引。
- 删除简历时会清理教育和工作经历、尝试删除 `fileUrl` 中 `/download/{id}` 对应的文件、逻辑删除主记录，并删除ES文档。

### AI文件解析

文本解析入口 `POST /api/ai/resume/parse` 接收 JSON：

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|:---:|--------|------|
| resumeContent | String | 是 | - | 已提取的简历正文 |
| fileType | String | 否 | - | 原文件类型，用于补充解析上下文 |
| forceRefresh | Boolean | 否 | false | 为true时跳过已有缓存并重新调用模型，随后覆盖缓存 |

`POST /api/ai/resume/parse-file` 使用 `multipart/form-data`，文件字段名为 `file`：

| 限制 | 说明 |
|------|------|
| 文件类型 | 仅支持 PDF、DOC、DOCX |
| 文件大小 | 最大10MB |
| 文本提取 | 核心服务使用 Apache Tika 提取文本 |
| AI调用 | 提取成功后转发到 `recruitment-ai-service`，需要有效的AI API Key |
| 失败响应 | 空文件/类型或大小错误返回400，无法提取文本返回422，AI服务不可用时返回相应失败结果 |

解析响应包含姓名、联系方式、教育经历、工作年限、技能、摘要、综合评分、问题诊断、优势、修改建议和优化摘要。解析结果不会自动覆盖数据库简历，前端应在用户确认后调用 `PUT /api/resume/my` 保存。

## 投递接口
| 接口 | 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|:--:|
| 投递 | POST | /api/application | 投递职位 | ✅️ |
| 投递记录 | GET | /api/application/list | 查询投递记录（支持职位、状态筛选） | ✅️ |
| 修改状态 | PUT | /api/application/{id}/status | 修改投递状态 | ✅️ |

## 面试接口
| 接口 | 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|:--:|
| 创建面试 | POST | /api/interview | 创建面试任务 | ✅️ |
| 面试列表 | GET | /api/interview/list | 查询面试列表（支持状态筛选） | ✅ |
| 面试详情 | GET | /api/interview/{id} | 查询面试详情（含题目+候选人答案+评价） | ✅ |
| 我的面试 | GET | /api/interview/my-interviews | 候选人查看自己的面试列表 | ✅ |
| 生成面试题 | POST | /api/interview/question/generate | AI生成面试题（需配置 AI API Key） | ✅ |
| 保存正式面试题 | POST | /api/interview/question | 保存面试官确认的面试题 | ✅ |
| 查询面试题 | GET | /api/interview/question/{interviewId} | 根据面试ID查询面试题 | ✅ |
| 候选人题目 | GET | /api/interview/question/candidate/{interviewId} | 候选人查看面试题目（不含参考答案） | ✅ |
| 提交答案 | PUT | /api/interview/question/answer | 候选人提交单题答案 | ✅ |
| 保存评价 | POST | /api/interview/evaluation | 填写面试评价 | ✅ |
| 查询评价 | GET | /api/interview/evaluation/{interviewId} | 根据面试ID查询评价 | ✅ |
| 取消面试 | POST | /api/interview/{id}/cancel | HR取消待面试的面试 | ✅ |
| 处理结果 | POST | /api/interview/{id}/process | HR录用或淘汰候选人 | ✅ |

## AI服务接口
| 接口 | 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|:--:|
| 简历解析与诊断 | POST | /api/ai/resume/parse | AI简历结构化解析、质量评分、问题诊断与优化建议（需配置 API Key） | ✅  |
| 简历文件解析 | POST | /api/ai/resume/parse-file | 上传PDF、DOC或DOCX，提取文本后调用AI结构化解析 | ✅  |
| 简历评估 | POST | /api/ai/resume/evaluate | AI简历质量评估 | ✅  |
| 简历优化建议 | POST | /api/ai/resume/optimize | AI简历优化建议 | ✅  |
| 人岗匹配 | POST | /api/ai/match | AI人岗匹配分析（需配置 API Key） | ✅️ |
| 面试题生成 | POST | /api/ai/question/generate | AI生成面试题（需配置 API Key） |  ✅  |
| 模拟面试生成 | POST | /api/ai/mock-interview/generate | 生成模拟面试题 | ✅️ |
| 模拟面试答题记录 | POST | /api/ai/mock-interview/record | 记录用户回答 | ✅️ |
| 模拟追问 | POST | /api/ai/mock-interview/follow-up | AI追问 | ✅️ |
| 模拟面试报告 | GET | /api/ai/mock-interview/report/{sessionId} | 生成模拟面试报告 | ✅️ |

### AI 服务团队配置

本地开发密钥可配置在：

`recruitment-backend/recruitment-ai-service/application-secrets.properties`

该文件通过 `optional:file:` 加载，不应提交真实密钥。也可以通过 `OPENAI_API_KEY`、`OPENAI_BASE_URL`、`OPENAI_MODEL` 等环境变量覆盖；dev 环境还可以在 Nacos 的 `recruitment-ai-service.yml` 中集中管理 `ai.openai.*` 配置。

分别启动 `recruitment-ai-service`（8081）和 `recruitment-service`（8080）后即可通过核心服务调用 AI 接口。未配置有效 API Key 时，模型接口会返回失败结果，不应将服务启动成功等同于 AI 调用成功。

### AI 简历解析缓存

`POST /api/ai/resume/parse` 根据简历原文的 SHA-256 生成缓存键 `AI:RESUME:{SHA-256}`，完整解析结果缓存15分钟：

1. 优先读取 Redis 共享缓存，多实例部署和服务重启后仍可复用结果。
2. Redis 不可用时回退到 Caffeine 进程内缓存，最多保留200条。
3. 简历内容变化后摘要随之变化，会重新调用模型并写入新缓存。

该机制保证相同请求的幂等性，并减少重复模型调用的响应时间和费用。

### Nacos 配置与服务发现

dev 环境会从 Nacos `SMART_RECRUITMENT` 分组导入公共配置 `recruitment-common.yml` 和服务专属配置。当前配置使用 `refreshEnabled=false`，修改远程配置后需要重启服务才能稳定生效。

核心服务通过 OpenFeign 客户端调用 AI 服务，支持两种寻址方式：

| 模式 | 配置 | 调用方式 |
|------|------|----------|
| 本地直连 | `ai.service.url=http://127.0.0.1:8081`（当前dev默认值） | 直接访问指定AI服务地址 |
| 服务发现 | 将 `ai.service.url` 配置为空 | 按服务名 `recruitment-ai-service` 从Nacos选择健康实例，支持多实例负载均衡 |

数据库、Redis、Elasticsearch、JWT和AI模型参数可集中放入Nacos；API Key等敏感配置应限制访问权限，避免硬编码或提交到仓库。

## 搜索接口
| 接口 | 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|:--:|
| 职位搜索 | GET | /api/search/job | ES职位搜索（支持分页、高亮） | ✅ |
| 简历搜索 | GET | /api/search/resume | ES简历搜索（支持分页、高亮） | ✅ |
| 重建索引 | POST | /api/search/rebuild-index | 重建ES职位/简历索引 | ✅ |

### 搜索参数

`GET /api/search/job` 与 `GET /api/search/resume` 使用相同的分页参数：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|:---:|--------|------|
| keyword | String | 是 | - | 搜索关键词，服务端会去除首尾空格并转为小写 |
| page | Long | 否 | 1 | 页码，从1开始 |
| size | Long | 否 | 10 | 每页数量，服务端限制为1至100 |

返回值为 `Result<PageResult<T>>`。每条搜索记录包含 `highlights`，用于前端展示命中字段的高亮片段。

### Elasticsearch 匹配字段

职位搜索使用多字段匹配，字段权重如下：

| 字段 | 权重 | 说明 |
|------|:----:|------|
| `jobName` | ^3 | 职位名称，最高优先级 |
| `education` | ^2 | 最低学历要求 |
| `experience` | ^2 | 工作经验要求 |
| `requirements` | ^2 | 任职要求 |
| `skills` | ^2 | 技能标签 |
| `description` | ^1 | 工作描述 |
| `category` | ^1 | 职位分类 |
| `city` | ^1 | 工作城市 |

职位搜索对 `jobName`、`description`、`requirements`、`skills` 返回高亮。简历搜索匹配并高亮 `education`、`experience`、`skills`；这些字段由结构化简历的 `parsedContent` 提取，其中教育经历拼接学校、专业和学历，工作经历拼接公司、职位和描述，技能以标签列表合并。

### 索引同步

| 索引 | 触发操作 | 同步行为 |
|------|----------|----------|
| `job_index` | 创建、更新、发布、下架职位 | 调用 `syncJob` 保存最新职位文档；记录标记删除时删除ES文档 |
| `resume_index` | 保存或更新当前用户简历 | 调用 `syncResume` 保存最新简历文档 |
| `resume_index` | 删除当前用户简历 | 调用 `deleteResume` 删除ES文档 |
| 两个索引 | `POST /api/search/rebuild-index` | 删除并重建mapping，再从MySQL导入所有未删除数据 |

搜索接口首次发现索引不存在时会自动创建并从MySQL同步；检测到简历mapping缺少结构化字段时也会触发重建。

## 仪表板接口
| 接口 | 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|:--:|
| HR统计数据 | GET | /api/dashboard/stats | HR工作台统计，包含在招职位、累计/今日投递、今日/进行中面试、本月入职、阶段漏斗、近6个月趋势和最近投递 | ✅ |
| 面试官统计数据 | GET | /api/dashboard/interviewer-stats | 面试官工作台统计 | ✅ |

### 看板实时计数

投递成功和面试创建成功时分别使用 Redis `INCR` 更新当日计数：

| 指标 | 写入触发点 | Redis Key |
|------|------------|-----------|
| 今日投递数 | `POST /api/application` 成功 | `stats:application:daily:{yyyyMMdd}` |
| 今日面试数 | `POST /api/interview` 成功 | `stats:interview:daily:{yyyyMMdd}` |

计数键首次创建时设置TTL至当天结束，次日自动归零。`GET /api/dashboard/stats` 读取这两个实时计数；累计投递、阶段漏斗、近6个月投递趋势及最近投递仍以MySQL数据为准。Redis不可用时计数读取降级为0，不影响投递和面试主流程。

## 文件接口
| 接口 | 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|:--:|
| 上传文件 | POST | /api/file/upload | 文件上传 | ✅️ |
| 下载文件 | GET | /api/file/download/{id} | 文件下载 | ❓️ |
