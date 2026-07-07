-- M7 Tu Tien — Linh Thạch (item 2016) + Shop Công Pháp ở NPC Quy Lão Kame.
--   * Linh Thạch (2016): rơi từ MỌI boss (Boss.reward, tỷ lệ 20%); dùng -> +500 linh khí +50 tu vi (UseItem case 2016).
--   * Là TIỀN TỆ mua bí tịch công pháp (2002-2015) ở shop Quy Lão (SPEC_SHOP type 3 -> subIemByItemShop trừ item).
-- icon_id 32486 = server/data/icon/x4/32486.png (đã có sẵn).
-- Shop mở qua menu Quy Lão Kame -> "Mua Công Pháp" -> ShopService.opendShop(player, "CONGPHAP_QUYLAO", false).
--   tag_name PHẢI khớp "CONGPHAP_QUYLAO" (QuyLaoKame.java). type_shop=3 (SPEC_SHOP). npc_id=13 (QUY_LAO_KAME).
--   item_spec=2016 (tiền tệ linh thạch), cost = số linh thạch / bí tịch.
-- Re-runnable (ON DUPLICATE KEY UPDATE). Chạy SAU migration_m4_cong_phap.sql:
--   mysql -u root -p tomahoc_db < migration_m7_linh_thach.sql
-- Verify: SELECT id,NAME,icon_id FROM item_template WHERE id=2016;
--         SELECT * FROM shop WHERE tag_name='CONGPHAP_QUYLAO'; SELECT id,shop_id,tab_name FROM tab_shop WHERE shop_id=900;

-- ── 1) Item Linh Thạch (2016) ────────────────────────────────────────────────
INSERT INTO `item_template`
  (`id`, `TYPE`, `gender`, `NAME`, `description`, `level`, `icon_id`, `part`, `is_up_to_up`,
   `power_require`, `gold`, `gem`, `head`, `body`, `leg`, `is_up_to_up_over_99`, `can_trade`, `comment`)
VALUES
  (2016, 27, 3, 'Linh Thạch',
   'Linh thạch ngưng tụ linh khí trời đất. Dùng để mua Công Pháp ở Quy Lão, hoặc dùng trực tiếp +500 linh khí & một chút tu vi.',
   1, 32486, -1, 0, 0, 0, 0, -1, -1, -1, 0, 1, '')
ON DUPLICATE KEY UPDATE `icon_id` = VALUES(`icon_id`), `NAME` = VALUES(`NAME`),
                        `description` = VALUES(`description`), `TYPE` = VALUES(`TYPE`);

-- ── 2) Shop Công Pháp (SPEC_SHOP, currency = Linh Thạch 2016) ─────────────────
INSERT INTO `shop` (`id`, `npc_id`, `tag_name`, `type_shop`) VALUES
  (900, 13, 'CONGPHAP_QUYLAO', 3)
ON DUPLICATE KEY UPDATE `npc_id` = VALUES(`npc_id`), `tag_name` = VALUES(`tag_name`), `type_shop` = VALUES(`type_shop`);

-- Tab 0: Công Pháp linh căn (2002-2008). Tab 1: Võ Học phổ (2009-2015). item_spec=2016, cost = số linh thạch.
INSERT INTO `tab_shop` (`id`, `shop_id`, `tab_name`, `tab_index`, `items`) VALUES
  (900, 900, 'Công Pháp', 0, '[{"temp_id":2002,"is_new":false,"cost":3,"item_spec":2016,"type_sell":0,"options":[],"is_sell":true},{"temp_id":2003,"is_new":false,"cost":3,"item_spec":2016,"type_sell":0,"options":[],"is_sell":true},{"temp_id":2004,"is_new":false,"cost":3,"item_spec":2016,"type_sell":0,"options":[],"is_sell":true},{"temp_id":2005,"is_new":false,"cost":3,"item_spec":2016,"type_sell":0,"options":[],"is_sell":true},{"temp_id":2006,"is_new":false,"cost":3,"item_spec":2016,"type_sell":0,"options":[],"is_sell":true},{"temp_id":2007,"is_new":false,"cost":3,"item_spec":2016,"type_sell":0,"options":[],"is_sell":true},{"temp_id":2008,"is_new":false,"cost":5,"item_spec":2016,"type_sell":0,"options":[],"is_sell":true}]'),
  (901, 900, 'Võ Học', 1, '[{"temp_id":2009,"is_new":false,"cost":3,"item_spec":2016,"type_sell":0,"options":[],"is_sell":true},{"temp_id":2010,"is_new":false,"cost":3,"item_spec":2016,"type_sell":0,"options":[],"is_sell":true},{"temp_id":2011,"is_new":false,"cost":3,"item_spec":2016,"type_sell":0,"options":[],"is_sell":true},{"temp_id":2012,"is_new":false,"cost":3,"item_spec":2016,"type_sell":0,"options":[],"is_sell":true},{"temp_id":2013,"is_new":false,"cost":3,"item_spec":2016,"type_sell":0,"options":[],"is_sell":true},{"temp_id":2014,"is_new":false,"cost":3,"item_spec":2016,"type_sell":0,"options":[],"is_sell":true},{"temp_id":2015,"is_new":false,"cost":5,"item_spec":2016,"type_sell":0,"options":[],"is_sell":true}]')
ON DUPLICATE KEY UPDATE `shop_id` = VALUES(`shop_id`), `tab_name` = VALUES(`tab_name`),
                        `tab_index` = VALUES(`tab_index`), `items` = VALUES(`items`);
