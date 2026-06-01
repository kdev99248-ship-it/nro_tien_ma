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
import services.Service;
import utils.Util;

public class NguoiVoHinh extends DeathOrAliveArena {

    public NguoiVoHinh(BossConfig config, BossData[] data) throws Exception {
        super(config, data);
    }


    private long lastTimeTanHinh;
    private boolean goToPlayer;

    

    @Override
    public void tanHinh() {
        if (Util.canDoWithTime(lastTimeTanHinh, 15000)) {
            lastTimeTanHinh = System.currentTimeMillis();
        }

        if (!Util.canDoWithTime(this.lastTimeTanHinh, 5000)) {
            Service.gI().setPos2(this, playerAtt.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 200)),
                    10000);
            goToPlayer = false;
        } else {
            if (!goToPlayer) {
                goToPlayer = true;
                goToPlayer(playerAtt, false);
            }
        }

    }

}