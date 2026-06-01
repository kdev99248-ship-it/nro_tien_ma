package boss.boss_manifest.DeathOrAliveArena;
import boss.BossID;
import boss.BossData;
import boss.BossConfig;


/*
 *
 *
 * 
 */

import static boss.BossType.PHOBAN;
import player.Player;
import services.PlayerService;
import services.Service;
import utils.Util;

public class Dracula extends DeathOrAliveArena {

    public Dracula(BossConfig config, BossData[] data) throws Exception {
        super(config, data);
    }


    private long lastTimeHutMau = System.currentTimeMillis();

    

    @Override
    public void hutMau() {
        try {
            if (Util.canDoWithTime(lastTimeHutMau, 15000) && this.nPoint.hp > this.nPoint.hpMax / 30) {
                long hp = playerAtt.nPoint.hpMax / 10;
                playerAtt.nPoint.subHP(hp);
                this.nPoint.addHp(hp);
                Service.gI().Send_Info_NV(this);
                Service.gI().Send_Info_NV_do_Injure(playerAtt);
                this.chat("Máu ngon quá hehe");
                lastTimeHutMau = System.currentTimeMillis();
            }
        } catch (Exception e) {
        }
    }
}