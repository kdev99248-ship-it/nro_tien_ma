package boss.boss_manifest.BossPhu;
import boss.BossID;
import boss.BossData;


import boss.Boss;
import boss.BossStatus;
import static boss.BossType.ANTROM;
import consts.ConstPlayer;
import lombok.Setter;
import map.Zone;
import player.Player;
import services.MapService;
import services.PlayerService;
import services.SkillService;
import services.func.ChangeMapService;
import skill.Skill;
import utils.Util;
import java.util.List;


public class SOI_HEC_QUEN extends Boss {

    @Setter
    private Player player;

    private long lastTimeAttack;
    private long lastMoveTime;
    private long st;
    private static final long TIME_CHANGE_MAP = 200000; // 200 giây

    public SOI_HEC_QUEN() throws Exception {
        super(ANTROM, BossID.SOI_HEC_QUEN, new BossData(
                "SÓI HEC QUYN " + Util.nextInt(1, 100),
                ConstPlayer.TRAI_DAT,
                new short[] { 394, 395, 396, -1, -1, -1 },
                1,
                new long[] { 500000L },
                new int[] { 5, 7, 0, 14, 1, 2, 3, 4, 5, 8, 9, 10, 11 },
                new int[][] {
                        { Skill.TAI_TAO_NANG_LUONG, 3, 500000 }
                },
                new String[] { "|-1|Gâu gâu gâu:))" },
                new String[] { "|-1|Cho ăn xương trả quà!" },
                new String[] { "|-1|Cho xương hay bị tao cắn???" },
                600));
    }

    @Override
    public Zone getMapJoin() {
        int mapId = this.data[this.currentLevel].getMapJoin()[Util.nextInt(0,
                this.data[this.currentLevel].getMapJoin().length - 1)];
        return MapService.gI().getMapById(mapId).zones.get(5);
    }

    @Override
    public synchronized long injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        return 0; // Không nhận sát thương từ người chơi
    }

    @Override
    public void attack() {
        if (Util.canDoWithTime(this.lastTimeAttack, 100) && this.typePk == ConstPlayer.PK_ALL) {
            this.lastTimeAttack = System.currentTimeMillis();

            try {
                Player pl = this.getPlayerAttack();
                if (pl == null || pl.isDie()) {
                    return;
                }

                if (Util.getDistance(this, pl) >= 40) {
                    pl.nPoint.xoihecquen = false;
                    this.moveToPlayer(pl);
                } else {
                    pl.nPoint.xoihecquen = true;
                    this.moveAroundPlayer(pl); // Di chuyển quanh người chơi
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void moveAroundPlayer(Player pl) {
        if (pl != null && !pl.isDie()) {
            if (Util.canDoWithTime(lastMoveTime, 1000)) {
                int xOffset = Util.nextInt(-40, 40);
                int yOffset = Util.nextInt(-20, 20);
                this.moveTo(pl.location.x + xOffset, pl.location.y + yOffset);
                lastMoveTime = System.currentTimeMillis();
            }
        }
    }

    @Override
    public void moveTo(int x, int y) {
        byte dir = (byte) (this.location.x - x < 0 ? 1 : -1);
        byte move = (byte) Util.nextInt(30, 40);
        PlayerService.gI().playerMove(this, this.location.x + (dir == 1 ? move : -move), y);
    }

    @Override
    public void die(Player plKill) {
        if (plKill != null && plKill.nPoint != null && plKill.nPoint.diexoihecquen) {
            this.chat("GO GO, ăn 1 cục xương trả 1 phần quà..."); // Chat trước khi chết
            this.changeStatus(BossStatus.DIE);
            plKill.nPoint.diexoihecquen = false;
        }
    }

    @Override
    public void active() {
        if (this.typePk == ConstPlayer.NON_PK) {
            this.changeToTypePK();
        }
        this.attack();

        List<Player> players = this.zone.getPlayers();
        for (int i = players.size() - 1; i >= 0; i--) {
            Player pl = players.get(i);
            if (pl != null && pl.nPoint != null) {
                if (pl.nPoint.diexoihecquen) {
                    this.chat("GO GO, ăn 1 cục xương trả 1 phần quà...");
                    this.changeStatus(BossStatus.DIE);
                    pl.nPoint.diexoihecquen = false;
                    return;
                }
            }
        }

        if (Util.canDoWithTime(st, TIME_CHANGE_MAP)) {
            this.chat("Gâu Gâu Gâu!!!");

            this.changeStatus(BossStatus.LEAVE_MAP);
        }
    }

    @Override
    public void joinMap() {
        this.name = "SÓi Hec Quyn " + Util.nextInt(1, 49);
        this.nPoint.hpMax = 500000;
        this.nPoint.hp = this.nPoint.hpMax;

        this.joinMap2();
        st = System.currentTimeMillis();
    }

    public void joinMap2() {
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
                int zoneId = 0;
                this.zone = this.zone.map.zones.get(zoneId);
                ChangeMapService.gI().changeMap(this, this.zone, -1, -1);
                this.changeStatus(BossStatus.CHAT_S);
            } catch (Exception e) {
                this.changeStatus(BossStatus.REST);
            }
        } else {
            this.changeStatus(BossStatus.RESPAWN);
        }
    }
}