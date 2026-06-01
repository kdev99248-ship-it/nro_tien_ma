package boss.boss_manifest.Android;

/*
 *
 *
 * 
 */

import boss.Boss;
import boss.BossID;
import boss.BossesData;
import map.ItemMap;
import player.Player;
import services.Service;
import services.TaskService;
import utils.Util;

public class Android13 extends Boss {

    public Android13() throws Exception {
        super(BossID.ANDROID_13, BossesData.ANDROID_13);
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
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
    }

    @Override
    public void doneChatS() {
        if (this.parentBoss == null) {
            return;
        }
        if (this.parentBoss.bossAppearTogether == null
                || this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel] == null) {
            return;
        }
        for (Boss boss : this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel]) {
            if (boss.id == BossID.ANDROID_15 && !boss.isDie()) {
                boss.changeToTypePK();
                break;
            }
        }
        this.parentBoss.changeToTypePK();
    }

    @Override
    public synchronized long injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (damage >= this.nPoint.hp) {
            boolean flag = true;
            if (this.parentBoss != null) {
                if (this.parentBoss.bossAppearTogether != null && this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel] != null) {
                    for (Boss boss : this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel]) {
                        if (boss.id == BossID.ANDROID_15 && !boss.isDie()) {
                            flag = false;
                            break;
                        }
                    }
                }
                if (flag && !this.parentBoss.isDie()) {
                    flag = false;
                }
            }
            if (!flag) {
                return 0;
            }
        }
        return super.injured(plAtt, damage, piercing, isMobAttack);
    }
}
