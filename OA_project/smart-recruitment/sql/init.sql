-- 智能招聘系统数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS smart_recruitment DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smart_recruitment;

-- 系统用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    avatar VARCHAR(255) COMMENT '头像',
    status TINYINT DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) COMMENT '系统用户表';

-- 系统角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    description VARCHAR(255) COMMENT '描述',
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '系统角色表';

-- 系统权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    permission_code VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码',
    permission_name VARCHAR(50) NOT NULL COMMENT '权限名称',
    description VARCHAR(255),
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '系统权限表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    UNIQUE KEY uk_user_role (user_id, role_id)
) COMMENT '用户角色关联表';

-- 职位表
CREATE TABLE IF NOT EXISTS job (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL COMMENT '职位名称',
    description TEXT COMMENT '职位描述',
    requirements TEXT COMMENT '任职要求',
    category VARCHAR(50) COMMENT '职位分类',
    location VARCHAR(100) COMMENT '工作地点',
    salary_min DECIMAL(10,2) COMMENT '最低薪资',
    salary_max DECIMAL(10,2) COMMENT '最高薪资',
    experience_years INT COMMENT '工作年限要求',
    education VARCHAR(20) COMMENT '学历要求',
    headcount INT COMMENT '招聘人数',
    status TINYINT DEFAULT 0 COMMENT '状态 0-草稿 1-已发布 2-已下架',
    hr_user_id BIGINT COMMENT 'HR用户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) COMMENT '职位表';

-- 简历表
CREATE TABLE IF NOT EXISTS resume (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    name VARCHAR(50) COMMENT '姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '电话',
    education VARCHAR(20) COMMENT '学历',
    school VARCHAR(100) COMMENT '学校',
    major VARCHAR(100) COMMENT '专业',
    work_years INT COMMENT '工作年限',
    skills TEXT COMMENT '技能',
    summary TEXT COMMENT '个人简介',
    work_experience TEXT COMMENT '工作经历',
    file_path VARCHAR(255) COMMENT '简历文件路径',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) COMMENT '简历表';

-- 职位投递表
CREATE TABLE IF NOT EXISTS job_application (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id BIGINT NOT NULL COMMENT '职位ID',
    user_id BIGINT NOT NULL COMMENT '求职者ID',
    resume_id BIGINT COMMENT '简历ID',
    status TINYINT DEFAULT 0 COMMENT '状态 0-已投递 1-初筛通过 2-初筛不通过 3-面试中 4-录用 5-拒绝',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_job_user (job_id, user_id)
) COMMENT '职位投递表';

-- 投递状态流转日志
CREATE TABLE IF NOT EXISTS application_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL,
    from_status TINYINT COMMENT '原状态',
    to_status TINYINT COMMENT '新状态',
    remark VARCHAR(500) COMMENT '备注',
    operator_id BIGINT COMMENT '操作人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '投递状态流转日志';

-- 面试表
CREATE TABLE IF NOT EXISTS interview (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL COMMENT '投递ID',
    job_id BIGINT NOT NULL COMMENT '职位ID',
    candidate_id BIGINT NOT NULL COMMENT '候选人ID',
    interviewer_id BIGINT COMMENT '面试官ID',
    status TINYINT DEFAULT 0 COMMENT '状态 0-待面试 1-面试中 2-已完成 3-已取消',
    interview_time DATETIME COMMENT '面试时间',
    location VARCHAR(200) COMMENT '面试地点',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '面试表';

-- 面试题表
CREATE TABLE IF NOT EXISTS interview_question (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    interview_id BIGINT NOT NULL COMMENT '面试ID',
    title TEXT NOT NULL COMMENT '题目',
    type VARCHAR(20) COMMENT '类型',
    difficulty VARCHAR(20) COMMENT '难度',
    reference_answer TEXT COMMENT '参考答案',
    scoring_criteria TEXT COMMENT '评分标准',
    sort_order INT DEFAULT 0 COMMENT '排序',
    is_ai_generated TINYINT DEFAULT 0 COMMENT '是否AI生成',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '面试题表';

-- 面试评价表
CREATE TABLE IF NOT EXISTS interview_evaluation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    interview_id BIGINT NOT NULL COMMENT '面试ID',
    total_score DECIMAL(5,2) COMMENT '总分',
    technical_score VARCHAR(20) COMMENT '技术评分',
    communication_score VARCHAR(20) COMMENT '沟通评分',
    summary TEXT COMMENT '面试总结',
    recommendation VARCHAR(20) COMMENT '建议 PASS/REJECT/PENDING',
    evaluator_id BIGINT COMMENT '评价人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '面试评价表';

-- 模拟面试表
CREATE TABLE IF NOT EXISTS mock_interview (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    job_id BIGINT COMMENT '目标职位ID',
    status TINYINT DEFAULT 0 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '模拟面试表';

-- 模拟面试记录表
CREATE TABLE IF NOT EXISTS mock_interview_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mock_interview_id BIGINT NOT NULL,
    question TEXT COMMENT '问题',
    answer TEXT COMMENT '回答',
    follow_up TEXT COMMENT '追问',
    score DECIMAL(5,2) COMMENT '评分',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '模拟面试记录表';

-- AI分析表
CREATE TABLE IF NOT EXISTS ai_analysis (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) COMMENT '类型',
    request_data TEXT COMMENT '请求数据',
    response_data TEXT COMMENT '响应数据',
    user_id BIGINT COMMENT '用户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT 'AI分析记录表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    module VARCHAR(50) COMMENT '模块',
    description VARCHAR(255) COMMENT '描述',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人名称',
    request_url VARCHAR(255) COMMENT '请求URL',
    request_method VARCHAR(10) COMMENT '请求方法',
    ip VARCHAR(50) COMMENT 'IP地址',
    cost_time INT COMMENT '耗时(ms)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '操作日志表';

-- 初始化角色数据
INSERT INTO sys_role (role_code, role_name, description) VALUES
('ADMIN', '管理员', '系统管理员'),
('HR', 'HR', '人力资源'),
('INTERVIEWER', '面试官', '面试官'),
('CANDIDATE', '求职者', '求职者');

-- 初始化管理员账号（密码: admin123）
INSERT INTO sys_user (username, password, real_name, status) VALUES
('admin', '$2a$10$N.ZOn9G6/YbEHhcMbMkYxeEaVjLKHjvNp8F5F5F5F5F5F5F5F5F5F', '系统管理员', 1);
