package boss.boss_manifest.The23rdMartialArtCongress;

import consts.ConstRatio;
import boss.Boss;
import boss.BossData;
import boss.BossConfig;
import boss.OtherBossManager;
import boss.BossStatus;
import boss.BossType;
import player.Player;
import services.PlayerService;
import services.SkillService;
import services.func.ChangeMapService;
import utils.SkillUtil;
import utils.Util;

public class DHVT23Boss extends Boss {

    protected Player playerAtt;
    protected long timeJoinMap;

    public DHVT23Boss(BossConfig config, BossData[] data) throws Exception {
        super(config, data);
        this.bossStatus = BossStatus.RESPAWN;
    }

    public void setPlayerAtt(Player player) {
        this.playerAtt = player;
    }

    @Override
    public void afk() {
        if (playerAtt == null || playerAtt.location == null || playerAtt.zone == null || this.zone == null
                || !this.zone.equals(playerAtt.zone)) {
            this.leaveMap();
        }
    }

    protected void goToXY(int x, int y, boolean isTeleport) {
        if (!isTeleport) {
            byte dir = (byte) (this.location.x - x < 0 ? 1 : -1);
            byte move = (byte) Util.nextInt(50, 100);
            PlayerService.gI().playerMove(this, this.location.x + (dir == 1 ? move : -move), y);
        } else {
            ChangeMapService.gI().changeMapYardrat(this, this.zone, x, y);
        }
    }

    @Override
    public void attack() {
        try {
            if (playerAtt != null && playerAtt.location != null && playerAtt.zone != null && this.zone != null
                    && this.zone.equals(playerAtt.zone)) {
                if (this.isDie() || playerAtt.lostByDeath) {
                    return;
                }
                this.playerSkill.skillSelect = this.playerSkill.skills
                        .get(Util.nextInt(0, this.playerSkill.skills.size() - 1));
                if (Util.getDistance(this, playerAtt) <= this.getRangeCanAttackWithSkillSelect()) {
                    if (Util.isTrue(15, ConstRatio.PER100) && SkillUtil.isUseSkillChuong(this)) {
                        goToXY(playerAtt.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 80)),
                                Util.nextInt(10) % 2 == 0 ? playerAtt.location.y
                                        : playerAtt.location.y - Util.nextInt(0, 50),
                                false);
                    }
                    SkillService.gI().useSkill(this, playerAtt, null, -1, null);
                } else {
                    goToPlayer(playerAtt, false);
                }
            } else {
                this.leaveMap();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void goToPlayer(Player pl, boolean isTeleport) {
        goToXY(pl.location.x, pl.location.y, isTeleport);
    }

    @Override
    public void joinMap() {
        if (playerAtt != null && playerAtt.zone != null) {
            this.zone = playerAtt.zone;
            ChangeMapService.gI().changeMap(this, this.zone, 435, 264);
        }
    }

    protected void immortalMp() {
        this.nPoint.mp = this.nPoint.mpg;
    }

    @Override
    public void update() {
        try {
            super.updateInfo();
            if ((this.effectSkill != null && this.effectSkill.isHaveEffectSkill())
                    || (this.newSkill != null && this.newSkill.isStartSkillSpecial)) {
                return;
            }
            switch (this.bossStatus) {
                case RESPAWN:
                    this.respawn();
                    this.changeStatus(BossStatus.JOIN_MAP);
                case JOIN_MAP:
                    joinMap();
                    if (this.zone != null) {
                        changeStatus(BossStatus.AFK);
                        timeJoinMap = System.currentTimeMillis();
                        this.immortalMp();
                        this.typePk = 3;
                    }
                    break;
                case AFK:
                    afk();
                    break;
                case ACTIVE:
                    if (this.playerSkill.prepareTuSat || this.playerSkill.prepareLaze || this.playerSkill.prepareQCKK) {
                        break;
                    } else {
                        this.attack();
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void die(Player plKill) {
        this.changeStatus(BossStatus.DIE);
    }

    @Override
    public void leaveMap() {
        ChangeMapService.gI().exitMap(this);
        this.lastZone = null;
        this.lastTimeRest = System.currentTimeMillis();
        this.changeStatus(BossStatus.REST);
        this.dispose();
    }
}
