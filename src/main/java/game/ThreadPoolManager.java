package game;

import java.util.concurrent.ForkJoinPool;

public class ThreadPoolManager {
	
    private static ThreadPoolManager instance;
    private final ForkJoinPool forkJoinPool;

    // Приватный конструктор (Singleton)
    private ThreadPoolManager() {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        this.forkJoinPool = new ForkJoinPool(availableProcessors);
    }

    // Метод для получения единственного экземпляра менеджера
    public static synchronized ThreadPoolManager getInstance() {
        if (instance == null) {
            instance = new ThreadPoolManager();
        }
        return instance;
    }

    // Метод для отправки задачи на выполнение
    public void submitTask(Runnable task) {
        forkJoinPool.submit(task);
    }

    // Метод для получения состояния пула потоков
    public String getPoolStatus() {
        return String.format(
                "Parallelism: %d, Active Threads: %d, Queued Tasks: %d",
                forkJoinPool.getParallelism(),
                forkJoinPool.getActiveThreadCount(),
                forkJoinPool.getQueuedTaskCount()
        );
    }

    // Метод для безопасного завершения пула потоков
    public void shutdown() {
        forkJoinPool.shutdown();
    }
}
