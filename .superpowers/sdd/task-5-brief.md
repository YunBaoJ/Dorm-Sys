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
