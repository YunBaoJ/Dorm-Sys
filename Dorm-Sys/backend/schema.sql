CREATE DATABASE IF NOT EXISTS dormitory DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE dormitory;

CREATE TABLE `sys_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '账号',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `role` varchar(20) NOT NULL COMMENT '角色(student/dormmanager/admin)',
  `name` varchar(50) NOT NULL COMMENT '真实姓名',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像链接',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 初始化测试数据
INSERT INTO `sys_user` (`username`, `password`, `role`, `name`, `avatar`) VALUES
('student', '123456', 'student', '张伟', '/images/avatar.jpg'),
('dormmanager', '123456', 'dormmanager', '周强', NULL),
('admin', '123456', 'admin', '系统管理员', NULL);
