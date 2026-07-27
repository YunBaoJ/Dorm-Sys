# 学生宿舍管理系统

基于 Spring Boot + Vue 3 前后端分离架构的学生宿舍管理系统，面向学生、宿管员、管理员三类角色，覆盖宿舍资源管理、业务申请流转、日常服务与沟通等完整闭环。

本科毕业设计 · 作者：孙乾云 · 四川大学锦江学院 计算机学院 人工智能专业

---

## 系统预览

| 角色 | 核心界面 |
|------|---------|
| 学生端 | 服务台、我的宿舍、报修申请、费用查询、访客预约、调宿申请、校园公告、室友聊天、智慧辅导 |
| 宿管端 | 工作台、入住管理、报修处理、访客审批、卫生检查、调宿审批、晚归登记、水电计费 |
| 管理端 | 管理概览、用户管理、楼栋/房间/床位资源管理、报修监控、公告管理、操作日志、数据报表 |

---

## 技术栈

### 前端

| 技术 | 用途 |
|------|------|
| Vue 3 | 组件化页面开发 |
| Vite | 构建工具与开发服务器 |
| Element Plus | UI 组件库（表单、对话框、表格等） |
| Vue Router | 按角色划分的三端路由 |
| Pinia | 状态管理（登录态、用户信息） |
| Axios | HTTP 请求封装与 JWT 自动携带 |
| Lucide Vue | 图标库 |
| Playwright | E2E 端到端测试 |

### 后端

| 技术 | 用途 |
|------|------|
| Java 17 | 运行语言 |
| Spring Boot 3.3.0 | 后端框架 |
| MyBatis-Plus 3.5.5 | ORM 与数据访问 |
| MySQL | 业务数据持久化 |
| JJWT 0.12.3 | JWT 令牌签发与验证 |
| BCrypt | 密码加密 |
| Lombok | 代码简化 |
| Maven Wrapper | 构建管理 |
---
### 关键特性

- 调宿事务同步：审批通过后自动释放原床位、分配新床位，联动房间状态更新
- 批量建房：支持一键批量创建房间并自动生成对应数量的床位
- 室友聊天：同寝室私聊与群聊，消息记录持久化，仅限同寝范围
- 公告管理：草稿/发布状态隔离，学生端仅获取已发布的脱敏字段
- 操作审计：管理员关键变更自动记录操作日志，支持按模块追溯
- 数据报表：楼栋、房间、床位、报修实时聚合，支持 CSV 导出
- 智慧辅导：基于规则的宿舍常见问题知识问答


## 核心功能

### 三类角色

| 角色 | 功能范围 |
|------|---------|
| 学生 | 查看宿舍信息、报修、费用查询、访客预约、调宿申请、室友聊天、校园公告、规则问答、意见反馈 |
| 宿管员 | 入住管理、报修处理、访客审批、卫生检查、调宿审批、晚归登记、水电计费、物品出入、消息通知 |
| 管理员 | 用户管理、楼栋/房间/床位资源管理、报修监控、公告管理、操作日志审计、数据报表与 CSV 导出 |



---

## 本地运行

### 前置条件

- JDK 17
- Node.js 18+
- MySQL 服务已启动

### 1. 克隆项目

```bash
git clone https://github.com/YunBaoJ/Dorm-Sys.git
cd Dorm-Sys/Dorm-Sys
```

### 2. 数据库初始化

在 MySQL 中执行 backend/src/main/resources/schema.sql 创建表结构，可按需执行种子数据脚本。

### 3. 启动后端

```bash
cd backend
.\mvnw.cmd spring-boot:run
```

后端地址：http://127.0.0.1:8088

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端地址：http://127.0.0.1:5173

---

## 运行测试

```bash
# 后端测试
cd backend
.\mvnw.cmd test

# 前端 E2E 测试
cd frontend
npm run test:e2e
```

---

## 部署

### 前端（Vercel）

1. 在 Vercel 导入本仓库
2. 设置 Root Directory 为 Dorm-Sys/frontend
3. 添加环境变量 VITE_API_BASE_URL → 后端部署地址
4. 部署

### 后端（Railway / Render / VPS）

需要 Java 17 + MySQL 环境，通过环境变量配置：

| 变量 | 说明 |
|------|------|
| DB_URL | MySQL 连接地址 |
| DB_USERNAME | 数据库用户名 |
| DB_PASSWORD | 数据库密码 |
| JWT_SECRET | JWT 签名密钥 |

---

## 项目结构

```
Dorm-Sys/
├── frontend/                # Vue 3 前端
│   ├── src/
│   │   ├── api/            # API 请求层
│   │   ├── views/          # 页面（admin / manager / student）
│   │   ├── components/     # 公共组件
│   │   ├── router/         # 路由配置
│   │   ├── store/          # Pinia 状态管理
│   │   └── utils/          # Axios 封装等工具
│   ├── vercel.json         # Vercel 部署配置
│   └── package.json
├── backend/                 # Spring Boot 后端
│   └── src/main/java/com/dorm/backend/
│       ├── controller/     # REST 控制器
│       ├── service/        # 业务逻辑层
│       ├── mapper/         # MyBatis-Plus Mapper
│       ├── entity/         # 实体类
│       ├── dto/            # 数据传输对象
│       ├── config/         # 配置（CORS、拦截器）
│       └── common/         # 通用工具类
└── README.md
