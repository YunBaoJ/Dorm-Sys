# 宿舍管理系统 Code Wiki

> 本文档为「宿舍管理系统（Dorm-Sys）」项目的结构化代码 Wiki，覆盖项目整体架构、模块职责、关键类与函数、依赖关系与运行方式。
> 仓库根目录：`f:\bishe\Antigravity\Dorm-Sys`

---

## 目录

- [1. 项目概览](#1-项目概览)
- [2. 技术栈](#2-技术栈)
- [3. 项目目录结构](#3-项目目录结构)
- [4. 整体架构](#4-整体架构)
- [5. 后端模块详解](#5-后端模块详解)
  - [5.1 启动入口](#51-启动入口)
  - [5.2 通用工具层（common）](#52-通用工具层common)
  - [5.3 配置与安全层（config）](#53-配置与安全层config)
  - [5.4 认证授权机制](#54-认证授权机制)
  - [5.5 数据访问层（mapper）](#55-数据访问层mapper)
  - [5.6 实体与数据模型（entity / dto / enums）](#56-实体与数据模型entity--dto--enums)
  - [5.7 业务服务层（service）](#57-业务服务层service)
  - [5.8 控制器层（controller）](#58-控制器层controller)
  - [5.9 数据库设计](#59-数据库设计)
- [6. 前端模块详解](#6-前端模块详解)
  - [6.1 应用入口](#61-应用入口)
  - [6.2 状态管理（store）](#62-状态管理store)
  - [6.3 路由系统](#63-路由系统)
  - [6.4 HTTP 请求层](#64-http-请求层)
  - [6.5 API 模块](#65-api-模块)
  - [6.6 布局与共享组件](#66-布局与共享组件)
  - [6.7 视图层（views）](#67-视图层views)
  - [6.8 样式系统](#68-样式系统)
- [7. 关键业务流程](#7-关键业务流程)
- [8. 依赖关系](#8-依赖关系)
- [9. 项目运行方式](#9-项目运行方式)
- [10. 测试体系](#10-测试体系)
- [11. 架构特点与注意事项](#11-架构特点与注意事项)

---

## 1. 项目概览

**宿舍管理系统（Dorm-Sys）** 是一个面向高校宿舍场景的前后端分离 Web 应用，支持三种角色的完整业务闭环：

| 角色 | 角色标识 | 首页路由 | 职责 |
|---|---|---|---|
| 学生 | `student` | `/student/desk` | 个人宿舍信息查询、报修、缴费、访客、调宿申请、智能问答、室友通讯 |
| 宿管 | `dormmanager` | `/dormmanager/workbench` | 入住办理、报修处理、卫生检查、费用录入、调宿审批、晚归/物品登记、通知发布 |
| 管理员 | `admin` | `/admin/overview` | 全局看板、用户/楼栋/房间资源管理、操作审计、报表导出 |

系统采用经典的「三层角色 + 双重授权」模型，前端按角色分台展示工作台，后端基于 URL 前缀 + 数据范围双重控制权限。覆盖报修、调宿、访客、卫生、费用、通话、聊天、物品、晚归等 10+ 类业务，并提供仪表盘、报表、操作日志等管理能力。

---

## 2. 技术栈

### 后端

| 维度 | 技术 / 版本 |
|---|---|
| 框架 | Spring Boot 3.3.0 |
| 语言 | Java 17 |
| ORM | MyBatis-Plus 3.5.5（`mybatis-plus-spring-boot3-starter`） |
| 数据库 | MySQL（驱动 `mysql-connector-j`，库名 `dormitory`） |
| 鉴权 | JWT（`jjwt` 0.12.3，HS 签名，24 小时过期） |
| 密码 | BCrypt（`spring-security-crypto`，仅用加密工具，**未启用 Spring Security 过滤器链**） |
| 构建 | Maven（含 `mvnw` 包装器） |
| 其他 | Lombok |
| 端口 | 8088 |

### 前端

| 维度 | 技术 / 版本 |
|---|---|
| 框架 | Vue 3.5.39 |
| 构建 | Vite 8.1.1 |
| 路由 | Vue Router 5.1.0 |
| 状态 | Pinia 3.0.4 + `pinia-plugin-persistedstate` 4.7.1 |
| UI 库 | Element Plus 2.14.2（按需引入，`unplugin-vue-components` 32.1.0） |
| HTTP | Axios 1.18.1 |
| 图标 | `@lucide/vue` 1.24.0 |
| E2E 测试 | Playwright 1.61.1 |
| 端口 | 5173（Vite dev） |

---

## 3. 项目目录结构

```
Dorm-Sys/
├── backend/                          # Spring Boot 后端
│   ├── pom.xml                       # Maven 依赖
│   ├── mvnw / mvnw.cmd               # Maven 包装器
│   └── src/
│       ├── main/
│       │   ├── java/com/dorm/backend/
│       │   │   ├── BackendApplication.java        # 启动类
│       │   │   ├── common/                        # 通用工具（Result/JwtUtils/AuthUtils/异常处理）
│       │   │   ├── config/                        # 配置与拦截器（CORS/JWT/审计/WebMvc）
│       │   │   ├── controller/                    # 19 个 REST 控制器
│       │   │   ├── dto/                           # 请求/响应数据传输对象
│       │   │   ├── entity/                        # 20 个数据实体
│       │   │   ├── enums/                         # 状态枚举
│       │   │   ├── mapper/                        # 19 个 MyBatis-Plus Mapper 接口
│       │   │   └── service/                       # Service 接口 + impl 实现
│       │   └── resources/
│       │       ├── mapper/                        # 4 个 XML Mapper（空壳）
│       │       ├── application.yml                # 主配置
│       │       ├── application-dev.yml           # dev profile（打印 SQL）
│       │       ├── logback-spring.xml            # 日志配置
│       │       ├── schema.sql                    # 建表 + 初始化数据
│       │       └── schema-business-record.sql     # 历史遗留（business_record 单表）
│       └── test/                                  # 单元/集成测试
└── frontend/                         # Vue 3 前端
    ├── package.json
    ├── vite.config.js                # Vite 配置（含 /api 代理到 8088）
    ├── index.html
    ├── playwright.config.js         # E2E 测试配置
    └── src/
        ├── main.js                   # 应用入口
        ├── App.vue                   # 根组件（登录页全屏 / 其它页带外壳）
        ├── api/                      # 16 个 axios 接口模块
        ├── assets/css/               # style.css + theme.css
        ├── components/
        │   ├── layout/               # Sidebar / Topbar
        │   ├── ui/                   # Hero / ServiceCard
        │   ├── ChatDrawer.vue        # 聊天抽屉
        │   ├── WeatherWidget.vue     # 天气组件
        │   └── LocalFeaturePage.vue  # 通用业务记录页模板
        ├── router/index.js           # 角色路由
        ├── store/                    # app.js + user.js
        ├── utils/request.js          # axios 实例
        └── views/                    # 按角色分目录的页面
            ├── Login.vue
            ├── student/  (11 个)
            ├── manager/  (14 个)
            └── admin/    (8 个)
```

---

## 4. 整体架构

### 4.1 分层架构

系统采用前后端分离 + 后端经典分层架构：

```
┌─────────────────────────────────────────────────────────────┐
│  浏览器（Vue 3 SPA）                                         │
│  Views → Components → API 模块 → utils/request.js (axios)   │
└──────────────────────────┬──────────────────────────────────┘
                           │  HTTP /api/** (Vite 代理 → 8088)
                           ▼
┌─────────────────────────────────────────────────────────────┐
│  Spring Boot 后端                                           │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ Controller 层（19 个，REST 端点）                       │  │
│  └───────────────┬───────────────────────────────────────┘  │
│                  │ 调用                                      │
│  ┌───────────────▼───────────────────────────────────────┐  │
│  │ Service 层（业务逻辑 + 数据范围控制）                   │  │
│  └───────────────┬───────────────────────────────────────┘  │
│                  │ 继承 ServiceImpl                          │
│  ┌───────────────▼───────────────────────────────────────┐  │
│  │ Mapper 层（MyBatis-Plus BaseMapper，零自定义 SQL）     │  │
│  └───────────────┬───────────────────────────────────────┘  │
│                  │ JDBC                                       │
│  ┌───────────────▼───────────────────────────────────────┐  │
│  │ MySQL（dormitory 库，20 张表）                          │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  横切关注点：                                                │
│   • JwtAuthInterceptor（认证 + URL 粗粒度授权）             │
│   • OperationAuditInterceptor（管理员操作审计）             │
│   • GlobalExceptionHandler（统一异常 → Result）             │
│   • DormManagerScopeService（宿管数据范围 + 5 秒缓存）       │
└─────────────────────────────────────────────────────────────┘
```

### 4.2 角色权限模型

**双重授权机制**：

1. **粗粒度（拦截器层）**：`JwtAuthInterceptor` 基于 URL 前缀 + HTTP 方法做角色黑白名单。例如 `/api/dashboard/stats` 仅 admin 可读；student 不能 GET `/api/user/*`；非 admin 不能 POST/PUT/DELETE `/api/building/`、`/api/room/`。
2. **细粒度（Service/Controller 层）**：`DormManagerScopeService` 提供 `canManageBuilding/Room/Student` 谓词，宿管只能操作自己管辖楼栋下的资源；学生只能看/改自己的数据（强制 `studentId = currentUserId`）。

### 4.3 关键架构模式

- **三层用户模型**：`sys_user` 主表 + `student_info` / `manager_info` / `admin_info` 三个角色扩展表，按 `user_id` 一一对应。
- **床位即占用关系**：`bed.student_id` 是入住的权威标识，所有「我的宿舍」「室友」查询都从 `bed` 表反查。
- **通用业务表**：`business_record` 一张表 + 一组 API 通过 `type` 字段区分公告/消息/备忘/反馈等多类型业务。
- **虚拟字段填充**：实体用 `@TableField(exist=false)` 声明展示字段，Service 层按需批量加载关联数据填充，避免 N+1。
- **`listXxxWithDetails` 模式**：所有列表查询都接收 `role + userId` 参数，内部按角色过滤并批量加载关联。
- **懒加密迁移**：登录时检测旧明文密码并自动升级为 BCrypt。

---

## 5. 后端模块详解

### 5.1 启动入口

**文件**：[BackendApplication.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/BackendApplication.java)

```java
@SpringBootApplication
@MapperScan("com.dorm.backend.mapper")
public class BackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
```

- `@MapperScan` 扫描 `com.dorm.backend.mapper` 包下所有 Mapper 接口。
- **未启用** Spring Security 过滤器链，认证完全靠自定义拦截器 + JWT 实现。

---

### 5.2 通用工具层（common）

#### `Result.java` — 统一响应封装

**文件**：[Result.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/common/Result.java)

泛型类 `Result<T>`，字段：`code` / `message` / `data`，Lombok `@Data`。全系统所有 Controller 返回 `Result<T>`，前端按 `code` 字段判断成败。

| 静态工厂方法 | 说明 |
|---|---|
| `success(T data)` | 成功响应，code=200，message="success" |
| `success()` | 无数据成功 |
| `error(Integer code, String message)` | 指定错误码 |
| `error(String message)` | 默认 code=500 |

#### `JwtUtils.java` — JWT 生成与解析

**文件**：[JwtUtils.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/common/JwtUtils.java)

`@Component`，构造器注入 `@Value("${jwt.secret:...}")`，用 `Keys.hmacShaKeyFor()` 构建 `SecretKey`。

| 方法签名 | 说明 |
|---|---|
| `String generateToken(Long userId, String username, String role)` | 生成 JWT，subject=username，claims 含 `userId`/`role`，**过期时间 24 小时**（`1000*60*60*24`） |
| `Map<String, Object> parseToken(String token)` | 解析 token，返回 `userId`/`username`/`role`；失败抛异常由拦截器捕获 |

#### `AuthUtils.java` — 当前登录人上下文

**文件**：[AuthUtils.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/common/AuthUtils.java)

静态工具类，从 `RequestContextHolder` 取 `JwtAuthInterceptor` 注入的请求属性：

| 方法 | 说明 |
|---|---|
| `Long getCurrentUserId()` | 当前登录用户 ID |
| `String getCurrentUserRole()` | 当前登录用户角色 |
| `boolean isStudent()` | 便捷判断角色为 student |

角色常量：`student` / `dormmanager` / `admin`。

#### `GlobalExceptionHandler.java` — 全局异常处理

**文件**：[GlobalExceptionHandler.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/common/GlobalExceptionHandler.java)

`@RestControllerAdvice`，把异常统一转成 `Result<Void>`：

| 异常 | code | message |
|---|---|---|
| `MethodArgumentNotValidException` | 400 | 拼接所有字段错误 |
| `HttpMessageNotReadableException` | 400 | "请求数据格式错误" |
| `MissingServletRequestParameterException` | 400 | "缺少必要参数" |
| `DataIntegrityViolationException` | 409 | "操作失败，数据冲突" |
| `HttpRequestMethodNotSupportedException` | 405 | — |
| 兜底 `Exception` | 500 | "服务器内部错误，请联系管理员"（并 `log.error` 记录堆栈） |

---

### 5.3 配置与安全层（config）

#### `CorsConfig.java`

**文件**：[CorsConfig.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/config/CorsConfig.java)

`@Configuration` 实现 `WebMvcConfigurer`。
- 允许来源：`http://localhost:*`、`http://127.0.0.1:*`（开发环境）。
- 允许方法：GET/POST/PUT/DELETE/OPTIONS，允许所有头，`allowCredentials(true)`，`maxAge(3600)`。

#### `JwtAuthInterceptor.java` — 认证 + 角色路由隔离

**文件**：[JwtAuthInterceptor.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/config/JwtAuthInterceptor.java)

`@Component` 实现 `HandlerInterceptor`，`preHandle` 逻辑：

1. OPTIONS 请求直接放行（CORS 预检）。
2. 从 `Authorization: Bearer xxx` 头取 token，缺失 → 401 "请先登录"。
3. `jwtUtils.parseToken()` 解析，把 `currentUserId` / `currentUsername` / `currentUserRole` 注入 request attribute。
4. **四道角色关卡**（基于 URL 前缀的粗粒度授权）：

| 关卡 | 规则 |
|---|---|
| `isAdminOnlyRead` | 非 admin 的 GET 访问 `/api/dashboard/stats`、`/api/dashboard/alerts`、`/api/operationLog/*`、`/api/admin/report/*` → 403 |
| `isForbiddenStudentRead` | student 的 GET 访问 `/api/user/*`、`/api/bed/*`、`/api/dashboard/buildings` → 403 |
| `isForbiddenStudentMutation` | student 非 GET 访问 `/api/room/`、`/api/building/`、`/api/bed/`、`/api/feeBill/`、`/api/user/*`（除 `/api/user/save`）→ 403 |
| `isAdminOnlyMutation` | 非 admin 非 GET 访问 `/api/building/`、`/api/room/` → 403 |

401/403 响应体手写 JSON：`{"code":401/403,"message":"...","data":null}`。

#### `OperationAuditInterceptor.java` — 管理员操作审计

**文件**：[OperationAuditInterceptor.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/config/OperationAuditInterceptor.java)

`@Component`，`afterCompletion` 阶段执行（请求结束后）。

- **只审计 admin 角色**的写操作（POST/PUT/DELETE），且排除 `/api/operationLog/` 自身。
- 写入 `operation_log` 表：`operatorId` / `operatorName` / `module` / `action` / `path` / `result`（成功/失败，按 status<400 且无异常判断）/ `summary` / `createTime`。
- 模块归类 `moduleOf`：

| URL 前缀 | 模块名 |
|---|---|
| `/api/user/` | 用户权限 |
| `/api/building/` | 楼栋资源 |
| `/api/room/` + `/api/bed/` | 房间资源 |
| `/api/businessRecord/` | 公告管理 |
| `/api/repair` | 报修监控 |
| 其余 | 系统管理 |

- 动作归类 `actionOf`：POST→新增、PUT→修改、DELETE→删除。

#### `WebMvcConfig.java` — 拦截器注册

**文件**：[WebMvcConfig.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/config/WebMvcConfig.java)

`@Configuration`，构造器注入两个拦截器。`addInterceptors`：
- `jwtAuthInterceptor` 拦截 `/api/**`，排除 `/api/auth/login` 和 `/error`。
- `operationAuditInterceptor` 拦截 `/api/**`。
- 顺序：JWT 认证在前，审计在后。

---

### 5.4 认证授权机制

#### `LoginDTO`

**文件**：[LoginDTO.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/dto/LoginDTO.java)

字段：`username` / `password` / `role`（登录时必须选角色）。

#### `AuthController.login()` 流程

**文件**：[AuthController.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/controller/AuthController.java)

`POST /api/auth/login`：

1. 用 `QueryWrapper` 按 `username` + `role` 联合查询 `sys_user`（同一用户名不同角色视为不同账号）。
2. 用户不存在或 `passwordService.matches()` 失败 → 401 "账号、密码错误或角色不匹配"。
3. `enabled == false` → 403 "账号已被停用"。
4. **懒加密**：若数据库密码还不是 BCrypt 格式（`isEncoded` 返回 false），用本次输入的明文密码 `encode` 后回写数据库（兼容旧明文数据迁移）。
5. 调 `jwtUtils.generateToken(userId, username, role)` 生成 JWT。
6. 把 `user.password` 置 null，返回 `{token, user}`。

> **注意**：系统没有独立注册接口，用户由 admin 通过 `/api/user/save` 创建；学生自助修改个人信息也走 `/api/user/save`。

#### `PasswordService`

**文件**：[PasswordService.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/service/PasswordService.java)

`@Service`，内部 `BCryptPasswordEncoder`（来自 spring-security-crypto）。

| 方法 | 说明 |
|---|---|
| `String encode(String rawPassword)` | BCrypt 加密 |
| `boolean matches(String rawPassword, String storedPassword)` | 校验 |
| `boolean isEncoded(String password)` | 用正则 `^\$2[aby]\$\d{2}\$.{53}$` 判断是否已是 BCrypt 哈希 |

#### `UserServiceImpl`

**文件**：[UserServiceImpl.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/service/impl/UserServiceImpl.java)

极简：`extends ServiceImpl<UserMapper, User>`，无自定义方法，完全依赖 MyBatis-Plus 通用 CRUD。

#### 角色与访问控制总结

- 三角色：`student` / `dormmanager` / `admin`。
- 访问控制分两层：拦截器粗粒度 URL 控制 + Service/Controller 细粒度数据范围控制。
- 默认密码：admin 新建用户未传密码时，自动 `encode("123456")`（见 `UserController.save`）。

---

### 5.5 数据访问层（mapper）

**位置**：`backend/src/main/java/com/dorm/backend/mapper/`

所有 19 个 Mapper 全部 `extends BaseMapper<T>`，**无自定义方法**：

| Mapper | 实体 | 表 |
|---|---|---|
| `UserMapper` | `User` | `sys_user` |
| `RoomMapper` | `Room` | `room` |
| `BedMapper` | `Bed` | `bed` |
| `BuildingMapper` | `Building` | `building` |
| `BusinessRecordMapper` | `BusinessRecord` | `business_record` |
| `CallRecordMapper` | `CallRecord` | `call_record` |
| `ChatMessageMapper` | `ChatMessage` | `chat_message` |
| `FeeBillMapper` | `FeeBill` | `fee_bill` |
| `HygieneRecordMapper` | `HygieneRecord` | `hygiene_record` |
| `ItemRecordMapper` | `ItemRecord` | `item_record` |
| `LateReturnRecordMapper` | `LateReturnRecord` | `late_return_record` |
| `ManagerInfoMapper` | `ManagerInfo` | `manager_info` |
| `AdminInfoMapper` | `AdminInfo` | `admin_info` |
| `StudentInfoMapper` | `StudentInfo` | `student_info` |
| `OperationLogMapper` | `OperationLog` | `operation_log` |
| `RepairRequestMapper` | `RepairRequest` | `repair_request` |
| `StayHistoryMapper` | `StayHistory` | `stay_history` |
| `TransferRequestMapper` | `TransferRequest` | `transfer_request` |
| `VisitorRecordMapper` | `VisitorRecord` | `visitor_record` |

#### XML Mapper 文件

**位置**：`backend/src/main/resources/mapper/`

含 `CallRecordMapper.xml`、`ItemRecordMapper.xml`、`LateReturnRecordMapper.xml`、`FeedbackMapper.xml`，**全部为空壳**：只有 `<mapper namespace="...">` 声明，无任何 `<select>`/`<insert>` 等自定义 SQL。

> **结论**：系统 100% 依赖 MyBatis-Plus 的 `BaseMapper` + `QueryWrapper` / `UpdateWrapper` 进行数据操作，没有手写 SQL（除 `schema.sql` 初始化）。

#### MyBatis-Plus 配置（application.yml）

- `map-underscore-to-camel-case: true`（下划线转驼峰）
- `id-type: auto`（主键自增）
- dev profile 用 `Slf4jImpl` 打印 SQL，prod 用 `NoLoggingImpl`
- 实体用 `@TableName` 指定表名、`@TableId(type=IdType.AUTO)`、`@TableField(exist=false)` 标记虚拟字段。

#### `DormManagerScopeService` — 数据范围控制核心

**文件**：[DormManagerScopeService.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/service/DormManagerScopeService.java)

`@Service`，构造器对 `BuildingService` / `RoomService` / `BedService` 用 `@Lazy` 注入（打破循环依赖）。

- **5 秒 TTL 内存缓存**（`ConcurrentHashMap` + 自定义 `CacheEntry`），避免短时间内重复查库。

| 方法 | 说明 |
|---|---|
| `List<Long> managedBuildingIds(Long managerUserId)` | 先查 `manager_info` 按 `user_id` 找 `building_id`；若为空回退按 `building.manager`（宿管姓名）匹配 |
| `List<Long> managedRoomIds(Long managerUserId)` | 基于管辖楼栋 ID 查 `room` 表 |
| `List<Long> managedStudentIds(Long managerUserId)` | 基于管辖房间查 `bed.student_id`（非空） |
| `boolean canManageBuilding(managerUserId, buildingId)` | 包含判断 |
| `boolean canManageRoom(managerUserId, roomId)` | 同上 |
| `boolean canManageStudent(managerUserId, studentId)` | 同上 |
| `List<String> managedBuildingNames(...)` | 返回楼栋名称列表 |

**使用模式**：各 Service 的 `listXxxWithDetails` 方法接收 `role` + `userId` 参数，若 role=dormmanager 则用本服务过滤；若 role=student 则强制 `studentId = userId`；admin 不过滤。Controller 的 `getById`/`save`/`delete` 再做单条 `canManageXxx` 校验。

#### 「按需加载关联数据」模式

几乎所有 `listXxxWithDetails` 都遵循同一模式（以 `RepairRequestServiceImpl` 为典型）：

1. 用 `QueryWrapper` 查主表（带角色过滤）。
2. 收集所有 `userId` / `roomId` / `bedId` 等 FK 到 Set。
3. 用 `listByIds` 批量查关联表，构造 `Map<Long, X>`。
4. 遍历主表填充虚拟字段（`submitterName`、`roomName` 等）。

> 这避免了 N+1 查询，但 `BuildingServiceImpl.getBuildingsWithStats` 例外——它 `roomService.list()` 和 `bedService.list()` 全表加载（潜在性能问题）。

---

### 5.6 实体与数据模型（entity / dto / enums）

**位置**：`backend/src/main/java/com/dorm/backend/entity/`

20 个实体与数据库表一一对应（详见 [5.9 数据库设计](#59-数据库设计)）。实体统一使用 Lombok `@Data` + MyBatis-Plus 注解。

#### 虚拟字段填充约定

实体中通过 `@TableField(exist=false)` 声明的「展示字段」由 Service 层手动填充，典型如：

- `Room.buildingName`、`Room.occupied`（已入住人数）
- `RepairRequest.submitterName` / `handlerName` / `roomName`
- `Bed.studentName` / `roomName` / `buildingName`
- `Building.totalRooms` / `occupiedRooms` / `freeRooms`（动态统计，不落库）

#### 状态枚举（enums）

**位置**：`backend/src/main/java/com/dorm/backend/enums/`

| 枚举 | 取值 |
|---|---|
| `BedStatusEnum` | `EMPTY` / `OCCUPIED` / `BROKEN` |
| `RoomStatusEnum` | `NORMAL` / `FULL` / `MAINTENANCE` |
| `RepairStatusEnum` | `PENDING` / `PROCESSING` / `COMPLETED` |
| `FeeStatusEnum` | `UNPAID` / `PAID` |

#### DTO

**位置**：`backend/src/main/java/com/dorm/backend/dto/`

- `LoginDTO`：登录请求（username / password / role）
- `RoomBatchCreateRequest`：批量建房间请求（buildingId / startFloor / endFloor / roomsPerFloor / capacity）
- `AdminNoticeResponse`：管理员公告响应

---

### 5.7 业务服务层（service）

**位置**：`backend/src/main/java/com/dorm/backend/service/`（接口） + `service/impl/`（实现）

绝大多数 Service 实现继承 MyBatis-Plus 的 `ServiceImpl<XxxMapper, Xxx>`，业务逻辑集中在 Controller 和少数自定义 Service 方法。

#### 核心业务服务

##### 5.7.1 房间/床位分配

###### `RoomServiceImpl`

**文件**：[RoomServiceImpl.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/service/impl/RoomServiceImpl.java)

- `listRoomsWithDetails(Long buildingId, String role, Long userId)`：
  - dormmanager 用 `managedBuildingIds` 过滤；指定 `buildingId` 时校验是否在管辖范围内。
  - 按需加载楼栋名称（`listByIds`）。
  - 每间房用 `bedService.count(QueryWrapper)` 统计入住人数（`student_id IS NOT NULL OR status='OCCUPIED'`）。

###### `BedController`（含床位分配核心逻辑）

**文件**：[BedController.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/controller/BedController.java)

`POST /api/bed/save` 是床位分配的核心：

- dormmanager 三重权限校验：原房间、学生所在房间、目标房间都必须在管辖范围。
- **换人入住逻辑**（`oldStudentId != newStudentId` 时）：
  - 旧学生退宿：查 `stay_history` 中 `check_out_date IS NULL` 的记录，设置退宿时间。
  - 新学生入住：先把该学生在其他床位的 `student_id` 清空、状态置 `EMPTY`（防止一人多床）；关闭其旧住宿历史；新建 `StayHistory` 记录 `check_in_date=now`。
- 用 `UpdateWrapper` 精细更新字段（null 也显式 set）。

###### `TransferRequestController` — 调宿审批闭环

**文件**：[TransferRequestController.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/controller/TransferRequestController.java)

`POST /api/transferRequest/save`（`@Transactional`）：

- **学生**：不能改已有申请，强制 `studentId=自己`、`status=PENDING`。
- **宿管**：校验对当前床位所在房间的管辖权；目标房间也必须在管辖内；不能改 `studentId`/`currentBedId`/`reason`/`createTime`。

**审批通过执行 `applyApprovedTransfer`**：

1. 必须指定 `targetRoomId`。
2. `findCurrentBed`：按 `currentBedId` 或 `student_id` 找当前床位。
3. `findAvailableTargetBed`：在目标房间找 `student_id=null` 且 `status=EMPTY` 的床位。
4. `releaseBed`：释放原床位（student_id=null, status=EMPTY）。
5. 占用目标床位（student_id=学生, status=OCCUPIED）。
6. `releaseOtherBedsForStudent`：清理该学生在其他床位的残留占用。
7. `refreshRoomStatus`：重算原房间和目标房间状态——入住数 ≥ capacity 则 `FULL`，否则 `NORMAL`。

`canManageTransfer` 辅助方法：按 currentBedId 或 studentId 反查床位，判断宿管管辖权。

###### `StayHistoryServiceImpl`

**文件**：[StayHistoryServiceImpl.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/service/impl/StayHistoryServiceImpl.java)

极简，无自定义方法；住宿历史的写入/关闭全部在 `BedController` 和 `TransferRequestController` 中手动管理。

##### 5.7.2 报修

**文件**：[RepairRequestServiceImpl.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/service/impl/RepairRequestServiceImpl.java) + [RepairRequestController.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/controller/RepairRequestController.java)

- **状态机**：`PENDING`（待处理）→ `PROCESSING`（处理中）→ `COMPLETED`（已完成）。
- `listRepairRequestsWithDetails`：学生只看自己的；宿管只看管辖房间下的；按需加载提交人/处理人/房间/楼栋名称。
- `save` 业务规则：
  - 学生提交：强制 `submitterId=自己`、`status=PENDING`；通过 `bed.student_id` 反查当前宿舍作为 `roomId`；未分宿舍不能报修。
  - 学生修改已有报修：必须自己是提交人，且不能改 `status`/`handlerId`（防止学生自己结单）。
  - 宿管修改：校验对 `roomId` 的管辖权。
  - 兜底：`roomId` 为空时按 `submitterId` 反查床位补全。
- `delete`：学生无权删除；宿管校验管辖权。

##### 5.7.3 聊天 / 通话记录

###### `ChatController`

**文件**：[ChatController.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/controller/ChatController.java)

服务 `ChatMessageServiceImpl` 极简，逻辑全在 Controller。

- **核心约束：只能与当前室友聊天**（通过 `bed.student_id` 反查 `roomId` 比对）。
- 消息类型：`PRIVATE`（私聊，`receiverId` 必填，`roomId` 置空）、`GROUP`（群聊，`roomId`=自己房间，`receiverId` 置空）。
- 内容长度 1–1000 字。

| 端点 | 说明 |
|---|---|
| `POST /api/chat/send` | 发送消息 |
| `GET /api/chat/private/{targetUserId}` | 私聊历史（双向查询，LIMIT 200，按时间升序） |
| `GET /api/chat/conversations` | 会话列表（取最近 500 条私聊，按对方去重保留最新一条，过滤掉非当前室友） |
| `GET /api/chat/notifications` | 通知（发给我的私聊 + 我房间的群聊除自己发的），LIMIT 30 |
| `GET /api/chat/group/{roomId}` | 群聊历史（校验 roomId 是自己房间），LIMIT 200 |

`populateSenderInfo`：批量填充发送者姓名和头像。

###### `CallRecordController`

**文件**：[CallRecordController.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/controller/CallRecordController.java)

- **状态机**：`PENDING` → `ACCEPTED` → `FINISHED`。
- `list`：学生只看自己；宿管按 `managedStudentIds` 过滤；admin 全看。
- `save`：新增时学生强制 `studentId=自己`；宿管新增校验 `canManageStudent`；学生不能修改（改状态只能宿管/admin）。

##### 5.7.4 仪表盘与报表

###### `DashboardController`

**文件**：[DashboardController.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/controller/DashboardController.java)

| 端点 | 角色 | 说明 |
|---|---|---|
| `GET /api/dashboard/stats` | admin only | 学生数、宿管数、楼栋数、房间数 |
| `GET /api/dashboard/alerts` | admin only | 待处理报修数、维护状态房间数 → 告警列表 |
| `GET /api/dashboard/student` | 学生 | 个人仪表盘——未缴账单金额、待处理报修数、最新卫生分、待办列表（未缴账单+处理中报修+待审批访客）、近 12 个月电费趋势 |
| `GET /api/dashboard/buildings` | dormmanager（限管辖） | 各楼栋入住率，状态分级（≥100% 已满、≥80% 紧张、否则正常） |
| `GET /api/dashboard/dorm` | 学生 | 宿舍详情——床位/房间/楼栋、室友列表、设施故障（按报修类型关键词推断）、卫生分/排名 |

> 设施故障按报修类型关键词「灯/电/空调/网/水」推断 `lightIssue`/`acIssue`/`netIssue`/`waterIssue`。

###### `AdminReportController`

**文件**：[AdminReportController.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/controller/AdminReportController.java)

`GET /api/admin/report/summary`（admin only，Controller 内二次校验角色）：

- 全局统计：楼栋数、房间数、总床位、已入住、空床位、入住率、报修总数、已完成报修、报修完成率。
- 分楼栋明细：用 `listByIds` 批量查房间和床位，按楼栋聚合。
- `rate(numerator, denominator)` 辅助算百分比。

##### 5.7.5 AI 集成

**文件**：[AiController.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/controller/AiController.java)

`GET /api/ai/chat?question=xxx`：**纯本地规则匹配**，未接入任何大模型。

- 关键词路由：门禁/晚归、调宿/换宿舍、报修/维修/坏了、水电费/费用/账单、访客/来访、遥控器/钥匙、用电/电器。
- 命中返回预设话术，未命中返回兜底提示。
- 无状态、无上下文记忆。

##### 5.7.6 用户管理

**文件**：[UserController.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/controller/UserController.java)

| 端点 | 说明 |
|---|---|
| `GET /api/user/list` | dormmanager 只看管辖范围内的学生（`managedStudentIds`），admin 全看；填充学生 `className`/`major` |
| `GET /api/user/unassigned` | 查未分配床位的学生（按 `bed.student_id` 反排除），支持按性别过滤 |
| `POST /api/user/save` | 见下方详细说明 |
| `DELETE /api/user/{id}` | 仅 admin |

**`/api/user/save` 规则**：

- 非 admin 只能改自己的 `name`/`avatar`/`className`/`email`/`phone`（从 existing 实例覆盖，防止越权改角色/密码）。
- 密码处理：新建无密码 → 默认 `encode("123456")`；已有且密码空 → 不改；明文 → 加密。
- 保存后按角色同步扩展表：student → `student_info`（className/major），dormmanager → `manager_info`（仅建空记录），admin → `admin_info`。

---

### 5.8 控制器层（controller）

**位置**：`backend/src/main/java/com/dorm/backend/controller/`

19 个 REST 控制器，统一返回 `Result<T>`。按业务模块分组：

#### 认证与用户

| 控制器 | 基础路径 | 职责 |
|---|---|---|
| `AuthController` | `/api/auth` | 登录 |
| `UserController` | `/api/user` | 用户/学生管理（list/unassigned/save/delete） |

#### 核心业务（资源管理）

| 控制器 | 基础路径 | 职责 |
|---|---|---|
| `BuildingController` | `/api/building` | 楼栋 CRUD + 统计 |
| `RoomController` | `/api/room` | 房间 CRUD + 批量建房间 + 自动建床位 |
| `BedController` | `/api/bed` | 床位分配/换床/退宿 + 住宿历史联动 |
| `StayHistoryController` | `/api/stayHistory` | 住宿历史查询 |
| `TransferRequestController` | `/api/transferRequest` | 调宿申请 + 审批闭环 |

#### 服务运营

| 控制器 | 基础路径 | 职责 |
|---|---|---|
| `RepairRequestController` | `/api/repairRequest` | 报修申请 + 状态推进 |
| `VisitorRecordController` | `/api/visitorRecord` | 访客登记/审批 |
| `HygieneRecordController` | `/api/hygieneRecord` | 卫生检查评分 |
| `FeeBillController` | `/api/feeBill` | 水电费账单 |
| `LateReturnRecordController` | `/api/lateReturnRecord` | 晚归登记 |
| `ItemRecordController` | `/api/itemRecord` | 物品出入登记 |
| `BusinessRecordController` | `/api/businessRecord` | 通用业务记录（公告/消息/备忘/反馈） |
| `CallRecordController` | `/api/callRecord` | 通话预约 |
| `ChatController` | `/api/chat` | 室友聊天 |

#### 管理仪表盘

| 控制器 | 基础路径 | 职责 |
|---|---|---|
| `DashboardController` | `/api/dashboard` | 三角色仪表盘聚合 |
| `AdminReportController` | `/api/admin/report` | 管理员报表汇总 |
| `OperationLogController` | `/api/operationLog` | 操作日志查询（admin 只读） |

#### 其他

| 控制器 | 基础路径 | 职责 |
|---|---|---|
| `AiController` | `/api/ai` | 关键词规则问答（非真正 LLM） |

---

### 5.9 数据库设计

**来源**：[schema.sql](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/resources/schema.sql)（20 张表 + 初始化数据） + [schema-business-record.sql](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/resources/schema-business-record.sql)（历史遗留单表）。

#### 表清单与关键字段

| # | 表名 | 用途 | 关键列 / 关系 |
|---|---|---|---|
| 1 | `sys_user` | 用户主表 | id, username(唯一), password(BCrypt), role(student/dormmanager/admin), name, avatar, class_name, email, phone, enabled |
| 2 | `building` | 楼栋 | id, name, type(男生楼/女生楼), floors, manager(负责人姓名字符串), location, active, total_rooms/occupied_rooms/free_rooms(冗余统计，实体标记 `@TableField(exist=false)`) |
| 3 | `room` | 房间 | id, building_id(FK→building), room_number, floor, capacity(默认4), status(NORMAL/FULL/MAINTENANCE) |
| 4 | `bed` | 床位 | id, room_id(FK→room), bed_number, status(EMPTY/OCCUPIED/BROKEN), student_id(FK→sys_user, 可空) |
| 5 | `repair_request` | 报修 | id, submitter_id(FK→sys_user), room_id(FK→room), type, description, images, status(PENDING/PROCESSING/COMPLETED), handler_id |
| 6 | `visitor_record` | 访客 | id, student_id, visitor_name, phone, relation, visit_time, leave_time, status(PENDING/APPROVED/LEFT) |
| 7 | `transfer_request` | 调宿申请 | id, student_id, current_bed_id(FK→bed), target_room_id(FK→room, 可空), reason, status(PENDING/APPROVED/REJECTED) |
| 8 | `fee_bill` | 水电费账单 | id, room_id, type(WATER/ELECTRICITY), amount(decimal), month(如 2026-01), status(UNPAID/PAID) |
| 9 | `hygiene_record` | 卫生检查 | id, room_id, inspector_id, score(0-100), comment, check_date |
| 10 | `call_record` | 通话预约 | id, student_id, topic, target_person, status(PENDING/ACCEPTED/FINISHED) |
| 11 | `feedback` | 意见反馈 | id, student_id, type, content, reply, status(UNREAD/REPLIED) |
| 12 | `operation_log` | 操作日志 | id, operator_id, operator_name, module, action, path, result, summary, create_time |
| 13 | `business_record` | 通用业务记录 | id, type, title, owner, description, status, creator_id, event_time — 通用表，承载公告/消息/备忘/反馈等多类型 |
| 14 | `item_record` | 物品出入登记 | id, title, owner, description, status(PENDING/RELEASED/RETURNED), creator_id |
| 15 | `late_return_record` | 晚归登记 | id, student_id, student_name, room_number, reason, status(PENDING/CONFIRMED), return_time |
| 16 | `chat_message` | 聊天消息 | id, sender_id, receiver_id(私聊), room_id(群聊), type(PRIVATE/GROUP), content |
| 17 | `stay_history` | 住宿历史 | id, student_id, bed_id, check_in_date, check_out_date(可空,空表示在住) |
| 18 | `student_info` | 学生扩展 | id, user_id(唯一FK), class_name, major, enrollment_year |
| 19 | `manager_info` | 宿管扩展 | id, user_id(唯一FK), employee_no, building_id(管辖楼栋) |
| 20 | `admin_info` | 管理员扩展 | id, user_id(唯一FK), department |

#### 关键关系与设计要点

- **无外键约束**：所有表用 `KEY idx_xxx` 索引，但无 `FOREIGN KEY` 约束，关系靠应用层维护。
- **三层用户模型**：`sys_user` 主表 + 三个角色扩展表（`student_info`/`manager_info`/`admin_info`），按 `user_id` 一一对应。
- **宿管-楼栋绑定**：`manager_info.building_id` 是宿管管辖范围的权威来源；`building.manager`（姓名字符串）是历史遗留的回退匹配字段。
- **床位即占用关系**：`bed.student_id` 直接表示当前入住学生，是系统最核心的占用状态字段。
- **通用业务表设计**：`business_record` 是多用途表，通过 `type` 字段区分公告/物品/晚归等，被 `BusinessRecordController` 统一管理。
- **冗余统计字段**：`building` 表的 `total_rooms`/`occupied_rooms`/`free_rooms` 由业务逻辑维护（实体标记为 `@TableField(exist=false)` 虚拟字段，实际由 `BuildingServiceImpl.getBuildingsWithStats` 动态计算填充）。
- **初始化数据**：3 个测试账号（`20240001`/学生、`manager1`/宿管、`admin`/管理员），密码均为 BCrypt 哈希，明文为 `123456`；3 条系统公告。

---

## 6. 前端模块详解

### 6.1 应用入口

#### `frontend/src/main.js`

**文件**：[main.js](file:///f:/bishe/Antigravity/Dorm-Sys/frontend/src/main.js)

- 顶部有一段 Edge 浏览器 `history.replaceState` 的兼容补丁：当页面处于隐藏状态时跳过路由替换，规避 Edge 最小化时的 bug。
- 创建 Vue 应用，依次 `app.use(pinia)`（已注册 `piniaPluginPersistedstate` 插件）和 `app.use(router)`。
- 全局引入 Element Plus 基础样式、Element Plus 暗色变量 CSS、本地 `style.css` 与 `theme.css`。
- **Element Plus 组件按需引入**（配合 `unplugin-vue-components`），`main.js` 中没有 `app.use(ElementPlus)`。

#### `frontend/src/App.vue`

**文件**：[App.vue](file:///f:/bishe/Antigravity/Dorm-Sys/frontend/src/App.vue)

- 通过 `useRoute().path === '/login'` 计算 `isLoginRoute`：
  - 登录页：纯 `<router-view />` 全屏布局。
  - 其它页：`el-container` 外壳 = `<Sidebar />` + (`<Topbar />` + `<el-main><router-view /></el-main>`)。
- 外壳样式 `100vh / 100vw + overflow:hidden`，主区域 `padding:24px`、背景色 `var(--bg)`、可纵向滚动。

---

### 6.2 状态管理（store）

#### `frontend/src/store/app.js`（应用级状态）

**文件**：[app.js](file:///f:/bishe/Antigravity/Dorm-Sys/frontend/src/store/app.js)

- 不是 Pinia store，而是一个 `reactive({ role, theme, sidebarCollapsed })` 单例 + `watch`。
- `theme` 初值来自 `localStorage.getItem("dorm-theme")`，watch 回调同步写回 localStorage、设置 `document.documentElement.dataset.theme`、并在 `dark` 时给 `<html>` 加 `.dark` 类（用于 Element Plus 暗色变量）。
- `sidebarCollapsed` 控制侧边栏宽度（64px ↔ 240px）。
- `role` 字段在此处实际未被业务使用（真实角色在 `userStore`），属于历史遗留。

#### `frontend/src/store/user.js`（用户态）

**文件**：[user.js](file:///f:/bishe/Antigravity/Dorm-Sys/frontend/src/store/user.js)

- Pinia composition store，名为 `user`，开启 `persist: true`（自动序列化到 localStorage）。
- 状态：`token`、`role`（`'student' | 'dormmanager' | 'admin'`，默认 `'student'`）、`userInfo`（对象）。
- 方法：`setToken` / `setRole` / `setUserInfo` / `logout`（logout 同时清空三个字段）。

---

### 6.3 路由系统

**文件**：[router/index.js](file:///f:/bishe/Antigravity/Dorm-Sys/frontend/src/router/index.js)

- `createWebHistory()` 模式，所有路由组件均通过 `() => import(...)` 懒加载。
- 路由表分四组：根重定向 `/ → /login`、登录 `/login`、学生路由（11 条，前缀 `/student/`）、宿管路由（14 条，前缀 `/dormmanager/`）、管理员路由（9 条，前缀 `/admin/`）、`/:pathMatch(.*)*` 404 兜底重定向回根。
- `homeByRole` 映射：`student → /student/desk`、`dormmanager → /dormmanager/workbench`、`admin → /admin/overview`。

`router.beforeEach` 守卫做三件事：

1. 已登录访问 `/login` 时直接跳到该角色首页。
2. 无 token 访问任何非登录页 → 重定向到 `/login?redirect=...`。
3. 拦截跨角色访问：从路径首段解析 `targetRole`，若不属于当前用户角色则跳回本人首页。

> 没有使用 `meta` 字段做权限元数据，所有鉴权逻辑全部基于路径前缀 + `userStore.role`。

---

### 6.4 HTTP 请求层

**文件**：[request.js](file:///f:/bishe/Antigravity/Dorm-Sys/frontend/src/utils/request.js)

- **Axios 实例**：`baseURL: '/api'`（依赖 Vite 代理转发到后端 `http://127.0.0.1:8088`），`timeout: 5000`ms。
- **请求拦截器**：每次请求从 `useUserStore()` 取 `token`，若存在则写入 `config.headers['Authorization'] = 'Bearer ${token}'`。
- **响应拦截器（成功分支）**：
  - 后端有两种返回格式：无 `code` 包装的直接数据 → 原样 `return res`；
  - 有 `code` 包装：`code !== 200` 时弹 `ElMessage.error` 并 `reject`，`code === 200` 时只 `return res.data`（剥掉外层信封）。
- **响应拦截器（错误分支）**：按 HTTP 状态码分支：
  - `401`：消息"登录状态过期，请重新登录"。使用模块级 `handlingUnauthorized` 标志位 + `userStore.token` 判空，避免并发请求重复弹窗；调用 `userStore.logout()`、`router.replace('/login')`，1.5 秒后释放锁。
  - `403/404/500` 及默认：分别提示"没有权限 / 接口不存在 / 后端服务器异常 / 网络异常 (xxx)"。
  - `error.message` 含 `timeout` → "请求超时，请检查网络"。
  - 所有错误统一 `ElMessage({ grouping: true })`（grouping 避免重复堆叠）。

> 这是一个非常标准的「中央邮局」模式，所有 API 模块都 `import request from '../utils/request'`。

---

### 6.5 API 模块

**位置**：`frontend/src/api/`

所有模块遵循统一约定：`request({ url, method, params|data })`，CRUD 命名 `getXxx / saveXxx / deleteXxx`。完整清单：

| 文件 | 后端基础路径 | 导出函数 | 用途 |
|---|---|---|---|
| `auth.js` | `/auth` | `loginApi(username, password, role)` → POST `/auth/login` | 登录 |
| `user.js` | `/user` | `getUserList()`, `getUnassignedStudents(gender)`, `getUsers`(别名), `saveUser(data)`, `deleteUser(id)` | 用户/学生管理 |
| `room.js` | `/room` + `/bed` | `getRooms(buildingId)`, `saveRoom(data)`, `batchCreateRooms(data)`, `deleteRoom(id)`, `getBeds(roomId)`, `saveBed(data)`, `deleteBed(id)` | 房间与床位 |
| `building.js` | `/building` | `getBuildings()`, `getBuildingList`(别名), `saveBuilding(data)`, `deleteBuilding(id)` | 楼栋 |
| `businessRecord.js` | `/businessRecord` | `getBusinessRecords(type, status)`, `saveBusinessRecord(data)`, `deleteBusinessRecord(id)` | **通用业务记录**（公告/反馈/备忘/消息，靠 `type` 区分） |
| `call.js` | `/callRecord` | `getCalls(studentId)`, `saveCall(data)`, `deleteCall(id)` | 通话预约 |
| `dashboard.js` | `/dashboard` | `getStats()`, `getBuildingStats()`, `getAlerts()` | 仪表盘 |
| `fee.js` | `/feeBill` | `getFeeBills(roomId, status)`, `saveFeeBill(data)`, `deleteFeeBill(id)` | 水电费账单 |
| `hygiene.js` | `/hygieneRecord` | `getHygieneRecords(roomId)`, `saveHygieneRecord(data)`, `deleteHygieneRecord(id)` | 卫生检查记录 |
| `item.js` | `/itemRecord` | `getItems()`, `saveItem(data)`, `deleteItem(id)` | 物品出入 |
| `lateReturn.js` | `/lateReturnRecord` | `getLateReturns()`, `saveLateReturn(data)`, `deleteLateReturn(id)` | 晚归登记 |
| `operationLog.js` | `/operationLog` | `getOperationLogs(params)` GET `/operationLog/list` | 操作日志（只读） |
| `repair.js` | `/repairRequest` | `getRepairs(submitterId, status)`, `saveRepair(data)`, `deleteRepair(id)` | 报修 |
| `transfer.js` | `/transferRequest` | `getTransfers(studentId, status)`, `saveTransfer(data)`, `deleteTransfer(id)` | 调宿申请 |
| `visitor.js` | `/visitorRecord` | `getVisitorRecords(studentId)`, `saveVisitorRecord(data)`, `deleteVisitorRecord(id)` | 访客 |
| `adminReport.js` | `/admin/report` | `getAdminReport()` GET `/admin/report/summary` | 管理员报表汇总（只读） |

#### 4 个代表性模块详解

1. **`room.js`** — 唯一同时管理两类资源（房间 + 床位）的模块。`batchCreateRooms` 调用专用端点 `POST /room/batch`，被 `admin/Rooms.vue` 的「批量创建」对话框使用（playwright e2e 测试覆盖了该表单）。`getBeds(roomId)` 用于 `manager/Workbench.vue` 的房间床铺平面图弹窗。

2. **`repair.js`** — 函数签名 `getRepairs(submitterId, status)` 体现「角色数据范围」：学生端调用时传 `submitterId = 当前用户 id` 只看自己的报修；宿管/管理员端不传 `submitterId` 则查全部。`saveRepair` 同时承担「创建」和「状态推进」两种语义（同 `businessRecord` 模式）。

3. **`businessRecord.js`** — 这是全系统的「万能记录表」。同一个表 + 同一组 API 通过 `type` 字段区分用途，已被使用的 type 包括：`admin_notice`（管理员公告）、`manager_messages`（宿管通知）、`manager_memos`（宿管备忘录）、`feedback`（学生意见反馈）。`LocalFeaturePage.vue`、`manager/Memos.vue`、`student/Feedback.vue`、`manager/Messages.vue`、`admin/Notices.vue` 都依赖它。

4. **`dashboard.js`** — 三个只读端点。学生端额外直接调用未封装的 `/dashboard/student` 和 `/dashboard/dorm`（在 `Desk.vue` 内联调用 `request`）。

> **模式总结**：除 `auth`、`dashboard`、`adminReport`、`operationLog` 外，其余 12 个模块都是「list / save / delete」三件套，命名一致、参数风格一致，是后端「一表一控制器」映射的直接体现。

---

### 6.6 布局与共享组件

#### `frontend/src/components/layout/Sidebar.vue`

**文件**：[Sidebar.vue](file:///f:/bishe/Antigravity/Dorm-Sys/frontend/src/components/layout/Sidebar.vue)

- 渲染 `el-aside`，宽度由 `appStore.sidebarCollapsed` 决定（240px / 64px）。
- 顶部 logo（`Building2` 图标 + 「学生宿舍管理系统」，折叠时隐藏文字）。
- `el-menu` 启用 `router` 模式，`default-active` 绑定 `route.path` 实现高亮。
- **核心数据**：`navMap` 对象按角色键控菜单数组：
  - `admin`：7 项 + 1 个子菜单（「宿舍资源」展开为「楼栋管理 / 房间管理」）。
  - `dormmanager`：14 项平铺菜单。
  - `student`：11 项平铺菜单。
- 图标全部来自 `@lucide/vue`。
- `navItems` 是 `computed`，随 `userStore.role` 切换菜单集。

#### `frontend/src/components/layout/Topbar.vue`（约 690 行，最复杂的布局组件）

**文件**：[Topbar.vue](file:///f:/bishe/Antigravity/Dorm-Sys/frontend/src/components/layout/Topbar.vue)

- 左侧：侧边栏折叠按钮（`MenuIcon`）+ `el-breadcrumb` 面包屑（「角色组名 / 当前页名」）。
- `groupName` 映射角色 → 「系统管理 / 宿管服务 / 学生服务」；`pageNameMap` 是路径→中文名的字典（约 30 条）。
- 右侧按钮组：
  - **刷新**：`RefreshCw` 图标，点击后 `ElLoading.service` 锁屏 450ms 再 `window.location.reload()`。
  - **主题切换**：`Sun / Moon` 图标，切换 `appStore.theme`。
  - **消息通知**：`el-popover` + `el-badge`（未读数），点击展开通知面板。
  - **用户头像下拉**：`el-dropdown`，仅一项「退出登录」。
- **通知系统**（核心特色）：
  - `fetchNotifications()` 按 `userStore.role` 走两条完全不同的聚合路径：
    - **宿管**：`Promise.all` 并发拉取通话/反馈/报修/调宿/访客 5 类 `PENDING` 记录，前端聚合为通知项，最多 30 条按时间倒序。
    - **学生**：并发拉取公告、`/chat/notifications`、反馈回复、通话状态、报修进度、调宿结果、访客审批 7 类记录。
  - 每 8 秒轮询一次（`setInterval(fetchNotifications, 8000)`）。
  - 已读状态用 `localStorage` 持久化，key 为 `dorm-notifications-read:${accountId}`，最多保留 300 条。
  - `watch([role, userId, token])` 重启轮询；`onBeforeUnmount` 清理定时器。
- 退出登录：`userStore.logout()` + `router.push('/login')` + `ElMessage.info`。

#### `frontend/src/components/ui/Hero.vue`

**文件**：[Hero.vue](file:///f:/bishe/Antigravity/Dorm-Sys/frontend/src/components/ui/Hero.vue)

- 极简展示组件，props：`title / subtitle / actionLabel / actionIcon`，emit：`action`。
- 渲染一个带「玻璃球」装饰背景的横幅，含主标题、副标题、可选 CTA 按钮。

#### `frontend/src/components/ui/ServiceCard.vue`

**文件**：[ServiceCard.vue](file:///f:/bishe/Antigravity/Dorm-Sys/frontend/src/components/ui/ServiceCard.vue)

- 列表项卡片，props：`title / value / actionLabel / icon`，emit：`action`。
- 左图标 + 中间标题/数值 + 右侧 `ChevronRight` 跳转提示，点击整卡触发 `action`。

#### `frontend/src/components/ChatDrawer.vue`（约 870 行，宿舍通讯抽屉）

**文件**：[ChatDrawer.vue](file:///f:/bishe/Antigravity/Dorm-Sys/frontend/src/components/ChatDrawer.vue)

- 用 `el-dialog` 实现的聊天窗口，宽度 `min(980px, 100vw-64px)`，玻璃拟态（`backdrop-filter: blur(34px) saturate(138%)`）。
- props：`modelValue`（v-model 控制显隐）、`targetUser`（私聊对象）、`roomId`（群聊房间）、`myId`、`roomMemberCount`。
- **双模式**：
  - `mode='group'`：拉 `GET /chat/group/${roomId}` 群消息。
  - `mode='private'`：拉 `GET /chat/private/${targetUser.id}` 私聊消息。
  - 会话列表来自 `GET /chat/conversations`。
- 发消息 `POST /chat/send`，payload 含 `content / type (PRIVATE|GROUP) / receiverId / roomId`。
- 每 3 秒轮询消息和会话列表；消息按日期分隔显示。
- 自身消息靠右蓝色气泡，他人靠左白色气泡，自动滚到底部。
- 响应式：≤720px 改为纵向布局；尊重 `prefers-reduced-motion`。

#### `frontend/src/components/WeatherWidget.vue`（约 290 行，天气组件）

**文件**：[WeatherWidget.vue](file:///f:/bishe/Antigravity/Dorm-Sys/frontend/src/components/WeatherWidget.vue)

- 紧凑触发条：emoji + 温度 + 城市名，点击用 `Teleport to="body"` 弹出 1100px 宽的横向天气卡片。
- 数据源是 **Open-Meteo** 公共 API（不经过自家后端）：
  - 实时 + 7 天日报：`https://api.open-meteo.com/v1/forecast?...current_weather=true&daily=...`
  - 城市搜索：`https://geocoding-api.open-meteo.com/v1/search?name=...&language=zh`
- 三层布局：当前位置 + 当前天气 / 24 小时折线图（纯 SVG 手绘，非图表库）/ 7 天预报横向条。
- `weathercode` 到 emoji 的映射内置（0=晴、1-3=多云、45-48=雾、51-67=雨、71-77=雪、95+=雷暴）。
- 城市选择持久化到 `localStorage['dorm_sys_weather_location']`。
- 在 `student/Desk.vue`、`admin/Overview.vue`、`manager/Workbench.vue` 三个工作台首页都嵌入了此组件。

#### `frontend/src/components/LocalFeaturePage.vue`（约 240 行，通用业务记录页模板）

**文件**：[LocalFeaturePage.vue](file:///f:/bishe/Antigravity/Dorm-Sys/frontend/src/components/LocalFeaturePage.vue)

- 这是一个**配置驱动的页面模板**，被需要「列表 + 新建对话框 + 状态推进 + 侧边统计」的页面复用。
- props（13 个）：`storageKey`、`recordType`（业务类型，传给 `getBusinessRecords(type)`）、`title`、`subtitle`、`listTitle`、`actionText`、`icon`、`defaultRecords`、`statuses`（状态数组，默认 `['待处理','处理中','已完成']`）、`tips`、`manageRecords`、各占位符。
- 内部直接调用 `getBusinessRecords / saveBusinessRecord / deleteBusinessRecord` 三个 API。
- 功能：搜索关键词过滤、状态筛选、新建对话框、`advance(record)` 把状态向前推一格、`countByStatus` 侧边统计、`statusType` 根据 statuses 索引返回 warning/primary/success tag 颜色。
- 右侧「流程提示」和「状态统计」两个 side card。被 `manager/Items.vue`、`manager/Memos.vue` 等复用。

#### 组件协作关系

`Sidebar` + `Topbar` 构成 `App.vue` 的外壳骨架；`Hero` / `ServiceCard` 是工作台首页的展示原子；`WeatherWidget` 是跨角色首页共享的「装饰 + 实用」组件；`LocalFeaturePage` 是宿管端多个 CRUD 页面的模板基类；`ChatDrawer` 是 `student/Dorm.vue` 通过 `openGroupChat` 触发的宿舍内通讯弹层。

---

### 6.7 视图层（views）

#### `frontend/src/views/Login.vue`（登录页，约 670 行）

**文件**：[Login.vue](file:///f:/bishe/Antigravity/Dorm-Sys/frontend/src/views/Login.vue)

- **左右双栏布局**：左 `campus-panel`（背景图 `login-campus.jpg` + 半透明深色遮罩 + 品牌标语 + 3 条服务亮点），右 `login-surface`（玻璃拟态登录卡片）。
- 三角色按钮组（学生 / 宿管 / 管理员），切换时自动填入演示账号（`20240001 / manager1 / admin`）— 这段逻辑被 playwright e2e 测试直接利用。
- 表单：用户名 / 密码（带眼睛图标切换显隐）/ 记住账号 / 忘记密码（仅 `ElMessage.info` 提示联系宿管）。
- 字段级校验（`validateField`），错误信息红色显示在输入框下，提交前 `validateForm`。
- 登录流程：`loginApi(username, password, role)` → 写入 `userStore.token/role/userInfo` → 成功 toast → 按角色 `router.push` 到对应首页。
- 响应式：≤860px 隐藏左侧品牌栏，显示顶部 `mobile-brand`；≤430px 隐藏角色按钮图标。

#### 学生端 `frontend/src/views/student/`（11 个 .vue）

| 文件 | 一句话用途 |
|---|---|
| `Desk.vue` | 学生首页服务台，含问候语、天气、4 张服务卡（宿舍/欠费/报修/卫生）、待办、12 个月电费柱状图、公告、室友、快捷入口 |
| `Dorm.vue` | 「我的宿舍」详情：楼栋/房间/床位信息卡 + 室友列表 + 宿舍群聊入口（打开 `ChatDrawer`） + 卫生历史 |
| `RepairApply.vue` | 报修申请提交与个人报修进度跟踪 |
| `Fees.vue` | 个人水电费账单查询与缴费状态 |
| `Notice.vue` | 校园公告列表与详情查看 |
| `Visitor.vue` | 访客预约登记与本人访客记录 |
| `Transfer.vue` | 调宿申请提交与审批结果跟踪 |
| `AI.vue` | 宿舍自助问答（调 `/ai/chat`）+ 侧边「智慧锦囊」和「热门咨询」 |
| `Call.vue` | 智能通话预约（学生发起通话申请） |
| `Feedback.vue` | 意见反馈提交与回复查看 |
| `Settings.vue` | 账户设置（个人信息修改） |

#### 宿管端 `frontend/src/views/manager/`（14 个 .vue）

| 文件 | 一句话用途 |
|---|---|
| `Workbench.vue` | 宿管工作台首页：4 张统计卡 + 楼栋房间网格 + 房间床铺平面图弹窗 + 备忘录 + 系统通知 + 快捷工具 |
| `Checkin.vue` | 入住办理：选未入住学生→选楼栋→选房间→选空床位，含换床/退床流程 |
| `Fee.vue` | 水电计费中心，管理账单 |
| `Repair.vue` | 报修处理（接单/完成） |
| `Visitor.vue` | 访客管理（审批/登记进出） |
| `Hygiene.vue` | 卫生检查评分录入 |
| `Transfer.vue` | 调宿审批 |
| `LateReturn.vue` | 晚归记录管理 |
| `Items.vue` | 物品出入登记（基于 `LocalFeaturePage`） |
| `Messages.vue` | 消息通知发布（type=`manager_messages`） |
| `Memos.vue` | 宿管备忘录（type=`manager_memos`） |
| `Call.vue` | 智能通话管理（处理学生通话申请） |
| `Feedback.vue` | 意见反馈处理（type=`feedback` 回复） |
| `Profile.vue` | 个人中心 |

#### 管理员端 `frontend/src/views/admin/`（8 个 .vue）

| 文件 | 一句话用途 |
|---|---|
| `Overview.vue` | 管理看板：系统统计 + 楼栋入住分布（网格/列表双视图 + 分页）+ 预警 + 资源监控 |
| `Users.vue` | 用户账户管理（同时服务 `/admin/users/list` 和 `/admin/users/roles` 两个路由） |
| `Resources.vue` | 楼栋资源管理（含运营状态开关，e2e 测试覆盖） |
| `Rooms.vue` | 房间管理（含批量创建表单，e2e 测试覆盖） |
| `Repairs.vue` | 全校报修监控 |
| `Notices.vue` | 公告管理（type=`admin_notice` 发布） |
| `OperationLogs.vue` | 操作日志查询（按模块/结果/关键词筛选，只读） |
| `Reports.vue` | 数据报表（4 个指标卡 + 入住率/报修完成率进度条 + 楼栋对比表 + **CSV 导出**） |

#### 重点视图数据流

**1. `student/Desk.vue`（学生服务台）**

- 三路并发数据：`request('/dashboard/student')` 拿待办/电费历史/卫生分；`request('/dashboard/dorm')` 拿床铺+室友；`getBusinessRecords('admin_notice'|'manager_messages', '已发布')` 拿公告。
- 公告已读状态用 `localStorage['student-notices-read:${userId}']` 持久化，点击公告卡弹 `el-dialog` 显示详情并标记已读。
- 电费柱状图是纯 CSS 实现（`chart-bar-fill` 高度按 `amount/max*72` 计算），加骨架屏占位。
- Hero 区右侧内嵌 `WeatherWidget`。

**2. `admin/Overview.vue`（管理员看板）**

- `Promise.all([getStats(), getBuildingStats(), getAlerts()])` 一次性拉数据。
- 楼栋入住分布有 `viewMode`（grid/list）双视图，`pagedBuildings` computed 做前端分页（pageSize=4）。
- `percentage` 后端没返回时前端兜底计算 `Math.round(occupiedBeds/totalBeds*100)`，并 `Math.min(100, Math.max(0, ...))` 钳制。
- 状态自动推断：`>=100` 已满、`>=80` 紧张、否则正常。
- 失败时显示 `el-alert` + 「重新加载」按钮（不抛 toast）。

**3. `manager/Workbench.vue`（宿管工作台）**

- 最复杂的视图，`loadData()` 并发 5 个请求 + 串行 2 个：`getRepairs / getVisitorRecords / getBuildings / /dashboard/buildings / getBusinessRecords('admin_notice')`，再 `loadMemos()`，再 `fetchRoomsForBuilding(firstBuildingId)`，再请求 `/lateReturnRecord/list`。
- 4 张统计卡（在住学子/待办报修/今日访客/近日晚归）都可点击跳转，带键盘可访问性（`tabindex=0` + Enter/Space 处理）。
- 楼栋房间网格用「圆点表示床位占用」的可视化（`room-dots` + `dot.filled`），点击房间弹**床铺平面图** dialog（用绝对定位画大门/阳台/4 个床槽）。
- 备忘录用 `saveBusinessRecord({type:'manager_memos', ...})` 实现 CRUD，按 `PENDING` 排前 `DONE` 排后。
- `watch(selectedBuilding)` 自动重新拉房间列表。

**4. `student/AI.vue`（宿舍自助问答）**

- 关键澄清：**不是真正的 LLM**。前端内置一份 `rules` 数组（4 条规则：门禁/调宿/报修/水电费），同时调用 `GET /ai/chat?question=...` 让后端做规则匹配。
- UI 是经典聊天界面：消息列表 + 输入栏 + 空状态显示 `prompts`（4 个常见问题按钮）。
- 右侧侧栏「智慧锦囊」硬编码 4 条核心规定（门禁 23:30、禁 800W+ 电器、访客 22:00 前离开、周四卫生检查），点击「报修小贴士/用电安全指南」等价于 `ask(...)` 直接发问。
- 底部 disclaimer：「当前为本地规则库问答；紧急故障、安全问题或未匹配事项请联系宿管。」

**5. `admin/Reports.vue`（CSV 导出）**

- 全文件压成 2 行模板 + 1 行脚本（极简写法），但功能完整：4 个指标卡 + 入住率/报修完成率 `el-progress` + 楼栋对比 `el-table`。
- **CSV 导出纯前端实现**：构造二维数组 → `Blob` + `URL.createObjectURL` → 动态 `<a download>` 点击 → `revokeObjectURL`。文件名带日期 `宿舍运行报表-YYYY-MM-DD.csv`，BOM `\uFEFF` 保证 Excel 中文不乱码。

---

### 6.8 样式系统

#### `frontend/src/assets/css/style.css`（约 2270 行，全局设计系统）

**文件**：[style.css](file:///f:/bishe/Antigravity/Dorm-Sys/frontend/src/assets/css/style.css)

**色彩系统（CSS 变量）**：

- 中性色阶：`--bg #f8fafc` / `--surface #fff` / `--muted #f1f5f9` / `--line #e2e8f0` / `--text #0f172a` / `--text-secondary #475569` / `--sub #64748b`（典型 Tailwind slate 调色板）。
- 11 组语义色，每组含主色 + 浅底（`-2` 后缀）：`--primary #2563eb`、`--purple`、`--pink`、`--teal`、`--orange`、`--warn #f59e0b`、`--danger #ef4444`、`--ok #22c55e`、`--info #06b6d4`、`--indigo`。
- 阴影四级：`--shadow-sm / --shadow / --shadow-lg / --shadow-xl`。
- 圆角四级：`--radius-sm 6px / --radius 8px / --radius-lg 12px / --radius-xl 16px`。
- 过渡三级：`--transition-fast .15s / --transition .2s / --transition-slow .3s`。

**暗色主题**（`:root[data-theme="dark"]`）：

- 切换为深 slate 色阶：`--bg #020617` / `--surface #0f172a` / `--line #1e293b` / `--text #f8fafc`。
- 浅底色（`-2`）改为 `rgba(color, 0.15)` 透明形式。
- 阴影改为 `0 0 0 1px rgba(255,255,255,0.06)` 描边 + 深色投影。
- 同时覆盖 Element Plus 暗色变量：`--el-bg-color / --el-border-color / --el-text-color-primary / --el-mask-color` 等。

**布局约定**：

- `.app` 用 CSS Grid：`220px minmax(0,1fr)` 双列 + `64px / 1fr / 24px` 三行（topbar / main / footer），折叠时改 `72px`。
- `.grid` 内容栅格：`minmax(0, 2fr) 404px`（主内容 + 右侧栏）。
- `.stats` 四列等宽，`.split-2 / .service-cards / .wallets` 两列。
- `.hero` 横幅、`.card` 卡片、`.row` 列表行、`.tag` 圆角标签、`.ghost-btn` 描边按钮等是全局原子类。

**动效系统**：

- 9 个 keyframes：`riseIn / fadeIn / slideInLeft / slideInRight / slideInUp / scaleIn / pulse / shimmer / bounce / spin / slideDown`。
- 卡片、stat、service-card、side-stat 都有错峰 `animation-delay`（0.05s 步进）实现「瀑布入场」。
- hover 效果丰富：卡片 `translateY(-1px)` + 加深阴影、行 `translateX(4px)`、按钮 `scale` 等。
- 完整支持 `prefers-reduced-motion: reduce`：所有动画降到 0.01ms。

**响应式**：≤1180px 内容栏变单列、stats 变 2 列；≤760px 重构为单列纵向布局，侧边栏变横向滚动 nav。

#### `frontend/src/assets/css/theme.css`（约 67 行，Element Plus 主题桥接）

**文件**：[theme.css](file:///f:/bishe/Antigravity/Dorm-Sys/frontend/src/assets/css/theme.css)

- 把 Element Plus 主色覆写为 `--el-color-primary: #2563eb`（与本地 `--primary` 对齐），并定义 9 级 light 阶梯。
- 圆角统一 `--el-border-radius-base: 8px`。
- `body` 背景改为 `#f4f7f9`（浅灰蓝）。
- `.el-card` 去边框、圆角 12px、轻投影。
- 暗色模式覆写 `--el-color-primary: #3b82f6` 等，body 改 `#020617`。

> **设计系统总结**：采用 Tailwind slate + 自定义语义色的双轨制；通过 `data-theme` 属性切换浅/暗主题，CSS 变量同时驱动本地组件和 Element Plus；动效充沛但尊重无障碍偏好；移动端有完整断点降级。

---

## 7. 关键业务流程

### 7.1 登录流程

```
浏览器 Login.vue
  ├─ 用户选角色 → 自动填演示账号（20240001 / manager1 / admin）
  ├─ 表单校验 → loginApi(username, password, role)
  └─ POST /api/auth/login
       ├─ QueryWrapper 按 username + role 查 sys_user
       ├─ passwordService.matches() 校验密码
       ├─ enabled==false → 403
       ├─ 懒加密：旧明文密码 → encode → 回写 DB
       ├─ jwtUtils.generateToken(userId, username, role)  // 24h 过期
       └─ 返回 {token, user}
  ↓
  userStore.setToken/Role/UserInfo  // persist 落 localStorage
  ↓
  router.push(homeByRole[role])
```

### 7.2 床位分配（入住办理）

```
宿管 Checkin.vue → saveBed(data)
  ↓
POST /api/bed/save（BedController）
  ├─ 三重权限校验：原房间 / 学生所在房间 / 目标房间都在宿管管辖内
  ├─ oldStudentId != newStudentId（换人入住）：
  │    ├─ 旧学生退宿：stay_history.check_out_date = now
  │    ├─ 新学生其他床位：student_id=null, status=EMPTY（防一人多床）
  │    ├─ 关闭新学生旧住宿历史
  │    └─ 新建 StayHistory {check_in_date=now}
  └─ UpdateWrapper 精细更新 bed 字段
```

### 7.3 调宿审批闭环

```
学生 Transfer.vue → saveTransfer({status:PENDING, ...})  // 学生不能改已有申请
  ↓
宿管 Transfer.vue 审批 → saveTransfer({status:APPROVED, targetRoomId})
  ↓
applyApprovedTransfer(@Transactional)：
  1. findCurrentBed(currentBedId 或 student_id)
  2. findAvailableTargetBed(targetRoomId 内 student_id=null && status=EMPTY)
  3. releaseBed(原床位)
  4. 占用目标床位
  5. releaseOtherBedsForStudent(清理残留占用)
  6. refreshRoomStatus(原房间 + 目标房间：入住数≥capacity→FULL，否则→NORMAL)
```

### 7.4 报修流程

```
学生 RepairApply.vue → saveRepair({status:PENDING, roomId=bed反查})
  ↓ 宿管接单
宿管 Repair.vue → saveRepair({status:PROCESSING, handlerId=自己})
  ↓ 处理完成
宿管 → saveRepair({status:COMPLETED})
  ↓
（学生端只读，不能改 status / handlerId）
```

### 7.5 角色数据范围过滤（以报修列表为例）

```
GET /api/repairRequest/list
  ↓
JwtAuthInterceptor 注入 currentUserId / currentUserRole
  ↓
RepairRequestServiceImpl.listRepairRequestsWithDetails(role, userId)
  ├─ role=admin：不过滤
  ├─ role=student：WHERE submitter_id = userId
  └─ role=dormmanager：
       ├─ managedStudentIds = dormManagerScopeService.managedStudentIds(userId)
       │    └─ 5 秒内存缓存（ConcurrentHashMap + CacheEntry）
       └─ WHERE submitter_id IN (managedStudentIds)
  ↓
按需批量加载 submitterName / handlerName / roomName / buildingName（避免 N+1）
  ↓
返回 Result<List<RepairRequest>>
```

### 7.6 前端 401 防抖流程

```
并发请求 → 任意一个返回 401
  ↓
handlingUnauthorized == false?
  ├─ 是：置 true → ElMessage.error → userStore.logout() → router.replace('/login') → 1.5s 后释放锁
  └─ 否：静默（避免重复弹窗）
  ↓
若 userStore.token 已为空（主动退出后）：跳过弹窗
```

---

## 8. 依赖关系

### 8.1 后端 Maven 依赖

| 依赖 | 版本 | 用途 |
|---|---|---|
| `spring-boot-starter-web` | 3.3.0(parent) | Spring MVC + Tomcat |
| `spring-security-crypto` | — | BCrypt 密码加密（仅工具类，未启用 Security 过滤器链） |
| `mysql-connector-j` | — | MySQL JDBC 驱动 |
| `lombok` | — | 简化 POJO（optional） |
| `mybatis-plus-spring-boot3-starter` | 3.5.5 | ORM |
| `jjwt-api` / `jjwt-impl` / `jjwt-jackson` | 0.12.3 | JWT 生成与解析 |
| `spring-boot-starter-test` | — | 测试 |

### 8.2 前端 npm 依赖

**运行时依赖**：

| 依赖 | 版本 | 用途 |
|---|---|---|
| `vue` | ^3.5.39 | 视图框架 |
| `vue-router` | ^5.1.0 | 路由 |
| `pinia` | ^3.0.4 | 状态管理 |
| `pinia-plugin-persistedstate` | ^4.7.1 | Pinia 状态持久化 |
| `element-plus` | ^2.14.2 | UI 组件库 |
| `axios` | ^1.18.1 | HTTP 客户端 |
| `@lucide/vue` | ^1.24.0 | 图标库 |

**开发依赖**：

| 依赖 | 版本 | 用途 |
|---|---|---|
| `vite` | ^8.1.1 | 构建工具 |
| `@vitejs/plugin-vue` | ^6.0.7 | Vue SFC 支持 |
| `unplugin-vue-components` | ^32.1.0 | Element Plus 按需引入 |
| `@playwright/test` | ^1.61.1 | E2E 测试 |

> 无 ESLint / Prettier / TypeScript 配置——纯 JS + Vue SFC，无类型检查。

### 8.3 模块间依赖关系

#### 后端依赖链

```
Controller → Service → ServiceImpl(extends ServiceImpl<Mapper, Entity>) → Mapper(extends BaseMapper<Entity>) → MySQL
                                     ↑
                                     └── DormManagerScopeService（@Lazy 注入 Building/Room/BedService 打破循环依赖）
                                     └── PasswordService（BCrypt）

横切：
  WebMvcConfig 注册 JwtAuthInterceptor + OperationAuditInterceptor
  GlobalExceptionHandler 捕获所有 Controller 抛出的异常
  AuthUtils 从 RequestContextHolder 读 JwtAuthInterceptor 注入的 attribute
```

#### 前端依赖链

```
main.js
  ├─ App.vue
  │    ├─ components/layout/Sidebar.vue  ← store/app.js, store/user.js
  │    ├─ components/layout/Topbar.vue   ← store/app.js, store/user.js, 各 api/*
  │    └─ <router-view /> ← router/index.js
  │
  ├─ router/index.js ← store/user.js（路由守卫）
  │
  └─ views/*
       ├─ api/* ← utils/request.js（axios 实例）← store/user.js（取 token）
       ├─ components/*（ChatDrawer / WeatherWidget / LocalFeaturePage / Hero / ServiceCard）
       ├─ store/app.js, store/user.js
       └─ assets/css/style.css, theme.css
```

### 8.4 外部服务依赖

| 服务 | 用途 | 调用方 |
|---|---|---|
| MySQL `dormitory` 库 | 数据持久化 | 后端 |
| Open-Meteo API | 天气数据 | 前端 `WeatherWidget.vue`（直接调用，不经过自家后端） |

> 无 Redis、MQ、对象存储、第三方 LLM 等依赖。整个系统是单体应用，部署简单。

---

## 9. 项目运行方式

### 9.1 环境要求

| 软件 | 版本要求 | 用途 |
|---|---|---|
| JDK | 17 | 后端运行 |
| Maven | 3.6+（项目自带 `mvnw` 包装器，可不预装 Maven） | 后端构建 |
| Node.js | 18+ | 前端运行 |
| MySQL | 8.0+ | 数据库 |

### 9.2 后端启动

**目录**：`f:\bishe\Antigravity\Dorm-Sys\backend`

#### 方式一：Maven 包装器（推荐）

```bash
# Windows
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

#### 方式二：IDE

直接运行 [BackendApplication.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/BackendApplication.java) 的 `main` 方法。

#### 方式三：打包后运行

```bash
mvnw.cmd package
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

#### 配置覆盖（环境变量）

| 环境变量 | 默认值 | 说明 |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/dormitory?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai` | 数据库连接 URL（URL 内含 `createDatabaseIfNotExist=true`，库不存在会自动创建） |
| `DB_USERNAME` | `root` | 数据库用户名 |
| `DB_PASSWORD` | `123456` | 数据库密码 |
| `JWT_SECRET` | `dormitory-demo-secret-key-2026-is-here!` | JWT 签名密钥（生产环境务必覆盖） |

- **端口**：8088
- **SQL 自动初始化**：`spring.sql.init.mode=always`，每次启动都执行 [schema.sql](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/resources/schema.sql)（全是 `CREATE TABLE IF NOT EXISTS` + `INSERT IGNORE`，幂等）。
- **dev profile**：`-Dspring-boot.run.profiles=dev` 或 `--spring.profiles.active=dev`，会打印 SQL 日志到 `./logs/sql.log`。

#### 默认测试账号

| 用户名 | 角色 | 密码 |
|---|---|---|
| `20240001` | 学生 | `123456` |
| `manager1` | 宿管 | `123456` |
| `admin` | 管理员 | `123456` |

### 9.3 前端启动

**目录**：`f:\bishe\Antigravity\Dorm-Sys\frontend`

```bash
# 安装依赖（首次）
npm install

# 开发模式（默认端口 5173）
npm run dev

# 生产构建
npm run build

# 预览生产构建
npm run preview
```

- Vite dev server 已配置代理：`/api` → `http://127.0.0.1:8088`（见 [vite.config.js](file:///f:/bishe/Antigravity/Dorm-Sys/frontend/vite.config.js)），前端发起的所有 `/api/**` 请求会被转发到后端 8088。
- 因此**前后端必须同时启动**：先启动后端 8088，再启动前端 5173。

### 9.4 访问应用

浏览器打开 `http://localhost:5173`，进入登录页，选择角色（会自动填入对应演示账号），点击登录即可进入对应角色的工作台。

### 9.5 日志

**位置**：`backend/logs/`

| 文件 | 说明 |
|---|---|
| `app.log` | 应用日志（按天滚动，保留 30 天） |
| `sql.log` | SQL 日志（仅 dev profile，按天滚动保留 30 天） |

日志格式：`%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n`

---

## 10. 测试体系

### 10.1 后端测试

**位置**：`backend/src/test/java/com/dorm/backend/`

| 测试类 | 覆盖范围 |
|---|---|
| `BackendApplicationTests` | Spring Boot 上下文加载 |
| `common/JwtUtilsTest` | JWT 生成与解析 |
| `config/JwtAuthInterceptorTest` | JWT 拦截器认证与授权 |
| `controller/AuthControllerTest` | 登录流程 |
| `controller/BusinessRecordControllerTest` | 通用业务记录 |
| `controller/ChatControllerTest` | 聊天接口 |
| `controller/DashboardControllerTest` | 仪表盘 |
| `controller/RepairRequestControllerTest` | 报修 |
| `controller/RoomControllerTest` | 房间 |
| `controller/TransferRequestControllerTest` | 调宿审批 |
| `controller/UserControllerTest` | 用户管理 |
| `controller/ScopeMutationAuthorizationTest` | 数据范围写授权 |
| `controller/StudentDataOwnershipTest` | 学生数据归属 |
| `service/DormManagerScopeServiceTest` | 宿管管辖范围服务 |

运行：`mvnw.cmd test`

### 10.2 前端 E2E 测试

**位置**：`frontend/tests/e2e/roles.spec.js` + [playwright.config.js](file:///f:/bishe/Antigravity/Dorm-Sys/frontend/playwright.config.js)

#### Playwright 配置

- `testDir: './tests/e2e'`，单 worker 串行执行，`reporter: 'list'`，`baseURL: http://127.0.0.1:5173`，失败时保留 trace。
- 启动两个 webServer：
  1. 后端 Spring Boot：`cmd /c "..\backend\mvnw.cmd spring-boot:run"`，健康检查 `http://127.0.0.1:8088/api/dashboard/stats`，超时 120s。
  2. 前端 Vite：`npm run dev -- --host 127.0.0.1`，健康检查 `http://127.0.0.1:5173`。
  - 两个都 `reuseExistingServer: true`，已起则不重启。

#### 测试用例（7 个）

1. **三角色演示账号登录**（参数化 3 个 test）：点角色按钮 → 断言 `#login-username` 自动填入对应账号 → 点登录 → 断言 URL 跳到该角色首页正则。
2. **管理概览和角色数据范围**：直接调 `POST /api/auth/login` 拿 token，再调 `/api/dashboard/stats` 和 `/api/dashboard/alerts` 断言 200；宿管 token 调 `/api/building/list` 和 `/api/dashboard/buildings` 断言「统计返回长度 = 管辖楼栋长度」；学生 token 调 `/api/user/list` 断言 403（权限隔离）。
3. **并发登录过期响应只提示一次**：用 `page.route` 把 `/api/businessRecord/list` 和 `/api/chat/notifications` mock 成 401，登录后期望 URL 仍在 `/login` 且 `.el-message--error` 只出现 1 次（验证 `handlingUnauthorized` 锁）。
4. **主动退出后忽略尚未完成请求的过期提示**：mock 延迟 800ms 的 401 响应，登录后立刻退出，断言只看到 `.el-message--info`（退出提示），1 秒后没有 `.el-message--error`（验证 token 已清空时 401 不弹窗）。
5. **管理员批量创建房间表单**：登录管理员 → 进 `/admin/resources/rooms` → 点「批量创建」 → 断言 dialog 可见且含「每层房间数」文本。
6. **宿管端不再展示无实际智能能力的 AI 巡查**：断言页面无「AI巡查」和「AI 巡查报告」文本（回归测试，防止删掉的功能复活）。
7. **管理员切换楼栋运营状态会保存到后端**：mock `/api/building/list` 返回一个测试楼、`/api/room/list` 返空、`/api/building/save` 拦截记录 postDataJSON；进楼栋管理页点 `.status-toggle .el-switch` → 断言提交的 building 对象含 `{id:1, active:false}`（验证开关反向 + 持久化）。

运行：`npm run test:e2e`（会自动起后端+前端，或复用已起的服务）

> **测试覆盖特点**：覆盖了三角色登录、权限隔离、并发 401 防抖、主动退出后过期静默、批量房间表单、AI 巡查回归、楼栋状态持久化 7 个关键场景。是端到端「happy path + 关键边界」级别覆盖，前端未做单元测试。

---

## 11. 架构特点与注意事项

### 11.1 显著架构模式

1. **三层角色 + 双重授权**：拦截器粗粒度 URL 控制 + Service/Controller 细粒度数据范围控制。
2. **`DormManagerScopeService` + 5 秒缓存**：把宿管管辖范围抽象为可复用的 `canManageXxx` 谓词，全系统统一调用。
3. **`listXxxWithDetails` 模式**：所有列表查询都带 `role`+`userId` 参数，内部按角色过滤 + 批量加载关联，避免 N+1。
4. **虚拟字段填充**：实体用 `@TableField(exist=false)` 声明展示字段，Service 层手动填充。
5. **懒加密迁移**：登录时检测旧明文密码并自动升级为 BCrypt。
6. **床位即状态**：`bed.student_id` 是入住的权威标识，所有「我的宿舍」「室友」查询都从 `bed` 表反查。
7. **MyBatis-Plus 极简数据层**：零自定义 SQL，全部 `BaseMapper` + `QueryWrapper`/`UpdateWrapper`。
8. **角色路由三段式（前端）**：路径首段即角色名，路由表 + Sidebar 的 `navMap` + Topbar 的 `pageNameMap` 三处都按角色键控。
9. **`businessRecord` 万能表模式（前端）**：4 种业务（公告/反馈/备忘/消息）共用一张表 + 一组 API，靠 `type` 字段区分，前端 `LocalFeaturePage` 是其通用 UI 模板。
10. **状态推进模式（前端）**：所有 `saveXxx` 都既是「新建」又是「更新」（按 `id` 是否存在判断），`advance(record)` 把状态数组索引 +1 实现流程推进。
11. **localStorage 多 key 持久化（前端）**：`dorm-theme`（主题）、`dorm-notifications-read:${accountId}`（通知已读，按账号隔离）、`student-notices-read:${userId}`（公告已读）、`dorm_sys_weather_location`（天气城市）、Pinia persist 的整个 user store。
12. **轮询而非 WebSocket**：通知 8 秒轮询、聊天 3 秒轮询，没有用 SSE/WebSocket，适合毕设规模但生产需改造。
13. **401 防抖（前端）**：模块级 `handlingUnauthorized` 标志 + token 判空，确保并发请求过期时只弹一次窗、主动退出后静默——这是被 e2e 测试明确覆盖的边界。
14. **演示账号自动填充（前端）**：Login.vue 切换角色时自动填入 `20240001/manager1/admin`，是 e2e 测试的契约。
15. **CSV 导出纯前端**：`admin/Reports.vue` 用 Blob + a.download 实现，带 BOM 防 Excel 乱码。
16. **天气组件纯前端**：直接调 Open-Meteo 公共 API，不经过自家后端，是唯一的外部依赖。

### 11.2 潜在改进点

1. `BuildingServiceImpl.getBuildingsWithStats` 全表加载 room+bed，数据量大时性能问题。
2. `schema.sql` 每次 `mode: always` 启动都执行，依赖 `IF NOT EXISTS`/`INSERT IGNORE` 幂等性。
3. `business_record` 通用表设计 vs `item_record`/`late_return_record` 独立表并存，存在职责重叠。
4. JWT 无刷新机制，24 小时过期后需重新登录；无黑名单/主动失效能力。
5. CORS 只允许 localhost，生产部署需调整。
6. `AiController` 是纯规则匹配，名为 AI 实为关键词路由。
7. 无 `@Transactional` 的多步操作（如部分 Controller 的 save）可能存在部分失败风险；`BedController.save` 未加事务注解但涉及多表写入。
8. 前端无 ESLint / Prettier / TypeScript 配置，部分视图（`Reports.vue`、`OperationLogs.vue`）被压缩成单行，可读性较低但功能完整。
9. 数据库无外键约束，关系完全靠应用层维护，存在数据不一致风险。
10. `building.manager`（姓名字符串）与 `manager_info.building_id`（外键）两套宿管-楼栋绑定关系并存，前者为历史回退字段。

---

## 附录：核心文件索引

### 后端

| 类别 | 文件 |
|---|---|
| 启动类 | [BackendApplication.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/BackendApplication.java) |
| 通用工具 | [Result.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/common/Result.java) · [JwtUtils.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/common/JwtUtils.java) · [AuthUtils.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/common/AuthUtils.java) · [GlobalExceptionHandler.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/common/GlobalExceptionHandler.java) |
| 配置 | [CorsConfig.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/config/CorsConfig.java) · [JwtAuthInterceptor.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/config/JwtAuthInterceptor.java) · [OperationAuditInterceptor.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/config/OperationAuditInterceptor.java) · [WebMvcConfig.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/config/WebMvcConfig.java) |
| 核心服务 | [DormManagerScopeService.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/service/DormManagerScopeService.java) · [PasswordService.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/service/PasswordService.java) |
| 核心控制器 | [AuthController.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/controller/AuthController.java) · [UserController.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/controller/UserController.java) · [RoomController.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/controller/RoomController.java) · [BedController.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/controller/BedController.java) · [TransferRequestController.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/controller/TransferRequestController.java) · [RepairRequestController.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/controller/RepairRequestController.java) · [ChatController.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/controller/ChatController.java) · [DashboardController.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/controller/DashboardController.java) · [AdminReportController.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/controller/AdminReportController.java) · [AiController.java](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/java/com/dorm/backend/controller/AiController.java) |
| 数据库 | [schema.sql](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/resources/schema.sql) · [schema-business-record.sql](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/resources/schema-business-record.sql) |
| 配置文件 | [application.yml](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/resources/application.yml) · [application-dev.yml](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/resources/application-dev.yml) · [logback-spring.xml](file:///f:/bishe/Antigravity/Dorm-Sys/backend/src/main/resources/logback-spring.xml) · [pom.xml](file:///f:/bishe/Antigravity/Dorm-Sys/backend/pom.xml) |

### 前端

| 类别 | 文件 |
|---|---|
| 入口 | [main.js](file:///f:/bishe/Antigravity/Dorm-Sys/frontend/src/main.js) · [App.vue](file:///f:/bishe/Antigravity/Dorm-Sys/frontend/src/App.vue) · [index.html](file:///f:/bishe/Antigravity/Dorm-Sys/frontend/index.html) |
| 配置 | [vite.config.js](file:///f:/bishe/Antigravity/Dorm-Sys/frontend/vite.config.js) · [playwright.config.js](file:///f:/bishe/Antigravity/Dorm-Sys/frontend/playwright.config.js) · [package.json](file:///f:/bishe/Antigravity/Dorm-Sys/frontend/package.json) |
| 路由/状态 | [router/index.js](file:///f:/bishe/Antigravity/Dorm-Sys/frontend/src/router/index.js) · [store/app.js](file:///f:/bishe/Antigravity/Dorm-Sys/frontend/src/store/app.js) · [store/user.js](file:///f:/bishe/Antigravity/Dorm-Sys/frontend/src/store/user.js) |
| HTTP | [utils/request.js](file:///f:/bishe/Antigravity/Dorm-Sys/frontend/src/utils/request.js) |
| 组件 | [components/layout/Sidebar.vue](file:///f:/bishe/Antigravity/Dorm-Sys/frontend/src/components/layout/Sidebar.vue) · [components/layout/Topbar.vue](file:///f:/bishe/Antigravity/Dorm-Sys/frontend/src/components/layout/Topbar.vue) · [components/ui/Hero.vue](file:///f:/bishe/Antigravity/Dorm-Sys/frontend/src/components/ui/Hero.vue) · [components/ui/ServiceCard.vue](file:///f:/bishe/Antigravity/Dorm-Sys/frontend/src/components/ui/ServiceCard.vue) · [components/ChatDrawer.vue](file:///f:/bishe/Antigravity/Dorm-Sys/frontend/src/components/ChatDrawer.vue) · [components/WeatherWidget.vue](file:///f:/bishe/Antigravity/Dorm-Sys/frontend/src/components/WeatherWidget.vue) · [components/LocalFeaturePage.vue](file:///f:/bishe/Antigravity/Dorm-Sys/frontend/src/components/LocalFeaturePage.vue) |
| 样式 | [assets/css/style.css](file:///f:/bishe/Antigravity/Dorm-Sys/frontend/src/assets/css/style.css) · [assets/css/theme.css](file:///f:/bishe/Antigravity/Dorm-Sys/frontend/src/assets/css/theme.css) |
| 测试 | [tests/e2e/roles.spec.js](file:///f:/bishe/Antigravity/Dorm-Sys/frontend/tests/e2e/roles.spec.js) |

---

*本文档基于项目代码现状生成，覆盖架构、模块、关键类与函数、依赖关系与运行方式等核心信息。*
