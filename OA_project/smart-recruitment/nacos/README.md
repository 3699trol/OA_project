# Nacos 配置与服务发现

`dev` profile 将 Nacos 作为必需依赖。核心服务和 AI 服务会先读取配置中心，再启动并注册服务；如果 Nacos 或必需 Data ID 不可用，启动会直接失败，避免带着不完整配置运行。

每个 Data ID 包含启动校验标记。即使 Nacos 客户端只把空配置记录为警告，应用也会在创建业务 Bean 前检查共享配置和服务专属配置，缺失时直接报告对应 Data ID、分组或命名空间配置错误。

## Data ID

所有配置使用 `SMART_RECRUITMENT` 分组：

| Data ID | 使用方 | 主要内容 |
| --- | --- | --- |
| `recruitment-common.yml` | 两个后端服务 | JWT、安全策略、公共日志配置 |
| `recruitment-service.yml` | 核心服务 | 端口、MySQL、Redis、Elasticsearch、上传目录、登录限流 |
| `recruitment-ai-service.yml` | AI 服务 | 端口、模型地址、模型名称、API Key、超时和代理 |

仓库中的配置模板保留环境变量占位符，避免提交真实密码和密钥。应用读取 Nacos 配置后，再从各自进程的环境变量解析这些占位符。

登录限流等需要在线调整的业务参数直接在 Nacos 配置中保存默认值，不应再用同名环境变量覆盖，否则环境变量的更高优先级会让 Nacos 修改看起来没有生效。

## 发布配置

首次启动或模板变更后执行：

```powershell
docker compose -p docker --env-file .env -f docker/docker-compose.yml up --force-recreate nacos-config-init
```

初始化容器会等待 Nacos 就绪，并幂等发布 `nacos/config/*.yml`。执行成功后容器状态为 `Exited (0)` 是正常结果。

当前 Docker Nacos 未启用鉴权，用户名和密码保持为空；若其他环境启用了鉴权，初始化容器会使用 `NACOS_USERNAME`、`NACOS_PASSWORD` 登录后再发布配置。

控制台地址：`http://localhost:8848/nacos`

## 服务注册发现

两个服务注册到 `SMART_RECRUITMENT` 分组：

- `recruitment-service`
- `recruitment-ai-service`

`recruitment-service` 的 Feign 客户端默认不设置固定 URL，而是通过服务名 `recruitment-ai-service` 从 Nacos 选择健康实例。只有明确设置 `AI_SERVICE_URL` 时才改为直连。

服务注册地址默认由客户端自动探测。多网卡或特殊容器网络环境可设置 `NACOS_DISCOVERY_IP`，该地址必须能被其他服务实例访问，不能盲目使用 `127.0.0.1`。

## 动态刷新

以下业务参数支持在 Nacos 修改后实时生效，无需重启核心服务：

- `recruitment.auth.user-cache-ttl`
- `recruitment.auth.login-failure-window`
- `recruitment.auth.max-login-failures`

AI 模型参数和 HTTP 客户端也处于刷新作用域。数据库、Redis、Elasticsearch连接地址、服务端口和 JWT 密钥虽然由 Nacos 托管，但修改后应重启对应服务；运行中切换连接池、监听端口或签名密钥会造成不一致。

环境变量和 JVM 启动参数优先级高于 Nacos。需要通过控制台动态修改某个参数时，不要同时为该参数设置环境变量。

Nacos 连接地址、命名空间和鉴权凭据属于连接配置中心之前必须知道的引导参数，不能再存入同一个 Nacos 实例，需通过 `application-dev.yml` 默认值或环境变量提供。

## 本地兜底

不启动 Nacos 时可显式使用 `local` profile：

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

`local` 仅用于轻量开发，不代表完整中间件联调环境，也不会进行服务注册与配置动态刷新。
