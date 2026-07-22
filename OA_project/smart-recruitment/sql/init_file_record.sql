-- 文件记录表迁移脚本
-- 用于支持简历/头像等文件本地存储功能
-- 执行方式：mysql -u root -p smart_recruitment < sql/init_file_record.sql
-- 或在 MySQL 客户端中直接 USE smart_recruitment; 后执行本脚本

USE smart_recruitment;

-- 文件记录表
CREATE TABLE IF NOT EXISTS file_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    stored_name VARCHAR(255) NOT NULL COMMENT '存储文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件存储路径',
    file_size BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
    content_type VARCHAR(100) DEFAULT NULL COMMENT '文件MIME类型',
    file_type VARCHAR(50) DEFAULT 'other' COMMENT '文件分类: resume/avatar/other',
    uploader_id BIGINT DEFAULT NULL COMMENT '上传人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    KEY idx_file_record_uploader (uploader_id),
    KEY idx_file_record_type (file_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '文件记录表';
