# OA_project / 企业招聘与人才智能匹配系统

本仓库主工程位于：

```text
OA_project/smart-recruitment/
```

请阅读完整说明文档：

- [Smart Recruitment README](./OA_project/smart-recruitment/README.md)

## 技术方向（已校正）

| 端 | 技术 |
|----|------|
| **前端** | Vue.js（Vue 3 + Vite + Element Plus） |
| **后端** | Spring Boot（Spring Security + Nacos + MyBatis + Redis + Elasticsearch + Spring AI） |

> 说明：开发沟通中若出现“前端 Spring Boot / 后端 Vue”的表述，属于前后端命名写反；本仓库按上表标准架构执行。

## 角色一览

- 管理员：用户/角色/权限/配置/日志
- HR：职位、简历、面试安排、统计
- 面试官：面试任务、AI 出题、评价
- 求职者：注册登录、浏览投递、简历、面试结果

## 快速入口

```bash
# 中间件
cd OA_project/smart-recruitment/docker
docker compose up -d

# 后端
cd ../recruitment-backend
mvn clean install -DskipTests

# 前端
cd ../recruitment-web
npm install
npm run dev
```
