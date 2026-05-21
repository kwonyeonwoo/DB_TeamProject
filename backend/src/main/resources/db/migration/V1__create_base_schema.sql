CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    login_id VARCHAR(50) NULL,
    password VARCHAR(255) NULL,
    name VARCHAR(50) NULL,
    email_address VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    CONSTRAINT uk_users_login_id UNIQUE (login_id),
    CONSTRAINT uk_users_email_address UNIQUE (email_address),
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'DELETED')),
    CONSTRAINT chk_users_role CHECK (role IN ('USER', 'ADMIN')),
    CONSTRAINT chk_users_active_login_id CHECK (status = 'DELETED' OR login_id IS NOT NULL),
    CONSTRAINT chk_users_active_password CHECK (status = 'DELETED' OR password IS NOT NULL),
    CONSTRAINT chk_users_active_name CHECK (status = 'DELETED' OR name IS NOT NULL),
    CONSTRAINT chk_users_active_email_address CHECK (status = 'DELETED' OR email_address IS NOT NULL)
);

CREATE TABLE post (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    view_count INT NOT NULL DEFAULT 0,
    main_category VARCHAR(100) NOT NULL,
    sub_category VARCHAR(100) NOT NULL,
    is_anonymous BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_post_view_count CHECK (view_count >= 0),
    CONSTRAINT fk_post_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE NO ACTION ON UPDATE CASCADE
);

CREATE TABLE likes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    post_id INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_likes_user_post UNIQUE (user_id, post_id),
    CONSTRAINT fk_likes_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE NO ACTION ON UPDATE CASCADE,
    CONSTRAINT fk_likes_post_id FOREIGN KEY (post_id) REFERENCES post (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE comments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    post_id INT NOT NULL,
    parent_comment INT NULL,
    content TEXT NOT NULL,
    is_anonymous BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    CONSTRAINT fk_comments_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE NO ACTION ON UPDATE CASCADE,
    CONSTRAINT fk_comments_post_id FOREIGN KEY (post_id) REFERENCES post (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_comments_parent_comment FOREIGN KEY (parent_comment) REFERENCES comments (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE report (
    id INT AUTO_INCREMENT PRIMARY KEY,
    reporter_id INT NOT NULL,
    target_type VARCHAR(20) NOT NULL,
    target_id INT NOT NULL,
    reason_type INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    processed_by INT NULL,
    processed_at TIMESTAMP NULL,
    CONSTRAINT uk_report_target UNIQUE (reporter_id, target_type, target_id),
    CONSTRAINT chk_report_target_type CHECK (target_type IN ('POST', 'COMMENT')),
    CONSTRAINT chk_report_reason_type CHECK (reason_type IN (1, 2, 3, 4)),
    CONSTRAINT chk_report_status CHECK (status IN ('PENDING', 'PROCESSED')),
    CONSTRAINT fk_report_reporter_id FOREIGN KEY (reporter_id) REFERENCES users (id) ON DELETE NO ACTION ON UPDATE CASCADE,
    CONSTRAINT fk_report_processed_by FOREIGN KEY (processed_by) REFERENCES users (id) ON DELETE NO ACTION ON UPDATE CASCADE
);

CREATE TABLE `groups` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    group_code VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    leader_id INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_groups_group_code UNIQUE (group_code),
    CONSTRAINT fk_groups_leader_id FOREIGN KEY (leader_id) REFERENCES users (id) ON DELETE NO ACTION ON UPDATE CASCADE
);

CREATE TABLE group_members (
    group_id INT NOT NULL,
    user_id INT NOT NULL,
    role VARCHAR(20) NOT NULL,
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (group_id, user_id),
    CONSTRAINT chk_group_members_role CHECK (role IN ('LEADER', 'MEMBER')),
    CONSTRAINT fk_group_members_group_id FOREIGN KEY (group_id) REFERENCES `groups` (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_group_members_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE NO ACTION ON UPDATE CASCADE
);

CREATE TABLE schedules (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    group_id INT NULL,
    title VARCHAR(255) NOT NULL,
    start_at TIMESTAMP NOT NULL,
    end_at TIMESTAMP NOT NULL,
    description VARCHAR(500) NULL,
    type INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    CONSTRAINT chk_schedules_period CHECK (end_at >= start_at),
    CONSTRAINT chk_schedules_type CHECK (type IN (1, 2, 3, 4, 5)),
    CONSTRAINT fk_schedules_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE NO ACTION ON UPDATE CASCADE,
    CONSTRAINT fk_schedules_group_id FOREIGN KEY (group_id) REFERENCES `groups` (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE file (
    id INT NOT NULL,
    file_url VARCHAR(255) NOT NULL,
    PRIMARY KEY (id, file_url),
    CONSTRAINT fk_file_id FOREIGN KEY (id) REFERENCES post (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE notification (
    id INT AUTO_INCREMENT PRIMARY KEY,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    comment_content TEXT NOT NULL,
    commented_post_id INT NOT NULL,
    commented_user_id INT NOT NULL,
    commented_id INT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_post_id FOREIGN KEY (commented_post_id) REFERENCES post (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_notification_user_id FOREIGN KEY (commented_user_id) REFERENCES users (id) ON DELETE NO ACTION ON UPDATE CASCADE
);

CREATE INDEX idx_users_status ON users (status);
CREATE INDEX idx_users_deleted_at ON users (deleted_at);
CREATE INDEX idx_post_user_id ON post (user_id);
CREATE INDEX idx_post_category ON post (main_category, sub_category);
CREATE INDEX idx_post_created_at ON post (created_at);
CREATE INDEX idx_post_author_filter ON post (user_id, is_anonymous);
CREATE INDEX idx_likes_post_id ON likes (post_id);
CREATE INDEX idx_comments_user_id ON comments (user_id);
CREATE INDEX idx_comments_post_id ON comments (post_id);
CREATE INDEX idx_comments_parent_comment ON comments (parent_comment);
CREATE INDEX idx_comments_anonymous ON comments (post_id, is_anonymous);
CREATE INDEX idx_report_reporter_id ON report (reporter_id);
CREATE INDEX idx_report_target ON report (target_type, target_id);
CREATE INDEX idx_report_created_at ON report (created_at);
CREATE INDEX idx_report_reason_type ON report (reason_type);
CREATE INDEX idx_report_status ON report (status);
CREATE INDEX idx_report_processed_by ON report (processed_by);
CREATE INDEX idx_groups_leader_id ON `groups` (leader_id);
CREATE INDEX idx_group_members_user_id ON group_members (user_id);
CREATE INDEX idx_group_members_leader_transfer ON group_members (group_id, role, joined_at);
CREATE INDEX idx_schedules_user_period ON schedules (user_id, start_at, end_at);
CREATE INDEX idx_schedules_group_period ON schedules (group_id, start_at, end_at);
CREATE INDEX idx_schedules_type ON schedules (type);
CREATE INDEX idx_file_post_id ON file (id);
CREATE INDEX idx_notification_receiver ON notification (commented_user_id, is_read, created_at);
CREATE INDEX idx_notification_post_id ON notification (commented_post_id);
CREATE INDEX idx_notification_comment_id ON notification (commented_id);
