package boss;
import boss.BossManager;

/*
 *
 *
 *
 */

public class TrungThuEventManager extends BossManager {

    private static TrungThuEventManager instance;

    public static TrungThuEventManager gI() {
        if (instance == null) {
            instance = new TrungThuEventManager();
        }
        return instance;
    }

}
