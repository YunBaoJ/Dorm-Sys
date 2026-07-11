CREATE TABLE IF NOT EXISTS `business_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `type` varchar(50) NOT NULL COMMENT '业务类型',
  `title` varchar(100) NOT NULL COMMENT '标题',
  `owner` varchar(100) DEFAULT NULL COMMENT '对象/联系人/位置',
  `description` text COMMENT '说明',
  `status` varchar(30) DEFAULT NULL COMMENT '状态',
  `creator_id` bigint(20) DEFAULT NULL COMMENT '创建人ID',
  `event_time` datetime DEFAULT NULL COMMENT '业务时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_business_record_type` (`type`),
  KEY `idx_business_record_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通用业务记录表';
