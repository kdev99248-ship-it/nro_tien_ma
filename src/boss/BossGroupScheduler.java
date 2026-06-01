package boss;
import boss.BossManager;

import EMTI.Functions;
import server.Maintenance;

/**
 * Gộp nhiều BossManager vào 1 thread duy nhất để giảm số lượng thread.
 * Mỗi manager vẫn giữ danh sách boss riêng, chỉ chia sẻ thread tick.
 * Dynamic sleep: 150ms khi có boss active, 1s khi tất cả REST.
 */
public class BossGroupScheduler implements Runnable {

    private final BossManager[] managers;
    private final boolean[] useRemoveOnError;
    private final String groupName;

    /**
     * @param groupName        tên nhóm (dùng cho log/debug)
     * @param managers         danh sách BossManager cần gộp
     * @param useRemoveOnError mảng tương ứng: true = remove boss khi exception (phó
     *                         bản), false = chỉ log
     */
    public BossGroupScheduler(String groupName, BossManager[] managers, boolean[] useRemoveOnError) {
        this.groupName = groupName;
        this.managers = managers;
        this.useRemoveOnError = useRemoveOnError;
    }

    @Override
    public void run() {
        while (!Maintenance.isRunning) {
            try {
                long st = System.currentTimeMillis();
                boolean hasActiveBoss = false;
                for (int m = 0; m < managers.length; m++) {
                    boolean active;
                    if (useRemoveOnError[m]) {
                        active = managers[m].updateBossesWithRemove();
                    } else {
                        active = managers[m].updateBosses();
                    }
                    if (active) {
                        hasActiveBoss = true;
                    }
                }
                // Tối ưu: Tất cả boss đang REST → sleep 1s thay vì 150ms
                int delay = hasActiveBoss ? 150 : 1000;
                Functions.sleep(Math.max(delay - (System.currentTimeMillis() - st), 10));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
