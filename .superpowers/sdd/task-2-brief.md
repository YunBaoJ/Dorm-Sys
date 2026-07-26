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
