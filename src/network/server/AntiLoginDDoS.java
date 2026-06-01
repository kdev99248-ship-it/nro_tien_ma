package network.server;

import utils.Logger;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Chống DDoS tại lớp kết nối đầu vào.
 *
 * Cơ chế:
 * 1. Rate Limit: Đếm số kết nối từng IP trong 1 giây.
 *    Nếu vượt ngưỡng MAX_CONN_PER_SECOND → tạm thời ban IP.
 * 2. Blacklist: IP bị ban sẽ bị block hoàn toàn trong BAN_DURATION_MS.
 * 3. Clears tự động sau khi hết thời gian ban.
 *
 * Cấu hình (có thể đặt vào config.properties sau):
 * - MAX_CONN_PER_SECOND: Số kết nối tối đa từ 1 IP trong 1 giây.
 * - BAN_DURATION_MS: Thời gian ban (ms).
 * - MAX_PACKET_PER_SECOND: Số packet tối đa từ 1 session mỗi giây.
 */
public class AntiLoginDDoS {

    // === Cấu hình ===
    public static int MAX_CONN_PER_SECOND = 5;     // > 5 conn/s → tạm ban
    public static long BAN_DURATION_MS   = 30_000; // Ban 30 giây
    public static int MAX_PACKET_PER_SECOND = 100;  // > 100 packet/s → kick

    // === State ===
    // IP → số kết nối trong giây hiện tại
    private static final Map<String, AtomicInteger> connRate = new ConcurrentHashMap<>();
    // IP → thời điểm hết ban
    private static final Map<String, Long> banList = new ConcurrentHashMap<>();

    private static final ScheduledExecutorService cleaner =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "AntiDDoS-Cleaner");
                t.setDaemon(true);
                return t;
            });

    static {
        // Reset rate counter mỗi giây
        cleaner.scheduleAtFixedRate(() -> {
            connRate.clear();
            // Dọn các IP hết thời gian ban
            long now = System.currentTimeMillis();
            banList.entrySet().removeIf(e -> {
                boolean expired = now >= e.getValue();
                if (expired) {
                    Logger.success("[AntiDDoS] IP được gỡ ban: " + e.getKey() + "\n");
                }
                return expired;
            });
        }, 1, 1, TimeUnit.SECONDS);
    }

    private AntiLoginDDoS() {}

    /**
     * Gọi khi có kết nối mới đến từ IP.
     * @return true nếu IP được phép kết nối, false nếu bị block.
     */
    public static boolean checkNewConnection(String ip) {
        // Kiểm tra blacklist trước
        if (isBanned(ip)) {
            return false;
        }

        // Tăng counter và kiểm tra rate
        AtomicInteger counter = connRate.computeIfAbsent(ip, k -> new AtomicInteger(0));
        int count = counter.incrementAndGet();

        if (count > MAX_CONN_PER_SECOND) {
            ban(ip, "Kết nối quá nhanh (" + count + "/s)");
            return false;
        }
        return true;
    }

    /**
     * Kiểm tra IP có đang bị ban không.
     */
    public static boolean isBanned(String ip) {
        Long banUntil = banList.get(ip);
        if (banUntil == null) return false;
        if (System.currentTimeMillis() >= banUntil) {
            banList.remove(ip);
            return false;
        }
        return true;
    }

    /**
     * Ban thủ công một IP (ví dụ từ lệnh admin).
     */
    public static void ban(String ip, String reason) {
        banList.put(ip, System.currentTimeMillis() + BAN_DURATION_MS);
        Logger.warning("[AntiDDoS] Banned IP: " + ip + " | Lý do: " + reason + "\n");
    }

    /**
     * Gỡ ban thủ công.
     */
    public static void unban(String ip) {
        banList.remove(ip);
        Logger.success("[AntiDDoS] Gỡ ban IP: " + ip + "\n");
    }

    /**
     * Lấy danh sách IP đang bị ban.
     */
    public static Set<String> getBannedIPs() {
        return banList.keySet();
    }

    /**
     * Lấy số kết nối hiện tại trong giây của một IP.
     */
    public static int getConnRate(String ip) {
        AtomicInteger c = connRate.get(ip);
        return c == null ? 0 : c.get();
    }
}
