-- M9 Tu Tien — Tien Duyen (item 2039): tien te MO KHOA tu tien & luyen dan.
--   * Roi tu quai (1/1000) & boss (1/20), moi proc 1-3 vien (Mob.java / Boss.java -> TuTienService.rollDropTienDuyen).
--   * Hoc Tu Tien (Quy Lao) can 150 Tien Duyen; Hoc Luyen Dan can dat Luyen Khi Tang 4 + 100 Tien Duyen.
--   * Dot pha tieu Linh Thach (tang x3); luyen dan tieu Linh Thach (req_level x1) — item 2016 da co (migration_m7).
-- icon_id 32487 = server/data/icon/x4/32487.png (da co san; client tai icon tu server nhu Linh Thach 32486).
-- TYPE 27 = item tieu hao (giong Linh Thach); "dung" chi hien thong bao huong dan, KHONG tieu (UseItem case 2039).
-- can_trade = 0 (bind tai khoan). LUU Y: server nay KHONG doc cot can_trade -> chan trade THAT o
--   Trade.isItemCannotTran (hardcode case 2039). Cot nay chi de tai lieu/tuong thich.
-- Re-runnable (ON DUPLICATE KEY UPDATE). Chay:  mysql -u root -p tomahoc_db < migration_m9_tien_duyen.sql
-- Verify: SELECT id,NAME,icon_id,TYPE FROM item_template WHERE id=2039;

INSERT INTO `item_template`
  (`id`, `TYPE`, `gender`, `NAME`, `description`, `level`, `icon_id`, `part`, `is_up_to_up`,
   `power_require`, `gold`, `gem`, `head`, `body`, `leg`, `is_up_to_up_over_99`, `can_trade`, `comment`)
VALUES
  (2039, 27, 3, 'Tiên Duyên',
   'Cơ duyên với tiên đạo, ngưng tụ từ sinh linh bị đánh bại. Cần 150 để học Tu Tiên và 100 để học Luyện Đan tại Quy Lão (Đảo Kame).',
   1, 32487, -1, 0, 0, 0, 0, -1, -1, -1, 0, 0, '')
ON DUPLICATE KEY UPDATE `icon_id` = VALUES(`icon_id`), `NAME` = VALUES(`NAME`),
                        `description` = VALUES(`description`), `TYPE` = VALUES(`TYPE`),
                        `can_trade` = VALUES(`can_trade`);
