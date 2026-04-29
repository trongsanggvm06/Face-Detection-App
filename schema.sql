-- ============================================================
-- schema.sql – Face Detection App
-- Tạo database và bảng cho ứng dụng nhận diện khuôn mặt
--
-- Hướng dẫn sử dụng:
--   1. Mở phpMyAdmin (http://localhost/phpmyadmin)
--   2. Vào tab SQL → paste nội dung file này → click Go
--   HOẶC chạy lệnh: mysql -u root -p < schema.sql
-- ============================================================

-- Tạo database nếu chưa tồn tại
CREATE DATABASE IF NOT EXISTS detection_log
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Chọn database để làm việc
USE detection_log;

-- ============================================================
-- Bảng detection_log
-- Lưu lịch sử mỗi lần nhận diện khuôn mặt
-- ============================================================
CREATE TABLE IF NOT EXISTS detection_log (
    id          INT          AUTO_INCREMENT PRIMARY KEY COMMENT 'ID tự tăng',
    person_name VARCHAR(255) NOT NULL                  COMMENT 'Tên người được nhận diện (Unknown nếu không xác định)',
    timestamp   DATETIME     NOT NULL
                             DEFAULT CURRENT_TIMESTAMP  COMMENT 'Thời điểm nhận diện',
    score       DOUBLE       NOT NULL
                             DEFAULT 0.0               COMMENT 'Độ tin cậy của kết quả nhận diện (0.0 – 1.0)'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Lịch sử nhận diện khuôn mặt';

-- ============================================================
-- Dữ liệu mẫu để test (có thể xóa sau khi xác nhận hoạt động)
-- ============================================================
INSERT INTO detection_log (person_name, timestamp, score) VALUES
    ('Nguyen Van A', '2026-04-30 08:00:01', 0.9231),
    ('Unknown',      '2026-04-30 08:00:15', 0.4512),
    ('Tran Thi B',   '2026-04-30 08:01:02', 0.8750);

-- ============================================================
-- Kiểm tra kết quả
-- ============================================================
SELECT * FROM detection_log;
