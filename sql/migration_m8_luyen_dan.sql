-- M8 Tu Tien — He Luyen Dan (luyen dan su + dan phuong).
-- Tao 2 bang: dan phuong (cong thuc) + nguyen lieu cua tung dan phuong (bang con).
-- Nguyen lieu = item_template 2017..2038 (da co, migration rieng). Vien dan (pill_item_id) = item_template
--   loai dan duoc (TYPE 27). Sample duoi dung tam 4 vien dan M2 (1998..2001) de test ngay; thay bang dan that sau.
-- Chay tren tomahoc_db SAU khi da co material item 2017..2038:
--   mysql -u root -p tomahoc_db < migration_m8_luyen_dan.sql
-- Server tu load 2 bang nay luc khoi dong (tutien.DanPhuongTemplate.load()); doi data -> "tutien danphuong reload".
-- Verify: SELECT * FROM tutien_dan_phuong; SELECT * FROM tutien_dan_phuong_nl ORDER BY recipe_id;

-- ── Bang dan phuong (cong thuc luyen dan) ────────────────────────────────────
CREATE TABLE IF NOT EXISTS `tutien_dan_phuong` (
  `id`           INT          NOT NULL,                 -- id dan phuong (PK)
  `name`         VARCHAR(64)  NOT NULL DEFAULT '',      -- ten dan (rong = lay ten item pill_item_id)
  `pill_item_id` INT          NOT NULL,                 -- item_template vien dan (output)
  `realm`        TINYINT      NOT NULL,                 -- canh gioi 1..10 (nhom hien thi: 1 Luyen Khi ... 6 Nguyen Anh ...)
  `req_level`    TINYINT      NOT NULL,                 -- cap luyen dan su CAN co (BAT BUOC tu khai)
  `description`  VARCHAR(255) NOT NULL DEFAULT '',      -- mo ta (tuy chon)
  `is_active`    TINYINT      NOT NULL DEFAULT 1,       -- 0 = an khoi danh sach
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Bang con: nguyen lieu cua moi dan phuong ─────────────────────────────────
CREATE TABLE IF NOT EXISTS `tutien_dan_phuong_nl` (
  `recipe_id` INT NOT NULL,                             -- = tutien_dan_phuong.id
  `item_id`   INT NOT NULL,                             -- = item_template.id (nguyen lieu)
  `qty`       INT NOT NULL DEFAULT 1,                   -- so luong can
  PRIMARY KEY (`recipe_id`, `item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Sample data (testable ngay; thay bang dan that sau) ──────────────────────
INSERT INTO `tutien_dan_phuong` (`id`, `name`, `pill_item_id`, `realm`, `req_level`, `description`, `is_active`) VALUES
  (1, 'Tụ Khí Đan',     1998, 1, 1, 'Đan nhập môn Luyện Khí — tăng tốc tu luyện.',        1),
  (2, 'Bồi Nguyên Đan',  1999, 1, 1, 'Củng cố căn cơ, bồi bổ tu vi.',                      1),
  (3, 'Nguyên Linh Đan', 2000, 2, 2, 'Đan Trúc Cơ — hồi phục linh lực dồi dào.',           1),
  (4, 'Hoàn Hồn Đan',    2001, 2, 2, 'Đan Trúc Cơ — cứu nguy khi tuổi thọ cạn.',           1)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `pill_item_id` = VALUES(`pill_item_id`),
                        `realm` = VALUES(`realm`), `req_level` = VALUES(`req_level`),
                        `description` = VALUES(`description`), `is_active` = VALUES(`is_active`);

INSERT INTO `tutien_dan_phuong_nl` (`recipe_id`, `item_id`, `qty`) VALUES
  (1, 2017, 3), (1, 2018, 2),                           -- Tụ Khí Đan: Ngưng Khí Thảo x3 + Tụ Linh Thảo x2
  (2, 2019, 2), (2, 2020, 2),                           -- Bồi Nguyên Đan: Huyết Thảo x2 + Thanh Tâm Thảo x2
  (3, 2021, 3), (3, 2022, 2), (3, 2023, 1),             -- Nguyên Linh Đan: Hàn Sương x3 + Xích Dương x2 + Tử Vân Hoa x1
  (4, 2024, 2), (4, 2025, 2)                            -- Hoàn Hồn Đan: Bạch Nguyệt Hoa x2 + Linh Chi Ngàn Năm x2
ON DUPLICATE KEY UPDATE `qty` = VALUES(`qty`);
