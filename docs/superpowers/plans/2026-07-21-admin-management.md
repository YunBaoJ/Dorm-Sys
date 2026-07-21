# 管理员端管理能力 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为管理员端增加公告管理、操作日志和实时数据报表，并让已发布公告进入学生通知链路。

**Architecture:** 管理员公告复用 `business_record`；操作日志使用独立表和 MVC 拦截器；报表由后端实时聚合楼栋、房间、床位和报修数据。前端新增三个独立页面和 API 模块，CSV 在浏览器端基于已加载数据生成。

**Tech Stack:** Java 17、Spring Boot 3.3、MyBatis-Plus、MySQL、Vue 3、Element Plus、Playwright。

## Global Constraints

- 不引入消息队列、报表平台或新的前端依赖。
- 操作日志不保存密码、令牌、Authorization 请求头或完整请求体。
- 操作日志写入失败不得阻断原业务请求。
- 报表无数据时返回零值和空楼栋列表。
- CSV 使用 UTF-8 BOM，确保 Windows Excel 可直接显示中文。
- 三个管理模块只允许 `admin` 角色访问。

---

### Task 1: 管理员公告后端与学生通知链路

**Files:**
- Modify: `Dorm-Sys/backend/src/main/java/com/dorm/backend/controller/BusinessRecordController.java`
- Modify: `Dorm-Sys/backend/src/test/java/com/dorm/backend/controller/BusinessRecordControllerTest.java`
- Modify: `Dorm-Sys/frontend/src/views/student/Notice.vue`

**Interfaces:**
- Consumes: `BusinessRecordService` 和现有 `business_record` 表。
- Produces: `GET /api/businessRecord/list?type=admin_notice&status=已发布` 与现有 `/save`、`DELETE /{id}`。

- [ ] **Step 1: 写失败测试**

在 `BusinessRecordControllerTest` 增加：学生只能读取已发布 `admin_notice`，宿管不能保存或删除 `admin_notice`，管理员可保存。

```java
@Test
void studentCanReadOnlyPublishedAdminNotices() {
    authenticate(7L, "student");
    Result<List<BusinessRecord>> result = controller.list("admin_notice", null);
    assertThat(result.getCode()).isEqualTo(200);
    verify(service).list(argThat(wrapper -> wrapper.getExpression().getNormal().toString().contains("status")));
}

@Test
void managerCannotPublishAdminNotice() {
    authenticate(8L, "dormmanager");
    BusinessRecord notice = notice("admin_notice", "已发布");
    assertThat(controller.save(notice).getCode()).isEqualTo(403);
    verify(service, never()).saveOrUpdate(any());
}
```

- [ ] **Step 2: 运行测试并确认失败**

Run: `cd Dorm-Sys/backend && .\mvnw.cmd -Dtest=BusinessRecordControllerTest test`

Expected: FAIL，学生读取 `admin_notice` 返回 403，宿管保存未被拒绝。

- [ ] **Step 3: 实现公告权限**

在 `list` 中为学生增加只读分支，在 `save`/`delete` 中保护管理员公告：

```java
if (AuthUtils.isStudent() && "admin_notice".equals(type)) {
    status = "已发布";
} else if (AuthUtils.isStudent()) {
    // 保留现有 feedback 与 manager_messages 逻辑
}

if ("admin_notice".equals(record.getType()) && !"admin".equals(AuthUtils.getCurrentUserRole())) {
    return Result.error(403, "仅管理员可维护全校公告");
}
```

保存时管理员公告只接受 `草稿` 或 `已发布`，发布时设置 `eventTime`，并强制 `creatorId` 为当前管理员。

- [ ] **Step 4: 学生公告页合并两个来源**

```js
const [managerMessages, adminNotices] = await Promise.all([
  getBusinessRecords('manager_messages', '已发布'),
  getBusinessRecords('admin_notice', '已发布')
])
notices.value = [...(adminNotices || []), ...(managerMessages || [])]
  .sort((a, b) => new Date(b.eventTime || b.createTime) - new Date(a.eventTime || a.createTime))
```

- [ ] **Step 5: 验证并提交**

Run: `cd Dorm-Sys/backend && .\mvnw.cmd -Dtest=BusinessRecordControllerTest test`

Expected: PASS。

Commit: `feat: 增加管理员公告权限与学生通知`

---

### Task 2: 操作日志数据模型与自动审计

**Files:**
- Create: `Dorm-Sys/backend/src/main/java/com/dorm/backend/entity/OperationLog.java`
- Create: `Dorm-Sys/backend/src/main/java/com/dorm/backend/mapper/OperationLogMapper.java`
- Create: `Dorm-Sys/backend/src/main/java/com/dorm/backend/service/OperationLogService.java`
- Create: `Dorm-Sys/backend/src/main/java/com/dorm/backend/service/impl/OperationLogServiceImpl.java`
- Create: `Dorm-Sys/backend/src/main/java/com/dorm/backend/config/OperationLogSchemaInitializer.java`
- Create: `Dorm-Sys/backend/src/main/java/com/dorm/backend/config/OperationAuditInterceptor.java`
- Create: `Dorm-Sys/backend/src/main/java/com/dorm/backend/controller/OperationLogController.java`
- Modify: `Dorm-Sys/backend/src/main/java/com/dorm/backend/config/WebMvcConfig.java`
- Modify: `Dorm-Sys/backend/src/main/java/com/dorm/backend/config/JwtAuthInterceptor.java`
- Create: `Dorm-Sys/backend/src/test/java/com/dorm/backend/config/OperationAuditInterceptorTest.java`

**Interfaces:**
- Produces: `GET /api/operationLog/list?module=&result=&keyword=`。
- Entity fields: `id`, `operatorId`, `operatorName`, `module`, `action`, `path`, `result`, `summary`, `createTime`。

- [ ] **Step 1: 写审计拦截器失败测试**

```java
@Test
void recordsAdminMutationWithoutSensitiveData() {
    request.setMethod("POST");
    request.setRequestURI("/api/user/save");
    request.setAttribute("currentUserId", 1L);
    request.setAttribute("currentUserRole", "admin");
    interceptor.afterCompletion(request, response, new Object(), null);
    verify(logService).save(argThat(log ->
        "用户权限".equals(log.getModule())
            && "成功".equals(log.getResult())
            && !log.getSummary().contains("password")));
}

@Test
void ignoresReadsAndNonAdminMutations() { /* verify never save */ }
```

- [ ] **Step 2: 运行测试并确认缺少类型而失败**

Run: `cd Dorm-Sys/backend && .\mvnw.cmd -Dtest=OperationAuditInterceptorTest test`

Expected: test compilation FAIL because `OperationAuditInterceptor` does not exist。

- [ ] **Step 3: 创建表与领域类型**

初始化 SQL：

```sql
CREATE TABLE IF NOT EXISTS operation_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  operator_id BIGINT NOT NULL,
  operator_name VARCHAR(100) NOT NULL,
  module VARCHAR(50) NOT NULL,
  action VARCHAR(20) NOT NULL,
  path VARCHAR(255) NOT NULL,
  result VARCHAR(20) NOT NULL,
  summary VARCHAR(255),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_operation_log_time (create_time),
  KEY idx_operation_log_module (module)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- [ ] **Step 4: 实现拦截器**

`afterCompletion` 仅处理角色为 `admin` 且方法为 POST/PUT/DELETE 的请求，排除 `/api/auth/` 与 `/api/operationLog/`。动作映射为新增/修改/删除，模块按 URL 前缀映射；摘要只写 HTTP 方法和路径。`operationLogService.save` 放在 try/catch 中，失败仅记录 `log.error`。

- [ ] **Step 5: 实现只读查询接口和权限**

```java
@GetMapping("/list")
public Result<List<OperationLog>> list(String module, String result, String keyword) {
    if (!"admin".equals(AuthUtils.getCurrentUserRole())) return Result.error(403, "仅管理员可查看操作日志");
    QueryWrapper<OperationLog> query = new QueryWrapper<>();
    if (StringUtils.hasText(module)) query.eq("module", module);
    if (StringUtils.hasText(result)) query.eq("result", result);
    if (StringUtils.hasText(keyword)) query.and(q -> q.like("operator_name", keyword).or().like("summary", keyword));
    return Result.success(service.list(query.orderByDesc("create_time").last("LIMIT 500")));
}
```

- [ ] **Step 6: 验证并提交**

Run: `cd Dorm-Sys/backend && .\mvnw.cmd -Dtest=OperationAuditInterceptorTest test`

Expected: PASS。

Commit: `feat: 增加管理员操作审计日志`

---

### Task 3: 管理员实时数据报表接口

**Files:**
- Create: `Dorm-Sys/backend/src/main/java/com/dorm/backend/controller/AdminReportController.java`
- Create: `Dorm-Sys/backend/src/test/java/com/dorm/backend/controller/AdminReportControllerTest.java`
- Modify: `Dorm-Sys/backend/src/main/java/com/dorm/backend/config/JwtAuthInterceptor.java`

**Interfaces:**
- Produces: `GET /api/admin/report/summary` returning `{ buildingCount, roomCount, totalBeds, occupiedBeds, occupancyRate, emptyBeds, repairCount, completedRepairs, repairCompletionRate, buildings }`。

- [ ] **Step 1: 写失败测试**

构造 2 栋楼、3 个房间、6 个床位（3 个入住）和 4 条报修（3 条完成），断言全局入住率 50、报修完成率 75，并断言楼栋分项。

```java
assertThat(data).containsEntry("totalBeds", 6L)
    .containsEntry("occupiedBeds", 3L)
    .containsEntry("occupancyRate", 50)
    .containsEntry("repairCompletionRate", 75);
```

- [ ] **Step 2: 运行测试并确认失败**

Run: `cd Dorm-Sys/backend && .\mvnw.cmd -Dtest=AdminReportControllerTest test`

Expected: test compilation FAIL because controller does not exist。

- [ ] **Step 3: 实现实时聚合**

一次读取楼栋、房间、床位和报修集合，通过 `roomId -> buildingId` 映射统计。入住床位口径为 `studentId != null || status == OCCUPIED`；完成报修口径为 `status == COMPLETED`。百分比四舍五入为整数，分母为零时返回 0。

- [ ] **Step 4: 增加管理员只读权限**

将 `/api/admin/report/` 加入 `JwtAuthInterceptor.isAdminOnlyRead`，非管理员 GET 返回 403。

- [ ] **Step 5: 验证并提交**

Run: `cd Dorm-Sys/backend && .\mvnw.cmd -Dtest=AdminReportControllerTest test`

Expected: PASS。

Commit: `feat: 增加管理员宿舍数据报表`

---

### Task 4: 管理员公告管理页面

**Files:**
- Create: `Dorm-Sys/frontend/src/views/admin/Notices.vue`
- Modify: `Dorm-Sys/frontend/src/api/businessRecord.js`
- Modify: `Dorm-Sys/frontend/src/router/index.js`
- Modify: `Dorm-Sys/frontend/src/components/layout/Sidebar.vue`
- Modify: `Dorm-Sys/frontend/tests/e2e/roles.spec.js`

**Interfaces:**
- Consumes: Task 1 的 `admin_notice` CRUD。

- [ ] **Step 1: 写失败 E2E**

登录管理员，点击“公告管理”，断言页面标题、状态筛选和“新建公告”按钮可见；打开表单后断言标题、正文和状态字段。

- [ ] **Step 2: 运行并确认菜单不存在**

Run: `cd Dorm-Sys/frontend && npx playwright test --grep "管理员可以维护全校公告"`

Expected: FAIL，找不到“公告管理”。

- [ ] **Step 3: 实现页面与路由**

页面加载 `getBusinessRecords('admin_notice')`，表单保存以下结构：

```js
await saveBusinessRecord({
  id: form.id,
  type: 'admin_notice',
  title: form.title.trim(),
  description: form.description.trim(),
  status: form.status
})
```

列表提供编辑、发布/撤回和删除；状态使用 `草稿`、`已发布`。

- [ ] **Step 4: 验证并提交**

Run: `cd Dorm-Sys/frontend && npx playwright test --grep "管理员可以维护全校公告"`

Expected: PASS。

Commit: `feat: 增加管理员公告管理页面`

---

### Task 5: 操作日志页面

**Files:**
- Create: `Dorm-Sys/frontend/src/api/operationLog.js`
- Create: `Dorm-Sys/frontend/src/views/admin/OperationLogs.vue`
- Modify: `Dorm-Sys/frontend/src/router/index.js`
- Modify: `Dorm-Sys/frontend/src/components/layout/Sidebar.vue`
- Modify: `Dorm-Sys/frontend/tests/e2e/roles.spec.js`

**Interfaces:**
- Consumes: Task 2 的 `GET /api/operationLog/list`。

- [ ] **Step 1: 写失败 E2E**

拦截日志 API 返回两条数据，登录管理员进入“操作日志”，断言操作人、模块、动作、结果和时间显示，并验证模块筛选会重新请求。

- [ ] **Step 2: 运行并确认页面不存在**

Run: `cd Dorm-Sys/frontend && npx playwright test --grep "管理员可以筛选操作日志"`

Expected: FAIL。

- [ ] **Step 3: 实现只读表格**

API：

```js
export const getOperationLogs = params => request.get('/operationLog/list', { params })
```

页面提供模块、结果、关键词筛选和刷新，不提供新增、编辑或删除按钮。

- [ ] **Step 4: 验证并提交**

Run: `cd Dorm-Sys/frontend && npx playwright test --grep "管理员可以筛选操作日志"`

Expected: PASS。

Commit: `feat: 增加管理员操作日志页面`

---

### Task 6: 数据报表页面与 CSV 导出

**Files:**
- Create: `Dorm-Sys/frontend/src/api/adminReport.js`
- Create: `Dorm-Sys/frontend/src/views/admin/Reports.vue`
- Modify: `Dorm-Sys/frontend/src/router/index.js`
- Modify: `Dorm-Sys/frontend/src/components/layout/Sidebar.vue`
- Modify: `Dorm-Sys/frontend/tests/e2e/roles.spec.js`

**Interfaces:**
- Consumes: Task 3 的 `GET /api/admin/report/summary`。

- [ ] **Step 1: 写失败 E2E**

拦截报表 API，断言入住率、空床位、报修完成率和楼栋对比可见；点击导出后监听 `download`，断言文件名以 `.csv` 结尾。

- [ ] **Step 2: 运行并确认页面不存在**

Run: `cd Dorm-Sys/frontend && npx playwright test --grep "管理员可以查看并导出数据报表"`

Expected: FAIL。

- [ ] **Step 3: 实现报表页面**

使用紧凑 KPI 区、入住与报修两个进度条、楼栋对比表。导出函数：

```js
const rows = [
  ['楼栋', '房间数', '床位数', '入住数', '入住率'],
  ...report.buildings.map(item => [item.name, item.roomCount, item.totalBeds, item.occupiedBeds, `${item.occupancyRate}%`])
]
const csv = '\uFEFF' + rows.map(row => row.map(cell => `"${String(cell).replaceAll('"', '""')}"`).join(',')).join('\r\n')
const url = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8' }))
```

触发隐藏 `<a download="宿舍运行报表-YYYY-MM-DD.csv">` 后撤销 URL。

- [ ] **Step 4: 验证并提交**

Run: `cd Dorm-Sys/frontend && npx playwright test --grep "管理员可以查看并导出数据报表"`

Expected: PASS。

Commit: `feat: 增加管理员数据报表与导出`

---

### Task 7: 全量回归与运行服务

**Files:**
- Verify all modified files.

- [ ] **Step 1: 后端全量测试**

Run: `cd Dorm-Sys/backend && .\mvnw.cmd test`

Expected: 全部通过，0 failures，0 errors。

- [ ] **Step 2: 前端生产构建**

Run: `cd Dorm-Sys/frontend && npm run build`

Expected: Vite build exit code 0。

- [ ] **Step 3: 前端全量 E2E**

Run: `cd Dorm-Sys/frontend && npx playwright test`

Expected: 全部通过。

- [ ] **Step 4: 权限检查**

使用学生和宿管令牌请求 `/api/operationLog/list` 与 `/api/admin/report/summary`，期望 HTTP 403；管理员请求期望 200。

- [ ] **Step 5: 重启服务并提交最终修正**

后端监听 `8088`，前端开发服务监听 `5173`。只提交本计划涉及文件，不提交 `application.yml`、论文目录或用户本地文件。

