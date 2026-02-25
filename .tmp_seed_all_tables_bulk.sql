SET NAMES utf8mb4;

-- 1) 菜单与按钮
INSERT IGNORE INTO sys_menu (id, pid, menu_code, menu_name, menu_order, icon, url) VALUES
(120, 0, 'qaDemo', '测试演示', 99, 'flask', NULL),
(121, 120, 'qaDemoQuery', '测试查询', 1, 'search', '/qa/demo/query'),
(122, 120, 'qaDemoManage', '测试维护', 2, 'edit', '/qa/demo/manage');

INSERT IGNORE INTO sys_button (id, menu_id, btn_code, btn_name, method, uri) VALUES
(2001, 121, 'qaDemoQuery:list', '查询', 'GET', '/qa/demo/query'),
(2002, 121, 'qaDemoQuery:export', '导出', 'GET', '/qa/demo/export'),
(2003, 122, 'qaDemoManage:add', '新增', 'POST', '/qa/demo/add'),
(2004, 122, 'qaDemoManage:update', '修改', 'PUT', '/qa/demo/update'),
(2005, 122, 'qaDemoManage:delete', '删除', 'DELETE', '/qa/demo/delete');

-- 2) 部门
INSERT IGNORE INTO department
(id, dept_name, remark, dept_status, created_oper_name, created_at, updated_oper_name, updated_at, review_oper_name, review_time)
VALUES
(910001, '测试采购一部', '测试数据-采购', 'NORMAL', 'seed', NOW(), 'seed', NOW(), 'seed', NOW()),
(910002, '测试采购二部', '测试数据-采购', 'NORMAL', 'seed', NOW(), 'seed', NOW(), 'seed', NOW()),
(910003, '测试销售一部', '测试数据-销售', 'NORMAL', 'seed', NOW(), 'seed', NOW(), 'seed', NOW()),
(910004, '测试销售二部', '测试数据-销售', 'NORMAL', 'seed', NOW(), 'seed', NOW(), 'seed', NOW()),
(910005, '测试运营一部', '测试数据-运营', 'NORMAL', 'seed', NOW(), 'seed', NOW(), 'seed', NOW()),
(910006, '测试运营二部', '测试数据-运营', 'NORMAL', 'seed', NOW(), 'seed', NOW(), 'seed', NOW()),
(910007, '测试风控中心', '测试数据-风控', 'NORMAL', 'seed', NOW(), 'seed', NOW(), 'seed', NOW()),
(910008, '测试财务中心', '测试数据-财务', 'NORMAL', 'seed', NOW(), 'seed', NOW(), 'seed', NOW());

-- 3) 用户
INSERT IGNORE INTO `user`
(id, oper_code, oper_name, oper_status, phone, created_oper_name, created_at, updated_oper_name, updated_at, review_oper_name, review_time, user_type, tel_phone, remark, password, dept_id)
VALUES
(920001, 'TSTADM0001', '测试管理员01', 'NORMAL', '13800138001', 'seed', NOW(), 'seed', NOW(), 'seed', NOW(), 'DEPT_ADMIN', '021-60010001', '测试管理员', '123456', 910001),
(920002, 'TSTADM0002', '测试管理员02', 'NORMAL', '13800138002', 'seed', NOW(), 'seed', NOW(), 'seed', NOW(), 'DEPT_ADMIN', '021-60010002', '测试管理员', '123456', 910002),
(920003, 'TSTADM0003', 'RESET', 'RESET', '13800138003', 'seed', NOW(), 'seed', NOW(), 'seed', NOW(), 'DEPT_ADMIN', '021-60010003', '测试管理员', '123456', 910003),
(920011, 'TSTOPR0011', '测试操作员11', 'NORMAL', '13900138011', 'seed', NOW(), 'seed', NOW(), 'seed', NOW(), 'DEPT_OPERATOR', '021-61010011', '测试操作员', '123456', 910001),
(920012, 'TSTOPR0012', '测试操作员12', 'NORMAL', '13900138012', 'seed', NOW(), 'seed', NOW(), 'seed', NOW(), 'DEPT_OPERATOR', '021-61010012', '测试操作员', '123456', 910001),
(920013, 'TSTOPR0013', '测试操作员13', 'FROZEN', '13900138013', 'seed', NOW(), 'seed', NOW(), 'seed', NOW(), 'DEPT_OPERATOR', '021-61010013', '测试操作员', '123456', 910001),
(920014, 'TSTOPR0014', '测试操作员14', 'NORMAL', '13900138014', 'seed', NOW(), 'seed', NOW(), 'seed', NOW(), 'DEPT_OPERATOR', '021-61010014', '测试操作员', '123456', 910002),
(920015, 'TSTOPR0015', '测试操作员15', 'RESET', '13900138015', 'seed', NOW(), 'seed', NOW(), 'seed', NOW(), 'DEPT_OPERATOR', '021-61010015', '测试操作员', '123456', 910002),
(920016, 'TSTOPR0016', '测试操作员16', 'NORMAL', '13900138016', 'seed', NOW(), 'seed', NOW(), 'seed', NOW(), 'DEPT_OPERATOR', '021-61010016', '测试操作员', '123456', 910003),
(920017, 'TSTOPR0017', '测试操作员17', 'NORMAL', '13900138017', 'seed', NOW(), 'seed', NOW(), 'seed', NOW(), 'DEPT_OPERATOR', '021-61010017', '测试操作员', '123456', 910003),
(920018, 'TSTOPR0018', '测试操作员18', 'NORMAL', '13900138018', 'seed', NOW(), 'seed', NOW(), 'seed', NOW(), 'DEPT_OPERATOR', '021-61010018', '测试操作员', '123456', 910004),
(920019, 'TSTOPR0019', '测试操作员19', 'NORMAL', '13900138019', 'seed', NOW(), 'seed', NOW(), 'seed', NOW(), 'DEPT_OPERATOR', '021-61010019', '测试操作员', '123456', 910004),
(920020, 'TSTOPR0020', '测试操作员20', 'NORMAL', '13900138020', 'seed', NOW(), 'seed', NOW(), 'seed', NOW(), 'DEPT_OPERATOR', '021-61010020', '测试操作员', '123456', 910005),
(920021, 'TSTOPR0021', '测试操作员21', 'NORMAL', '13900138021', 'seed', NOW(), 'seed', NOW(), 'seed', NOW(), 'DEPT_OPERATOR', '021-61010021', '测试操作员', '123456', 910005),
(920022, 'TSTOPR0022', '测试操作员22', 'NORMAL', '13900138022', 'seed', NOW(), 'seed', NOW(), 'seed', NOW(), 'DEPT_OPERATOR', '021-61010022', '测试操作员', '123456', 910006),
(920023, 'TSTOPR0023', '测试操作员23', 'NORMAL', '13900138023', 'seed', NOW(), 'seed', NOW(), 'seed', NOW(), 'DEPT_OPERATOR', '021-61010023', '测试操作员', '123456', 910006),
(920024, 'TSTOPR0024', '测试操作员24', 'NORMAL', '13900138024', 'seed', NOW(), 'seed', NOW(), 'seed', NOW(), 'DEPT_OPERATOR', '021-61010024', '测试操作员', '123456', 910007),
(920025, 'TSTOPR0025', '测试操作员25', 'NORMAL', '13900138025', 'seed', NOW(), 'seed', NOW(), 'seed', NOW(), 'DEPT_OPERATOR', '021-61010025', '测试操作员', '123456', 910007),
(920026, 'TSTOPR0026', '测试操作员26', 'NORMAL', '13900138026', 'seed', NOW(), 'seed', NOW(), 'seed', NOW(), 'DEPT_OPERATOR', '021-61010026', '测试操作员', '123456', 910008),
(920027, 'TSTOPR0027', '测试操作员27', 'NORMAL', '13900138027', 'seed', NOW(), 'seed', NOW(), 'seed', NOW(), 'DEPT_OPERATOR', '021-61010027', '测试操作员', '123456', 910008);

-- 4) 部门申请
INSERT IGNORE INTO department_apply
(id, arr_no, dept_id, dept_name, remark, oper_type, oper_status, oper_code, oper_name, arr_date, review_oper_code, review_oper_name, review_time)
VALUES
(940001, 'DAP202602120001', 910001, '测试采购一部', '新增申请', 'ADD', 'APPROVED', 920001, '测试管理员01', NOW() - INTERVAL 12 DAY, 920002, '测试管理员02', NOW() - INTERVAL 11 DAY),
(940002, 'DAP202602120002', 910002, '测试采购二部', '新增申请', 'ADD', 'APPROVED', 920002, '测试管理员02', NOW() - INTERVAL 10 DAY, 920001, '测试管理员01', NOW() - INTERVAL 9 DAY),
(940003, 'DAP202602120003', 910003, '测试销售一部', '信息更新', 'MODIFY', 'PENDING', 920001, '测试管理员01', NOW() - INTERVAL 3 DAY, NULL, NULL, NULL),
(940004, 'DAP202602120004', 910004, '测试销售二部', '信息更新', 'MODIFY', 'REJECTED', 920002, '测试管理员02', NOW() - INTERVAL 4 DAY, 920001, '测试管理员01', NOW() - INTERVAL 3 DAY),
(940005, 'DAP202602120005', 910005, '测试运营一部', '申请注销', 'CANCEL', 'PENDING', 920001, '测试管理员01', NOW() - INTERVAL 1 DAY, NULL, NULL, NULL),
(940006, 'DAP202602120006', 910006, '测试运营二部', '申请注销', 'CANCEL', 'APPROVED', 920002, '测试管理员02', NOW() - INTERVAL 6 DAY, 920001, '测试管理员01', NOW() - INTERVAL 5 DAY);

-- 5) 岗位
INSERT IGNORE INTO post
(id, dept_id, dept_name, post_name, post_type, remark, post_status, created_oper_name, created_at, updated_oper_name, updated_at, review_oper_name, review_time)
VALUES
(930001, 910001, '测试采购一部', '采购专员A', 'INTERNAL', '测试岗位', 'NORMAL', 'seed', NOW(), 'seed', NOW(), 'seed', NOW()),
(930002, 910001, '测试采购一部', '采购专员B', 'INTERNAL', '测试岗位', 'NORMAL', 'seed', NOW(), 'seed', NOW(), 'seed', NOW()),
(930003, 910002, '测试采购二部', '采购主管A', 'INTERNAL', '测试岗位', 'NORMAL', 'seed', NOW(), 'seed', NOW(), 'seed', NOW()),
(930004, 910002, '测试采购二部', '采购主管B', 'INTERNAL', '测试岗位', 'NORMAL', 'seed', NOW(), 'seed', NOW(), 'seed', NOW()),
(930005, 910003, '测试销售一部', '销售经理A', 'INTERNAL', '测试岗位', 'NORMAL', 'seed', NOW(), 'seed', NOW(), 'seed', NOW()),
(930006, 910003, '测试销售一部', '销售经理B', 'INTERNAL', '测试岗位', 'NORMAL', 'seed', NOW(), 'seed', NOW(), 'seed', NOW()),
(930007, 910004, '测试销售二部', '销售代表A', 'INTERNAL', '测试岗位', 'NORMAL', 'seed', NOW(), 'seed', NOW(), 'seed', NOW()),
(930008, 910004, '测试销售二部', '销售代表B', 'INTERNAL', '测试岗位', 'NORMAL', 'seed', NOW(), 'seed', NOW(), 'seed', NOW()),
(930009, 910005, '测试运营一部', '运营专员A', 'INTERNAL', '测试岗位', 'NORMAL', 'seed', NOW(), 'seed', NOW(), 'seed', NOW()),
(930010, 910005, '测试运营一部', '运营专员B', 'INTERNAL', '测试岗位', 'NORMAL', 'seed', NOW(), 'seed', NOW(), 'seed', NOW()),
(930011, 910006, '测试运营二部', '运营主管A', 'INTERNAL', '测试岗位', 'NORMAL', 'seed', NOW(), 'seed', NOW(), 'seed', NOW()),
(930012, 910006, '测试运营二部', '运营主管B', 'INTERNAL', '测试岗位', 'NORMAL', 'seed', NOW(), 'seed', NOW(), 'seed', NOW()),
(930013, 910007, '测试风控中心', '风控专员A', 'INTERNAL', '测试岗位', 'NORMAL', 'seed', NOW(), 'seed', NOW(), 'seed', NOW()),
(930014, 910007, '测试风控中心', '风控专员B', 'INTERNAL', '测试岗位', 'NORMAL', 'seed', NOW(), 'seed', NOW(), 'seed', NOW()),
(930015, 910008, '测试财务中心', '财务专员A', 'INTERNAL', '测试岗位', 'NORMAL', 'seed', NOW(), 'seed', NOW(), 'seed', NOW()),
(930016, 910008, '测试财务中心', '财务专员B', 'INTERNAL', '测试岗位', 'NORMAL', 'seed', NOW(), 'seed', NOW(), 'seed', NOW());

-- 6) 岗位用户关系
INSERT IGNORE INTO post_user (id, post_id, oper_code, created_at) VALUES
(950001, 930001, 920011, NOW()),
(950002, 930002, 920012, NOW()),
(950003, 930003, 920014, NOW()),
(950004, 930004, 920015, NOW()),
(950005, 930005, 920016, NOW()),
(950006, 930006, 920017, NOW()),
(950007, 930007, 920018, NOW()),
(950008, 930008, 920019, NOW()),
(950009, 930009, 920020, NOW()),
(950010, 930010, 920021, NOW()),
(950011, 930011, 920022, NOW()),
(950012, 930012, 920023, NOW()),
(950013, 930013, 920024, NOW()),
(950014, 930014, 920025, NOW()),
(950015, 930015, 920026, NOW()),
(950016, 930016, 920027, NOW());

-- 7) 岗位申请
INSERT IGNORE INTO post_apply
(id, arr_no, post_id, dept_id, dept_name, post_name, remark, post_type, oper_type, post_status, oper_code, oper_name, arr_date, review_oper_code, review_oper_name, review_time)
VALUES
(960001, 'PAP202602120001', 930001, 910001, '测试采购一部', '采购专员A', '新增岗位申请', 'INTERNAL', 'ADD', 'APPROVED', 920001, '测试管理员01', NOW() - INTERVAL 8 DAY, 920002, '测试管理员02', NOW() - INTERVAL 7 DAY),
(960002, 'PAP202602120002', 930002, 910001, '测试采购一部', '采购专员B', '修改岗位备注', 'INTERNAL', 'MODIFY', 'PENDING', 920001, '测试管理员01', NOW() - INTERVAL 1 DAY, NULL, NULL, NULL),
(960003, 'PAP202602120003', 930003, 910002, '测试采购二部', '采购主管A', '注销岗位', 'INTERNAL', 'CANCEL', 'APPROVED', 920002, '测试管理员02', NOW() - INTERVAL 5 DAY, 920001, '测试管理员01', NOW() - INTERVAL 4 DAY),
(960004, 'PAP202602120004', 930004, 910002, '测试采购二部', '采购主管B', '新增岗位申请', 'INTERNAL', 'ADD', 'REJECTED', 920002, '测试管理员02', NOW() - INTERVAL 6 DAY, 920001, '测试管理员01', NOW() - INTERVAL 5 DAY),
(960005, 'PAP202602120005', 930005, 910003, '测试销售一部', '销售经理A', '新增岗位申请', 'INTERNAL', 'ADD', 'PENDING', 920001, '测试管理员01', NOW() - INTERVAL 2 DAY, NULL, NULL, NULL),
(960006, 'PAP202602120006', 930006, 910003, '测试销售一部', '销售经理B', '修改岗位备注', 'INTERNAL', 'MODIFY', 'APPROVED', 920001, '测试管理员01', NOW() - INTERVAL 9 DAY, 920002, '测试管理员02', NOW() - INTERVAL 8 DAY);

-- 8) 操作员申请
INSERT IGNORE INTO operator_apply
(id, arr_no, dept_id, dept_name, tel_phone, mobile, remark, oper_type, oper_status, oper_code, oper_name, arr_date, review_oper_code, review_oper_name, review_time)
VALUES
(970001, 'OAP202602120001', 910001, '测试采购一部', '021-61010011', '13900138011', '新增操作员', 'ADD', 'APPROVED', 920001, '测试管理员01', NOW() - INTERVAL 10 DAY, 920002, '测试管理员02', NOW() - INTERVAL 9 DAY),
(970002, 'OAP202602120002', 910001, '测试采购一部', '021-61010012', '13900138012', '修改操作员', 'MODIFY', 'PENDING', 920001, '测试管理员01', NOW() - INTERVAL 1 DAY, NULL, NULL, NULL),
(970003, 'OAP202602120003', 910002, '测试采购二部', '021-61010014', '13900138014', '冻结操作员', 'FREEZE', 'APPROVED', 920002, '测试管理员02', NOW() - INTERVAL 7 DAY, 920001, '测试管理员01', NOW() - INTERVAL 6 DAY),
(970004, 'OAP202602120004', 910003, '测试销售一部', '021-61010016', '13900138016', '重置密码', 'RESET_PASSWORD', 'REJECTED', 920001, '测试管理员01', NOW() - INTERVAL 4 DAY, 920002, '测试管理员02', NOW() - INTERVAL 3 DAY),
(970005, 'OAP202602120005', 910004, '测试销售二部', '021-61010018', '13900138018', '分配权限', 'ASSIGN_PERMISSION', 'PENDING', 920002, '测试管理员02', NOW() - INTERVAL 2 DAY, NULL, NULL, NULL),
(970006, 'OAP202602120006', 910005, '测试运营一部', '021-61010020', '13900138020', '注销操作员', 'CANCEL', 'APPROVED', 920001, '测试管理员01', NOW() - INTERVAL 8 DAY, 920002, '测试管理员02', NOW() - INTERVAL 7 DAY);

-- 9) 管理员申请
INSERT IGNORE INTO admin_apply
(id, arr_no, oper_type, tel_phone, mobile, remark, operation_type, oper_status, oper_code, oper_name, dept_id, arr_date, review_oper_code, review_oper_name, review_time)
VALUES
(980001, 'AAP202602120001', 'DEPT_ADMIN', '021-60010001', '13800138001', '新增管理员', 'ADD', 'APPROVED', 920001, '测试管理员01', 910001, NOW() - INTERVAL 12 DAY, 920002, '测试管理员02', NOW() - INTERVAL 11 DAY),
(980002, 'AAP202602120002', 'DEPT_ADMIN', '021-60010002', '13800138002', '修改管理员', 'MODIFY', 'PENDING', 920002, '测试管理员02', 910002, NOW() - INTERVAL 1 DAY, NULL, NULL, NULL),
(980003, 'AAP202602120003', 'DEPT_ADMIN', '021-60010003', '13800138003', '冻结管理员', 'FREEZE', 'APPROVED', 920003, 'RESET', 910003, NOW() - INTERVAL 7 DAY, 920001, '测试管理员01', NOW() - INTERVAL 6 DAY),
(980004, 'AAP202602120004', 'DEPT_ADMIN', '021-60010001', '13800138001', '重置密码', 'RESET_PASSWORD', 'REJECTED', 920001, '测试管理员01', 910001, NOW() - INTERVAL 3 DAY, 920002, '测试管理员02', NOW() - INTERVAL 2 DAY);

-- 10) 请求日志
INSERT IGNORE INTO request_log
(id, request_id, method, path, query_string, handler, args, status_code, success, error_message, cost_ms, client_ip, user_agent, created_at)
VALUES
(990001, 'seed-req-0001', 'GET', '/api/internal-positions', 'deptId=910001', 'InternalPositionController#queryPositions', '{}', 200, 1, NULL, 23, '127.0.0.1', 'seed-agent', NOW() - INTERVAL 3 DAY),
(990002, 'seed-req-0002', 'GET', '/api/department-applications', 'statusType=PENDING', 'DepartmentApplyController#queryApplies', '{}', 200, 1, NULL, 31, '127.0.0.1', 'seed-agent', NOW() - INTERVAL 2 DAY),
(990003, 'seed-req-0003', 'POST', '/api/auth/login', NULL, 'AuthController#login', '{}', 200, 1, NULL, 18, '127.0.0.1', 'seed-agent', NOW() - INTERVAL 1 DAY),
(990004, 'seed-req-0004', 'POST', '/api/department-admin-applications/1/review', NULL, 'DepartmentAdminApplyController#reviewApply', '{}', 400, 0, '参数错误', 27, '127.0.0.1', 'seed-agent', NOW() - INTERVAL 1 DAY),
(990005, 'seed-req-0005', 'GET', '/api/permissions/tree', NULL, 'PermissionController#queryPermissionTree', '{}', 200, 1, NULL, 12, '127.0.0.1', 'seed-agent', NOW());

-- 11) 岗位按钮关系
INSERT IGNORE INTO post_btn (id, post_id, btn_id, created_at) VALUES
(991001, 930001, 1013, NOW()), (991002, 930001, 1014, NOW()),
(991003, 930002, 1015, NOW()), (991004, 930002, 1016, NOW()),
(991005, 930003, 1017, NOW()), (991006, 930003, 1018, NOW()),
(991007, 930004, 1019, NOW()), (991008, 930004, 1020, NOW()),
(991009, 930005, 1021, NOW()), (991010, 930005, 1022, NOW()),
(991011, 930006, 1023, NOW()), (991012, 930006, 1024, NOW()),
(991013, 930007, 2001, NOW()), (991014, 930008, 2002, NOW());

-- 12) 部门按钮关系
INSERT IGNORE INTO dept_btn (id, dept_id, btn_id, created_at) VALUES
(992001, 910001, 1001, NOW()), (992002, 910001, 1002, NOW()), (992003, 910001, 1003, NOW()),
(992004, 910002, 1004, NOW()), (992005, 910002, 1005, NOW()), (992006, 910002, 1006, NOW()),
(992007, 910003, 1007, NOW()), (992008, 910003, 1008, NOW()), (992009, 910003, 1009, NOW()),
(992010, 910004, 1010, NOW()), (992011, 910004, 1011, NOW()), (992012, 910004, 1012, NOW()),
(992013, 910005, 2001, NOW()), (992014, 910005, 2002, NOW()), (992015, 910005, 2003, NOW()),
(992016, 910006, 2004, NOW()), (992017, 910006, 2005, NOW());