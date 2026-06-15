-- M2 Tu Tien (docs/tutien-m1-plan.md Roadmap M2, req docs/req/req.md §8-10): 4 vien dan duoc.
-- TYPE = 27 (vat pham dung-tu-tui, client tu hien "Su dung"); icon_id 32468-32471 = server/data/icon/x4/{id}.png
-- (server tu gui icon ve client qua DataGame.sendIcon; KHONG bundle vao client smallimage).
-- Re-runnable: ON DUPLICATE KEY UPDATE -> chay lai file nay se sua icon_id cho DB da deploy ban cu.
-- Chay tren tomahoc_db SAU migration_m1_data_tutien.sql:
--   mysql -u root -p tomahoc_db < migration_m2_dan_duoc.sql
-- Verify: SELECT id, NAME FROM item_template WHERE id BETWEEN 1998 AND 2001;
-- Nguon (dot nay): lenh admin "tutien dan <1-4> [soluong]".

INSERT INTO `item_template`
  (`id`, `TYPE`, `gender`, `NAME`, `description`, `level`, `icon_id`, `part`, `is_up_to_up`,
   `power_require`, `gold`, `gem`, `head`, `body`, `leg`, `is_up_to_up_over_99`, `can_trade`, `comment`)
VALUES
  (1998, 27, 3, 'Tụ Khí Đan',           'Tăng 100% tốc độ tu luyện trong 1 năm tu tiên',           1, 32468, -1, 0, 0, 0, 0, -1, -1, -1, 0, 1, ''),
  (1999, 27, 3, 'Bồi Nguyên Đan',        'Tăng tu vi và củng cố căn cơ (hiệu quả giảm theo cảnh giới)', 1, 32469, -1, 0, 0, 0, 0, -1, -1, -1, 0, 1, ''),
  (2000, 27, 3, 'Nguyên Linh Đan',       'Hồi phục 100% linh lực, chỉ hiệu lực trong Luyện Khí Kỳ', 1, 32470, -1, 0, 0, 0, 0, -1, -1, -1, 0, 1, ''),
  (2001, 27, 3, 'Âm Dương Hoàn Hồn Đan', 'Khôi phục trạng thái tu tiên sau khi tuổi thọ cạn kiệt',  1, 32471, -1, 0, 0, 0, 0, -1, -1, -1, 0, 1, '')
ON DUPLICATE KEY UPDATE `icon_id` = VALUES(`icon_id`), `NAME` = VALUES(`NAME`),
                        `description` = VALUES(`description`), `TYPE` = VALUES(`TYPE`);
