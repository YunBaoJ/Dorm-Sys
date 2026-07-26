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
