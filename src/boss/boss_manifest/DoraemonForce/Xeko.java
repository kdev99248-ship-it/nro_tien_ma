package boss.boss_manifest.DoraemonForce;

/*
 *
 *
 * @author EMTI
 */
import boss.Boss;
import boss.BossID;
import boss.BossStatus;
import boss.BossesData;
import player.Player;
import utils.Util;

public class Xeko extends Boss {

    private long st;

    public Xeko() throws Exception {
        super(BossID.XEKO_DRM, false, true, BossesData.XEKO_DRM);
    }

    @Override
    public void moveTo(int x, int y) {
        if (this.currentLevel == 1) {
            return;
        }
        super.moveTo(x, y);
    }

    @Override
    public void reward(Player plKill) {
        plKill.pointbossday += 3;
        if (this.currentLevel == 1) {
            return;
        }

    }

    @Override
    protected void notifyJoinMap() {
        if (this.currentLevel == 1) {
            return;
        }
        super.notifyJoinMap();
    }

    @Override
    public void joinMap() {
        super.joinMap();
        st = System.currentTimeMillis();
    }

    @Override
    public void autoLeaveMap() {
        if (Util.canDoWithTime(st, 900000)) {
            this.leaveMapNew();
        }
        if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
            st = System.currentTimeMillis();
        }
    }

    @Override
    public void doneChatE() {
        if (this.parentBoss == null || this.parentBoss.bossAppearTogether == null
                || this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel] == null) {
            return;
        }
        for (Boss boss : this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel]) {
            if (boss.id == BossID.CHAIEN_DRM && !boss.isDie()) {
                boss.changeStatus(BossStatus.ACTIVE);
                break;
            }
        }
    }
}
