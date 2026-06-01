package utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Quản lý tập trung toàn bộ Thread Pool của server.
 * Thay thế việc tạo new Thread() thủ công, giảm RAM và tránh thread explosion.
 */
public class ServerPool {

    // Pool cho I/O của từng client (Sender + Collector).
    // Mỗi client dùng 2 task, với 1000 người cần ~2000 slot.
    // CachedThreadPool: tự scale, auto reuse thread.
    public static final ExecutorService CLIENT_IO_POOL = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r);
        t.setName("ClientIO-" + t.getId());
        t.setDaemon(true);
        return t;
    });

    // Pool cho update các Map (thay thế mỗi map một thread).
    // Map.run() là vòng while(true) với adaptive sleep → dùng FixedThreadPool.
    // 16 thread: map trống sleep 5s, map có người sleep 1s → đủ cho hàng trăm map.
    public static final ExecutorService MAP_UPDATE_POOL = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r);
        t.setName("MapUpdate-" + t.getId());
        t.setDaemon(true);
        return t;
    });

    // Pool cho các task nền nhỏ (Boss, Event, Service).
    public static final ScheduledExecutorService SERVICE_POOL = Executors.newScheduledThreadPool(4, r -> {
        Thread t = new Thread(r);
        t.setName("Service-" + t.getId());
        t.setDaemon(true);
        return t;
    });

    /**
     * Tắt tất cả pool khi server shutdown.
     * Chờ tối đa 10 giây để hoàn thành task đang chạy.
     */
    public static void shutdown() {
        shutdownPool(CLIENT_IO_POOL, "ClientIO");
        shutdownPool(MAP_UPDATE_POOL, "MapUpdate");
        shutdownPool(SERVICE_POOL, "Service");
    }

    private static void shutdownPool(ExecutorService pool, String name) {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                pool.shutdownNow();
                Logger.warning("[ServerPool] " + name + " pool forced shutdown.\n");
            } else {
                Logger.success("[ServerPool] " + name + " pool shutdown cleanly.\n");
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
