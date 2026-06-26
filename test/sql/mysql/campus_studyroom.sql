-- =============================================
-- 校园自习室预约系统数据库 - MySQL版本
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS campus_studyroom CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
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
    study_preferences JSON COMMENT '学习偏好(JSON格式)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记(0-未删除 1-已删除)',
    INDEX idx_username (username),
    INDEX idx_student_no (student_no),
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

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
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_role_key (role_key),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

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
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_permission_key (permission_key),
    INDEX idx_parent_id (parent_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- =============================================
-- 6. 自习室表
-- =============================================
CREATE TABLE study_room (
    room_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_name VARCHAR(100) NOT NULL COMMENT '自习室名称',
    building VARCHAR(50) NOT NULL COMMENT '教学楼',
    floor INT NOT NULL COMMENT '楼层',
    capacity INT NOT NULL COMMENT '容量',
    current_count INT NOT NULL DEFAULT 0 COMMENT '当前人数',
    facilities JSON COMMENT '设施信息(JSON格式)',
    open_time TIME NOT NULL COMMENT '开放时间',
    close_time TIME NOT NULL COMMENT '关闭时间',
    status VARCHAR(20) NOT NULL DEFAULT 'open' COMMENT '状态(open/closed/maintenance)',
    description VARCHAR(500) COMMENT '自习室描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_building_floor (building, floor),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自习室表';

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
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_room_id (room_id),
    INDEX idx_seat_number (seat_number),
    INDEX idx_status (status),
    FOREIGN KEY (room_id) REFERENCES study_room(room_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='座位表';

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
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_room_id (room_id),
    INDEX idx_start_end_time (start_time, end_time),
    FOREIGN KEY (room_id) REFERENCES study_room(room_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='时间段模板表';

-- =============================================
-- 9. 预约表
-- =============================================
CREATE TABLE reservation (
    reservation_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    room_id BIGINT NOT NULL COMMENT '自习室ID',
    seat_id BIGINT NOT NULL COMMENT '座位ID',
    slot_id BIGINT COMMENT '时间段ID（可选）',
    reserve_date DATE NOT NULL COMMENT '预约日期',
    reservation_date DATETIME COMMENT '预约日期时间(冗余)',
    start_time TIME NOT NULL COMMENT '开始时间',
    end_time TIME NOT NULL COMMENT '结束时间',
    status ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'CHECKED_IN', 'COMPLETED', 'EXPIRED', 'VIOLATED') NOT NULL DEFAULT 'PENDING' COMMENT '状态',
    purpose VARCHAR(200) COMMENT '预约目的',
    notes VARCHAR(500) COMMENT '备注',
    qrcode VARCHAR(255) COMMENT '二维码',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    check_in_time DATETIME COMMENT '签到时间',
    check_out_time DATETIME COMMENT '签退时间',
    penalty_days INT NOT NULL DEFAULT 0 COMMENT '处罚天数',
    violation_id BIGINT COMMENT '违规ID',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记(0-未删除 1-已删除)',
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES study_room(room_id) ON DELETE CASCADE,
    FOREIGN KEY (seat_id) REFERENCES study_seat(seat_id) ON DELETE CASCADE,
    FOREIGN KEY (slot_id) REFERENCES time_slot(slot_id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_room_seat_date (room_id, seat_id, reserve_date),
    INDEX idx_reserve_date (reserve_date),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约表';

-- =============================================
-- 10. 考勤表
-- =============================================
CREATE TABLE attendance (
    attendance_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reservation_id BIGINT NOT NULL COMMENT '预约ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    room_id BIGINT NOT NULL COMMENT '自习室ID',
    seat_id BIGINT NOT NULL COMMENT '座位ID',
    check_in_time DATETIME COMMENT '签到时间',
    check_out_time DATETIME COMMENT '签退时间',
    duration_minutes INT NOT NULL DEFAULT 0 COMMENT '学习时长(分钟)',
    current_count INT NOT NULL DEFAULT 0 COMMENT '当前人数',
    status VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态(active/completed/timeout/cancelled)',
    check_in_method VARCHAR(20) COMMENT '签到方式(qrcode/manual/auto)',
    check_out_method VARCHAR(20) COMMENT '签退方式(qrcode/manual/auto)',
    location VARCHAR(200) COMMENT '签到位置',
    device_info VARCHAR(500) COMMENT '设备信息',
    notes VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记(0-未删除 1-已删除)',
    FOREIGN KEY (reservation_id) REFERENCES reservation(reservation_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES study_room(room_id) ON DELETE CASCADE,
    FOREIGN KEY (seat_id) REFERENCES study_seat(seat_id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_reservation_id (reservation_id),
    INDEX idx_room_id (room_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤表';

-- =============================================
-- 10.5 考勤规则表
-- =============================================
CREATE TABLE attendance_rule (
    rule_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    room_id BIGINT COMMENT '适用自习室ID（NULL表示全局）',
    start_time TIME COMMENT '允许签到开始时间',
    end_time TIME COMMENT '允许签到结束时间',
    max_duration INT COMMENT '最大学习时长（分钟）',
    min_duration INT COMMENT '最小时长（分钟）',
    allow_extend BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否允许延长',
    max_extend_times INT NOT NULL DEFAULT 0 COMMENT '最大延长次数',
    max_extend_duration INT NOT NULL DEFAULT 0 COMMENT '最大延长时长（分钟）',
    auto_check_out BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否自动签退',
    check_out_delay INT NOT NULL DEFAULT 0 COMMENT '自动签退延迟（分钟）',
    status ENUM('active', 'inactive') NOT NULL DEFAULT 'active' COMMENT '状态',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_room_id (room_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤规则表';

-- 插入默认考勤规则（每个自习室一条，开放时间内允许签到）
INSERT INTO attendance_rule (rule_name, room_id, start_time, end_time, max_duration, min_duration, allow_extend, max_extend_times, max_extend_duration, auto_check_out, check_out_delay, status)
SELECT CONCAT('默认规则-', room_name), room_id, open_time, close_time, 240, 30, TRUE, 2, 60, TRUE, 30, 'active'
FROM study_room;

-- =============================================
-- 10.6 考勤操作记录表
-- =============================================
CREATE TABLE attendance_record (
    record_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    attendance_id BIGINT NOT NULL COMMENT '考勤ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    room_id BIGINT COMMENT '自习室ID',
    seat_id BIGINT COMMENT '座位ID',
    action VARCHAR(50) NOT NULL COMMENT '操作类型(check_in/check_out/extend/timeout)',
    action_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    action_user BIGINT COMMENT '操作人ID',
    device_info VARCHAR(500) COMMENT '设备信息',
    location VARCHAR(200) COMMENT '位置信息',
    notes VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_attendance_id (attendance_id),
    INDEX idx_user_id (user_id),
    INDEX idx_action (action),
    INDEX idx_action_time (action_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤操作记录表';

-- =============================================
-- 11. 违规记录表
-- =============================================
CREATE TABLE violation (
    violation_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    reservation_id BIGINT NOT NULL COMMENT '预约ID',
    type ENUM('NO_SHOW', 'LATE_CHECK_IN', 'EARLY_LEAVE', 'DAMAGE') NOT NULL COMMENT '违规类型',
    description VARCHAR(500) NOT NULL COMMENT '违规描述',
    penalty_days INT NOT NULL DEFAULT 0 COMMENT '处罚天数',
    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING' COMMENT '状态',
    appeal_reason VARCHAR(500) COMMENT '申诉理由',
    appeal_status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING' COMMENT '申诉状态',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_reservation_id (reservation_id),
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (reservation_id) REFERENCES reservation(reservation_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='违规记录表';

-- =============================================
-- 12. 通知记录表
-- =============================================
CREATE TABLE notification (
    notification_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    type ENUM('RESERVE_CONFIRM', 'TIMEOUT_REMIND', 'VIOLATION_NOTIFY', 'SYSTEM_ANNOUNCE') NOT NULL COMMENT '通知类型',
    title VARCHAR(100) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    is_read BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已读',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_is_read (is_read),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知记录表';

-- =============================================
-- 13. AI推荐记录表
-- =============================================
CREATE TABLE ai_recommendation (
    recommendation_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    room_id BIGINT NOT NULL COMMENT '自习室ID',
    seat_id BIGINT NOT NULL COMMENT '座位ID',
    score DECIMAL(10,2) NOT NULL COMMENT '推荐得分',
    reason TEXT COMMENT '推荐理由',
    strategy VARCHAR(50) NOT NULL COMMENT '推荐策略',
    is_accepted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否被接受',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES study_room(room_id) ON DELETE CASCADE,
    FOREIGN KEY (seat_id) REFERENCES study_seat(seat_id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_score (score),
    INDEX idx_strategy (strategy),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI推荐记录表';

-- =============================================
-- 14. 异常分析记录表
-- =============================================
CREATE TABLE anomaly (
    anomaly_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    attendance_id BIGINT NOT NULL COMMENT '考勤ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    type ENUM('FREQUENT_NO_SHOW', 'SUSPICIOUS_PATTERN', 'ABNORMAL_DURATION', 'PROXY_CHECK_IN') NOT NULL COMMENT '异常类型',
    description TEXT NOT NULL COMMENT '异常描述',
    confidence DECIMAL(5,2) NOT NULL COMMENT '置信度(0-100)',
    is_handled BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已处理',
    handled_by BIGINT COMMENT '处理人ID',
    handle_time DATETIME COMMENT '处理时间',
    handle_note TEXT COMMENT '处理备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (attendance_id) REFERENCES attendance(attendance_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (handled_by) REFERENCES user(user_id),
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_is_handled (is_handled),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='异常分析记录表';

-- =============================================
-- 15. 知识库表
-- =============================================
CREATE TABLE knowledge_base (
    knowledge_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category VARCHAR(50) NOT NULL COMMENT '分类',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    keywords VARCHAR(500) COMMENT '关键词',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    view_count INT NOT NULL DEFAULT 0 COMMENT '查看次数',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_category (category),
    INDEX idx_keywords (keywords),
    INDEX idx_is_active (is_active),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库表';

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
('admin', '$2a$10$WDsxJb5oaDbRADic42U9t.QrZsfak5rmr4IkSLJq9o170Ek/Vg/v6', '管理员', 'admin001', 'admin@campus.edu', 'super_admin'),
('student1', '$2a$10$WDsxJb5oaDbRADic42U9t.QrZsfak5rmr4IkSLJq9o170Ek/Vg/v6', '张三', '20230001', 'zhangsan@campus.edu', 'student'),
('student2', '$2a$10$WDsxJb5oaDbRADic42U9t.QrZsfak5rmr4IkSLJq9o170Ek/Vg/v6', '李四', '20230002', 'lisi@campus.edu', 'student');

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
('一号自习室', '主教学楼A', 3, 100, '{"wifi": true, "power": true, "ac": true}', '07:00', '22:00'),
('二号自习室', '主教学楼B', 4, 80, '{"wifi": true, "power": true, "ac": false}', '08:00', '22:00'),
('三号自习室', '图书馆', 2, 150, '{"wifi": true, "power": true, "ac": true, "printer": true}', '07:30', '21:30');

-- 插入示例座位（每个自习室按 capacity 生成对应数量）
INSERT INTO study_seat (room_id, seat_number, zone, position_x, position_y, status, type, has_power, has_usb, has_wifi)
WITH RECURSIVE numbers AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM numbers WHERE n < (SELECT MAX(capacity) FROM study_room)
)
SELECT r.room_id,
       CONCAT('A', n.n) AS seat_number,
       'A区' AS zone,
       (n.n - 1) % 10 AS position_x,
       (n.n - 1) / 10 AS position_y,
       'available' AS status,
       CASE WHEN n.n % 4 = 1 THEN 'window'
            WHEN n.n % 4 = 2 THEN 'power'
            WHEN n.n % 4 = 3 THEN 'corner'
            ELSE 'normal' END AS type,
       n.n % 2 = 0 AS has_power,
       n.n % 3 = 0 AS has_usb,
       TRUE AS has_wifi
FROM study_room r
CROSS JOIN numbers n
WHERE n.n <= r.capacity
  AND r.deleted = 0;

-- 插入示例时间段
INSERT INTO time_slot (room_id, start_time, end_time, max_reservations)
SELECT room_id,
       ADDTIME('07:00:00', SEC_TO_TIME((seq - 1) * 3600)),
       ADDTIME('08:00:00', SEC_TO_TIME((seq - 1) * 3600)),
       5
FROM study_room
CROSS JOIN (SELECT 1 AS seq UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6
            UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10 UNION SELECT 11
            UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15) AS slots;

-- 插入示例预约记录（每个自习室 5 条，确保推荐数据覆盖全部房间）
INSERT INTO reservation (user_id, room_id, seat_id, slot_id, reserve_date, start_time, end_time, status)
SELECT 2, r.room_id, s.seat_id, ts.slot_id,
       CURDATE() + INTERVAL FLOOR(RAND() * 7) DAY,
       ts.start_time, ts.end_time, 'CONFIRMED'
FROM study_room r
CROSS JOIN (
    SELECT room_id, seat_id,
           ROW_NUMBER() OVER (PARTITION BY room_id ORDER BY seat_id) AS rn
    FROM study_seat
    WHERE status = 'available' AND deleted = 0
) s ON s.room_id = r.room_id AND s.rn <= 5
CROSS JOIN (
    SELECT room_id, slot_id, start_time, end_time,
           ROW_NUMBER() OVER (PARTITION BY room_id ORDER BY slot_id) AS rn
    FROM time_slot
    WHERE is_active = TRUE
) ts ON ts.room_id = r.room_id AND ts.rn = ((s.rn - 1) % 5) + 1;

-- 插入示例考勤记录
INSERT INTO attendance (reservation_id, user_id, room_id, seat_id, check_in_time, check_out_time, duration_minutes, status, check_in_method, check_out_method)
SELECT reservation_id, user_id, room_id, seat_id,
       DATE_ADD(DATE_ADD(reserve_date, INTERVAL HOUR(start_time) HOUR), INTERVAL MINUTE(start_time) MINUTE) + INTERVAL FLOOR(RAND() * 10) MINUTE,
       DATE_ADD(DATE_ADD(reserve_date, INTERVAL HOUR(end_time) HOUR), INTERVAL MINUTE(end_time) MINUTE) - INTERVAL FLOOR(RAND() * 30) MINUTE,
       60 + FLOOR(RAND() * 120),
       CASE WHEN FLOOR(RAND() * 10) = 0 THEN 'timeout'
            WHEN FLOOR(RAND() * 10) = 1 THEN 'completed'
            ELSE 'active' END,
       'qrcode',
       'manual'
FROM reservation
WHERE status = 'CONFIRMED'
LIMIT 10;

-- 插入示例知识库
INSERT INTO knowledge_base (category, title, content, keywords) VALUES
('预约规则', '如何创建预约', '1. 登录系统\n2. 选择自习室\n3. 选择座位和时间段\n4. 确认预约\n5. 记得按时签到', '预约,创建,步骤'),
('考勤说明', '签到签退规则', '1. 必须在预约时间前15分钟内签到\n2. 签到后30分钟内签退视为短时占座\n3. 迟到或早退会被记录', '签到,签退,规则'),
('违规处罚', '违规行为处罚', '1. 预约未签到：限制预约3天\n2. 短时占座：限制预约1天\n3. 频繁取消：限制预约2天', '违规,处罚,规则');