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
