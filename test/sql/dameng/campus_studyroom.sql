-- =============================================
-- 校园自习室预约系统数据库 - 达梦8版本
-- =============================================

-- 创建数据库
CREATE DATABASE campus_studyroom;
USE campus_studyroom;

-- =============================================
-- 1. 用户表
-- =============================================
CREATE TABLE user (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码(加密存储)',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    student_no VARCHAR(20) NOT NULL UNIQUE COMMENT '学号',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    avatar VARCHAR(255) COMMENT '头像URL',
    role VARCHAR(20) NOT NULL COMMENT '角色(student/admin/super_admin)',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态(1-正常 0-禁用)',
    study_preferences CLOB COMMENT '学习偏好(JSON格式)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记(0-未删除 1-已删除)',
    INDEX idx_username (username),
    INDEX idx_student_no (student_no),
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) COMPRESS=1;
COMMENT ON TABLE user IS '用户表';

-- =============================================
-- 2. 角色表
-- =============================================
CREATE TABLE role (
    role_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_key VARCHAR(50) NOT NULL UNIQUE COMMENT '角色标识',
    description VARCHAR(200) COMMENT '角色描述',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态(1-正常 0-禁用)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_role_key (role_key),
    INDEX idx_status (status)
) COMPRESS=1;
COMMENT ON TABLE role IS '角色表';

-- =============================================
-- 3. 权限表
-- =============================================
CREATE TABLE permission (
    permission_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    permission_name VARCHAR(50) NOT NULL COMMENT '权限名称',
    permission_key VARCHAR(50) NOT NULL UNIQUE COMMENT '权限标识',
    permission_type VARCHAR(20) NOT NULL COMMENT '权限类型(menu/button)',
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父权限ID',
    path VARCHAR(200) COMMENT '路由路径',
    component VARCHAR(200) COMMENT '组件路径',
    icon VARCHAR(50) COMMENT '图标',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态(1-正常 0-禁用)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_permission_key (permission_key),
    INDEX idx_parent_id (parent_id),
    INDEX idx_status (status)
) COMPRESS=1;
COMMENT ON TABLE permission IS '权限表';

-- =============================================
-- 4. 用户角色关联表
-- =============================================
CREATE TABLE user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES role(role_id) ON DELETE CASCADE
) COMPRESS=1;
COMMENT ON TABLE user_role IS '用户角色关联表';

-- =============================================
-- 5. 角色权限关联表
-- =============================================
CREATE TABLE role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id),
    FOREIGN KEY (role_id) REFERENCES role(role_id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permission(permission_id) ON DELETE CASCADE
) COMPRESS=1;
COMMENT ON TABLE role_permission IS '角色权限关联表';

-- =============================================
-- 6. 自习室表
-- =============================================
CREATE TABLE study_room (
    room_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_name VARCHAR(100) NOT NULL COMMENT '自习室名称',
    building VARCHAR(50) NOT NULL COMMENT '教学楼',
    floor INT NOT NULL COMMENT '楼层',
    capacity INT NOT NULL COMMENT '容量',
    facilities CLOB COMMENT '设施信息(JSON格式)',
    open_time TIME NOT NULL COMMENT '开放时间',
    close_time TIME NOT NULL COMMENT '关闭时间',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态(1-开放 0-关闭 2-维护中)',
    description VARCHAR(500) COMMENT '自习室描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_building_floor (building, floor),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) COMPRESS=1;
COMMENT ON TABLE study_room IS '自习室表';

-- =============================================
-- 7. 座位表
-- =============================================
CREATE TABLE study_seat (
    seat_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_id BIGINT NOT NULL COMMENT '自习室ID',
    seat_number VARCHAR(20) NOT NULL COMMENT '座位编号',
    zone VARCHAR(50) COMMENT '区域',
    position_x INT COMMENT 'X坐标',
    position_y INT COMMENT 'Y坐标',
    status VARCHAR(20) NOT NULL DEFAULT 'available' COMMENT '状态(available/occupied/reserved/maintenance)',
    type VARCHAR(20) NOT NULL DEFAULT 'normal' COMMENT '类型(normal/power/window/corner)',
    has_power BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否有电源',
    has_usb BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否有USB',
    has_wifi BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否有WiFi',
    description VARCHAR(200) COMMENT '描述',
    current_user_id BIGINT COMMENT '当前用户ID',
    current_reservation_id BIGINT COMMENT '当前预约ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_room_id (room_id),
    INDEX idx_seat_number (seat_number),
    INDEX idx_status (status),
    FOREIGN KEY (room_id) REFERENCES study_room(room_id) ON DELETE CASCADE
) COMPRESS=1;
COMMENT ON TABLE study_seat IS '座位表';

-- =============================================
-- 8. 时间段模板表
-- =============================================
CREATE TABLE time_slot (
    slot_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_id BIGINT NOT NULL COMMENT '自习室ID',
    start_time TIME NOT NULL COMMENT '开始时间',
    end_time TIME NOT NULL COMMENT '结束时间',
    max_reservations INT NOT NULL DEFAULT 1 COMMENT '最大预约数',
    current_reservations INT NOT NULL DEFAULT 0 COMMENT '当前预约数',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_room_id (room_id),
    INDEX idx_start_end_time (start_time, end_time),
    FOREIGN KEY (room_id) REFERENCES study_room(room_id) ON DELETE CASCADE
) COMPRESS=1;
COMMENT ON TABLE time_slot IS '时间段模板表';

-- =============================================
-- 9. 预约表
-- =============================================
CREATE TABLE reservation (
    reservation_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    room_id BIGINT NOT NULL COMMENT '自习室ID',
    seat_id BIGINT NOT NULL COMMENT '座位ID',
    slot_id BIGINT NOT NULL COMMENT '时间段ID',
    reserve_date DATE NOT NULL COMMENT '预约日期',
    start_time TIME NOT NULL COMMENT '开始时间',
    end_time TIME NOT NULL COMMENT '结束时间',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    check_in_time DATETIME COMMENT '签到时间',
    check_out_time DATETIME COMMENT '签退时间',
    penalty_days INT NOT NULL DEFAULT 0 COMMENT '处罚天数',
    violation_id BIGINT COMMENT '违规ID',
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES study_room(room_id) ON DELETE CASCADE,
    FOREIGN KEY (seat_id) REFERENCES seat(seat_id) ON DELETE CASCADE,
    FOREIGN KEY (slot_id) REFERENCES time_slot(slot_id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_room_seat_date (room_id, seat_id, reserve_date),
    INDEX idx_reserve_date (reserve_date),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) COMPRESS=1;
COMMENT ON TABLE reservation IS '预约表';

-- =============================================
-- 10. 考勤表
-- =============================================
CREATE TABLE attendance (
    attendance_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reservation_id BIGINT NOT NULL COMMENT '预约ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    check_in_time DATETIME COMMENT '签到时间',
    check_out_time DATETIME COMMENT '签退时间',
    duration INT NOT NULL DEFAULT 0 COMMENT '学习时长(分钟)',
    status VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '状态',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reservation_id) REFERENCES reservation(reservation_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_reservation_id (reservation_id),
    INDEX idx_create_time (create_time)
) COMPRESS=1;
COMMENT ON TABLE attendance IS '考勤表';

-- =============================================
-- 11. 违规记录表
-- =============================================
CREATE TABLE violation (
    violation_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    reservation_id BIGINT NOT NULL COMMENT '预约ID',
    type VARCHAR(20) NOT NULL COMMENT '违规类型',
    description VARCHAR(500) NOT NULL COMMENT '违规描述',
    penalty_days INT NOT NULL DEFAULT 0 COMMENT '处罚天数',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态',
    appeal_reason CLOB COMMENT '申诉理由',
    appeal_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '申诉状态',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_reservation_id (reservation_id),
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (reservation_id) REFERENCES reservation(reservation_id) ON DELETE CASCADE
) COMPRESS=1;
COMMENT ON TABLE violation IS '违规记录表';

-- =============================================
-- 12. 通知记录表
-- =============================================
CREATE TABLE notification (
    notification_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    type VARCHAR(20) NOT NULL COMMENT '通知类型',
    title VARCHAR(100) NOT NULL COMMENT '标题',
    content CLOB NOT NULL COMMENT '内容',
    is_read BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已读',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_is_read (is_read),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
) COMPRESS=1;
COMMENT ON TABLE notification IS '通知记录表';

-- =============================================
-- 13. AI推荐记录表
-- =============================================
CREATE TABLE ai_recommendation (
    recommendation_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    room_id BIGINT NOT NULL COMMENT '自习室ID',
    seat_id BIGINT NOT NULL COMMENT '座位ID',
    score DECIMAL(10,2) NOT NULL COMMENT '推荐得分',
    reason CLOB COMMENT '推荐理由',
    strategy VARCHAR(50) NOT NULL COMMENT '推荐策略',
    is_accepted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否被接受',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES study_room(room_id) ON DELETE CASCADE,
    FOREIGN KEY (seat_id) REFERENCES seat(seat_id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_score (score),
    INDEX idx_strategy (strategy),
    INDEX idx_create_time (create_time)
) COMPRESS=1;
COMMENT ON TABLE ai_recommendation IS 'AI推荐记录表';

-- =============================================
-- 14. 异常分析记录表
-- =============================================
CREATE TABLE anomaly (
    anomaly_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    attendance_id BIGINT NOT NULL COMMENT '考勤ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    type VARCHAR(20) NOT NULL COMMENT '异常类型',
    description CLOB NOT NULL COMMENT '异常描述',
    confidence DECIMAL(5,2) NOT NULL COMMENT '置信度(0-100)',
    is_handled BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已处理',
    handled_by BIGINT COMMENT '处理人ID',
    handle_time DATETIME COMMENT '处理时间',
    handle_note CLOB COMMENT '处理备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (attendance_id) REFERENCES attendance(attendance_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (handled_by) REFERENCES user(user_id),
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_is_handled (is_handled),
    INDEX idx_create_time (create_time)
) COMPRESS=1;
COMMENT ON TABLE anomaly IS '异常分析记录表';

-- =============================================
-- 15. 知识库表
-- =============================================
CREATE TABLE knowledge_base (
    knowledge_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category VARCHAR(50) NOT NULL COMMENT '分类',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content CLOB NOT NULL COMMENT '内容',
    keywords VARCHAR(500) COMMENT '关键词',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    view_count INT NOT NULL DEFAULT 0 COMMENT '查看次数',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_category (category),
    INDEX idx_keywords (keywords),
    INDEX idx_is_active (is_active),
    INDEX idx_create_time (create_time)
) COMPRESS=1;
COMMENT ON TABLE knowledge_base IS '知识库表';

-- =============================================
-- 插入初始数据
-- =============================================

-- 插入默认角色
INSERT INTO role (role_name, role_key, description) VALUES
('学生', 'student', '普通学生用户'),
('管理员', 'admin', '自习室管理员'),
('超级管理员', 'super_admin', '系统超级管理员');

-- 插入默认用户（密码：123456）
INSERT INTO user (username, password, real_name, student_no, email, role) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVYITi', '管理员', 'admin001', 'admin@campus.edu', 'super_admin'),
('student1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVYITi', '张三', '20230001', 'zhangsan@campus.edu', 'student'),
('student2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVYITi', '李四', '20230002', 'lisi@campus.edu', 'student');

-- 插入管理员角色关联
INSERT INTO user_role (user_id, role_id) VALUES
(1, 3); -- 超级管理员

-- 插入基础权限（示例）
INSERT INTO permission (permission_name, permission_key, permission_type, parent_id) VALUES
('首页', 'dashboard', 'menu', 0),
('自习室管理', 'room', 'menu', 0),
('预约管理', 'reservation', 'menu', 0),
('考勤管理', 'attendance', 'menu', 0),
('AI智能', 'ai', 'menu', 0),
('用户管理', 'user', 'menu', 0),
('系统设置', 'system', 'menu', 0),
('查看自习室列表', 'room:list', 'button', 2),
('添加自习室', 'room:add', 'button', 2),
('修改自习室', 'room:edit', 'button', 2),
('删除自习室', 'room:delete', 'button', 2),
('查看预约列表', 'reservation:list', 'button', 3),
('取消预约', 'reservation:cancel', 'button', 3),
('创建预约', 'reservation:create', 'button', 3);

-- 插入超级管理员权限
INSERT INTO role_permission (role_id, permission_id)
SELECT 3, permission_id FROM permission;

-- 插入示例自习室
INSERT INTO study_room (room_name, building, floor, capacity, facilities, open_time, close_time) VALUES
('一号自习室', '主教学楼A', 3, 100, '{"wifi": true, "power": true, "ac": true}', '07:00:00', '22:00:00'),
('二号自习室', '主教学楼B', 4, 80, '{"wifi": true, "power": true, "ac": false}', '08:00:00', '22:00:00'),
('三号自习室', '图书馆', 2, 150, '{"wifi": true, "power": true, "ac": true, "printer": true}', '07:30:00', '21:30:00');

-- 插入示例座位
INSERT INTO study_seat (room_id, seat_number, zone, position_x, position_y, status, type, has_power, has_usb, has_wifi)
SELECT r.room_id,
       CONCAT('A', seq.n) AS seat_number,
       'A区' AS zone,
       (seq.n - 1) % 10 AS position_x,
       (seq.n - 1) / 10 AS position_y,
       'available' AS status,
       CASE WHEN seq.n % 4 = 1 THEN 'window'
            WHEN seq.n % 4 = 2 THEN 'power'
            WHEN seq.n % 4 = 3 THEN 'corner'
            ELSE 'normal' END AS type,
       CASE WHEN seq.n % 2 = 0 THEN 1 ELSE 0 END AS has_power,
       CASE WHEN seq.n % 3 = 0 THEN 1 ELSE 0 END AS has_usb,
       1 AS has_wifi
FROM study_room r
CROSS JOIN (SELECT 1 AS n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
            UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) seq
WHERE r.deleted = 0;

-- 插入示例时间段
INSERT INTO time_slot (room_id, start_time, end_time, max_reservations)
SELECT room_id,
       TO_TIME('07:00:00' + INTERVAL (seq - 1) HOUR),
       TO_TIME('08:00:00' + INTERVAL (seq - 1) HOUR),
       5
FROM study_room
CROSS JOIN (SELECT 1 AS seq UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6
            UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10 UNION SELECT 11
            UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15) AS numbers;

-- 插入示例预约记录
INSERT INTO reservation (user_id, room_id, seat_id, slot_id, reserve_date, start_time, end_time, status)
SELECT 2, room_id, seat_id, slot_id,
       TO_DATE(CURRENT_DATE + INTERVAL FLOOR(RAND() * 7) DAY),
       start_time, end_time, 'CONFIRMED'
FROM study_room
CROSS JOIN seat
CROSS JOIN time_slot
WHERE seat.room_id = study_room.room_id
  AND time_slot.room_id = study_room.room_id
  AND seat.status = 'AVAILABLE'
  AND time_slot.is_active = TRUE
LIMIT 20;

-- 插入示例考勤记录
INSERT INTO attendance (reservation_id, user_id, check_in_time, check_out_time, duration, status)
SELECT reservation_id, user_id,
       TO_TIMESTAMP(TO_DATE(reserve_date) + HOUR(start_time) || ':' || MINUTE(start_time) || ':00') + INTERVAL FLOOR(RAND() * 10) MINUTE,
       TO_TIMESTAMP(TO_DATE(reserve_date) + HOUR(end_time) || ':' || MINUTE(end_time) || ':00') - INTERVAL FLOOR(RAND() * 30) MINUTE,
       60 + FLOOR(RAND() * 120),
       CASE WHEN FLOOR(RAND() * 10) = 0 THEN 'LATE'
            WHEN FLOOR(RAND() * 10) = 1 THEN 'EARLY_LEAVE'
            ELSE 'NORMAL' END
FROM reservation
WHERE status = 'CONFIRMED'
LIMIT 10;

-- 插入示例知识库
INSERT INTO knowledge_base (category, title, content, keywords) VALUES
('预约规则', '如何创建预约', '1. 登录系统\n2. 选择自习室\n3. 选择座位和时间段\n4. 确认预约\n5. 记得按时签到', '预约,创建,步骤'),
('考勤说明', '签到签退规则', '1. 必须在预约时间前15分钟内签到\n2. 签到后30分钟内签退视为短时占座\n3. 迟到或早退会被记录', '签到,签退,规则'),
('违规处罚', '违规行为处罚', '1. 预约未签到：限制预约3天\n2. 短时占座：限制预约1天\n3. 频繁取消：限制预约2天', '违规,处罚,规则');

-- 创建序列用于某些场景
CREATE SEQUENCE seq_reservation_id INCREMENT 1 START 1 CACHE 20;
CREATE SEQUENCE seq_user_id INCREMENT 1 START 1 CACHE 20;