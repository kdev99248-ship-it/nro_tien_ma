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
import services.func.ChangeMapService;
import skill.Skill;
import utils.Util;
import java.util.List;


public class XINBATO1 extends Boss {
    private static final String[] textOdo = {
        "Hôi quá, tránh xa ta ra", "Biến đi", "Trời ơi đồ ở dơ", "Thúi quá", "Mùi gì hôi quá"
    };

    @Setter
    private Player player;
    
    private long lastMoveTime;
    private long lastTimeAttack;
    private long startTime;

    private static final long TIME_CHANGE_MAP = 200000; // 200 giây = 3 phút 20 giây
    private static final long MOVE_INTERVAL = 3000; // Di chuyển mỗi 3 giây

    public XINBATO1() throws Exception {
        super(ANTROM, BossID.XINBATO1, new BossData(
                "XIN BA TO " + Util.nextInt(1, 100),
                ConstPlayer.TRAI_DAT,
                new short[]{359, 360, 361, -1, -1, -1},
                1,
                new long[]{500000L},
                new int[]{5, 7, 0, 14, 1, 2, 3, 4, 5, 8, 9, 10, 11},
                new int[][]{
                    {Skill.TAI_TAO_NANG_LUONG, 3, 500000}
                },
                new String[]{"|-1|Đường ống nước làng ta bị vỡ..."},
                new String[]{"|-1|Hãy Mang 99 Bình nước đến đây giúp ta.."},
                new String[]{"|-1|Please!!!!"},
                600)
        );
    }

    @Override
    public Zone getMapJoin() {
        int mapId = this.data[this.currentLevel].getMapJoin()[Util.nextInt(0, this.data[this.currentLevel].getMapJoin().length - 1)];
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
                    pl.nPoint.isxinbato = false;
                    this.moveToPlayer(pl);
                } else {
                    pl.nPoint.isxinbato = true;
                    this.moveAroundPlayer(pl); // Di chuyển quanh người chơi
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Lấy người chơi gần nhất trong khu vực của boss.
     */
    private Player getNearestPlayer() {
        Player nearestPlayer = null;
        double minDistance = Double.MAX_VALUE;

        List<Player> players = this.zone.getPlayers();
        for (int i = players.size() - 1; i >= 0; i--) {
            Player pl = players.get(i);
            if (pl != null) {
                double distance = Util.getDistance(this, pl);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestPlayer = pl;
                }
            }
        }
        return nearestPlayer;
    }

    /**
     * Hàm giúp boss di chuyển xung quanh người chơi theo các hướng ngẫu nhiên.
     */
    private void moveAroundPlayer(Player pl) {
        if (pl != null && !pl.isDie()) {
            if (Util.canDoWithTime(lastMoveTime, MOVE_INTERVAL)) {
                int xOffset = Util.nextInt(-30, 30); // Di chuyển trong phạm vi nhỏ quanh người chơi
                int yOffset = Util.nextInt(-20, 20);
                
                int newX = pl.location.x + xOffset;
                int newY = pl.location.y + yOffset;

                this.moveTo(newX, newY);
                lastMoveTime = System.currentTimeMillis();
               
            }
        }
    }

    @Override
    public void die(Player plKill) {
        if (plKill != null) {
            this.chat("Cảm ơn các hạ đã giúp, ta sẽ tặng các hạ món quà!");
        }
        this.changeStatus(BossStatus.DIE);
    }

    @Override
    public void active() {
        if (this.typePk == ConstPlayer.NON_PK) {
            this.changeToTypePK();
        }

        Player targetPlayer = getNearestPlayer();
        if (targetPlayer != null) {
            attack();
        }

        List<Player> playerList = this.zone.getPlayers();
        for (int i = playerList.size() - 1; i >= 0; i--) {
            Player pl = playerList.get(i);
            if (pl != null && pl.nPoint != null && pl.nPoint.diexinbato) {
                this.die(pl);
                pl.nPoint.diexinbato = false;
                return;
            }
        }

        if (Util.canDoWithTime(startTime, TIME_CHANGE_MAP)) {
            this.chat("Xìa Xịa ta có món quà tặng ngươi rồi biến mất");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.changeStatus(BossStatus.LEAVE_MAP);
        }
    }

    @Override
    public void joinMap() {
        this.name = "XIN BA TO " + Util.nextInt(1, 49);
        this.nPoint.hpMax = 50000;
        this.nPoint.hp = this.nPoint.hpMax;

        this.zone = getMapJoin();
        if (this.zone != null) {
            ChangeMapService.gI().changeMap(this, this.zone, -1, -1);
            this.changeStatus(BossStatus.CHAT_S);
        } else {
            this.changeStatus(BossStatus.RESPAWN);
        }

        startTime = System.currentTimeMillis();
    }
}