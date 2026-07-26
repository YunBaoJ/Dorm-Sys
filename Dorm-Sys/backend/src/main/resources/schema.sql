-- 数据库初始化（URL 中已含 createDatabaseIfNotExist=true，此处保留兼容）
CREATE DATABASE IF NOT EXISTS dormitory DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE dormitory;

-- ----------------------------
-- 1. 系统用户表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '账号',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `role` varchar(20) NOT NULL COMMENT '角色(student/dormmanager/admin)',
  `name` varchar(50) NOT NULL COMMENT '真实姓名',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像链接',
  `class_name` varchar(50) DEFAULT NULL COMMENT '班级',
  `email` varchar(50) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '电话',
  `enabled` tinyint(1) DEFAULT 1 COMMENT '是否启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';


-- ----------------------------
-- 2. 楼栋表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `building` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '楼栋名称',
  `type` varchar(20) DEFAULT '男生楼' COMMENT '楼栋类型',
  `floors` int(11) NOT NULL DEFAULT 6 COMMENT '总楼层数',
  `manager` varchar(50) DEFAULT NULL COMMENT '负责人',
  `location` varchar(100) DEFAULT NULL COMMENT '位置信息',
  `active` tinyint(1) DEFAULT 1 COMMENT '是否运营中',
  `total_rooms` int(11) DEFAULT 0 COMMENT '冗余字段：房间总数，由业务逻辑维护',
  `occupied_rooms` int(11) DEFAULT 0 COMMENT '冗余字段：已入住房间数，由业务逻辑维护',
  `free_rooms` int(11) DEFAULT 0 COMMENT '冗余字段：空房间数，由业务逻辑维护',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='楼栋表';

-- ----------------------------
-- 3. 房间表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `room` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `building_id` bigint(20) NOT NULL COMMENT '关联楼栋ID',
  `room_number` varchar(20) NOT NULL COMMENT '房间号',
  `floor` int(11) NOT NULL COMMENT '所在楼层',
  `capacity` int(11) NOT NULL DEFAULT 4 COMMENT '容纳床位数',
  `status` varchar(20) DEFAULT 'NORMAL' COMMENT '状态(NORMAL, FULL, MAINTENANCE)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_building_id` (`building_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房间表';

-- ----------------------------
-- 4. 床位表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `bed` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `room_id` bigint(20) NOT NULL COMMENT '关联房间ID',
  `bed_number` varchar(20) NOT NULL COMMENT '床位编号',
  `status` varchar(20) DEFAULT 'EMPTY' COMMENT '状态(EMPTY, OCCUPIED, BROKEN)',
  `student_id` bigint(20) DEFAULT NULL COMMENT '当前占用学生ID(关联sys_user)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_room_id` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='床位表';

-- ----------------------------
-- 5. 报修表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `repair_request` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `submitter_id` bigint(20) NOT NULL COMMENT '提交人ID',
  `room_id` bigint(20) NOT NULL COMMENT '关联房间ID',
  `type` varchar(50) NOT NULL COMMENT '报修类型(水管, 电器, 门窗等)',
  `description` text NOT NULL COMMENT '问题描述',
  `images` varchar(500) DEFAULT NULL COMMENT '图片链接(逗号分隔)',
  `status` varchar(20) DEFAULT 'PENDING' COMMENT '状态(PENDING, PROCESSING, COMPLETED)',
  `handler_id` bigint(20) DEFAULT NULL COMMENT '处理人ID(宿管/维修工)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_submitter_id` (`submitter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报修申请表';

-- ----------------------------
-- 6. 访客表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `visitor_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_id` bigint(20) NOT NULL COMMENT '受访学生ID',
  `visitor_name` varchar(50) NOT NULL COMMENT '访客姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `relation` varchar(50) DEFAULT NULL COMMENT '与学生关系',
  `visit_time` datetime NOT NULL COMMENT '来访时间',
  `leave_time` datetime DEFAULT NULL COMMENT '离开时间',
  `status` varchar(20) DEFAULT 'PENDING' COMMENT '状态(PENDING, APPROVED, LEFT)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_student_id` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访客记录表';

-- ----------------------------
-- 7. 调宿申请表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `transfer_request` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_id` bigint(20) NOT NULL COMMENT '申请学生ID',
  `current_bed_id` bigint(20) NOT NULL COMMENT '当前床位ID',
  `target_room_id` bigint(20) DEFAULT NULL COMMENT '目标房间ID(可空，由宿管分配)',
  `reason` text NOT NULL COMMENT '调宿原因',
  `status` varchar(20) DEFAULT 'PENDING' COMMENT '状态(PENDING, APPROVED, REJECTED)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_student_id` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='调宿申请表';

-- ----------------------------
-- 8. 水电费账单表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `fee_bill` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `room_id` bigint(20) NOT NULL COMMENT '关联房间ID',
  `type` varchar(20) NOT NULL COMMENT '费用类型(WATER, ELECTRICITY)',
  `amount` decimal(10,2) NOT NULL COMMENT '金额',
  `month` varchar(20) NOT NULL COMMENT '账单月份(如 2026-01)',
  `status` varchar(20) DEFAULT 'UNPAID' COMMENT '状态(UNPAID, PAID)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_room_id` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='水电费账单表';

-- ----------------------------
-- 9. 卫生检查表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `hygiene_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `room_id` bigint(20) NOT NULL COMMENT '被检查房间ID',
  `inspector_id` bigint(20) NOT NULL COMMENT '检查人ID(宿管)',
  `score` int(11) NOT NULL COMMENT '分数(0-100)',
  `comment` varchar(255) DEFAULT NULL COMMENT '评语/备注',
  `check_date` date NOT NULL COMMENT '检查日期',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_hygiene_room_id` (`room_id`),
  KEY `idx_hygiene_check_date` (`check_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='卫生检查记录表';

-- ----------------------------
-- 10. 智能通话预约表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `call_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_id` bigint(20) NOT NULL COMMENT '发起学生ID',
  `topic` varchar(100) NOT NULL COMMENT '通话事由',
  `target_person` varchar(50) DEFAULT NULL COMMENT '联系人或房间',
  `status` varchar(20) DEFAULT 'PENDING' COMMENT '状态(PENDING, ACCEPTED, FINISHED)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发起时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_call_student_id` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能通话预约表';

-- ----------------------------
-- 11. 意见反馈表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `feedback` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_id` bigint(20) NOT NULL COMMENT '提交学生ID',
  `type` varchar(50) NOT NULL COMMENT '反馈类型',
  `content` text NOT NULL COMMENT '反馈内容',
  `reply` text DEFAULT NULL COMMENT '宿管回复',
  `status` varchar(20) DEFAULT 'UNREAD' COMMENT '状态(UNREAD, REPLIED)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_feedback_student_id` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='意见反馈表';

-- ----------------------------
-- 12. 操作日志表（原由 Java CommandLineRunner 创建）
-- ----------------------------
CREATE TABLE IF NOT EXISTS `operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `operator_id` bigint NOT NULL,
  `operator_name` varchar(100) NOT NULL,
  `module` varchar(50) NOT NULL,
  `action` varchar(20) NOT NULL,
  `path` varchar(255) NOT NULL,
  `result` varchar(20) NOT NULL,
  `summary` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_operation_log_time` (`create_time`),
  KEY `idx_operation_log_module` (`module`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ----------------------------
-- 13. 通用业务记录表（原由 Java CommandLineRunner 创建）
-- ----------------------------
CREATE TABLE IF NOT EXISTS `business_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type` varchar(50) NOT NULL COMMENT '业务类型',
  `title` varchar(100) NOT NULL COMMENT '标题',
  `owner` varchar(100) DEFAULT NULL COMMENT '对象/联系人/位置',
  `description` text COMMENT '说明',
  `status` varchar(30) DEFAULT NULL COMMENT '状态',
  `creator_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `event_time` datetime DEFAULT NULL COMMENT '业务时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_business_record_type` (`type`),
  KEY `idx_business_record_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通用业务记录表';

-- 初始化测试数据（用户名唯一，重复时自动跳过）
INSERT IGNORE INTO `sys_user` (`username`, `password`, `role`, `name`, `avatar`, `class_name`, `email`, `phone`) VALUES
('20240001', '$2b$12$fkepBhQatdh.trQqZmPZcuZwJhLFNz1I6DuLntDfPnQiv5YlaTRrC', 'student', '张伟', '/images/avatar.jpg', '计科2201', 'stu001@stu.edu.cn', '13800010001'),
('manager1', '$2b$12$fkepBhQatdh.trQqZmPZcuZwJhLFNz1I6DuLntDfPnQiv5YlaTRrC', 'dormmanager', '王叔', NULL, NULL, NULL, NULL),
('admin', '$2b$12$fkepBhQatdh.trQqZmPZcuZwJhLFNz1I6DuLntDfPnQiv5YlaTRrC', 'admin', '超级管理员', NULL, NULL, NULL, NULL);

-- 补充索引（如使用 JPA 自动建表则下方索引由 JPA 自动创建，此处保留用于手动建库参考）
CREATE INDEX IF NOT EXISTS idx_late_return_student_id ON late_return_record(student_id);
