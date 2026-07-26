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

