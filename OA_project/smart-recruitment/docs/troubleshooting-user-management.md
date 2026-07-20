# 用户管理功能故障排查指南

## 问题：重置密码返回 500 错误

### 可能原因及解决方案

#### 1. Service 未注入
**症状**: 后端日志显示 "SysUserService 未注入，无法执行重置密码操作"

**原因**: `SysUserServiceImpl` 没有被 Spring 扫描到

**解决方案**:
- 检查 `SysUserServiceImpl` 类上是否有 `@Service` 注解
- 检查包路径是否正确：`com.recruitment.system.service.impl`
- 检查主类的 `@SpringBootApplication` 是否能扫描到该包

#### 2. 数据库连接问题
**症状**: 日志显示 "找到用户" 但更新失败

**原因**: 数据库连接异常或权限不足

**解决方案**:
```yaml
# 检查 application-dev.yml 中的数据库配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/smart_recruitment
    username: root
    password: 你的密码
```

#### 3. PasswordEncoder 未注入
**症状**: 日志显示 "NullPointerException" 在加密密码时

**原因**: `PasswordEncoder` Bean 未配置

**解决方案**: 检查 `SecurityConfig` 中是否有 `passwordEncoder()` Bean

#### 4. 用户不存在
**症状**: 日志显示 "用户不存在，ID: xxx"

**原因**: 数据库中没有该用户或已被删除

**解决方案**: 
```sql
-- 检查用户是否存在
SELECT * FROM sys_user WHERE id = 你的用户ID AND deleted = 0;
```

#### 5. MyBatis-Plus 配置问题
**症状**: 更新或删除操作返回 0

**原因**: MyBatis-Plus 配置不正确

**解决方案**:
```yaml
# 检查 application-dev.yml
mybatis-plus:
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

### 诊断步骤

1. **启动后端服务**
   ```bash
   cd recruitment-backend/recruitment-service
   mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
   ```

2. **查看启动日志**
   - 确认 `SysUserServiceImpl` 被扫描到
   - 确认数据库连接成功
   - 确认 MyBatis-Plus 配置加载

3. **测试重置密码**
   - 打开浏览器开发者工具 (F12)
   - 点击重置密码按钮
   - 查看 Network 标签中的请求详情
   - 查看后端控制台日志

4. **分析日志输出**
   ```
   正常流程应该看到：
   INFO  - 重置密码请求，用户ID: 2
   INFO  - 开始重置用户密码，用户ID: 2
   INFO  - 找到用户: lijingli, 开始加密新密码
   INFO  - 密码加密完成，开始更新数据库
   INFO  - 数据库更新结果: 1, 用户: lijingli
   INFO  - 用户 2 密码重置成功
   ```

### 常见错误及解决

#### 错误 1: NullPointerException
```
java.lang.NullPointerException: Cannot invoke "..." because "this.sysUserService" is null
```
**解决**: Service 未注入，检查 `@Service` 注解和包扫描

#### 错误 2: SQLSyntaxErrorException
```
java.sql.SQLSyntaxErrorException: Table 'smart_recruitment.sys_user' doesn't exist
```
**解决**: 运行数据库初始化脚本 `sql/init.sql`

#### 错误 3: BadSqlGrammarException
```
BadSqlGrammarException: ... column 'deleted' not found
```
**解决**: 数据库表结构不完整，重新运行 `sql/init.sql`

#### 错误 4: 更新返回 0
```
INFO  - 数据库更新结果: 0, 用户: admin
```
**解决**: 
- 检查用户是否已被逻辑删除 (`deleted = 1`)
- 检查 MyBatis-Plus 逻辑删除配置

### 验证修复

1. **验证重置密码**
   ```bash
   # 使用 PowerShell 测试
   Invoke-WebRequest -Uri "http://localhost:8080/api/system/user/2/reset-password" `
     -Method PUT `
     -Headers @{"Authorization"="Bearer 你的token"}
   ```

2. **验证密码已重置**
   - 退出登录
   - 使用该用户和密码 `123456` 登录
   - 应该能成功登录

3. **验证删除用户**
   ```bash
   Invoke-WebRequest -Uri "http://localhost:8080/api/system/user/3" `
     -Method DELETE `
     -Headers @{"Authorization"="Bearer 你的token"}
   ```

4. **验证用户已删除**
   - 刷新用户列表
   - 被删除的用户不应出现
   - 数据库中 `deleted = 1`

### 联系支持

如果以上方法都无法解决问题，请提供：
1. 完整的后端启动日志
2. 重置密码时的完整错误堆栈
3. 数据库版本和配置信息
4. `application-dev.yml` 配置文件内容
