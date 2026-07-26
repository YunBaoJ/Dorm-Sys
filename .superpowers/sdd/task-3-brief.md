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
