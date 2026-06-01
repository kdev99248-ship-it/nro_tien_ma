package boss.boss_manifest.Earth;

import boss.Boss;
import boss.BossID;
import boss.BossStatus;
import boss.BossesData;
import item.Item;
import java.util.List;
import map.ItemMap;
import player.Player;
import services.ItemService;
import services.Service;
import utils.Util;

public class ZANGYA extends Boss {

    private long st;

    public ZANGYA() throws Exception {
        super(BossID.ZANGYA, false, true, BossesData.ZANGYA);
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
        plKill.pointbossday+=1;
//        Service.gI().dropItemMap(this.zone, new ItemMap(zone, 2055, Util.nextInt(1, 10), this.location.x + Util.nextInt(-50, 50), this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id));
        
        
                 for (int i = 1; i < Util.nextInt(1, 2) ; i++) {
            Service.gI().dropItemMap(this.zone, new ItemMap(zone, 457, 1, this.location.x - i * 10, this.zone.map.yPhysicInTop(this.location.x,
                    this.location.y - 24), plKill.id));
        }
        short itTemp = 425;
        ItemMap it = new ItemMap(zone, itTemp, 1, this.location.x + Util.nextInt(-50, 50), this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id);
        List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop(itTemp);
        if (!ops.isEmpty()) {
            it.options = ops;
        }
        Service.gI().dropItemMap(this.zone, it);
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
            if ((boss.id == BossID.BUJIN || boss.id == BossID.KOGU || boss.id == BossID.BIDO) && !boss.isDie()) {
                return;
            }
        }
        this.parentBoss.changeStatus(BossStatus.ACTIVE);
    }

}
