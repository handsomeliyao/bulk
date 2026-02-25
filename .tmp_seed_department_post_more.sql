-- More Department/Post test data (idempotent)
-- Adds: 20 departments + 60 posts

DELETE FROM post WHERE post_name LIKE 'TEST26M_POST_%' OR dept_name LIKE 'TEST26M_DEPT_%';
DELETE FROM department WHERE dept_name LIKE 'TEST26M_DEPT_%';

INSERT INTO department
(dept_name, remark, dept_status, created_oper_name, created_at, updated_oper_name, updated_at, review_oper_name, review_time)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 20
)
SELECT
    CONCAT('TEST26M_DEPT_', LPAD(n, 3, '0')),
    CONCAT('批量测试部门-', n),
    CASE WHEN MOD(n, 7) = 0 THEN 'CANCELED' ELSE 'NORMAL' END,
    'system.seed.bulk',
    DATE_ADD('2026-02-01 08:00:00', INTERVAL n HOUR),
    'system.seed.bulk',
    DATE_ADD('2026-02-08 09:00:00', INTERVAL n HOUR),
    'review.seed.bulk',
    DATE_ADD('2026-02-08 10:00:00', INTERVAL n HOUR)
FROM seq;

INSERT INTO post
(dept_id, dept_name, post_name, post_type, remark, post_status, created_oper_name, created_at, updated_oper_name, updated_at, review_oper_name, review_time)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 20
), kinds AS (
    SELECT 1 AS k, 'ANALYST' AS role_name, '测试岗位-分析岗' AS role_remark
    UNION ALL
    SELECT 2 AS k, 'TRADER' AS role_name, '测试岗位-交易岗' AS role_remark
    UNION ALL
    SELECT 3 AS k, 'RISK' AS role_name, '测试岗位-风控岗' AS role_remark
)
SELECT
    d.id,
    d.dept_name,
    CONCAT('TEST26M_POST_', LPAD(s.n, 3, '0'), '_', k.role_name),
    'INTERNAL',
    CONCAT(k.role_remark, '-', s.n),
    CASE WHEN d.dept_status = 'CANCELED' AND k.k = 3 THEN 'CANCELED' ELSE 'NORMAL' END,
    'system.seed.bulk',
    DATE_ADD('2026-02-10 08:00:00', INTERVAL (s.n * 3 + k.k) HOUR),
    'system.seed.bulk',
    DATE_ADD('2026-02-11 08:00:00', INTERVAL (s.n * 3 + k.k) HOUR),
    'review.seed.bulk',
    DATE_ADD('2026-02-11 09:00:00', INTERVAL (s.n * 3 + k.k) HOUR)
FROM seq s
JOIN department d ON d.dept_name = CONCAT('TEST26M_DEPT_', LPAD(s.n, 3, '0'))
JOIN kinds k;

SELECT 'new_department_count' AS item, COUNT(*) AS cnt FROM department WHERE dept_name LIKE 'TEST26M_DEPT_%'
UNION ALL
SELECT 'new_post_count' AS item, COUNT(*) AS cnt FROM post WHERE post_name LIKE 'TEST26M_POST_%'
UNION ALL
SELECT 'all_test26_department_count' AS item, COUNT(*) AS cnt FROM department WHERE dept_name LIKE 'TEST26%DEPT_%'
UNION ALL
SELECT 'all_test26_post_count' AS item, COUNT(*) AS cnt FROM post WHERE post_name LIKE 'TEST26%POST_%';
