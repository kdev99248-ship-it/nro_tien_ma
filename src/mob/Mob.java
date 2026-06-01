package mob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import network.Message;
import utils.Logger;
import utils.Util;
import utils.TimeUtil;

import consts.ConstMap;
import consts.ConstMob;
import consts.ConstPlayer;
import consts.ConstTask;
import consts.ConstTaskBadges;
import consts.cn;

import event.EventManager;
import intrinsic.Intrinsic;
import item.Item;
import map.ItemMap;
import map.Zone;
import player.Location;
import player.Pet;
import player.Player;
import player.PlayerClone;
import player.LinhDanhThue;
import server.Maintenance;
import server.Manager;
import server.ServerNotify;
import services.InventoryService;
import services.ItemService;
import services.MapService;
import services.Service;
import services.TaskService;
import services.ItemMapService;
import services.func.ChangeMapService;
import skill.Skill;
import task.Badges.BadgesTaskService;
import models.Achievement.AchievementService;
import models.Training.TrainingService;

public class Mob {

    public int id;
    public Zone zone;
    public int tempId;
    public String name;
    public byte level;
    public int pt4la = 1;
    public List<Player> temporaryEnemies = new ArrayList<>();

    public MobPoint point;
    public MobEffectSkill effectSkill;
    public Location location;
    private Intrinsic intrinsic;
    public byte pDame;
    public int pTiemNang;
    private long maxTiemNang;

    public long lastTimeDie;
    public int lvMob = 0;
    public int status = 5;
    public int type = 1;

    private long lastTimeAttackPlayer;
    private long timeAttack = 2000;
    public long lastTimePhucHoi = System.currentTimeMillis();
    public long lastTimeSendEffect = System.currentTimeMillis();

    public Mob(Mob mob) {
        this.point = new MobPoint(this);
        this.effectSkill = new MobEffectSkill(this);
        this.location = new Location();
        this.id = mob.id;
        this.tempId = mob.tempId;
        this.level = mob.level;
        this.point.setHpFull(mob.point.getHpFull());
        this.point.sethp((this.point.getHpFull()));
        this.location.x = mob.location.x;
        this.location.y = mob.location.y;
        this.pDame = mob.pDame;
        this.pTiemNang = mob.pTiemNang;
        this.type = mob.type;
        this.setTiemNang();
    }

    public Mob() {
        this.point = new MobPoint(this);
        this.effectSkill = new MobEffectSkill(this);
        this.location = new Location();
    }

    public void setTiemNang() {
        this.maxTiemNang = (int) ((long) this.point.getHpFull() * (long) (this.pTiemNang + Util.nextInt(-2, 2)) / 100L);
    }

    public boolean isDie() {
        return this.point.gethp() <= 0;
    }

    public void setDie() {
        this.lastTimePhucHoi = System.currentTimeMillis();
        this.lastTimeDie = System.currentTimeMillis();
    }

    public void addTemporaryEnemies(Player pl) {
        if (pl != null && !temporaryEnemies.contains(pl)) {
            temporaryEnemies.add(pl);
        }
    }

    public void injured(Player plAtt, long damage, boolean dieWhenHpFull) {
        if (!this.isDie()) {
            if (damage >= this.point.hp) {
                damage = this.point.hp;
            }
            if (!dieWhenHpFull) {
                if (this.point.hp == this.point.maxHp && damage >= this.point.hp) {
                    damage = this.point.hp - 1;
                }
                if ((this.tempId == ConstMob.MOC_NHAN || this.tempId == ConstMob.BU_NHIN_MA_QUAI)
                        && damage > this.point.maxHp / 10) {
                    damage = this.point.maxHp / 10;
                }
            }
            if (MapService.gI().isMapKhiGasHuyDiet(this.zone.map.mapId)) {
                boolean mob76Die = true;
                for (Mob mob : this.zone.mobs) {
                    if (!mob.isDie() && mob.tempId == ConstMob.CO_MAY_HUY_DIET) {
                        mob76Die = false;
                        break;
                    }
                }
                if (!mob76Die && plAtt != null && plAtt.playerSkill != null && plAtt.playerSkill.skillSelect != null) {
                    switch (plAtt.playerSkill.skillSelect.template.id) {
                        case Skill.LIEN_HOAN, Skill.ANTOMIC, Skill.MASENKO, Skill.KAMEJOKO ->
                            damage = 1;
                    }
                }
            }
            if (!dieWhenHpFull && !isBigBoss() && !MapService.gI().isMapPhoBan(this.zone.map.mapId) && this.lvMob > 0
                    && plAtt != null && plAtt.charms.tdOaiHung < System.currentTimeMillis()) {
                damage = (int) ((this.point.maxHp <= 20000000 ? this.point.maxHp * 10 : 2000000000) * (10.0 / 100));
                this.mobAttackPlayer(plAtt);
            }
            if (plAtt != null && plAtt.isBoss && this.tempId > 0 && Util.isTrue(1, 2)
                    && Util.canDoWithTime(lastTimeAttackPlayer, 2500)) {
                this.mobAttackPlayer(plAtt);
                lastTimeAttackPlayer = System.currentTimeMillis();
            }

            // if (damage > 2_000_000_000) {
            // damage = 2_000_000_000;
            // }
            this.point.sethp(this.point.hp - damage);
            addTemporaryEnemies(plAtt);
            if (this.isDie()) {
                this.status = 0;
                this.setDie();
                this.temporaryEnemies.clear();
                if (plAtt != null) {
                    this.sendMobDieAffterAttacked(plAtt, (int) damage);
                    Player plCheck = plAtt;
                    if (plCheck instanceof Pet) {
                        plCheck = ((Pet) plCheck).master;
                    } else if (plCheck instanceof PlayerClone) {
                        plCheck = ((PlayerClone) plCheck).master;
                    } else if (plCheck instanceof LinhDanhThue) {
                        plCheck = ((LinhDanhThue) plCheck).master;
                    }
                    if (plCheck != null) {
                        TaskService.gI().checkDoneTaskKillMob(plCheck, this);
                        TaskService.gI().checkDoneSideTaskKillMob(plCheck, this);
                        TaskService.gI().checkDoneClanTaskKillMob(plCheck, this);
                        AchievementService.gI().checkDoneTaskKillMob(plCheck, this);
                    }
                }
                if (this.id == 13) {
                    this.zone.isbulon1Alive = false;
                }
                if (this.id == 14) {
                    this.zone.isbulon2Alive = false;
                }
            } else {
                this.sendMobStillAliveAffterAttacked(damage,
                        plAtt != null ? (plAtt.nPoint != null && plAtt.nPoint.isCrit) : false);
            }
            if (plAtt != null) {
                Player plReceive = plAtt;
                if (plAtt instanceof PlayerClone) {
                    plReceive = ((PlayerClone) plAtt).master;
                }
                if (plAtt instanceof LinhDanhThue) {
                    plReceive = ((LinhDanhThue) plAtt).master;
                }
                if (plReceive != null) {
                    if (plReceive.isPl() && plReceive.satellite != null && plReceive.satellite.isDefend) {
                        plReceive.satellite.isDefend = false;
                    }
                    Service.gI().addSMTN(plReceive, (byte) 2, getTiemNangForPlayer(plReceive, damage), true);
                    TrainingService.gI().tangTnsmLuyenTap(plReceive, getTiemNangForPlayer(plReceive, damage));
                }
            }
        }
    }

    public long getTiemNangForPlayer(Player pl, long dame) {
        int levelPlayer = Service.gI().getCurrLevel(pl);
        int n = levelPlayer - this.level;
        if (pl.zone != null && MapService.gI().isMapBanDoKhoBau(pl.zone.map.mapId)) {
            n = 0;
        }
        if (pl.nPoint != null && pl.nPoint.power < 40_000_000_000L) {
            n = 0;
        }
        long pDameHit = dame * 100 / point.getHpFull();
        long tiemNang = pDameHit * maxTiemNang / 100;
        if (tiemNang <= 0) {
            tiemNang = 1;
        }
        if (tiemNang >= 30000000) {
            tiemNang = 29999999;
        }

        if (n >= 0) {
            for (int i = 0; i < n; i++) {
                long sub = tiemNang * 10 / 100;
                if (sub <= 0) {
                    sub = 1;
                }
                tiemNang -= sub;
            }
        } else {
            for (int i = 0; i < -n; i++) {
                long add = tiemNang * 10 / 100;
                if (add <= 0) {
                    add = 1;
                }
                tiemNang += add;
            }
        }
        if (tiemNang <= 0) {
            tiemNang = 1;
        }
        if (pl.nPoint != null) {
            tiemNang = (int) pl.nPoint.calSucManhTiemNang(tiemNang);
        } else {
            return 0;
        }
        if (pl.zone.map.mapId == 122 || pl.zone.map.mapId == 123 || pl.zone.map.mapId == 124) {
            tiemNang *= 2;
        }
        return tiemNang;
    }

    public void update() {
        if (zone.isGoldenFriezaAlive && TimeUtil.is21H()) {
            if (!isDie()) {
                startDie();
                return;
            }
        }
        if (!this.isDie() && this.tempId == ConstMob.CO_MAY_HUY_DIET && Util.canDoWithTime(lastTimeSendEffect, 1000)) {
            sendEffect(55);
            lastTimeSendEffect = System.currentTimeMillis();
        }

        if (this.isDie() && !Maintenance.isRunning && !isBigBoss()) {
            switch (zone.map.type) {
                case ConstMap.MAP_DOANH_TRAI:
                    if (this.tempId == ConstMob.BULON && this.zone.isTUTAlive
                            && Util.canDoWithTime(lastTimeDie, 10000)) {
                        this.hoiSinh();
                        this.hoiSinhMobPhoBan();
                        if (this.id == 13) {
                            this.zone.isbulon1Alive = true;
                        }
                        if (this.id == 14) {
                            this.zone.isbulon2Alive = true;
                        }
                    }
                    break;
                case ConstMap.MAP_BAN_DO_KHO_BAU:
                    break;
                case ConstMap.MAP_CON_DUONG_RAN_DOC:
                    break;
                case ConstMap.MAP_KHI_GAS_HUY_DIET:
                    break;
                case ConstMap.MAP_TAY_KARIN:
                    break;
                default:
                    if (this.zone.isGoldenFriezaAlive && TimeUtil.is21H()) {
                        return;
                    }
                    if (Util.canDoWithTime(lastTimeDie, 5000)) {
                        this.hoiSinh();
                        this.sendMobHoiSinh();
                    }
                    if (Util.canDoWithTime(lastTimePhucHoi, 30000) && !isDie()) {
                        lastTimePhucHoi = System.currentTimeMillis();
                        long hpMax = this.point.maxHp;
                        if (this.point.hp < hpMax) {
                            hoi_hp(hpMax / 10);
                        } else {
                            this.sendMobHoiSinh();
                        }
                    }
            }
        }

        effectSkill.update();
        attack();
    }

    public boolean isBigBoss() {
        return (this.tempId == ConstMob.HIRUDEGARN
                || this.tempId == ConstMob.VUA_BACH_TUOC
                || this.tempId == ConstMob.ROBOT_BAO_VE
                || this.tempId == ConstMob.GAU_TUONG_CUOP
                || this.tempId == ConstMob.VOI_CHIN_NGA
                || this.tempId == ConstMob.GA_CHIN_CUA
                || this.tempId == ConstMob.NGUA_CHIN_LMAO
                || this.tempId == ConstMob.PIANO
                || this.tempId == ConstMob.KONG
                || this.tempId == ConstMob.GOZILLA);
    }

    public void attack() {
        Player player = getPlayerCanAttack();
        if (!isDie() && !effectSkill.isHaveEffectSkill() && tempId != ConstMob.MOC_NHAN
                && tempId != ConstMob.BU_NHIN_MA_QUAI && tempId != ConstMob.CO_MAY_HUY_DIET && !this.isBigBoss()
                && (this.lvMob < 1 || MapService.gI().isMapPhoBan(this.zone.map.mapId))
                && Util.canDoWithTime(lastTimeAttackPlayer, timeAttack)) {
            if (player != null) {
                this.mobAttackPlayer(player);
            }
            this.lastTimeAttackPlayer = System.currentTimeMillis();
        }
    }

    public Player getPlayerCanAttack() {
        Player plAttack = getFirstPlayerCanAttack();
        if (plAttack != null) {
            return plAttack;
        }
        int distance = 100;
        try {
            List<Player> players = this.zone.getNotBosses();
            for (Player pl : players) {
                if (!pl.isDie() && !pl.isBoss && !pl.isNewPet && (pl.satellite == null || !pl.satellite.isDefend)
                        && (pl.effectSkin == null || !pl.effectSkin.isVoHinh)
                        && (pl.effectSkill == null || !pl.effectSkill.isTanHinh)
                        && (this.tempId > 18 || (this.tempId > 9 && this.type == 4)) || isBigBoss()) {
                    int dis = Util.getDistance(pl, this);
                    if (dis <= distance || isBigBoss()) {
                        plAttack = pl;
                        distance = dis;
                    }
                }
            }
            this.timeAttack = 2000;
        } catch (Exception e) {

        }
        return plAttack;
    }

    private Player getFirstPlayerCanAttack() {
        Player plAtt = null;
        try {
            List<Player> playersMap = zone.getHumanoids();
            int dis = 300;
            if (playersMap != null) {
                for (Player plAttt : playersMap) {
                    if (plAttt.isDie() || plAttt.isBoss || (plAttt.satellite != null && plAttt.satellite.isDefend)
                            || (plAttt.effectSkin != null && plAttt.effectSkin.isVoHinh)
                            || (plAttt.effectSkill != null && plAttt.effectSkill.isTanHinh)
                            || !this.temporaryEnemies.contains(plAttt)) {
                        continue;
                    }
                    int d = Util.getDistance(plAttt, this);
                    if (d <= dis) {
                        dis = d;
                        plAtt = plAttt;
                    }
                }
            }
            this.timeAttack = 1000;
        } catch (Exception e) {

        }
        return plAtt;
    }

    private void mobAttackPlayer(Player player) {
        long dameMob = Util.maxIntValue(this.point.getDameAttack());
        if (player.charms != null && player.charms.tdDaTrau > System.currentTimeMillis()) {
            dameMob /= 2;
        }
        if (player.isPet && ((Pet) player).master.charms != null
                && ((Pet) player).master.charms.tdDeTu > System.currentTimeMillis()) {
            dameMob /= 2;
        }

        if (this.lvMob > 0 && !MapService.gI().isMapPhoBan(this.zone.map.mapId)) {
            dameMob = (long) (player.nPoint.hpMax * (10.0 / 100));
        }
        if (player.satellite != null && player.satellite.isDefend) {
            dameMob -= dameMob / 5;
        }
        if (player.itemTime != null && player.itemTime.isUseCMS) {
            dameMob = (long) Math.round(dameMob * 0.1);
        }
        if (this.lvMob > 0 && player.charms.tdOaiHung > System.currentTimeMillis()) {
            dameMob = 0;
        }
        long dame = player.injured(null, Util.maxIntValue(dameMob), false, true);

        this.sendMobAttackMe(player, dame);
        this.sendMobAttackPlayer(player);
        this.phanSatThuong(player, dame);
    }

    private void sendMobAttackMe(Player player, long dame) {
        if (!player.isPet && !player.isNewPet) {
            Message msg;
            try {
                msg = new Message(-11);
                msg.writer().writeByte(this.id);
                msg.writeLongByEmti(Util.maxIntValue(dame), cn.readInt); // dame
                player.sendMessage(msg);
                msg.cleanup();
            } catch (Exception e) {
            }
        }
    }

    private void sendMobAttackPlayer(Player player) {
        Message msg;
        try {
            msg = new Message(-10);
            msg.writer().writeByte(this.id);
            msg.writer().writeInt((int) player.id);
            msg.writeLongByEmti(Util.maxIntValue(player.nPoint.hp), cn.readInt); // hp
            Service.gI().sendMessAnotherNotMeInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void hoiSinh() {
        this.status = 5;
        this.point.sethp(this.point.maxHp);
        this.setTiemNang();
    }

    public int lvMob() {
        for (Mob mobMap : this.zone.mobs) {
            if (mobMap.lvMob > 0) {
                return 0;
            }
        }
        this.lvMob = this.tempId > 18 && !isBigBoss() ? Util.isTrue(10, 100) ? 1 : 0 : 0;
        this.point.sethp(this.lvMob > 0 ? this.point.maxHp <= 20000000 ? this.point.maxHp * 10 : 2000000000
                : this.point.maxHp);
        return this.lvMob;
    }

    public void sendMobHoiSinh() {
        Message msg = null;
        try {
            msg = new Message(-13);
            msg.writer().writeByte(this.id);
            msg.writer().writeByte(this.tempId);
            msg.writer().writeByte(lvMob());
            msg.writeLongByEmti(Util.maxIntValue(this.point.hp), cn.readInt);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            this.sendMobMaxHp(this.point.hp);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void hoi_hp(long hp) {
        Message msg = null;
        try {
            this.point.sethp((this.point.gethp() + hp));
            long HP = hp > 0 ? 1 : Math.abs(hp);
            msg = new Message(-9);
            msg.writer().writeByte(this.id);
            msg.writeLongByEmti(Util.maxIntValue(this.point.gethp()), cn.readInt);
            msg.writeLongByEmti(Util.maxIntValue(HP), cn.readInt);
            msg.writer().writeBoolean(false);
            msg.writer().writeByte(-1);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }

    public void sendEffect(int Effect) {
        Message msg = null;
        try {
            msg = new Message(-9);
            msg.writer().writeByte(this.id);
            msg.writeLongByEmti(Util.maxIntValue(this.point.gethp()), cn.readInt);
            msg.writeLongByEmti(Util.maxIntValue(this.point.gethp()), cn.readInt);
            msg.writer().writeBoolean(false);
            msg.writer().writeByte(Effect);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }

    private void sendMobDieAffterAttacked(Player plKill, long dameHit) {
        Message msg;
        try {
            msg = new Message(-12);
            msg.writer().writeByte(this.id);
            msg.writeLongByEmti(Util.maxIntValue(dameHit), cn.readInt);
            msg.writer().writeBoolean(plKill.nPoint.isCrit); // crit
            List<ItemMap> items = mobReward(plKill, this.dropItemTask(plKill), msg);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
            hutItem(plKill, items);
        } catch (Exception e) {
        }
    }

    private void hutItem(Player player, List<ItemMap> items) {
        if (!player.isPet && !player.isNewPet) {
            if (player.charms.tdThuHut > System.currentTimeMillis()) {
                for (ItemMap item : items) {
                    ItemMapService.gI().pickItem(player, item.itemMapId, true);
                }
            }
        } else {
            if (((Pet) player).master.charms.tdThuHut > System.currentTimeMillis()) {
                for (ItemMap item : items) {
                    ItemMapService.gI().pickItem(((Pet) player).master, item.itemMapId, true);
                }
            }
        }
    }

    private List<ItemMap> mobReward(Player player, ItemMap itemTask, Message msg) {
        List<ItemMap> itemReward = new ArrayList<>();
        try {
            itemReward = this.getItemMobReward(player, this.location.x + Util.nextInt(-10, 10),
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y));
            if (itemTask != null) {
                itemReward.add(itemTask);
            }
            msg.writer().writeByte(itemReward.size()); // sl item roi
            for (ItemMap itemMap : itemReward) {
                msg.writer().writeShort(itemMap.itemMapId);// itemmapid
                msg.writer().writeShort(itemMap.itemTemplate.id); // id item
                msg.writer().writeShort(itemMap.x); // xend item
                msg.writer().writeShort(itemMap.y); // yend item
                msg.writer().writeInt((int) itemMap.playerId); // id nhan vat
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return itemReward;
    }

    /**
     * Lấy danh sách vật phẩm rơi khi quái chết
     * ĐÃ REFACTOR: Sử dụng MobRewardService để load cấu hình từ Database
     * 
     * @param player Người chơi giết quái
     * @param x      Tọa độ x rơi item
     * @param yEnd   Tọa độ y rơi item
     * @return Danh sách ItemMap sẽ rơi
     */
    public List<ItemMap> getItemMobReward(Player player, int x, int yEnd) {
        List<ItemMap> list = new ArrayList<>();

        // Xử lý Linh Đánh Thuê
        if (player instanceof LinhDanhThue) {
            player = ((LinhDanhThue) player).master;
        }

        // Cập nhật nhiệm vụ huy hiệu
        BadgesTaskService.updateCountBagesTask(player, ConstTaskBadges.ONG_THAN_VE_CHAI, 1);

        // Boss không rơi đồ
        if (player.isBoss) {
            return list;
        }

        // Quái tempId = 0 không rơi đồ
        if (this.tempId == 0) {
            return list;
        }

        // === SỬ DỤNG HỆ THỐNG MỚI: Load drop từ Database ===
        List<ItemMap> dbDrops = MobRewardService.gI().getDrops(player, this, x, yEnd);
        list.addAll(dbDrops);

        // === CÁC LOGIC ĐẶC BIỆT VẪN GIỮ LẠI (không thể config trong DB) ===

        if (player.zone == null) {
            return list;
        }
        int mapid = player.zone.map.mapId;

        // Map 5, 13 - Bình nước (cần check itemEvent.canDropBinhNuoc)
        if (mapid == 5 || mapid == 13) {
            Player pl = player.isPet ? ((Pet) player).master : player;
            if (Util.isTrue(1, 500)) {
                if (pl.itemEvent != null && pl.itemEvent.canDropBinhNuoc(100)) {
                    list.add(new ItemMap(zone, 456, 1, x, yEnd, pl.id));
                }
            }
        }

        // Sự kiện 8/3 - Cần check itemEvent.canDropHoaHong
        if (EventManager.INTERNATIONAL_WOMANS_DAY) {
            Player pl = player.isPet ? ((Pet) player).master : player;
            if (Util.isTrue(1 * pt4la, 50)) {
                if (pl.itemEvent != null && pl.itemEvent.canDropHoaHong(100)) {
                    list.add(new ItemMap(zone, 610, 1, x, yEnd, player.id));
                }
            }
        }

        // Nhiệm vụ TASK_8_1 - Drop theo gender và mob
        if (player.isPl() && TaskService.gI().getIdTask(player) == ConstTask.TASK_8_1) {
            if (player.gender == 0 && this.tempId == 11 || player.gender == 1 && this.tempId == 12
                    || player.gender == 2 && this.tempId == 10) {
                list.add(new ItemMap(zone, 20, 1, x, yEnd, player.id));
            }
        }

        // Map Ngũ Hành Sơn - Cộng điểm event
        if (MapService.gI().isMapNguHanhSon(mapid) && this.tempId != 22) {
            if (Util.isTrue(20 * pt4la, 100) || (player.actived() && Util.isTrue(50, 100))) {
                player.event.addEventPointNHS(1);
            }
        }

        // Vàng rơi - Logic phức tạp với tlGold
        int nt = 1;
        if (player.nPoint.tlGold > 0) {
            nt += nt * player.nPoint.tlGold / 100;
        }
        if (Util.isTrue(10 * pt4la, 100) || player.actived() && Util.isTrue(1 * pt4la, 10)) {
            int vang = Util.nextInt(1000, 30000) * nt;
            if (vang < 10000) {
                list.add(new ItemMap(zone, 189, vang, x, yEnd, player.id));
            } else if (vang < 20000) {
                list.add(new ItemMap(zone, 188, vang, x, yEnd, player.id));
            } else {
                list.add(new ItemMap(zone, 190, vang, x, yEnd, player.id));
            }
        }

        // Buff Buma - Có chat message
        if (player.nPoint.isBuma) {
            int[] listitem = { 16, 1150, 1151, 1152, 1153, 1045, 1154, 663, 663, 665, 666, 667, 720 };
            int[] listitem2 = { 1788, 15, 1430 };
            if (Util.isTrue(1, 800)) {
                ItemMap it = new ItemMap(zone, listitem[Util.nextInt(0, 11)], 1, x, yEnd, player.id);
                Service.gI().chat(player, "Em ơi chị lụm được hàng hiếm");
                list.add(it);
            }
            if (Util.isTrue(1, 3000)) {
                ItemMap it = new ItemMap(zone, listitem2[Util.nextInt(0, 2)], 1, x, yEnd, player.id);
                Service.gI().chat(player, "Nổ Hũ rồi em ơi!!");
                list.add(it);
            }
        }

        return list;
    }

    private ItemMap dropItemTask(Player player) {
        ItemMap itemMap = null;
        switch (tempId) {
            case ConstMob.KHUNG_LONG:
                if (Util.isTrue(1, 100)) {
                    itemMap = new ItemMap(zone, 828, 1, location.x, location.y, player.id);
                }
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_2_0) {
                    itemMap = new ItemMap(zone, 73, 1, location.x, location.y, player.id);
                }
                break;
            case ConstMob.LON_LOI:
                if (Util.isTrue(1, 100)) {
                    itemMap = new ItemMap(zone, 829, 1, location.x, location.y, player.id);
                }
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_2_0) {
                    itemMap = new ItemMap(zone, 73, 1, location.x, location.y, player.id);
                }
                break;
            case ConstMob.QUY_DAT:
                if (Util.isTrue(1, 100)) {
                    itemMap = new ItemMap(zone, 830, 1, location.x, location.y, player.id);
                }
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_2_0) {
                    itemMap = new ItemMap(zone, 73, 1, location.x, location.y, player.id);
                }
                break;
            case ConstMob.KHUNG_LONG_ME:
                if (Util.isTrue(1, 100)) {
                    itemMap = new ItemMap(zone, 831, 1, location.x, location.y, player.id);
                }
                break;
            case ConstMob.LON_LOI_ME:

                if (Util.isTrue(1, 100)) {
                    itemMap = new ItemMap(zone, 832, 1, location.x, location.y, player.id);
                }
                break;
            case ConstMob.QUY_DAT_ME:

                if (Util.isTrue(1, 100)) {
                    itemMap = new ItemMap(zone, 833, 1, location.x, location.y, player.id);
                }
                break;
            case ConstMob.THAN_LAN_BAY:

                if (Util.isTrue(1, 100)) {
                    itemMap = new ItemMap(zone, 834, 1, location.x, location.y, player.id);
                }
                break;
            case ConstMob.PHI_LONG:

                if (Util.isTrue(1, 100)) {
                    itemMap = new ItemMap(zone, 835, 1, location.x, location.y, player.id);
                }
                break;
            case ConstMob.QUY_BAY:

                if (Util.isTrue(1, 100)) {
                    itemMap = new ItemMap(zone, 836, 1, location.x, location.y, player.id);
                }
                break;
            case ConstMob.LINH_DOC_NHAN:

                if (Util.isTrue(1, 100)) {
                    itemMap = new ItemMap(zone, 837, 1, location.x, location.y, player.id);
                }
                break;
            case ConstMob.LINH_DOC_NHAN2:

                if (Util.isTrue(1, 100)) {
                    itemMap = new ItemMap(zone, 838, 1, location.x, location.y, player.id);
                }
                break;

            case ConstMob.SOI_XAM:

                if (Util.isTrue(1, 100)) {
                    itemMap = new ItemMap(zone, 839, 1, location.x, location.y, player.id);
                }
                break;
            case ConstMob.THAN_LAN_ME:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_8_1) {
                    if (Util.isTrue(1, 3)) {
                        itemMap = new ItemMap(zone, 20, 1, location.x, location.y, player.id);
                    } else {
                        Service.gI().sendThongBao(player,
                                "Con thằn lằn mẹ này không giữ ngọc, hãy tìm con thằn lằn mẹ khác");
                    }
                }
            case ConstMob.OC_MUON_HON:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_14_1) {
                    if (Util.isTrue(1, 3)) {
                        itemMap = new ItemMap(zone, 85, 1, location.x, location.y, player.id);
                    } else {
                        Service.gI().sendThongBao(player,
                                "Con ốc mượn hồn này không giữ truyện tranh, hãy thử tìm con ốc mượn hồn khác");
                    }
                }
            case ConstMob.HEO_XAYDA_ME:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_14_1) {
                    if (Util.isTrue(1, 3)) {
                        itemMap = new ItemMap(zone, 85, 1, location.x, location.y, player.id);
                    } else {
                        Service.gI().sendThongBao(player,
                                "Con heo xayda mẹ này không giữ truyện tranh, hãy thử tìm con heo xayda mẹ khác");
                    }
                }
            case ConstMob.OC_SEN:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_14_1) {
                    if (Util.isTrue(1, 3)) {
                        itemMap = new ItemMap(zone, 85, 1, location.x, location.y, player.id);
                    } else {
                        Service.gI().sendThongBao(player,
                                "Con ốc xên này không giữ truyện tranh, hãy thử tìm con ốc xên khác");
                    }
                }
        }
        if (itemMap != null) {
            return itemMap;
        }
        return null;
    }

    private void sendMobStillAliveAffterAttacked(long dameHit, boolean crit) {
        Message msg;
        try {
            msg = new Message(-9);
            msg.writer().writeByte(this.id);
            msg.writeLongByEmti(Util.maxIntValue(this.point.gethp()), cn.readInt);
            msg.writeLongByEmti(Util.maxIntValue(dameHit), cn.readInt);
            msg.writer().writeBoolean(crit); // chí mạng
            msg.writer().writeInt(-1);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void hoiSinhMobPhoBan() {
        this.point.sethp(this.point.maxHp);
        this.setTiemNang();
        Message msg;
        try {
            msg = new Message(-13);
            msg.writer().writeByte(this.id);
            msg.writer().writeByte(this.tempId);
            msg.writer().writeByte(this.lvMob); // level mob
            msg.writeLongByEmti(Util.maxIntValue(this.point.hp), cn.readInt);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void hoiSinhMobTayKarin() {
        this.point.sethp(this.point.maxHp);
        this.maxTiemNang = 1;
        Message msg;
        try {
            msg = new Message(-13);
            msg.writer().writeByte(this.id);
            msg.writer().writeByte(this.tempId);
            msg.writer().writeByte(this.lvMob); // level mob
            msg.writeLongByEmti(Util.maxIntValue(this.point.hp), cn.readInt);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendSieuQuai(int type) {
        Message msg;
        try {
            msg = new Message(-75);
            msg.writer().writeByte(this.id);
            msg.writer().writeByte(type);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendDisable(boolean bool) {
        Message msg;
        try {
            msg = new Message(81);
            msg.writer().writeByte(this.id);
            msg.writer().writeBoolean(bool);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendDoneMove(boolean bool) {
        Message msg;
        try {
            msg = new Message(82);
            msg.writer().writeByte(this.id);
            msg.writer().writeBoolean(bool);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendFire(boolean bool) {
        Message msg;
        try {
            msg = new Message(85);
            msg.writer().writeByte(this.id);
            msg.writer().writeBoolean(bool);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendIce(boolean bool) {
        Message msg;
        try {
            msg = new Message(86);
            msg.writer().writeByte(this.id);
            msg.writer().writeBoolean(bool);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendWind(boolean bool) {
        Message msg;
        try {
            msg = new Message(87);
            msg.writer().writeByte(this.id);
            msg.writer().writeBoolean(bool);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendMobMaxHp(long maxHp) {
        Message msg;
        try {
            msg = new Message(87);
            msg.writer().writeByte(this.id);
            msg.writeLongByEmti(Util.maxIntValue(maxHp), cn.readInt);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    private void phanSatThuong(Player plTarget, long dame) {
        if (plTarget.nPoint == null) {
            return;
        }
        int percentPST = plTarget.nPoint.tlPST;
        if (percentPST != 0) {
            long damePST = Util.maxIntValue(dame * percentPST / 100L);
            Message msg;
            try {
                msg = new Message(-9);
                msg.writer().writeByte(this.id);
                if (damePST >= this.point.hp) {
                    damePST = this.point.hp - 1;
                }
                long hpMob = Util.maxIntValue(this.point.hp);
                injured(null, damePST, true);
                damePST = hpMob - this.point.hp;
                msg.writeLongByEmti(Util.maxIntValue(this.point.hp), cn.readInt);
                msg.writeLongByEmti(Util.maxIntValue(damePST), cn.readInt);
                msg.writer().writeBoolean(false);
                msg.writer().writeByte(36);
                Service.gI().sendMessAllPlayerInMap(this.zone, msg);
                msg.cleanup();
            } catch (IOException e) {
            }
        }
    }

    public void startDie() {
        Message msg;
        try {
            setDie();
            this.point.hp = -1;
            this.status = 0;
            msg = new Message(-12);
            msg.writer().writeByte(this.id);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }
}
