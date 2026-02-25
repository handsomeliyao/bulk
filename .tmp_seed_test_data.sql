DELETE FROM post WHERE post_name LIKE 'TEST_POST_%' OR dept_name LIKE 'TEST_DEPT_%';
DELETE FROM department WHERE dept_name LIKE 'TEST_DEPT_%';

INSERT INTO department (dept_name, remark, dept_status, created_at, updated_at) VALUES
('TEST_DEPT_ALPHA', '测试部门-大宗商品现货', 'NORMAL', '2026-01-05 09:10:00', '2026-01-12 14:20:00'),
('TEST_DEPT_BETA', '测试部门-期货与衍生品', 'NORMAL', '2026-01-06 10:15:00', '2026-01-13 15:25:00'),
('TEST_DEPT_GAMMA', '测试部门-风控管理', 'CANCELED', '2026-01-07 11:20:00', '2026-01-14 16:30:00'),
('TEST_DEPT_DELTA', '测试部门-清算结算', 'NORMAL', '2026-01-08 12:25:00', '2026-01-15 17:35:00'),
('TEST_DEPT_EPSILON', '测试部门-运营支持', 'NORMAL', '2026-01-09 13:30:00', '2026-01-16 18:40:00');

INSERT INTO post (dept_id, dept_name, post_name, post_type, remark, post_status, created_at, updated_at)
SELECT d.id, d.dept_name, 'TEST_POST_ALPHA_ANALYST', 'INTERNAL', '测试岗位-现货分析岗', 'NORMAL', '2026-01-10 09:00:00', '2026-01-18 09:00:00'
FROM department d WHERE d.dept_name = 'TEST_DEPT_ALPHA'
UNION ALL
SELECT d.id, d.dept_name, 'TEST_POST_ALPHA_TRADER', 'INTERNAL', '测试岗位-交易执行岗', 'NORMAL', '2026-01-10 09:30:00', '2026-01-18 09:30:00'
FROM department d WHERE d.dept_name = 'TEST_DEPT_ALPHA'
UNION ALL
SELECT d.id, d.dept_name, 'TEST_POST_BETA_PM', 'INTERNAL', '测试岗位-产品经理岗', 'NORMAL', '2026-01-10 10:00:00', '2026-01-18 10:00:00'
FROM department d WHERE d.dept_name = 'TEST_DEPT_BETA'
UNION ALL
SELECT d.id, d.dept_name, 'TEST_POST_BETA_OPS', 'INTERNAL', '测试岗位-运营岗', 'CANCELED', '2026-01-10 10:30:00', '2026-01-18 10:30:00'
FROM department d WHERE d.dept_name = 'TEST_DEPT_BETA'
UNION ALL
SELECT d.id, d.dept_name, 'TEST_POST_GAMMA_RISK', 'INTERNAL', '测试岗位-风险复核岗', 'CANCELED', '2026-01-10 11:00:00', '2026-01-18 11:00:00'
FROM department d WHERE d.dept_name = 'TEST_DEPT_GAMMA'
UNION ALL
SELECT d.id, d.dept_name, 'TEST_POST_DELTA_CLEAR', 'INTERNAL', '测试岗位-清算处理岗', 'NORMAL', '2026-01-10 11:30:00', '2026-01-18 11:30:00'
FROM department d WHERE d.dept_name = 'TEST_DEPT_DELTA'
UNION ALL
SELECT d.id, d.dept_name, 'TEST_POST_EPSILON_SUPPORT', 'INTERNAL', '测试岗位-运营支持岗', 'NORMAL', '2026-01-10 12:00:00', '2026-01-18 12:00:00'
FROM department d WHERE d.dept_name = 'TEST_DEPT_EPSILON';
