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
import utils.Util;

public class VuaQuySaTang extends DeathOrAliveArena {

    public VuaQuySaTang(BossConfig config, BossData[] data) throws Exception {
        super(config, data);
    }


    private long lastTimeBay;

    

    @Override
    public void bayLungTung() {
        if (Util.canDoWithTime(lastTimeBay, 1000)) {
            goToXY(playerAtt.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 80)), Util.nextInt(10) % 2 == 0 ? playerAtt.location.y : playerAtt.location.y - Util.nextInt(0, 200), false);
            lastTimeBay = System.currentTimeMillis();
        }
    }
}