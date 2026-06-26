-- Fix missing deleted columns for MyBatis-Plus logic delete
USE campus_studyroom;

ALTER TABLE reservation ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记(0-未删除 1-已删除)' AFTER violation_id;
ALTER TABLE attendance ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记(0-未删除 1-已删除)' AFTER create_time;
