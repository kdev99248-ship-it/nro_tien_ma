-- M1 Tu Tien (docs/tutien-m1-plan.md): them cot luu du lieu tu tien cho player.
-- NULL = chua kich hoat (chua hoc Tu Tien voi Quy Lao). Khong can sua INSERT tao nhan vat.
-- Chay tren database tomahoc_db, vi du:
--   mysql -u root -p tomahoc_db < migration_m1_data_tutien.sql
-- Verify: SHOW COLUMNS FROM player LIKE 'data_tutien';

ALTER TABLE `player`
  ADD COLUMN `data_tutien` LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL;
