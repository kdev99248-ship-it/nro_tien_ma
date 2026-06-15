-- M4 Tu Tien (docs/tutien-m4-congphap-plan.md, req docs/req/req.md §6-7): 14 bi tich cong phap & vo hoc.
-- TYPE = 27 (vat pham dung-tu-tui, client tu hien "Su dung" -> UseItem.java goi useCongPhapItem).
-- icon_id 32472-32485 = server/data/icon/x4/{id}.png (DA co san; server gui qua DataGame.sendIcon).
--   item 2002+idx <-> idx 0-13 <-> icon 32472+idx (thu tu khop TuTienScr CONG_PHAP[0-6]+VO_HOC[7-13]).
-- Dung lan dau = hoc (Cap 1); dung lai khi da hoc = nhoi exp (~1 cap). Len cap chinh = exp khi tu luyen.
-- Re-runnable: ON DUPLICATE KEY UPDATE -> chay lai file nay se sua icon_id/ten cho DB da deploy.
-- Chay tren tomahoc_db SAU migration_m2_dan_duoc.sql:
--   mysql -u root -p tomahoc_db < migration_m4_cong_phap.sql
-- Verify: SELECT id, NAME, icon_id FROM item_template WHERE id BETWEEN 2002 AND 2015;
-- Nguon (dot nay): lenh admin "tutien cp <1-14> [soluong]".

INSERT INTO `item_template`
  (`id`, `TYPE`, `gender`, `NAME`, `description`, `level`, `icon_id`, `part`, `is_up_to_up`,
   `power_require`, `gold`, `gem`, `head`, `body`, `leg`, `is_up_to_up_over_99`, `can_trade`, `comment`)
VALUES
  (2002, 27, 3, 'Lôi Pháp Bí Tịch',    'Học Lôi Pháp — tăng mạnh Lôi linh căn. Tu luyện để lên cấp; dùng lại khi đã học để nhồi thêm exp.',     1, 32472, -1, 0, 0, 0, 0, -1, -1, -1, 0, 1, ''),
  (2003, 27, 3, 'Hỏa Pháp Bí Tịch',    'Học Hỏa Pháp — tăng mạnh Hỏa linh căn. Tu luyện để lên cấp; dùng lại khi đã học để nhồi thêm exp.',     1, 32473, -1, 0, 0, 0, 0, -1, -1, -1, 0, 1, ''),
  (2004, 27, 3, 'Thủy Pháp Bí Tịch',   'Học Thủy Pháp — tăng mạnh Thủy linh căn. Tu luyện để lên cấp; dùng lại khi đã học để nhồi thêm exp.',   1, 32474, -1, 0, 0, 0, 0, -1, -1, -1, 0, 1, ''),
  (2005, 27, 3, 'Phong Pháp Bí Tịch',  'Học Phong Pháp — tăng mạnh Phong linh căn. Tu luyện để lên cấp; dùng lại khi đã học để nhồi thêm exp.', 1, 32475, -1, 0, 0, 0, 0, -1, -1, -1, 0, 1, ''),
  (2006, 27, 3, 'Mộc Pháp Bí Tịch',    'Học Mộc Pháp — tăng mạnh Mộc linh căn. Tu luyện để lên cấp; dùng lại khi đã học để nhồi thêm exp.',     1, 32476, -1, 0, 0, 0, 0, -1, -1, -1, 0, 1, ''),
  (2007, 27, 3, 'Thổ Pháp Bí Tịch',    'Học Thổ Pháp — tăng mạnh Thổ linh căn. Tu luyện để lên cấp; dùng lại khi đã học để nhồi thêm exp.',     1, 32477, -1, 0, 0, 0, 0, -1, -1, -1, 0, 1, ''),
  (2008, 27, 3, 'Bách Pháp Bí Tịch',   'Học Bách Pháp — tăng cả 6 linh căn (hiệu quả thấp hơn công pháp chuyên hệ). Tu luyện để lên cấp.',      1, 32478, -1, 0, 0, 0, 0, -1, -1, -1, 0, 1, ''),
  (2009, 27, 3, 'Kiếm Phổ Bí Tịch',    'Học Kiếm Phổ — tăng Kiếm pháp tư chất. Tu luyện để lên cấp; dùng lại khi đã học để nhồi thêm exp.',      1, 32479, -1, 0, 0, 0, 0, -1, -1, -1, 0, 1, ''),
  (2010, 27, 3, 'Đao Phổ Bí Tịch',     'Học Đao Phổ — tăng Đao pháp tư chất. Tu luyện để lên cấp; dùng lại khi đã học để nhồi thêm exp.',        1, 32480, -1, 0, 0, 0, 0, -1, -1, -1, 0, 1, ''),
  (2011, 27, 3, 'Thương Phổ Bí Tịch',  'Học Thương Phổ — tăng Thương pháp tư chất. Tu luyện để lên cấp; dùng lại khi đã học để nhồi thêm exp.',  1, 32481, -1, 0, 0, 0, 0, -1, -1, -1, 0, 1, ''),
  (2012, 27, 3, 'Quyền Phổ Bí Tịch',   'Học Quyền Phổ — tăng Quyền pháp tư chất. Tu luyện để lên cấp; dùng lại khi đã học để nhồi thêm exp.',    1, 32482, -1, 0, 0, 0, 0, -1, -1, -1, 0, 1, ''),
  (2013, 27, 3, 'Chỉ Phổ Bí Tịch',     'Học Chỉ Phổ — tăng Chỉ pháp tư chất. Tu luyện để lên cấp; dùng lại khi đã học để nhồi thêm exp.',        1, 32483, -1, 0, 0, 0, 0, -1, -1, -1, 0, 1, ''),
  (2014, 27, 3, 'Chưởng Phổ Bí Tịch',  'Học Chưởng Phổ — tăng Chưởng pháp tư chất. Tu luyện để lên cấp; dùng lại khi đã học để nhồi thêm exp.',  1, 32484, -1, 0, 0, 0, 0, -1, -1, -1, 0, 1, ''),
  (2015, 27, 3, 'Bách Phổ Bí Tịch',    'Học Bách Phổ — tăng cả 6 võ học tư chất (hiệu quả thấp hơn các phổ chuyên môn). Tu luyện để lên cấp.',  1, 32485, -1, 0, 0, 0, 0, -1, -1, -1, 0, 1, '')
ON DUPLICATE KEY UPDATE `icon_id` = VALUES(`icon_id`), `NAME` = VALUES(`NAME`),
                        `description` = VALUES(`description`), `TYPE` = VALUES(`TYPE`);
