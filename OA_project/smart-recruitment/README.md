# 企业招聘与人才智能匹配系统（Smart Recruitment）

基于 **Vue 3 前端 + Spring Boot 后端** 的智能招聘平台，覆盖职位发布、简历管理、面试安排、权限控制、统计分析，并集成 Elasticsearch 全文检索、Redis Stack 向量检索与 Spring AI 能力。

> 架构说明（重要）：前端使用 **Vue.js**，后端使用 **Spring Boot**。  
> 若将二者写反，将无法按当前技术栈完成前后端分离开发与部署。

---

## 1. 项目功能

### 1.1 核心业务

| 模块 | 功能说明 |
|------|----------|
| 职位管理 | HR 发布/编辑/上下架职位，求职者浏览与检索职位 |
| 简历管理 | 求职者上传/编辑简历，AI 解析结构化信息，同步 ES 与向量库 |
| 投递流程 | 投递职位、防重复投递、初筛、状态流转与流转日志 |
| 面试安排 | HR 创建面试、指定面试官；面试官查看任务、生成试题、填写评价 |
| 智能匹配 | 简历与岗位向量匹配、技能/学历/年限综合评分、匹配解释 |
| 全文搜索 | Elasticsearch 搜索简历与职位，支持筛选与高亮 |
| 统计分析 | HR 工作台：职位数、投递数、待筛选、面试数、转化率 |
| 系统维护 | 用户/角色/权限/系统配置/操作日志 |

### 1.2 角色权限

#### （1）管理员 Admin — 系统维护

- 用户管理
- 角色管理
- 权限管理
- 系统配置
- 日志管理

#### （2）HR — 招聘流程管理

- 发布、管理职位
- 查看简历与候选人
- 安排面试
- 查看统计数据
- 人岗匹配分析

#### （3）面试官 Interviewer — 面试执行

- 查看面试任务
- AI 生成正式面试题（求职者不可调用）
- 编辑试题、填写面试评价

#### （4）求职者 Candidate — 应聘岗位

- 注册登录
- 浏览职位
- 上传/编辑简历
- 投递职位
- 查看面试结果
- 模拟面试（与正式面试题接口隔离）

---

## 2. 技术栈

### 2.1 总体架构

```text
Vue 3 (recruitment-web)
        │  HTTP /api
        ▼
Spring Boot 核心服务 (recruitment-service :8080)
        │  OpenFeign
        ▼
Spring Boot AI 服务 (recruitment-ai-service :8081)
        │
        ├── MySQL            业务数据
        ├── Redis / Redis Stack   缓存 + 向量
        ├── Elasticsearch    简历/职位全文检索
        ├── Nacos            注册发现 / 配置中心
        └── Spring AI        简历解析、出题、匹配解释
```

### 2.2 前端

| 技术 | 说明 |
|------|------|
| Vue 3 | SPA 框架 |
| Vite | 构建工具 |
| Vue Router | 路由与角色页面隔离 |
| Pinia | 状态管理 |
| Element Plus | UI 组件库 |
| Axios | HTTP 请求 |

### 2.3 后端

| 技术 | 说明 |
|------|------|
| Spring Boot 3.2.x | 主框架 |
| Spring Security + JWT | 认证鉴权 |
| Spring Cloud Alibaba Nacos | 服务注册与配置 |
| OpenFeign | 核心服务调用 AI 服务 |
| MyBatis-Plus | ORM / CRUD |
| Redis / Redis Stack | 缓存、Token、向量检索 |
| Elasticsearch | 简历与职位搜索 |
| Spring AI | 大模型调用（解析/出题/匹配） |
| Apache Tika | PDF/Word 文本提取 |
| MySQL 8 | 关系型数据库 |

### 2.4 开发环境要求

请提前安装并配置：

| 序号 | 环境 | 版本要求 | 说明 |
|------|------|----------|------|
| 1 | JDK | **21 及以上** | 本仓库已按 JDK 21 编译配置 |
| 2 | Maven | **3.9 及以上** | 后端多模块构建 |
| 3 | Nacos | **nacos-server 1.1.3 及以上** | 推荐 2.2.x（兼容当前 Spring Cloud Alibaba） |
| 4 | Elasticsearch | **7.13.0（Windows）及以上** | 本地可装 7.13；Docker 默认使用 7.17.21 便于联调 |
| 5 | Redis | **2.8.9 及以上** | 基础缓存可用；**向量能力请使用 Redis Stack** |
| 6 | Spring AI | 已集成于 `recruitment-ai-service` | 需配置模型 API Key |
| 7 | Redis Stack | 向量数据库 | 存储简历/岗位 Embedding，支持相似度检索 |
| 8 | Node.js | 18+（建议） | 前端开发 |
| 9 | MySQL | 8.0（建议） | 业务库 |

> 兼容提示：  
> - Redis 2.8.9 **不支持** Redis Stack 向量检索，向量能力请单独启动 Redis Stack。  
> - Nacos 1.1.3 较旧，与 Spring Boot 3 / SCA 2023 联调时建议升级到 Nacos 2.x。

---

## 3. 项目结构（规范框架）

```text
smart-recruitment/
├── recruitment-backend/              # 后端（Spring Boot 多模块）
│   ├── pom.xml
│   ├── recruitment-common/           # 公共能力
│   │   ├── common-core               # Result、异常、工具、上下文
│   │   ├── common-web                # 全局异常、拦截器、操作日志
│   │   ├── common-security           # Security、JWT、权限
│   │   └── common-redis              # Redis 工具、缓存、防重
│   ├── recruitment-api/              # 服务间 DTO / Feign 接口
│   ├── recruitment-service/          # 核心招聘业务（8080）
│   │   └── com.recruitment
│   │       ├── system / auth / job / resume
│   │       ├── application           # 投递（非 Spring Application）
│   │       ├── interview / search / dashboard / file
│   └── recruitment-ai-service/       # AI 与向量服务（8081）
│       └── com.recruitment.ai
│           ├── resume / matching / question
│           ├── mockinterview / evaluation / vector
├── recruitment-web/                  # 前端（Vue 3）
│   └── src/
│       ├── api / components / layouts
│       ├── router / stores / utils
│       └── views/{auth,candidate,hr,interviewer,admin,error}
├── sql/                              # 数据库脚本
├── docs/                             # 接口与设计文档
└── docker/                           # Redis Stack、ES、MySQL、Nacos
```

包命名约定：`com.recruitment.*`  
前端路径别名：`@` → `src`

---

# 4. 数据库设计

### 4.1 命名与通用字段

- 统一下划线命名：`job_application.status`、`resume_education.start_date`。
- 所有主数据表包含：`create_time`、`update_time`（`DEFAULT CURRENT_TIMESTAMP` + `ON UPDATE`）。
- 软删除字段统一使用 `deleted TINYINT DEFAULT 0`（0=有效，1=删除）。
- 金额字段使用 `DECIMAL(10,2)`，评分使用 `DECIMAL(5,2)` / `INT`，布尔值使用 `TINYINT(1)`。

### 4.2 用户表（sys_user）

| 字段名 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK | 用户 ID |
| username | VARCHAR(50) | UNIQUE, NOT NULL | 登录账号 |
| password | VARCHAR(255) | NOT NULL | Bcrypt 密码 |
| real_name | VARCHAR(50) |  | 真实姓名 |
| user_type | TINYINT | NOT NULL | 1 管理员 / 2 HR / 3 面试官 / 4 求职者 |
| gender | TINYINT | DEFAULT 0 | 性别（0 未知、1 男、2 女） |
| phone | VARCHAR(20) |  | 手机号（建立唯一索引防重复） |
| email | VARCHAR(100) |  | 邮箱 |
| status | TINYINT | DEFAULT 1 | 账号状态（0 禁用，1 正常） |
| last_login_time | DATETIME |  | 最后登录时间 |
| company_id | BIGINT | FK | HR 所属公司，关联 `company.id` |
| avatar_url | VARCHAR(255) |  | 头像 |
| create_time | DATETIME |  | 创建时间 |
| update_time | DATETIME |  | 更新时间 |
| deleted | TINYINT | DEFAULT 0 | 逻辑删除标记 |

> 索引：`uk_sys_user_username`、`idx_sys_user_phone`、`idx_sys_user_company`。

### 4.3 公司表（company）

| 字段名 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK | 公司 ID |
| company_name | VARCHAR(100) | NOT NULL | 公司全称 |
| short_name | VARCHAR(50) |  | 简称 |
| industry | VARCHAR(100) |  | 行业 |
| scale | VARCHAR(50) |  | 规模（50 人、500 人等） |
| city | VARCHAR(100) |  | 所在城市 |
| address | VARCHAR(255) |  | 办公地址或线上办公 |
| logo_url | VARCHAR(255) |  | LOGO |
| description | TEXT |  | 公司简介 |
| create_time | DATETIME |  | 创建时间 |
| update_time | DATETIME |  | 更新时间 |

### 4.4 职位表（job）

| 字段名 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK | 职位 ID |
| job_name | VARCHAR(100) | NOT NULL | 职位名称 |
| department | VARCHAR(100) |  | 所属部门 |
| category | VARCHAR(50) |  | 岗位类别（技术/产品/职能…） |
| city | VARCHAR(100) |  | 工作城市（远程可写 remote） |
| salary_min | DECIMAL(10,2) |  | 最低薪资（K/月） |
| salary_max | DECIMAL(10,2) |  | 最高薪资 |
| education | VARCHAR(50) |  | 学历要求 |
| experience | VARCHAR(50) |  | 经验要求 |
| headcount | INT | DEFAULT 1 | 招聘人数 |
| description | TEXT |  | 岗位描述 |
| requirements | TEXT |  | 任职要求 |
| status | TINYINT | DEFAULT 0 | 0 草稿 / 1 招聘中 / 2 下架 |
| publisher_id | BIGINT | FK | 发布 HR（`sys_user.id`） |
| publish_time | DATETIME |  | 发布时间 |
| create_time | DATETIME |  | 创建时间 |
| update_time | DATETIME |  | 更新时间 |
| deleted | TINYINT | DEFAULT 0 | 逻辑删除 |

> 索引：`idx_job_status`、`idx_job_city`，并建立全文索引 `ft_job_content(job_name, description, requirements)` 供检索。

### 4.5 简历主表（resume）

| 字段名 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK | 简历 ID |
| user_id | BIGINT | FK, NOT NULL | 求职者 ID（`sys_user.id`） |
| file_url | VARCHAR(500) |  | 原始文件存储位置（PDF/Word） |
| parsed_content | LONGTEXT |  | AI/ETL解析出的纯文本，供 ES 索引 |
| ai_score | DECIMAL(5,2) |  | AI 质量评分 |
| ai_analysis | TEXT |  | AI 优化建议与点评 |
| current_salary | DECIMAL(10,2) |  | 当前薪资 |
| expected_salary | DECIMAL(10,2) |  | 期望薪资 |
| self_evaluation | TEXT |  | 自我评价或求职意向 |
| embedding_vector | JSON |  | 768 维向量 JSON，作为 Redis Stack 向量的备份 |
| create_time | DATETIME |  | 创建时间 |
| update_time | DATETIME |  | 更新时间 |
| deleted | TINYINT | DEFAULT 0 | 逻辑删除 |

### 4.6 教育经历子表（resume_education）

| 字段名 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK | 主键 |
| resume_id | BIGINT | FK, NOT NULL | 对应简历 ID |
| school | VARCHAR(100) | NOT NULL | 学校名称 |
| major | VARCHAR(100) |  | 专业 |
| degree | VARCHAR(20) |  | 学历（专科/本科/硕士/博士） |
| start_date | DATE |  | 入学时间 |
| end_date | DATE |  | 毕业时间（NULL=至今） |
| is_highest | TINYINT | DEFAULT 0 | 是否最高学历 |
| sort_order | INT | DEFAULT 0 | 排序 |

> `resume_id` 建立外键并开启 `ON DELETE CASCADE`，便于删除简历时同步清表。

### 4.7 工作经历子表（resume_experience）

| 字段名 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK | 主键 |
| resume_id | BIGINT | FK, NOT NULL | 对应简历 ID |
| company | VARCHAR(100) | NOT NULL | 公司名称 |
| position | VARCHAR(100) |  | 职位名称 |
| department | VARCHAR(100) |  | 部门 |
| start_date | DATE |  | 入职时间 |
| end_date | DATE |  | 离职时间（NULL=至今） |
| description | TEXT |  | 工作内容与成绩 |
| sort_order | INT | DEFAULT 0 | 排序 |

### 4.8 职位投递表（job_application）

| 字段名 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK | 投递 ID |
| job_id | BIGINT | FK, NOT NULL | 职位 ID |
| user_id | BIGINT | FK, NOT NULL | 求职者 ID |
| resume_id | BIGINT | FK, NOT NULL | 使用的简历 ID |
| apply_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 投递时间 |
| status | TINYINT | DEFAULT 0 | 0 待筛选 / 1 面试中 / 2 录用 / 3 淘汰 / 4 撤回 |
| remark | VARCHAR(255) |  | HR 备注 |
| ai_match_score | DECIMAL(5,2) |  | AI 人岗匹配得分 |
| ai_match_reason | TEXT |  | AI 匹配解释/推荐原因 |
| create_time | DATETIME |  | 创建时间 |
| update_time | DATETIME |  | 更新时间 |

> 约束：`UNIQUE KEY uk_job_user (job_id, user_id)` 防重复投递；`idx_application_status` 便于列表筛选。

### 4.9 投递流转日志（application_log）

| 字段名 | 类型 | 说明 |
|---|---|---|
| id | BIGINT PK | 日志 ID |
| application_id | BIGINT | 投递记录 |
| from_status | TINYINT | 原状态 |
| to_status | TINYINT | 新状态 |
| remark | VARCHAR(500) | 操作备注 |
| operator_id | BIGINT | 操作人 |
| create_time | DATETIME | 操作时间 |

### 4.10 面试表（interview）

| 字段名 | 类型 | 说明 |
|---|---|---|
| id | BIGINT PK | 面试 ID |
| application_id | BIGINT | 投递记录 ID |
| interviewer_id | BIGINT | 面试官 ID |
| interview_time | DATETIME | 面试时间 |
| interview_type | VARCHAR(20) | 线上 / 线下 |
| address | VARCHAR(255) | 会议链接或地点 |
| ai_questions | TEXT | AI 生成题目缓存 |
| status | TINYINT | 0 待面试、1 已完成、2 取消 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### 4.11 面试评价表（interview_evaluation）

| 字段名 | 类型 | 说明 |
|---|---|---|
| id | BIGINT PK | 评价 ID |
| interview_id | BIGINT | 面试 ID |
| technical_score | INT | 技术评分 |
| communication_score | INT | 沟通评分 |
| logic_score | INT | 逻辑评分 |
| overall_score | INT | 综合评分 |
| evaluation | TEXT | 评价内容 |
| result | TINYINT | 0 淘汰、1 待定、2 通过 |
| feedback_time | DATETIME | 评价时间 |


## 5. 编码规范

### 5.1 通用约定

1. **统一语言**：代码注释、提交说明、接口文档使用简体中文或英文，同一模块内保持一致。  
2. **统一响应**：后端接口统一返回 `Result<T>`，禁止直接裸返回业务对象（特殊文件流除外）。  
3. **统一异常**：业务异常使用 `BusinessException` + 错误码枚举；禁止吞异常。  
4. **统一鉴权**：接口默认需登录；白名单仅限注册/登录等公开接口。  
5. **统一日志**：关键业务操作使用操作日志注解；敏感信息脱敏后落库/输出。

### 5.2 Java 后端规范

| 项 | 规范 |
|----|------|
| JDK | 21+，开启合理空安全与现代语法（record/switch 等按团队约定） |
| 分层 | `controller → service → mapper/repository`，Controller 不写复杂业务 |
| 命名 | 类名大驼峰；方法/变量小驼峰；常量全大写下划线 |
| DTO/VO | 入参 `*Request`/`*DTO`，出参 `*VO`/`*Response`，禁止 Entity 直接暴露给前端 |
| 包结构 | 按业务域分包（job/resume/interview…），不按技术层大杂烩 |
| 权限 | 方法级权限与角色隔离：正式面试题仅面试官；模拟面试仅求职者 |
| 数据库 | 表名/字段下划线；逻辑删除字段 `deleted`；时间字段 `create_time`/`update_time` |
| MyBatis | Mapper 接口 + XML（复杂 SQL）或注解（简单查询），禁止拼接未校验 SQL |
| 配置 | 敏感配置走环境变量 / Nacos，禁止把真实 Key 提交到仓库 |

推荐包结构示例：

```text
com.recruitment.job
├── controller
├── service
├── mapper
├── entity
├── dto
└── vo
```

### 5.3 Vue 前端规范

| 项 | 规范 |
|----|------|
| 组件 | 单文件组件 `.vue`，页面放 `views`，复用组件放 `components` |
| API | 按领域拆分 `src/api/*.js`，禁止在页面内散落硬编码 URL |
| 状态 | 全局状态进 Pinia；页面局部状态用组件内 `ref/reactive` |
| 路由 | 按角色划分路由与菜单；未授权跳转 403/登录页 |
| 样式 | 优先 Element Plus；业务样式按模块组织，避免全局污染 |
| 命名 | 组件大驼峰；目录小写；事件 `onXxx` / `handleXxx` 语义清晰 |

角色页面目录：

```text
views/
├── auth/
├── candidate/
├── hr/
├── interviewer/
├── admin/
└── error/
```

### 5.4 Git 与协作规范

- 分支建议：`main`（稳定） / `develop`（集成） / `feature/*`（功能） / `fix/*`（修复）
- Commit 建议：`feat:` / `fix:` / `docs:` / `refactor:` / `chore:`
- 提交前确认：可编译、关键路径可访问、不提交密钥与本地绝对路径
- 代码评审关注：权限边界、状态流转、AI 接口隔离、搜索/向量同步一致性

### 4.5 AI / 搜索专项规范

1. **正式面试题** 与 **模拟面试** 必须使用不同接口与权限。  
2. 简历解析、人岗匹配、出题等 AI 调用统一走 `recruitment-ai-service`，核心服务通过 Feign 调用。  
3. 简历/职位变更后，需同步：
   - MySQL（主数据）
   - Elasticsearch（检索）
   - Redis Stack（向量）
4. Embedding 维度、模型名称、提示词模板集中配置，避免散落硬编码。

---

## 6. 快速开始

### 6.1 启动中间件

方式 A：Docker（推荐联调）

```bash
cd smart-recruitment/docker
docker compose up -d
```

方式 B：按课程要求安装本机环境（JDK21、Maven3.9+、Nacos、ES、Redis、Redis Stack）

### 6.2 初始化数据库

执行：

```text
smart-recruitment/sql/init.sql
```

默认库名：`smart_recruitment`  
默认数据源（可改）：`root/root@localhost:3306`

### 6.3 启动后端（推荐：本地轻量模式）

默认 `spring.profiles.active=local`，**不依赖** Nacos / MySQL / Redis / ES，仅用于先把前后端跑通。

```bash
cd smart-recruitment/recruitment-backend
mvn clean install -DskipTests

# 核心服务（8080）
cd recruitment-service
mvn spring-boot:run

# AI 服务可选（8081，另开终端）
cd ../recruitment-ai-service
mvn spring-boot:run
```

后续接入完整中间件时再切换：

```bash
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
```

可选环境变量（AI 联调）：

```bash
# Windows PowerShell 示例
$env:OPENAI_API_KEY="your-key"
$env:OPENAI_BASE_URL="https://api.openai.com"
```

### 6.4 启动前端

```bash
cd smart-recruitment/recruitment-web
npm install
npm run dev
```

> Windows 若出现 `Cannot find module ... npm-cli.js`，请使用完整路径：  
> `"C:\Program Files\nodejs\npm.cmd" install` / `"C:\Program Files\nodejs\npm.cmd" run dev`

默认地址：

| 服务 | 地址 |
|------|------|
| 前端 | http://localhost:3000 |
| 核心服务 | http://localhost:8080 |
| AI 服务 | http://localhost:8081 |
| Nacos | http://localhost:8848/nacos |
| ES | http://localhost:9200 |
| Redis Stack | 6379（Redis） / 8001（RedisInsight） |

前端通过 Vite 代理将 `/api` 转发到 `http://localhost:8080`。

---

## 7. 接口约定

- 统一前缀：`/api`
- 认证方式：`Authorization: Bearer <JWT>`
- 详细接口见：`docs/api-docs.md`

主要能力分组：

- 认证：`/api/auth/**`
- 系统管理：`/api/system/**`
- 职位/简历/投递/面试：`/api/job|resume|application|interview/**`
- 搜索：`/api/search/**`
- AI：`/api/ai/**`
- 统计：`/api/dashboard/**`
- 文件：`/api/file/**`

---

## 8. 开发建议顺序

1. 基础 CRUD：用户、角色、职位、简历、投递、面试相关表  
2. 登录鉴权与角色权限  
3. 投递状态流转、面试分配  
4. ES 同步与搜索  
5. Redis Stack 向量写入与人岗匹配  
6. Spring AI：简历解析、正式出题、模拟面试  

最小可运行闭环：

```text
前端：Vue 3 + Element Plus + Pinia + Vue Router
核心服务：Spring Boot + Security + MyBatis + Redis + ES
AI 服务：Spring Boot + Spring AI + Redis Stack
服务治理：Nacos + OpenFeign
数据库：MySQL
```

---

## 9. 相关文档

- 模块划分：`../模块划分.md`
- 接口文档：`docs/api-docs.md`
- 数据库脚本：`sql/init.sql`
- Docker 编排：`docker/docker-compose.yml`

---

## 10. 许可证与团队约定

本项目用于课程/团队实训开发。提交代码前请确认：

1. 未提交 API Key、密码等敏感信息  
2. 角色权限边界正确（尤其是 AI 出题接口）  
3. 前后端字段命名与接口文档一致  
4. 本地可启动并通过基础联调

如有架构变更，请先更新本 README 与 `模块划分.md`，再合入主干。
