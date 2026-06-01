package event.event_manifest;
import boss.BossID;


/**
 *
 * @author EMTI
 */

import event.Event;

public class Halloween extends Event {

    @Override
    public void boss() {
        createBoss(BossID.BIMA, 10);
        createBoss(BossID.MATROI, 10);
        createBoss(BossID.DOI, 10);
    }
}