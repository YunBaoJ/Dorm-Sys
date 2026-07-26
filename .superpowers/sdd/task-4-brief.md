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
