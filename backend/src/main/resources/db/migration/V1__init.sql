-- 简历狗 初始 schema
-- 内容列用 JSON 而不是拆成多张子表，理由见 Resume 实体的注释。

CREATE TABLE users
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    email         VARCHAR(128) NOT NULL,
    password_hash VARCHAR(100) NOT NULL COMMENT 'BCrypt 散列，永不返回前端',
    nickname      VARCHAR(64)  NOT NULL,
    created_at    DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY idx_users_email (email)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE resumes
(
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL,
    title           VARCHAR(128) NOT NULL COMMENT '用户给这份简历起的名字',
    target_position VARCHAR(128) DEFAULT NULL COMMENT '目标岗位，AI 据此决定详略',
    template_code   VARCHAR(64)  NOT NULL DEFAULT 'classic',
    raw_fragments   TEXT         DEFAULT NULL COMMENT '原始经历碎片，AI 生成的输入',
    content         JSON         DEFAULT NULL COMMENT '结构化简历内容，AI 生成结果 + 用户编辑结果',
    created_at      DATETIME(6)  NOT NULL,
    updated_at      DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    KEY idx_resumes_user (user_id, updated_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
