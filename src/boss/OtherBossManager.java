package boss;
import boss.BossManager;

import EMTI.Functions;
import server.Maintenance;

public class OtherBossManager extends BossManager {

    private static OtherBossManager instance;

    public static OtherBossManager gI() {
        if (instance == null) {
            instance = new OtherBossManager();
        }
        return instance;
    }

    @Override
    public void run() {
        while (!Maintenance.isRunning) {
            try {
                long st = System.currentTimeMillis();
                boolean hasActiveBoss = updateBossesWithRemove();
                // Tối ưu: Boss đang REST → sleep 1s thay vì 150ms
                int delay = hasActiveBoss ? 150 : 1000;
                Functions.sleep(Math.max(delay - (System.currentTimeMillis() - st), 10));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
