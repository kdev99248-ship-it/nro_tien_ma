package models.Training;
import boss.BossManager;
import boss.BossID;


/*
 *
 *
 * @author EMTI
 */

import boss.Boss;
import consts.ConstNpc;
import map.Zone;
import player.Player;
import services.MapService;
import services.NpcService;
import services.Service;
import services.func.ChangeMapService;
import utils.Logger;
import utils.Util;

public class TrainingService {

    private static TrainingService instance;

    public static TrainingService gI() {
        if (instance == null) {
            instance = new TrainingService();
        }
        return instance;
    }

    public Player getNonInteractiveNPC(Zone zone, int id) {
        for (Player pl : zone.getNonInteractiveNPCs()) {
            if (pl != null && pl.id == id) {
                return pl;
            }
        }
        return null;
    }

    public int getNpc(int BossId) {
        switch (BossId) {
            case BossID.KARIN -> {
                return ConstNpc.THAN_MEO_KARIN;
            }
            case BossID.THUONG_DE -> {
                return ConstNpc.THUONG_DE;
            }
            case BossID.THAN_VU_TRU -> {
                return ConstNpc.THAN_VU_TRU;
            }
            case BossID.TO_SU_KAIO -> {
                return ConstNpc.TO_SU_KAIO;
            }
            case BossID.WHIS -> {
                return ConstNpc.WHIS;
            }
        }
        return -1;
    }

    public void luyenTapEnd(Player pl, int bossID) {
        if (getNpc(bossID) != -1) {
            Service.gI().sendHideNpc(pl, getNpc(bossID), false);
        }
    }

    public Boss callBoss(Player pl, int bossID, boolean isThachDau) {
        try {
            pl.isThachDau = isThachDau;
            if (getNpc(bossID) != -1) {
                Service.gI().sendHideNpc(pl, getNpc(bossID), true);
            }
            Boss boss = BossManager.gI().createBoss(bossID);
            if (boss != null) {
                if (boss != null) {
                    boss.playerAtt = pl;
                }
                
                // Handle special case for map change
                if (bossID == BossID.THUONG_DE) {
                    ChangeMapService.gI().changeMap(pl, MapService.gI().getMapCanJoin(pl, 49, 0), 362, 408);
                }
                
                return boss;
            }

        } catch (Exception e) {
            Logger.logException(TrainingService.class, e);
        }
        return null;
    }

    public int getTnsmMoiPhut(Player player) {
        return switch (player.levelLuyenTap) {
            case 0 -> 20;
            case 1 -> 40;
            case 2 -> 80;
            case 3 -> 160;
            case 4 -> 320;
            case 5 -> 640;
            default -> player.tnsmLuyenTap > 1280 ? player.tnsmLuyenTap : 1280;
        };
    }

    public void tangTnsmLuyenTap(Player player, long tnsm) {
        if (player.isPl()) {
            player.tnsmLuyenTap += Math.max(100, tnsm / (100 * (Service.gI().getCurrLevel(player) + 1)));
            if (player.tnsmLuyenTap > 10_000_000) {
                player.tnsmLuyenTap = 10_000_000;
            }
        }
    }

    public void tnsmLuyenTapUp(Player player) {
        long tnsm;
        int time = (int) ((System.currentTimeMillis() - player.lastTimeOffline) / 1000);
        if (time > 60) {
            tnsm = ((long) getTnsmMoiPhut(player) * (long) ((time > 86400 ? 86400 : time)) / 60);
            if (MapService.gI().isMapLuyenTap(player.zone.map.mapId)) {
                NpcService.gI().createTutorial(player, -1, "Bạn tăng được " + Util.numberToMoney(tnsm) + " sức mạnh trong thời gian " + (time / 60) + " phút tập luyện Offline");
                Service.gI().addSMTN(player, (byte) 2, tnsm, false);
            } else if (player.dangKyTapTuDong && time > 1800) {
                if (player.inventory.getGemAndRuby() > 1) {
                    new Thread(() -> {
                        try {
                            player.inventory.subGemAndRuby(1);
                            Thread.sleep(1000);
                            if (player.zone == null) {
                                return;
                            }
                            player.lastMapOffline = player.zone.map.mapId;
                            player.lastZoneOffline = player.zone.zoneId;
                            player.lastXOffline = player.location.x;
                            Service.gI().addSMTN(player, (byte) 2, tnsm, false);
                            player.teleTapTuDong = true;
                            player.thongBaoTapTuDong = "Bạn tăng được " + Util.numberToMoney(tnsm) + " sức mạnh trong thời gian " + (time / 60) + " phút tập luyện Offline, -1 ngọc (phí đăng ký tập tự động)";
                            ChangeMapService.gI().changeMapBySpaceShip(player, player.mapIdDangTapTuDong, 0, Util.nextInt(200, 400));
                            Service.gI().sendMoney(player);
                        } catch (InterruptedException e) {
                        }
                    }, "Luyện Tập").start();
                } else {
                    player.dangKyTapTuDong = false;
                    Service.gI().sendThongBao(player, "Bạn không đủ ngọc, đăng ký luyện tập tự động đã bị hủy");
                }
            }
        }
        if (Util.isAfterMidnight(player.lastTimeOffline)) {
            if (player.tnsmLuyenTap > 1) {
                player.tnsmLuyenTap -= player.tnsmLuyenTap / 3;
            }
            player.lastTimeOffline = System.currentTimeMillis();
        }
    }
}