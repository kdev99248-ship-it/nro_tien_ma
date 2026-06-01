package boss.boss_manifest.BossPhu;
import boss.BossID;
import boss.BossData;


import boss.Boss;
import boss.BossStatus;
import static boss.BossType.ANTROM;
import consts.ConstPlayer;
import consts.ConstTaskBadges;
import lombok.Setter;
import map.Zone;
import player.Player;
import services.MapService;
import services.Service;
import services.SkillService;
import services.func.ChangeMapService;
import skill.Skill;
import utils.Util;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import map.ItemMap;
import services.ItemService;
import services.PlayerService;
import services.TaskService;
import task.Badges.BadgesTaskService;

public class O_DO1 extends Boss {
    private static final String[] textOdo = {
            "Hôi quá, tránh xa ta ra", "Biến đi", "Trời ơi đồ ở dơ", "Thúi quá", "Mùi gì hôi quá"
    };

    @Setter
    private Player player;

    private long lastTimeOdo;
    private long lastTimeJoinMap;
    private long lastTimeMove;
    private long lastTimeLaugh;

    private static final long LAUGH_INTERVAL = 3000; // 3 giây
    private static final long MOVE_INTERVAL = 2000; // 2 giây

    public O_DO1() throws Exception {
        super(ANTROM, BossID.O_DO1, new BossData(
                "Ở DƠ " + Util.nextInt(1, 100),
                ConstPlayer.TRAI_DAT,
                new short[] { 400, 401, 402, -1, -1, -1 },
                1,
                new long[] { 500000L },
                new int[] { 5, 7, 0, 14 },
                new int[][] {
                        { Skill.THAI_DUONG_HA_SAN, 1, 100000 },
                        { Skill.DICH_CHUYEN_TUC_THOI, 3, 50000 }
                },
                new String[] { "|-1|Mùi gì hôi hôi!! :))" },
                new String[] { "|-1|Mùi cứt!!!", "|-2|Hôi quá tránh ra đi....", "|-2|Cút ngay không là ăn đòn" },
                new String[] { "|-1|Tắm đi em!", "|-2|Chừa thói ở bẩn nha em" },
                600));
    }

    @Override
    public Zone getMapJoin() {
        int mapId = this.data[this.currentLevel].getMapJoin()[Util.nextInt(0,
                this.data[this.currentLevel].getMapJoin().length - 1)];
        return MapService.gI().getMapById(mapId).zones.get(5);
    }

    @Override
    public Player getPlayerAttack() {
        this.nPoint.tlHpGiamODo = 10;
        return super.getPlayerAttack();
    }

    @Override
    public long injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (this.nPoint.hp <= 0) {
            return 0; // Nếu boss đã chết, không nhận sát thương nữa
        }

        // Giới hạn sát thương tối đa boss có thể nhận trong một lần tấn công là 16,666
        long actualDamage = Math.min(damage, 16666);

        // Giảm HP boss
        this.nPoint.subHP(actualDamage);

        // Nếu boss còn kỹ năng thì phản công
        if (this.playerSkill != null && !this.playerSkill.skills.isEmpty()) {
            this.playerSkill.skillSelect = this.playerSkill.skills
                    .get(Util.nextInt(0, this.playerSkill.skills.size() - 1));
            SkillService.gI().useSkill(this, plAtt, null, -1, null);
        }

        // Nếu boss hết HP, chuyển sang trạng thái chết
        if (this.nPoint.hp <= 0) {
            this.die(plAtt);
        }

        return actualDamage;
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
                    if (Util.isTrue(1, 2)) {
                        this.moveToPlayer(pl);
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
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
        this.reward(plKill);
        this.changeStatus(BossStatus.DIE);
    }

    @Override
    public void reward(Player plKill) {
        Random random = new Random();
        int id;
        int rand = random.nextInt(100);
        if (rand >= 0 && rand < 20) {
            id = Util.nextInt(16, 20);
        } else if (rand >= 20 && rand < 50) {
            id = Util.nextInt(342, 345);
        } else if (rand >= 50 && rand < 80) {
            id = Util.nextInt(441, 447);
        } else if (rand >= 80 && rand < 95) {
            id = 1635;
        } else {
            id = 15;
        }
        BadgesTaskService.updateCountBagesTask(plKill, ConstTaskBadges.O_DO, 1);
        ItemMap it = new ItemMap(this.zone, id, 1, this.location.x + Util.nextInt(-15, 15),
                this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id);
        Service.gI().dropItemMap(this.zone, it);
    }

    private long st;

    @Override
    public void active() {
        if (this.typePk == ConstPlayer.NON_PK) {
            this.changeToTypePK();
        }
        this.attack();

        // Boss nói "Khà khà.." mỗi 3 giây
        if (Util.canDoWithTime(lastTimeLaugh, LAUGH_INTERVAL)) {
            Service.gI().chat(this, "Khà khà..");
            lastTimeLaugh = System.currentTimeMillis();
        }

        // Boss đi lại gần và xung quanh người chơi mỗi 2 giây
        if (Util.canDoWithTime(lastTimeMove, MOVE_INTERVAL)) {
            List<Player> playersInZone = this.zone.getPlayers();
            if (!playersInZone.isEmpty()) {
                Player target = playersInZone.get(Util.nextInt(0, playersInZone.size() - 1));
                int offsetX = Util.nextInt(-20, 20);
                int offsetY = Util.nextInt(-20, 20);
                this.moveTo(target.location.x + offsetX, target.location.y + offsetY);
            }
            lastTimeMove = System.currentTimeMillis();
        }

        // Người chơi phản ứng khi boss đến gần
        List<Player> playerReact = this.zone.getPlayers();
        for (int i = playerReact.size() - 1; i >= 0; i--) {
            Player pl = playerReact.get(i);
            if (pl != null && !pl.isDie() && Util.getDistance(this, pl) <= 20) {
                Service.gI().chat(pl, "Hôi quá, tránh ra đi");
            }
        }
    }

    @Override
    public void joinMap() {
        this.name = "Ở DƠ " + Util.nextInt(1, 49);
        this.nPoint.hpMax = 500000;
        this.nPoint.hp = this.nPoint.hpMax;
        this.nPoint.dameg = this.nPoint.hpMax / 10;
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
                this.zone = this.zone.map.zones.get(0);
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