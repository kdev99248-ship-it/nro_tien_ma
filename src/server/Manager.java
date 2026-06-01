package server;

import models.Card.OptionCard;
import models.Card.RadarService;
import models.Card.RadarCard;
import models.Consign.ConsignItem;
import models.Consign.ConsignShopManager;
import models.Farm.CropTemplate;
import models.Farm.CropType;
import jdbc.DBConnecter;
import consts.ConstPlayer;
import consts.ConstMap;
import data.DataGame;
import jdbc.daos.ShopDAO;
import models.Template.*;
import clan.Clan;
import clan.ClanMember;
import consts.ConstNpc;
import consts.ConstSQL;

import static data.DataGame.MAP_MOUNT_NUM;
import encrypt.ImageUtil;

import models.GiftCode.GiftCode;
import models.GiftCode.GiftCodeManager;
import intrinsic.Intrinsic;
import item.Item;
import item.Item.ItemOption;
import map.WayPoint;
import npc.Npc;
import npc.NpcFactory;
import player.badges.BagesTemplate;
import shop.Shop;
import skill.NClass;
import skill.Skill;
import task.Badges.BadgesTaskTemplate;
import task.SideTaskTemplate;
import task.SubTaskMain;
import task.TaskMain;
import services.ItemService;
import services.MapService;
import utils.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Properties;
import java.util.stream.IntStream;
import map.EffectMap;

import matches.TOP;
import utils.Util;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import npc.NonInteractiveNPC;
import power.CaptionManager;
import power.PowerLimitManager;
import sosumenh.SoSuMenhManager;
import task.ClanTaskTemplate;
import models.Farm.CropTemplate;
import models.Farm.CropType;
import player.mercenary.MercenaryManager;

public final class Manager {

    private static Manager instance;

    public static byte SERVER = 1;
    public static byte SECOND_WAIT_LOGIN = 5;
    public static int MAX_PER_IP = 10;
    public static int MAX_PLAYER = 2000;
    public static byte RATE_EXP_SERVER = 1;
    public static boolean LOCAL = false;
    public static boolean TEST = false;
    public static boolean DAO_AUTO_UPDATER = false;

    public static MapTemplate[] MAP_TEMPLATES;
    public static final List<map.Map> MAPS = new ArrayList<>();
    public static final List<ItemOptionTemplate> ITEM_OPTION_TEMPLATES = new ArrayList<>();
    public static final List<ArrHead2Frames> ARR_HEAD_2_FRAMES = new ArrayList<>();
    public static final Map<String, Byte> IMAGES_BY_NAME = new HashMap<>();
    public static final List<ItemTemplate> ITEM_TEMPLATES = new ArrayList<>();
    public static final Map<Short, ItemTemplate> ITEM_TEMPLATE_MAP = new HashMap<>(); // Map để truy cập theo ID
    public static final List<MobTemplate> MOB_TEMPLATES = new ArrayList<>();
    public static final List<NpcTemplate> NPC_TEMPLATES = new ArrayList<>();
    public static final List<TaskMain> TASKS = new ArrayList<>();
    public static final List<SideTaskTemplate> SIDE_TASKS_TEMPLATE = new ArrayList<>();
    public static final List<ClanTaskTemplate> CLAN_TASKS_TEMPLATE = new ArrayList<>();
    public static final List<AchievementTemplate> ACHIEVEMENT_TEMPLATE = new ArrayList<>();
    public static final List<Intrinsic> INTRINSICS = new ArrayList<>();
    public static final List<Intrinsic> INTRINSIC_TD = new ArrayList<>();
    public static final List<Intrinsic> INTRINSIC_NM = new ArrayList<>();
    public static final List<Intrinsic> INTRINSIC_XD = new ArrayList<>();
    public static final List<HeadAvatar> HEAD_AVATARS = new ArrayList<>();
    public static final List<BgItem> BG_ITEMS = new ArrayList<>();
    public static final List<FlagBag> FLAGS_BAGS = new ArrayList<>();
    public static final List<NClass> NCLASS = new ArrayList<>();
    public static final List<Npc> NPCS = new ArrayList<>();
    public static List<Shop> SHOPS = new ArrayList<>();
    public static final List<Clan> CLANS = new ArrayList<>();
    public static final List<String> NOTIFY = new ArrayList<>();
    public static final List<BadgesTaskTemplate> TASKS_BADGES_TEMPLATE = new ArrayList<>();
    public static final List<BagesTemplate> BAGES_TEMPLATES = new ArrayList<>();
    public static final List<item.ItemNhaBep> ITEM_NHA_BEP = new ArrayList<>();

    public static List<TOP> topSM;
    public static List<TOP> topNap;
    public static List<TOP> topSSM;
    public static List<TOP> topTet;
    public static List<TOP> topHopQua;
    public static List<TOP> topbossday;
    public static List<TOP> topbang;
    // public static List<TOP> topDuaSM;
    // public static List<TOP> topDuaNap;
    public static List<TOP> topSD;
    public static List<TOP> topHP;
    public static List<TOP> topKI;
    public static List<TOP> topNV;
    public static List<TOP> topSK;
    public static List<TOP> topPVP;
    public static List<TOP> topNHS;
    public static List<TOP> topDC;
    public static List<TOP> topVDST;
    public static List<TOP> topWHIS;
    public static long timeRealTop = 0;

    public static final short[][] trangBiKichHoat = { { 0, 6, 21, 27 }, { 1, 7, 22, 28 }, { 2, 8, 23, 29 } };
    public static final short[][] trangBiKichHoatVipRada = { { 281, 561, 656, 1060 }, { 281, 561, 656, 1061 },
            { 281, 561, 656, 1062 } };

    public static int getRankByName(String playerName) {
        // Tìm player trong danh sách topbossday theo tên
        Optional<TOP> playerOpt = topbossday.stream()
                .filter(p -> p.getName().equalsIgnoreCase(playerName)) // So sánh tên không phân biệt chữ hoa chữ thường
                .findFirst();

        // Nếu tìm thấy player, trả về rank (bắt đầu từ 1)
        if (playerOpt.isPresent()) {
            return topbossday.indexOf(playerOpt.get()) + 1; // Rank bắt đầu từ 1
        } else {
            return -1; // Trả về -1 nếu không tìm thấy player
        }
    }

    public static final short[][] trangBiKichHoatVipJay = { { 269,
            563,
            658,
            1057 },
            { 273,
                    565,
                    660,
                    1058 },
            { 277,
                    567,
                    662,
                    1059 } };
    public static final short[][] trangBiKichHoatVipAo = { { 233,
            555,
            650,
            1048 },
            { 237,
                    557,
                    652,
                    1049 },
            { 241,
                    559,
                    654,
                    1050,
                    1050 } };
    public static final short[][] trangBiKichHoatVipQuan = { { 245,
            556,
            651,
            1051 },
            { 249,
                    558,
                    653,
                    1052 },
            { 253,
                    560,
                    655,
                    1053 } };
    public static final short[][] trangBiKichHoatVipGang = { { 257,
            562,
            657,
            1054 },
            { 261,
                    564,
                    659,
                    1055 },
            { 265,
                    566,
                    661,
                    1056 } };

    public static final int[][] doTraiDathd = new int[][] { { 3, 33, 34, 136, 137, 138, 139, 230, 231, 232, 233, 555 }, // ao
            { 9, 35, 36, 140, 141, 142, 143, 242, 243, 244, 245, 556 }, // quan
            { 24, 37, 38, 144, 145, 146, 147, 254, 255, 256, 257, 562 }, // gang
            { 30, 39, 40, 148, 149, 150, 151, 266, 267, 268, 269, 563 },// giay
    };
    public static final int[] LINHTHU = { 1811, 1642, 1643, 1652, 1655, 1664, 1695, 1490, 1489, 1492, 1493, 1494, 1778,
            1776, 1644, 1645, 1646, 1647, 1807, 1742,
            1779, 1648, 1651, 1653, 1812, 1649, 1491, 1650, 1744
    };
    public static final int[][] doNamechd = new int[][] { { 4, 41, 42, 152, 153, 154, 155, 234, 235, 236, 237, 557 }, // ao
            { 10, 43, 44, 156, 157, 158, 159, 246, 247, 248, 249, 558 }, // quan
            { 25, 45, 46, 160, 161, 162, 163, 258, 259, 260, 261, 564 }, // gang
            { 31, 47, 48, 164, 165, 166, 167, 270, 271, 272, 273, 565 },// giay
    };
    public static final int[][] doXaydahd = new int[][] { { 5, 49, 50, 168, 169, 170, 171, 238, 239, 240, 241, 559 }, // ao
            { 11, 51, 52, 172, 173, 174, 175, 250, 251, 252, 253, 560 }, // quan
            { 26, 53, 54, 176, 177, 178, 179, 262, 263, 264, 265, 566 }, // gang
            { 32, 55, 56, 180, 181, 182, 183, 274, 275, 276, 277, 567 },// giay
    };
    public static final int[] Rada = new int[] { 12, 57, 58, 59, 184, 185, 186, 187, 278, 279, 280, 281, 561 };

    public static Manager gI() {
        if (instance == null) {
            instance = new Manager();
        }
        return instance;
    }

    private Manager() {
        try {
            loadProperties();

        } catch (IOException ex) {
            Logger.logException(Manager.class,
                    ex, "Lỗi load properites");
            System.exit(0);
        }
        // ImageUtil.initImage();
        this.loadDatabase();
        NpcFactory.createNpcConMeo();
        NpcFactory.createNpcRongThieng();
        this.initMap();
        SoSuMenhManager.getInstance().loading();
        // Load mercenary templates từ database
        MercenaryManager.gI().loadTemplates();
        System.out.println("Finish connect Server: " + DBConnecter.DB_DATA);
    }

    /*
     * private void initMap() {
     * int[][] tileTyleTop = readTileIndexTileType(ConstMap.TILE_TOP);
     * for (MapTemplate mapTemp : MAP_TEMPLATES) {
     * int[][] tileMap = readTileMap(mapTemp.id);
     * int index = mapTemp.tileId - 1;
     * int[] tileTop = null;
     * if (tileTyleTop != null && index >= 0 && index < tileTyleTop.length) {
     * tileTop = tileTyleTop[index];
     * }
     * if (tileTop == null) {
     * tileTop = new int[0];
     * }
     * map.Map map = new map.Map(mapTemp.id,
     * mapTemp.name, mapTemp.planetId, mapTemp.tileId, mapTemp.bgId,
     * mapTemp.bgType, mapTemp.type, tileMap, tileTop,
     * mapTemp.zones,
     * mapTemp.maxPlayerPerZone, mapTemp.wayPoints, mapTemp.effectMaps);
     * MAPS.add(map);
     * map.initMob(mapTemp.mobTemp, mapTemp.mobLevel, mapTemp.mobHp, mapTemp.mobX,
     * mapTemp.mobY);
     * map.initNpc(mapTemp.npcId, mapTemp.npcX, mapTemp.npcY);
     * new Thread(map, "Update map " + map.mapName).start();
     * }
     */

    private void initMap() {
        int[][] tileTyleTop = readTileIndexTileType(ConstMap.TILE_TOP);
        for (MapTemplate mapTemp : MAP_TEMPLATES) {
            int[][] tileMap = readTileMap(mapTemp.id);
            int[] tileTop = tileTyleTop[mapTemp.tileId - 1];
            map.Map map = new map.Map(mapTemp.id,
                    mapTemp.name, mapTemp.planetId, mapTemp.tileId, mapTemp.bgId,
                    mapTemp.bgType, mapTemp.type, tileMap, tileTop,
                    mapTemp.zones,
                    mapTemp.maxPlayerPerZone, mapTemp.wayPoints, mapTemp.effectMaps);
            MAPS.add(map);
            map.initMob(mapTemp.mobTemp, mapTemp.mobLevel, mapTemp.mobHp, mapTemp.mobX, mapTemp.mobY);
            map.initNpc(mapTemp.npcId, mapTemp.npcX, mapTemp.npcY);
            // Dùng MAP_UPDATE_POOL thay vì new Thread() để tránh thread explosion
            // 8 thread trong pool xử lý hàng trăm map nhờ cơ chế adaptive sleep
            utils.ServerPool.MAP_UPDATE_POOL.submit(map);
        }

        int cx;
        int cy;

        new NonInteractiveNPC().initNonInteractiveNPC();
        Logger.success("Initialize map successfully!\n");
    }

    private void loadDatabase() {
        System.out.println(">> Đang kết nối đến Database... (Nếu lâu quá 30s hãy kiểm tra lại MySQL)");
        long st = System.currentTimeMillis();
        JSONArray dataArray;
        JSONObject dataObject;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try (Connection con2 = DBConnecter.getConnectionServer();) {
            // load item option template
            ps = con2.prepareStatement("select id, name from item_option_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                ItemOptionTemplate optionTemp = new ItemOptionTemplate();
                optionTemp.id = rs.getInt("id");
                optionTemp.name = rs.getString("name");
                ITEM_OPTION_TEMPLATES.add(optionTemp);
            }
            System.out.println("Successfully loaded map item option template (" + ITEM_OPTION_TEMPLATES.size() + ")\n");

            // load clan
            ps = con2.prepareStatement("select * from clan");
            rs = ps.executeQuery();
            while (rs.next()) {
                Clan clan = new Clan();
                clan.id = rs.getInt("id");
                clan.name = rs.getString("name");
                clan.name2 = rs.getString("name_2");
                clan.slogan = rs.getString("slogan");
                clan.imgId = rs.getByte("img_id");
                clan.powerPoint = rs.getLong("power_point");
                clan.maxMember = rs.getByte("max_member");
                clan.capsuleClan = rs.getInt("clan_point");
                clan.level = rs.getByte("level");
                if (clan.level < 1) {
                    clan.level = 1;
                }
                clan.createTime = (int) (rs.getTimestamp("create_time").getTime() / 1000);
                dataArray = (JSONArray) JSONValue.parse(rs.getString("members"));
                for (int i = 0; i < dataArray.size(); i++) {
                    dataObject = (JSONObject) JSONValue.parse(String.valueOf(dataArray.get(i)));
                    ClanMember cm = new ClanMember();
                    cm.clan = clan;
                    cm.id = Integer.parseInt(String.valueOf(dataObject.get("id")));
                    cm.name = String.valueOf(dataObject.get("name"));
                    cm.head = Short.parseShort(String.valueOf(dataObject.get("head")));
                    cm.body = Short.parseShort(String.valueOf(dataObject.get("body")));
                    cm.leg = Short.parseShort(String.valueOf(dataObject.get("leg")));
                    cm.role = Byte.parseByte(String.valueOf(dataObject.get("role")));
                    cm.donate = Integer.parseInt(String.valueOf(dataObject.get("donate")));
                    cm.receiveDonate = Integer.parseInt(String.valueOf(dataObject.get("receive_donate")));
                    cm.memberPoint = Integer.parseInt(String.valueOf(dataObject.get("member_point")));
                    cm.clanPoint = Integer.parseInt(String.valueOf(dataObject.get("clan_point")));
                    cm.joinTime = Integer.parseInt(String.valueOf(dataObject.get("join_time")));
                    cm.timeAskPea = Long.parseLong(String.valueOf(dataObject.get("ask_pea_time")));
                    try {
                        cm.powerPoint = Long.parseLong(String.valueOf(dataObject.get("power")));
                    } catch (NumberFormatException e) {
                    }
                    clan.addClanMember(cm);
                }
                dataArray.clear();
                CLANS.add(clan);
            }

            ps = con2.prepareStatement("select id from clan order by id desc limit 1");
            rs = ps.executeQuery();
            if (rs.next()) {
                Clan.NEXT_ID = rs.getInt("id") + 1;
            }

            Logger.success("Successfully loaded clan (" + CLANS.size() + "), clan next id: " + Clan.NEXT_ID + "\n");

            // Load item ki gui
            ps = con2.prepareStatement("SELECT * FROM shop_ky_gui");
            rs = ps.executeQuery();
            while (rs.next()) {
                int i = rs.getInt("id");
                int idPl = rs.getInt("player_id");
                byte tab = rs.getByte("tab");
                short itemId = rs.getShort("item_id");
                int gold = rs.getInt("gold");
                int gem = rs.getInt("gem");
                int quantity = rs.getInt("quantity");
                long isTime = rs.getLong("lasttime");
                boolean isBuy = rs.getByte("isBuy") == 1;
                List<Item.ItemOption> op = new ArrayList<>();
                JSONArray jsa2 = (JSONArray) JSONValue.parse(rs.getString("itemOption"));
                for (int j = 0; j < jsa2.size(); ++j) {
                    JSONObject jso2 = (JSONObject) jsa2.get(j);
                    int idOptions = Integer.parseInt(jso2.get("id").toString());
                    int param = Integer.parseInt(jso2.get("param").toString());
                    op.add(new Item.ItemOption(idOptions, param));
                }
                ConsignShopManager.gI().listItem
                        .add(new ConsignItem(i, itemId, idPl, tab, gold, gem, quantity, isTime, op, isBuy));
            }
            Logger.success("Successfully loaded Consign Item (" + ConsignShopManager.gI().listItem.size() + ")\n");

            // Load giftcode
            ps = con2.prepareStatement("SELECT * FROM giftcode");
            rs = ps.executeQuery();
            while (rs.next()) {
                GiftCode giftcode = new GiftCode();
                giftcode.code = rs.getString("code");
                giftcode.id = rs.getInt("id");
                giftcode.countLeft = rs.getInt("count_left");
                if (giftcode.countLeft == -1) {
                    giftcode.countLeft = 999999999;
                }
                giftcode.datecreate = rs.getTimestamp("datecreate");
                giftcode.dateexpired = rs.getTimestamp("expired");
                try {
                    giftcode.type = rs.getInt("type");
                } catch (Exception e) {
                    giftcode.type = 0; // Mặc định type = 0 nếu không có cột type
                }
                JSONArray jar = (JSONArray) JSONValue.parse(rs.getString("detail"));
                if (jar != null) {
                    for (int i = 0; i < jar.size(); ++i) {
                        JSONObject jsonObj = (JSONObject) jar.get(i);

                        int id = Integer.parseInt(jsonObj.get("temp_id").toString());
                        int quantity = Integer.parseInt(jsonObj.get("quantity").toString());

                        JSONArray option = (JSONArray) jsonObj.get("options");
                        ArrayList<ItemOption> optionList = new ArrayList<>();

                        if (option != null) {
                            for (int u = 0; u < option.size(); u++) {
                                JSONObject jsonobject = (JSONObject) option.get(u);
                                int optionId = Integer.parseInt(jsonobject.get("id").toString());
                                int param = Integer.parseInt(jsonobject.get("param").toString());
                                optionList.add(new Item.ItemOption(optionId, param));
                            }
                        }
                        giftcode.option.put(id, optionList);
                        giftcode.detail.put(id, quantity);
                    }
                }
                GiftCodeManager.gI().listGiftCode.add(giftcode);
            }
            Logger.success("Successfully loaded giftcode (" + GiftCodeManager.gI().listGiftCode.size() + ")\n");

        } catch (Exception ex) {
            System.err.println("Lỗi load giftcode hoặc consign item: " + ex.getMessage());
            ex.printStackTrace();
        }

        try (Connection con = DBConnecter.getConnectionServer();) {
            // load part
            ps = con.prepareStatement("select * from part");
            rs = ps.executeQuery();
            List<Part> parts = new ArrayList<>();
            while (rs.next()) {
                Part part = new Part();
                part.id = rs.getShort("id");
                part.type = rs.getByte("type");
                dataArray = (JSONArray) JSONValue.parse(rs.getString("data").replaceAll("\\\"", ""));
                for (int j = 0; j < dataArray.size(); j++) {
                    JSONArray pd = (JSONArray) JSONValue.parse(String.valueOf(dataArray.get(j)));
                    part.partDetails.add(new PartDetail(Short.parseShort(String.valueOf(pd.get(0))),
                            (byte) Integer.parseInt(String.valueOf(pd.get(1))),
                            (byte) Integer.parseInt(String.valueOf(pd.get(2)))));
                    pd.clear();
                }
                parts.add(part);
                dataArray.clear();
            }
            DataOutputStream dos = new DataOutputStream(new FileOutputStream("data/update_data/part"));
            dos.writeShort(parts.size());
            for (Part part : parts) {
                dos.writeByte(part.type);
                for (PartDetail partDetail : part.partDetails) {
                    dos.writeShort(partDetail.iconId);
                    dos.writeByte(partDetail.dx);
                    dos.writeByte(partDetail.dy);
                }
            }
            dos.flush();
            Logger.success("Successfully loaded part (" + parts.size() + ")\n");

            // load bg item template
            ps = con.prepareStatement("select * from bg_item_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                BgItem bgItem = new BgItem();
                bgItem.id = rs.getInt("id");
                bgItem.layer = rs.getByte("layer");
                bgItem.dx = rs.getShort("dx");
                bgItem.dy = rs.getShort("dy");
                bgItem.idImage = rs.getShort("image_id");
                BG_ITEMS.add(bgItem);
            }
            Logger.success("Successfully loaded bg item template (" + BG_ITEMS.size() + ")\n");

            // load array head 2 frames
            ps = con.prepareStatement("select * from array_head_2_frames");
            rs = ps.executeQuery();
            while (rs.next()) {
                ArrHead2Frames arrHead2Frames = new ArrHead2Frames();
                dataArray = (JSONArray) JSONValue.parse(rs.getString("data"));
                for (int i = 0; i < dataArray.size(); i++) {
                    arrHead2Frames.frames.add(Integer.valueOf(dataArray.get(i).toString()));
                }
                ARR_HEAD_2_FRAMES.add(arrHead2Frames);
            }
            Logger.success("Successfully loaded arr head 2 frames (" + ARR_HEAD_2_FRAMES.size() + ")\n");

            // load skill
            ps = con.prepareStatement("select * from skill_template order by nclass_id, slot");
            rs = ps.executeQuery();
            byte nClassId = -1;
            NClass nClass = null;
            while (rs.next()) {
                byte id = rs.getByte("nclass_id");
                if (id != nClassId) {
                    nClassId = id;
                    nClass = new NClass();
                    nClass.name = id == ConstPlayer.TRAI_DAT ? "Trái Đất" : id == ConstPlayer.NAMEC ? "Namếc" : "Xayda";
                    nClass.classId = nClassId;
                    NCLASS.add(nClass);
                }
                SkillTemplate skillTemplate = new SkillTemplate();
                skillTemplate.classId = nClassId;
                skillTemplate.id = rs.getByte("id");
                skillTemplate.name = rs.getString("name");
                skillTemplate.maxPoint = rs.getByte("max_point");
                skillTemplate.manaUseType = rs.getByte("mana_use_type");
                skillTemplate.type = rs.getByte("type");
                skillTemplate.iconId = rs.getShort("icon_id");
                skillTemplate.damInfo = rs.getString("dam_info");
                nClass.skillTemplatess.add(skillTemplate);

                dataArray = (JSONArray) JSONValue.parse(
                        rs.getString("skills")
                                .replaceAll("\\[\"", "[")
                                .replaceAll("\"\\[", "[")
                                .replaceAll("\"\\]", "]")
                                .replaceAll("\\]\"", "]")
                                .replaceAll("\\}\",\"\\{", "},{"));
                if (dataArray != null) {
                    for (int j = 0; j < dataArray.size(); j++) {
                        JSONObject dts = (JSONObject) JSONValue.parse(String.valueOf(dataArray.get(j)));
                        Skill skill = new Skill();
                        skill.template = skillTemplate;
                        skill.skillId = Short.parseShort(String.valueOf(dts.get("id")));
                        skill.point = Byte.parseByte(String.valueOf(dts.get("point")));
                        skill.powRequire = Long.parseLong(String.valueOf(dts.get("power_require")));
                        skill.manaUse = Integer.parseInt(String.valueOf(dts.get("mana_use")));
                        skill.coolDown = Integer.parseInt(String.valueOf(dts.get("cool_down")));
                        skill.dx = Integer.parseInt(String.valueOf(dts.get("dx")));
                        skill.dy = Integer.parseInt(String.valueOf(dts.get("dy")));
                        skill.maxFight = Integer.parseInt(String.valueOf(dts.get("max_fight")));
                        skill.damage = Short.parseShort(String.valueOf(dts.get("damage")));
                        skill.price = Short.parseShort(String.valueOf(dts.get("price")));
                        skill.moreInfo = String.valueOf(dts.get("info"));
                        if (dts.get("time_tanghinh") != null) skill.timeTangHinh = Integer.parseInt(String.valueOf(dts.get("time_tanghinh")));
                        if (dts.get("time_choang") != null) skill.timeChoang = Integer.parseInt(String.valueOf(dts.get("time_choang")));
                        if (dts.get("ti_le_choang") != null) skill.tiLeChoang = Integer.parseInt(String.valueOf(dts.get("ti_le_choang")));
                        skillTemplate.skillss.add(skill);
                    }
                }
            }
            Logger.success("Successfully loaded skill (" + NCLASS.size() + ")\n");

            // load head avatar
            ps = con.prepareStatement("select * from head_avatar");
            rs = ps.executeQuery();
            while (rs.next()) {
                HeadAvatar headAvatar = new HeadAvatar(rs.getInt("head_id"), rs.getInt("avatar_id"));
                HEAD_AVATARS.add(headAvatar);
            }
            Logger.success("Successfully loaded head avatar (" + HEAD_AVATARS.size() + ")\n");

            // load flag bag
            ps = con.prepareStatement("select * from flag_bag");
            rs = ps.executeQuery();
            while (rs.next()) {
                FlagBag flagBag = new FlagBag();
                flagBag.id = rs.getInt("id");
                flagBag.name = rs.getString("name");
                flagBag.gold = rs.getInt("gold");
                flagBag.gem = rs.getInt("gem");
                flagBag.iconId = rs.getShort("icon_id");
                String[] iconData = rs.getString("icon_data").split(",");
                flagBag.iconEffect = new short[iconData.length];
                for (int j = 0; j < iconData.length; j++) {
                    flagBag.iconEffect[j] = Short.parseShort(iconData[j].trim());
                }
                FLAGS_BAGS.add(flagBag);
            }
            Logger.success("Successfully loaded flag bag (" + FLAGS_BAGS.size() + ")\n");

            // load intrinsic
            ps = con.prepareStatement("select * from intrinsic");
            rs = ps.executeQuery();
            while (rs.next()) {
                Intrinsic intrinsic = new Intrinsic();
                intrinsic.id = rs.getByte("id");
                intrinsic.name = rs.getString("name");
                intrinsic.paramFrom1 = rs.getShort("param_from_1");
                intrinsic.paramTo1 = rs.getShort("param_to_1");
                intrinsic.paramFrom2 = rs.getShort("param_from_2");
                intrinsic.paramTo2 = rs.getShort("param_to_2");
                intrinsic.icon = rs.getShort("icon");
                intrinsic.gender = rs.getByte("gender");
                switch (intrinsic.gender) {
                    case ConstPlayer.TRAI_DAT ->
                        INTRINSIC_TD.add(intrinsic);
                    case ConstPlayer.NAMEC ->
                        INTRINSIC_NM.add(intrinsic);
                    case ConstPlayer.XAYDA ->
                        INTRINSIC_XD.add(intrinsic);
                    default -> {
                        INTRINSIC_TD.add(intrinsic);
                        INTRINSIC_NM.add(intrinsic);
                        INTRINSIC_XD.add(intrinsic);
                    }
                }
                INTRINSICS.add(intrinsic);
            }
            Logger.success("Successfully loaded intrinsic (" + INTRINSICS.size() + ")\n");

            // load task
            ps = con.prepareStatement("SELECT id, task_main_template.name, detail, "
                    + "task_sub_template.name AS 'sub_name', max_count, notify, npc_id, map "
                    + "FROM task_main_template JOIN task_sub_template ON task_main_template.id = "
                    + "task_sub_template.task_main_id");
            rs = ps.executeQuery();
            int taskId = -1;
            TaskMain task = null;
            while (rs.next()) {
                int id = rs.getInt("id");
                if (id != taskId) {
                    taskId = id;
                    task = new TaskMain();
                    task.id = taskId;
                    task.name = rs.getString("name");
                    task.detail = rs.getString("detail");
                    TASKS.add(task);
                }
                SubTaskMain subTask = new SubTaskMain();
                subTask.name = rs.getString("sub_name");
                subTask.maxCount = rs.getShort("max_count");
                subTask.notify = rs.getString("notify");
                subTask.npcId = rs.getByte("npc_id");
                subTask.mapId = rs.getShort("map");
                task.subTasks.add(subTask);
            }
            Logger.success("Successfully loaded task (" + TASKS.size() + ")\n");

            // load side task
            ps = con.prepareStatement("select * from side_task_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                SideTaskTemplate sideTask = new SideTaskTemplate();
                sideTask.id = rs.getInt("id");
                sideTask.name = rs.getString("name");
                String[] mc1 = rs.getString("max_count_lv1").split("-");
                String[] mc2 = rs.getString("max_count_lv2").split("-");
                String[] mc3 = rs.getString("max_count_lv3").split("-");
                String[] mc4 = rs.getString("max_count_lv4").split("-");
                String[] mc5 = rs.getString("max_count_lv5").split("-");
                sideTask.count[0][0] = Integer.parseInt(mc1[0]);
                sideTask.count[0][1] = Integer.parseInt(mc1[1]);
                sideTask.count[1][0] = Integer.parseInt(mc2[0]);
                sideTask.count[1][1] = Integer.parseInt(mc2[1]);
                sideTask.count[2][0] = Integer.parseInt(mc3[0]);
                sideTask.count[2][1] = Integer.parseInt(mc3[1]);
                sideTask.count[3][0] = Integer.parseInt(mc4[0]);
                sideTask.count[3][1] = Integer.parseInt(mc4[1]);
                sideTask.count[4][0] = Integer.parseInt(mc5[0]);
                sideTask.count[4][1] = Integer.parseInt(mc5[1]);
                SIDE_TASKS_TEMPLATE.add(sideTask);
            }
            Logger.success("Successfully loaded side task (" + SIDE_TASKS_TEMPLATE.size() + ")\n");

            // load task badges
            ps = con.prepareStatement("select * from task_badges_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                BadgesTaskTemplate badgesTaskTemplate = new BadgesTaskTemplate();
                badgesTaskTemplate.id = rs.getInt("id");
                badgesTaskTemplate.name = rs.getString("NAME");
                badgesTaskTemplate.count = rs.getInt("maxCount");
                badgesTaskTemplate.idbadgesReward = rs.getInt("idbadgesReward");
                TASKS_BADGES_TEMPLATE.add(badgesTaskTemplate);
            }
            Logger.success("Successfully loaded task badges (" + TASKS_BADGES_TEMPLATE.size() + ")\n");

            // load clan task
            ps = con.prepareStatement("select * from clan_task_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                ClanTaskTemplate clanTask = new ClanTaskTemplate();
                clanTask.id = rs.getInt("id");
                clanTask.name = rs.getString("name");
                String[] mc1 = rs.getString("max_count_lv1").split("-");
                String[] mc2 = rs.getString("max_count_lv2").split("-");
                String[] mc3 = rs.getString("max_count_lv3").split("-");
                String[] mc4 = rs.getString("max_count_lv4").split("-");
                String[] mc5 = rs.getString("max_count_lv5").split("-");
                clanTask.count[0][0] = Integer.parseInt(mc1[0]);
                clanTask.count[0][1] = Integer.parseInt(mc1[1]);
                clanTask.count[1][0] = Integer.parseInt(mc2[0]);
                clanTask.count[1][1] = Integer.parseInt(mc2[1]);
                clanTask.count[2][0] = Integer.parseInt(mc3[0]);
                clanTask.count[2][1] = Integer.parseInt(mc3[1]);
                clanTask.count[3][0] = Integer.parseInt(mc4[0]);
                clanTask.count[3][1] = Integer.parseInt(mc4[1]);
                clanTask.count[4][0] = Integer.parseInt(mc5[0]);
                clanTask.count[4][1] = Integer.parseInt(mc5[1]);
                CLAN_TASKS_TEMPLATE.add(clanTask);
            }
            Logger.success("Successfully loaded clan task (" + CLAN_TASKS_TEMPLATE.size() + ")\n");

            // load achievement template
            ps = con.prepareStatement("select * from achievement_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                ACHIEVEMENT_TEMPLATE.add(new AchievementTemplate(rs.getString("info1"), rs.getString("info2"),
                        rs.getInt("money"), rs.getLong("max_count")));
            }
            Logger.success("Successfully loaded achievement (" + ACHIEVEMENT_TEMPLATE.size() + ")\n");

            // load item template - PHẢI ORDER BY id để đảm bảo thứ tự đúng
            ps = con.prepareStatement("select * from item_template ORDER BY id ASC");
            rs = ps.executeQuery();
            while (rs.next()) {
                ItemTemplate itemTemp = new ItemTemplate();
                itemTemp.id = rs.getShort("id");
                itemTemp.type = rs.getByte("type");
                itemTemp.gender = rs.getByte("gender");
                itemTemp.name = rs.getString("name");
                itemTemp.description = rs.getString("description");
                itemTemp.level = rs.getByte("level");
                itemTemp.iconID = rs.getShort("icon_id");
                itemTemp.part = rs.getShort("part");
                itemTemp.isUpToUp = rs.getBoolean("is_up_to_up");
                itemTemp.strRequire = rs.getInt("power_require");
                itemTemp.gold = rs.getInt("gold");
                itemTemp.gem = rs.getInt("gem");
                itemTemp.head = rs.getInt("head");
                itemTemp.body = rs.getInt("body");
                itemTemp.leg = rs.getInt("leg");
                ITEM_TEMPLATES.add(itemTemp);
                ITEM_TEMPLATE_MAP.put(itemTemp.id, itemTemp); // Thêm vào map để truy cập theo ID
            }
            Logger.success("Successfully loaded map item template (" + ITEM_TEMPLATES.size() + ")\n");

            // load crop template (cho Cloud Garden)
            CropTemplate.clear(); // Clear dữ liệu cũ
            ps = con.prepareStatement("select * from crop_template ORDER BY id ASC");
            rs = ps.executeQuery();
            while (rs.next()) {
                CropTemplate cropTemp = new CropTemplate();
                cropTemp.id = rs.getByte("id");
                cropTemp.name = rs.getString("name");
                cropTemp.seedItemId = rs.getShort("seed_item_id");
                cropTemp.harvestItemId = rs.getShort("harvest_item_id");
                // Đọc TỔNG thời gian (phút) và convert sang ms
                int totalMinutes = rs.getInt("growth_time_minutes");
                cropTemp.totalGrowthTimeMs = totalMinutes * 60000L;
                // Tính thời gian từng giai đoạn theo tỷ lệ 1:2:2:3:2
                cropTemp.calculateStageTimes();
                cropTemp.minHarvest = rs.getInt("min_harvest");
                cropTemp.maxHarvest = rs.getInt("max_harvest");

                // Load image fields (with fallback if columns don't exist yet)
                try {
                    cropTemp.imgYoung = rs.getString("img_young");
                    cropTemp.imgMature = rs.getString("img_mature");
                    cropTemp.imgWithered = rs.getString("img_withered");
                } catch (Exception ignored) {
                    // Cột chưa có trong DB, sẽ dùng naming convention mặc định
                }

                CropTemplate.addCropTemplate(cropTemp);
            }
            // Refresh CropType array để tương thích với code cũ
            CropType.refreshFromTemplates();
            Logger.success("Successfully loaded crop template (" + CropTemplate.CROP_TEMPLATES.size() + ")\n");

            // load shop
            SHOPS = ShopDAO.getShops(con);
            Logger.success("Successfully loaded shop (" + SHOPS.size() + ")\n");

            // load item_nhabep
            try {
                ps = con.prepareStatement("select * from item_nhabep");
                rs = ps.executeQuery();
                while (rs.next()) {
                    item.ItemNhaBep inb = new item.ItemNhaBep();
                    inb.id = rs.getInt("id");
                    inb.item_id = rs.getInt("item_id");
                    inb.thoi_gian_nau = rs.getInt("thoi_gian_nau");
                    inb.don_gia_id = rs.getInt("don_gia_id");
                    inb.gia = rs.getInt("gia");
                    String nl = rs.getString("nguyen_lieu");
                    if (nl != null && !nl.isEmpty()) {
                        String[] arr = nl.replaceAll("[\\[\\]\\s]", "").split(",");
                        inb.nguyen_lieu = new short[arr.length];
                        for (int i = 0; i < arr.length; i++) {
                            if (!arr[i].isEmpty()) {
                                inb.nguyen_lieu[i] = Short.parseShort(arr[i]);
                            }
                        }
                    } else {
                        inb.nguyen_lieu = new short[0];
                    }

                    String slnl = rs.getString("soluong_nguyen_lieu");
                    if (slnl != null && !slnl.isEmpty()) {
                        String[] arr = slnl.replaceAll("[\\[\\]\\s]", "").split(",");
                        inb.soluong_nguyen_lieu = new short[arr.length];
                        for (int i = 0; i < arr.length; i++) {
                            if (!arr[i].isEmpty()) {
                                inb.soluong_nguyen_lieu[i] = Short.parseShort(arr[i]);
                            }
                        }
                    } else {
                        inb.soluong_nguyen_lieu = new short[0];
                    }

                    ITEM_NHA_BEP.add(inb);
                }
                Logger.success("Successfully loaded item_nhabep (" + ITEM_NHA_BEP.size() + ")\n");
            } catch (Exception e) {
                Logger.error("Failed to load item_nhabep: " + e.getMessage());
            }

            // load notify
            ps = con.prepareStatement("select * from notify order by id desc");
            rs = ps.executeQuery();
            while (rs.next()) {
                NOTIFY.add(rs.getString("name") + "<>" + rs.getString("text"));
            }
            Logger.success("Successfully loaded notify (" + NOTIFY.size() + ")\n");

            // load image by name
            ps = con.prepareStatement("select name, n_frame from img_by_name");
            rs = ps.executeQuery();
            while (rs.next()) {
                IMAGES_BY_NAME.put(rs.getString("name"), rs.getByte("n_frame"));
            }
            Logger.success("Successfully loaded images by name (" + IMAGES_BY_NAME.size() + ")\n");

            // Load mount
            for (ItemTemplate item : ITEM_TEMPLATES) {
                if (item.type == 23 && getNFrameImageByName("mount_" + item.part + "_0") != 0) {
                    MAP_MOUNT_NUM.put(item.id, (short) (item.part + 30000));
                }
            }
            Logger.success("Successfully loaded mount (" + MAP_MOUNT_NUM.size() + ")\n");

            PowerLimitManager.getInstance().load();
            CaptionManager.getInstance().load();

            // load mob template
            ps = con.prepareStatement("select * from mob_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                MobTemplate mobTemp = new MobTemplate();
                mobTemp.id = rs.getByte("id");
                mobTemp.type = rs.getByte("type");
                mobTemp.name = rs.getString("name");
                mobTemp.hp = rs.getInt("hp");
                mobTemp.rangeMove = rs.getByte("range_move");
                mobTemp.speed = rs.getByte("speed");
                mobTemp.dartType = rs.getByte("dart_type");
                mobTemp.percentDame = rs.getByte("percent_dame");
                mobTemp.percentTiemNang = rs.getByte("percent_tiem_nang");
                MOB_TEMPLATES.add(mobTemp);
            }
            Logger.success("Successfully loaded mob template (" + MOB_TEMPLATES.size() + ")\n");

            // load npc template
            ps = con.prepareStatement("select * from npc_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                NpcTemplate npcTemp = new NpcTemplate();
                npcTemp.id = rs.getByte("id");
                npcTemp.name = rs.getString("name");
                npcTemp.head = rs.getShort("head");
                npcTemp.body = rs.getShort("body");
                npcTemp.leg = rs.getShort("leg");
                npcTemp.avatar = rs.getInt("avatar");
                NPC_TEMPLATES.add(npcTemp);
            }
            Logger.success("Successfully loaded npc template (" + NPC_TEMPLATES.size() + ")\n");

            // load map template
            ps = con.prepareStatement("select count(id) from map_template");
            rs = ps.executeQuery();
            if (rs.next()) {
                int countRow = rs.getShort(1);
                MAP_TEMPLATES = new MapTemplate[countRow];
                ps = con.prepareStatement("select * from map_template");
                rs = ps.executeQuery();
                short i = 0;
                while (rs.next()) {
                    MapTemplate mapTemplate = new MapTemplate();
                    int mapId = rs.getInt("id");
                    String mapName = rs.getString("name");
                    mapTemplate.id = mapId;
                    mapTemplate.name = mapName;
                    mapTemplate.type = rs.getByte("type");
                    mapTemplate.planetId = rs.getByte("planet_id");
                    mapTemplate.bgType = rs.getByte("bg_type");
                    mapTemplate.tileId = rs.getByte("tile_id");
                    mapTemplate.bgId = rs.getByte("bg_id");
                    mapTemplate.zones = rs.getByte("zones");
                    mapTemplate.maxPlayerPerZone = rs.getByte("max_player");
                    // load waypoints
                    dataArray = (JSONArray) JSONValue.parse(rs.getString("waypoints")
                            .replaceAll("\\[\"\\[", "[[")
                            .replaceAll("\\]\"\\]", "]]")
                            .replaceAll("\",\"", ","));
                    for (int j = 0; j < dataArray.size(); j++) {
                        WayPoint wp = new WayPoint();
                        JSONArray dtwp = (JSONArray) JSONValue.parse(String.valueOf(dataArray.get(j)));
                        wp.name = String.valueOf(dtwp.get(0));
                        wp.minX = Short.parseShort(String.valueOf(dtwp.get(1)));
                        wp.minY = Short.parseShort(String.valueOf(dtwp.get(2)));
                        wp.maxX = Short.parseShort(String.valueOf(dtwp.get(3)));
                        wp.maxY = Short.parseShort(String.valueOf(dtwp.get(4)));
                        wp.isEnter = Byte.parseByte(String.valueOf(dtwp.get(5))) == 1;
                        wp.isOffline = Byte.parseByte(String.valueOf(dtwp.get(6))) == 1;
                        wp.goMap = Short.parseShort(String.valueOf(dtwp.get(7)));
                        wp.goX = Short.parseShort(String.valueOf(dtwp.get(8)));
                        wp.goY = Short.parseShort(String.valueOf(dtwp.get(9)));
                        mapTemplate.wayPoints.add(wp);
                        dtwp.clear();
                    }
                    dataArray.clear();
                    // load mobs
                    dataArray = (JSONArray) JSONValue.parse(rs.getString("mobs").replaceAll("\\\"", ""));
                    mapTemplate.mobTemp = new byte[dataArray.size()];
                    mapTemplate.mobLevel = new byte[dataArray.size()];
                    mapTemplate.mobHp = new int[dataArray.size()];
                    mapTemplate.mobX = new short[dataArray.size()];
                    mapTemplate.mobY = new short[dataArray.size()];
                    for (int j = 0; j < dataArray.size(); j++) {
                        JSONArray dtm = (JSONArray) JSONValue.parse(String.valueOf(dataArray.get(j)));
                        mapTemplate.mobTemp[j] = Byte.parseByte(String.valueOf(dtm.get(0)));
                        mapTemplate.mobLevel[j] = Byte.parseByte(String.valueOf(dtm.get(1)));
                        mapTemplate.mobHp[j] = Integer.parseInt(String.valueOf(dtm.get(2)));
                        mapTemplate.mobX[j] = Short.parseShort(String.valueOf(dtm.get(3)));
                        mapTemplate.mobY[j] = Short.parseShort(String.valueOf(dtm.get(4)));
                        dtm.clear();
                    }
                    dataArray.clear();
                    // load npcs
                    dataArray = (JSONArray) JSONValue.parse(rs.getString("npcs").replaceAll("\\\"", ""));
                    mapTemplate.npcId = new byte[dataArray.size()];
                    mapTemplate.npcX = new short[dataArray.size()];
                    mapTemplate.npcY = new short[dataArray.size()];
                    for (int j = 0; j < dataArray.size(); j++) {
                        JSONArray dtn = (JSONArray) JSONValue.parse(String.valueOf(dataArray.get(j)));
                        mapTemplate.npcId[j] = Byte.parseByte(String.valueOf(dtn.get(0)));
                        mapTemplate.npcX[j] = Short.parseShort(String.valueOf(dtn.get(1)));
                        mapTemplate.npcY[j] = Short.parseShort(String.valueOf(dtn.get(2)));
                        dtn.clear();
                    }
                    dataArray.clear();
                    // load eff
                    dataArray = (JSONArray) JSONValue.parse(rs.getString("effect"));
                    // for (int j = 0; j < dataArray.size(); j++) {
                    // EffectMap em = new EffectMap();
                    // dataObject = (JSONObject) JSONValue.parse(dataArray.get(j).toString());
                    // em.setKey(String.valueOf(dataObject.get("key")));
                    // em.setValue(String.valueOf(dataObject.get("value")));
                    // mapTemplate.effectMaps.add(em);
                    // }
                    EffectMap em = new EffectMap();
                    em.setKey("beff");
                    em.setValue("15");
                    mapTemplate.effectMaps.add(em);

                    dataArray.clear();
                    MAP_TEMPLATES[i++] = mapTemplate;
                }
                Logger.success("Successfully loaded map template (" + MAP_TEMPLATES.length + ")\n");
            }

            ps = con.prepareStatement("select * from radar");
            rs = ps.executeQuery();
            while (rs.next()) {
                RadarCard rd = new RadarCard();
                rd.Id = rs.getShort("id");
                rd.IconId = rs.getShort("iconId");
                rd.Rank = rs.getByte("rank");
                rd.Max = rs.getByte("max");
                rd.Type = rs.getByte("type");
                rd.Template = rs.getShort("mob_id");
                rd.Name = rs.getString("name");
                rd.Info = rs.getString("info");
                JSONArray arr = (JSONArray) JSONValue.parse(rs.getString("body"));
                for (int i = 0; i < arr.size(); i++) {
                    JSONObject ob = (JSONObject) arr.get(i);
                    if (ob != null) {
                        rd.Head = Short.parseShort(ob.get("head").toString());
                        rd.Body = Short.parseShort(ob.get("body").toString());
                        rd.Leg = Short.parseShort(ob.get("leg").toString());
                        rd.Bag = Short.parseShort(ob.get("bag").toString());
                    }
                }
                rd.Options.clear();
                arr = (JSONArray) JSONValue.parse(rs.getString("options"));
                for (int i = 0; i < arr.size(); i++) {
                    JSONObject ob = (JSONObject) arr.get(i);
                    if (ob != null) {
                        rd.Options.add(new OptionCard(Integer.parseInt(ob.get("id").toString()),
                                Short.parseShort(ob.get("param").toString()),
                                Byte.parseByte(ob.get("activeCard").toString())));
                    }
                }
                // rd.Require = rs.getShort("require");
                // rd.RequireLevel = rs.getShort("require_level");
                rd.AuraId = rs.getShort("aura_id");
                RadarService.gI().RADAR_TEMPLATE.add(rd);
            }
            Logger.success("Successfully loaded radar template (" + RadarService.gI().RADAR_TEMPLATE.size() + ")\n");

            File directory = new File("data/icon/x4");
            if (directory.isDirectory()) {
                Optional<File> maxFile = Arrays.stream(directory.listFiles())
                        .filter(File::isFile)
                        .filter(file -> file.getName().endsWith(".png"))
                        .max(Comparator.comparingInt(file -> {
                            String name = file.getName();
                            return Integer.valueOf(name.substring(0, name.length() - 4));
                        }));
                if (maxFile.isPresent()) {
                    String fileName = maxFile.get().getName();
                    short maxVersion = Short.parseShort(fileName.substring(0, fileName.length() - 4));
                    DataGame.maxSmallVersion = (short) (maxVersion + 1);
                    Logger.success("Successfully loaded max small version (" + DataGame.maxSmallVersion + ")\n");
                }
            }

            ps = con.prepareStatement("select * from data_badges");
            rs = ps.executeQuery();
            while (rs.next()) {
                BagesTemplate template = new BagesTemplate();
                template.id = rs.getInt("id");
                template.idEffect = rs.getInt("idEffect");
                template.idItem = rs.getInt("idItem");
                template.NAME = rs.getString("NAME");

                JSONArray option = (JSONArray) JSONValue.parse(rs.getString("Options"));
                ;
                if (option != null) {
                    for (int u = 0; u < option.size(); u++) {
                        JSONObject jsonobject = (JSONObject) option.get(u);
                        int optionId = Integer.parseInt(jsonobject.get("id").toString());
                        int param = Integer.parseInt(jsonobject.get("param").toString());
                        template.options.add(new Item.ItemOption(optionId, param));
                    }
                }
                BAGES_TEMPLATES.add(template);
            }
            Logger.success("Successfully loaded badges template (" + BAGES_TEMPLATES.size() + ")\n");

            topNV = realTop(ConstSQL.TOP_NV, con);
            Logger.success("Successfully loaded task top (" + topNV.size() + ")\n");
            topSM = realTop(ConstSQL.TOP_SM, con);
            Logger.success("Successfully loaded power top (" + topSM.size() + ")\n");
            topNap = realTop(ConstSQL.TOP_NAP, con);
            Logger.success("Successfully loaded nạp top (" + topNap.size() + ")\n");
            topWHIS = realTop(ConstSQL.TOP_WHIS, con);
            Logger.success("Successfully loaded WHIS top (" + topWHIS.size() + ")\n");
            topVDST = realTop(ConstSQL.TOP_VDST, con);
            Logger.success("Successfully loaded VDST top (" + topVDST.size() + ")\n");

            topSSM = realTop(ConstSQL.TOP_SO_SU_MENH, con);
            Logger.success("Successfully loaded topSSM top (" + topSSM.size() + ")\n");
            topbossday = realTop(ConstSQL.TOP_BOSS_DAY, con);
            Logger.success("Successfully loaded topbossday top (" + topbossday.size() + ")\n");
            topTet = realTop(ConstSQL.TOP_TET, con);
            Logger.success("Successfully loaded TOP_TET top (" + topTet.size() + ")\n");
            /*
             * topSD = realTop(ConstSQL.TOP_SD, con);
             * Logger.success("Successfully loaded TOP_SD top (" + topSD.size() + ")\n");
             */
            // topbang = realTop(ConstSQL.TOP_BANG, con);
            // Logger.success("Successfully loaded TOP_BANG top (" + topbang.size() +
            // ")\n");
            // topDuaSM = realTop(ConstSQL.TOP_DUA_SM, con);
            // Logger.success("Successfully loaded đua sm top (" + topDuaSM.size() + ")\n");
            // topDuaNap = realTop(ConstSQL.TOP_DUA_NAP, con);
            // Logger.success("Successfully loaded đua nạp top (" + topDuaNap.size() +
            // ")\n");

        } catch (Exception e) {
            Logger.logException(Manager.class,
                    e, "Database loading error");
            System.exit(0);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
            }
        }

        Logger.log(Logger.PURPLE, "Total database loading time: " + (System.currentTimeMillis() - st) + " (ms)\n");

        // Load MobReward từ database (hệ thống drop item mới)
        mob.MobRewardService.gI().load();
    }

    public void updateShop() {
        try (Connection con = DBConnecter.getConnectionServer();) {
            SHOPS = ShopDAO.getShops(con);
        } catch (Exception ex) {

        }
    }

    public static List<TOP> realTop(String query, Connection con) {
        int i = 0;
        List<TOP> tops = new ArrayList<>();
        JSONArray dataArray;
        try {
            PreparedStatement ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                short head = Util.getHead((byte) rs.getInt("gender"));
                short body = (short) (rs.getInt("gender") == 1 ? 59 : 57);
                short leg = (short) (rs.getInt("gender") == 1 ? 60 : 58);
                dataArray = (JSONArray) JSONValue.parse(rs.getString("items_body"));
                JSONArray dataItem = (JSONArray) JSONValue.parse(dataArray.get(0).toString());
                if (dataItem != null && dataItem.get(0) != null) {
                    Item item;
                    short tempId = Short.parseShort(String.valueOf(dataItem.get(0)));
                    if (tempId != -1) {
                        item = ItemService.gI().createNewItem(tempId,
                                Integer.parseInt(String.valueOf(dataItem.get(1))));
                        if (item.template != null) {
                            body = (short) item.template.part;
                        }
                    }
                }
                dataItem = (JSONArray) JSONValue.parse(dataArray.get(1).toString());
                if (dataItem != null && dataItem.get(0) != null) {
                    Item item;
                    short tempId = Short.parseShort(String.valueOf(dataItem.get(0)));
                    if (tempId != -1) {
                        item = ItemService.gI().createNewItem(tempId,
                                Integer.parseInt(String.valueOf(dataItem.get(1))));
                        if (item.template != null) {
                            leg = (short) item.template.part;
                        }
                    }
                }
                dataItem = (JSONArray) JSONValue.parse(dataArray.get(5).toString());
                if (dataItem != null && dataItem.get(0) != null) {
                    Item item;
                    short tempId = Short.parseShort(String.valueOf(dataItem.get(0)));
                    if (tempId != -1) {
                        item = ItemService.gI().createNewItem(tempId,
                                Integer.parseInt(String.valueOf(dataItem.get(1))));
                        if (item.template != null) {
                            if (item.template.head != -1) {
                                head = (short) item.template.head;
                            }
                            if (item.template.body != -1) {
                                body = (short) item.template.body;
                            }
                            if (item.template.leg != -1) {
                                leg = (short) item.template.leg;
                            }
                        }
                    }
                }
                dataArray.clear();
                TOP top = TOP.builder().name(rs.getString("name")).gender(rs.getByte("gender")).head(head).body(body)
                        .leg(leg).build();
                switch (query) {
                    case ConstSQL.TOP_NV -> {
                        top.setNv(rs.getByte("nv"));
                        top.setSubnv(rs.getByte("subnv"));
                        top.setLasttime(rs.getLong("lasttime"));
                    }
                    case ConstSQL.TOP_KI -> {

                    }
                    case ConstSQL.TOP_DC -> {
                        top.setDicanh(rs.getInt("dicanh"));
                        top.setJuventus(rs.getInt("juventus"));
                    }

                    case ConstSQL.TOP_SM -> {
                        top.setPower(rs.getLong("sm"));
                    }
                    case ConstSQL.TOP_NAP -> {
                        top.setCash(rs.getInt("cash"));
                    }
                    case ConstSQL.TOP_SO_SU_MENH -> {
                        top.setDiemsm(rs.getInt("point_value"));
                    }

                    case ConstSQL.TOP_TET -> {
                        top.setDiemtet(rs.getInt("pointtet"));
                    }
                    case ConstSQL.TOP_BANG -> {
                        top.setBanghoi(rs.getLong("power_point"));
                    }
                    case ConstSQL.TOP_BOSS_DAY -> {
                        top.setBossday(rs.getInt("san_boss_points"));
                    }
                    /*
                     * case ConstSQL.TOP_SD -> {
                     * top.setSd(rs.getInt("dame_point_fusion"));
                     * }
                     */

                    // case ConstSQL.TOP_DUA_SM -> {
                    // top.setPower(rs.getLong("sm"));
                    // }
                    // case ConstSQL.TOP_DUA_NAP -> {
                    // top.setCash(rs.getInt("danap"));
                    // }
                    // case ConstSQL.TOP_DUA_QUOC_VUONG -> {
                    // top.setThoivang(rs.getInt("thoi_vang"));
                    // }
                    case ConstSQL.TOP_WHIS -> {
                        top.setLasttime(rs.getLong("lasttime"));
                        top.setLevel(rs.getInt("top"));
                        top.setTime(rs.getInt("time"));

                        // switch (i) {
                        // case 0 ->
                        // top1Whis = rs.getLong("id");
                        // case 1 ->
                        // top2Whis = rs.getLong("id");
                        // case 2 ->
                        // top3Whis = rs.getLong("id");
                        // }
                        i++;
                    }
                    case ConstSQL.TOP_VDST -> {
                        top.setDivdst(rs.getInt("time"));
                        top.setLasttime(rs.getLong("lasttime"));
                        i++;
                    }
                }
                tops.add(top);
            }
        } catch (NumberFormatException | SQLException e) {
            e.printStackTrace();
        }
        return tops;
    }

    public void loadProperties() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream("data/config/config.properties"));
        Object value;
        if ((value = properties.get("server.sv")) != null) {
            SERVER = Byte.parseByte(String.valueOf(value));
        }
        if ((value = properties.get("server.name")) != null) {
            String name = String.valueOf(value);
            ServerManager.NAME = name;
        }
        if ((value = properties.get("server.port")) != null) {
            ServerManager.PORT = Integer.parseInt(String.valueOf(value));
        }
        String linkServer = "";
        if ((value = properties.get("server.ip")) != null) {
            ServerManager.IP = String.valueOf(value);
            linkServer += ServerManager.NAME + ":" + ServerManager.IP + ":" + ServerManager.PORT + ":0,";
        }
        for (int i = 1; i <= 10; i++) {
            value = properties.get("server.sv" + i);
            if (value != null) {
                linkServer += String.valueOf(value) + ":0,";
            }
        }
        DataGame.LINK_IP_PORT = linkServer.substring(0, linkServer.length() - 1);
        if ((value = properties.get("server.waitlogin")) != null) {
            SECOND_WAIT_LOGIN = Byte.parseByte(String.valueOf(value));
        }
        if ((value = properties.get("server.maxperip")) != null) {
            MAX_PER_IP = Integer.parseInt(String.valueOf(value));
        }
        if ((value = properties.get("server.maxplayer")) != null) {
            MAX_PLAYER = Integer.parseInt(String.valueOf(value));
        }
        if ((value = properties.get("server.expserver")) != null) {
            RATE_EXP_SERVER = Byte.parseByte(String.valueOf(value));
        }
        if ((value = properties.get("server.local")) != null) {
            LOCAL = String.valueOf(value).toLowerCase().equals("true");
        }
        if ((value = properties.get("server.test")) != null) {
            TEST = String.valueOf(value).toLowerCase().equals("true");
        }
        if ((value = properties.get("server.daoautoupdater")) != null) {
            DAO_AUTO_UPDATER = String.valueOf(value).toLowerCase().equals("true");
        }

        // Load event config
        for (Object key : properties.keySet()) {
            String keyStr = String.valueOf(key);
            if (keyStr.startsWith("event.")) {
                config.EventConfig.loadFromValue(keyStr, String.valueOf(properties.get(key)));
            }
        }
        config.EventConfig.printStatus();
    }

    /**
     * @param tileTypeFocus tile type: top, bot, left, right...
     * @return [tileMapId][tileType]
     */
    private int[][] readTileIndexTileType(int tileTypeFocus) {
        int[][] tileIndexTileType = null;
        try {
            DataInputStream dis = new DataInputStream(new FileInputStream("data/map/tile_set_info"));
            int numTileMap = dis.readByte();
            tileIndexTileType = new int[numTileMap][];
            for (int i = 0; i < numTileMap; i++) {
                int numTileOfMap = dis.readByte();
                for (int j = 0; j < numTileOfMap; j++) {
                    int tileType = dis.readInt();
                    int numIndex = dis.readByte();
                    if (tileType == tileTypeFocus) {
                        tileIndexTileType[i] = new int[numIndex];
                    }
                    for (int k = 0; k < numIndex; k++) {
                        int typeIndex = dis.readByte();
                        if (tileType == tileTypeFocus) {
                            tileIndexTileType[i][k] = typeIndex;

                        }
                    }
                }
            }
        } catch (IOException e) {
            Logger.logException(MapService.class,
                    e);
        }
        return tileIndexTileType;
    }
    /*
     * private int[][] readTileIndexTileType(int tileTypeFocus) {
     * int[][] tileIndexTileType = null;
     * try {
     * DataInputStream dis = new DataInputStream(new
     * FileInputStream("data/map/tile_set_info"));
     * int numTileMap = dis.readUnsignedByte();
     * tileIndexTileType = new int[numTileMap][];
     * for (int i = 0; i < numTileMap; i++) {
     * int numTileOfMap = dis.readUnsignedByte();
     * for (int j = 0; j < numTileOfMap; j++) {
     * int tileType = dis.readInt();
     * int numIndex = dis.readUnsignedByte();
     * if (tileType == tileTypeFocus) {
     * tileIndexTileType[i] = new int[numIndex];
     * }
     * for (int k = 0; k < numIndex; k++) {
     * int typeIndex = dis.readUnsignedByte();
     * if (tileType == tileTypeFocus) {
     * tileIndexTileType[i][k] = typeIndex;
     * 
     * }
     * }
     * }
     * }
     * } catch (IOException e) {
     * Logger.logException(MapService.class,
     * e);
     * }
     * return tileIndexTileType;
     * }
     */

    /**
     * @param mapId mapId
     * @return tile map for paint
     */
    private int[][] readTileMap(int mapId) {
        int[][] tileMap = null;
        try {
            try (DataInputStream dis = new DataInputStream(new FileInputStream("data/map/tile_map_data/" + mapId))) {
                int w = dis.readByte();
                int h = dis.readByte();
                tileMap = new int[h][w];
                for (int[] tm : tileMap) {
                    for (int j = 0; j < tm.length; j++) {
                        tm[j] = dis.readByte();
                    }
                }
            }
        } catch (IOException e) {
        }
        return tileMap;
    }

    public static Clan getClanById(int id) throws Exception {
        for (Clan clan : CLANS) {
            if (clan.id == id) {
                return clan;
            }
        }
        throw new Exception("Không tìm thấy clan id: " + id);
    }

    public static void addClan(Clan clan) {
        CLANS.add(clan);
    }

    public static int getNumClan() {
        return CLANS.size();

    }

    public static MobTemplate getMobTemplateByTemp(int mobTempId) {
        for (MobTemplate mobTemp : MOB_TEMPLATES) {
            if (mobTemp.id == mobTempId) {
                return mobTemp;
            }
        }
        return null;
    }

    public static byte getNFrameImageByName(String name) {
        Object n = IMAGES_BY_NAME.get(name);
        if (n != null) {
            return Byte.parseByte(String.valueOf(n));
        } else {
            return 0;
        }
    }

    // Xử lý menu Top
    public static Timestamp timeSuKienDuaTop = Timestamp.valueOf("2024-06-10 23:59:59");
    public static String timeStartDuaTop = "10h ngày 25/5/2024";
    public static String timeEndDuaTop = "23h59 ngày 10/6/2024";

    public static String demTimeSuKien() {
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime eventTime = timeSuKienDuaTop.toLocalDateTime();

        long daysRemaining = ChronoUnit.DAYS.between(currentTime, eventTime);
        if (daysRemaining > 0) {
            return "(" + daysRemaining + " ngày nữa)";
        } else {
            return "(Đã kết thúc)";
        }
    }
    // End xử lý menu top
}
