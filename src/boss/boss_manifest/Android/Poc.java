package boss.boss_manifest.Android;

/*
 *
 *
 * 
 */

import boss.Boss;
import boss.BossID;
import boss.BossStatus;
import boss.BossesData;
import map.ItemMap;
import player.Player;
import services.Service;
import services.TaskService;
import utils.Util;

public class Poc extends Boss {

    public Poc() throws Exception {
        super(BossID.POC, BossesData.POC);
    }

    @Override
    public void reward(Player plKill) {
        plKill.pointbossday+=7;
         for (int i = 0; i < Util.nextInt(1,3); i++) {
                
                ItemMap it = new ItemMap(this.zone, 457, (int) 1, this.location.x + i * 10, this.zone.map.yPhysicInTop(this.location.x,
                        this.location.y - 24), plKill.id);
                
                Service.gI().dropItemMap(this.zone, it);
            }
        int[] itemRan = new int[]{380, 381, 382, 383, 384, 385};
        int itemId = itemRan[2];
        if (Util.isTrue(15, 100)) {
            ItemMap it = new ItemMap(this.zone, itemId, 1, this.location.x, this.zone.map.yPhysicInTop(this.location.x,
                    this.location.y - 24), plKill.id);
            Service.gI().dropItemMap(this.zone, it);
        }
        if (Util.isTrue(5, 50)) {
            for (int i = 0; i < Util.nextInt(25, 50); i++) {
                ItemMap it = new ItemMap(this.zone, 1229, 1, this.location.x + Util.nextInt(-15, 15), this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id);
                Service.gI().dropItemMap(this.zone, it);
            }
        }
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
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
    public void joinMap() {
        super.joinMap(); //To change body of generated methods, choose Tools | Templates.
        st = System.currentTimeMillis();
    }
    private long st;

//    @Override
//    public void wakeupAnotherBossWhenDisappear() {
//        if (this.parentBoss != null && !this.parentBoss.isDie()) {
//            this.parentBoss.changeToTypePK();
//        }
//    }
    @Override
    public void doneChatE() {
        if (this.parentBoss == null || this.parentBoss.bossAppearTogether == null
                || this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel] == null) {
            return;
        }
        for (Boss boss : this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel]) {
            if (boss.id == BossID.PIC && !boss.isDie()) {
                boss.changeStatus(BossStatus.ACTIVE);
                break;
            }
        }
    }

}
