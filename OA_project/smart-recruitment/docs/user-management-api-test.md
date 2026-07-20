# 用户管理接口测试说明

## 已实现功能

### 1. 重置密码
- **接口**: `PUT /api/system/user/{id}/reset-password`
- **说明**: 将指定用户的密码重置为默认密码 `123456`
- **权限**: 管理员
- **请求示例**:
  ```bash
  PUT http://localhost:8080/api/system/user/1/reset-password
  Authorization: Bearer {token}
  ```
- **响应示例**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": null
  }
  ```

### 2. 删除用户
- **接口**: `DELETE /api/system/user/{id}`
- **说明**: 删除指定用户（逻辑删除，数据库中 deleted 字段标记为 1）
- **权限**: 管理员
- **请求示例**:
  ```bash
  DELETE http://localhost:8080/api/system/user/1
  Authorization: Bearer {token}
  ```
- **响应示例**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": null
  }
  ```

## 测试步骤

1. **启动后端服务**
   ```bash
   cd recruitment-backend/recruitment-service
   mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
   ```

2. **使用管理员账号登录获取 Token**
   ```bash
   POST http://localhost:8080/api/auth/login
   Content-Type: application/json
   
   {
     "username": "admin",
     "password": "admin123",
     "expectedRole": "ADMIN"
   }
   ```

3. **测试重置密码**
   ```bash
   PUT http://localhost:8080/api/system/user/2/reset-password
   Authorization: Bearer {从登录获取的token}
   ```

4. **验证密码已重置**
   - 使用被重置的用户账号和密码 `123456` 尝试登录
   - 应该能成功登录

5. **测试删除用户**
   ```bash
   DELETE http://localhost:8080/api/system/user/3
   Authorization: Bearer {从登录获取的token}
   ```

6. **验证用户已删除**
   - 查询用户列表，被删除的用户不应出现
   - 数据库中该用户的 `deleted` 字段应为 1

## 错误处理

- **用户不存在**: 返回 `BusinessException: "用户不存在"`
- **未授权**: 返回 401 状态码
- **无权限**: 返回 403 状态码

## 注意事项

1. 重置密码功能将密码统一重置为 `123456`（已使用 BCrypt 加密）
2. 删除操作是逻辑删除，不会真正从数据库中移除数据
3. 建议不要删除管理员账号，避免无法登录系统
4. 重置密码后，用户需要使用新密码 `123456` 登录
