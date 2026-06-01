package config;

/**
 * Quản lý trạng thái bật/tắt của các sự kiện.
 * Được load từ file config.properties
 */
public class EventConfig {

    private static EventConfig instance;

    // Sự kiện Tết (gồm cả exchange, use item và boss Tết)
    public static boolean LUNAR_NEW_YEAR = true;

    // Sự kiện Giáng Sinh
    public static boolean CHRISTMAS_EVENT = false;

    // Sự kiện Halloween
    public static boolean HALLOWEEN_EVENT = false;

    // Sự kiện Trung Thu
    public static boolean TRUNG_THU_EVENT = false;

    // Sự kiện Hùng Vương
    public static boolean HUNG_VUONG_EVENT = false;

    // Sự kiện đua top Sức Mạnh
    public static boolean TOP_SM_EVENT = true;

    // Sự kiện đua top Nạp
    public static boolean TOP_NAP_EVENT = true;

    public static EventConfig gI() {
        if (instance == null) {
            instance = new EventConfig();
        }
        return instance;
    }

    /**
     * Kiểm tra sự kiện có được bật không
     * 
     * @param eventKey Tên mã sự kiện (vd: "lunar_new_year", "christmas")
     * @return true nếu sự kiện được bật
     */
    public static boolean isEventEnabled(String eventKey) {
        return switch (eventKey.toLowerCase()) {
            case "lunar_new_year" -> LUNAR_NEW_YEAR;
            case "christmas" -> CHRISTMAS_EVENT;
            case "halloween" -> HALLOWEEN_EVENT;
            case "trung_thu" -> TRUNG_THU_EVENT;
            case "hung_vuong" -> HUNG_VUONG_EVENT;
            case "top_sm" -> TOP_SM_EVENT;
            case "top_nap" -> TOP_NAP_EVENT;
            default -> false;
        };
    }

    /**
     * Load config từ properties value (được gọi từ Manager.loadProperties)
     */
    public static void loadFromValue(String key, String value) {
        boolean enabled = "1".equals(value) || "true".equalsIgnoreCase(value);
        switch (key.toLowerCase()) {
            case "event.lunar_new_year" -> LUNAR_NEW_YEAR = enabled;
            case "event.christmas" -> CHRISTMAS_EVENT = enabled;
            case "event.halloween" -> HALLOWEEN_EVENT = enabled;
            case "event.trung_thu" -> TRUNG_THU_EVENT = enabled;
            case "event.hung_vuong" -> HUNG_VUONG_EVENT = enabled;
            case "event.top_sm" -> TOP_SM_EVENT = enabled;
            case "event.top_nap" -> TOP_NAP_EVENT = enabled;
        }
    }

    /**
     * In ra console trạng thái các sự kiện
     */
    public static void printStatus() {
        System.out.println("=== EVENT CONFIG ===");
        System.out.println("Lunar New Year Event: " + (LUNAR_NEW_YEAR ? "ON" : "OFF"));
        System.out.println("Christmas Event: " + (CHRISTMAS_EVENT ? "ON" : "OFF"));
        System.out.println("Halloween Event: " + (HALLOWEEN_EVENT ? "ON" : "OFF"));
        System.out.println("Trung Thu Event: " + (TRUNG_THU_EVENT ? "ON" : "OFF"));
        System.out.println("Hung Vuong Event: " + (HUNG_VUONG_EVENT ? "ON" : "OFF"));
        System.out.println("Top SM Event: " + (TOP_SM_EVENT ? "ON" : "OFF"));
        System.out.println("Top Nap Event: " + (TOP_NAP_EVENT ? "ON" : "OFF"));
        System.out.println("====================");
    }
}
