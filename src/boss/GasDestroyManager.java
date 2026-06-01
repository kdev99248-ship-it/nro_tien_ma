package boss;
import boss.BossManager;

import EMTI.Functions;
import server.Maintenance;

public class GasDestroyManager extends BossManager {

    private static GasDestroyManager instance;

    public static GasDestroyManager gI() {
        if (instance == null) {
            instance = new GasDestroyManager();
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
