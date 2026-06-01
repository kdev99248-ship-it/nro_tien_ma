package event.event_manifest;
import boss.BossID;


/*
 *
 *
 * @author EMTI
 */

import event.Event;

public class Christmas extends Event {

    @Override
    public void boss() {
        createBoss(BossID.ONG_GIA_NOEL, 30);
    }
}