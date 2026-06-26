-- Fix existing campus_studyroom database to match current entity definitions
USE campus_studyroom;

-- Update default user passwords to match plain text 123456
UPDATE user SET password = '$2a$10$WDsxJb5oaDbRADic42U9t.QrZsfak5rmr4IkSLJq9o170Ek/Vg/v6'
WHERE username IN ('admin', 'student1', 'student2');

-- Fix study_room table
SET @col = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'campus_studyroom' AND TABLE_NAME = 'study_room' AND COLUMN_NAME = 'current_count');
SET @sql = IF(@col = 0,
    'ALTER TABLE study_room ADD COLUMN current_count INT NOT NULL DEFAULT 0 COMMENT "current people count"',
    'SELECT "study_room.current_count already exists" AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Convert study_room.status from TINYINT to VARCHAR if needed
SET @col_type = (SELECT DATA_TYPE FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'campus_studyroom' AND TABLE_NAME = 'study_room' AND COLUMN_NAME = 'status');
SET @sql = IF(@col_type = 'tinyint',
    'ALTER TABLE study_room ADD COLUMN status_new VARCHAR(20) NOT NULL DEFAULT "open" COMMENT "status" AFTER status',
    'SELECT "study_room.status already string" AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(@col_type = 'tinyint',
    'UPDATE study_room SET status_new = CASE status WHEN 1 THEN "open" WHEN 2 THEN "maintenance" ELSE "closed" END',
    'SELECT "study_room.status already string" AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(@col_type = 'tinyint',
    'ALTER TABLE study_room DROP COLUMN status',
    'SELECT "study_room.status already string" AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(@col_type = 'tinyint',
    'ALTER TABLE study_room CHANGE COLUMN status_new status VARCHAR(20) NOT NULL DEFAULT "open" COMMENT "status"',
    'SELECT "study_room.status already string" AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Fix reservation table
SET @col = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'campus_studyroom' AND TABLE_NAME = 'reservation' AND COLUMN_NAME = 'reservation_date');
SET @sql = IF(@col = 0,
    'ALTER TABLE reservation ADD COLUMN reservation_date DATETIME COMMENT "reservation datetime" AFTER reserve_date',
    'SELECT "reservation.reservation_date already exists" AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'campus_studyroom' AND TABLE_NAME = 'reservation' AND COLUMN_NAME = 'purpose');
SET @sql = IF(@col = 0,
    'ALTER TABLE reservation ADD COLUMN purpose VARCHAR(200) COMMENT "purpose" AFTER status',
    'SELECT "reservation.purpose already exists" AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'campus_studyroom' AND TABLE_NAME = 'reservation' AND COLUMN_NAME = 'notes');
SET @sql = IF(@col = 0,
    'ALTER TABLE reservation ADD COLUMN notes VARCHAR(500) COMMENT "notes" AFTER purpose',
    'SELECT "reservation.notes already exists" AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'campus_studyroom' AND TABLE_NAME = 'reservation' AND COLUMN_NAME = 'qrcode');
SET @sql = IF(@col = 0,
    'ALTER TABLE reservation ADD COLUMN qrcode VARCHAR(255) COMMENT "qrcode" AFTER notes',
    'SELECT "reservation.qrcode already exists" AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'campus_studyroom' AND TABLE_NAME = 'reservation' AND COLUMN_NAME = 'update_time');
SET @sql = IF(@col = 0,
    'ALTER TABLE reservation ADD COLUMN update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT "update time" AFTER create_time',
    'SELECT "reservation.update_time already exists" AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add deleted columns if not exist
SET @col = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'campus_studyroom' AND TABLE_NAME = 'reservation' AND COLUMN_NAME = 'deleted');
SET @sql = IF(@col = 0,
    'ALTER TABLE reservation ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT "logic delete flag" AFTER violation_id',
    'SELECT "reservation.deleted already exists" AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'campus_studyroom' AND TABLE_NAME = 'attendance' AND COLUMN_NAME = 'deleted');
SET @sql = IF(@col = 0,
    'ALTER TABLE attendance ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT "logic delete flag" AFTER create_time',
    'SELECT "attendance.deleted already exists" AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Create attendance_rule table if not exists
CREATE TABLE IF NOT EXISTS attendance_rule (
    rule_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rule_name VARCHAR(100) NOT NULL COMMENT 'rule name',
    room_id BIGINT COMMENT 'room id (NULL for global)',
    start_time TIME COMMENT 'allowed check-in start time',
    end_time TIME COMMENT 'allowed check-in end time',
    max_duration INT COMMENT 'max study duration in minutes',
    min_duration INT COMMENT 'min study duration in minutes',
    allow_extend BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'allow extend time',
    max_extend_times INT NOT NULL DEFAULT 0 COMMENT 'max extend times',
    max_extend_duration INT NOT NULL DEFAULT 0 COMMENT 'max extend duration in minutes',
    auto_check_out BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'auto check out enabled',
    check_out_delay INT NOT NULL DEFAULT 0 COMMENT 'auto check out delay in minutes',
    status ENUM('active', 'inactive') NOT NULL DEFAULT 'active' COMMENT 'status',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_room_id (room_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='attendance rule table';
