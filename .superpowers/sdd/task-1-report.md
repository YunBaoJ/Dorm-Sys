# Task 1 Report

## 改动

- `Dorm-Sys/backend/src/main/java/com/dorm/backend/controller/BusinessRecordController.java`
  - 学生访问 `type=admin_notice` 时自动收敛为只读 `status=已发布`。
  - 非管理员保存 `admin_notice` 直接返回 `403`。
  - 管理员保存 `admin_notice` 时仅接受 `草稿`、`已发布` 两种状态。
  - 管理员发布 `admin_notice` 时强制写入当前 `creatorId`，并在 `已发布` 状态下写入 `eventTime`。
- `Dorm-Sys/backend/src/test/java/com/dorm/backend/controller/BusinessRecordControllerTest.java`
  - 先新增失败测试覆盖：学生读取已发布管理员公告、宿管禁止保存/删除管理员公告、管理员可发布管理员公告、管理员公告状态校验。
  - 补充测试辅助方法，复用认证上下文与公告构造。
- `Dorm-Sys/frontend/src/views/student/Notice.vue`
  - 学生公告页并行拉取 `manager_messages` 与 `admin_notice` 两个来源。
  - 合并结果后按 `eventTime || createTime` 倒序排序。
  - 列表展示时间改为优先显示 `eventTime`，文案同步更新为管理员与宿管通知。

## 测试命令与输出摘要

1. 红灯验证
   - 命令：`cd Dorm-Sys/backend && .\mvnw.cmd -Dtest=BusinessRecordControllerTest test`
   - 结果：FAIL
   - 摘要：
     - `studentCanReadOnlyPublishedAdminNotices` 返回 `403`，说明学生读取 `admin_notice` 尚未放行。
     - `adminNoticeOnlyAcceptsDraftOrPublishedStatus` 返回 `200`，说明管理员公告状态未校验。
     - `adminCanSavePublishedAdminNotice` 的 `eventTime` 为空，说明发布时未写发布时间。

2. 绿灯验证
   - 命令：`cd Dorm-Sys/backend && .\mvnw.cmd -Dtest=BusinessRecordControllerTest test`
   - 结果：PASS
   - 摘要：`Tests run: 10, Failures: 0, Errors: 0, Skipped: 0`

## 自审

- 改动严格限制在 brief 指定的三个业务文件内，未动其他工作树改动。
- 后端按 brief 完成 `admin_notice` 的学生只读、宿管禁改、管理员状态校验与发布时间写入。
- 学生端按 brief 合并了 `manager_messages` 与 `admin_notice` 两个来源，并按发布时间/创建时间统一排序。
- 删除权限没有额外放宽：宿管删除 `admin_notice` 仍被拒绝，现有管理员删除路径保持不变。

## 关注项

- 本次按 brief 只运行了 `BusinessRecordControllerTest`；`Notice.vue` 没有独立自动化测试覆盖。
- Maven 测试输出仍包含与本任务无关的 `ChatControllerTest` unchecked 编译警告，以及 JVM CDS 警告；不影响本任务测试结果。
