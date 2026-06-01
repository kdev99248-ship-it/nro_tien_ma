package event.event_manifest;
import boss.BossID;


/*
 *
 *
 * @author EMTI
 */

import event.Event;

public class TrungThu extends Event {

    @Override
    public void boss() {
        createBoss(BossID.KHIDOT, 10);
        createBoss(BossID.NGUYETTHAN, 10);
    }
}