package event;

/**
 *
 * @author EMTI
 */

import config.EventConfig;
import event.event_manifest.TopUp;
import event.event_manifest.TrungThu;
import event.event_manifest.HungVuong;
import event.event_manifest.Christmas;
import event.event_manifest.Default;
import event.event_manifest.Halloween;
import event.event_manifest.LunarNewYear;
import event.event_manifest.InternationalWomensDay;

public class EventManager {

    private static EventManager instance;

    public static boolean LUNNAR_NEW_YEAR = false;

    public static boolean INTERNATIONAL_WOMANS_DAY = false;

    public static boolean CHRISTMAS = false;

    public static boolean HALLOWEEN = true;

    public static boolean HUNG_VUONG = false;

    public static boolean TRUNG_THU = false;

    public static boolean TOP_UP = false;

    public static EventManager gI() {
        if (instance == null) {
            instance = new EventManager();
        }
        return instance;
    }

    public void init() {
        // Đồng bộ cờ từ EventConfig để boss, NPC, item drop dùng chung 1 config
        syncFromEventConfig();

        new Default().init();
        if (LUNNAR_NEW_YEAR) {
            new LunarNewYear().init();
        }
        if (INTERNATIONAL_WOMANS_DAY) {
            new InternationalWomensDay().init();
        }
        if (HALLOWEEN) {
            new Halloween().init();
        }
        if (CHRISTMAS) {
            new Christmas().init();
        }
        if (HUNG_VUONG) {
            new HungVuong().init();
        }
        if (TRUNG_THU) {
            new TrungThu().init();
        }
        if (TOP_UP) {
            new TopUp().init();
        }
    }

    /**
     * Đồng bộ trạng thái event từ EventConfig sang EventManager
     * Để boss, NPC, item drop đều được quản lý bởi cùng 1 config
     */
    private void syncFromEventConfig() {
        LUNNAR_NEW_YEAR = EventConfig.LUNAR_NEW_YEAR;
        CHRISTMAS = EventConfig.CHRISTMAS_EVENT;
        HALLOWEEN = EventConfig.HALLOWEEN_EVENT;
        HUNG_VUONG = EventConfig.HUNG_VUONG_EVENT;
        TRUNG_THU = EventConfig.TRUNG_THU_EVENT;
    }
}
