-- Department/Post full-field test data (idempotent)
-- Execute in target database after init.sql schema is applied.

-- 1) Cleanup old test data first (post depends on department)
DELETE FROM post WHERE post_name LIKE 'TEST26_POST_%' OR dept_name LIKE 'TEST26_DEPT_%';
DELETE FROM department WHERE dept_name LIKE 'TEST26_DEPT_%';

-- 2) Insert departments (all business fields populated)
INSERT INTO department
(dept_name, remark, dept_status, created_oper_name, created_at, updated_oper_name, updated_at, review_oper_name, review_time)
VALUES
('TEST26_DEPT_ALPHA', '测试部门-大宗商品现货交易部', 'NORMAL', 'system.seed', '2026-02-01 09:00:00', 'system.seed', '2026-02-05 10:00:00', 'review.seed', '2026-02-05 11:00:00'),
('TEST26_DEPT_BETA', '测试部门-衍生品与风险管理部', 'NORMAL', 'system.seed', '2026-02-01 09:10:00', 'system.seed', '2026-02-05 10:10:00', 'review.seed', '2026-02-05 11:10:00'),
('TEST26_DEPT_GAMMA', '测试部门-清算与结算支持部', 'NORMAL', 'system.seed', '2026-02-01 09:20:00', 'system.seed', '2026-02-05 10:20:00', 'review.seed', '2026-02-05 11:20:00'),
('TEST26_DEPT_DELTA', '测试部门-运营协同部', 'CANCELED', 'system.seed', '2026-02-01 09:30:00', 'system.seed', '2026-02-05 10:30:00', 'review.seed', '2026-02-05 11:30:00');

-- 3) Insert posts (all business fields populated)
INSERT INTO post
(dept_id, dept_name, post_name, post_type, remark, post_status, created_oper_name, created_at, updated_oper_name, updated_at, review_oper_name, review_time)
SELECT d.id, d.dept_name, 'TEST26_POST_ALPHA_ANALYST', 'INTERNAL', '测试岗位-现货分析岗', 'NORMAL', 'system.seed', '2026-02-06 09:00:00', 'system.seed', '2026-02-06 10:00:00', 'review.seed', '2026-02-06 11:00:00'
FROM department d WHERE d.dept_name = 'TEST26_DEPT_ALPHA'
UNION ALL
SELECT d.id, d.dept_name, 'TEST26_POST_ALPHA_TRADER', 'INTERNAL', '测试岗位-现货交易执行岗', 'NORMAL', 'system.seed', '2026-02-06 09:10:00', 'system.seed', '2026-02-06 10:10:00', 'review.seed', '2026-02-06 11:10:00'
FROM department d WHERE d.dept_name = 'TEST26_DEPT_ALPHA'
UNION ALL
SELECT d.id, d.dept_name, 'TEST26_POST_BETA_RISK_CTRL', 'INTERNAL', '测试岗位-风控岗', 'NORMAL', 'system.seed', '2026-02-06 09:20:00', 'system.seed', '2026-02-06 10:20:00', 'review.seed', '2026-02-06 11:20:00'
FROM department d WHERE d.dept_name = 'TEST26_DEPT_BETA'
UNION ALL
SELECT d.id, d.dept_name, 'TEST26_POST_BETA_DERIV_PM', 'INTERNAL', '测试岗位-衍生品产品经理', 'NORMAL', 'system.seed', '2026-02-06 09:30:00', 'system.seed', '2026-02-06 10:30:00', 'review.seed', '2026-02-06 11:30:00'
FROM department d WHERE d.dept_name = 'TEST26_DEPT_BETA'
UNION ALL
SELECT d.id, d.dept_name, 'TEST26_POST_GAMMA_CLEARING', 'INTERNAL', '测试岗位-清算岗', 'NORMAL', 'system.seed', '2026-02-06 09:40:00', 'system.seed', '2026-02-06 10:40:00', 'review.seed', '2026-02-06 11:40:00'
FROM department d WHERE d.dept_name = 'TEST26_DEPT_GAMMA'
UNION ALL
SELECT d.id, d.dept_name, 'TEST26_POST_DELTA_SUPPORT', 'INTERNAL', '测试岗位-运营支持岗', 'CANCELED', 'system.seed', '2026-02-06 09:50:00', 'system.seed', '2026-02-06 10:50:00', 'review.seed', '2026-02-06 11:50:00'
FROM department d WHERE d.dept_name = 'TEST26_DEPT_DELTA';

-- 4) Verify
SELECT 'department_count' AS item, COUNT(*) AS cnt FROM department WHERE dept_name LIKE 'TEST26_DEPT_%'
UNION ALL
SELECT 'post_count' AS item, COUNT(*) AS cnt FROM post WHERE post_name LIKE 'TEST26_POST_%';
