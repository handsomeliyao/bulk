# Bulk Commodity Platform

本项目是一个基于 Spring Boot 3 的后台系统，覆盖部门、岗位、部门管理员、部门操作员、权限管理及申请复核流程。

## 技术栈

- Java 21
- Spring Boot 3.5.0（Web / Actuator / AOP / Redis）
- MyBatis + MySQL
- Redis（登录态缓存）
- Springdoc OpenAPI
- EasyExcel（导出）
- Maven

前端位于 `frontend/`，使用 Vue 3 + Vite。

## 目录结构

```text
src/main/java/com/liyao/bulk
  controller/    接口控制层
  service/       业务层
  mapper/        MyBatis Mapper 接口
  model/         实体模型
  dto/           请求与响应对象
  common/        通用返回与异常
  config/ filter/ aop/

src/main/resources
  application.yml
  application-prod.yml
  mapper/*.xml
  sql/init.sql
  static/scalar.html

frontend/
```

## 环境要求

- JDK 21
- Maven 3.9+
- MySQL 8+
- Redis 6+

## 配置说明

主要配置文件：

- `src/main/resources/application.yml`
- `src/main/resources/application-prod.yml`

关键配置项：

- `server.port`：默认 `18889`
- `spring.datasource.*`：MySQL 连接配置
- `spring.data.redis.*`：Redis 连接配置（`application.yml` 当前为 TLS + 6380）
- `springdoc.swagger-ui.path`：`/swagger-ui.html`

登录态缓存配置（含默认值）：

- `bulk.login-user.token-header`：`Authorization`
- `bulk.login-user.fallback-token-header`：`X-Token`
- `bulk.login-user.cache-key-prefix`：`login:token:`
- `bulk.login-user.expire-seconds`：`7200`

## 数据库初始化

执行以下脚本：

- `src/main/resources/sql/init.sql`
- 可选：`src/main/resources/sql/sys-menu-data.sql`

## 启动方式

### 启动后端

```bash
mvn clean compile
mvn spring-boot:run
```

或使用 Maven Wrapper：

```bash
./mvnw spring-boot:run
```

### 启动前端（可选）

```bash
cd frontend
npm install
npm run dev
```

## API 文档

- Swagger UI：`http://localhost:18889/swagger-ui.html`
- OpenAPI：`http://localhost:18889/v3/api-docs`
- Scalar：`http://localhost:18889/scalar.html`

## 主要接口分组

- 认证：`/api/auth`
- 部门：`/api/departments`
- 部门申请：`/api/department-applications`
  - 待复核查询：`GET /pending`（`PENDING`、`REJECTED`、`CANCELED`）
  - 已复核查询：`GET /reviewed`（`APPROVED`）
  - 复核：`POST /review`
  - 复核兼容：`POST /{id}/review`
  - 撤销：`POST /{id}/revoke`
- 内部岗位：`/api/internal-positions`
- 岗位申请：`/api/position-applications`
- 部门管理员：`/api/department-admins`
- 管理员申请：`/api/department-admin-applications`
- 部门操作员：`/api/department-operators`
- 操作员申请：`/api/department-operator-applications`
- 菜单：`/api/menus/tree`
- 权限：`/api/permissions/tree`

## 日志

日志配置文件：`src/main/resources/logback-spring.xml`

- 控制台 + 文件输出
- 文件路径：`logs/bulk-commodity-platform.log`
- 按天滚动，保留 30 天

## 常见问题

1. Redis 连接失败
- 检查端口与 TLS 是否匹配（`6380 + ssl.enabled=true` 或 `6379 + ssl=false`）
- 检查白名单/安全组

2. MySQL 连接失败
- 检查 `spring.datasource.url / username / password`

3. 登录后提示 token 异常
- 检查请求头（`Authorization` 或 `X-Token`）
- 检查 Redis 可用性与 key 前缀

## 最小验证

```bash
mvn -DskipTests compile
```