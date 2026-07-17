-- 智能招聘系统数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS smart_recruitment DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smart_recruitment;

-- 公司信息表
CREATE TABLE IF NOT EXISTS company (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_name VARCHAR(100) NOT NULL COMMENT '公司全称',
    short_name VARCHAR(50) DEFAULT NULL COMMENT '简称',
    industry VARCHAR(100) DEFAULT NULL COMMENT '行业',
    scale VARCHAR(50) DEFAULT NULL COMMENT '规模',
    city VARCHAR(100) DEFAULT NULL COMMENT '所在城市',
    address VARCHAR(255) DEFAULT NULL COMMENT '办公地址',
    logo_url VARCHAR(255) DEFAULT NULL COMMENT 'LOGO',
    description TEXT COMMENT '公司简介',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '公司信息表';

-- 系统用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    user_type TINYINT NOT NULL DEFAULT 4 COMMENT '1 管理员 2 HR 3 面试官 4 求职者',
    gender TINYINT DEFAULT 0 COMMENT '性别 0未知 1男 2女',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    avatar_url VARCHAR(255) DEFAULT NULL COMMENT '头像',
    company_id BIGINT DEFAULT NULL COMMENT '所属公司',
    status TINYINT DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_sys_user_username (username),
    UNIQUE KEY uk_sys_user_phone (phone),
    KEY idx_sys_user_company (company_id),
    CONSTRAINT fk_sys_user_company FOREIGN KEY (company_id) REFERENCES company (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '系统用户表';

-- 系统角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    description VARCHAR(255) COMMENT '描述',
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '系统角色表';

-- 系统权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    permission_code VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码',
    permission_name VARCHAR(50) NOT NULL COMMENT '权限名称',
    description VARCHAR(255) DEFAULT NULL COMMENT '描述',
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '系统权限表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    CONSTRAINT fk_role_permission_role FOREIGN KEY (role_id) REFERENCES sys_role (id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permission_permission FOREIGN KEY (permission_id) REFERENCES sys_permission (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '角色权限关联表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_role (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '用户角色关联表';

-- 职位表
CREATE TABLE IF NOT EXISTS job (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_name VARCHAR(100) NOT NULL COMMENT '职位名称',
    department VARCHAR(100) DEFAULT NULL COMMENT '部门',
    category VARCHAR(50) DEFAULT NULL COMMENT '岗位类别',
    city VARCHAR(100) DEFAULT NULL COMMENT '工作城市',
    salary_min DECIMAL(10,2) DEFAULT NULL COMMENT '最低薪资',
    salary_max DECIMAL(10,2) DEFAULT NULL COMMENT '最高薪资',
    education VARCHAR(50) DEFAULT NULL COMMENT '学历要求',
    experience VARCHAR(50) DEFAULT NULL COMMENT '经验要求',
    headcount INT DEFAULT 1 COMMENT '招聘人数',
    description TEXT COMMENT '岗位描述',
    requirements TEXT COMMENT '任职要求',
    status TINYINT DEFAULT 0 COMMENT '0 草稿 1 招聘中 2 下架',
    publisher_id BIGINT DEFAULT NULL COMMENT '发布人',
    publish_time DATETIME DEFAULT NULL COMMENT '发布时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    KEY idx_job_status (status),
    KEY idx_job_city (city),
    KEY idx_job_publisher (publisher_id),
    FULLTEXT KEY ft_job_content (job_name, description, requirements),
    CONSTRAINT fk_job_publisher FOREIGN KEY (publisher_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '职位表';

-- 简历主表
CREATE TABLE IF NOT EXISTS resume (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '求职者ID',
    file_url VARCHAR(500) DEFAULT NULL COMMENT '简历文件地址',
    parsed_content LONGTEXT COMMENT '解析后的文本',
    ai_score DECIMAL(5,2) DEFAULT NULL COMMENT 'AI评分',
    ai_analysis TEXT COMMENT 'AI分析',
    current_salary DECIMAL(10,2) DEFAULT NULL COMMENT '当前薪资',
    expected_salary DECIMAL(10,2) DEFAULT NULL COMMENT '期望薪资',
    self_evaluation TEXT COMMENT '自我评价',
    embedding_vector JSON COMMENT 'Embedding JSON 存储',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    KEY idx_resume_user (user_id),
    CONSTRAINT fk_resume_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '简历表';

-- 教育经历子表
CREATE TABLE IF NOT EXISTS resume_education (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resume_id BIGINT NOT NULL COMMENT '简历ID',
    school VARCHAR(100) NOT NULL COMMENT '学校',
    major VARCHAR(100) DEFAULT NULL COMMENT '专业',
    degree VARCHAR(20) DEFAULT NULL COMMENT '学历',
    start_date DATE DEFAULT NULL COMMENT '入学时间',
    end_date DATE DEFAULT NULL COMMENT '毕业时间',
    is_highest TINYINT DEFAULT 0 COMMENT '是否最高学历',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_resume_education_resume (resume_id),
    CONSTRAINT fk_resume_education_resume FOREIGN KEY (resume_id) REFERENCES resume (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '教育经历表';

-- 工作经历子表
CREATE TABLE IF NOT EXISTS resume_experience (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resume_id BIGINT NOT NULL COMMENT '简历ID',
    company VARCHAR(100) NOT NULL COMMENT '公司',
    position VARCHAR(100) DEFAULT NULL COMMENT '职位',
    department VARCHAR(100) DEFAULT NULL COMMENT '部门',
    start_date DATE DEFAULT NULL COMMENT '入职时间',
    end_date DATE DEFAULT NULL COMMENT '离职时间',
    description TEXT COMMENT '工作内容',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_resume_experience_resume (resume_id),
    CONSTRAINT fk_resume_experience_resume FOREIGN KEY (resume_id) REFERENCES resume (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '工作经历表';

-- 职位投递表
CREATE TABLE IF NOT EXISTS job_application (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id BIGINT NOT NULL COMMENT '职位ID',
    user_id BIGINT NOT NULL COMMENT '求职者ID',
    resume_id BIGINT NOT NULL COMMENT '简历ID',
    apply_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '投递时间',
    status TINYINT DEFAULT 0 COMMENT '0 待筛选 1 面试中 2 录用 3 淘汰 4 撤回',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    ai_match_score DECIMAL(5,2) DEFAULT NULL COMMENT 'AI匹配得分',
    ai_match_reason TEXT COMMENT 'AI匹配说明',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_job_user (job_id, user_id),
    KEY idx_application_status (status),
    KEY idx_application_user (user_id),
    CONSTRAINT fk_job_application_job FOREIGN KEY (job_id) REFERENCES job (id),
    CONSTRAINT fk_job_application_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
    CONSTRAINT fk_job_application_resume FOREIGN KEY (resume_id) REFERENCES resume (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '职位投递表';

-- 投递状态流转日志
CREATE TABLE IF NOT EXISTS application_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL COMMENT '投递ID',
    from_status TINYINT DEFAULT NULL COMMENT '原状态',
    to_status TINYINT DEFAULT NULL COMMENT '新状态',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    operator_id BIGINT DEFAULT NULL COMMENT '操作人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_application_log_application (application_id),
    CONSTRAINT fk_application_log_application FOREIGN KEY (application_id) REFERENCES job_application (id) ON DELETE CASCADE,
    CONSTRAINT fk_application_log_operator FOREIGN KEY (operator_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '投递状态日志';

-- 面试表
CREATE TABLE IF NOT EXISTS interview (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL COMMENT '投递记录ID',
    interviewer_id BIGINT NOT NULL COMMENT '面试官ID',
    interview_time DATETIME DEFAULT NULL COMMENT '面试时间',
    interview_type VARCHAR(20) DEFAULT NULL COMMENT '线上/线下',
    address VARCHAR(255) DEFAULT NULL COMMENT '地点或链接',
    ai_questions TEXT COMMENT 'AI生成题目缓存',
    status TINYINT DEFAULT 0 COMMENT '0 待面试 1 已完成 2 取消',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_interview_application (application_id),
    CONSTRAINT fk_interview_application FOREIGN KEY (application_id) REFERENCES job_application (id),
    CONSTRAINT fk_interview_interviewer FOREIGN KEY (interviewer_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '面试表';

-- 面试题表
CREATE TABLE IF NOT EXISTS interview_question (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    interview_id BIGINT NOT NULL COMMENT '面试ID',
    title TEXT NOT NULL COMMENT '题目',
    question_type VARCHAR(20) DEFAULT NULL COMMENT '题目类型',
    difficulty VARCHAR(20) DEFAULT NULL COMMENT '难度',
    reference_answer TEXT COMMENT '参考答案',
    scoring_criteria TEXT COMMENT '评分标准',
    sort_order INT DEFAULT 0 COMMENT '排序',
    is_ai_generated TINYINT DEFAULT 0 COMMENT '是否AI生成',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_interview_question_interview FOREIGN KEY (interview_id) REFERENCES interview (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '面试题表';

-- 面试评价表
CREATE TABLE IF NOT EXISTS interview_evaluation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    interview_id BIGINT NOT NULL COMMENT '面试ID',
    technical_score INT DEFAULT NULL COMMENT '技术评分',
    communication_score INT DEFAULT NULL COMMENT '沟通评分',
    logic_score INT DEFAULT NULL COMMENT '逻辑评分',
    overall_score INT DEFAULT NULL COMMENT '综合评分',
    evaluation TEXT COMMENT '评价内容',
    result TINYINT DEFAULT 0 COMMENT '0 淘汰 1 待定 2 通过',
    feedback_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
    CONSTRAINT fk_interview_evaluation_interview FOREIGN KEY (interview_id) REFERENCES interview (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '面试评价表';

-- 模拟面试表
CREATE TABLE IF NOT EXISTS mock_interview (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    job_id BIGINT DEFAULT NULL COMMENT '目标职位ID',
    status TINYINT DEFAULT 0 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_mock_interview_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
    CONSTRAINT fk_mock_interview_job FOREIGN KEY (job_id) REFERENCES job (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '模拟面试表';

-- 模拟面试记录表
CREATE TABLE IF NOT EXISTS mock_interview_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mock_interview_id BIGINT NOT NULL,
    question TEXT COMMENT '问题',
    answer TEXT COMMENT '回答',
    follow_up TEXT COMMENT '追问',
    score DECIMAL(5,2) DEFAULT NULL COMMENT '评分',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mock_interview_record_mock FOREIGN KEY (mock_interview_id) REFERENCES mock_interview (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '模拟面试记录表';

-- AI分析记录表
CREATE TABLE IF NOT EXISTS ai_analysis (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) DEFAULT NULL COMMENT '类型',
    request_data TEXT COMMENT '请求数据',
    response_data TEXT COMMENT '响应数据',
    user_id BIGINT DEFAULT NULL COMMENT '用户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_ai_analysis_user (user_id),
    CONSTRAINT fk_ai_analysis_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT 'AI分析记录表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    module VARCHAR(50) DEFAULT NULL COMMENT '模块',
    description VARCHAR(255) DEFAULT NULL COMMENT '描述',
    operator_id BIGINT DEFAULT NULL COMMENT '操作人ID',
    operator_name VARCHAR(50) DEFAULT NULL COMMENT '操作人名称',
    request_url VARCHAR(255) DEFAULT NULL COMMENT '请求URL',
    request_method VARCHAR(10) DEFAULT NULL COMMENT '请求方法',
    ip VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    cost_time INT DEFAULT NULL COMMENT '耗时(ms)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_operation_log_operator (operator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '操作日志表';

-- 初始化角色数据
INSERT INTO sys_role (role_code, role_name, description) VALUES
('ADMIN', '管理员', '系统管理员'),
('HR', 'HR', '人力资源'),
('INTERVIEWER', '面试官', '面试官'),
('CANDIDATE', '求职者', '求职者');

-- 初始化管理员账号（密码: admin123）
INSERT INTO sys_user (username, password, real_name, user_type, status)
VALUES ('admin', '$2a$10$CwTycUXWue0Thq9StjUM0uJ8b6Kp0U0zZMfjXC8iP9FeCFGXFZ8Ca', '系统管理员', 1, 1);
