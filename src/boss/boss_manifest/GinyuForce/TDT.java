package boss.boss_manifest.GinyuForce;

/*
 *
 *
 * @author EMTI
 */

import boss.Boss;
import boss.BossID;
import boss.BossStatus;
import boss.BossesData;
import map.ItemMap;
import player.Player;
import java.util.List;

import services.EffectSkillService;
import services.Service;
import utils.Util;

public class TDT extends Boss {

    private long st;

    private long lastBodyChangeTime;

    public TDT() throws Exception {
        super(BossID.TIEU_DOI_TRUONG, false, true, BossesData.TIEU_DOI_TRUONG);
    }

    private void bodyChangePlayerInMap() {
        if (this.zone != null) {
            List<Player> players = this.zone.getPlayers();
            for (int i = players.size() - 1; i >= 0; i--) {
                Player pl = players.get(i);
                if (pl != null && pl.isPl() && Util.isTrue(5, 10) && pl.effectSkill != null
                        && !pl.effectSkill.isBodyChangeTechnique) {
                    EffectSkillService.gI().setIsBodyChangeTechnique(pl);
                }
            }
        }
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
        for (int i = 0; i < Util.nextInt(1, 2); i++) {

            ItemMap it = new ItemMap(this.zone, 457, (int) 1, this.location.x + i * 10,
                    this.zone.map.yPhysicInTop(this.location.x,
                            this.location.y - 24),
                    plKill.id);

            Service.gI().dropItemMap(this.zone, it);
        }
        super.reward(plKill);
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
    public void attack() {
        if (Util.canDoWithTime(lastBodyChangeTime, 10000)) {
            bodyChangePlayerInMap();
            this.chat("Úm ba la xì bùa");
            this.lastBodyChangeTime = System.currentTimeMillis();
        }
        super.attack();
    }

    @Override
    public void joinMap() {
        super.joinMap();
        st = System.currentTimeMillis();
    }

    @Override
    public void doneChatS() {
        this.changeStatus(BossStatus.AFK);
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
}
