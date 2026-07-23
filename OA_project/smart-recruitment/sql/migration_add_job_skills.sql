-- ============================================================
-- 增量迁移：为 job 表增加 skills 字段（技能标签，逗号分隔）
-- 适用：已在低版本 schema 上完成初始化的库，需要补齐技能标签列
-- ============================================================

ALTER TABLE job
    ADD COLUMN skills VARCHAR(500) DEFAULT NULL COMMENT '技能标签，逗号分隔' AFTER requirements;
