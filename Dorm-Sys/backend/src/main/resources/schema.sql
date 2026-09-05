-- 数据库初始化：表结构（URL 中已含 createDatabaseIfNotExist=true）
-- 使用当前连接指定的数据库，不切换库

-- ----------------------------
-- 1. 系统用户表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '账号',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `role` varchar(20) NOT NULL COMMENT '角色(student/dormmanager/admin)',
  `name` varchar(50) NOT NULL COMMENT '真实姓名',
  `gender` varchar(10) DEFAULT NULL COMMENT '性别',
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
  UNIQUE KEY `uk_bed_student_id` (`student_id`),
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
  UNIQUE KEY `uk_fee_bill_room_type_month` (`room_id`, `type`, `month`),
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
  `reply` text DEFAULT NULL COMMENT '处理回复',
  `event_time` datetime DEFAULT NULL COMMENT '业务时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_business_record_type` (`type`),
  KEY `idx_business_record_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通用业务记录表';

-- ----------------------------
-- 14. 物品出入登记表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `item_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL COMMENT '物品名称',
  `owner` varchar(100) DEFAULT NULL COMMENT '经办人/房间',
  `description` text COMMENT '详细说明',
  `status` varchar(30) DEFAULT 'PENDING' COMMENT '状态(PENDING/RELEASED/RETURNED)',
  `creator_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物品出入登记表';

-- ----------------------------
-- 15. 晚归登记表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `late_return_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `student_name` varchar(50) DEFAULT NULL COMMENT '学生姓名',
  `room_number` varchar(20) DEFAULT NULL COMMENT '房间号',
  `reason` text COMMENT '晚归原因',
  `status` varchar(30) DEFAULT 'PENDING' COMMENT '状态(PENDING/CONFIRMED)',
  `return_time` datetime DEFAULT NULL COMMENT '晚归时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_late_return_student_id` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='晚归登记表';

-- ----------------------------
-- 16. 聊天消息表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `sender_id` bigint NOT NULL COMMENT '发送者ID',
  `receiver_id` bigint DEFAULT NULL COMMENT '接收者ID(私聊)',
  `room_id` bigint DEFAULT NULL COMMENT '房间ID(群聊)',
  `type` varchar(20) NOT NULL COMMENT '类型(PRIVATE/GROUP)',
  `content` text NOT NULL COMMENT '消息内容',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`),
  KEY `idx_chat_sender` (`sender_id`),
  KEY `idx_chat_receiver` (`receiver_id`),
  KEY `idx_chat_room` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

-- ----------------------------
-- 17. 住宿历史表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `stay_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `bed_id` bigint NOT NULL COMMENT '床位ID',
  `check_in_date` datetime NOT NULL COMMENT '入住时间',
  `check_out_date` datetime DEFAULT NULL COMMENT '退宿时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_stay_student` (`student_id`),
  KEY `idx_stay_bed` (`bed_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='住宿历史表';

-- ----------------------------
-- 18. 学生扩展信息表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `student_info` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `class_name` varchar(50) DEFAULT NULL COMMENT '班级',
  `major` varchar(50) DEFAULT NULL COMMENT '专业',
  `enrollment_year` int DEFAULT NULL COMMENT '入学年份',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_info_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生扩展信息表';

-- ----------------------------
-- 19. 宿管扩展信息表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `manager_info` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `employee_no` varchar(50) DEFAULT NULL COMMENT '工号',
  `building_id` bigint DEFAULT NULL COMMENT '管辖楼栋ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_manager_info_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宿管扩展信息表';

-- ----------------------------
-- 20. 管理员扩展信息表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `admin_info` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `department` varchar(50) DEFAULT NULL COMMENT '部门',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_info_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员扩展信息表';

-- ----------------------------
-- 兼容旧版本表结构（MySQL 不支持 ADD COLUMN IF NOT EXISTS）
-- ----------------------------
SET @migration_sql = (
  SELECT IF(COUNT(*) = 0, 'DO 0',
    CONCAT('ALTER TABLE `sys_user` ', GROUP_CONCAT(ddl ORDER BY sort_order SEPARATOR ', ')))
  FROM (
    SELECT 1 AS sort_order, CAST('ADD COLUMN `gender` varchar(10) DEFAULT NULL COMMENT ''性别''' AS CHAR(1000)) AS ddl
      WHERE NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'gender')
    UNION ALL SELECT 2, 'ADD COLUMN `avatar` varchar(255) DEFAULT NULL COMMENT ''头像链接'''
      WHERE NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'avatar')
    UNION ALL SELECT 3, 'ADD COLUMN `class_name` varchar(50) DEFAULT NULL COMMENT ''班级'''
      WHERE NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'class_name')
    UNION ALL SELECT 4, 'ADD COLUMN `email` varchar(50) DEFAULT NULL COMMENT ''邮箱'''
      WHERE NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'email')
    UNION ALL SELECT 5, 'ADD COLUMN `phone` varchar(20) DEFAULT NULL COMMENT ''电话'''
      WHERE NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'phone')
    UNION ALL SELECT 6, 'ADD COLUMN `enabled` tinyint(1) DEFAULT 1 COMMENT ''是否启用'''
      WHERE NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'enabled')
    UNION ALL SELECT 7, 'ADD COLUMN `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间'''
      WHERE NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'create_time')
    UNION ALL SELECT 8, 'ADD COLUMN `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间'''
      WHERE NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'update_time')
  ) missing_columns
);
PREPARE schema_migration FROM @migration_sql;
EXECUTE schema_migration;
DEALLOCATE PREPARE schema_migration;

SET @migration_sql = IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'business_record' AND COLUMN_NAME = 'reply'),
  'DO 0', 'ALTER TABLE `business_record` ADD COLUMN `reply` text DEFAULT NULL COMMENT ''处理回复'''
);
PREPARE schema_migration FROM @migration_sql;
EXECUTE schema_migration;
DEALLOCATE PREPARE schema_migration;

SET @migration_sql = IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'item_record' AND COLUMN_NAME = 'creator_id'),
  'DO 0', 'ALTER TABLE `item_record` ADD COLUMN `creator_id` bigint DEFAULT NULL COMMENT ''创建人ID'''
);
PREPARE schema_migration FROM @migration_sql;
EXECUTE schema_migration;
DEALLOCATE PREPARE schema_migration;

-- 旧库若已有重复业务数据，先保留数据并跳过唯一索引，避免阻断启动。
-- 应用层会串行化后续写入；人工归并历史数据后，下次启动会自动补建索引。
SET @migration_sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'fee_bill'
      AND INDEX_NAME = 'uk_fee_bill_room_type_month'
    GROUP BY INDEX_NAME
    HAVING MIN(NON_UNIQUE) = 0
      AND GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) = 'room_id,type,month'
  ),
  'DO 0',
  IF(
    EXISTS (
      SELECT 1 FROM `fee_bill`
      GROUP BY `room_id`, `type`, `month`
      HAVING COUNT(*) > 1
    ),
    'DO 0',
    'ALTER TABLE `fee_bill` ADD UNIQUE KEY `uk_fee_bill_room_type_month` (`room_id`, `type`, `month`)'
  )
);
PREPARE schema_migration FROM @migration_sql;
EXECUTE schema_migration;
DEALLOCATE PREPARE schema_migration;

SET @migration_sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'bed'
      AND INDEX_NAME = 'uk_bed_student_id'
    GROUP BY INDEX_NAME
    HAVING MIN(NON_UNIQUE) = 0
      AND GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) = 'student_id'
  ),
  'DO 0',
  IF(
    EXISTS (
      SELECT 1 FROM `bed`
      WHERE `student_id` IS NOT NULL
      GROUP BY `student_id`
      HAVING COUNT(*) > 1
    ),
    'DO 0',
    'ALTER TABLE `bed` ADD UNIQUE KEY `uk_bed_student_id` (`student_id`)'
  )
);
PREPARE schema_migration FROM @migration_sql;
EXECUTE schema_migration;
DEALLOCATE PREPARE schema_migration;

-- 重复历史数据会暂时阻止唯一索引；此时保留普通索引，避免按学生加锁时扫描整张床位表。
SET @migration_sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'bed'
      AND INDEX_NAME = 'uk_bed_student_id'
    GROUP BY INDEX_NAME
    HAVING MIN(NON_UNIQUE) = 0
      AND GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) = 'student_id'
  ) AND EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'bed'
      AND INDEX_NAME = 'idx_bed_student_lookup'
    GROUP BY INDEX_NAME
    HAVING MIN(NON_UNIQUE) = 1
      AND GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) = 'student_id'
  ),
  'ALTER TABLE `bed` DROP INDEX `idx_bed_student_lookup`',
  IF(
    EXISTS (
      SELECT 1 FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'bed'
        AND COLUMN_NAME = 'student_id'
        AND SEQ_IN_INDEX = 1
    ),
    'DO 0',
    IF(
      EXISTS (
        SELECT 1 FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'bed'
          AND INDEX_NAME = 'idx_bed_student_lookup'
      ),
      'DO 0',
      'ALTER TABLE `bed` ADD KEY `idx_bed_student_lookup` (`student_id`)'
    )
  )
);
PREPARE schema_migration FROM @migration_sql;
EXECUTE schema_migration;
DEALLOCATE PREPARE schema_migration;

-- 仅为唯一床位分配补齐当前住宿记录；重复分配需先人工确认实际床位。
-- 床位更新时间是现有数据中最接近入住时间的依据。
INSERT INTO `stay_history` (`student_id`, `bed_id`, `check_in_date`)
SELECT bed.`student_id`, bed.`id`, COALESCE(bed.`update_time`, bed.`create_time`, CURRENT_TIMESTAMP)
FROM `bed` bed
WHERE bed.`student_id` IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `bed` duplicate_bed
    WHERE duplicate_bed.`student_id` = bed.`student_id`
      AND duplicate_bed.`id` <> bed.`id`
  )
  AND NOT EXISTS (
    SELECT 1
    FROM `stay_history` history
    WHERE history.`student_id` = bed.`student_id`
      AND history.`check_out_date` IS NULL
  );

-- 以实际分配了学生的床位数刷新房间状态，维修状态由人工维护，不在迁移中覆盖。
UPDATE `room` room
LEFT JOIN (
  SELECT bed.`room_id`, COUNT(bed.`student_id`) AS occupied
  FROM `bed` bed
  GROUP BY bed.`room_id`
) occupancy ON occupancy.`room_id` = room.`id`
SET room.`status` = CASE
  WHEN COALESCE(occupancy.`occupied`, 0) >= room.`capacity` THEN 'FULL'
  ELSE 'NORMAL'
END
WHERE (room.`status` IS NULL OR room.`status` <> 'MAINTENANCE')
  AND room.`capacity` IS NOT NULL
  AND NOT (room.`status` <=> CASE
    WHEN COALESCE(occupancy.`occupied`, 0) >= room.`capacity` THEN 'FULL'
    ELSE 'NORMAL'
  END);

-- 初始化测试数据（用户名唯一，重复时自动跳过）
INSERT IGNORE INTO `sys_user` (`username`, `password`, `role`, `name`, `gender`, `avatar`, `class_name`, `email`, `phone`) VALUES
('20240001', '$2b$12$fkepBhQatdh.trQqZmPZcuZwJhLFNz1I6DuLntDfPnQiv5YlaTRrC', 'student', '张伟', '男', '/images/avatar.jpg', '计科2201', 'stu001@stu.edu.cn', '13800010001'),
('manager1', '$2b$12$fkepBhQatdh.trQqZmPZcuZwJhLFNz1I6DuLntDfPnQiv5YlaTRrC', 'dormmanager', '王叔', '男', NULL, NULL, NULL, NULL),
('admin', '$2b$12$fkepBhQatdh.trQqZmPZcuZwJhLFNz1I6DuLntDfPnQiv5YlaTRrC', 'admin', '超级管理员', NULL, NULL, NULL, NULL, NULL);

-- 三条演示公告只声明一次，便于精确去重和幂等插入。
DROP TEMPORARY TABLE IF EXISTS `_seed_admin_notice`;
CREATE TEMPORARY TABLE `_seed_admin_notice` LIKE `business_record`;
INSERT INTO `_seed_admin_notice` (`type`, `title`, `description`, `status`, `creator_id`, `event_time`) VALUES
('admin_notice', '宿舍楼消防演练通知', '各位同学：\n兹定于本周六（8月1日）上午10:00进行宿舍楼消防疏散演练，届时将启动消防警报，请各位同学听到警报后有序撤离至楼下空地集合。\n注意事项：\n1. 请勿使用电梯\n2. 请随身携带湿毛巾\n3. 请勿嬉戏打闹\n请各寝室长做好组织工作。', '已发布', 3, '2026-07-28 10:00:00'),
('admin_notice', '关于暑假留校安排的通知', '根据学校暑假工作安排，暑假期间留校学生需在宿管处登记，办理留校手续。\n暑假期间宿舍楼开放时间调整为：早6:30-晚22:30。\n请同学们注意用电安全，严禁使用违规电器。', '已发布', 3, '2026-07-25 08:00:00'),
('admin_notice', '宿舍水电费缴纳提醒', '2026年7月份水电费已统计完毕，请各位同学及时在系统内查询并缴纳。\n缴费截止日期：2026年8月10日。\n逾期未缴者将影响宿舍用电。', '已发布', 3, '2026-07-20 14:00:00');

-- 只清理与上述种子内容完全相同的历史副本，保留最早的一条。
DELETE duplicate_notice
FROM `business_record` duplicate_notice
INNER JOIN `_seed_admin_notice` seed_notice
  ON seed_notice.`type` = duplicate_notice.`type`
  AND seed_notice.`title` = duplicate_notice.`title`
  AND seed_notice.`owner` <=> duplicate_notice.`owner`
  AND seed_notice.`description` = duplicate_notice.`description`
  AND seed_notice.`status` = duplicate_notice.`status`
  AND seed_notice.`creator_id` <=> duplicate_notice.`creator_id`
  AND seed_notice.`reply` <=> duplicate_notice.`reply`
  AND seed_notice.`event_time` <=> duplicate_notice.`event_time`
INNER JOIN `business_record` kept_notice
  ON kept_notice.`id` < duplicate_notice.`id`
  AND kept_notice.`type` = seed_notice.`type`
  AND kept_notice.`title` = seed_notice.`title`
  AND kept_notice.`owner` <=> seed_notice.`owner`
  AND kept_notice.`description` = seed_notice.`description`
  AND kept_notice.`status` = seed_notice.`status`
  AND kept_notice.`creator_id` <=> seed_notice.`creator_id`
  AND kept_notice.`reply` <=> seed_notice.`reply`
  AND kept_notice.`event_time` <=> seed_notice.`event_time`;

-- 初始化系统公告数据；存在完全相同的公告时不重复插入。
INSERT INTO `business_record` (`type`, `title`, `owner`, `description`, `status`, `creator_id`, `reply`, `event_time`)
SELECT seed_notice.`type`, seed_notice.`title`, seed_notice.`owner`, seed_notice.`description`,
       seed_notice.`status`, seed_notice.`creator_id`, seed_notice.`reply`, seed_notice.`event_time`
FROM `_seed_admin_notice` seed_notice
WHERE NOT EXISTS (
  SELECT 1
  FROM `business_record` existing_notice
  WHERE existing_notice.`type` = seed_notice.`type`
    AND existing_notice.`title` = seed_notice.`title`
    AND existing_notice.`owner` <=> seed_notice.`owner`
    AND existing_notice.`description` = seed_notice.`description`
    AND existing_notice.`status` = seed_notice.`status`
    AND existing_notice.`creator_id` <=> seed_notice.`creator_id`
    AND existing_notice.`reply` <=> seed_notice.`reply`
    AND existing_notice.`event_time` <=> seed_notice.`event_time`
);

DROP TEMPORARY TABLE `_seed_admin_notice`;
