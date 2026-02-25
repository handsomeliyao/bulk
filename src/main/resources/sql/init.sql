DROP TABLE IF EXISTS `department`;
CREATE TABLE `department`
(
    `id`         bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `dept_name`  varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '部门名称',
    `remark`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
    `dept_status`     varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '状态',
    `created_oper_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
    `created_at` datetime                                                      NOT NULL COMMENT '创建时间',
    `updated_oper_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
    `updated_at` datetime                                                      NOT NULL COMMENT '更新时间',
    `review_oper_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '复核人',
    `review_time` datetime                                                      DEFAULT NULL COMMENT '复核时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_department_name` (`dept_name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='部门表';

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`
(
    `id`          bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `oper_code`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '用户名',
    `oper_name`   varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户姓名',
    `oper_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '状态',
    `phone`       varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL COMMENT '电话',
    `created_oper_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
    `created_at`  datetime                                                      NOT NULL COMMENT '创建时间',
    `updated_oper_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
    `updated_at` datetime                                                      DEFAULT NULL COMMENT '更新时间',
    `review_oper_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '复核人',
    `review_time` datetime                                                      DEFAULT NULL COMMENT '复核时间',
    `user_type`   varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL COMMENT '用户类型',
    `tel_phone`   varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL COMMENT '办公电话',
    `remark`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
    `password`    varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL COMMENT '用户密码',
    `dept_id`     bigint                                                        DEFAULT NULL COMMENT '部门ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_oper_code` (`oper_code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='用户表';

INSERT IGNORE INTO `user` (`oper_code`, `oper_name`, `oper_status`, `phone`, `created_oper_name`,
                           `created_at`, `updated_oper_name`, `updated_at`, `review_oper_name`,
                           `review_time`, `user_type`, `tel_phone`, `remark`, `password`, `dept_id`)
VALUES ('TSTADM0001', '测试管理员', 'NORMAL', NULL, 'system',
        NOW(), NULL, NULL, NULL,
        NULL, 'ADMIN', NULL, 'seed admin', '123456', NULL);


DROP TABLE IF EXISTS `department_apply`;
CREATE TABLE `department_apply`
(
    `id`               bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `arr_no`           varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '申请单号',
    `dept_id`          bigint                                                        DEFAULT NULL COMMENT '部门ID',
    `dept_name`        varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '申请部门名称',
    `remark`           varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '申请备注',
    `oper_type`        varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '操作类型',
    `arr_status`           varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '申请状态',
    `arr_oper_code`        varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '申请人编码',
    `arr_oper_name`        varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '申请人姓名',
    `arr_date`         datetime                                                      NOT NULL COMMENT '申请时间',
    `review_oper_code` bigint                                                        DEFAULT NULL COMMENT '审核人ID',
    `review_oper_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '审核人姓名',
    `review_time`      datetime                                                      DEFAULT NULL COMMENT '审核时间',
    `dept_status`           varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '部门状态',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_department_apply_no` (`arr_no`),
    KEY `idx_department_apply_dept` (`dept_id`),
    KEY `idx_department_apply_status` (`arr_status`),
    KEY `idx_department_apply_time` (`arr_date`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='部门申请表';

DROP TABLE IF EXISTS `post`;
CREATE TABLE `post`
(
    `id`          bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `dept_id`     bigint                                                        NOT NULL COMMENT '部门ID',
    `dept_name`   varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '部门名称',
    `post_name`   varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '岗位名称',
    `post_type`   varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '岗位类型',
    `remark`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
    `post_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '状态',
    `created_oper_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
    `created_at`  datetime                                                      NOT NULL COMMENT '创建时间',
    `updated_oper_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
    `updated_at`  datetime                                                      NOT NULL COMMENT '更新时间',
    `review_oper_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '复核人',
    `review_time` datetime                                                      DEFAULT NULL COMMENT '复核时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='岗位表';


DROP TABLE IF EXISTS `post_user`;
CREATE TABLE `post_user`
(
    `id`         bigint   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `post_id`    bigint   NOT NULL COMMENT '岗位ID',
    `oper_code`  bigint   NOT NULL COMMENT '用户ID',
    `created_at` datetime NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_post_user` (`post_id`, `oper_code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='岗位用户关系表';

DROP TABLE IF EXISTS `post_apply`;
CREATE TABLE `post_apply`
(
    `id`               bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `arr_no`           varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '申请单号',
    `post_id`          bigint                                                        DEFAULT NULL COMMENT '岗位ID',
    `dept_id`          bigint                                                        NOT NULL COMMENT '部门ID',
    `dept_name`        varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '部门名称',
    `post_name`        varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '申请岗位名称',
    `remark`           varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '申请备注',
    `post_type`        varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '岗位类型',
    `oper_type`        varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '操作类型',
    `post_status`           varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '状态',
    `arr_oper_code`        bigint                                                        NOT NULL COMMENT '申请人ID',
    `arr_oper_name`        varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '申请人姓名',
    `arr_date`         datetime                                                      NOT NULL COMMENT '申请时间',
    `review_oper_code` bigint                                                        DEFAULT NULL COMMENT '审核人ID',
    `review_oper_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '审核人姓名',
    `review_time`      datetime                                                      DEFAULT NULL COMMENT '审核时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_position_apply_no` (`arr_no`),
    KEY `idx_position_apply_post` (`post_id`),
    KEY `idx_position_apply_status` (`post_status`),
    KEY `idx_position_apply_time` (`arr_date`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='岗位申请表';

DROP TABLE IF EXISTS `operator_apply`;
CREATE TABLE `operator_apply`
(
    `id`               bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `arr_no`           varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '申请单号',
    `dept_id`          bigint                                                        NOT NULL COMMENT '部门ID',
    `dept_name`        varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '部门名称',
    `tel_phone`        varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL COMMENT '办公电话',
    `mobile`           varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL COMMENT '手机号码',
    `remark`           varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
    `oper_type`        varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '操作类型',
    `oper_status`      varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '状态',
    `arr_oper_code`        bigint                                                        NOT NULL COMMENT '申请人ID',
    `arr_oper_name`        varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '申请人姓名',
    `arr_date`         datetime                                                      NOT NULL COMMENT '申请时间',
    `review_oper_code` bigint                                                        DEFAULT NULL COMMENT '审核人ID',
    `review_oper_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '审核人姓名',
    `review_time`      datetime                                                      DEFAULT NULL COMMENT '审核时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_operator_apply_no` (`arr_no`),
    KEY `idx_operator_apply_user` (`arr_oper_code`),
    KEY `idx_operator_apply_status` (`oper_status`),
    KEY `idx_operator_apply_time` (`arr_date`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='操作员申请表';

DROP TABLE IF EXISTS `admin_apply`;
CREATE TABLE `admin_apply`
(
    `id`               bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `arr_no`           varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '申请单号',
    `oper_type`        varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '用户类型',
    `tel_phone`        varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL COMMENT '办公电话',
    `mobile`           varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL COMMENT '手机号码',
    `remark`           varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
    `operation_type`   varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '操作类型',
    `oper_status`      varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '状态',
    `arr_oper_code`        bigint                                                        NOT NULL COMMENT '申请人ID',
    `arr_oper_name`        varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '申请人姓名',
    `dept_id`          bigint                                                        DEFAULT NULL COMMENT '申请人部门ID',
    `arr_date`         datetime                                                      NOT NULL COMMENT '申请时间',
    `review_oper_code` bigint                                                        DEFAULT NULL COMMENT '审核人ID',
    `review_oper_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '审核人姓名',
    `review_time`      datetime                                                      DEFAULT NULL COMMENT '审核时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_admin_apply_no` (`arr_no`),
    KEY `idx_admin_apply_user` (`arr_oper_code`),
    KEY `idx_admin_apply_status` (`oper_status`),
    KEY `idx_admin_apply_time` (`arr_date`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='管理员申请表';


DROP TABLE IF EXISTS `request_log`;
CREATE TABLE `request_log`
(
    `id`            bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `request_id`    varchar(64)  DEFAULT NULL COMMENT '请求ID',
    `method`        varchar(16)  NOT NULL COMMENT '请求方法',
    `path`          varchar(255) NOT NULL COMMENT '请求路径',
    `query_string`  text COMMENT '查询参数',
    `handler`       varchar(200) DEFAULT NULL COMMENT '处理方法',
    `args`          text COMMENT '参数摘要',
    `status_code`   int          DEFAULT NULL COMMENT '响应状态码',
    `success`       tinyint(1)   NOT NULL COMMENT '是否成功',
    `error_message` text COMMENT '错误信息',
    `cost_ms`       bigint       NOT NULL COMMENT '耗时毫秒',
    `client_ip`     varchar(64)  DEFAULT NULL COMMENT '客户端IP',
    `user_agent`    varchar(512) DEFAULT NULL COMMENT 'User-Agent',
    `created_at`    datetime     NOT NULL COMMENT '请求时间',
    PRIMARY KEY (`id`),
    KEY `idx_request_log_request_id` (`request_id`),
    KEY `idx_request_log_created_at` (`created_at`),
    KEY `idx_request_log_path` (`path`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='请求日志表';

DROP TABLE IF EXISTS `post_btn`;
CREATE TABLE `post_btn`
(
    `id`         bigint   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `post_id`    bigint   NOT NULL COMMENT '岗位ID',
    `btn_id`     bigint   NOT NULL COMMENT '按钮ID',
    `created_at` datetime NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_post_menu_btn` (`post_id`, `btn_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='岗位菜单按钮关系';

DROP TABLE IF EXISTS `dept_btn`;
CREATE TABLE `dept_btn`
(
    `id`         bigint   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `dept_id`    bigint   NOT NULL COMMENT '部门ID',
    `btn_id`     bigint   NOT NULL COMMENT '按钮ID',
    `created_at` datetime NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dept_menu_btn` (`dept_id`, `btn_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='部门菜单按钮关系';

DROP TABLE IF EXISTS `sys_button`;
CREATE TABLE `sys_button`
(
    `id`       bigint                                                        NOT NULL COMMENT '主键',
    `menu_id`  bigint                                                        NOT NULL COMMENT '菜单ID',
    `btn_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '按钮编码',
    `btn_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '按钮名称',
    `method`   varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '请求方法',
    `uri`      varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '请求路径',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='按钮';

DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`
(
    `id`         bigint                                                        NOT NULL COMMENT '主键',
    `pid`        bigint                                                        NOT NULL COMMENT '父ID',
    `menu_code`  varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '菜单编码',
    `menu_name`  varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单名称',
    `menu_order` int                                                           NOT NULL COMMENT '菜单排序',
    `icon`       varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL COMMENT '图标',
    `url`        varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '菜单访问地址',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='菜单';


-- auto-generated sys_menu/sys_button data from doc/permission.txt
INSERT INTO `sys_menu` (`id`, `pid`, `menu_code`, `menu_name`, `menu_order`, `icon`, `url`) VALUES
(1, 0, 'department', '部门管理', 1, 'tree', NULL),
(11, 1, 'departmentQuery', '查询', 1, 'tree', '/department/query'),
(12, 1, 'departmentReview', '待复核', 2, 'tree', '/department/review'),
(13, 1, 'departmentReviewed', '已复核', 3, 'tree', '/department/reviewed'),
(2, 0, 'post', '岗位管理', 2, 'tree', NULL),
(21, 2, 'postQuery', '查询', 1, 'tree', '/post/query'),
(22, 2, 'postReview', '待复核', 2, 'tree', '/post/review'),
(23, 2, 'postReviewed', '已复核', 3, 'tree', '/post/reviewed'),
(3, 0, 'userAdmin', '用户管理员管理', 3, 'tree', NULL),
(31, 3, 'userAdminQuery', '查询', 1, 'tree', '/userAdmin/query'),
(32, 3, 'userAdminReview', '待复核', 2, 'tree', '/userAdmin/review'),
(33, 3, 'userAdminReviewed', '已复核', 3, 'tree', '/userAdmin/reviewed'),
(4, 0, 'userOperate', '用户操作员管理', 4, 'tree', NULL),
(41, 4, 'userOperateQuery', '查询', 1, 'tree', '/userOperate/query'),
(42, 4, 'userOperateReview', '待复核', 2, 'tree', '/userOperate/review'),
(43, 4, 'userOperateReviewed', '已复核', 3, 'tree', '/userOperate/reviewed');

INSERT INTO `sys_button` (`id`, `menu_id`, `btn_code`, `btn_name`, `method`, `uri`) VALUES
(1001, 11, 'departmentQuery:query', '查询', 'POST', '/department/query'),
(1002, 11, 'departmentQuery:add', '新增', 'POST', '/department/add'),
(1003, 11, 'departmentQuery:update', '修改', 'POST', '/department/update'),
(1004, 11, 'departmentQuery:detail', '详情', 'POST', '/department/detail'),
(1005, 11, 'departmentQuery:export', '下载', 'POST', '/department/export'),
(1006, 11, 'departmentQuery:logout', '注销', 'POST', '/department/logout'),
(1007, 11, 'departmentQuery:userQuery', '用户查询', 'POST', '/department/userQuery'),
(1008, 12, 'departmentReview:query', '查询', 'POST', '/department/review/query'),
(1009, 12, 'departmentReview:review', '复核', 'POST', '/department/review/review'),
(1010, 12, 'departmentReview:export', '下载', 'POST', '/department/review/export'),
(1011, 13, 'departmentReviewed:query', '查询', 'POST', '/department/reviewed/query'),
(1012, 13, 'departmentReviewed:export', '下载', 'POST', '/department/reviewed/export'),
(1013, 21, 'postQuery:query', '查询', 'POST', '/post/query'),
(1014, 21, 'postQuery:add', '新增', 'POST', '/post/add'),
(1015, 21, 'postQuery:update', '修改', 'POST', '/post/update'),
(1016, 21, 'postQuery:detail', '详情', 'POST', '/post/detail'),
(1017, 21, 'postQuery:export', '下载', 'POST', '/post/export'),
(1018, 21, 'postQuery:logout', '注销', 'POST', '/post/logout'),
(1019, 21, 'postQuery:userQuery', '用户查询', 'POST', '/post/userQuery'),
(1020, 22, 'postReview:query', '查询', 'POST', '/post/review/query'),
(1021, 22, 'postReview:review', '复核', 'POST', '/post/review/review'),
(1022, 22, 'postReview:export', '下载', 'POST', '/post/review/export'),
(1023, 23, 'postReviewed:query', '查询', 'POST', '/post/reviewed/query'),
(1024, 23, 'postReviewed:export', '下载', 'POST', '/post/reviewed/export'),
(1025, 31, 'userAdminQuery:query', '查询', 'POST', '/userAdmin/query'),
(1026, 31, 'userAdminQuery:add', '新增', 'POST', '/userAdmin/add'),
(1027, 31, 'userAdminQuery:update', '修改', 'POST', '/userAdmin/update'),
(1028, 31, 'userAdminQuery:detail', '详情', 'POST', '/userAdmin/detail'),
(1029, 31, 'userAdminQuery:export', '下载', 'POST', '/userAdmin/export'),
(1030, 31, 'userAdminQuery:logout', '注销', 'POST', '/userAdmin/logout'),
(1031, 31, 'userAdminQuery:freeze', '冻结', 'POST', '/userAdmin/freeze'),
(1032, 31, 'userAdminQuery:unfreeze', '解冻', 'POST', '/userAdmin/unfreeze'),
(1033, 31, 'userAdminQuery:pwdReset', '密码重置', 'POST', '/userAdmin/pwdReset'),
(1034, 31, 'userAdminQuery:bindCert', '绑定证书', 'POST', '/userAdmin/bindCert'),
(1035, 32, 'userAdminReview:query', '查询', 'POST', '/userAdmin/review/query'),
(1036, 32, 'userAdminReview:review', '复核', 'POST', '/userAdmin/review/review'),
(1037, 32, 'userAdminReview:export', '下载', 'POST', '/userAdmin/review/export'),
(1038, 33, 'userAdminReviewed:query', '查询', 'POST', '/userAdmin/reviewed/query'),
(1039, 33, 'userAdminReviewed:export', '下载', 'POST', '/userAdmin/reviewed/export'),
(1040, 41, 'userOperateQuery:query', '查询', 'POST', '/userOperate/query'),
(1041, 41, 'userOperateQuery:add', '新增', 'POST', '/userOperate/add'),
(1042, 41, 'userOperateQuery:update', '修改', 'POST', '/userOperate/update'),
(1043, 41, 'userOperateQuery:detail', '详情', 'POST', '/userOperate/detail'),
(1044, 41, 'userOperateQuery:export', '下载', 'POST', '/userOperate/export'),
(1045, 41, 'userOperateQuery:logout', '注销', 'POST', '/userOperate/logout'),
(1046, 41, 'userOperateQuery:freeze', '冻结', 'POST', '/userOperate/freeze'),
(1047, 41, 'userOperateQuery:unfreeze', '解冻', 'POST', '/userOperate/unfreeze'),
(1048, 41, 'userOperateQuery:pwdReset', '密码重置', 'POST', '/userOperate/pwdReset'),
(1049, 41, 'userOperateQuery:bindCert', '绑定证书', 'POST', '/userOperate/bindCert'),
(1050, 41, 'userOperateQuery:assignPermission', '分配权限', 'POST', '/userOperate/assignPermission'),
(1051, 42, 'userOperateReview:query', '查询', 'POST', '/userOperate/review/query'),
(1052, 42, 'userOperateReview:review', '复核', 'POST', '/userOperate/review/review'),
(1053, 42, 'userOperateReview:export', '下载', 'POST', '/userOperate/review/export'),
(1054, 43, 'userOperateReviewed:query', '查询', 'POST', '/userOperate/reviewed/query'),
(1055, 43, 'userOperateReviewed:export', '下载', 'POST', '/userOperate/reviewed/export');

INSERT INTO `user` (
    `id`,
    `oper_code`,
    `oper_name`,
    `oper_status`,
    `phone`,
    `created_oper_name`,
    `created_at`,
    `updated_oper_name`,
    `updated_at`,
    `review_oper_name`,
    `review_time`,
    `user_type`,
    `tel_phone`,
    `remark`,
    `password`,
    `dept_id`
) VALUES
      (990001, 'supper01', '超级管理员01', 'NORMAL', '13800000001', 'system', NOW(), 'system', NOW(), 'system', NOW(), 'ADMIN', '021-60000001', '超级管理员测试数据01', '123456', 910001),
      (990002, 'supper02', '超级管理员02', 'NORMAL', '13800000002', 'system', NOW(), 'system', NOW(), 'system', NOW(), 'ADMIN', '021-60000002', '超级管理员测试数据02', '123456', 910001),
      (990003, 'supper03', '超级管理员03', 'NORMAL', '13800000003', 'system', NOW(), 'system', NOW(), 'system', NOW(), 'ADMIN', '021-60000003', '超级管理员测试数据03', '123456', 910001),
      (990004, 'supper04', '超级管理员04', 'NORMAL', '13800000004', 'system', NOW(), 'system', NOW(), 'system', NOW(), 'ADMIN', '021-60000004', '超级管理员测试数据04', '123456', 910001);