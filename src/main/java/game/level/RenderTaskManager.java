package game.level;

import java.util.concurrent.ConcurrentLinkedQueue;

public class RenderTaskManager {
    private static final ConcurrentLinkedQueue<Runnable> renderQueue = new ConcurrentLinkedQueue<>();

    public static void submitRenderTask(Runnable task) {
        renderQueue.add(task);
    }

    public static void executeRenderTasks() {
        while (!renderQueue.isEmpty()) {
            Runnable task = renderQueue.poll();
            if (task != null) {
                task.run();
            }
        }
    }
}