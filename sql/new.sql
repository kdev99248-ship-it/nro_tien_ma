-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               5.7.26 - MySQL Community Server (GPL)
-- Server OS:                    Win64
-- HeidiSQL Version:             12.8.0.6908
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- Dumping database structure for funny1
CREATE DATABASE IF NOT EXISTS `funny1` /*!40100 DEFAULT CHARACTER SET utf8mb4 */;
USE `funny1`;

-- Dumping structure for table funny1.users
CREATE TABLE IF NOT EXISTS `users` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(30) NOT NULL,
  `password` varchar(100) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `otp` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `status` int(11) DEFAULT '1' COMMENT '0: Deactivate, 1: Active, 2: Block',
  `activated` tinyint(4) NOT NULL DEFAULT '1',
  `luong` int(11) NOT NULL DEFAULT '0',
  `amount_unpaid` int(11) NOT NULL DEFAULT '0',
  `online` tinyint(1) NOT NULL DEFAULT '0',
  `role` int(11) DEFAULT NULL,
  `group_id` int(11) NOT NULL DEFAULT '1',
  `last_login_at` timestamp NULL DEFAULT NULL,
  `received_first_gift` int(11) NOT NULL DEFAULT '0',
  `last_attendance_at` bigint(20) DEFAULT '0',
  `ip_address` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin,
  `level_reward` varchar(30) NOT NULL DEFAULT '[0,0,0,0,0]',
  `note` text,
  `ban_until` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT NULL,
  `tongnap` int(11) NOT NULL DEFAULT '0',
  `tongNaps` int(11) NOT NULL DEFAULT '0',
  `dokiep` int(11) NOT NULL DEFAULT '1',
  `MocNap` int(11) NOT NULL DEFAULT '1',
  `choden` int(11) NOT NULL DEFAULT '0',
  `vnd` int(11) NOT NULL DEFAULT '0',
  `ruby` int(11) NOT NULL DEFAULT '0',
  `dangnhap` int(11) NOT NULL DEFAULT '1',
  `diemvip` int(11) NOT NULL DEFAULT '0',
  `gianhap` int(11) NOT NULL DEFAULT '0',
  `Svip` int(11) NOT NULL DEFAULT '10',
  `goiqua` bigint(11) NOT NULL DEFAULT '0',
  `diemtl` int(11) NOT NULL DEFAULT '0',
  `chuyensinh` int(11) NOT NULL DEFAULT '0',
  `nhan_v-vip` int(11) NOT NULL DEFAULT '0',
  `isVIP` int(11) NOT NULL DEFAULT '0',
  `nhan_vip` int(11) NOT NULL DEFAULT '0',
  `kh` int(11) NOT NULL DEFAULT '1',
  `consecutive_days` int(11) DEFAULT '0',
  `admin_web` int(11) NOT NULL DEFAULT '0',
  `coin` int(11) NOT NULL DEFAULT '0',
  `account_number` varchar(8) DEFAULT NULL,
  `pw2` varchar(4) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `username` (`username`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- Dumping data for table funny1.users: ~1 rows (approximately)
INSERT INTO `users` (`id`, `username`, `password`, `name`, `otp`, `email`, `status`, `activated`, `luong`, `amount_unpaid`, `online`, `role`, `group_id`, `last_login_at`, `received_first_gift`, `last_attendance_at`, `ip_address`, `level_reward`, `note`, `ban_until`, `created_at`, `updated_at`, `tongnap`, `tongNaps`, `dokiep`, `MocNap`, `choden`, `vnd`, `ruby`, `dangnhap`, `diemvip`, `gianhap`, `Svip`, `goiqua`, `diemtl`, `chuyensinh`, `nhan_v-vip`, `isVIP`, `nhan_vip`, `kh`, `consecutive_days`, `admin_web`, `coin`, `account_number`, `pw2`) VALUES
	(1, '1', '1', NULL, NULL, NULL, 1, 1, 971000, 0, 0, 1997, 1, NULL, 1, 0, '["127.0.0.1"]', '[1,0,0,0,0,0,0,0,0,0,0,0,0]', NULL, NULL, NULL, NULL, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 10, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, NULL, NULL);

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
