package boss.boss_manifest.LunarNewYearEvent;

/*
 *
 *
 * @author EMTI
 */
import boss.*;
import static boss.BossType.TET_EVENT;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import map.ItemMap;
import map.Zone;
import models.TetEvent.TetEventService;
import player.Player;
import server.Client;
import services.EffectSkillService;
import services.ItemService;
import services.MapService;
import services.Service;
import services.func.ChangeMapService;
import utils.Logger;
import utils.Util;

public class LanCon extends Boss {

    private long st;
    private int timeLeave;
    private long lastTimeAtt;
    private long playerId;
    private boolean afk;

    public LanCon() throws Exception {
        super(TET_EVENT, BossID.LAN_CON - Util.nextInt(1000), BossesData.LAN_CON);
    }

    /**
     * Kiểm tra zone có boss LanCon nào khác đang tồn tại không
     */
    private boolean hasOtherLanConInZone(Zone zoneCheck) {
        for (Boss boss : BossManager.gI().getBosses()) {
            if (boss != this && boss instanceof LanCon && boss.zone != null
                    && boss.zone.equals(zoneCheck) && !boss.isDie()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Kiểm tra map có boss LanCon nào khác đang tồn tại không
     */
    private boolean hasOtherLanConInMap(int mapId) {
        for (Boss boss : BossManager.gI().getBosses()) {
            if (boss != this && boss instanceof LanCon && boss.zone != null
                    && boss.zone.map.mapId == mapId && !boss.isDie()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Zone getMapJoin() {
        // Lấy danh sách map join từ data
        int[] mapJoinArr = this.data[this.currentLevel].getMapJoin();
        // Shuffle danh sách map để random
        List<Integer> mapList = new ArrayList<>();
        for (int mapId : mapJoinArr) {
            mapList.add(mapId);
        }
        Collections.shuffle(mapList);

        // Ưu tiên chọn map chưa có boss LanCon nào
        for (int mapId : mapList) {
            if (!hasOtherLanConInMap(mapId)) {
                Zone zone = MapService.gI().getMapWithRandZone(mapId);
                if (zone != null) {
                    return zone;
                }
            }
        }

        // Nếu tất cả map đều có boss LanCon, chọn map+zone không trùng
        for (int mapId : mapList) {
            map.Map map = MapService.gI().getMapById(mapId);
            if (map != null && map.zones != null) {
                List<Integer> zoneIndexes = new ArrayList<>();
                for (int i = 0; i < map.zones.size(); i++) {
                    zoneIndexes.add(i);
                }
                Collections.shuffle(zoneIndexes);
                for (int zIdx : zoneIndexes) {
                    Zone z = map.zones.get(zIdx);
                    if (!hasOtherLanConInZone(z)) {
                        return z;
                    }
                }
            }
        }

        // Fallback: trả về random map+zone
        int mapId = mapJoinArr[Util.nextInt(0, mapJoinArr.length - 1)];
        return MapService.gI().getMapWithRandZone(mapId);
    }

    @Override
    public void joinMap() {
        if (zoneFinal != null) {
            joinMapByZone(zoneFinal);
            this.notifyJoinMap();
            this.changeStatus(BossStatus.CHAT_S);
            this.wakeupAnotherBossWhenAppear();
            return;
        }
        if (this.zone == null) {
            if (this.parentBoss != null) {
                this.zone = parentBoss.zone;
            } else if (this.lastZone == null) {
                this.zone = getMapJoin();
            } else {
                this.zone = this.lastZone;
            }
        }
        if (this.zone != null) {
            try {
                // Random zone trong map, tránh trùng với boss LanCon khác
                List<Integer> zoneIndexes = new ArrayList<>();
                for (int i = 0; i < this.zone.map.zones.size(); i++) {
                    zoneIndexes.add(i);
                }
                Collections.shuffle(zoneIndexes);

                int selectedZoneId = -1;
                for (int zIdx : zoneIndexes) {
                    Zone candidateZone = this.zone.map.zones.get(zIdx);
                    // Bỏ qua khu quá đông (>10 người)
                    if (candidateZone.getNumOfPlayers() > 10) {
                        continue;
                    }
                    // Bỏ qua khu đã có boss LanCon khác
                    if (hasOtherLanConInZone(candidateZone)) {
                        continue;
                    }
                    selectedZoneId = zIdx;
                    break;
                }

                if (selectedZoneId >= 0) {
                    this.zone = this.zone.map.zones.get(selectedZoneId);
                } else {
                    // Không tìm được zone phù hợp → rời map
                    this.leaveMapNew();
                    return;
                }
                ChangeMapService.gI().changeMap(this, this.zone, Util.nextInt(100, 500),
                        this.zone.map.yPhysicInTop(this.location.x,
                                this.location.y - 24));
                this.changeStatus(BossStatus.CHAT_S);
                st = System.currentTimeMillis();
                timeLeave = Util.nextInt(100000, 300000);
            } catch (Exception e) {
                Logger.error(this.data[0].getName() + ": Lỗi đang tiến hành REST\n");
                this.changeStatus(BossStatus.REST);
            }
        } else {
            Logger.error(this.data[0].getName() + ": Lỗi map đang tiến hành RESPAWN\n");
            this.changeStatus(BossStatus.RESPAWN);
        }
    }

    @Override
    public void chatM() {
        if (this.data[this.currentLevel].getTextM().length == 0) {
            return;
        }
        if (!Util.canDoWithTime(this.lastTimeChatM, this.timeChatM)) {
            return;
        }
        String textChat = this.data[this.currentLevel].getTextM()[Util.nextInt(0,
                this.data[this.currentLevel].getTextM().length - 1)];
        int prefix = Integer.parseInt(textChat.substring(1, textChat.lastIndexOf("|")));
        textChat = textChat.substring(textChat.lastIndexOf("|") + 1);
        this.chat(prefix, textChat);
        this.lastTimeChatM = System.currentTimeMillis();
        this.timeChatM = Util.nextInt(3000, 20000);
    }

    @Override
    public void autoLeaveMap() {
        if (Util.canDoWithTime(st, timeLeave)) {
            this.leaveMapNew();
        }
    }

    @Override
    public void leaveMap() {
        ChangeMapService.gI().exitMap(this);
        this.lastZone = null;
        this.playerId = -1;
        this.lastTimeRest = System.currentTimeMillis();
        this.changeStatus(BossStatus.REST);
    }

    // @Override
    // public void attack() {
    // if (Util.canDoWithTime(this.lastTimeAttack, 250)) {
    // this.lastTimeAttack = System.currentTimeMillis();
    // try {
    // Player pl = getPlayerAttack();
    // if (pl == null || pl.location == null || pl.isDie()) {
    // return;
    // }
    // int dis = Util.getDistance(this, pl);
    // if (dis > 450) {
    // move(pl.location.x - 24, pl.location.y);
    // } else if (dis > 100) {
    // int dir = (this.location.x - pl.location.x < 0 ? 1 : -1);
    // int move = Util.nextInt(50, 100);
    // move(this.location.x + (dir == 1 ? move : -move), pl.location.y);
    // } else {
    // if (Util.canDoWithTime(lastTimeAtt, 30000) && this.nPoint.hp <
    // this.nPoint.hpMax) {
    // if (Util.isTrue(10, 100)) {
    // Service.gI().moveFast(pl, this.location.x, this.location.y);
    // pl.setDie();
    // Service.gI().sendThongBao(pl, "Bạn đã bị Lân con húc chết!");
    // }
    // lastTimeAtt = System.currentTimeMillis();
    // }
    // }
    // } catch (Exception ex) {
    // }
    // }
    // }
    // @Override
    // public void afk() {
    // if (Util.canDoWithTime(this.lastTimeAttack, 500)) {
    // this.lastTimeAttack = System.currentTimeMillis();
    // Player pl = Client.gI().getPlayer(playerId);
    // if (pl == null || pl.zone == null) {
    // return;
    // }
    // if (pl.haveReward) {
    // pl.haveReward = false;
    // this.leaveMap();
    // return;
    // }
    // if (this.zone.equals(pl.zone)) {
    // int dis = Util.getDistance(this, pl);
    // if (dis <= 300) {
    // if (dis > 50) {
    // int dir = (this.location.x - pl.location.x < 0 ? 1 : -1);
    // int move = Util.nextInt(50, 100);
    // move(this.location.x + (dir == 1 ? move : -move), pl.location.y);
    // st = System.currentTimeMillis();
    // }
    // afk = false;
    // pl.canReward = true;
    // } else {
    // afk = true;
    // pl.canReward = false;
    // }
    // } else if (!afk) {
    // if (pl.changeMapVIP) {
    // pl.changeMapVIP = false;
    // pl.canReward = false;
    // afk = true;
    // return;
    // }
    // ChangeMapService.gI().changeMap(this, pl.zone, pl.location.x +
    // Util.nextInt(-10, 10), pl.location.y);
    // }
    // }
    // }
    @Override
    public synchronized long injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (!piercing && Util.isTrue(100, 1000)) {
                this.chat("Xí hụt");
                return 0;
            }
            damage = this.nPoint.subDameInjureWithDeff(damage);
            if (!piercing && effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = 1;
            }
            if (damage > 1) {
                damage = 1;
            }
            this.nPoint.subHP(damage);
            if (isDie()) {
                this.setDie(plAtt);
                die(plAtt);
            }
            return damage;
        } else {
            this.setDie(plAtt);
            die(plAtt);
            return 0;
        }
    }

    @Override
    public void reward(Player plKill) {
        int x = this.location.x;
        int y = this.zone.map.yPhysicInTop(x, this.location.y - 24);

        // Rơi Bao Lì Xì (item 1183) - xác suất 70%, số lượng 1-3
        Random random = new Random();
        int randomValue = random.nextInt(100);
        if (randomValue < 70) {
            Service.gI().dropItemMap(this.zone, new ItemMap(zone,
                    TetEventService.BAO_LI_XI, Util.nextInt(1, 3), x, y, plKill.id));
        }

        // Rơi nguyên liệu sự kiện Tết - mỗi loại 5 cái
        int[] eventMaterials = {
                TetEventService.MANG_CAU, // 1177
                TetEventService.DUA, // 1178
                TetEventService.DU_DU, // 1179
                TetEventService.XOAI, // 1180
                TetEventService.SUNG // 1181
        };
        for (int matId : eventMaterials) {
            Service.gI().dropItemMap(this.zone, new ItemMap(zone,
                    matId, Util.nextInt(2, 10), x + Util.nextInt(-30, 30), y, plKill.id));
        }
    }
}
