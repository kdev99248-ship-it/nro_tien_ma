package boss;
import boss.BossManager;

/*
 *
 *
 * 
 */
import EMTI.Functions;
import server.Maintenance;

public class TreasureUnderSeaManager extends BossManager {

    private static TreasureUnderSeaManager instance;

    public static TreasureUnderSeaManager gI() {
        if (instance == null) {
            instance = new TreasureUnderSeaManager();
        }
        return instance;
    }

    @Override
    public void run() {
        while (!Maintenance.isRunning) {
            try {
                long st = System.currentTimeMillis();
                boolean hasActiveBoss = updateBossesWithRemove();
                int delay = hasActiveBoss ? 150 : 1000;
                Functions.sleep(Math.max(delay - (System.currentTimeMillis() - st), 10));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
