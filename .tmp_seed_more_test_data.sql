-- cleanup (idempotent)
DELETE FROM admin_apply WHERE arr_no LIKE 'TST26_ADM_%';
DELETE FROM operator_apply WHERE arr_no LIKE 'TST26_OPR_%';
DELETE FROM post_apply WHERE arr_no LIKE 'TST26_POS_%';
DELETE FROM department_apply WHERE arr_no LIKE 'TST26_DEP_%';
DELETE FROM `user` WHERE oper_code LIKE 'TEST_USER_%';

-- user test data (all columns except auto id)
INSERT INTO `user`
(oper_code, oper_name, oper_status, phone, created_at, user_type, tel_phone, remark, password, dept_id)
VALUES
('TEST_USER_SUPER_01', '测试超级管理员01', 'NORMAL', '13800000001', '2026-01-20 09:00:00', 'SUPER_ADMIN', '021-60000001', '测试用户-超级管理员', 'Pwd@1001', 6),
('TEST_USER_ADMIN_01', '测试部门管理员01', 'NORMAL', '13800000002', '2026-01-20 09:10:00', 'DEPT_ADMIN', '021-60000002', '测试用户-部门管理员', 'Pwd@1002', 6),
('TEST_USER_ADMIN_02', '测试部门管理员02', 'RESET',  '13800000003', '2026-01-20 09:20:00', 'DEPT_ADMIN', '021-60000003', '测试用户-部门管理员', 'Pwd@1003', 7),
('TEST_USER_OP_01',    '测试部门操作员01', 'NORMAL', '13800000004', '2026-01-20 09:30:00', 'DEPT_OPERATOR', '021-60000004', '测试用户-部门操作员', 'Pwd@1004', 6),
('TEST_USER_OP_02',    '测试部门操作员02', 'FROZEN', '13800000005', '2026-01-20 09:40:00', 'DEPT_OPERATOR', '021-60000005', '测试用户-部门操作员', 'Pwd@1005', 7),
('TEST_USER_REVIEW_01','测试复核员01',     'NORMAL', '13800000006', '2026-01-20 09:50:00', 'SUPER_ADMIN', '021-60000006', '测试用户-复核',     'Pwd@1006', 9);

-- department_apply test data
INSERT INTO department_apply
(arr_no, dept_id, dept_name, remark, oper_type, status, oper_code, oper_name, arr_date, review_oper_code, review_oper_name, review_time)
SELECT 'TST26_DEP_0001', 6, 'TEST_DEPT_ALPHA', '测试部门新增申请', 'ADD', 'PENDING', u1.id, u1.oper_name, '2026-01-21 10:00:00', NULL, NULL, NULL
FROM `user` u1 WHERE u1.oper_code='TEST_USER_ADMIN_01'
UNION ALL
SELECT 'TST26_DEP_0002', 7, 'TEST_DEPT_BETA', '测试部门修改申请', 'MODIFY', 'APPROVED', u2.id, u2.oper_name, '2026-01-21 10:10:00', r.id, r.oper_name, '2026-01-21 11:00:00'
FROM `user` u2 JOIN `user` r ON r.oper_code='TEST_USER_REVIEW_01' WHERE u2.oper_code='TEST_USER_ADMIN_02'
UNION ALL
SELECT 'TST26_DEP_0003', 8, 'TEST_DEPT_GAMMA', '测试部门注销申请', 'CANCEL', 'REJECTED', u1.id, u1.oper_name, '2026-01-21 10:20:00', r.id, r.oper_name, '2026-01-21 11:10:00'
FROM `user` u1 JOIN `user` r ON r.oper_code='TEST_USER_REVIEW_01' WHERE u1.oper_code='TEST_USER_ADMIN_01';

-- post_apply test data
INSERT INTO post_apply
(arr_no, post_id, dept_id, dept_name, post_name, remark, post_type, oper_type, post_status, oper_code, oper_name, arr_date, review_oper_code, review_oper_name, review_time)
SELECT 'TST26_POS_0001', p.id, p.dept_id, p.dept_name, p.post_name, '测试岗位新增申请', 'INTERNAL', 'ADD', 'PENDING', u.id, u.oper_name, '2026-01-21 12:00:00', NULL, NULL, NULL
FROM post p JOIN `user` u ON u.oper_code='TEST_USER_ADMIN_01' WHERE p.post_name='TEST_POST_ALPHA_ANALYST'
UNION ALL
SELECT 'TST26_POS_0002', p.id, p.dept_id, p.dept_name, p.post_name, '测试岗位修改申请', 'INTERNAL', 'MODIFY', 'APPROVED', u.id, u.oper_name, '2026-01-21 12:10:00', r.id, r.oper_name, '2026-01-21 13:00:00'
FROM post p JOIN `user` u ON u.oper_code='TEST_USER_ADMIN_02' JOIN `user` r ON r.oper_code='TEST_USER_REVIEW_01' WHERE p.post_name='TEST_POST_BETA_PM'
UNION ALL
SELECT 'TST26_POS_0003', p.id, p.dept_id, p.dept_name, p.post_name, '测试岗位注销申请', 'INTERNAL', 'CANCEL', 'REJECTED', u.id, u.oper_name, '2026-01-21 12:20:00', r.id, r.oper_name, '2026-01-21 13:10:00'
FROM post p JOIN `user` u ON u.oper_code='TEST_USER_ADMIN_01' JOIN `user` r ON r.oper_code='TEST_USER_REVIEW_01' WHERE p.post_name='TEST_POST_GAMMA_RISK';

-- operator_apply test data
INSERT INTO operator_apply
(arr_no, dept_id, dept_name, tel_phone, mobile, remark, oper_type, oper_status, oper_code, oper_name, arr_date, review_oper_code, review_oper_name, review_time)
SELECT 'TST26_OPR_0001', 6, 'TEST_DEPT_ALPHA', '021-60010001', '13900000001', '测试操作员新增申请', 'ADD', 'PENDING', u.id, u.oper_name, '2026-01-21 14:00:00', NULL, NULL, NULL
FROM `user` u WHERE u.oper_code='TEST_USER_ADMIN_01'
UNION ALL
SELECT 'TST26_OPR_0002', 7, 'TEST_DEPT_BETA', '021-60010002', '13900000002', '测试操作员修改申请', 'MODIFY', 'APPROVED', u.id, u.oper_name, '2026-01-21 14:10:00', r.id, r.oper_name, '2026-01-21 15:00:00'
FROM `user` u JOIN `user` r ON r.oper_code='TEST_USER_REVIEW_01' WHERE u.oper_code='TEST_USER_ADMIN_02'
UNION ALL
SELECT 'TST26_OPR_0003', 6, 'TEST_DEPT_ALPHA', '021-60010003', '13900000003', '测试操作员冻结申请', 'FREEZE', 'REJECTED', u.id, u.oper_name, '2026-01-21 14:20:00', r.id, r.oper_name, '2026-01-21 15:10:00'
FROM `user` u JOIN `user` r ON r.oper_code='TEST_USER_REVIEW_01' WHERE u.oper_code='TEST_USER_ADMIN_01';

-- admin_apply test data
INSERT INTO admin_apply
(arr_no, oper_type, tel_phone, mobile, remark, operation_type, oper_status, oper_code, oper_name, dept_id, arr_date, review_oper_code, review_oper_name, review_time)
SELECT 'TST26_ADM_0001', 'DEPT_ADMIN', '021-70010001', '13600000001', '测试管理员新增申请', 'ADD', 'PENDING', u.id, u.oper_name, 6, '2026-01-21 16:00:00', NULL, NULL, NULL
FROM `user` u WHERE u.oper_code='TEST_USER_SUPER_01'
UNION ALL
SELECT 'TST26_ADM_0002', 'DEPT_ADMIN', '021-70010002', '13600000002', '测试管理员修改申请', 'MODIFY', 'APPROVED', u.id, u.oper_name, 7, '2026-01-21 16:10:00', r.id, r.oper_name, '2026-01-21 17:00:00'
FROM `user` u JOIN `user` r ON r.oper_code='TEST_USER_REVIEW_01' WHERE u.oper_code='TEST_USER_SUPER_01'
UNION ALL
SELECT 'TST26_ADM_0003', 'DEPT_ADMIN', '021-70010003', '13600000003', '测试管理员冻结申请', 'FREEZE', 'REJECTED', u.id, u.oper_name, 6, '2026-01-21 16:20:00', r.id, r.oper_name, '2026-01-21 17:10:00'
FROM `user` u JOIN `user` r ON r.oper_code='TEST_USER_REVIEW_01' WHERE u.oper_code='TEST_USER_SUPER_01';
