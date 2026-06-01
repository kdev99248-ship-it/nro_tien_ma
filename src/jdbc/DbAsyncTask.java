package jdbc;

import utils.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Hàng đợi lưu dữ liệu DB bất đồng bộ.
 *
 * Thay vì mỗi lần gọi updatePlayer() đều block thread hiện tại để
 * query DB, class này đưa task vào hàng đợi và xử lý tuần tự bởi
 * một pool riêng → tránh N+1 block game thread.
 *
 * QUAN TRỌNG: Khi logout hoặc bảo trì, phải gọi forceFlush() hoặc
 * đợi task trong queue hoàn tất để tránh mất dữ liệu.
 */
public class DbAsyncTask {

    private static final int QUEUE_CAPACITY = 20_000;
    private static final int WORKER_THREADS = 4;
    private static final int WARN_BACKLOG_SIZE = 5_000;

    private static final DbAsyncTask INSTANCE = new DbAsyncTask();

    private final BlockingQueue<Runnable> queue;
    private final ExecutorService workers;
    private final AtomicInteger enqueuedCount = new AtomicInteger(0);

    private DbAsyncTask() {
        this.queue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
        this.workers = Executors.newFixedThreadPool(WORKER_THREADS, r -> {
            Thread t = new Thread(r);
            t.setName("DbAsync-" + t.getId());
            t.setDaemon(true);
            return t;
        });

        // Khởi động các worker thread
        for (int i = 0; i < WORKER_THREADS; i++) {
            workers.submit(this::workerLoop);
        }
        Logger.success("[DbAsyncTask] Khởi động " + WORKER_THREADS + " DB worker threads.\n");
    }

    public static DbAsyncTask gI() {
        return INSTANCE;
    }

    /**
     * Đẩy một task DB vào hàng đợi (non-blocking).
     * Nếu queue đầy → log cảnh báo và bỏ qua để tránh block.
     */
    public void submit(Runnable task) {
        int backlog = queue.size();
        if (backlog > WARN_BACKLOG_SIZE) {
            Logger.warning("[DbAsyncTask] Backlog cao: " + backlog + " tasks. Server có thể đang quá tải!\n");
        }
        boolean offered = queue.offer(task);
        if (!offered) {
            Logger.warning("[DbAsyncTask] Queue đầy! Task bị bỏ qua. Cần tăng QUEUE_CAPACITY.\n");
        } else {
            enqueuedCount.incrementAndGet();
        }
    }

    /**
     * Chờ tất cả task trong queue hoàn thành (dùng khi bảo trì/shutdown).
     * Timeout tối đa 60 giây.
     */
    public void forceFlush() {
        Logger.warning("[DbAsyncTask] Đang flush hàng đợi DB (" + queue.size() + " tasks)...\n");
        long deadline = System.currentTimeMillis() + 60_000;
        while (!queue.isEmpty() && System.currentTimeMillis() < deadline) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        Logger.success("[DbAsyncTask] Flush hoàn tất.\n");
    }

    /**
     * Dừng pool an toàn.
     */
    public void shutdown() {
        forceFlush();
        workers.shutdown();
        try {
            if (!workers.awaitTermination(30, TimeUnit.SECONDS)) {
                workers.shutdownNow();
            }
        } catch (InterruptedException e) {
            workers.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Trả về số lượng task còn trong queue.
     */
    public int getBacklog() {
        return queue.size();
    }

    private void workerLoop() {
        while (true) {
            try {
                Runnable task = queue.poll(500, TimeUnit.MILLISECONDS);
                if (task != null) {
                    try {
                        task.run();
                    } catch (Exception e) {
                        Logger.logException(DbAsyncTask.class, e, "Lỗi thực thi DB task");
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
